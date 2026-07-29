plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":rdr"))

    implementation(libs.pdfbox)

    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("java.awt.headless", "true")
}
