#mapsforge Titanium module
This module wraps the following open source components of:
[graphhopper](https://github.com/graphhopper) 0.6
[mapsforge](https://github.com/mapsforge) 0.6.0
[trove4j](https://bitbucket.org/trove4j/trove) 3.1
Just as above, this module is in development.

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

