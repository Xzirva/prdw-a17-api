package com.google.api.services.samples.youtube.cmdline.data;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.client.util.DateTime;
import com.google.api.services.samples.youtube.cmdline.AsterDatabaseInterface;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.google.common.collect.Lists;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class Videos {

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
            credential = Auth.authorize(scopes, "commentthreads");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This object is used to make YouTube Data API requests.
        youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                .setApplicationName("youtube-cmdline-commentthreads-sample").build();

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
            String channelId = "UCupvZG-5ko_eiXAupbDfxWw";
            System.out.println("You chose " + channelId + " to subscribe.");

            // Prompt the user for the ID of a video to comment on.
            // Retrieve the video ID that the user is commenting to.
            String videoId = "AZ8mB-eudv0";
            System.out.println("You chose " + videoId + " to subscribe.");

            List<Video> channelComments = getVideos(channelId);
            StringBuilder prettyStringToSave = new StringBuilder("");
            BufferedWriter out = new BufferedWriter(new FileWriter("fetched-data"+"CNN"+"videos"+ System.nanoTime()+".json"));
            for(Video c : channelComments) {
                prettyStringToSave.append(c.toPrettyString());
            }
            out.write(prettyStringToSave.toString());
            if (channelComments.isEmpty()) {
                System.out.println("Can't get channel comments.");
            } else {
                // Print information from the API response.
                System.out
                        .println("\n================== Returned Channel Comments" + channelComments.size() + " ==================\n");

//                for (Video channelComment : channelComments) {
//                    snippet = channelComment.getSnippet().getTopLevelComment()
//                            .getSnippet();
//                    System.out.println("  - Author: " + snippet.getAuthorDisplayName());
//                    System.out.println("  - Comment: " + snippet.getTextDisplay());
//                    System.out.println("  - Likes: " + snippet.getLikeCount());
//                    System.out.println("  - Likes: " + snippet.getPublishedAt());
//                    System.out
//                            .println("\n-------------------------------------------------------------\n");
//                }
            }

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

    private static List<Video> getVideos(String channelId, DateTime publishedDateTime) throws Exception {
        YouTube.Search.List playlistItemRequest = youtube.search ().list("id").setChannelId(channelId).setType("video").setPublishedAfter(publishedDateTime);
        String parts = "snippet,statistics,contentDetails,topicDetails";
        String nextToken = "";
        System.out.println("Fetching Videos. Channel: " + channelId);
        Stack<String> video_ids = new Stack<String>();
        Joiner stringJoiner = Joiner.on(',');
        List<Video> videos = new ArrayList<Video>();
//        new Video().getSnippet().getP
        // Call the API one or more times to retrieve all items in the
        // list. As long as the API response returns a nextPageToken,
        // there are still more items to retrieve.
        do {
            playlistItemRequest.setPageToken(nextToken);
            playlistItemRequest.setFields("items(id),nextPageToken,pageInfo");
            SearchListResponse playlistItemResult = playlistItemRequest.execute();
            System.out.println("Total Results: " + playlistItemResult.getPageInfo().getTotalResults() + "/" +
                    "\nCurrentPageResult: " + playlistItemResult.getPageInfo().getResultsPerPage());
            List<SearchResult> items = playlistItemResult.getItems();
            for(SearchResult item : items) {
//                if(video_ids.size() < 50)
                video_ids.add(item.getId().getVideoId());
//                else {
            }
            String videoId = stringJoiner.join(video_ids);
            // Call the YouTube Data API's youtube.videos.list method to
            // retrieve the resources that represent the specified videos.
            String videoNextToken = "";
            do {
                YouTube.Videos.List listVideosRequest = youtube.videos().list(parts).setId(videoId);
                listVideosRequest.setPageToken(videoNextToken);
                VideoListResponse listResponse = listVideosRequest.execute();
                videos.addAll(listResponse.getItems());
                videoNextToken = listResponse.getNextPageToken();
                video_ids.clear();
//                }
            }while(videoNextToken != null);
//            }

            nextToken = playlistItemResult.getNextPageToken();
        } while (nextToken != null);
        return videos;
    }

    public static void insertVideos(String channelId) {
        try {

            // Prompt the user for the ID of a channel to comment on.
            // Retrieve the channel ID that the user is commenting to.
            System.out.println("Inserting videos from channel: " + channelId);
            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            List<Video> videos = getVideos(channelId, new DateTime(cal.getTime()));
            Connection conn = AsterDatabaseInterface.connect();
            String query = " INSERT INTO \"prdwa17_staging\".\"videos\" (\"id\", \"channelid\", \"title\", \"description\", \"publishedat\", \"viewcount\", \"commentcount\", \"likecount\", \"dislikecount\", \"favoritecount\"," +
                    " \"subscribercount\", \"videocount\", \"categoryid\", \"topiccategory_1\", \"topiccategory_2\", \"topiccategory_3\", \"keywords\") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
                }

            }
            ps.setString(12, channel.getBrandingSettings().getChannel().getKeywords());
            System.out.println(ps.toString());
            boolean res = ps.execute();
            System.out.println("inserted: " + res);
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
}
