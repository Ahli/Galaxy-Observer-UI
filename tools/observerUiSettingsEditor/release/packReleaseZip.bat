@echo off
: Batch Script 
: author: @AhliSC2

DEL "Observer UI Settings Editor Alpha.zip"

: Archive size: 2321573 bytes (2268 KiB) - not compatible with windows
: "C:\Program Files\7-Zip\7z.exe" a -tzip -mmt=on -mtc=off -mx=9 -mm=LZMA -r "Observer UI Settings Editor Alpha.zip" "Observer UI Settings Editor\*"

: Archive size: 2412432 bytes (2356 KiB)
: "C:\Program Files\7-Zip\7z.exe" a -tzip -mmt=on -mtc=off -mx=9 -mm=Deflate -mfb=128 -mpass=15 -r "Observer UI Settings Editor Alpha.zip" "Observer UI Settings Editor\*"

: Archive size: 2403707 bytes (2348 KiB)
: "C:\Program Files\7-Zip\7z.exe" a -tzip -mmt=on -mtc=off -mx=9 -mm=Deflate64 -mfb=128 -mpass=15 -r "Observer UI Settings Editor Alpha.zip" "Observer UI Settings Editor\*"

: Archive size: 2416765 bytes (2361 KiB)
: "C:\Program Files\7-Zip\7z.exe" a -tzip -mmt=on -mtc=off -mx=9 -mm=BZip2 -md=900000b -r "Observer UI Settings Editor Alpha.zip" "Observer UI Settings Editor\*"

: Archive size: 2384030 bytes (2329 KiB)
:"C:\Program Files\7-Zip\7z.exe" a -tzip -mmt=on -mtc=off -mx=9 -mm=PPMd -mmem=256m -mo=16 -r "Observer UI Settings Editor Alpha.zip" "Observer UI Settings Editor\*"

"C:\Program Files\7-Zip\7z.exe" a -tzip -mmt=on -mtc=off -mx=9 -mm=Deflate64 -mfb=128 -mpass=15 -r "Observer UI Settings Editor Alpha.zip" "Observer UI Settings Editor\*"

: pause
