name         := "trade-boi"
version      := "0.3.0"
scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.lmax"                   %  "disruptor"        % "3.3.4",
  "io.netty"                   %  "netty-all"        % "4.1.0.Final",
  "com.google.protobuf"        %  "protobuf-java"    % "3.0.0",
  "com.squareup.okhttp3"       %  "okhttp-ws"        % "3.2.0",
  "com.fasterxml.jackson.core" %  "jackson-databind" % "2.7.4",
  "org.slf4j"                  %  "slf4j-simple"     % "1.7.13",
  "com.novocode"               %  "junit-interface"  % "0.11"  % "test",
  "org.scalatest"              %% "scalatest"        % "3.0.1" % "test"
)

