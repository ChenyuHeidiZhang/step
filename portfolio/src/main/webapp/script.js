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
  const funFacts =
      ["sister", 
      "selfie",
      "I'm a fan of collections. I collect postcards, stickers, books, snacks, etc.",
      "My English name is Heidi. Many people can't pronounce that.",
      "Funny enough that most of my Chinese friends call me by my English name while my American friends call me by my Chinese name."];

  // Pick a random index for the fun fact.
  const index = Math.floor(Math.random() * funFacts.length);

  // Add the fun fact to the page.
  const funFactContainer = document.getElementById('fun-fact-container');
  if (index == 0 || index == 1) {
  	funFactContainer.innerHTML = document.getElementById('fun-fact-' + funFacts[index]).innerText;
  } else {
  	funFactContainer.innerText = funFacts[index];
  }

}

/* 
 * Creates the pagination bar given the number of pages and the previous active page number.
 * Returns the current active page number.
 */
function createPagination(numPages, activePageNumber) {
  // If the previous active page number is greater than the current number of pages, then set it to 1;
  // Otherwise, use the previous as current active page number.
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

/*
 * Creates an <li> element for a pagination item whose a.innerText is 'text'.
 * If 'active' is true, then add 'active' to its className.
 */
function createPageElement(text, active) {
  const pageElement = document.createElement('li');
  pageElement.className = 'page-item';
  if (active) { pageElement.classList.add('active'); }
  
  const linkElement = document.createElement('a');
  linkElement.className = 'page-link';
  linkElement.innerText = text;
  pageElement.appendChild(linkElement);

  pageElement.addEventListener('click', event => changePage(event));
  return pageElement;
}

/* 
 * When a page item is clicked, changes the active page element and displays the comments on that page.
 */
function changePage(event) {
  const currentPage = document.querySelector('.page-item.active');
  const pageText = event.currentTarget.firstElementChild.innerText;
  
  if (pageText == 'Previous') {
    // If the currentPage is the first page, then return without change.
    if (currentPage.firstElementChild.innerText == 1) { return; }

    currentPage.previousSibling.classList.add('active');
  } else if (pageText == 'Next') {
    // If the currentPage is the last page, then return without change.
    const numPages = document.querySelectorAll('#page-list li').length;
    if (currentPage.firstElementChild.innerText == numPages - 2) { return; }

    currentPage.nextSibling.classList.add('active');
  } else {
    event.currentTarget.classList.add('active');
  }

  currentPage.classList.remove('active');

  // Fetch comments on that page without recreating pagination.
  fetchComments(false);
}

/* 
 * Fetches the comments data on the current page from the server and displays them.
 */
function fetchComments(createNewPagination = false) {
  const currentPage = document.querySelector('.page-item.active');
  var currentPageNumber = currentPage.firstElementChild.innerText;

  const selectElement = document.getElementById('num-comments-per-page');
  const commentsPerPage = selectElement.options[selectElement.selectedIndex].value;

  fetch('/data').then(response => response.json()).then(comments => {
    const commentsListElement = document.getElementById('comments-list');
    commentsListElement.innerHTML = '';

    if (createNewPagination) {
      currentPageNumber = createPagination(Math.ceil(comments.length / commentsPerPage), currentPageNumber);
    }

    const startIndex = (currentPageNumber - 1) * commentsPerPage;
    const endIndex = Math.min(Number(commentsPerPage) + Number(startIndex), comments.length);
    for (var i = startIndex; i < endIndex; i++) {
      commentsListElement.appendChild(createCommentElement(comments[i]));
    }
  });
}

/* 
 * Creates an <li> element containing a comment.
 */
function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';

  const divElement = document.createElement('div');

  const nameElement = document.createElement('strong');
  nameElement.className = 'text-success';
  nameElement.innerText = '@' + comment.name + ' - ' + comment.mood;
  const timeElement = document.createElement('span');
  timeElement.className = 'pull-right text-muted';  // Add Bootstrap classes to style the timeElement.
  timeElement.innerText = convertToDateTime(comment.timestamp);
  const contentElement = document.createElement('p');
  contentElement.innerText = comment.content;
  
  divElement.appendChild(timeElement);
  divElement.appendChild(nameElement);
  divElement.appendChild(contentElement);

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);
    commentElement.remove();    // Remove the comment from the DOM.
  });

  commentElement.appendChild(divElement);
  commentElement.appendChild(deleteButtonElement);
  return commentElement;
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
  const responsePromise = fetch('/delete-data', {method: 'POST'});
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
