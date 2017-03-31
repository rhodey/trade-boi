inThisBuild(Seq(
  name         := "trade-boi",
  version      := "0.4.0",
  scalaVersion := "2.12.1"
))

lazy val tradeBoi = crossProject.in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi"         %%% "fastparse"     % "0.4.2",
      "org.slf4j"           %   "slf4j-simple"  % "1.7.13",
      "org.scalatest"       %%  "scalatest"     % "3.0.1" % "test"
    )
  ).jvmSettings(
  libraryDependencies ++= Seq(
    "com.lmax"                   % "disruptor"        % "3.3.4",
    "io.netty"                   % "netty-all"        % "4.1.0.Final",
    "com.squareup.okhttp3"       % "okhttp-ws"        % "3.2.0",
    "com.google.protobuf"        % "protobuf-java"    % "3.0.0",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.4",
    "com.novocode"               % "junit-interface"  % "0.11" % "test"
  )
).jsSettings(
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  )
)

lazy val tradeBoiJVM  = tradeBoi.jvm
lazy val tradeBoiJS   = tradeBoi.js
lazy val tradeBoiRoot = project.in(file(".")).aggregate(tradeBoiJVM, tradeBoiJS)
