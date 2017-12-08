package com.google.api.services.samples.youtube.cmdline.data;

import com.google.api.client.util.DateTime;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sun.javafx.util.Utils.split;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.*;

public class Tools {
    
    public static String parseTopicCategory(String topiccategory) {
        String[] tmp = topiccategory.split("/");
        try {
            return tmp[tmp.length - 1];
        } catch (Exception e) {
            return "NA";
        }
    }
    
    
    public static void separateTime(Timestamp time){
        //Timestamp publishTime = new Timestamp(video.getSnippet().getPublishedAt().getValue());
        //convert the time to type long
        long timelong = time.getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timelong);
        int year;
        int month;
        int day;
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        //faut enregistrer les variables locales year, month and day 
    }
    
    
    public static void main(String[] args){
        System.out.println(parseTopicCategory("https://en.wikipedia.org/wiki/Society"));
        writeLog();
        System.out.println(getDateTime());
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
        return getDateTime("api.log");
    }

    public static DateTime getDateTimeComments(){
        return getDateTime("api_comments.log");
    }

    private static DateTime getDateTime(String filename){
        try {
            BufferedReader logReader = new BufferedReader(new FileReader(filename));
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

    public static String convertToUTF8(String str) {
        try{
            byte[] bytes = str.getBytes();
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("Couldn't encode: " + str);
            return str;
        }
    }
}
