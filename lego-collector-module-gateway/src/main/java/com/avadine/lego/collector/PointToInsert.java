package com.avadine.lego.collector;

// import java.text.DateFormat;
// import java.text.SimpleDateFormat;
import java.sql.Date;
/**
 * PointToInsert
 */
public class PointToInsert {
    public int Id;
    public Date EffectiveDate;
    public String PointValue;
    public int Duration;

    public PointToInsert(int id, Date effectiveDate, String pointValue, int duration) {
        Id = id;
        EffectiveDate = effectiveDate;
        PointValue = pointValue;
        Duration = duration;
    }

    // **No longer Needed**
    // public String[] getPoint(){
    //     String[] point = new String[4];
    //     point[0] = Integer.toString(Id);
    //     DateFormat dateFormat = new SimpleDateFormat("YYYYMMDD HH:MM:SS"); // Needed for datetime conversion in SQL 
    //     point[1] = dateFormat.format(EffectiveDate);
    //     point[2] = PointValue;
    //     point[3] = Integer.toString(Duration);
    //     return point;
    // }
}
