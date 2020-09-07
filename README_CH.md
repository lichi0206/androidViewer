## Android Viewer

[[中文](README_CH.md)|[English](README.md)]

### 简介

这是一个安卓开发辅助工具，拥有以下特性：

* 实时屏幕截图（无需在手机端安装任何应用或服务，也因此延迟可能有点高）
* 列出当前连接的所有安卓设备
* 打印安卓设备的相关信息（Android 版本，设备名称，设备制造厂商，设备型号等等...）
* 打印当前设备正在前端运行的应用的信息（主要为：App Activity 和 App Package）
* 打印当前设备后台运行的所有应用的信息（主要为：App Activity 和 App Package）
* 唤醒设备（支持使用PIN码解锁设备）
* 远程安装应用
* 常规截图

为什么要开发这么一个工具呢，有以下几个原因：

* 2020年，一场突如其来的疫情打乱了整个国家的脚步，我们公司也在政府的疫情防控政策指导下开始尝试远程办公，但是因为测试设备有限，所以手机等测试设备还是放在公司里，这样我就必须得远程操控手机才行
* 我们大部分的应用都是Hybird APP，也需要这么一个辅助工具也帮助开发
* 自动化测试人员也非常需要这么一个工具来辅助他们做端到端的自动化

### 功能

#### 实时截屏

无需在移动设备上安卓任何应用或者服务，这是该工具的一个优点，但也正式因为这个，在传输图片的过程中可能会有一些延迟。

![](/documents/realtimeScreenshot.gif)

#### 列出所有连接的设备

![](/documents/listAllDevices.png)

#### 打印选中设备信息

可以将所选中的设备的基本信息打印出来，包括：

* Android version
* SDK version
* Device Brand
* Device name
* Device model
* Device manufacturer
* Locale

![](/documents/deviceInfo.png)

#### 当前前端运行应用的信息

这个功能对于自动化人员非常有帮助，因为他们经常需要调起移动设备上的某个应用，这就需要知道该应用的appActivity 和 appPackage 信息，该工具可以非常方便的打印出特定应用的这两个信息，前提是必须要将改应用手动调起并置于前台。

![](/documents/focusWindow.png)

#### 所有后台运行的应用的信息

可以打印出所有正在后台运行的应用的信息，主要为appActivity 和 appPackage。

![](/documents/allActivities.png)

#### 唤醒设备

支持以下两种唤醒设备的方式：

* 无密码直接唤醒
* 使用PIN码进行唤醒

注意：有一些设备（华为某些型号）目前还不能通过PIN码来唤醒。

![](/documents/WakeupDevicePIN.gif)

#### 远程安装应用

支持远程安装以“apk”结尾的应用。

![](/documents/InstallApp.gif)

### Reference tools

#### JavaFX Scene Builder

Version 2.0: https://www.oracle.com/java/technologies/javafxscenebuilder-1x-archive-downloads.html

Version 8.0: https://gluonhq.com/products/scene-builder/

#### JFoeniX

http://www.jfoenix.com/
