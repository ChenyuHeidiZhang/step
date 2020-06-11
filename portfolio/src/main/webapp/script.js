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
 * @param {string} id The id of the dropdown content to be toggled.
 */
function toggleDropDown(id) {
  document.getElementById(id).classList.toggle("show");
}

/**
 * Adds a random favorite quote to the page.
 */
function addRandomFavoriteQuote() {
  const favoriteQuotes = [
    'Many years later, as he faced the firing squad, Colonel Aureliano Buendía was to remember that distant afternoon when his father took him to discover ice. \n —Gabriel García Márquez, One Hundred Years of Solitude',
    'Hope is a dangerous thing. Hope can drive a man insane. \n - The Shawshank Redemption',
    'Bran thought about it. "Can a man still be brave if he\'s afraid?" "That is the only time a man can be brave," his father told him. \n ― George R.R. Martin, A Game of Thrones',
  ];

  // Pick a random quote.
  const randomFavoriteQuote = favoriteQuotes[Math.floor(Math.random() * favoriteQuotes.length)];

  // Add the favorite quote to the page.
  const favoriteQuoteContainer = document.getElementById('favorite-quote-container');
  favoriteQuoteContainer.innerText = randomFavoriteQuote;
}

/**
 * Adds a random fun fact to the page.
 */
function addRandomFunFact() {
  const funFactsJsIds = [
    'fun-fact-sister',
    'fun-fact-selfie',
  ];

  const funFactsStrings = [
    'I\'m a fan of collections. I collect postcards, stickers, books, snacks, etc.',
    'My English name is Heidi. Many people can\'t pronounce that.',
    'Funny enough that most of my Chinese friends call me by my English name while my American friends call me by my Chinese name.',
  ];

  // Pick a random index for the fun fact.
  const index = Math.floor(Math.random() * (funFactsJsIds.length + funFactsStrings.length));

  // Add the fun fact to the page.
  const funFactContainer = document.getElementById('fun-fact-container');
  if (index < funFactsJsIds.length) {
    funFactContainer.innerHTML = document.getElementById(funFactsJsIds[index]).innerText;
  } else {
    funFactContainer.innerText = funFactsStrings[index - funFactsJsIds.length];
  }
}

/**
 * Creates the pagination bar such that the active page number is the previously active page number, unless it no longer exists. 
 * @param {number} numPages The total number of pages. 
 * @param {number} activePageNumber The previously active page number.
 * @return {number} The currently active page number.
 */
function createPaginationBar(numPages, activePageNumber) {
  // Set the currently active page number to 1 if the previously active page number no longer exists.
  if (activePageNumber > numPages) {
    activePageNumber = 1;
  }
  
  pageListElement = document.getElementById('page-list');
  pageListElement.innerHTML = '';

  pageListElement.appendChild(createPageElement('Previous'));
  for (var i = 1; i <= numPages; i++) {
    if (i == activePageNumber) {
      pageListElement.appendChild(createPageElement(i, true));
    } else {
      pageListElement.appendChild(createPageElement(i, false));
    }
  }
  pageListElement.appendChild(createPageElement('Next'));

  return activePageNumber;
}

/**
 * Creates an <li> element for a page item in the pagination bar. 
 * @param {string} text The innerText of the <a> child element of the <li>.
 * @param {boolean} active Whether the created page item is active.
 * @return {!Element<li>} The page element created.
 */
function createPageElement(text, active) {
  const pageElement = document.createElement('li');
  pageElement.className = 'page-item';
  if (active) { 
    pageElement.classList.add('active'); 
  }
  
  const linkElement = document.createElement('a');
  linkElement.className = 'page-link';
  linkElement.innerText = text;
  pageElement.appendChild(linkElement);

  pageElement.addEventListener('click', event => changePage(event));
  return pageElement;
}

/**
 * Changes the active page element and displays the comments on that page when a page element is clicked.
 * @param {!Event} event The click event that triggered the change in active page.
 */
