FROM openjdk:11
ADD ./build/libs/github-download-helper-0.1.12.jar app.jar
ENTRYPOINT ["java","-Dfile.encoding=utf8","-jar","/app.jar"]