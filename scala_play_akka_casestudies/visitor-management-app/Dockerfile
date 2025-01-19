FROM hseeberger/scala-sbt:11.0.13_1.6.1_2.13.7 as build

WORKDIR /app

COPY . /app

RUN sbt clean compile stage

FROM openjdk:11-jre-slim

COPY --from=build /app/target/universal/stage /app

WORKDIR /app

EXPOSE 9000

CMD ["./bin/visitor-management-system", "-Dplay.http.secret.key=UoCX4d1NOjY4DWv-zSYd_Jz1Z-0WHVAOjcaBuYv5XzE"]