# Changelog

# 2.9.1 *(2015-12-05)*

* [#103](https://github.com/dlew/joda-time-android/pull/103) Fixed missing Arctic/Longyearbyen timezone
* [#97](https://github.com/dlew/joda-time-android/pull/97) Updated to joda-time 2.9.1

# 2.9.0 *(2015-10-25)*

* [#91](https://github.com/dlew/joda-time-android/pull/91) Updated to joda-time 2.9
* [#88](https://github.com/dlew/joda-time-android/pull/88) Updated to tzdata 2015g
* [#85](https://github.com/dlew/joda-time-android/pull/85) Fixed resource handling on some OSes by shortening library prefix

# 2.8.2 *(2015-08-11)*

* [#76](https://github.com/dlew/joda-time-android/pull/76) Removed Eclipse library support. You'll just have to learn to love Gradle, or you can use an old version of this library and update the tzdata yourself manually.
* [#80](https://github.com/dlew/joda-time-android/pull/80) Updated to joda-time 2.8.2
* [#82](https://github.com/dlew/joda-time-android/pull/82) Updated to tzdata 2015f

# 2.8.1 *(2015-06-16)*

* [#64](https://github.com/dlew/joda-time-android/pull/64) Automatically include proguard files
* [#65](https://github.com/dlew/joda-time-android/pull/65) Updated to joda-time 2.8.1
* [#67](https://github.com/dlew/joda-time-android/pull/67) Updated to tzdata 2015e

# 2.8.0 *(2015-06-02)*

* [#47](https://github.com/dlew/joda-time-android/pull/47) Fixed compatibility w/ Android Gradle plugin 1.3
* [#51](https://github.com/dlew/joda-time-android/pull/51) Updated to joda-time 2.8
* [#52](https://github.com/dlew/joda-time-android/pull/52) Updated to tzdata 2015d
* [#57](https://github.com/dlew/joda-time-android/pull/57) Added instructions for proguarding and using joda-convert

# 2.7.2 *(2015-04-12)*

* Updated to tzdata 2015b

# 2.7.1 *(2015-02-11)*

* Updated to tzdata 2015a
* Use joda-time's ZoneInfoCompiler (for bugfixes/consistency)

# 2.7.0 *(2015-01-19)*

This release includes couple (possibly) breaking changes to previous versions of joda-time-android, depending on how
your project was configured.

#### Dependencies

Previously, joda-time-android included joda-time sources itself. Due to improvements made to the main project, it is
now possible for us to properly use the joda-time dependency. This means that:

1. If you are using Gradle, nothing should change, unless you were doing something weird.

2. If you're using Eclipse libraries, you will have to manually include joda-time.jar yourself now; make sure to
   get the no-tzdb artifact.

#### Initialization

Initially you initialized the library via `ResourceZoneInfoProvider.init()`. Later this was moved to
`JodaTimeAndroid.init()`. We're removing the other endpoint because it just makes this more difficult to
maintain; it should be simple to change from one to the other.

#### Other changes

* Updated to joda-time 2.7

# 2.6.0 *(2014-12-20)*

* Updated to joda-time 2.6

# 2.5.1 *(2014-11-15)*

* Modified how default DateTimeZone is updated by system (#20)
* Updated to tzdata 2014j

# 2.5.0 *(2014-11-02)*

* Updated to joda-time 2.5
* Updated to tzdata 2014i

# 2.4.1 *(2014-10-02)*

* Updated to tzdata 2014h

# 2.4.0 *(2014-08-02)*

* Updated to joda-time 2.4

# 2.3.4 *(2014-06-14)*

* `#10` Receive TIMEZONE_CHANGED broadcasts only when running, add JodaTimeAndroid.init
* Updated to tzdata 2014e

# 2.3.3 *(2014-04-20)*

* `#4` Added library sample app
* `#9` Reset default DateTimeZone when TIMEZONE_CHANGED broadcast is received

## 2.3.2 *(2014-04-09)*

* `#7` Added DateUtils.formatDuration()

## 2.3.1 *(2014-04-08)*

* `#5` Add compatibility version of DateUtils
* `#6` Fixed lint error

## 2.3 *(2014-01-25)*

* Initial port of joda-time to Android