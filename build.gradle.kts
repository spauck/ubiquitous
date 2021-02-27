plugins {
  val kotlinVersion = "1.4.30"
  kotlin("jvm") version kotlinVersion apply false
}

subprojects {
  group = "com.github.spauck.ubiquitous"

  repositories {
    mavenCentral()
    jcenter()
  }
}
