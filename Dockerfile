FROM java:11
ADD ./build/libs/github-download-helper-0.1.11.jar app.jar
ENTRYPOINT ["java","-Dfile.encoding=utf8","-jar","/app.jar"]