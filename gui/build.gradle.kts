plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":rdr"))
    implementation(project(":prt"))

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("io.nthcristian.zplrdr.gui.Main")
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
