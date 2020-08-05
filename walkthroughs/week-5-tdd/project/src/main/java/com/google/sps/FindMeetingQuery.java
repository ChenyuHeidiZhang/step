// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the License
// is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied. See the License for the specific language governing permissions and limitations under
// the License.

package com.google.sps;

import com.google.common.collect.ImmutableList;
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
  private List<List<String>> optionalAttendeesCombinations;

  /** 
   * Finds a list of times when the requested event can happen, given the list of current events and
   * the request information. An event includes a list of attendees, a title, and a time range of
   * the event. The request includes lists of mandatory and optional attendees (either list can be
   * empty) and a duration of the meeting in minutes.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    HashSet<String> attendees = new HashSet<>(request.getAttendees());
    HashSet<String> optionalAttendees = new HashSet<>(request.getOptionalAttendees());
    // On the webapp, if form is left empty, an empty string is obtained. Remove them here.
    attendees.removeAll(Collections.singleton(""));
    optionalAttendees.removeAll(Collections.singleton(""));

    long duration = request.getDuration();

    // Populate {@code attendeesEventTimes} and {@code optionalAttendeesEventTimes}.
    getAttendeesEventTimes(events, attendees, optionalAttendees);

    Collection<TimeRange> possibleTimes = new ArrayList<>();
    if (attendees.isEmpty()) {
      // Case: No mandatory attendee, only consider availabilities of optional attendees.

      // A list of current events' time ranges that need to be avoided when scheduling.
      List<TimeRange> eventTimes = new ArrayList<>();
      for (List<TimeRange> timeList : this.optionalAttendeesEventTimes.values()) {
        eventTimes.addAll(timeList);
      }
      Collections.sort(eventTimes, TimeRange.ORDER_BY_START);
      possibleTimes = findTimeRangeGaps(eventTimes, duration);
    } else {
      // Case: There are mandatory attendees, consider optional attendees who can possibly attend
      // and ignore those who can't. 
      List<TimeRange> eventTimes = new ArrayList<>(this.attendeesEventTimes);

      // Find the time slot(s) that maximize the number of optional attendees who can attend. Count
      // down from the maximum number of optional attendees and find all combinations of that number
      // of attendees. Once we find a combination where scheduling is possible, we've found the
      // maximal group of attendees.
      for (int num = optionalAttendees.size(); num > 0; num--) {
        ImmutableList<String> optionalAttendeesList = ImmutableList.copyOf(optionalAttendees);
        // Get all combinations of size num of the optional attendees.
        getAllCombinations(optionalAttendeesList, optionalAttendeesList.size(), num);

        for (List<String> optionalAttendeesChosen : optionalAttendeesCombinations) {
          for (String attendee : optionalAttendeesChosen) {
            eventTimes.addAll(this.optionalAttendeesEventTimes.get(attendee));
          }
          Collections.sort(eventTimes, TimeRange.ORDER_BY_START);
          possibleTimes = findTimeRangeGaps(eventTimes, duration);
          if (!possibleTimes.isEmpty()) {
            // We've found a combination where scheduling is possible, so those are the desired time
            // slot(s).
            return possibleTimes;
          }
          // Re-initialize unavailable time ranges with those of the mandatory attendees.
          eventTimes = new ArrayList<>(attendeesEventTimes);
        }
      }
      // Calculate time ranges when there are no optional attendees, or none of the optional
      // attendees can fit the schedule.
      possibleTimes = findTimeRangeGaps(eventTimes, duration);
    }
    return possibleTimes;
  }

  /**
   * Recursive helper function used to find all combinations of the given size of the elements of
   * a given list.
   * @param inputList The list of optional attendees to consider. This is immutable.
   * @param currentCombination Temporary array to store current combination of optional attendees.
   * @param startIndex Starting index in the input array to consider for the current combination.
   * @param endIndex Ending index in the input array to consider for the current combination.
   * @param index Current index to be set in the current combination list.
   * @param size The size of combinations to be found.
   */
  private void combinationUtil(
      ImmutableList<String> inputList,
      ArrayList<String> currentCombination,
      int startIndex,
      int endIndex,
      int index,
      int size) {
    if (index == size) {
      // Combination is ready to be added, add it to the list.
      this.optionalAttendeesCombinations.add(currentCombination);
      return;
    }

    // Replace index with all possible elements. The condition "end-i+1 >= r-index" makes sure that
    // including one element at index will make a combination with remaining elements at remaining
    // positions.
    for (int i = startIndex; i <= endIndex && endIndex - i + 1 >= size - index; i++) {
      currentCombination.set(index, inputList.get(i));
      combinationUtil(inputList, currentCombination, i + 1, endIndex, index + 1, size);
    }
  }

  /**
   * Returns all combinations of the given size in the given list of optional attendees, which has 
   * size n.
   */
  private void getAllCombinations(ImmutableList<String> inputList, int n, int size) { 
    this.optionalAttendeesCombinations = new ArrayList<>();

    // A temporary array to store all possible combinations, one at a time.
    ArrayList<String> currentCombination = new ArrayList(Arrays.asList(new String[size]));

    combinationUtil(inputList, currentCombination, 0, n - 1, 0, size); 
  }

  /** 
   * Finds a collection of time ranges that occur as gaps between the specified event times and have
   * length longer than the specified duration.
   */
  private Collection<TimeRange> findTimeRangeGaps(List<TimeRange> eventTimes, long duration) {
    Collection<TimeRange> possibleTimes = new ArrayList<>();
    int lastEndTime = TimeRange.START_OF_DAY;
    TimeRange currentRange;
    for (int i = 0; i < eventTimes.size(); i++) {
      currentRange = eventTimes.get(i);
      if (currentRange.start() <= lastEndTime) {
        // Case: There is an overlap, update the previous end marker.
        lastEndTime = Math.max(lastEndTime, currentRange.end());  
      } else {
        // Case: There is a gap, check if it can fit the requested event.
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
   * Records the time ranges of events that involve any of the mandatory attendees. Records the
   * optional attendees and the corresponding event times that involve each of them.
   */
  private void getAttendeesEventTimes(
      Collection<Event> events, HashSet<String> attendees, HashSet<String> optionalAttendees) {
    this.attendeesEventTimes = new ArrayList<>();
    this.optionalAttendeesEventTimes = new HashMap<>();
    Iterator<Event> eventsIterator = events.iterator();
    while (eventsIterator.hasNext()) {
      Event currentEvent = eventsIterator.next();
      Iterator<String> attendeesItr = currentEvent.getAttendees().iterator();
      while (attendeesItr.hasNext()) {
        String currentAttendee = attendeesItr.next();
        if (attendees.contains(currentAttendee)) {
          // Add event time to list if it has a mandatory attendee.
          this.attendeesEventTimes.add(currentEvent.getWhen());
        }
        if (optionalAttendees.contains(currentAttendee)) {
          // Add event time to map if it has an optional attendee.
          if (this.optionalAttendeesEventTimes.containsKey(currentAttendee)) {
            this.optionalAttendeesEventTimes.get(currentAttendee).add(currentEvent.getWhen());
          } else {
            this.optionalAttendeesEventTimes.put(currentAttendee, 
                new ArrayList<>(Arrays.asList(currentEvent.getWhen())));
          }
        }
      }
    }
  }
}
