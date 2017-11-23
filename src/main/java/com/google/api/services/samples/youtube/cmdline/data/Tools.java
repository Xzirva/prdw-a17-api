package com.google.api.services.samples.youtube.cmdline.data;

import com.google.api.client.util.DateTime;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sun.javafx.util.Utils.split;

public class Tools {
    public static String parseTopicCategory(String topiccategory) {
        String[] tmp = topiccategory.split("/");
        try {
            return tmp[tmp.length - 1];
        } catch (Exception e) {
            return "NA";
        }
    }
    public static void main(String[] args){
        System.out.println(parseTopicCategory("https://en.wikipedia.org/wiki/Society"));
        writeLog();
        System.out.println(getDateTime().toString());
    }
    public static void writeLog(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        Date date = new Date();
        try {
            BufferedWriter logWriter = new BufferedWriter(new FileWriter(" api.log"));
            logWriter.write(dateFormat.format(date));
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static DateTime getDateTime(){
        try {
            BufferedReader logReader = new BufferedReader(new FileReader(" api.log"));
            String readDate = logReader.readLine();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
            Date tmpDate= sdf.parse(readDate);
            DateTime googleDate = new DateTime(tmpDate);
            logReader.close();
            return googleDate;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
