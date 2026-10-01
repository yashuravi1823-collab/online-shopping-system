FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY lib/mysql-connector-j-26.7.0.jar lib/
COPY src src

RUN mkdir -p out && \
    javac -cp "lib/mysql-connector-j-26.7.0.jar" \
    -d out src/main/java/com/shop/*.java

EXPOSE 8080

CMD ["java", "-cp", "lib/mysql-connector-j-26.7.0.jar:out", "com.shop.Main"]