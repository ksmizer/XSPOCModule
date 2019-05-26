package com.avadine.lego.collector;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.TagManager;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;

public class CollectorRunnable implements Runnable {

    private CollectorDatabaseConnection dc = new CollectorDatabaseConnection();

    private TagManager tagManager;
    // private GatewayHook gh = new GatewayHook();
    private TagPathParser parser = new TagPathParser();
    // private TagManager tagManager = gh.getTagManager();
    private int collectorId;

    public CollectorRunnable(TagManager newTagManager) { 
        tagManager = newTagManager;
        collectorId = 171;
    }

    public CollectorRunnable(TagManager newTagManager, int newCollectorId) { 
        tagManager = newTagManager;
        collectorId = newCollectorId;
    }

    @Override
    public void run() {
        Date startTime = new Date();
        List<Point> points = new ArrayList<Point>();
        List<Point> phantomPoints = new ArrayList<Point>();
        List<TagPath> tagPaths = new ArrayList<TagPath>();
        List<QualifiedValue> values = new ArrayList<QualifiedValue>();
        List<QualifiedValue> badQualities = new ArrayList<QualifiedValue>();
        PointToInsert insert;
    

        dc.getConnection("legoread", "legoread", "VNSQL01", "2500", "COLLECTOR");

        // Gather all Collector points for provided Collector Id
        points = dc.getPoints(collectorId);

        // Iterate through all points and check if they exist on gateway
        // Possible multi-threading opportunity
        // Would need threshold to split up points into different forks
        Iterator iterator = points.iterator();
        while (iterator.hasNext()) {
            Point point = (Point)iterator.next();
            String path = point.TagPath;
            if (path.startsWith("[")) {
                try {
                    TagPath tagPath = parser.parse(path);
                    if (tagManager.getTag(tagPath) == null) {
                        phantomPoints.add(point);
                        iterator.remove();
                    } else {
                        tagPaths.add(tagPath);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        // End possibility
        
        // Remove points that don't exist from List OLD
        // for (Point point : phantomPoints) {
        //     points.remove(point);
        // }

        // Read all points for gateway
        values = tagManager.read(tagPaths);

        // Iterate through all points and check if they are good quality
        for (int i = 0; i < values.size(); i++) { // Keoni: For loops should not use {length of list} - 1 unless we want to leave out the last value
            if (!values.get(i).getQuality().isGood()) {
                // Logging opportunity
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
            insert = new PointToInsert();
            insert.Id = points.get(i).Id;
            insert.EffectiveDate = points.get(i).EffectiveDate;
            insert.PointValue = values.get(i).getValue().toString();
            insert.Duration = duration;
            dc.insertPoint(insert);
        }
        dc.closeConnection(); // need this so we don't have a memory leak
    }
}