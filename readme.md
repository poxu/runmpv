# runmpv

Mpv launcher which helps to have a single instance of mpv running.
New videos are opened in a currently running instance of mpv, provided
that it was started by runmpv.

runmpv was inspired by umpv (unique mpv) and umpvw (unique mpv windows).
The proper naming for this piece of software should've been umpvwj 
(unique mpv widows java), but that's too much letters for my taste.

You can't use umpv on windows, because it requires sockets, and I didn't 
want to use umpvw, because I wanted something easily ported to linux.

Also, I like java.

# Usage

rumpv.exe <path-to-video-file>

# Configuration

Use mpv_runner.properties file to configure runmpv.
Here is an example of a configuration.
```properties
# Path to directory, containing mpv.exe
# should be absolute path
# %h - at the beginning means home directory
# %r - at the beginning means runmpv executable directory
# %v - at the beginning means directory, where video is located
# relative paths not supported, also %PATH% is not going to be used
mpvHomeDir=%h/soft/mpv

# Name of the pipe for JSON IPC.
# Used to identify single mpv instance.
# Pipe is going to be created by mpv.exe
pipeName=runmpv-mpv-pipe

# Path to mpv log file
# This is the file, where mpv.exe (the instance controlled by runmpv)
# will write it's own logs.
# If property is not found, then mpv logging is controlled by
# mpv settings entirely
# %h - at the beginning means home directory
# %r - at the beginning means runmpv executable directory
# %v - at the beginning means directory, where video is located
mpvLogFile=%r/debug.log

# Path to emergency logging system, used to diagnose
# why regular logging doesn't work.
# If property is not found, emergency logging system is disabled.
# %h - at the beginning means home directory
# %r - at the beginning means runmpv executable directory
# %v - at the beginning means directory, where video is located
runnerLogFile=%r/runner-debug.log

# Amount of time after which runmpv decides, that it couldn't launch mpv
# and it's time to quit. Measured in seconds.
# Needed, because if you launch video files from hard drive,
# a lot of time may pass until the HDD spins up
waitSeconds=5
```

# Build instructions

## Install GraalVM
## Install Graal Native Image
## Install Visual Studio 2019
## Open Native Tools Command Prompt for Visual Studio 
## Run the following commands or just run build.bat
```bat
if not exist build mkdir build
cd build
dir /s /B ..\*.java > sources.txt
javac -d graalout @sources.txt
call native-image ^
-H:ReflectionConfigurationFiles=../reflection.json ^
--static -cp graalout com.evilcorp.StartSingleMpvInstance runmpv
editbin /SUBSYSTEM:WINDOWS runmpv.exe
xcopy /Y runmpv.exe ..
cd ..
```