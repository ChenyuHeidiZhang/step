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
 * Toggles the dropdown text and the direction of the dropdown arrow.
 * @param {string} id The id of the dropdown content to be toggled.
 * @param {!Event} event The click event on a dropdown bar.
 */
function toggleDropDown(id, event) {
  document.getElementById(id).classList.toggle('show');
  const dropdownArrow = event.currentTarget.firstElementChild;
  dropdownArrow.classList.toggle('up');
}

/** Adds a random favorite quote to the page. */
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

/** Adds a random fun fact to the page. */
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
