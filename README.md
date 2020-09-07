## Android Viewer

[[中文](README_CH.md)|[English](README.md)]

### Summary

This is an Android development assistant tool, which contains the following functions:

* Real-time screenshot from device (No need to install any apps or services on your mobile phone, so it may be a bit high latency).
* List all connected Android devices.
* Print device information (Android version, Device name, Device model, Device brand etc.)
* Print the current device focus window information (App Activity and App Package).
* Print all active application information in the background (App Activity and App Package).
* Wake up device (Support wake up using PIN).
* Install apps remotely
* Normal screenshot

There are several reasons why such a tool is made:

* Due to the new crown pneumonia in 2020, I started to work remotely, but the equipment is still in the company.
* Most of the mobile products we develop are hybrid applications, and we need such a tool to assist development.
* Automated test developers also need such an auxiliary tool to help development.

### Features

#### Real-time screenshot

No need to install any apps or services on mobile devices, this is one of its advantages, but also because of this, there may be some delay in taking screenshots.

![](/documents/realtimeScreenshot.gif)

#### List all connected android devices

![](/documents/listAllDevices.png)

#### Print Device information

The basic information of the selected device will be printed out, including:

* Android version
* SDK version
* Device Brand
* Device name
* Device model
* Device manufacturer
* Locale

![](/documents/deviceInfo.png)

#### Current Focus window information

This is very helpful for automation developers. They often need to get appActivity and appPackage information of different applications. This function can easily print out the relevant information of the applications currently running on the device.

![](/documents/focusWindow.png)

#### All background active apps

Will print out all relevant information of applications running in the background.

![](/documents/allActivities.png)

#### Wake up device

Support the following two wakeup methods:

* Wake up without password
* Wake up with PIN

Note: Some specific devices have not yet passed the "Wake up with PIN" test.

![](/documents/WakeupDevicePIN.gif)

#### Install APP remotely

Support remote installation of applications ending in "apk".

![](/documents/InstallApp.gif)

### Reference tools

#### JavaFX Scene Builder

Version 2.0: https://www.oracle.com/java/technologies/javafxscenebuilder-1x-archive-downloads.html

Version 8.0: https://gluonhq.com/products/scene-builder/

#### JFoeniX

http://www.jfoenix.com/
