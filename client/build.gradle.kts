import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
}

version = "0.1-SNAPSHOT"

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  testImplementation(project(":server"))

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
