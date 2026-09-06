plugins {
    id("application")
}

application {
    mainModule = "fr.ensibs.robots.app"
    mainClass = "fr.ensibs.robots.Launcher"
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(project(":api"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}
