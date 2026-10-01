@echo off
if not exist out mkdir out
echo Compiling...
javac -cp "lib\mysql-connector-j.jar" -d out src\main\java\com\shop\*.java
if errorlevel 1 (
  echo Compilation failed. Check JDK and MySQL Connector/J.
  pause
  exit /b
)
echo Starting backend...
java -cp "out;lib\mysql-connector-j.jar" com.shop.Main
pause
