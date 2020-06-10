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


/*
 * Toggles the drop down text.
 */
function toggleDropDown(id) {
  document.getElementById(id).classList.toggle("show");
}

/*
 * Adds a random favorite quote to the page.
 */
function addRandomFavoriteQuote() {
  const favoriteQuotes = [
    'Many years later, as he faced the firing squad, Colonel Aureliano Buendía was to remember that distant afternoon when his father took him to discover ice. \n —Gabriel García Márquez, One Hundred Years of Solitude',
    'Hope is a dangerous thing. Hope can drive a man insane. \n - The Shawshank Redemption',
    'Bran thought about it. "Can a man still be brave if he\'s afraid?" "That is the only time a man can be brave," his father told him. \n ― George R.R. Martin, A Game of Thrones',
  ];

  // Picks a random quote.
  const randomFavoriteQuote = favoriteQuotes[Math.floor(Math.random() * favoriteQuotes.length)];

  // Adds the favorite quote to the page.
  const favoriteQuoteContainer = document.getElementById('favorite-quote-container');
  favoriteQuoteContainer.innerText = randomFavoriteQuote;
}

/*
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

  // Picks a random index for the fun fact.
  const index = Math.floor(Math.random() * (funFactsJsIds.length + funFactsStrings.length));

  // Adds the fun fact to the page.
  const funFactContainer = document.getElementById('fun-fact-container');
  if (index < funFactsJsIds.length) {
    funFactContainer.innerHTML = document.getElementById(funFactsJsIds[index]).innerText;
  } else {
    funFactContainer.innerText = funFactsStrings[index - funFactsJsIds.length];
  }
}

/** Creates a map that shows landmarks around Google. */
function createMap() {
  const map = new google.maps.Map(
      document.getElementById('map'),
      {center: {lat: 39.329858, lng: -76.620540}, zoom: 5});
  
  // When the user clicks in the map, show a marker with a text box the user can
  // edit.
  map.addListener('click', (event) => {
    createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
  });

  addLandmark(
      map, 39.329858, -76.620540, 'Johns Hopkins University',
      'I go to Johns Hopkins University.')
  addLandmark(
      map, 32.003407, 118.734956, 'Nanjing',
      'This is where I was born.')
  addLandmark(
      map, 37.423829, -122.092154, 'Google',
      'This is where my internship was supposed to be, but now it\'s remote.');
  addLandmark(
      map, 23.117487, -82.373375, 'Havana',
      'I went there with my friends. It was great fun.');
  addLandmark(
      map, 38.642805, -90.195679, 'St Louis',
      'I worked here during summer, 2019.');

  fetchMarkers();
}

/** Adds a marker that shows an info window when clicked. */
function addLandmark(map, lat, lng, title, description) {
  const marker = new google.maps.Marker(
      {position: {lat: lat, lng: lng}, map: map, title: title});

  const infoWindow = new google.maps.InfoWindow({content: description});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}

/** Fetches markers from the backend and adds them to the map. */
function fetchMarkers() {
  fetch('/markers').then(response => response.json()).then((markers) => {
    markers.forEach(
        (marker) => {
            createMarkerForDisplay(marker.lat, marker.lng, marker.content)});
  });
}

