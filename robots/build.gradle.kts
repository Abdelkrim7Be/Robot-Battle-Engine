
// clean task: delete the root build directory
tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}