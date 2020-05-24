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


/* toggles the drop down text */
function toggleDropDown(id) {
  document.getElementById(id).classList.toggle("show");
}

/* display the content corresponding to the tab-button clicked */
function displayGallery(event, tab_name) {
  // hide all tab contents
  var tab_contents = document.getElementsByClassName("tab-content");
  for (var i = 0; i < tab_contents.length; i++) {
    tab_contents[i].style.display = "none";
  }

  // remove the class "active" from the previously active tab-button
  //var active_button = document.getElementsByClassName("tab-button active");
  //active_button[0].classList.toggle("active");
  
  var tab_buttons = document.getElementsByClassName("tab-button");
  for (i = 0; i < tab_buttons.length; i++) {
    tab_buttons[i].className = tab_buttons[i].className.replace(" active", "");
  }
  
  // add an "active" class the tab-button clicked and show the corresponding tab content
  event.currentTarget.className += " active";
  document.getElementById(tab_name).style.display = "flex";

}

/**
 * Adds a favorite quote to the page.
 */
function addFavoriteQuote() {
  const quotes =
      ["Many years later, as he faced the firing squad, Colonel Aureliano Buendía was to remember that distant afternoon when his father took him to discover ice. \n —Gabriel García Márquez, One Hundred Years of Solitude",
      "Hope is a dangerous thing. Hope can drive a man insane. \n - The Shawshank Redemption",
      "Bran thought about it. 'Can a man still be brave if he's afraid?' 'That is the only time a man can be brave,' his father told him. \n ― George R.R. Martin, A Game of Thrones"];

  // Pick a random greeting.
  const quote = quotes[Math.floor(Math.random() * quotes.length)];

  // Add it to the page.
  const quoteContainer = document.getElementById('favorite-quote-container');
  quoteContainer.innerText = quote;
}

/**
 * Adds a fun fact to the page.
 */
function addFunFact() {
  const facts =
      ["Do you want to meet my sister?", 
      "I like all sorts of collections (books, stickers, snacks, etc)",
      "I like to take pictures of others but not to have my picture taken.",
      "My English name is Heidi. Many people can't pronounce that.",
      "Funny enough that most of my Chinese friends call me by my English name while my American friends call me by my Chinese name."];

  // Pick a random greeting.
  const fact= facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fun-fact-container');
  factContainer.innerText = fact;
}

