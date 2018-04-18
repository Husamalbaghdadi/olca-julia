@echo off

echo delete old app
if exist .\libs\app.exe (
    del .\libs\app.exe
)

echo compile it
gcc -o app.exe -L.\libs main.c -lumfpack

if exist app.exe (
    move app.exe .\lib

    echo test app
    .\lib\app.exe
)

echo all done