// plugins to make additional functionalities available
plugins {
    // application plugin: create an executable Java application 
    id("application")  
}

// properties for the application plugin
application {
    // the main module and class of the Java application
    mainModule = "fr.ensibs.robots.app"
    mainClass = "fr.ensibs.robots.Launcher"  
}

// repositories for project dependencies
repositories {
    mavenCentral()
    mavenLocal()
}

// project dependencies
dependencies {
    implementation(project(":api"))
    // JUnit Jupiter tests
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// JUnit tests configuration
tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events("passed", "failed", "skipped")
    }
} 
