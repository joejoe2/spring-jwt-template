FROM openjdk:17
EXPOSE 8080
COPY ./target/demo-0.0.1-SNAPSHOT.jar web.jar
COPY start.sh wait-for-it.sh .
RUN bash -c 'touch /web.jar'
RUN chmod +x start.sh && chmod +x wait-for-it.sh
