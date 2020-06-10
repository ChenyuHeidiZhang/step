// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that creates and lists comments data. */
@WebServlet("/comments")
public class CommentsServlet extends HttpServlet {
  private final String USERID = "userId";
  private final String MOOD = "mood";
  private final String CONTENT = "content";
  private final String TIMESTAMP = "timestamp";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort(TIMESTAMP, SortDirection.DESCENDING);
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity commentEntity : results.asIterable()) {
      long id = commentEntity.getKey().getId();
      String userId = (String) commentEntity.getProperty(USERID);
      String mood = (String) commentEntity.getProperty(MOOD);
      String content = (String) commentEntity.getProperty(CONTENT);
      long timestamp = (long) commentEntity.getProperty(TIMESTAMP);

      comments.add(new Comment(id, userId, mood, content, timestamp));
    }

    // Convert the ArrayList into a JSON string using the Gson library.
    Gson gson = new Gson();
    String json = gson.toJson(comments);

    // Send the JSON as the response.
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    // Only logged-in users can post comments.
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/comments.html");
      return;
    }

    String userId = userService.getCurrentUser().getUserId();
    // Get the input parameters from the form.
    String mood = request.getParameter("mood");
    String content = request.getParameter("comment-content");
    long timestamp = System.currentTimeMillis();

    // Create a new comment entity.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty(USERID, userId);
    commentEntity.setProperty(MOOD, mood);
    commentEntity.setProperty(CONTENT, content);
    commentEntity.setProperty(TIMESTAMP, timestamp);

    // Store the comment entity to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/comments.html");
  }
}
