/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt.*
import sbt.Keys.*

val libName = "catalogue-wrapper"

ThisBuild / scalaVersion      := "3.3.7"
ThisBuild / majorVersion      := 0
ThisBuild / isPublicArtefact  := true
ThisBuild / organization      := "uk.gov.hmrc"
ThisBuild / scalafmtOnCompile := true

lazy val library = Project(libName, file("."))
  .settings(publish / skip := true)
  .aggregate(play30)

lazy val play30 = Project(s"$libName-play-30", file(s"$libName-play-30"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    libraryDependencies ++= LibDependencies.play30 ++ LibDependencies.play30Test,
    Compile / routes / sources ++= {
      val dirs = (Compile / unmanagedResourceDirectories).value
      (dirs * "routes").get ++ (dirs * "*.routes").get
    },
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=twirl/.*:s",
      "-Wconf:msg=Flag.*repeatedly:s"
    ),
    Test / Keys.fork := true,
    resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
  )
