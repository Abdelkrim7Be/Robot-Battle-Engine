// plugins to make additional functionalities available
plugins {
    // java plugin: create a jar library 
    id("java")  
}

// your team name
val team_name = "fr.ensibs.tasks"

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

// Create teams/ directory for team JARs
val teamsDir = File(rootProject.projectDir, "teams")
tasks.register("createTeamsDir") {
    doLast {
        teamsDir.mkdirs()
    }
}

// Task to build AbdelkrimS team JAR
tasks.register<Jar>("buildAbdelkrimTeam") {
    dependsOn("createTeamsDir", "classes")
    archiveBaseName.set("AbdelkrimS")
    destinationDirectory.set(teamsDir)
    
    from(sourceSets.main.get().output) {
        include("fr/ensibs/tasks/teams/abdelkrim/**")
    }
    
    manifest {
        attributes(mapOf("Main-Class" to "fr.ensibs.tasks.teams.abdelkrim.AbdelkrimLeader"))
    }
}

// Task to build AbdelhakimS team JAR
tasks.register<Jar>("buildAbdelhakimTeam") {
    dependsOn("createTeamsDir", "classes")
    archiveBaseName.set("AbdelhakimS")
    destinationDirectory.set(teamsDir)
    
    from(sourceSets.main.get().output) {
        include("fr/ensibs/tasks/teams/abdelhak/**")
    }
    
    manifest {
        attributes(mapOf("Main-Class" to "fr.ensibs.tasks.teams.abdelhak.AbdelhakimLeader"))
    }
}

// Task to build AbdelrazakS team JAR
tasks.register<Jar>("buildAbdelrazakTeam") {
    dependsOn("createTeamsDir", "classes")
    archiveBaseName.set("AbdelrazakS")
    destinationDirectory.set(teamsDir)
    
    from(sourceSets.main.get().output) {
        include("fr/ensibs/tasks/teams/abdelrazak/**")
    }
    
    manifest {
        attributes(mapOf("Main-Class" to "fr.ensibs.tasks.teams.abdelrazak.AbdelrazakLeader"))
    }
}

// Task to build NassimS team JAR
tasks.register<Jar>("buildNassimTeam") {
    dependsOn("createTeamsDir", "classes")
    archiveBaseName.set("NassimS")
    destinationDirectory.set(teamsDir)
    
    from(sourceSets.main.get().output) {
        include("fr/ensibs/tasks/teams/nassim/**")
    }
    
    manifest {
        attributes(mapOf("Main-Class" to "fr.ensibs.tasks.teams.nassim.NassimLeader"))
    }
}

// Task to build BHHH team JAR
tasks.register<Jar>("buildBHHHTeam") {
    dependsOn("createTeamsDir", "classes")
    archiveBaseName.set("BHHH")
    destinationDirectory.set(teamsDir)
    
    from(sourceSets.main.get().output) {
        include("fr/ensibs/tasks/teams/bhhh/**")
    }
    
    manifest {
        attributes(mapOf("Main-Class" to "fr.ensibs.tasks.teams.bhhh.BHHHLeader"))
    }
}

// Task to build all teams
tasks.register("buildAllTeams") {
    dependsOn("buildAbdelkrimTeam", "buildAbdelhakimTeam", "buildAbdelrazakTeam", "buildNassimTeam", "buildBHHHTeam")
    description = "Build all team JAR files"
}