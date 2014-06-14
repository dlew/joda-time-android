joda-time-android
=================

This library is a version of [Joda-Time](https://github.com/JodaOrg/joda-time) built with Android in mind.

Why Joda-Time?
==============

Android has built-in date and time handling - why bother with a library?  If you've worked with Java's Date and Calendar classes you can probably answer this question yourself, but if not, check out [Joda-Time's list of benefits](http://www.joda.org/joda-time/#Why_Joda-Time).

For Android developers Joda-Time solves one critical problem: stale timezone data.  Built-in timezone data is only updated when the OS is updated, and we all know how often that happens.  [Countries modify](http://www.bbc.co.uk/news/world-europe-15512177) [their timezones](http://www.heraldsun.com.au/news/breaking-news/samoa-to-move-the-international-dateline/story-e6frf7jx-1226051660380) [all the](http://www.indystar.com/apps/pbcs.dll/article?AID=/20070207/LOCAL190108/702070524/0/LOCAL) [time](http://uk.reuters.com/article/oilRpt/idUKBLA65048420070916); being able to update your own tz data keeps your app up-to-date and accurate.

Why This Library?
=================

I know what you are thinking: Joda-Time is a great library and it's just a single JAR, so why make things more complex by wrapping it in an Android library?

There is a particular problem with the JAR setup on Android: due to its usage of [ClassLoader.getResourceAsStream()](http://developer.android.com/reference/java/lang/ClassLoader.html#getResourceAsStream%28java.lang.String%29), it greatly inflates its memory footprint on apps.  (For more details, see [this blog post](http://blog.danlew.net/2013/08/20/joda_time_s_memory_issue_in_android/).)  This library avoids the problem for Android by loading from resources instead of a JAR.

This library also has extra utilities designed for Android.  For example, see [DateUtils](library/src/net/danlew/android/joda/DateUtils.java), a port of Android's [DateUtils](http://developer.android.com/reference/android/text/format/DateUtils.html).

Usage
=====

You can either import this project as a plain old library project, or you can use it as an AAR from Maven Central.

If you're using maven:

    <dependency>
      <groupId>net.danlew</groupId>
      <artifactId>android.joda</artifactId>
      <version>2.3.4</version>
      <type>aar</type>
    </dependency>

If you're using gradle:

    dependencies {
      compile 'net.danlew:android.joda:2.3.4'
    }

Once the project is imported, you **must** initialize the timezone provider and TIMEZONE_CHANGED broadcast receiver with a `Context` (via `JodaTimeAndroid.init()`) before you can start using this library.  I suggest putting this code in `Application.onCreate()`:

    public class MyApp extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
          
            JodaTimeAndroid.init(this);
        }
    }

Updating the TimeZone database
==============================

1. Download the latest Time Zone Data package from [IANA](http://www.iana.org/time-zones).  You will need only the tzdata package.
2. Unzip/untar the tzdata package and copy files from the package into the `tzdata/` folder in this repository.
3. Run the following command

        ./gradlew updateTzData

4. Re-compile the library
