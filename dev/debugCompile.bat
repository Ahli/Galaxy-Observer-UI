@echo off
: Batch Script 
: author: @AhliSC2

: G1GC
: java -jar compile-spring-boot.jar -XX:G1PeriodicGCInterval=5000 -XX:G1PeriodicGCSystemLoadThreshold=0.1

: ZGC (works without my GC hacks)
java -jar compile-spring-boot.jar -XX:+UseZGC

: ShenandoahGC
: java -jar compile-spring-boot.jar -XX:+UseShenandoahGC

: ParallelGC
: java -jar compile-spring-boot.jar -XX:+UseParallelGC


: line breaks
echo.
echo.
: keep window open
pause
