package com.sia.alarm_calendar;

import java.util.Map;

/**
 * Created by  on 2021/4/27.
 */
class CalendarEvent {

  String calendarId;
  String title;
  String description;
  long beginTime;
  long endTime;
  String rrule;

  public CalendarEvent(String calendarId, String title, String description, long beginTime, long endTime, String rrule) {
    this.calendarId = calendarId;
    this.title = title;
    this.description = description;
    this.beginTime = beginTime;
    this.endTime = endTime;
    this.rrule = rrule;
  }

  public CalendarEvent() {

  }


  public static CalendarEvent parseEvent(Object o) {
    Map<String, Object> json = (Map<String, Object>) o;
    CalendarEvent event = new CalendarEvent();
    event.calendarId = (String) json.get("calendarId");
    event.title = (String) json.get("title");
    event.description = (String) json.get("description");
    event.beginTime = (long) json.get("beginTime");
    event.endTime = (long) json.get("endTime");
    event.rrule = (String) json.get("rrule");
    return event;
  }
}
