About Open eCard
================

In the context of the Open eCard Project, industrial as well as academic
experts have decided to work together on providing an open source and cross
platform implementation of the eCard-API-Framework (BSI-TR-03112), through
which arbitrary applications can utilize authentication and signatures with
arbitrary chip cards.

The artifacts of the project consist of modularized, and to some extent
extensible, libraries for usage in desktop, Android and iOS applications, 
as well as client implementations such as a Desktop
application (richclient) and a Java Applet.


Build Instructions
==================

Detailed build instructions can be found in the INSTALL.md file bundled with
this source package.

Quick Start
-----------

The simplified build instructions are as follows:

    $ git clone git://github.com/ecsec/open-ecard.git
    $ cd open-ecard
    $ mvn clean install


In case you received a preassembled source bundle, the build instructions are
as follows:

    $ tar xaf open-ecard-${version}.tar.xz
    $ cd open-ecard-$version
    $ mvn clean install

Finally, you can run the Open eCard App from command line:

    $ ./packager/richclient-packager/target/open-ecard/bin/open-ecard

Packaging
-----------

Native packages which are based on a modular runtime image can be built with the new [jpackage](https://openjdk.java.net/jeps/343) tool which is a candidate for JDK-14. Early-access builds are already [provided](https://jdk.java.net/jpackage/). Native packages for the Open eCard can be built by downloading the JDK-14 early-access build, referencing it as toolchain and by specifying the following property:

    $ mvn clean install -Ddesktop-package

By default, the packager will take the predefined package types, such as dmg for Mac OS and deb for Linux-based systems. The package type can be overridden for Mac and Linux packages by using the following user property:

    $ mvn clean install -Ddesktop-package -Djlink-jpackager.package-type=<type>

Thereby, the following types are available:

 - dmg
 - pkg
 - deb
 - rpm

You have to make sure the required packaging tools are installed. In case of Windows, msi and exe packages are built. For this purpose, two additional tools are required:

 - [WiX toolset](https://wixtoolset.org/) - to create msi installers
 - [Inno Setup](http://www.jrsoftware.org/isinfo.php) - to create exe installers (Path environment variable must be set)

More information about the required JDK versions and the setup of the toolchain, can be found in the INSTALL.md file.

Mobile libs
-----------

Open eCard supports building of libraries for Android and iOS for usage in arbitrary mobile apps. 

### Android
After a successfull build the library for android can be found in `android-lib` sub project. 
It also can be used as prebuild dependency via gradle dependency management. 
See [open-ecard-android](https://github.com/ecsec/open-ecard-android) for further information.

### iOS (>2.x)
If building on MacOS a ready to use framework gets generated and can be found in 
`./packager/ios-framework/target/robovm`.  
The framework can also be found as asset of the release.

It also can be installed as a cocoapod dependency.
```
pod 'open-ecard'
```
See [open-ecard-ios](https://github.com/ecsec/open-ecard-ios) for further information.




License
=======

The Open eCard App uses a Dual Licensing model. The software is always
distributed under the GNU General Public License v3 (GPLv3). Additionally the
software can be licensed in an individual agreement between the licenser and
the licensee.


Contributing
============

New developers can find information on how to participate under
https://dev.openecard.org/projects/open-ecard/wiki/Developer_Guide.

Contributions can only be accepted when the contributor has signed the
contribution agreement (https://dev.openecard.org/documents/35). The agreement
basically states, that the contributed work can, additionally to the GPLv3, be
made available to others in an individual agreement as defined in the previous
section. For further details refer to the agreement.
