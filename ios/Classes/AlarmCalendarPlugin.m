#import "AlarmCalendarPlugin.h"
#if __has_include(<alarm_calendar/alarm_calendar-Swift.h>)
#import <alarm_calendar/alarm_calendar-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "alarm_calendar-Swift.h"
#endif

@implementation AlarmCalendarPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftAlarmCalendarPlugin registerWithRegistrar:registrar];
}
@end
