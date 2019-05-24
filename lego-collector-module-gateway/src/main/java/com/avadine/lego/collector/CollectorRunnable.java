package com.avadine.lego.collector;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class CollectorRunnable implements Runnable {

    static CollectorDatabaseConnection dc = new CollectorDatabaseConnection();
    static Connection conn = dc.getConnection("legoread", "legoread", "VNSQL01", "2500", "COLLECTOR");

    public static void main(String[] args) {
        Runnable runnable = new CollectorRunnable();
        Thread collector = new Thread(runnable);
        collector.start();
    }


    @Override
    public void run() {
        List<Point> points = new ArrayList<Point>();
        points = dc.getPoints(171);
    }
}