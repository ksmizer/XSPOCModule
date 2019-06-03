package com.avadine.xspoc.collector;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

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
    private TagPathParser parser = new TagPathParser();
    private List<Integer> collectorIds;

    public CollectorRunnable(GatewayContext newContext) { 
        context = newContext;
        tagManager = context.getTagManager();
        collectorIds = new ArrayList<Integer>();
    }

    public CollectorRunnable(GatewayContext newContext, List<Integer> newCollectorIds) { 
        context = newContext;
        tagManager = context.getTagManager();
        collectorIds = newCollectorIds;
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

                dc.getConnection(username, password, connectionString);
                // Gather all Collector points for provided Collector Id
                List<Point> points = new ArrayList<Point>();
                List<TagPath> tagPaths = new ArrayList<TagPath>();

                for (Integer collectorId : collectorIds) {
                    logger.info("Collector Id: " + Integer.toString(collectorId));
                    points.addAll(dc.getPoints(collectorId));
                }
                dc.resetCall();

                Iterator<Point> iterator = points.iterator();
                while (iterator.hasNext()) {
                    Point point = iterator.next();
                    String path = point.TagPath;
                    if (path.startsWith("[")) {
                        try {
                            TagPath tagPath = parser.parse(path);
                            if (tagManager.getTag(tagPath) == null) {
                                iterator.remove();
                            } else {
                                tagPaths.add(tagPath);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        iterator.remove();
                    }
                }
                    
                List<QualifiedValue> values = new ArrayList<QualifiedValue>();
                List<QualifiedValue> badQualities = new ArrayList<QualifiedValue>();

                // Read all points for gateway
                values = tagManager.read(tagPaths);

                //call sproc
                Date endTime = new Date();
                int duration = Math.toIntExact((endTime.getTime()-startTime.getTime())/1000);
                
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i).getQuality().isGood()) {
                        //construct PointToInsert objects
                        PointToInsert insert = new PointToInsert();
                        insert.Id = points.get(i).Id;
                        insert.EffectiveDate = points.get(i).EffectiveDate;
                        insert.PointValue = values.get(i).getValue().toString();
                        insert.Duration = duration;
                        logger.info("Inserting point with new value: " + insert.PointValue);
                        dc.insertPoint(insert);
                    } else {
                        badQualities.add(values.get(i));
                    }
                }
                logger.info("Closing connection");
            } catch (NullPointerException e) {
                logger.error("Could not create connection", e);
            }
            dc.closeConnection(); // need this so we don't have a memory leak
            
        }
    }
}