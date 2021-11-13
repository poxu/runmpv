mkdir -p build
find "$PWD"/src/main -type f -name '*.java' > build/sources.txt
cd build || exit
javac -d graalout @sources.txt
echo "compiling native"
native-image \
-H:ReflectionConfigurationFiles=../reflection.json \
--static -cp graalout com.evilcorp.StartSingleMpvInstance runmpv
echo "over"
mkdir -p runmpv-prog
cp ../runmpv.properties runmpv-prog
cp ../logging.properties runmpv-prog
cp ../runmpv-install.bat runmpv-prog
cp ../runmpv-uninstall.bat runmp-prog
cp ../runmpv-document.ico runmpv-prog
mv runmpv runmpv-prog/
mv runmpv-prog runmpv
tar -a -c -f runmpv.zip runmpv
cp -r runmpv/* /home/riptor/soft/runmpv
rm -r runmpv
