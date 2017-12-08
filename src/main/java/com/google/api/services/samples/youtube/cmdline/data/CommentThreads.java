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
import java.util.*;
import java.util.Date;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.samples.youtube.cmdline.AsterDatabaseInterface;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This sample creates and manages top-level comments by:
 *
 * 1. Creating a top-level comments for a video and a channel via "commentThreads.insert" method.
 * 2. Retrieving the top-level comments for a video and a channel via "commentThreads.list" method.
 * 3. Updating an existing comments via "commentThreads.update" method.
 *
 * @author Ibrahim Ulukaya
 */
//extends Thread
public class CommentThreads extends Thread {

    /**
     * Define a global instance of a YouTube object, which will be used to make
     * YouTube Data API requests.
     */
    private static Connection conn;
    private static YouTube youtube;
    private String videoId;
    private String videoTitle;
    private static PreparedStatement ps;
    public CommentThreads(String videoId, String videoTitle) {
        this.videoId = videoId;
        this.videoTitle = videoTitle;
    }
    static {
        try {
            conn = AsterDatabaseInterface.connect();
            String query = " INSERT INTO \"prdwa17_staging\".\"videoscomments\" " +
                    "(\"id\", \"authorchannelurl\", " +
                    "\"authordisplayedname\", \"authorchannelid\", \"videoid\", " +
                    "\"parentid\", \"textoriginal\", \"likecount\", \"publishedat\", " +
                    "\"fetchedat\") VALUES (?,?,?,?,?,?,?,?,?,?);";
            ps = conn.prepareStatement(query);
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
            credential = Auth.authorize(scopes, "commentthreads");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This object is used to make YouTube Data API requests.
        youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                .setApplicationName("youtube-cmdline-commentthreads-sample").build();

    }

    /**
     */
    public void run() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        insertComments(videoId);
        stopwatch.stop(); // optional

        long millis = stopwatch.elapsed(SECONDS);
        System.out.println("------------------ Inserted Comments of video " + videoTitle + "(" + videoId + ")" + " within " + millis + " s -------------------");
        System.out.println( "--------------------------------- END AT: "  + new Date() + " ----------------------------");

    }

    private List<CommentThread> getCommentThreads(String parentId, String type) throws IOException {
        CommentThreadListResponse CommentsListResponse;
        String nextPageToken = "";
        List<CommentThread> comments = new ArrayList<CommentThread>();
        System.out.println("Fetching CommentsThreads of video " + videoTitle + " -- " + videoId);
        while (nextPageToken != null) {
            if (type.equals("video"))
                CommentsListResponse = (nextPageToken.equals("") ? youtube.commentThreads().list("id,snippet,replies")
                        .setVideoId(parentId).setTextFormat("plaintext").setOrder("time").execute() :
                        youtube.commentThreads().list("snippet").setPageToken(nextPageToken)
                                .setVideoId(parentId).setOrder("time").execute());
            else
                CommentsListResponse = (nextPageToken.equals("") ? youtube.commentThreads().list("id,snippet,replies")
                        .setChannelId(parentId).setTextFormat("plaintext").setOrder("time").execute() :
                        youtube.commentThreads().list("snippet").setPageToken(nextPageToken)
                                .setChannelId(parentId).setTextFormat("UTF-8") .setOrder("time").execute());
            comments.addAll(CommentsListResponse.getItems());

            nextPageToken = CommentsListResponse.getNextPageToken();
        }
        System.out.println("Fetched " + comments.size() + " comments for video " + videoTitle + "(" + videoId +")" );
        return comments;
    }

    private void insertComments(String videoId) {
        try {
            int ignoredCount  = 0;
            // Prompt the user for the ID of a channel to comment on.
            // Retrieve the channel ID that the user is commenting to.
            List<CommentThread> comments = getCommentThreads(videoId, "video");

            List<Comment> topComments = new ArrayList<>();
            for(CommentThread comment : comments) {
                topComments.add(comment.getSnippet().getTopLevelComment());
            }

            System.out.println("Start Inserting " + topComments.size() + " Comments for video: " + videoId);
            //if(!insertDone) {
            insertUntilDone(topComments);
            System.out.println("Execute Insert query for CommentThreads of video: " + videoId);
            int[] res = new int[0];
            try {
                res = ps.executeBatch();
                ps.clearBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                System.out.println("(CommentThreads) Insert results: " + res[0] + " for video " + videoId);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("ArrayIndexOutOfBoundsException: Something went wrong inserting comments for video: " + videoId);
            }

        } catch (IOException e) {
            System.out.println("IOException: Probably comments disabled for : " + videoId);
        }

    }


    private String populate(String original) {
        return (original != null ? original : "");
    }

    private void insertUntilDone(List<Comment> comments) {
        int ignoredCount = 0;
        List<Comment> remainingComments = new ArrayList<>();
        //dzboolean continued = false;
        for (Comment topC : comments) {
//            if(continued) System.out.println("IT CONTINUED for " + videoId + " " + ignoredCount + " times");
            try {
                ps.setString(1, Tools.convertToUTF8(populate(topC.getId())));
                ps.setString(2, Tools.convertToUTF8( populate(topC.getSnippet().getAuthorChannelUrl())));
                ps.setString(3, Tools.convertToUTF8(populate(topC.getSnippet().getAuthorDisplayName())));
                ps.setString(4, Tools.convertToUTF8(populate(topC.getSnippet().getAuthorChannelId().toString())));
                ps.setString(5, Tools.convertToUTF8(populate(topC.getSnippet().getVideoId())));
                ps.setString(6, "");
                ps.setString(7, Tools.convertToUTF8(populate(topC.getSnippet().getTextOriginal())));
                ps.setLong(8,
                        (topC.getSnippet().getLikeCount() != null ? topC.getSnippet().getLikeCount().intValue() : 0));
                java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(new Date().getTime());

                ps.setTimestamp(9, new Timestamp(topC.getSnippet().getPublishedAt().getValue()));
                ps.setTimestamp(10, currentTimestamp);
                ps.addBatch();
            } catch (SQLException e) {
                //continued = true;
                System.out.println("Comment Ignored: " + topC.getId() + " from video " + videoId);
                remainingComments.add(topC);
                ignoredCount++;
            }
        }

        System.out.println("Have addBatched " + (comments.size() - ignoredCount)  + " comments for video: " + videoId );
        if(ignoredCount > 0) {
            System.out.println("INFO: Number of ignored comments: " + ignoredCount + " for channel " + videoId);
            insertUntilDone(remainingComments);
        }
        else {
            System.out.println("INFO: AddBatch comments done for video" + videoId);
        }
    }
}
