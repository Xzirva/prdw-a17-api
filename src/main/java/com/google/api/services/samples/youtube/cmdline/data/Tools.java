package com.google.api.services.samples.youtube.cmdline.data;

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
    }
}
