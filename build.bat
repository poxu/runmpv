if not exist build mkdir build
cd build
echo "compiling java"
dir /s /B ..\*.java > sources.txt
javac -d graalout @sources.txt
echo "compiling native"
call native-image --static -cp graalout com.evilcorp.StartSingleMpvInstance runmpv
echo "removing window generation script"
editbin /SUBSYSTEM:WINDOWS runmpv.exe
echo over
xcopy /Y runmpv.exe ..
cd ..
