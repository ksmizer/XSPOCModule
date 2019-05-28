package com.avadine.lego.collector;

import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.TagManager;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointTask extends RecursiveTask {

    private List<Point> points;
    private List<Point> phantomPoints;
    private List<TagPath> tagPaths;

    private TagManager tagManager;
    private TagPathParser parser = new TagPathParser();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Integer threshold;

    public PointTask(TagManager newTagManager, List<Point> pointList, List<Point> phantomPointList, List<TagPath> tagPathsList) {
        tagManager = newTagManager;
        points = pointList;
        phantomPoints = phantomPointList;
        tagPaths = tagPathsList;
    }
    
    @Override
    public List<TagPath> compute() {

        //determine threshold (arbitrary)
        threshold = 20;
        //if met, continue
        if (points.size() < threshold) {
            Iterator<Point> iterator = points.iterator();
            while (iterator.hasNext()) {
                Point point = iterator.next();
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
        } else { 
            //if not, split again
            int length = points.size();
            int split = length/2;
            PointTask left = new PointTask(tagManager,points.subList(0,split),phantomPoints.subList(0,split),tagPaths.subList(0,split));
            PointTask right = new PointTask(tagManager,points.subList(split,length),phantomPoints.subList(split,length),tagPaths.subList(split,length));
            invokeAll(left,right);
        }

        return null;
        
    }
}