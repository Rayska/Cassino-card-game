ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "CassinoRaySBT"
  )

libraryDependencies += "org.scalafx" % "scalafx_3" % "19.0.0-R30" // Added by Ray just so I know it myself if I forget

