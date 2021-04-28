import 'dart:async';

import 'package:alarm_calendar/calendar_event.dart';
import 'package:flutter/services.dart';

class AlarmCalendar {
  static const MethodChannel _channel = const MethodChannel('alarm_calendar');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<CalendarInsertResult> insertEvent(CalendarEvent event) async {
    final Map<dynamic, dynamic> result =
        await _channel.invokeMethod('insertEvent', event.toMap());
    return CalendarInsertResult.parse(result);
  }
}
