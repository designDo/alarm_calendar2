package com.sia.alarm_calendar;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by  on 2021/4/27.
 */
public class CalendarProvider {

  public static class CalendarInsertResult {
    boolean success;
    String message;

    public CalendarInsertResult(boolean success, String message) {
      this.success = success;
      this.message = message;
    }

    Map<String, Object> toJson() {
      Map<String, Object> json = new HashMap<>();
      json.put("success", success);
      json.put("message", message);
      return json;
    }
  }

  private static final Uri CALENDAR_URI = CalendarContract.Calendars.CONTENT_URI;
  private static final Uri CALENDAR_EVENT_URI = CalendarContract.Events.CONTENT_URI;
  private static final Uri CALENDAR_REMINDER_URI = CalendarContract.Reminders.CONTENT_URI;

  private static String CALENDARS_NAME = "TimeFly";
  private static String CALENDARS_ACCOUNT_NAME = "CALENDARS_ACCOUNT_NAME";
  private static String CALENDARS_ACCOUNT_TYPE = "CALENDARS_ACCOUNT_TYPE";
  private static String CALENDARS_DISPLAY_NAME = "CALENDARS_DISPLAY_NAME";


  public static CalendarInsertResult insertEvent(Context context, CalendarEvent event) {
    try {
      int calendarId = checkAndAddCalendarAccounts(context);
      if (calendarId == -1) {
        return new CalendarInsertResult(false, "您的手机可能不存在日历账户");
      }

      //查找是否有该事件，origin_id 标记为习惯id.若事件存在则先删除
      int deleteRow = queryAndDeleteCalendarEvent(context, calendarId, event.calendarId);

      // 准备event
      ContentValues valueEvent = new ContentValues();
      valueEvent.put(CalendarContract.Events.CALENDAR_ID, calendarId);
      valueEvent.put(CalendarContract.Events.EVENT_LOCATION, "TimeFly");
      valueEvent.put(CalendarContract.Events.TITLE, event.title);
      valueEvent.put(CalendarContract.Events.DESCRIPTION, event.calendarId);
      valueEvent.put(CalendarContract.Events.DTSTART, event.beginTime);
      valueEvent.put(CalendarContract.Events.DTEND, event.endTime);
      valueEvent.put(CalendarContract.Events.RRULE, event.rrule);
      valueEvent.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
      valueEvent.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai");

      Uri insertEventUri = context.getContentResolver().insert(CALENDAR_EVENT_URI, valueEvent);
      if (insertEventUri == null) {
        return new CalendarInsertResult(false, deleteRow > -1 ? "更新" : "新建" + "日历事件失败，您可以到日历中手动添加提醒事件");
      }
      // 添加提醒
      long eventId = ContentUris.parseId(insertEventUri);
      ContentValues valueReminder = new ContentValues();
      valueReminder.put(CalendarContract.Reminders.EVENT_ID, eventId);
      valueReminder.put(CalendarContract.Reminders.MINUTES, 5);
      valueReminder.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
      Uri insertReminderUri = context.getContentResolver().insert(CALENDAR_REMINDER_URI, valueReminder);
      if (insertReminderUri == null) {
        return new CalendarInsertResult(false, "日历事件提醒设置失败，您可以到日历中手动设置提醒");
      }
      return new CalendarInsertResult(true, "日历事件提醒已" + (deleteRow > -1 ? "更新" : "添加") + "！");
    } catch (Exception e) {
      return new CalendarInsertResult(false, "Ops！日历事件设置遇到不可预期的错误，您可以到日历中手动添加事件");
    }
  }


  /**
   * 查询日历事件
   *
   * @param context context
   * @param originId 事件 habit id
   * @return 事件id, 查询不到则返回""
   */
  private static int queryAndDeleteCalendarEvent(Context context, long calendarId, String originId) {
    int deleteRow = -1;
    Cursor cursor = context.getContentResolver().query(CALENDAR_EVENT_URI, null, null, null, null);
    try {
      long temp_calendar_id;
      String temp_origin_id;
      if (cursor != null && cursor.moveToFirst()) {
        do {
          temp_calendar_id = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
          temp_origin_id = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
          if (TextUtils.equals(originId, temp_origin_id) && calendarId == temp_calendar_id) {
            //取得id
            int id = cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars._ID));
            Uri deleteUri = ContentUris.withAppendedId(CALENDAR_EVENT_URI, id);
            deleteRow = context.getContentResolver().delete(deleteUri, null, null);
            Log.e("AAA", deleteRow + "");
            return deleteRow;
          }
        } while (cursor.moveToNext());
      }
    } catch (Exception e) {

    } finally {
      if (cursor != null)
        cursor.close();
      return deleteRow;
    }
  }


  /**
   * 获取日历ID
   *
   * @param context
   * @return 日历ID
   */
  private static int checkAndAddCalendarAccounts(Context context) {
    int oldId = checkCalendarAccounts(context);
    if (oldId >= 0) {
      return oldId;
    } else {
      long addId = addCalendarAccount(context);
      if (addId >= 0) {
        return checkCalendarAccounts(context);
      } else {
        return -1;
      }
    }
  }

  /**
   * 检查是否存在日历账户
   *
   * @param context
   * @return
   */
  private static int checkCalendarAccounts(Context context) {
    Cursor userCursor = context.getContentResolver().query(CALENDAR_URI, null, null, null, CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " ASC ");
    try {
      if (userCursor == null)//查询返回空值
        return -1;
      int count = userCursor.getCount();
      if (count > 0) {//存在现有账户，取第一个账户的id返回
        userCursor.moveToLast();
        return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
      } else {
        return -1;
      }
    } catch (Exception e) {
      return -1;
    } finally {
      if (userCursor != null) {
        userCursor.close();
      }
    }
  }


  /**
   * 添加一个日历账户
   *
   * @param context
   * @return
   */
  private static long addCalendarAccount(Context context) {
    TimeZone timeZone = TimeZone.getDefault();
    ContentValues value = new ContentValues();
    value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);

    value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
    value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
    value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
    value.put(CalendarContract.Calendars.VISIBLE, 1);
    value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
    value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
    value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
    value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
    value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
    value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

    Uri calendarUri = CALENDAR_URI;
    calendarUri = calendarUri.buildUpon()
        .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
        .build();

    Uri result = context.getContentResolver().insert(calendarUri, value);
    return result == null ? -1 : ContentUris.parseId(result);
  }
}
