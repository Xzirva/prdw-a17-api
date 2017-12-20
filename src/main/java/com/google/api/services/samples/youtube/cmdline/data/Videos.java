package com.google.api.services.samples.youtube.cmdline.data;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.client.util.DateTime;
import com.google.api.services.samples.youtube.cmdline.AsterDatabaseInterface;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.samples.youtube.cmdline.Main;
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
import static java.util.concurrent.TimeUnit.SECONDS;
// extends Thread
public class Videos extends Thread {

    /**
     * Define a global instance of a YouTube object, which will be used to make
     * YouTube Data API requests.
     */
    private static Connection conn;
    private String channelId;
    private YouTube youtube;
    private String client_secrets_filepath;
    private java.sql.Timestamp thisTime;
    private String channelName;
    private DateTime datePublishedAfter;
    private String account;
    private static final String query = "INSERT INTO \"prdwa17_staging\".\"videos\" " +
            "(\"id\", \"channelid\", \"title\", \"description\", \"publishedat\"," +
            " \"viewcount\", \"commentcount\", \"likecount\", \"dislikecount\", " +
            "\"favoritecount\", \"categoryid\", \"topiccategory_1\", \"topiccategory_2\", " +
            "\"topiccategory_3\", \"fetchedat\", \"duration\") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


    private static PreparedStatement ps;
    public Videos (String channelId, String channelName, DateTime datePublishedAfter,
                   String client_secrets_filepath, Timestamp thisTime, String account) {
        this.channelId = channelId;
        this.thisTime = thisTime;
        this.channelName = channelName;
        this.datePublishedAfter = datePublishedAfter;
        this.account = account;
        // This OAuth 2.0 access scope allows for full read/write access to the
        // authenticated user's account and requires requests to use an SSL connection.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");
        this.client_secrets_filepath = client_secrets_filepath;
        Credential credential = null;

        try {
            credential = Auth.authorize(scopes, "videos" + account
                    , client_secrets_filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This object is used to make YouTube Data API requests.
        youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                .setApplicationName("youtube-cmdline-commentthreads-sample").build();
    }
    static {
        try {
            conn = AsterDatabaseInterface.connect();
            ps = conn.prepareStatement(query);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

            long millis = stopwatch.elapsed(SECONDS);
            System.out.println("------------------inserted videos for channel " + channelName +"(" + channelId+ ")" +
                    " within: " + millis + " s -------------------");
            System.out.println("--------------------------------- END AT : "  + new Date() + " ----------------------------");


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

        do {
            playlistItemRequest.setPageToken(nextToken);
            playlistItemRequest.setFields("items(id),nextPageToken,pageInfo");
            SearchListResponse playlistItemResult = playlistItemRequest.execute();
            List<SearchResult> items = playlistItemResult.getItems();
            for(SearchResult item : items) {
                video_ids.add(item.getId().getVideoId());
            }
            String videoId = stringJoiner.join(video_ids);
            String videoNextToken = "";
            do {
                YouTube.Videos.List listVideosRequest = youtube.videos().list(parts).setId(videoId);
                listVideosRequest.setPageToken(videoNextToken);
                VideoListResponse listResponse = listVideosRequest.execute();
                videos.addAll(listResponse.getItems());
                videoNextToken = listResponse.getNextPageToken();
                video_ids.clear();
            }while(videoNextToken != null);

            int count = videos.size();
            if(count % 50 == 0)
                System.out.println("Have Fetched " + count + " videos for channel: " + channelId);

            nextToken = playlistItemResult.getNextPageToken();
        } while (nextToken != null);
        return videos;
    }

    public void insertVideos() {
        try {
            List<Video> videos = getVideos(channelId, datePublishedAfter);
            List<Video> toBeRemoved = new ArrayList<>();
            HashMap<String, Integer> doubleCount = new HashMap<>();

            for(Video video : videos) {
                if(doubleCount.get(video.getId()) != null && doubleCount.get(video.getId()) == 1) {
                    System.out.println("WARNING: We got video " +
                            video.getId() + " twice. Only keeping one them");
                    toBeRemoved.add(video);
                }else {
                    doubleCount.put(video.getId(), 1);
                }
            }
            System.out.println("WARNING: Removing " + toBeRemoved.size() + " videos");
            videos.removeAll(toBeRemoved);

            System.out.println("Start Inserting " + videos.size() + " videos from channel: " + channelName + " -- id: " + channelId);
            insertUntilDone(videos);
            //|| video.getId().equals("K8QdHuZJaPU") || video.getId().equals("HZcaCpbLqxg")
            System.out.println("Execute insert query for videos of channel: " + channelName+"(" + channelId + ")");
            int[] res = ps.executeBatch();
            ps.clearBatch(); // just to be cautious
            try {
                System.out.println("(Videos) Insert results: " + res[0]);
            } catch(ArrayIndexOutOfBoundsException e) {
                System.out.println("ArrayIndexOutOfBoundsException: Something went wrong inserting videos for channel: " + channelId);
            }
                System.out.println("Have inserted " + videos.size() + " videos for channel: " + channelName+"(" + channelId + ")");
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

    private void insertUntilDone(List<Video> videos) {
        int ignoredCount = 0;
        List<Video> remainingVideos = new ArrayList<>();
        for(Video video : videos){
            try {
            ps.setString(1, Tools.convertToUTF8(video.getId()));
            ps.setString(2, Tools.convertToUTF8(video.getSnippet().getChannelId()));
            ps.setString(3, Tools.convertToUTF8(video.getSnippet().getTitle()));
            ps.setString(4, Tools.convertToUTF8(video.getSnippet().getDescription()));
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
                ps.setString(11, Tools.convertToUTF8(video.getSnippet().getCategoryId()));
            } catch(NullPointerException e) {
                ps.setString(11,"");
            }
            int i;
            List<String> categories;
            try {
                categories = video.getTopicDetails().getTopicCategories();
                for(i = 0;i<3; i++) {
                    try {
                        ps.setString(12+i, Tools.convertToUTF8(categories.get(i)));
                    }catch (IndexOutOfBoundsException e) {
                        ps.setString(12+i, "");
                    }
                }
            } catch (NullPointerException e) {
                for(i = 0;i<3; i++) {
                    ps.setString(12+i, "");
                }
            }

//                java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(new Date().getTime());
                ps.setTimestamp(12 + i, thisTime);
                ps.setString(12 + i + 1, Tools.convertToUTF8((video.getContentDetails().getDuration() != null ?
                        video.getContentDetails().getDuration() : "PT0M0S")));
                ps.addBatch();
                if(Main.fetchComments) {
                    System.out.println("New Thread to load comments of video " + video.getId());
                    CommentThreads commentThreadsThread = new CommentThreads(video.getId(),
                            video.getSnippet().getTitle(), client_secrets_filepath, thisTime, account);
                    commentThreadsThread.start();
                }
            } catch (SQLException e) {
                remainingVideos.add(video);
                System.out.println("Video Ignored: " + video.getId() + " from channel " + channelId);
                ignoredCount++;
            }
        }

        if(ignoredCount > 0) {
            System.out.println("INFO: Number of ignored videos: " + ignoredCount + " for channel " + channelId);
            insertUntilDone(remainingVideos);
        }
        else {
            System.out.println("INFO: AddBatch videos done for channel " + channelId);
        }
    }
}

