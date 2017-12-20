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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Main {
    public static boolean fetchComments = false;
    public static DateTime latestUpdateComments = Tools.getDateTimeComments();

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {

        System.out.println(args[args.length - 1]);
        if(args[args.length - 1].equals("--comments")) {
            System.out.println("---------------Start Fetching Comments----------------");
            String[] channels = new String[args.length - 1];

            System.arraycopy(args, 0, channels, 0, args.length - 1);
            Account accountThread = new Account("/client_secrets_lr.json", channels);
            accountThread.run();
            fetchComments = true;
        }else {

            System.out.println("--------------------------------- START AT : " + new Date() + "----------------------------");
            System.out.println("--------------------------------- Fetching data published after: " + Tools.getDateTime() + "-------");
            //String[] ch = {"FoxNewsChannel"};

            String[] filepaths = new String[]
                    {
                            "/client_secrets.json",
                            "/client_secrets_mr.json",
                            "/client_secrets_qr.json"
                    };

            while (true) {
                for (String file : filepaths) {
                    Account accountThread = new Account(file, args);
                    accountThread.run();
                    try {
                        System.out.println("----------------------------------------------------");
                        System.out.println("Sleep before next fetch in 15 minutes");
                        System.out.println("----------------------------------------------------");
                        Thread.sleep(15 * 60 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

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
