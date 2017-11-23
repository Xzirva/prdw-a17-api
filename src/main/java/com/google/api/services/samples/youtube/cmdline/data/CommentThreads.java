/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.services.samples.youtube.cmdline.data;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.samples.youtube.cmdline.AsterDatabaseInterface;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.google.common.collect.Lists;

/**
 * This sample creates and manages top-level comments by:
 *
 * 1. Creating a top-level comments for a video and a channel via "commentThreads.insert" method.
 * 2. Retrieving the top-level comments for a video and a channel via "commentThreads.list" method.
 * 3. Updating an existing comments via "commentThreads.update" method.
 *
 * @author Ibrahim Ulukaya
 */
public class CommentThreads {

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

            List<CommentThread> channelComments = getCommentThreads(channelId, "channel");
            //String prettyStringToSave = "";
            BufferedWriter out = new BufferedWriter(new FileWriter("fetched-data"+"CNN"+ System.nanoTime()+".json"));
//            for(CommentThread c : channelComments) {
//                prettyStringToSave += c.toPrettyString();
//            }
//            out.write(prettyStringToSave);
            if (channelComments.isEmpty()) {
                System.out.println("Can't get channel comments.");
            } else {
                // Print information from the API response.
                System.out
                        .println("\n================== Returned Channel Comments" + channelComments.size() + " ==================\n");
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

    private static List<CommentThread> getCommentThreads(String parentId, String type, DateTime publishedDateTime) throws IOException {
        CommentThreadListResponse CommentsListResponse;
        String nextPageToken = "";
        List<CommentThread> comments = new ArrayList<CommentThread>();
        while(nextPageToken != null) {
            System.out.println("Fetching CommentsThreads");
                if (type.equals("video"))
                    CommentsListResponse = (nextPageToken.equals("") ? youtube.commentThreads().list("snippet")
                            .setVideoId(parentId).setTextFormat("plaintext").setOrder("time").execute() :
                            youtube.commentThreads().list("snippet").setPageToken(nextPageToken)
                                    .setVideoId(parentId).setOrder("time").execute());
                else
                    CommentsListResponse = (nextPageToken.equals("") ? youtube.commentThreads().list("snippet")
                            .setChannelId(parentId).setTextFormat("plaintext").setOrder("time").execute() :
                            youtube.commentThreads().list("snippet").setPageToken(nextPageToken)
                                    .setChannelId(parentId).setOrder("time").execute());
                comments.addAll(CommentsListResponse.getItems());
            System.out.println("Total Results: " + CommentsListResponse.getPageInfo().getTotalResults() + "/" +
                    "\nCurrentPageResult: " + CommentsListResponse.getPageInfo().getResultsPerPage());
            nextPageToken = CommentsListResponse.getNextPageToken();
        }
        return comments;
    }

    public static void insertComments(String videoId, DateTime publishedDateTime) {
        try {

            // Prompt the user for the ID of a channel to comment on.
            // Retrieve the channel ID that the user is commenting to.
            System.out.println("Inserting videos from channel: " + channelId);
            java.util.Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            List<Video> videos = getCommentThreads(videoId, ,new DateTime(cal.getTime()));
            Connection conn = AsterDatabaseInterface.connect();
            System.out.println("Inserting channel: " + channelName);
            String query = " INSERT INTO \"prdwa17_staging\".\"channels\" (\"id\", \"title\", \"description\", \"publishedat\", \"viewcount\", \"commentcount\", \"subscribercount\", " +
                    "\"videocount\", \"topiccategory_1\", \"topiccategory_2\", \"topiccategory_3\", \"keywords\") VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";
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
