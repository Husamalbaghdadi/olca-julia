@echo off

echo delete old app
if exist .\libs\app.exe (
    del .\libs\app.exe
)

echo compile it
gcc -o app.exe -L.\libs test.c -lumfpack

if exist app.exe (
    move app.exe .\libs

    echo test app
    .\libs\app.exe
)

echo all done
