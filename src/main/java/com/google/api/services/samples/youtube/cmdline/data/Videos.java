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
//            System.out.println("Total Results: " + playlistItemResult.getPageInfo().getTotalResults() + "/" +
//                    "\nCurrentPageResult: " + playlistItemResult.getPageInfo().getResultsPerPage());
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
            int count = videos.size();
            if(count % 50 == 0)
                System.out.println("Have Fetched" + count + " videos for channel: " + channelId);

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
            int count = 0;
            for(Video video : videos){
                ps.setString(1, video.getId());
                ps.setString(2, video.getSnippet().getChannelId());
                ps.setString(3, video.getSnippet().getTitle());
                ps.setString(4, video.getSnippet().getDescription());
                ps.setTimestamp(5, new Timestamp(video.getSnippet().getPublishedAt().getValue()));
                try {
                    ps.setLong(6, Long.parseLong(video.getStatistics().getViewCount().toString()));
                } catch(NullPointerException e) {
                    ps.setLong(6,0);
                }
                try {
                    ps.setLong(7, Long.parseLong(video.getStatistics().getCommentCount().toString()));
                } catch(NullPointerException e) {
                    ps.setLong(7,0);
                }
                try {
                    ps.setLong(8, Long.parseLong(video.getStatistics().getLikeCount().toString()));
                } catch(NullPointerException e) {
                    ps.setLong(8,0);
                }
                try {
                    ps.setLong (9, Long.parseLong(video.getStatistics().getDislikeCount().toString()));
                } catch(NullPointerException e) {
                    ps.setLong(9,0);
                }
                try {
                    ps.setLong (10, Long.parseLong(video.getStatistics().getFavoriteCount().toString()));
                } catch(NullPointerException e) {
                    ps.setLong(10,0);
                }
                try {
                    ps.setString(11, video.getSnippet().getCategoryId());
                } catch(NullPointerException e) {
                    ps.setString(11,"");
                }
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
                count++;
                if(count % 50 == 0)
                    System.out.println("Have inserted" + count + " videos for channel: " + channelId);

                //System.out.println("inserted video: " + res);
                /*try {
                    List<String> tags = video.getSnippet().getTags();
                    int nbtags = tags.size();
                    System.out.println("Inserting tags of the video" + video.getId() + " and channel " + channelName + " into the table");
                    String query1 = "INSERT INTO \"prdwa17_staging\".\"youtubetags\" (\"id\", \"title\", \"videoid\") VALUES (?,?,?)";
                    PreparedStatement pstag = conn.prepareStatement(query1);

                    for (i = 0; i < nbtags; i++) {
                        try {
                            try {
                                pstag.setString(1, UUID.randomUUID().toString());
                            } catch(NullPointerException e) {
                                ps.setString(1,"");
                            }
                            try {
                                pstag.setString(2, tags.get(i));
                            } catch(NullPointerException e) {
                                ps.setString(2,"");
                            }
                            try {
                                pstag.setString(3, video.getId());
                            } catch(NullPointerException e) {
                                ps.setString(3,"");
                            }
                            boolean res1 = pstag.execute();

                        } catch (IndexOutOfBoundsException e) {
                            System.out.println("Error: Invalid tag for video " + video.getId() +
                                    "(" + video.getSnippet().getTitle() + ")");
                        }
                    }
                } catch(NullPointerException e) {
                    System.out.println("Error inserting Tags : Probably no tags for video " +
                            video.getId() + " (" + video.getSnippet().getTitle()+ ")");
                }*/
                    //System.out.println(pstag.toString());
                //System.out.println("inserted tag: " + res1);
                /*
                System.out.println("New Thread to load comments of video " + video.getId());
                CommentThreads commentThreadsThread = new CommentThreads(video.getId(), video.getSnippet().getTitle());
                commentThreadsThread.start();
                System.out.println("--------------------------------- END AT : "  + new Date() + " ----------------------------");
                */
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

