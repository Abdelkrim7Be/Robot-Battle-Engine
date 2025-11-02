// plugins to make additional functionalities available
plugins {
    // java plugin: create a jar library 
    id("java")  
}

// your team name
val team_name = "examples" // TODO replace 'examples' with your team name

// project dependencies
dependencies {
    implementation(project(":api"))
}

// copy the jar file to the shared "libs/" submodule
tasks.withType<Jar> {
    destinationDirectory = File("../libs")
    archiveBaseName = "${team_name}"
}

// add the generated jar to the clean task
tasks {
    getByName<Delete>("clean") {
        delete.add("../libs/${team_name}.jar")
    }
}