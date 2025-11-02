This project contains the shared classes and interfaces that will be used to develop the robots application: 

* `fr.ensibs.robots.factories` contains the factories you must implement to provide your own implementations 
  of the interfaces and abstract classes of the other packages
* `fr.ensibs.robots.logic` contains the interfaces that define the robots application logic, and some useful classes
  (exceptions and `BattlefieldEngine` class)
* `fr.ensibs.robots.view` contains classes implementing the robots application view (UI) and some abstract classes 
  (`DroidView` and `RobotView`)

It should be included in your project as a GIT submodule (see documentation in the `libs` project)
