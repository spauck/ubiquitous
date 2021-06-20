plugins {
  val kotlinVersion = "1.5.10"
  kotlin("jvm") version kotlinVersion apply false
}

subprojects {
  group = "com.github.spauck.ubiquitous"

  repositories {
    mavenCentral()
    jcenter()
  }
}
