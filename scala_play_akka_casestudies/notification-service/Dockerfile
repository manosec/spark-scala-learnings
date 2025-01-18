FROM openjdk:11-jre-slim

WORKDIR /app

COPY . /app

RUN apt-get update && apt-get install -y curl gnupg && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" > /etc/apt/sources.list.d/sbt.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x99e82a75642ac823" | apt-key add && \
    apt-get update && apt-get install -y sbt

RUN sbt compile

CMD ["sbt", "run"]