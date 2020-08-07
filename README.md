# Apk Updater

App uses [FileObserver](https://developer.android.com/reference/android/os/FileObserver) class to monitor app specific [external file directory](https://developer.android.com/training/data-storage).
When-ever there is a new apk pushed to the directory the apk install process is triggered.

The Update button is disabled till there is a valid APK available in the directory.

## Testing
push apk files to `/mnt/sdcard/Android/data/com.example.otaapp/files/` to trigger an apk update.
`adb push <apk file> /mnt/sdcard/Android/data/com.example.otaapp/files/`

## To Do.

* Handle non apk related files pushed to the directory
* Better handling of transition across views
* Update Button visual feedback when disabled. 
* Refactor code for minimal exchange of info across views/activities and threads.

## Note:
Tested on Android 9 and 10. Other versions of android might require some more setting enabled. 
