plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":rdr"))
    implementation(project(":prt"))
    implementation(libs.picocli)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("io.nthcristian.zplrdr.cli.Main")
    applicationName = "zplrdr"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
