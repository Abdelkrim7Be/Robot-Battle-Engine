rootProject.name = "robots"

include("api", "app", "tasks")

project(":api").projectDir = File(rootDir, "../api")
