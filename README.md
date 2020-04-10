# ShibbyApp
ShibbyApp is an Android file manager and audio player made exclusively for listeners of [ShibbySays.](https://www.reddit.com/r/ShibbySays/)

## Installation
The app must be installed manually since these types of apps are not allowed in the Google Play store. Download the latest version of the app [here](https://github.com/kraowx/shibbyapp/releases/latest) on your device and save it to a place you can get to easily. Simply run the downloaded .apk file and click install when prompted. You may have to change your system settings to allow installation of apps from "unknown sources". This should be under the "Security" tab in the settings of your device.

The default server is currently hosted on [Heroku](https://shibbyapp.herokuapp.com). <s>The limitation however is that it doesn't support Patreon files at the moment. If this is a problem for you however, you can host your own server. See [this page](https://github.com/kraowx/shibbyapp-server) for instructions on how to set up a server. Once you have your server set up, go into the app, tap on the three-dot menu in the top-right corner of the screen and tap settings. Enter the address of the server that you set up into the box in the format <code>https://IP:port</code>, then click the "Apply" button and restart the app.</s>

## Features
- Built-in audio player to either stream or play downloaded files
- Patreon support (manage/play Patreon files directly from the app)
- Customizable playlists
- Loopable audio (either infinite or a set number)
- Pre-audio delay timer
- Autoplay
- Shuffle
- Search by tags, files, or series
- Filter by duration, file type, or tags
- Import files
- Light and dark themes

## [Showcase](https://ibb.co/album/gDq3aa)

## Permissions
This app uses the "Storage" (WRITE\_EXTERNAL\_STORAGE and READ\_EXTERNAL\_STORAGE) permission in order to save downloaded audio files to the device. Downloaded files are written to the local app data which is hidden from the user. Permission to read from the device's external storage is only requested for the "import file" feature, which requires listing the device's directories.

## Planned Features
- Notification with media controls (play/pause, next, previous)
- Some kind of vibration feature that syncs with audio? Add offset for bluetooth headphones as well
- File metadata that gives more information about a file other than its name and description
- Import/export audio files buttons

Feel free to suggest anything else you feel might improve the app!

## Patreon Files
ShibbyApp allows you to access Patreon files from directly in the app so your files are all in one place. You can download these files for quick loading times or stream them directly from the server just like regular files.

Patreon files can be accessed from the "Patreon Files" tab in the menu drawer. A Patreon account with a valid pledge to Shibby is **required** in order to access the files. *This is not a method to obtain these files for free.*

<s>This feature requires ShibbyApp server version >=1.1.0 in order to work. The feature is not enabled on servers by default, as it requires a bit more setup.</s>
This feature is not supported by servers at the moment. However, built-in Patreon support for the app was added in version 2.1.0.

## Additional Notes and "Hidden" Features
- In the audio player, when the loop button is pressed it will default to toggle infinite loop on and off. If you only want to loop a specific number of times you can long press on the loop icon and select a number from 0 to 100.
- Files in a playlist and playlists themselves can be reordered by long pressing on a file/playlist and dragging it above or below another file/playlist. Note that series playlists cannot be reordered.
- When the download button next to an undownloaded file is pressed it will turn red. This indicates that the file is downloading in the background, and the status of the download can be found in the "Download Manager" notification.
- You can refresh the content of most pages by "pulling down" (swiping down) on the page until the refresh icon appears.
- <s>Refreshing inside the tags tab will only include Patreon files if you are currently logged in to Patreon. Otherwise these files will be excluded or removed from the list. Note that you may have to confirm your Patreon email for this to work.</s>

## Importing Files
You can import your own downloaded shibbyfiles or custom/edited files using the "Import File" tool found in the menu in the top-right corner of the app. If you specify tags for your file, each tag must be separated by a comma. For example: "F4A, ASMR, Hypnosis". Casing does not necessarily matter, since the tags will be automatically formatted to match other tags.

For advanced users, instead of manually downloading each shibbyfile, you can manually import pre-existing shibbyfiles into the app by renaming the files to their SHA256 equivalent and then placing them in "/storage/self/primary/Android/data/io.github.kraowx.shibbyapp/files/audio" on your Android device. You can even modify the audio data if you want as long as the name of the file is *exactly* the same as how it appears on soundgasm. I have written some bash scripts to assist with this process. First run [this](https://gist.github.com/kraowx/4c1506f4dbb643f49203669756168413) script in a new directory which will automatically download all of Shibby's files from soundgasm, and then run [this](https://gist.github.com/kraowx/24104f038b9fee14a1466367381d465b) script in the same directory which will rename each file to its SHA256 hash equivalent.
