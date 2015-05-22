name := "LazyExp"

// The := method used in Name and Version is one of two fundamental methods.
// The other method is <<=
// All other initialization methods are implemented in terms of these.
version := "0.1"

organization := "org.lazyexp"

// append -deprecation to the options passed to the Scala compiler
scalacOptions += "-deprecation"

// set the Scala version used for the project
//scalaVersion := "2.11.6"

libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

testOptions in Test += Tests.Argument("-oDF")

// set the main Scala source directory to be <base>/src
scalaSource in Compile <<= baseDirectory(_ / "src")

// set the Scala test source directory to be <base>/test
scalaSource in Test <<= baseDirectory(_ / "test")

// set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => "LazyExp" + "> " }

//set the main class for packaging the main jar
// 'run' will still auto-detect and prompt
// change Compile to Test to set it for the test jar
mainClass in (Compile, packageBin) := Some("org.TestLazyCompo")

// set the main class for the main 'run' task
// change Compile to Test to set it for 'test:run'
//mainClass in (Compile, run) := Some("org.ReallyTestCompo")

unmanagedClasspath in Runtime <+= (baseDirectory) map { bd => Attributed.blank(bd / "config") }

// reduce the maximum number of errors shown by the Scala compiler
maxErrors := 10

// append -deprecation to the options passed to the Scala compiler
scalacOptions += "-deprecation"

// define the statements initially evaluated when entering 'console', 'console-quick', or 'console-project'
initialCommands := """
  import System.{currentTimeMillis => now}
  def time[T](f: => T): T = {
    val start = now
    try { f } finally { println("Elapsed: " + (now - start)/1000.0 + " s") }
  }
"""

// enable printing timing information, but still print [success]
showTiming := true //false

// enable printing a message indicating the success or failure of running a task
showSuccess := true

// change the format used for printing task completion time
timingFormat := {
    import java.text.DateFormat
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
}

// disable using the Scala version in output paths and artifacts
crossPaths := false

// add a JVM option to use when forking a JVM for 'run'
javaOptions += "-Xmx2G"

// only use a single thread for building
parallelExecution := false

// Execute tests in the current project serially
//   Tests from other projects may still run concurrently.
parallelExecution in Test := false

// set the location of the JDK to use for compiling Java code.
// if 'fork' is true, this is used for 'run' as well
//javaHome := Some(file("/usr/lib/jvm/sun-jdk-1.6"))

// Use Scala from a directory on the filesystem instead of retrieving from a repository
//scalaHome := Some(file("//home/lastefan/tools/scala-2.11.6/"))

// don't aggregate clean (See FullConfiguration for aggregation details)
aggregate in clean := false

// only show warnings and errors on the screen for compilations.
//  this applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// only show warnings and errors on the screen for all tasks (the default is Info)
//  individual tasks can then be more verbose using the previous setting
logLevel := Level.Info

// only store messages at info and above (the default is Debug)
//   this is the logging level for replaying logging with 'last'
persistLogLevel := Level.Debug

// only show 10 lines of stack traces
traceLevel := 10

// only show stack traces up to the first sbt stack frame
traceLevel := 0

// publish test jar, sources, and docs
publishArtifact in Test := true

// disable publishing of main docs
publishArtifact in (Compile, packageDoc) := false

// change the classifier for the docs artifact
artifactClassifier in packageDoc := Some("doc")

// Copy all managed dependencies to <build-root>/lib_managed/
//   This is essentially a project-local cache and is different
//   from the lib_managed/ in sbt 0.7.x.  There is only one
//   lib_managed/ in the build root (not per-project).
retrieveManaged := true

fork := true
