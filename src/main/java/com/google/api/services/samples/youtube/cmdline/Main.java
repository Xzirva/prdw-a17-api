package com.google.api.services.samples.youtube.cmdline;

import com.google.api.services.samples.youtube.cmdline.data.Channels;
import com.google.common.base.Stopwatch;

import java.sql.SQLException;
import java.util.Date;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        System.out.println("--------------------------------- START AT : "  + new Date() + "----------------------------");
        //, "cbcnews", "ibnlive", "CNNMoney"
        Channels.main(args);
    }

}
