@echo off
: Batch Script 
: author: @AhliSC2

: default Hotspot (max 1.5gb, min 338)
java -jar compile-spring-boot.jar -XX:G1PeriodicGCInterval=5000 -XX:G1PeriodicGCSystemLoadThreshold=0.1

: adoptOpenJdk J9 JRE (max 1.18gb, no explicit gc after workload) -Xcompactgc
:"C:\Program Files\AdoptOpenJDK\jdk-13.0.0.33-openj9\bin\java.exe" -jar compile-spring-boot.jar -Xshareclasses -Xscmx16m -Xquickstart

: line breaks
echo.
echo.
: keep window open
pause
