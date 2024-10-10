plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "main"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.h2database:h2:2.1.214") // using the same version as Prof in class
}

application {
    mainModule.set("main")
    mainClass.set("main.Main")
}

javafx {
    version = "21.0.4"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.media", "javafx.web", "javafx.swing")
}

tasks.test {
    useJUnitPlatform()
}