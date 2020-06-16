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

  /** 
   * Finds a list of times when the requested event can happen, 
   * given the list of current events and the request information. 
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    long duration = request.getDuration();

    Map<String, List<TimeRange>> optionalAttendeesEventTimes = 
        getAttendeesEventTimes(events, attendees, optionalAttendees);

    Collection<TimeRange> possibleTimes = new ArrayList<>();
    if (attendees.isEmpty()) {
      // If there is no mandatory attendee, only consider availabilities of optional attendees.
      List<TimeRange> eventTimes = new ArrayList<>();
      for (List<TimeRange> timeList : optionalAttendeesEventTimes.values()) {
        eventTimes.addAll(timeList);
      }
      Collections.sort(eventTimes, TimeRange.ORDER_BY_START);
      possibleTimes = findTimeRangeGaps(eventTimes, duration);
    } else {
      // If there are mandatory attendees, consider optional attendees who can possibly attend and ignore those who can't. 
      // TODO: Find time slot(s) that maximize the number of optional attendees who can attend.
      List<TimeRange> eventTimes = new ArrayList<>(attendeesEventTimes);
      for (List<TimeRange> timeList : optionalAttendeesEventTimes.values()) {
        eventTimes.addAll(timeList);
        Collections.sort(eventTimes, TimeRange.ORDER_BY_START);
        possibleTimes = findTimeRangeGaps(eventTimes, duration);
        if (possibleTimes.isEmpty()) {
          // If adding the optional attendee leaves no time slot for the event, remove them.
          eventTimes.removeAll(timeList);
        }
      }
      possibleTimes = findTimeRangeGaps(eventTimes, duration);
    }
    return possibleTimes;
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
   * Loads the timeRanges of events that involve any of the mandatory attendees.
   * Returns a map from the names of optional attendees to lists of time ranges of events that involve them.
   */
  private Map<String, List<TimeRange>> getAttendeesEventTimes(
      Collection<Event> events, Collection<String> attendees, Collection<String> optionalAttendees) {
    attendeesEventTimes = new ArrayList<>();
    Map<String, List<TimeRange>> optionalAttendeesEventTimes = new HashMap<>();
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
            optionalAttendeesEventTimes.put(currentAttendee, Arrays.asList(currentEvent.getWhen()));
          }
        }
      }
    }
    return optionalAttendeesEventTimes;
  }
}
