@echo off

set JNI_FLAGS=-D_JNI_IMPLEMENTATION_ -Wl,--kill-at
set JNI="-IC:\Program Files\Java\jdk1.8.0_131\include" "-IC:\Program Files\Java\jdk1.8.0_131\include\win32"

echo compile it
g++ %JNI_FLAGS% -O3 -DNDEBUG %JNI% -L.\libs -shared -o libs\olca-umfpack.dll lib.c -lumfpack

echo all done

