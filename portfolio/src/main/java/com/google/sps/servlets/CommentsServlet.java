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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that creates and lists comments data. */
@WebServlet("/comments")
public class CommentsServlet extends HttpServlet {
  private final static String USER_ID = "userId";
  private final static String MOOD = "mood";
  private final static String COMMENT_CONTENT = "content";
  private final static String BLOB_KEY = "blobkey";
  private final static String SENTIMENT = "sentiment";
  private final static String TIMESTAMP = "timestamp";
  private final static String LANGUAGE_CODE_ORIGINAL = "original";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String languageCode = request.getParameter("languageCode");

    Query query = new Query("Comment").addSort(TIMESTAMP, SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity commentEntity : results.asIterable()) {
      long id = commentEntity.getKey().getId();
      String userId = (String) commentEntity.getProperty(USER_ID);
      String mood = (String) commentEntity.getProperty(MOOD);
      String content = (String) commentEntity.getProperty(COMMENT_CONTENT);
      if (!LANGUAGE_CODE_ORIGINAL.equals(languageCode)) {
        content = getTranslatedComment(content, languageCode);
      }

      String blobKeyString = (String) commentEntity.getProperty(BLOB_KEY);
      double sentiment = (double) commentEntity.getProperty(SENTIMENT);  // Datastore keeps double by default.
      long timestamp = (long) commentEntity.getProperty(TIMESTAMP);

      comments.add(new Comment(id, userId, mood, content, blobKeyString, (float) sentiment, timestamp));
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

    // Get the BlobKey of the image uploaded by the user to Blobstore.
    String blobKeyString = getUploadedBlobKey(request, "image");

    long timestamp = System.currentTimeMillis();

    // Create a new comment entity.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty(USER_ID, userId);
    commentEntity.setProperty(MOOD, mood);
    commentEntity.setProperty(COMMENT_CONTENT, content);
    commentEntity.setProperty(BLOB_KEY, blobKeyString);
    commentEntity.setProperty(SENTIMENT, getSentimentScore(content));
    commentEntity.setProperty(TIMESTAMP, timestamp);

    // Store the comment entity to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/comments.html");
  }
  
  /**
   * Returns the String representation of the blobKey of the file uploaded by the user to Blobstore
   * or {@code null} if the user did not upload a file.
   */
  private String getUploadedBlobKey(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a BlobKey. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the key at the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a BlobKey. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // TODO(chenyuz): Check the validity of the file here, e.g. to make sure it's an image file.
    // https://stackoverflow.com/q/10779564/873165

    return blobKey.getKeyString();
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
    languageService.close();
    return sentiment.getScore();
  }

  /** 
   * Translates a comment to the language represented by {@code languageCode}.
   */
  private String getTranslatedComment(String originalComment, String languageCode) {
    Translate translateService = TranslateOptions.getDefaultInstance().getService();
    Translation translation =
        translateService.translate(originalComment, Translate.TranslateOption.targetLanguage(languageCode));
    return translation.getTranslatedText();
  }
}
