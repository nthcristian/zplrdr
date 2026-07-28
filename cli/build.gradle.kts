plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // Tells the CLI to use the code from your 'lib' module
    implementation(project(":rdr"))
}

application {
    // Point this to your new Main class
    mainClass.set("io.nthcristian.zplrdr.cli.Main")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}