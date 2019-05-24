@echo off
: Batch Script 
: author: @AhliSC2

: default Hotspot (max 1.5gb, min 338)
java -jar compile-spring-boot.jar -XX:G1PeriodicGCInterval=5000 -XX:G1PeriodicGCSystemLoadThreshold=0.1

: adoptOpenJdk J9 JRE (max 1.18gb, no explicit gc after workload)
:"C:\Program Files\Java\OpenJDK12U-jre_x64_windows_openj9_12.0.1_12_openj9-0.14.1\jdk-12.0.1+12-jre\bin\java.exe" -jar compile-spring-boot.jar  -Xshareclasses -Xscmx16m -Xquickstart -Xcompactgc

: line breaks
echo.
echo.
: keep window open
pause
