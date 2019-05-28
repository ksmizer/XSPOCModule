package com.avadine.lego.collector;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date; 
import java.util.concurrent.ForkJoinPool;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.TagManager;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simpleorm.dataset.SQuery;

public class CollectorRunnable implements Runnable {

    private CollectorDatabaseConnection dc = new CollectorDatabaseConnection();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GatewayContext context;
    private TagManager tagManager;
    // private TagPathParser parser = new TagPathParser();
    private int collectorId;

    protected List<Point> points;
    protected List<Point> phantomPoints;
    protected List<TagPath> tagPaths;

    public CollectorRunnable(GatewayContext newContext) { 
        context = newContext;
        tagManager = context.getTagManager();
        collectorId = 171;
    }

    public CollectorRunnable(GatewayContext newContext, int newCollectorId) { 
        context = newContext;
        tagManager = context.getTagManager();
        collectorId = newCollectorId;
    }

    //getters and setters
    // public List<Point> getPoints() {
    //     return points;
    // }

    // public List<Point> getPhantomPoints() {
    //     return phantomPoints;
    // }

    // public List<TagPath> getTagPaths() {
    //     return tagPaths;
    // }

    public void set(List<Point> points, List<Point> phantomPoints, List<TagPath> tagPaths) {
        this.points = points;
        this.phantomPoints = phantomPoints;
        this.tagPaths = tagPaths;
    }

    @Override
    public void run() {
        Date startTime = new Date();

        SQuery<CollectorConfiguration> query = new SQuery<CollectorConfiguration>(CollectorConfiguration.META);
        List<CollectorConfiguration> results = context.getPersistenceInterface().query(query);
        for (CollectorConfiguration result : results) {

            if (!result.getEnabled()) {
                continue;
            }

            String username = result.getUsername();
            String password = result.getPassword();
            String connectionString = result.getConnectionString();
            logger.info("Creating connection to database with the string: " + connectionString);
            try {

                points = new ArrayList<Point>();
                phantomPoints = new ArrayList<Point>();
                tagPaths = new ArrayList<TagPath>();

                dc.getConnection(username, password, connectionString);
    
                // Gather all Collector points for provided Collector Id               
                logger.info("Gathering points");
                points = dc.getPoints(collectorId);
                logger.info("Points gathered");
                // Iterate through all points and check if they exist on gateway

                ForkJoinPool pool = new ForkJoinPool();
                PointTask task = new PointTask(tagManager, points, phantomPoints, tagPaths);
                pool.invoke(task);

                // Possible multi-threading opportunity
                // Would need threshold to split up points into different forks
                // Iterator<Point> iterator = points.iterator();
                // while (iterator.hasNext()) {
                //     Point point = iterator.next();
                //     String path = point.TagPath;
                //     if (path.startsWith("[")) {
                //         try {
                //             TagPath tagPath = parser.parse(path);
                //             if (tagManager.getTag(tagPath) == null) {
                //                 phantomPoints.add(point);
                //                 iterator.remove();
                //             } else {
                //                 tagPaths.add(tagPath);
                //             }
                //         } catch (IOException e) {
                //             // TODO Auto-generated catch block
                //             e.printStackTrace();
                //         }
                //     }
                // }
                // End possibility
                
                // Remove points that don't exist from List OLD
                // for (Point point : phantomPoints) {
                //     points.remove(point);
                // }
                
                List<QualifiedValue> values = new ArrayList<QualifiedValue>();
                List<QualifiedValue> badQualities = new ArrayList<QualifiedValue>();

                // Read all points for gateway
                values = tagManager.read(tagPaths);
        
                // Iterate through all points and check if they are good quality
                for (int i = 0; i < values.size(); i++) { // Keoni: For loops should not use {length of list} - 1 unless we want to leave out the last value
                    if (!values.get(i).getQuality().isGood()) {
                        logger.warn("Collector Point: " + tagPaths.get(i) + " does not have Good quality. Skipping this tag.");
                        badQualities.add(values.get(i));
                        points.remove(i);
                    }
                }
                
                // Remove points that have bad qualities
                // for (QualifiedValue badValue: badQualities) {
                //     values.remove(badValue);
                // }
                values.removeAll(badQualities);
        
                //call sproc
                Date endTime = new Date();
                int duration = Math.toIntExact((endTime.getTime()-startTime.getTime())/1000);
        
                for (int i = 0; i < points.size(); i++) {
                    //construct PointToInsert objects
                    logger.info("Creating point to insert with the ID: " + points.get(i).Id);
                    PointToInsert insert = new PointToInsert();
                    insert.Id = points.get(i).Id;
                    insert.EffectiveDate = points.get(i).EffectiveDate;
                    insert.PointValue = values.get(i).getValue().toString();
                    insert.Duration = duration;
                    logger.info("Inserting point with new value: " + insert.PointValue);
                    // dc.insertPoint(insert);
                }
                logger.info("Closing connection");
                dc.closeConnection(); // need this so we don't have a memory leak
            } catch (NullPointerException e) {
                logger.error("Could not create connection", e);
            }
        }
    }
}