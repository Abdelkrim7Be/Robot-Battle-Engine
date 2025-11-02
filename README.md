The main robots project to be completed.

## Project structure

This project is composed of 4 subfolders:

* `api` subproject: contains the shared classes and interfaces that will be used to develop the robots' application. 
  It is a GIT submodule that references the shared `api` GIT project
* `app` subproject: contains the application `Launcher` and should be completed to provide implementations for the
  interfaces and abstract classes defined in the `api` subproject
* `tasks` subproject: in this project, you should implement your own robots tasks. It contains an examples of some 
  very naive tasks implementations
* `libs` subfolder: the jar files containing the tasks implementation will be generated in this folder. 
  It is a GIT submodule that references the shared `libs` GIT project

The dependencies between the 3 subprojects (no dependency between `app` and `tasks`):

      app <-- depends on --+
                           |---> api
    tasks <-- depends on --+

The `build.gradle.kts` file in the `tasks` project defines the jar task properties in order to generate the jar file
in the `libs` folder

## Create your own project (1st project member)

* copy this project to your own project folder (except the `api` and `libs` folders)
* initialize your GIT project: `git init --initial-branch=main`
* include the `api` and `libs` submodules (see `README.md` in the `libs` project)
* replace `examples` with your team name in the `tasks/build.gradle.kts` and `tasks/src/main/java/module-info.java` files

And then execute the following steps to initialize your project to the remote repository: 

    git remote add origin <your_project_url>
    git add .
    git commit -m "Initial commit"
    git push --set-upstream origin main

## Clone your project (other project members)

    git clone --recurse-submodules <your_project_url>

## Develop your project

* Game implementation: see the `TODO` comments in the `app` sources. you can add as many classes as you want.
* Robots tasks: develop `RobotTask` implementations (see the `examples` classes) in the `tasks` project 
  (and delete the `examples` package).

