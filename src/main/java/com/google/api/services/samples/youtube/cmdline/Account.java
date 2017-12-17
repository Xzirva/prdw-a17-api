package com.google.api.services.samples.youtube.cmdline;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.DateTime;
import com.google.api.services.samples.youtube.cmdline.data.Channels;
import com.google.api.services.samples.youtube.cmdline.data.Tools;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Account extends Thread {
    private String client_secrets_filepath;
    private String[] args;
    public static DateTime latestUpdate = Tools.getDateTime();

    public Account(String filepath, String[] args) {
        client_secrets_filepath = filepath;
        this.args = args;
    }


    public void run() {
        System.out.println("----------START with client_secret " + client_secrets_filepath + "----------");
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
//        System.out.println(client_secrets_filepath.substring(1));
//        System.out.println(client_secrets_filepath.substring(1).split("\\.")[0]);

        String account = client_secrets_filepath.substring(1).replace("_", "").split("\\.")[0];
        System.out.println(account);
        Channels channelsThread = new Channels(args, client_secrets_filepath, account
                );
        channelsThread.run();
    }
}
