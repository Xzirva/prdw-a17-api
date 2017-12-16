package com.google.api.services.samples.youtube.cmdline;

import com.google.api.client.util.DateTime;
import com.google.api.services.samples.youtube.cmdline.data.Channels;
import com.google.api.services.samples.youtube.cmdline.data.Tools;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public class Main {
    public static DateTime latestUpdate = Tools.getDateTime();
    public static DateTime latestUpdateComments = Tools.getDateTimeComments();
    public static boolean fetchComments = false;
    public static java.sql.Timestamp thisTime = new java.sql.Timestamp(new Date().getTime());

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        while(true){
            System.out.println("--------------------------------- START AT : " + new Date() + "----------------------------");
            System.out.println("--------------------------------- Fetching data published after: " + Tools.getDateTime() + "-------");
            //String[] ch = {"FoxNewsChannel"};

            int noOfDays = 7; //i.e two weeks
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -noOfDays);
            Date dateBefore7Days = calendar.getTime();
            System.out.println("Calendar Date: " + dateBefore7Days.getTime());
            System.out.println("Latest Update: " + latestUpdate);
            if (latestUpdate.getValue() < dateBefore7Days.getTime()) {
                System.out.println("...........Setting threshold date for next download(channels + videos)..........");
                Tools.writeLog(dateBefore7Days);
                latestUpdate = Tools.getDateTime();
            } else {
                System.out.println("Keep fetching data for videos published after " + latestUpdate);
            }

            if(Main.latestUpdateComments.getValue() < dateBefore7Days.getTime()) {
                System.out.println("...........Setting threshold date for next download(channels + videos)..........");
                Tools.writeLogComments();
                fetchComments = true;
            }
            System.out.println("FetchCommentsFalse: " + fetchComments);
            Channels.main(args);
            try {
                System.out.println("----------------------------------------------------");
                System.out.println("Sleep before next fetch in 15 minutes");
                System.out.println("----------------------------------------------------");
                Thread.sleep(20 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //System.out.println("CHarset: " + Charset.defaultCharset());
/*        try{
            Connection conn = AsterDatabaseInterface.connect();
            PreparedStatement ps = conn.prepareStatement("drop table prdwa17_staging.tf_idf_out; create table " +
                    "prdwa17_staging.tf_idf_out DISTRIBUTE BY HASH(docid) AS " +
                    "select * from tf_idf (\n" +
                    "ON TF ( ON  prdwa17_staging.parsed_titles_descriptions partition " +
                    "by docid) as tf PARTITION BY term\n" +
                    "ON (select count(distinct docid) " +
                    "from prdwa17_staging.parsed_titles_descriptions\n" +
                    ") AS doccount DIMENSION\n" +
                    ");");
            PreparedStatement ps = conn.prepareStatement("select * from tf_idf (ON tf " +
                    "(ON prdwa17_staging. parsed_descriptions partition by videoid)" +
                    " AS tf PARTITION BY term ON (select count(distinct videoid) " +
                    "from prdwa17_staging.parsed_descriptions) AS videoscount DIMENSION");
            System.out.println("Sql Result: " + ps.execute());
            ResultSet res = ps.getResultSet();
        }catch (ClassNotFoundException e) {
            System.out.println("Connection failed");
        }*/
    }
}
