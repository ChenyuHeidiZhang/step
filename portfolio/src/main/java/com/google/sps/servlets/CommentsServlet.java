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
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
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
  private final static String NAME = "name";
  private final static String MOOD = "mood";
  private final static String CONTENT = "content";
  private final static String SENTIMENT = "sentiment";
  private final static String TIMESTAMP = "timestamp";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String languageCode = request.getParameter("languageCode");

    Query query = new Query("Comment").addSort(TIMESTAMP, SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity commentEntity : results.asIterable()) {
      long id = commentEntity.getKey().getId();
      String name = (String) commentEntity.getProperty(NAME);
      String mood = (String) commentEntity.getProperty(MOOD);
      String content = (String) commentEntity.getProperty(CONTENT);
      if (!"original".equals(languageCode)) {
        content = translateText(content, languageCode);
      }

      double sentiment = (double) commentEntity.getProperty(SENTIMENT);  // Datastore keeps double by default.
      long timestamp = (long) commentEntity.getProperty(TIMESTAMP);

      comments.add(new Comment(id, name, mood, content, (float) sentiment, timestamp));
    }

    // Convert the ArrayList into a JSON string using the Gson library.
    Gson gson = new Gson();
    String json = gson.toJson(comments);

    // Send the JSON as the response.
    response.setContentType("text/html; charset=UTF-8;");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input parameters from the form.
    String name = request.getParameter("user-name");
    String mood = request.getParameter("mood");
    String content = request.getParameter("comment-content");

    long timestamp = System.currentTimeMillis();

    // Create a new comment entity.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty(NAME, name);
    commentEntity.setProperty(MOOD, mood);
    commentEntity.setProperty(CONTENT, content);
    commentEntity.setProperty(SENTIMENT, getSentimentScore(content));
    commentEntity.setProperty(TIMESTAMP, timestamp);

    // Store the comment entity to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/comments.html");
  }
  
  /** 
   * Returns the score of the sentiment of the given message, 
   * which is a float from -1 to 1 representing how negative or positive the text it.
   */
  private float getSentimentScore(String message) throws IOException {
    Document doc =
        Document.newBuilder().setContent(message).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();

    return score;
  }

  /** 
   * Translates "originalText" to the language represented by "languageCode" and returns the translated text.
   */
  private String translateText(String originalText, String languageCode) {
    Translate translate = TranslateOptions.getDefaultInstance().getService();
    Translation translation =
        translate.translate(originalText, Translate.TranslateOption.targetLanguage(languageCode));
    return translation.getTranslatedText();
  }
}
