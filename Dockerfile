FROM eclipse-temurin:17-jre
EXPOSE 8080
COPY start.sh wait-for-it.sh .
RUN chmod +x start.sh && chmod +x wait-for-it.sh
COPY ./target/jwt.jar jwt.jar
RUN sh -c 'touch jwt.jar'
