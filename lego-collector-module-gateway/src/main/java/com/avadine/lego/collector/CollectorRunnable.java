package com.avadine.lego.collector;

// import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
// import java.util.Iterator;
import java.util.Date; 
import java.util.concurrent.ForkJoinPool;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.TagManager;
// import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;
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
                PointTask task = new PointTask(tagManager, startTime, points, phantomPoints, tagPaths);
                logger.info("Beginning collection tasks");
                pool.invoke(task);
                logger.info("Completed collection tasks");

                logger.info("Closing connection");
                dc.closeConnection(); // need this so we don't have a memory leak
            } catch (NullPointerException e) {
                logger.error("Could not create connection", e);
            }
        }
    }
}