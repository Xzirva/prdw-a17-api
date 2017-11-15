package com.google.api.services.samples.youtube.cmdline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Reader {
    /*
  * Prompt the user to enter a channel ID. Then return the ID.
  */
    private static String getChannelId() throws IOException {

        String channelId = "";

        System.out.print("Please enter a channel id: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        channelId = bReader.readLine();

        return channelId;
    }

    /*
     * Prompt the user to enter a video ID. Then return the ID.
     */
    private static String getVideoId() throws IOException {

        String videoId = "";

        System.out.print("Please enter a video id: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        videoId = bReader.readLine();

        return videoId;
    }
}
