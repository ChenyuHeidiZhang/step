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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns user's login status and corresponding URLs for logging in or out. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String loggedInJson = "{\"isLoggedIn\": true, \"logoutUrl\": \"%s\", \"userId\": \"%s\"}";
    String loggedOutJson = "{\"isLoggedIn\": false, \"loginUrl\": \"%s\"}";

    response.setContentType("application/json;");

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String redirectUrlLogout = "/comments.html";  // The URL to redirect to after the user logs out.
      String logoutUrl = userService.createLogoutURL(redirectUrlLogout);
      response.getWriter().println(
          String.format(loggedInJson, logoutUrl, userService.getCurrentUser().getUserId()));
    } else {
      String redirectUrlLogin = "/comments.html";  // The URL to redirect to after the user logs in.
      String loginUrl = userService.createLoginURL(redirectUrlLogin);
      response.getWriter().println(String.format(loggedOutJson, loginUrl));
    }
  }
}
