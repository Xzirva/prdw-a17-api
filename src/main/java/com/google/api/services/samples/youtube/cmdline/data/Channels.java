package com.google.api.services.samples.youtube.cmdline.data;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.AsterDatabaseInterface;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.common.collect.Lists;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class Channels {
    /**
     * Define a global instance of a YouTube object, which will be used to make
     * YouTube Data API requests.
     */
    private static YouTube youtube;
    static {
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

    /**
     * Create, list and update top-level channel and video comments.
     *
     * @param args command line args (not used).
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try {

            // Prompt the user for the ID of a channel to comment on.
            // Retrieve the channel ID that the user is commenting to.
            String channelName = "CNN";
            System.out.println("You chose " + channelName);

            Channel channel = getChannel(channelName);
            Connection conn = AsterDatabaseInterface.connect();
            String query = "INSERT INTO prdwa17_staging.channel" +
                    " ( code_channel, t_channelid, t_channeldescription, " +
                    "t_channelcustomurl, t_channelcommentscount, t_channelsubscribercount, t_channelhiddensubscribercount, t_channelvideocount, t_channelkeywords) VALUES(?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, channel.hashCode());
            ps.setString(2, channel.getSnippet().getTitle() );
            ps.setString(3, channel.getSnippet().getDescription());
            ps.setString(4, channel.getSnippet().getCustomUrl());
            ps.setLong(5, Long.parseLong(channel.getStatistics().getCommentCount().toString()));
            ps.setLong(6, Long.parseLong(channel.getStatistics().getSubscriberCount().toString()));
            ps.setInt(7, 0);
            ps.setLong (8, Long.parseLong(channel.getStatistics().getVideoCount().toString()));
            ps.setString(9,channel.getBrandingSettings().getChannel().getKeywords());
            System.out.println(ps.toString());
            boolean res = ps.execute();
            System.out.println("inserted: " + res);
//            BufferedWriter out = new BufferedWriter(new FileWriter("fetched-data-"+"channel-"+channelName+".json"));
//            System.out.println(channel.toPrettyString());
//            out.write(channel.toPrettyString());
//            out.close();
//            if (channel.getId() == null) {
//                System.out.println("Can't get channel comments.");
//            } else {
//                // Print information from the API response.
//                System.out
//                        .println("\n================== Channel ID: " + channel.getId() + " ==================\n");
//
////                for (Channel channelComment : channelComments) {
////                    snippet = channelComment.getSnippet().getTopLevelComment()
////                            .getSnippet();
////                    System.out.println("  - Author: " + snippet.getAuthorDisplayName());
////                    System.out.println("  - Comment: " + snippet.getTextDisplay());
////                    System.out.println("  - Likes: " + snippet.getLikeCount());
////                    System.out.println("  - Likes: " + snippet.getPublishedAt());
////                    System.out
////                            .println("\n-------------------------------------------------------------\n");
////                }
//
//                // nCluster pour le transfert de données
//            }
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
    }

    private static Channel getChannel(String username) throws Exception {
        String parts = "snippet,statistics,contentDetails,topicDetails,brandingSettings";
        ChannelListResponse channelResponse = youtube.channels(). list(parts).setForUsername(username).execute();
        System.out.println("Fetching data about channel: " + username);
        return channelResponse.getItems().get(0);
    }
}
