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

## [Showcase](https://ibb.co/album/gDq3aa)

## Permissions
This app uses the "Storage" (WRITE_EXTERNAL_STORAGE and READ_EXTERNAL_STORAGE) permission in order to save downloaded audio files to the device. Downloaded files are written to the local app data which is hidden from the user. Permission to read from the device's external storage is only requested for the "import file" feature, which requires listing the device's directories.

## Planned Features
- Notification with media controls (play/pause, next, previous)
- File metadata that gives more information about a file other than its name and description
- Import/export audio files buttons

Feel free to suggest anything else you feel might improve the app!

## Patreon Files
ShibbyApp allows you to access Patreon files from directly in the app. You can download these files for quick loading times or stream them directly from the server just like regular files.

Patreon files can be accessed from the "Patreon Files" tab in the menu drawer. A Patreon account with a valid pledge to Shibby is **required** in order to access the files. *This is not a method to obtain these files for free.*

This feature requires ShibbyApp server version >=1.1.0 in order to work. The feature is not enabled on servers by default, as it requires a bit more setup.

## Importing Files
You can import your own downloaded shibbyfiles or custom made files using the "Import File" tool found in the menu in the top-right corner of the app. If you specify tags for your file, each tag must be separated by a comma. For example: "F4A, ASMR, Hypnosis". Casing does not necessarily matter, since the tags will be automatically formatted to match other tags.

For advanced users, instead of manually downloading each shibbyfile, you can manually import pre-existing shibbyfiles into the app by renaming the files to their SHA256 equivalent and then placing them in "/storage/self/primary/Android/data/io.github.kraowx.shibbyapp/files/audio" on your Android device. You can even modify the audio data if you want as long as the name of the file is *exactly* the same as how it appears on soundgasm. I have written some bash scripts to assist with this process. First run [this](https://gist.github.com/kraowx/4c1506f4dbb643f49203669756168413) script in a new directory which will automatically download all of Shibby's files from soundgasm, and then run [this](https://gist.github.com/kraowx/24104f038b9fee14a1466367381d465b) script in the same directory which will rename each file to its SHA256 hash equivalent.
