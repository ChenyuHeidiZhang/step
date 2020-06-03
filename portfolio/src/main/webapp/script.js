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


/** 
 * Toggles the drop down text.
 */
function toggleDropDown(id) {
  document.getElementById(id).classList.toggle("show");
}

/**
 * Adds a random favorite quote to the page.
 */
function addRandomFavoriteQuote() {
  const favoriteQuotes =
      ["Many years later, as he faced the firing squad, Colonel Aureliano Buendía was to remember that distant afternoon when his father took him to discover ice. \n —Gabriel García Márquez, One Hundred Years of Solitude",
      "Hope is a dangerous thing. Hope can drive a man insane. \n - The Shawshank Redemption",
      "Bran thought about it. 'Can a man still be brave if he's afraid?' 'That is the only time a man can be brave,' his father told him. \n ― George R.R. Martin, A Game of Thrones"];

  // Picks a random quote.
  const randomFavoriteQuote = favoriteQuotes[Math.floor(Math.random() * favoriteQuotes.length)];

  // Adds the favorite quote to the page.
  const favoriteQuoteContainer = document.getElementById('favorite-quote-container');
  favoriteQuoteContainer.innerText = randomFavoriteQuote;
}

/**
 * Adds a random fun fact to the page.
 */
function addRandomFunFact() {
  const funFacts =
      ["sister", 
      "selfie",
      "I'm a fan of collections. I collect postcards, stickers, books, snacks, etc.",
      "My English name is Heidi. Many people can't pronounce that.",
      "Funny enough that most of my Chinese friends call me by my English name while my American friends call me by my Chinese name."];

  // Picks a random index for the fun fact.
  const index = Math.floor(Math.random() * funFacts.length);

  // Adds the fun fact to the page.
  const funFactContainer = document.getElementById('fun-fact-container');
  if (index == 0 || index == 1) {
  	funFactContainer.innerHTML = document.getElementById('fun-fact-' + funFacts[index]).innerText;
  } else {
  	funFactContainer.innerText = funFacts[index];
  }

}

/*
 * Makes sure that the input number of comments is an integer between 1 and 20.
 */
function checkNumberComments() {
  const numCommentsElement = document.getElementById('num-comments');
  const numComments = parseInt(numCommentsElement.value, 10);
  if (numCommentsElement.value != numComments) {
    numCommentsElement.value = numComments;
  }
  if (numComments > 20) {
    numCommentsElement.value = 20;
  } else if (numComments < 1) {
    numCommentsElement.value = 1;
  }
}

/* 
 * Fetches the comments data from the server and displays them.
 */
function fetchComments() {
  checkNumberComments();
  
  const numComments = document.getElementById('num-comments').value;
  fetch('data?num-comments=' + numComments).then(response => response.json()).then(comments => {
    const commentsListElement = document.getElementById("comments-list");
    commentsListElement.innerHTML = "";
    comments.forEach((comment) => {
      commentsListElement.appendChild(createListElement(comment));
    });
  });
}

/* 
 * Creates an <li> element containing a comment.
 */
function createListElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';

  const divElement = document.createElement('div');

  const nameElement = document.createElement('strong');
  nameElement.className = 'text-success';
  nameElement.innerText = '@' + addslashes(comment.name);
  const timeElement = document.createElement('span');
  timeElement.className = 'pull-right text-muted';
  timeElement.innerText = convertToDateTime(comment.timestamp);
  const contentElement = document.createElement('p');
  contentElement.innerText = addslashes(comment.content);
  
  divElement.appendChild(timeElement);
  divElement.appendChild(nameElement);
  divElement.appendChild(contentElement);

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);
    commentElement.remove();    // Removes the comment from the DOM.
  });

  commentElement.appendChild(divElement);
  commentElement.appendChild(deleteButtonElement);
  return commentElement;
}

/*
 * Adds slashes to input when necessary.
 */
function addslashes(string) {
  return string.replace(/'/g, '\'').
    replace(/"/g, '\"');
}

/* 
 * Converts a timestamp in milliseconds to a formatted date/time string.
 * Returns dd/mm/yyyy if not on same day; otherwise, returns number of hours ago. 
 */
function convertToDateTime(timestamp) {
  var date = new Date(timestamp);
  var dateFormatted = date.toLocaleDateString(); 
  var today = new Date();
  if (dateFormatted == today.toLocaleDateString()) {
    var hourDiff = today.getHours() - date.getHours();
    if (hourDiff <= 1) {
      return hourDiff + ' hour ago';
    } else {
      return hourDiff + ' hours ago';
    }
  }
  return dateFormatted;
}

/*
 * Tells the server to delete all comments data in the Datastore.
 */
function deleteData() {
  const responsePromise = fetch('/delete-data', {method: 'POST'})
  responsePromise.then(fetchComments);
}

/* 
 * Tells the server to delete one comment.
 */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch('/delete-comment', {method: 'POST', body: params});
}
