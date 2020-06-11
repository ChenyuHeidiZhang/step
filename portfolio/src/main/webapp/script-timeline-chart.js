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

google.charts.load('current', {packages:['timeline']});
google.charts.setOnLoadCallback(drawTimelineChart);

/** 
 * Draws the timeline chart onto the DOM.
 */
function drawTimelineChart() {
  var timelineContainer = document.getElementById('timeline-container');
  var chart = new google.visualization.Timeline(timelineContainer);
  var dataTable = new google.visualization.DataTable();

  dataTable.addColumn({ type: 'string', id: 'InfoType' });
  dataTable.addColumn({ type: 'string', id: 'Item' });
  dataTable.addColumn({ type: 'date', id: 'Start' });
  dataTable.addColumn({ type: 'date', id: 'End' });
  // Note: months go from 0 to 11.
  dataTable.addRows([
    [ 'Location', 'Nanjing', new Date(2012, 8), new Date(2018, 7) ],
    [ 'Location', 'Baltimore', new Date(2018, 7), new Date() ],
    [ 'School', 'NFLS', new Date(2012, 8), new Date(2018, 7)],
    [ 'School', 'JHU', new Date(2018, 7), new Date()],
    [ 'Highlights', 'Cornell', new Date(2017, 5), new Date(2017, 7)],
    [ 'Highlights', 'Megapro', new Date(2019, 4), new Date(2019, 7)],
    [ 'Highlights', 'Google', new Date(2020, 4), new Date()],
  ]);

  chart.draw(dataTable);
}
