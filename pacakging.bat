@echo off
SET BASE_DIR=src/main/webapp/WEB-INF/lib
SET GROUP_ID="com.collabhub"
SET VERSION=1.0
for %%f in (%BASE_DIR%/*.jar) do (
    mvnw install:install-file -Dfile="%BASE_DIR%/%%f" -DgroupId=%GROUP_ID% -DartifactId=%%~nf -Dversion=%VERSION% -Dpackaging=jar
)

pause
