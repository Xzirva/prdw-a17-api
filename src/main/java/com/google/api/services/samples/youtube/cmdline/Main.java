package com.google.api.services.samples.youtube.cmdline;

import com.google.api.client.util.DateTime;
import com.google.api.services.samples.youtube.cmdline.data.Channels;
import com.google.api.services.samples.youtube.cmdline.data.Tools;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public class Main {
    public static DateTime latestUpdate = Tools.getDateTime();
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        System.out.println("--------------------------------- START AT : "  + new Date() + "----------------------------");
        System.out.println("--------------------------------- Fetching data published after: " + Tools.getDateTime() + "-------");
        String[] ch = {"FoxNewsChannel"};

        int noOfDays = 7; //i.e two weeks
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, -noOfDays);
        System.out.println("Calendar Date: " + calendar.getTime().getTime());
        System.out.println("Latest Update: " + latestUpdate);
        if(latestUpdate.getValue() < calendar.getTime().getTime()) {
            System.out.println("...........Setting threshold date for next download..........");
            Tools.writeLog();
        } else {
            System.out.println("Keep fetching data for videos published after " + latestUpdate);
        }
        Channels.main(ch);
        //System.out.println("CHarset: " + Charset.defaultCharset());
    }
}
