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

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawCommentChart);

/** Creates a pie chart for comments mood and adds it to the page. */
function drawCommentChart() {
  fetch('/comments-mood').then(response => response.json()).then((moodCounts) => {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Mood');
    data.addColumn('number', 'Count');
    Object.keys(moodCounts).forEach((mood) => {
      data.addRow([mood, moodCounts[mood]]);
    });

    const options = {
      'width': 450,
      'height': 350,
    };

    const chart = new google.visualization.PieChart(
      document.getElementById('mood-chart-container'));
    chart.draw(data, options);
  });
}
