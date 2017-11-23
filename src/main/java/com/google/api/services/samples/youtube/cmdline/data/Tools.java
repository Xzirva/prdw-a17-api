package com.google.api.services.samples.youtube.cmdline.data;

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
    }
}
