# HashmapEditor

Simple utility to try to adjust the offsets.dat created by Debezium 

To compile:

mvn clean install

Example use:

java -jar HashmapEditor-1.0-SNAPSHOT.jar -offset_path "/your/path/offsets.dat" -scn 745647234925
