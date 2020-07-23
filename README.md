<p align="center"><img src="assets/logo.png" alt="ChangeDetection" height="200px"></p>

Voice Search
=================

Speech to Text Searching Assistant without Ads makes it faster and easier than ever to search apps, web search, or image search for the content you want. It works as an assistant to apps installed in your device, and allows you to turn normal talk into searching text.

🔍🎙Download Voice Search - Speech to Text Searching Assistant without Ads now from Google Play, speak to your mic and start searching easier, faster and smarter!🎙🔍


This app also showcases the following Jetpack libraries working together: [Room](https://developer.android.com/topic/libraries/architecture/room.html), [ViewModel](https://developer.android.com/reference/android/arch/lifecycle/ViewModel.html), [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager), [DataBinding](https://developer.android.com/topic/libraries/data-binding/) and [Navigation](https://developer.android.com/topic/libraries/architecture/navigation/).

Introduction
------------

#### Presentation layer

This app is a Single-Activity app, with the following components:
* A main activity that handles navigation.
* A fragment to display the list of apps currently tracked. This fragment makes use of [MvRx](https://github.com/airbnb/MvRx) architecture, and it was a delight integrating search with database on the viewmodel.

The app uses a Model-View-ViewModel (MVVM) architecture for the presentation layer. Each of the fragments corresponds to a MVVM View.
The View and ViewModel communicate using RxJava2 and general good principles.

#### Data layer

The database is created using Room and it has two entities: a `App` and a `Version` that generate corresponding SQLite tables at runtime.
There is a one to many relationship between them. The packageName from `App` is a foreign key on `Version`.
App contains the app label, package name and color (to be displayed on the app, based on the icon).
Version contains the targetSDK version, versionName and versionCode. The app only adds a new version when the targetSDK changes.

To let other components know when the data has finished populating, the `ViewModel` exposes a `Flowable` object.
The app also makes use of Kotlin's Coroutines to deal with some callbacks.

#### How components were used

* MvRx and Epoxy: used on the main screen to fetch and filter (if necessary) the list of apps. Since Epoxy wasn't made for items that are changing, the Settings view makes use of Groupie.

* ViewModel: A *Observables.combineLatest* will merge the results from database (which will be fetched if empty) and search (which will be empty when app is first opened). Following this, the *execute* from MvRx will copy the state to the correct EpoxyController.

* WorkManager: responsible for automatically syncing when the app is in background.
There are two constraints: *battery not low* and *device charging*.

#### Third Party Libraries Used

  * [Android-Iconics][1] deal with icons without suffering.
  * [Architecture Components][2] stated above.
  * [Epoxy][3] for making static RecyclerViews as efficient and nice as possible.
  * [Coroutines][4] for simple background work.
  * [Logger][5] logs that are useful and can disabled on release.
  * [material-about-library][6] \[eventually\]create an about page without suffering.
  * [Material Dialogs][7] show dialogs in a simple and easy way.
  * [MvRx][8] on the main fragment.
  * [Stetho][9] debug the database easily.
  * [RxJava][10] deals with MvRx and coordinates most of the work on the app.
  * [Dagger 2][11] dependency injection for sharedPreferences with application Context, provides singleton database instances.


[1]: https://github.com/mikepenz/Android-Iconics
[2]: https://developer.android.com/topic/libraries/architecture/
[3]: https://github.com/airbnb/epoxy
[4]: https://github.com/Kotlin/kotlinx.coroutines
[5]: https://github.com/orhanobut/logger
[6]: https://github.com/daniel-stoneuk/material-about-library
[7]: https://github.com/afollestad/material-dialogs
[8]: https://github.com/airbnb/MvRx
[9]: http://facebook.github.io/stetho/
[10]: https://github.com/ReactiveX/RxJava
[11]: https://github.com/google/dagger


### Reporting Issues

Issues and Pull Requests are welcome.
You can report [here](https://github.com/sankaryg/voicesearch/issues).

License
-------

Copyright 2020 Sankaranarayanan Y G.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
