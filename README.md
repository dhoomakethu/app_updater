Excepts the following folder structure
/mnt/sdcard/otapkg/
# needs a version.txt file containing the version number,
# update button is enabled if the version is not same as "4.0.0.0" hardocded for now
/mnt/sdcard/otapkg/version.txt      -> should contain something like 4.0.0.1
# Actual apks, it will pick all files with *.apk extension.
/mnt/sdcard/otapkg/abc.apk
/mnt/sdcard/otapkg/xyz.apk