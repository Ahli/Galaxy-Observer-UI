@echo off
: Batch Script 
: author: @AhliSC2

java -XX:+UseZGC -XX:SharedArchiveFile=application.jsa -Xlog:class+load=info:file=class-load.log -Xlog:class+path=debug:file=class-path.log -Xshare:on -jar compiler/compile-spring-boot.jar

: line breaks
echo.
echo.
: keep window open
pause
