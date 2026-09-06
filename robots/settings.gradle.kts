// the root project name
rootProject.name = "robots"

// the sub-projects
include("api", "app", "tasks")

// point api module to the shared subproject outside this repo
project(":api").projectDir = File(rootDir, "../api")
