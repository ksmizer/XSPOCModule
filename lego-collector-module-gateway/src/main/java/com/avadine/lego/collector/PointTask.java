package com.avadine.lego.collector;

import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;

import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.TagManager;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;

public class PointTask extends RecursiveTask {

    private List<Point> points;
    private List<Point> leftPoints;
    private List<Point> rightPoints;
    private List<Point> phantomPoints;
    private List<TagPath> tagPaths;
    private Integer threshold;
    private TagManager tagManager;
    private TagPathParser parser = new TagPathParser();

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
        } else { //if not, split again
            int length = points.size();
            int split = length/2;
            PointTask left = new PointTask(tagManager,points.subList(0,split),phantomPoints.subList(0,split),tagPaths.subList(0,split));
            PointTask right = new PointTask(tagManager,points.subList(split,length),phantomPoints.subList(split,length),tagPaths.subList(split,length));
            invokeAll(left,right);
        }


       



        


        return null;
        
    }
}