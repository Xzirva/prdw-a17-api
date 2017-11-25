package com.google.api.services.samples.youtube.cmdline.data;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.client.util.DateTime;
import com.google.api.services.samples.youtube.cmdline.AsterDatabaseInterface;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.text.SimpleDateFormat;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Videos extends Thread {

    /**
     * Define a global instance of a YouTube object, which will be used to make
     * YouTube Data API requests.
     */
    private static Connection conn;
    private String channelId;
    private static YouTube youtube;
    private String channelName;
    private DateTime datePublishedAfter;
    public Videos (String channelId, String channelName, DateTime datePublishedAfter) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.datePublishedAfter = datePublishedAfter;
    }
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
            credential = Auth.authorize(scopes, "videos");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This object is used to make YouTube Data API requests.
        youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                .setApplicationName("youtube-cmdline-commentthreads-sample").build();

    }

    /**
     */
    // String channelId = "UCupvZG-5ko_eiXAupbDfxWw";
    public void run () {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();

            // Prompt the user for the ID of a channel to comment on.
            // Retrieve the channel ID that the user is commenting to.
            System.out.println("You chose " + channelId + " to subscribe.");
            insertVideos();
            stopwatch.stop(); // optional

            long millis = stopwatch.elapsed(MILLISECONDS);
            System.out.println("------------------Fetched videos within: " + stopwatch + "-------------------");

        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private List<Video> getVideos(String channelId, DateTime publishedDateTime) throws Exception {
        YouTube.Search.List playlistItemRequest = youtube.search ().list("id").setChannelId(channelId)
                .setType("video").setPublishedAfter(publishedDateTime);
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

    public void insertVideos() {
        try {

            // Prompt the user for the ID of a channel to comment on.
            // Retrieve the channel ID that the user is commenting to.
            System.out.println("Inserting videos from channel: " + channelName + " -- id: " + channelId);
            List<Video> videos = getVideos(channelId, datePublishedAfter);
            String query = "INSERT INTO \"prdwa17_staging\".\"videos\" " +
                    "(\"id\", \"channelid\", \"title\", \"description\", \"publishedat\"," +
                    " \"viewcount\", \"commentcount\", \"likecount\", \"dislikecount\", " +
                    "\"favoritecount\", \"categoryid\", \"topiccategory_1\", \"topiccategory_2\", " +
                    "\"topiccategory_3\", \"fetchedat\", \"screenshot\") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
 	
 
            PreparedStatement ps = conn.prepareStatement(query);
            for(Video video : videos){
                ps.setString(1, video.getId());
                ps.setString(2, video.getSnippet().getChannelId());
                ps.setString(3, video.getSnippet().getTitle());
                ps.setString(4, video.getSnippet().getDescription());
                ps.setTimestamp(5, new Timestamp(video.getSnippet().getPublishedAt().getValue()));
                ps.setLong (6, Long.parseLong(video.getStatistics().getViewCount().toString()));
                ps.setLong(7, Long.parseLong(video.getStatistics().getCommentCount().toString()));
                ps.setLong(8, Long.parseLong(video.getStatistics().getLikeCount().toString()));
                ps.setLong (9, Long.parseLong(video.getStatistics().getDislikeCount().toString()));
                ps.setLong (10, Long.parseLong(video.getStatistics().getFavoriteCount().toString()));
                ps.setString(11, video.getSnippet().getCategoryId());

                int i;
                List<String> categories;
                try {
                    categories = video.getTopicDetails().getTopicCategories();
                    for(i = 0;i<3; i++) {
                        try {
                            ps.setString(12+i, categories.get(i));
                        }catch (IndexOutOfBoundsException e) {
                            ps.setString(12+i, "");
                        }
                    }
                } catch (NullPointerException e) {
                    for(i = 0;i<3; i++) {
                        ps.setString(12+i, "");
                    }
                }

                java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(new Date().getTime());
                ps.setTimestamp(12+i, currentTimestamp);
                ps.setString(12+i+1, video.getId()+currentTimestamp.toString());
                //System.out.println(ps.toString());
                boolean res = ps.execute();
                //System.out.println("inserted video: " + res);

                System.out.println("Inserting tags of the video" + video.getId() + " and channel " + channelName + " into the table");
                String query1 = "INSERT INTO \"prdwa17_staging\".\"youtubetags\" (\"id\", \"title\", \"videoid\") VALUES (?,?,?)";
                PreparedStatement pstag = conn.prepareStatement(query1);
                List<String> tags = video.getSnippet().getTags();
                int nbtags = tags.size();
                for (i = 0; i < nbtags; i++){
                    try {
                            pstag.setString(1, UUID.randomUUID().toString());
                            pstag.setString(2, tags.get(i));
                            pstag.setString(3, video.getId());
                        
                    }catch (IndexOutOfBoundsException e) {
                        pstag.setString(1, "");
                        pstag.setString(2, "");
                        pstag.setString(3, "");
                    }
                }
                
                //System.out.println(pstag.toString());
                boolean res1 = pstag.execute();
                //System.out.println("inserted tag: " + res1);
                System.out.println("New Thread to load comments of video " + video.getId());
                CommentThreads commentThreadsThread = new CommentThreads(video.getId(), video.getSnippet().getTitle());
                commentThreadsThread.start();
                System.out.println("--------------------------------- END AT : "  + new Date() + " ----------------------------");

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
    

}

