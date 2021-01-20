import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
}

version = "0.1-SNAPSHOT"

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}
