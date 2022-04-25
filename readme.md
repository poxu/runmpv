# runmpv

Mpv launcher which helps to have a single instance of mpv running. New videos are opened in a currently running instance
of mpv, provided that it was started by runmpv.

runmpv was inspired by [umpv](https://github.com/mpv-player/mpv/blob/master/TOOLS/umpv) (unique mpv) 
and [umpvw](https://github.com/SilverEzhik/umpvw) (unique mpv windows). The proper naming for this piece of software
should've been umpvwj
(unique mpv windows java), but that's too many letters for my taste.

You can't use umpv on Windows, because it requires sockets, 
but mpv uses pipes to communicate.
And I didn't want to use umpvw, because I wanted something easily ported
to linux.

Also, I like java.

Currently runmpv works under Windows and under Linux. 

# Installation

## Windows

Copy runmpv directory to the directory, where mpv.exe is located and then
launch runmpv-install.bat to associate video files with runmpv.exe .
You still have to select runmpv manually from a dropdown list,
when you play a certain file type for the first time.

If you want to store runmpv.exe in a directory of you choice, you'll have
to manually specify mpvHomeDir parameter in runmpv.properties

## Linux

If mpv is installed with package manager and mpv directory is in your 
$PATH, then you can copy runmpv to the directory of your choice and
tune your environment to open video files with runmpv.

If you want to use custom mpv, then you need to specify mpvHomeDir
in runmpv.properties

# Usage

This is how you start new mpv instance, or tell existing instance to load
a new file.

```cmd
runmpv.exe <path-to-video-file>
```

# Configuration

Use runmpv.properties file to configure runmpv. Here is an example of a configuration.

```properties
# Path to directory, containing mpv.exe
# Should be absolute path, but place holders can be used
# to denote certain directories
# %h - at the beginning means home directory
# %r - at the beginning means runmpv executable directory
# %v - at the beginning means directory, where video is located
#
# Also, path should end with /. That is important, because
# runmpv concatenates this path and mpv to form full path to
# mpv executable.
#
# Default value under Linux is an empty string "",
# so that runmpv would launch just mpv without prefix.
#
# Default value under Windows is %r/../, because I
# suppose it's convenient ot put directory, which
# contains runmpv to mpv directory.
mpvHomeDir=%r/../

# Describes when runmpv opens a new mpv instance.
# If openMode is anything but instance-per-directory,
# runmpv opens single instance of mpv and always uses it to open videos.
# If openMode is instance-per-directory, then runmpv opens new mpv
# instance to play videos from every directory. And uses existing mpv instance
# to play new files from the same directory.
# Default value is single-instance
openMode=single-instance

# Name of the pipe for JSON IPC.
# Used to identify single mpv instance.
# Pipe is going to be created by mpv.exe
pipeName=runmpv-mpv-pipe

# Path to mpv log file
# This is the file, where mpv.exe (the instance controlled by runmpv)
# will write it's own logs.
# If property is not found, then mpv logging is controlled by
# mpv settings entirely
# Should be absolute path, but place holders can be used
# to denote certain directories
# %h - at the beginning means home directory
# %r - at the beginning means runmpv executable directory
# %v - at the beginning means directory, where video is located
mpvLogFile=%r/runmpv-mpv.log

# Path to emergency logging system, used to diagnose
# why regular logging doesn't work.
# If property is not found, emergency logging system is disabled.
# Should be absolute path, but place holders can be used
# to denote certain directories
# %h - at the beginning means home directory
# %r - at the beginning means runmpv executable directory
# %v - at the beginning means directory, where video is located
runnerLogFile=%r/runmpv.log

# Amount of time after which runmpv decides, that it couldn't launch mpv
# and it's time to quit. Measured in seconds.
# Needed, because if you launch video files from hard drive,
# a lot of time may pass until the HDD spins up
waitSeconds=5

# If true, mpv window is focused after new
# file is loaded. If false, mpv window
# is only focused on first launch.
# Anything but true or false will result
# in na error.
# Only works under windows
# Default value is true
focusAfterOpen=true
```

# Build instructions

## Windows

### Install GraalVM based on java 17

Go to graalvm.org, press Download button and when next page is loaded,
choose 21.3.0 version and press "Download from GitHub".

On the next page find Java 17 based heading and click a link for 
Windows (amd64). 

Here is a direct link, you can try it before searching for it manually
https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.3.0/graalvm-ce-java17-windows-amd64-21.3.0.zip

### Install Graal Native Image

Add GraalVM bin directory to your path

Execute ```gu install native-image```

### Install Visual Studio 2019

Probably Visual Studio 2022 will also work, but I have tried VS 2019
on my machine and automatic builds on github are made with VS 2017.

### Open Native Tools Command Prompt for Visual Studio

To do that on Windows 10 you manually find Visual Studio 2019 ->
x64 Native Tools Command Prompt for VS 2019 in main menu.

Or you can press WinKey and start typing x64 and
x64 Native Tools Command Prompt for VS 2019 will appear in search results.

### Run the following commands or just run build.bat

Before running commands you need to change you working directory to 
the directory where mpv sources are located.

```bat
if not exist build mkdir build
cd build
dir /s /B ..\src\main\*.java > sources.txt
javac -d graalout @sources.txt
call native-image ^
-H:ReflectionConfigurationFiles=../reflection.json ^
--static -cp graalout com.evilcorp.StartSingleMpvInstance runmpv
editbin /SUBSYSTEM:WINDOWS runmpv.exe
xcopy /Y runmpv.exe ..
cd ..
```

## Linux

### Install GraalVM based on java 17

Go to graalvm.org, press Download button and when next page is loaded,
choose 21.3.0 version and press "Download from GitHub".

On the next page find Java 17 based heading and click a link for
Linux (amd64).

Here is a direct link, you can try it before searching for it manually
https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.3.0/graalvm-ce-java11-linux-amd64-21.3.0.tar.gz

Copy archive contents to a directory of your choice.

### Install Graal Native Image

Add GraalVM bin directory to your path

Execute ```gu install native-image```

### Install programs to compile code

### Run the following commands or just run build.sh

Before running commands you need to change you working directory to
the directory where mpv sources are located.

```bash
mkdir -p build
find "$PWD"/src/main -type f -name '*.java' > build/sources.txt
cd build || exit
javac -d graalout @sources.txt
native-image \
-H:ReflectionConfigurationFiles=../reflection.json \
--static -cp graalout com.evilcorp.StartSingleMpvInstance runmpv
mkdir -p runmpv-prog
cp ../runmpv.properties runmpv-prog
cp ../logging.properties runmpv-prog
cp ../runmpv-install.bat runmpv-prog
cp ../runmpv-uninstall.bat runmp-prog
cp ../runmpv-document.ico runmpv-prog
mv runmpv runmpv-prog/
mv runmpv-prog runmpv
```
