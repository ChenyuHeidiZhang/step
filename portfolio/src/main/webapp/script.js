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
  const favoriteQuote = favoriteQuotes[Math.floor(Math.random() * favoriteQuotes.length)];

  // Adds the favorite quote to the page.
  const favoriteQuoteContainer = document.getElementById('favorite-quote-container');
  favoriteQuoteContainer.innerText = favoriteQuote;
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

