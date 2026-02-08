@echo off
: Batch Script 
: author: @AhliSC2

: G1GC
: java -jar compile-spring-boot.jar -XX:G1PeriodicGCInterval=5000 -XX:G1PeriodicGCSystemLoadThreshold=0.1

: ZGC (works without my GC hacks)
:java -jar compile-spring-boot.jar -XX:+UseZGC

: ShenandoahGC
: java -jar compile-spring-boot.jar -XX:+UseShenandoahGC

: ParallelGC
: java -jar compile-spring-boot.jar -XX:+UseParallelGC


: CDS DEBUGGING:
: java -Djarmode=tools -jar compile-spring-boot.jar extract --destination compiler
: java -XX:ArchiveClassesAtExit=application.jsa -Dspring.context.exit=onRefresh -Xlog:cds=debug:file=cds.log -jar compile-spring-boot.jar -XX:+UseZGC
: java -XX:SharedArchiveFile=application.jsa -Xlog:class+load=info:file=class-load.log -Xlog:class+path=debug:file=class-path.log -jar compile-spring-boot.jar -XX:+UseZGC

java -XX:+UseZGC -jar compile-spring-boot.jar 
timeout /T 1 /NOBREAK > nul
java -XX:+UseZGC -jar compiler/compile-spring-boot.jar
timeout /T 1 /NOBREAK > nul
rmdir compiler /s /q
del application.jsa /f /q
timeout /T 1 /NOBREAK > nul
java -Djarmode=tools -jar compile-spring-boot.jar extract --destination compiler
timeout /T 1 /NOBREAK > nul
java -XX:+UseZGC -XX:ArchiveClassesAtExit=application.jsa -Xlog:cds=debug:file=cds.log -jar compiler/compile-spring-boot.jar
timeout /T 6 /NOBREAK > nul
java -XX:+UseZGC -XX:SharedArchiveFile=application.jsa -jar compiler/compile-spring-boot.jar

: line breaks
echo.
echo.
: keep window open
pause
