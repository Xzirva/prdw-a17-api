package com.google.api.services.samples.youtube.cmdline;

import java.sql.*;

public class AsterDatabaseInterface {
    public static Connection conn;
    static {
        String connurl = "jdbc:ncluster://192.168.100.220:2406/beehive,tmode=ANSI,charset=UTF8?allowMultiQueries=true";

        try {
            Class.forName("com.asterdata.ncluster.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(connurl, "beehive", "beehive");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static Connection connect() throws ClassNotFoundException, SQLException {
        return conn;
    }
}
