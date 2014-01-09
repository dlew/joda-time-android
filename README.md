joda-time-android
=================

This library is a version of [Joda-Time](https://github.com/JodaOrg/joda-time) built with Android in mind.

Usage
=====

Currently, the only way to use it is to import it into your project as a library.  Coming soon: apklib/aar support.

Once it's building, you will need to initialize the timezone provider (via `ResourceZoneInfoProvider.init()`) before you can start using DateTime objects.  I suggest putting this code in `Application.onCreate()`:

    ResourceZoneInfoProvider.init(this);

Why Joda-Time?
==============

Android has built-in date and time handling - why bother with a library?  If you've worked with Java's Date and Calendar classes you can probably answer this question yourself, but if not, check out [Joda-Time's list of benefits](http://www.joda.org/joda-time/#Why_Joda-Time).

For Android developers in particular Joda-Time solves one critical problem: stale timezone data.  Built-in timezone data is only updated when the OS is updated, and we all know how often that happens for most devices.  [Countries modify their timezones all the time](http://www.bbc.co.uk/news/world-europe-15512177); being able to update your own data keeps your app accurate on all devices.

Why This Library?
=================

I know what you are thinking: Joda-Time is a great library and it's just a single JAR, so why make things more complex by wrapping it in an Android library?

There is a particular problem with the JAR setup on Android: due to its usage of [ClassLoader.getResourceAsStream()](http://developer.android.com/reference/java/lang/ClassLoader.html#getResourceAsStream%28java.lang.String%29), it greatly inflates its memory footprint on apps.  (For more details, see [this blog post](http://daniel-codes.blogspot.com/2013/08/joda-times-memory-issue-in-android.html).)  This library avoids the problem for Android by loading from resources instead of a JAR.

We also have the opportunity to improve Joda-Time usage for the Android platform (such as implementing Parcelable for classes).