import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:alarm_calendar/alarm_calendar.dart';
import 'package:alarm_calendar/calendar_event.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  CalendarInsertResult success;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await AlarmCalendar.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text('Running on: $_platformVersion'),
              GestureDetector(
                onTap: () async {
                  var value = await AlarmCalendar.insertEvent(CalendarEvent(
                      'abmww',
                      'Title4',
                      'Note',
                      DateTime(2021, 4, 28, 20, 21),
                      DateTime(2021, 4, 28, 22, 0),
                      'FREQ=DAILY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU'));
                  print(value.success);
                  print(value.message);
                },
                child: Container(
                  width: 100,
                  height: 100,
                  color: Colors.redAccent,
                  child: Text('新建'),
                ),
              ),
              GestureDetector(
                onTap: () async {
                  var value = await AlarmCalendar.insertEvent(CalendarEvent(
                      '111',
                      'Title5',
                      'Note',
                      DateTime(2021, 4, 27, 20, 21),
                      DateTime(2021, 4, 27, 22, 0),
                      'FREQ=DAILY'));
                  print(value.success);
                  print(value.message);
                },
                child: Container(
                  width: 100,
                  height: 100,
                  color: Colors.redAccent,
                  child: Text('修改'),
                ),
              ),
              GestureDetector(
                onTap: () async {
                  var value = await AlarmCalendar.insertEvent(CalendarEvent(
                      '1112',
                      'Week2',
                      'Week Note',
                      DateTime(2021, 4, 27, 20, 21),
                      DateTime(2021, 4, 27, 22, 0),
                      'FREQ=WEEKLY;WKST=SU;BYDAY=MO,TU,WE,FR,SA,SU'));
                  setState(() {
                    success = value;
                  });
                },
                child: Container(
                  width: 100,
                  height: 100,
                  color: Colors.redAccent,
                  child: Text('$success'),
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}
