import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  id("info.solidsoft.pitest") version "1.5.1"
  jacoco
  id("org.sonarqube") version "3.0"
}

sonarqube {
  properties {
    property("sonar.projectKey", "spauck_ubiquitous")
    property("sonar.organization", "spauck")
    property("sonar.host.url", "https://sonarcloud.io")
  }
}

version = "0.1-SNAPSHOT"

dependencies {
  implementation("org.slf4j:slf4j-simple:1.7.26")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")

  testImplementation(project(":client"))
  val junitVersion = "5.7.0"
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
  testImplementation("org.assertj:assertj-core:3.17.2")
  testImplementation("org.mockito:mockito-core:3.5.13")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.19.0")
  testImplementation("io.rest-assured:rest-assured:4.3.1")

  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed")
  }
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.isEnabled = true
    csv.isEnabled = false
    html.isEnabled = true
  }
}

pitest {
  junit5PluginVersion.set("0.12")
  threads.set(4)
  outputFormats.set(listOf("XML", "HTML"))
  timestampedReports.set(false)
}
