@echo off
: Batch Script 
: author: @AhliSC2

java -jar compile-spring-boot.jar -XX:G1PeriodicGCInterval=5000 -XX:G1PeriodicGCSystemLoadThreshold=0.1

: line breaks
echo.
echo.
: keep window open
pause
