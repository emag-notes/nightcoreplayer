lazy val commonSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.12.4",
  test in assembly := {}
)

lazy val app = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    mainClass in assembly := Some("nightcoreplayer.Main"),
    assemblyJarName in assembly := "nightcoreplayer.jar",
    unmanagedJars in Compile += {
      sys.props
      val sysprops = new sys.SystemProperties
      val jh       = sysprops("java.home")
      Attributed.blank(file(jh) / "lib/ext/jfxrt.jar")
    }
  )
