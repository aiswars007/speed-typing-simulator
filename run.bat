@echo off
echo Starting Intelligent Speed Typing Analytics Platform...
javac -encoding UTF-8 -cp "lib\mysql-connector-j-8.3.0.jar;lib\pdfbox-app-3.0.4.jar;src" -d "out\production\something gona work" src\com\speedtyping\Main.java src\com\speedtyping\model\*.java src\com\speedtyping\dao\*.java src\com\speedtyping\service\*.java src\com\speedtyping\ui\*.java src\com\speedtyping\config\*.java
if not exist "out\production\something gona work\db.properties" (
    copy "db.properties" "out\production\something gona work\db.properties"
)
java -cp "out\production\something gona work;lib\mysql-connector-j-8.3.0.jar;lib\pdfbox-app-3.0.4.jar" com.speedtyping.Main
