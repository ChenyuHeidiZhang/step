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

let map;
const flagIcon = 'https://developers.google.com/maps/documentation/javascript/examples/full/images/beachflag.png';

/* Editable marker that displays when a user clicks in the map. */
let editMarker;

/** Creates a map that shows travel markers and allows user to input markers. */
function createMap() {
  map = new google.maps.Map(
      document.getElementById('gallery-map'),
      {center: {lat: 39.329858, lng: -76.620540}, zoom: 5});
  
  // Show a marker with a text box that the user can edit when they click on the map.
  map.addListener('click', (event) => {
    createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
  });

  createMarkerForDisplay(
      39.329858, -76.620540,
      'I go to school here.', 'Johns Hopkins University', false)
  createMarkerForDisplay(
      32.003407, 118.734956,
      'This is where I was born.', 'Nanjing', false)
  createMarkerForDisplay(
      37.423829, -122.092154,
      'This is where my internship was supposed to be, but now it\'s remote.', 'Google', false);
  createMarkerForDisplay(
      23.117487, -82.373375,
      'I went there with my friends. It was great fun.', 'Havana', false);
  createMarkerForDisplay(
      38.642805, -90.195679,
      'I worked here during summer, 2019.', 'St Louis', false);

  fetchMarkers();
}

/** 
 * Adds a marker that shows an info window when clicked. 
 * @param {number} lat Latitude of the marker position.
 * @param {number} lng Longitude of the marker position.
 * @param {string} content The description of the marker, which is displayed onclick.
 * @param {string=} title Optional title of the marker, which is displayed on hover.
 * @param {boolean=} defaultIcon Whether to display the default marker icon or the flagIcon. 
 */
function createMarkerForDisplay(lat, lng, content, title = '', defaultIcon = true) {
  let marker;
  if (defaultIcon == false) {
    marker = new google.maps.Marker(
        {position: {lat: lat, lng: lng}, map: map, title: title, icon: flagIcon});
  } else {
    marker = new google.maps.Marker(
      {position: {lat: lat, lng: lng}, map: map});
  }

  const infoWindow = new google.maps.InfoWindow({content: content});
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

/** 
 * Creates a marker that shows a textbox the user can edit. 
 * @param {number} lat Latitude of the marker position.
 * @param {number} lng Longitude of the marker position.
 */
function createMarkerForEdit(lat, lng) {
  // Remove any editable marker that is already being displayed.
  if (editMarker) {
    editMarker.setMap(null);
  }

  editMarker = new google.maps.Marker({position: {lat: lat, lng: lng}, map: map});

  const infoWindow = new google.maps.InfoWindow({content: buildInfoWindowInput(lat, lng)});

  // Remove the marker when the user closes the editable info window.
  google.maps.event.addListener(infoWindow, 'closeclick', () => {
    editMarker.setMap(null);
  });

  infoWindow.open(map, editMarker);
}

/** 
 * Builds and returns HTML elements that show an editable textbox and a submit button. 
 * @param {number} lat Latitude of the marker associated with the pop up window.
 * @param {number} lng Longitude of the marker associated with the pop up window.
 */
function buildInfoWindowInput(lat, lng) {
  const textBox = document.createElement('textarea');
  const button = document.createElement('button');
  button.appendChild(document.createTextNode('Submit'));

  button.onclick = () => {
    postMarker(lat, lng, textBox.value);
    createMarkerForDisplay(lat, lng, textBox.value);
    editMarker.setMap(null);
  };

  const infoWindowContainer = document.createElement('div');
  infoWindowContainer.appendChild(textBox);
  infoWindowContainer.appendChild(document.createElement('br'));
  infoWindowContainer.appendChild(button);

  return infoWindowContainer;
}

/**
 * Sends a marker to the backend for saving. 
 * @param {number} lat Latitude of the marker position.
 * @param {number} lng Longitude of the marker position.
 * @param {string} content The content description of the marker.
 */
function postMarker(lat, lng, content) {
  const params = new URLSearchParams();
  params.append('lat', lat);
  params.append('lng', lng);
  params.append('content', content);

  fetch('/markers', {method: 'POST', body: params});
}
