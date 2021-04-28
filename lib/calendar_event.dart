class CalendarEvent {
  final String calendarId;
  final String title;
  final String description;
  final DateTime beginTime;
  final DateTime endTime;
  final String rrule;

  CalendarEvent(this.calendarId, this.title, this.description, this.beginTime,
      this.endTime, this.rrule);

  Map<String, dynamic> toMap() {
    return {
      'calendarId': calendarId,
      'title': title,
      'description': description,
      'beginTime': beginTime.millisecondsSinceEpoch,
      'endTime': endTime.millisecondsSinceEpoch,
      'rrule': rrule
    };
  }
}

class CalendarInsertResult {
  final bool success;
  final String message;

  CalendarInsertResult(this.success, this.message);

  static CalendarInsertResult parse(Map<dynamic, dynamic> json) {
    return CalendarInsertResult(json['success'], json['message']);
  }
}
