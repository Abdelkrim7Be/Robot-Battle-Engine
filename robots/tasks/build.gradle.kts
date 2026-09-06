plugins {
    id("java")
}

val teamName = "fr.ensibs.tasks"

dependencies {
    implementation(project(":api"))
}

tasks.withType<Jar> {
    destinationDirectory = File("../libs")
    archiveBaseName = teamName
}

tasks {
    getByName<Delete>("clean") {
        delete.add("../libs/$teamName.jar")
    }
}

val teamsDir = File(rootProject.projectDir.parentFile, "libs")
tasks.register("createTeamsDir") {
    doLast {
        teamsDir.mkdirs()
    }
}

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

tasks.register("buildAllTeams") {
    dependsOn("buildAbdelkrimTeam", "buildAbdelhakimTeam", "buildAbdelrazakTeam", "buildNassimTeam", "buildBHHHTeam")
    description = "Build all team JAR files"
}
