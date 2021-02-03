if not exist build mkdir build
cd build
echo "compiling java"
dir /s /B ..\src\main\*.java > sources.txt
javac -d graalout @sources.txt
echo "compiling native"
call native-image ^
-H:ReflectionConfigurationFiles=../reflection.json ^
--static -cp graalout com.evilcorp.StartSingleMpvInstance runmpv
echo "removing window generation script"
editbin /SUBSYSTEM:WINDOWS runmpv.exe
echo over
xcopy /Y runmpv.exe ..
if not exist runmpv mkdir runmpv
xcopy /Y runmpv.exe runmpv
xcopy /Y ..\mpv_runner.properties runmpv
xcopy /Y ..\logging.properties runmpv
tar -c -f runmpv.zip runmpv
cd ..

