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

package com.google.sps.data;

/** A class that represents a comment. */
public final class Comment {
  private final long id;
  private final String name;
  private final String mood;
  private final String content;
  private final String imageUrl;
  private final long timestamp;

  public Comment(long id, String name, String mood, String content, String imageUrl, long timestamp) {
    this.id = id;
    this.name = name;
    this.mood = mood;
    this.content = content;
    this.imageUrl = imageUrl;
    this.timestamp = timestamp;
  }
}
