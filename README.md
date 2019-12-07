# ShibbyApp
ShibbyApp is an Android file manager and audio player exclusively for [Shibby's audio files](https://soundgasm.net/u/kinkyshibby).

## Installation
The app must be installed manually since these types of apps are not allowed in the Google Play store. Download the latest version of the app [here](https://github.com/kraowx/shibbyapp/releases/latest) on your device and save it to a place you can get to easily. Simply run the downloaded .apk file and click install when prompted. You may have to change your system settings to allow installation of apps from "unknown sources". This should be under the "Security" tab in the settings of your device.

Note that default server is currently offline. For now, you must host your own server *or* connect to an existing server in order for the app to function. See [this page](https://github.com/kraowx/shibbyapp-server) for instructions on how to set up a server. Once you have your server set up, go into the app and close the popup that says "Fetching first-time data". Tap on the three-dot menu in the top-right corner of the screen and tap settings. Enter the address of the server that you set up into the box in the format IP:port, then click the "Apply" button and restart the app. The app should load properly the next time it opens.

## Features
- Built-in audio player to either stream or play downloaded files
- Customizable playlists
- Loopable audio
- Autoplay
- Search by tag
- Search by file
- Search by series
- Light and dark themes

## Permissions
This app uses the "Storage" (WRITE_EXTERNAL_STORAGE) permission in order to save downloaded audio files to the device. Downloaded files are written to the local app data which is inaccessable the user (without root access).

## Planned Features
- Notification with media controls (play/pause, next, previous)
- File metadata that gives more information about a file other than its name and description
- Ability to reorder items in a playlist
- Import/export audio files buttons

Feel free to suggest anything else you feel might improve the app!
