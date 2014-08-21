name := "Lift API for GoogleMaps"

version := "0.6"

organization := "com.github.antidata"

scalaVersion := "2.10.2"

resolvers ++= Seq("snapshots"     at "http://oss.sonatype.org/content/repositories/snapshots",
                  "staging"       at "http://oss.sonatype.org/content/repositories/staging",
                  "releases"      at "http://oss.sonatype.org/content/repositories/releases"
                 )

seq(webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val liftVersion = "3.0-M1"
  Seq(
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile",
    "net.liftweb"       %% "lift-mapper"        % liftVersion        % "compile",
    "net.liftmodules" %% "lift-jquery-module_3.0" % "2.5",
    "org.eclipse.jetty" % "jetty-webapp"        % "8.1.13.v20130916"  % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"    % "logback-classic"     % "1.0.6",
    "org.specs2"        %% "specs2"             % "1.14"             % "test",
    "com.h2database"    % "h2"                  % "1.3.167",
    "com.twitter" %% "finagle-http" % "6.7.4"
  )
}

