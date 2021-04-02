import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    application
}

group = "me.kuban"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}



tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "13"
}

application {
    mainClassName = "MainKt"
}


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
}