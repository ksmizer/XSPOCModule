package com.avadine.lego.collector;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.inductiveautomation.ignition.common.TypeUtilities;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.CommonQualities;
import com.inductiveautomation.ignition.common.model.values.Quality;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataQuality;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.sqltags.model.types.ExtendedTagType;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagEditingFlags;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagType;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.common.sqltags.model.TagManager;
import com.inductiveautomation.ignition.gateway.sqltags.simple.ProviderConfiguration;
import com.inductiveautomation.ignition.gateway.sqltags.simple.SimpleTagProvider;
import com.inductiveautomation.ignition.gateway.sqltags.simple.WriteHandler;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;

public class CollectorRunnable implements Runnable {

    private CollectorDatabaseConnection dc = new CollectorDatabaseConnection();
    private Connection conn = dc.getConnection("legoread", "legoread", "VNSQL01", "2500", "COLLECTOR");

    private TagManager tagManager;
    // private GatewayHook gh = new GatewayHook();
    private TagPathParser parser = new TagPathParser();
    // private TagManager tagManager = gh.getTagManager();

    public CollectorRunnable(TagManager newTagManager) { 
        tagManager = newTagManager;
    }

    @Override
    public void run() {
        List<Point> points = new ArrayList<Point>();
        List<Point> phantomPoints = new ArrayList<Point>();
        List<PointToInsert> insertionPoints = new ArrayList<PointToInsert>();
        List<TagPath> tagPaths = new ArrayList<TagPath>();
        List<QualifiedValue> values = new ArrayList<QualifiedValue>();
        List<QualifiedValue> badQualities = new ArrayList<QualifiedValue>();

        points = dc.getPoints(171);

        for (Point point : points) {
            try {
                TagPath tagPath = parser.parse(point.TagPath);
                if (tagManager.getTag(tagPath) == null) {
                    phantomPoints.add(point);
                } else {
                    tagPaths.add(tagPath);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for (Point point : phantomPoints) {
            points.remove(point);
        }

        values = tagManager.read(tagPaths);

        for (int i = 0; i < values.size()-1; i++) {
            if (!values.get(i).getQuality().isGood()) {
                badQualities.add(values.get(i));
                points.remove(i);
            }
        }
        
        for (QualifiedValue badValue: badQualities) {
            values.remove(badValue);
        }
        
        //call sproc
        
        for (int i = 0; i < points.size()-1; i++) {
            //construct PointToInsert objects
            int id = points.get(i).Id;
            //Date effectiveDate = new Date();
            String pointValue = values.get(i).getValue().toString();
            //int duration

            //PointToInsert insert = new PointToInsert(id, effectiveDate, pointValue, duration);
            //dc.insertPoint(insert);
        }


    }
}