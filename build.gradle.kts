import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.7.0"
  application
  id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.1.0"
val junitJupiterVersion = "5.7.0"

val mainVerticleName = "com.example.starter.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  applicationDefaultJvmArgs = listOf("-Djava.library.path =/export/pytorch/path/libtorch/lib")
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web-validation")
  implementation("io.vertx:vertx-config")
  implementation("io.vertx:vertx-auth-jwt")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-pg-client")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-lang-kotlin")

  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.0")
  implementation("org.jetbrains.kotlinx:multik-core:0.2.1")
  implementation("org.jetbrains.kotlinx:multik-default:0.2.1")
  implementation("org.jetbrains.kotlinx:multik-api:0.1.1")
  implementation("org.jetbrains.bio:npy:0.3.5")
  implementation("org.pytorch:pytorch_java_only:1.10.0")
  implementation("com.google.code.gson:gson:2.10.1")

  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "11"

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf(
    "run",
    mainVerticleName,
    "--redeploy=$watchForChange",
    "--launcher-class=$launcherClassName",
    "--on-redeploy=$doOnChange"
  )
}
