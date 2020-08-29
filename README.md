# Coronavirus Tracker 

## Table of contents
* [Introduction](#introduction)
* [Technologies](#technologies)
* [Launch](#launch)
* [Requirements](#requirements)
* [Usage](#usage)
* [Video Demo](#video demo)
* [Contributing](#contributing)
* [Links](#links)


## Introduction
The Coronavirus Tracker is an Android mobile app that notifies users when they have entered an area that has reports of a Coronavirus case.

<p align="center">
  <img width="460" height="900" src="https://user-images.githubusercontent.com/55412165/77963892-2a6dc600-72ac-11ea-81e2-17c73f263127.PNG">
</p>

## Technologies
* Java
* Python
* Firebase
* Android Studio
* Geofire API
* Google Maps and Utility API

## Launch

In Android Studio, click the "File" tab and click "New->Project From Version Control-> Git" and paste the Github repo.

## Requirements 
Go to the "strings.xml" file in the "res" folder and replace "BuildConfig.google_maps_key" with your own Google Maps API key
```XML
<string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">YOUR_API_KEY</string>
```

This app needs Location and GPS permissions granted in order to function.

## Usage

If the user walks within a 1 kilometer radius of a reported Coronavirus case, a notification will be sent to their phone like so:
<p align="center">
  <img width="460" height="900" src="https://user-images.githubusercontent.com/55412165/77965219-90f3e380-72ae-11ea-80a9-7c927811386a.PNG">
</p>

## Video Demo

Here is a video demo of the app in action!

[Video](https://www.youtube.com/watch?v=-cEClX7vh5Q)

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## Links

[Johns Hopkins University Data about Coronavirus cases](https://github.com/CSSEGISandData/COVID-19)

[Geofire API](https://github.com/firebase/geofire-android)
