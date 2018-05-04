@echo off

call mvn clean package

copy /Y .\target\db2lci-0.0.1.jar .\target\dist\db2lci.jar
copy /Y run.bat .\target\dist\run.bat