function changePage(event) {
  const currentPage = document.querySelector('.page-item.active');
  const pageText = event.currentTarget.firstElementChild.innerText;
  
  if (pageText == 'Previous') {
    if (currentPage.firstElementChild.innerText == 1) { 
      // Current page is the first page, no previous page to open.
      return; 
    }

    currentPage.previousSibling.classList.add('active');
  } else if (pageText == 'Next') {
    const numPages = document.querySelectorAll('#page-list li').length;
    if (currentPage.firstElementChild.innerText == numPages - 2) { 
      // Current page is the last page, no next page to open.
      return; 
    }

    currentPage.nextSibling.classList.add('active');
  } else {
    event.currentTarget.classList.add('active');
  }

  currentPage.classList.remove('active');

  // Fetch comments on that page without recreating pagination.
  fetchComments(false);
}

/**
 * Fetches the comments from the data server and displays them on the currently active page.
 * @param {boolean=} createNewPagination Whether a new pagination bar should be created.
 * @param {string=} languageCode The language in which the comments will be shown.
 */
function fetchComments(createNewPagination = false, languageCode = 'en') {
  fetch('/comments?languageCode=' + languageCode).then(response => response.json()).then(comments => {
    const commentsListElement = document.getElementById('comments-list');
    commentsListElement.innerHTML = '';

    const commentsPerPageElement = document.getElementById('num-comments-per-page');
    const commentsPerPage = commentsPerPageElement.options[commentsPerPageElement.selectedIndex].value;

    const currentPage = document.querySelector('.page-item.active');
    var currentPageNumber = currentPage.firstElementChild.innerText;
    if (createNewPagination) {
      currentPageNumber = createPaginationBar(Math.ceil(comments.length / commentsPerPage), currentPageNumber);
    }

    const startIndex = (currentPageNumber - 1) * commentsPerPage;
    const endIndex = Math.min(Number(commentsPerPage) + Number(startIndex), comments.length);
    for (var i = startIndex; i < endIndex; i++) {
      commentsListElement.appendChild(createCommentElement(comments[i]));
    }
  });
}

/**
 * Creates an <li> element containing an individual comment. The element includes the 
 * following attributes associated with the comment: username, content, time, and delete button.
 * @param {!Comment} comment The Comment object from which a <li> element is created.
 * @return {!Element<li>} The comment element created.
 */
function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';

  const commentContainer = document.createElement('div');

  const nameElement = document.createElement('strong');
  nameElement.className = 'text-success';
  nameElement.innerText = '@' + comment.name + ' - ' + comment.mood;
  const timeElement = document.createElement('span');
  timeElement.className = 'pull-right text-muted';  // Add Bootstrap classes to style the timeElement.
  timeElement.innerText = convertToDateTime(comment.timestamp);
  const contentElement = document.createElement('p');
  contentElement.innerText = comment.content;
  
  commentContainer.appendChild(timeElement);
  commentContainer.appendChild(nameElement);
  commentContainer.appendChild(contentElement);

  const sentimentScore = document.createElement('span');
  sentimentScore.className = 'sentiment';
  sentimentScore.innerText = 'sentiment score: ' + comment.sentiment;

  const deleteButton = document.createElement('button');
  deleteButton.className = 'delete-btn';
  deleteButton.innerText = 'Delete';
  deleteButton.addEventListener('click', () => {
    deleteComment(comment);
    deleteButton.parentElement.remove();  // Remove the comment from the DOM.
  });

  commentElement.appendChild(commentContainer);
  commentElement.appendChild(sentimentScore);
  commentElement.appendChild(deleteButton);
  return commentElement;
}

/**
 * Converts a timestamp in milliseconds to a formatted date/time string.
 * @param {string} timestamp The timestamp to be converted. 
 * @return {string} Date with format dd/mm/yyyy if the given time is not today; 
 *     otherwise, number of hours before the current time. 
 */
function convertToDateTime(timestamp) {
  var date = new Date(timestamp);
  var dateFormatted = date.toLocaleDateString(); 
  var today = new Date();
  // Use time in hours if timestamp is within today.
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

/**
 * Tells the server to delete all comments data in the Datastore.
 */
function deleteAllComments() {
  const responsePromise = fetch('/delete-comments', {method: 'POST'});
  responsePromise.then(fetchComments);
}

/** 
 * Tells the server to delete one comment.
 * @param {!Comment} comment The object that represents the comment to be deleted.
 */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch('/delete-comments', {method: 'POST', body: params});
}

/**
 * Fetches the comments again in the langauge specified. 
 */
function changeLanguage() {
  const languageCode = document.getElementById('language').value;
  fetchComments(false, languageCode);
}
