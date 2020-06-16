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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  /** 
   * Finds a list of times when the requested event can happen, 
   * given the list of current events and the request information. 
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendees = request.getAttendees();
    long duration = request.getDuration();

    List<TimeRange> eventTimes = getOtherEventsTimes(events, attendees);

    Collections.sort(eventTimes, TimeRange.ORDER_BY_START);
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
   * Returns the time ranges of events that involve any of the requested attendees.
   */
  private List<TimeRange> getOtherEventsTimes(Collection<Event> events, Collection<String> attendees) {
    List<TimeRange> eventTimes = new ArrayList<>();
    Iterator<Event> eventsIterator = events.iterator();
    while(eventsIterator.hasNext()) {
      Event currentEvent = eventsIterator.next();
      if (attendeesOverlap(currentEvent.getAttendees(), attendees)) {
        eventTimes.add(currentEvent.getWhen());
      }
    }
    return eventTimes;
  }

  private boolean attendeesOverlap(Set<String> eventAttendees, Collection<String> requestedAttendees) {
    Iterator<String> attendeesItr = eventAttendees.iterator();
    while(attendeesItr.hasNext()) {
      // QUESTION: does requestedAttendees still work as a HashSet here as it is originally initialized?
      if (requestedAttendees.contains(attendeesItr.next())) {
        return true;
      }
    }
    return false;
  }
}
