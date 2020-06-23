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

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FindMeetingQuery {
  private List<TimeRange> attendeesEventTimes;
  private Map<String, List<TimeRange>> optionalAttendeesEventTimes;
  private List<List<String>> optionalAttendeesCombs;

  /** 
   * Finds a list of times when the requested event can happen, 
   * given the list of current events and the request information. 
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendees = new ArrayList<>(request.getAttendees());
    Collection<String> optionalAttendees = new ArrayList<>(request.getOptionalAttendees());
    // On the webapp, if form is left empty, an empty string is obtained. Remove them here.
    attendees.removeAll(Collections.singleton(""));
    optionalAttendees.removeAll(Collections.singleton(""));

    long duration = request.getDuration();

    // Populate {@code attendeesEventTimes} and {@code optionalAttendeesEventTimes}.
    getAttendeesEventTimes(events, attendees, optionalAttendees);

    Collection<TimeRange> possibleTimes = new ArrayList<>();
    if (attendees.isEmpty()) {
      // If there is no mandatory attendee, only consider availabilities of optional attendees.

      // {@code eventTimes} is the list of current events' time ranges that need to be avoided when scheduling.
      List<TimeRange> eventTimes = new ArrayList<>();
      for (List<TimeRange> timeList : optionalAttendeesEventTimes.values()) {
        eventTimes.addAll(timeList);
      }
      Collections.sort(eventTimes, TimeRange.ORDER_BY_START);
      possibleTimes = findTimeRangeGaps(eventTimes, duration);
    } else {
      // If there are mandatory attendees, consider optional attendees who can possibly attend and ignore those who can't. 

      List<TimeRange> eventTimes = new ArrayList<>(attendeesEventTimes);

      // Find the time slot(s) that maximize the number of optional attendees who can attend.
      // Count down from the maximum number of optional attendees and find all combinations of that number of attendees.
      // Once we find a combination where scheduling is possible, we've found the maximal group of attendees.
      for (int num = optionalAttendees.size(); num > 0; num--) {
        String[] arr = optionalAttendees.toArray(new String[optionalAttendees.size()]);
        getAllCombinations(arr, arr.length, num);

        for (List<String> optionalAttendeesChosen : optionalAttendeesCombs) {
          for (String attendee : optionalAttendeesChosen) {
            eventTimes.addAll(optionalAttendeesEventTimes.get(attendee));
          }
          Collections.sort(eventTimes, TimeRange.ORDER_BY_START);
          possibleTimes = findTimeRangeGaps(eventTimes, duration);
          if (!possibleTimes.isEmpty()) {
            // We've found a combination where scheduling is possible, so those are the desired time slot(s).
            return possibleTimes;
          }
          // Re-initialize unavailable time ranges with those of the mandatory attendees.
          eventTimes = new ArrayList<>(attendeesEventTimes);
        }
      }
      // Calculate time ranges when there are no optional attendees, or none of the optional attendees can fit the schedule.
      possibleTimes = findTimeRangeGaps(eventTimes, duration);
    }
    return possibleTimes;
  }

  /**
   * Recursive helper function used to find all combinations of size r of the input array arr[].
   * chosen[]: temporary array to store current combination.
   * start & end: staring and ending indexes in arr[].
   * index: current index in data[].
   */
  private void combinationUtil(String arr[], String chosen[], int start, int end, int index, int r) { 
    // If current combination is ready to be added, then add it.
    if (index == r) {
      optionalAttendeesCombs.add(Arrays.asList(chosen));
      return;
    }

    // Replace index with all possible elements. 
    // The condition "end-i+1 >= r-index" makes sure that including one element
    // at index will make a combination with remaining elements at remaining positions.
    for (int i = start; i <= end && end - i + 1 >= r - index; i++) { 
      chosen[index] = arr[i];
      combinationUtil(arr, chosen, i + 1, end, index + 1, r);
    }
  }

  /**
   * Returns all combinations of size r in arr[], which has size n.
   * This function mainly uses combinationUtil().
   */
  private void getAllCombinations(String arr[], int n, int r) { 
    optionalAttendeesCombs = new ArrayList<>();

    // A temporary array to store all combination one by one.
    String chosen[] = new String[r]; 

    combinationUtil(arr, chosen, 0, n - 1, 0, r); 
  }

  /** 
   * Finds a collection of timeRanges that are gaps among the timeRanges in {@code eventTimes}
   * that have lengths longer than {@code duration}.
   */
  private Collection<TimeRange> findTimeRangeGaps(List<TimeRange> eventTimes, long duration) {
    Collection<TimeRange> possibleTimes = new ArrayList<>();
    int lastEndTime = TimeRange.START_OF_DAY;
    TimeRange currentRange;
    for (int i = 0; i < eventTimes.size(); i++) {
      currentRange = eventTimes.get(i);
      if (currentRange.start() <= lastEndTime) {
        // If there is an overlap, update the previous end marker.
        lastEndTime = Math.max(lastEndTime, currentRange.end());  
      } else {
        // If there is a gap, check if it can fit the requested event.
        if (currentRange.start() - lastEndTime >= duration) {
          possibleTimes.add(TimeRange.fromStartEnd(lastEndTime, currentRange.start(), false));
        }
        lastEndTime = currentRange.end();
      }
    }

    if (TimeRange.END_OF_DAY - lastEndTime >= duration) {
      possibleTimes.add(TimeRange.fromStartEnd(lastEndTime, TimeRange.END_OF_DAY, true));
    }
    return possibleTimes;
  }

  /**
   * Loads {@code attendeesEventTimes} with the time ranges of events that involve any of the mandatory attendees.
   * Loads {@code optionalAttendeesEventTimes} with a map from the names of optional attendees 
   * to lists of time ranges of events that involve them.
   */
  private void getAttendeesEventTimes(
      Collection<Event> events, Collection<String> attendees, Collection<String> optionalAttendees) {
    attendeesEventTimes = new ArrayList<>();
    optionalAttendeesEventTimes = new HashMap<>();
    Iterator<Event> eventsIterator = events.iterator();
    while (eventsIterator.hasNext()) {
      Event currentEvent = eventsIterator.next();
      Iterator<String> attendeesItr = currentEvent.getAttendees().iterator();
      while (attendeesItr.hasNext()) {
        String currentAttendee = attendeesItr.next();
        // QUESTION: does attendees still work as a HashSet here as it is originally initialized?
        if (attendees.contains(currentAttendee)) {
          // If a mandatory attendee is in a current event, add the event time to the list.
          attendeesEventTimes.add(currentEvent.getWhen());
        }
        if (optionalAttendees.contains(currentAttendee)) {
          // If an optional attendee is in a current event, add the event time to the map.
          if (optionalAttendeesEventTimes.containsKey(currentAttendee)) {
            optionalAttendeesEventTimes.get(currentAttendee).add(currentEvent.getWhen());
          } else {
            optionalAttendeesEventTimes.put(currentAttendee, 
                new ArrayList<>(Arrays.asList(currentEvent.getWhen())));
          }
        }
      }
    }
  }
}
