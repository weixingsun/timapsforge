#mapsforge Titanium module
This module wraps parts of the [mapsforge](https://code.google.com/p/mapsforge/) 0.5.2 (0.5.2-release) API.
Just as mapsforge, this module is in development.

#Building
Clone this repository.
Copy "build.properties.example" to "build.properties" and change the following variables to match your environment:
* titanium.sdk
* titanium.os
* titanium.version
* android.sdk

Then, run "ant" in the repository to build the module. If successful, the finished module can be found in the folder "dist" named "sc.mapsforge-android-x.x.zip".

#The mapsforge library
This repo includes a module built from the mapsforge repository on 2015-08-16.
If you want to upgrade the included jar library in the "lib" directory with your own. You then need to rebuild this module

#Usage
See the example app in "/example" for an overview of how this module can be used.

