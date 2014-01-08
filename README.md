joda-time-android
=================

This library is a version of [Joda-Time](https://github.com/JodaOrg/joda-time) built with Android in mind.

Usage
=====

TODO

Why?
====

I know what you are thinking: Joda-Time is a great library and it's just a single JAR, so why make things more complex by wrapping it in an Android library?

There is a particular problem with the JAR setup on Android: due to its usage of [ClassLoader.getResourceAsStream()](http://developer.android.com/reference/java/lang/ClassLoader.html#getResourceAsStream%28java.lang.String%29), it greatly inflates its memory footprint on apps.  (For more details, see [this blog post](http://daniel-codes.blogspot.com/2013/08/joda-times-memory-issue-in-android.html).)  This library avoids the problem for Android by loading from resources instead of a JAR.

We also have the opportunity to improve Joda-Time usage for the Android platform (such as implementing Parcelable for classes).