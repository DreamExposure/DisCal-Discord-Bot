mvn clean

mvn package jib:build -pl client -am

mvn package jib:build -pl server -am

mvn package jib:build -pl web -am
