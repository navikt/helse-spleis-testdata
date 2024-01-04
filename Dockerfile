FROM gcr.io/distroless/java21-debian12:nonroot

COPY build/libs/*.jar /app/
COPY public /app/public/

ENV TZ="Europe/Oslo"
ENV JAVA_OPTS='-XX:MaxRAMPercentage=90'

WORKDIR /app

CMD ["app.jar"]