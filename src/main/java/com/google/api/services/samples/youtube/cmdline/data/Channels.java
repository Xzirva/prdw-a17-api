package com.google.api.services.samples.youtube.cmdline.data;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.AsterDatabaseInterface;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Channels {
    /**
     * Define a global instance of a YouTube object, which will be used to make
     * YouTube Data API requests.
     */
    private static YouTube youtube;
    private static int count = 0;
    private static Connection conn;
    static {
        try {
            conn = AsterDatabaseInterface.connect();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // This OAuth 2.0 access scope allows for full read/write access to the
        // authenticated user's account and requires requests to use an SSL connection.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");

        Credential credential = null;
        try {
            credential = Auth.authorize(scopes, "channels");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This object is used to make YouTube Data API requests.
        youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                .setApplicationName("youtube-cmdline-channels-sample").build();

    }

    public static Channel insertChannel(String channelName) {
        try {

            // Prompt the user for the ID of a channel to comment on.
            // Retrieve the channel ID that the user is commenting to.
            System.out.println("Inserting channel: " + channelName);
            Channel channel = getChannel(channelName);
            String query = " INSERT INTO \"prdwa17_staging\".\"channels\" (\"id\", \"title\", \"description\", \"publishedat\", \"viewcount\", \"commentcount\", \"subscribercount\", " +
                    "\"videocount\", \"topiccategory_1\", \"topiccategory_2\", \"topiccategory_3\", \"keywords\", \"fetchedat\") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, channel.getId());
            ps.setString(2, channel.getSnippet().getTitle() );
            ps.setString(3, channel.getSnippet().getDescription());
            ps.setTimestamp(4, new Timestamp(channel.getSnippet().getPublishedAt().getValue()));
            ps.setLong (5, Long.parseLong(channel.getStatistics().getViewCount().toString()));
            ps.setLong(6, Long.parseLong(channel.getStatistics().getCommentCount().toString()));
            ps.setLong(7, Long.parseLong(channel.getStatistics().getSubscriberCount().toString()));
            ps.setLong (8, Long.parseLong(channel.getStatistics().getVideoCount().toString()));
            List<String> categories = channel.getTopicDetails().getTopicCategories();
            for(int i = 0; i<3; i++) {
                try {
                    ps.setString(9+i, categories.get(i));
                } catch (IndexOutOfBoundsException e) {
                    ps.setString(9+i, "");
                    ps.setString(9+i, "");
                }

            }
            ps.setString(12, channel.getBrandingSettings().getChannel().getKeywords());
            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(new Date().getTime());
            ps.setTimestamp(13, currentTimestamp);
            boolean res = ps.execute();
//            System.out.println("inserted: " + res);
            return channel;
        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode()
                    + " : " + e.getDetails().getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
        return null;

    }
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for(String ch : args){
            try {
                Channel channel = insertChannel(ch);
                System.out.println("New Thread to load videos of channel " + ch + " -- " +  channel.getId());
                Videos videoThread = new Videos(channel.getId(), ch);
                videoThread.start();
            } catch (NullPointerException e) {
                System.out.println(new Date() + " - NullPointerException with channel" + ch);
            }
        }
        stopwatch.stop(); // optional

        long millis = stopwatch.elapsed(MILLISECONDS);
        System.out.println("------------------Fetched Channels within: " + stopwatch + "-------------------");

    }

    private static Channel getChannel(String username) throws Exception {
        String parts = "snippet,statistics,contentDetails,topicDetails,brandingSettings";
        ChannelListResponse channelResponse = youtube.channels(). list(parts).setForUsername(username).execute();
        System.out.println("Fetching data about channel: " + username);
        return channelResponse.getItems().get(0);
    }
}
