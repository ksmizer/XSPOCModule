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

    public CollectorRunnable(TagManager newTagManager) { 
        tagManager = newTagManager;
    }

    @Override
    public void run() {
        Date startTime = new Date();
        List<Point> points = new ArrayList<Point>();
        List<Point> phantomPoints = new ArrayList<Point>();
        List<TagPath> tagPaths = new ArrayList<TagPath>();
        List<QualifiedValue> values = new ArrayList<QualifiedValue>();
        List<QualifiedValue> badQualities = new ArrayList<QualifiedValue>();
        PointToInsert insert = new PointToInsert();
    

        // Gather all Collector points for provided Collector Id
        points = dc.getPoints(171);

        // Iterate through all points and check if they exist on gateway
        Iterator iterator = points.iterator();
        while (iterator.hasNext()) {
            Point point = (Point)iterator.next();
            try {
                TagPath tagPath = parser.parse(point.TagPath);
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
        
        // Remove points that don't exist from List
        for (Point point : phantomPoints) {
            points.remove(point);
        }

        // Read all points for gateway
        values = tagManager.read(tagPaths);

        // Iterate through all points and check if they are good quality
        for (int i = 0; i < values.size()-1; i++) {
            if (!values.get(i).getQuality().isGood()) {
                badQualities.add(values.get(i));
                points.remove(i);
            }
        }
        // Remove points that have bad qualities
        for (QualifiedValue badValue: badQualities) {
            values.remove(badValue);
        }
        
        //call sproc
        Date endTime = new Date();
        int duration = Math.toIntExact((endTime.getTime()-startTime.getTime())/1000);

        for (int i = 0; i < points.size()-1; i++) {
            //construct PointToInsert objects
            insert.Id = points.get(i).Id;
            insert.EffectiveDate = points.get(i).EffectiveDate;
            insert.PointValue = values.get(i).getValue().toString();
            insert.Duration = duration;
            dc.insertPoint(insert);
        }
    }
}