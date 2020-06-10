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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that stores the user's id and nickname entered, and returns a display name upon GET request. */
@WebServlet("/nickname")
public class NicknameServlet extends HttpServlet {
  private final String USERID = "userId";
  private final String NICKNAME = "nickname";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    String userId = request.getParameter("userId");    
    String nickname = getUserNickname(userId);
    String displayName;
    // Display nickname if set, email otherwise.
    if (nickname == "") {
      displayName = userService.getCurrentUser().getEmail();
    } else {
      displayName = nickname;
    }

    response.setContentType("text/html;");
    response.getWriter().println(displayName);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/comments.html");
      return;
    }

    String nickname = request.getParameter("nickname-input");
    String userId = userService.getCurrentUser().getUserId();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity entity = new Entity("UserInfo", userId);
    entity.setProperty(USERID, userId);
    entity.setProperty(NICKNAME, nickname);
    // Insert a new entry for the user in the datastore.
    // Note: The "userId" is used as a key to identify each entry, so existing entries will be updated to use the new "nickname".
    datastore.put(entity);

    response.sendRedirect("/comments.html");
  }

  /**
   * Returns the nickname of the user with the specified id, or empty String if no nickname is set.
   */
  private String getUserNickname(String userId) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate(USERID, Query.FilterOperator.EQUAL, userId));

    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return "";
    }
    String nickname = (String) entity.getProperty(NICKNAME);
    return nickname;
  }
}
