package com.google.api.services.samples.youtube.cmdline;

import com.google.api.client.util.DateTime;
import com.google.api.services.samples.youtube.cmdline.data.Channels;
//import com.google.api.services.samples.youtube.cmdline.data.CommentThreads;
import com.google.api.services.samples.youtube.cmdline.data.Tools;
//import com.google.api.services.youtube.model.Comment;
//import com.google.api.services.youtube.model.CommentThread;
//import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.io.IOException;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
import java.sql.SQLException;
//import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
//import java.util.List;
//import java.util.UUID;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        System.out.println("--------------------------------- START AT : "  + new Date() + "----------------------------");
        System.out.println("--------------------------------- Fetching data published after: " + Tools.getDateTime() + "----------------------------");
        //String[] ch = {"CNN"};
        Channels.main(args);
        DateTime latestUpdate = Tools.getDateTime();
        int noOfDays = 7; //i.e two weeks
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, -noOfDays);
        if(latestUpdate.getValue() < calendar.getTime().getTime()) {
            System.out.println("...........Setting threshold date for next download..........");
            Tools.writeLog();
        }
        System.out.println(Tools.getDateTime());
    }

}
