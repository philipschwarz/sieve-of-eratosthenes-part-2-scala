ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.2"

lazy val root = (project in file("."))
  .settings(
    name := "sieve-of-eratosthenes-part-2-scala"
  )

  libraryDependencies ++= Seq("org.typelevel" %% "cats-core" % "2.8.0")