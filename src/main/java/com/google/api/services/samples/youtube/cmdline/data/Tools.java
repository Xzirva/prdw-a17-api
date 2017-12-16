package com.google.api.services.samples.youtube.cmdline.data;

import com.google.api.client.util.DateTime;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        //writeLog();
        System.out.println(getDateTime());
    }
    public static void writeLog(Date nextFetchDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        try {
            BufferedWriter logWriter = new BufferedWriter(new FileWriter("api.log"));
            logWriter.write(dateFormat.format(nextFetchDate));
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeLogComments(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        try {
            BufferedWriter logWriter = new BufferedWriter(new FileWriter("api-comments.log"));
            logWriter.write(dateFormat.format(new Date()));
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DateTime getDateTime(){
        return getDateTime("api.log");
    }

    public static DateTime getDateTimeComments(){
        return getDateTime("api-comments.log");
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
//        try{
//            byte[] bytes = str.getBytes();
//            return new String(bytes, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            System.out.println("Couldn't encode: " + str);
//            return str;
//        }
        return str;
    }
}
