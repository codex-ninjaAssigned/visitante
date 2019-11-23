organization := "mawazo"

name := "visitante"

version := "1.0"

scalaVersion := "2.12.0"

isSnapshot := true

sources in (Compile, doc) ~= (_ filter (_.getName endsWith ".scala"))

packageBin in Compile := file(s"target/${name.value}-${version.value}.jar")