# Robot Wars - Battle Royale Engine

A professional-grade Battle Royale engine for robot combat, built with Java and following SOLID principles, Clean Architecture, and TDD practices.

## Project Structure

This project is composed of 4 subfolders:

* `api` subproject: Contains the shared classes and interfaces used to develop the robots' application. It is a GIT submodule that references the shared `api` GIT project.
* `app` subproject: Contains the application `Launcher` and implementations for the interfaces and abstract classes defined in the `api` subproject.
* `tasks` subproject: Implement your own robot tasks here. Contains examples of naive task implementations.
* `libs` subfolder: JAR files containing task implementations are generated here. It is a GIT submodule that references the shared `libs` GIT project.

The dependencies between the 3 subprojects (no dependency between `app` and `tasks`):

```
      app <-- depends on --+
                           |---> api
    tasks <-- depends on --+
```

## How to Play

### Running the Game

1. **Build the project:**
   ```bash
   gradle build
   ```

2. **Run the application:**
   ```bash
   gradle :app:run
   ```
   Or use the generated launcher:
   ```bash
   ./app/build/scripts/app
   ```

3. **Load Robots:**
   - Click the "LOAD" button in the control panel
   - Select a robot class from the dialog
   - Choose a color for your robot
   - Click "OK" to add the robot to the battlefield

4. **Start the Battle:**
   - Click the "START" button to begin the battle
   - Watch robots fight in real-time on the battlefield
   - The battle continues until only one robot remains (or all are eliminated)

### Game Features

- **HUD Overlay:** Shows energy, gun heat, and robot name above each robot
- **Leaderboard:** Displays top 5 robots ranked by energy (top-left corner)
- **Particle Effects:** Visual feedback for hits and explosions
- **Grid Background:** Helps with positioning and navigation
- **Team Coordination:** Team leaders can command their teammates

### Controls

- **LOAD Button:** Add new robots to the battlefield
- **START/STOP Button:** Start or pause the battle
- **Robot Table:** Shows all active robots with their current energy levels

### Game Mechanics

- **Energy:** Robots consume energy when moving, firing, or scanning. Energy is lost when hit by bullets or collisions.
- **Gun Heat:** Firing increases gun heat. Guns overheat if heat > 1, preventing further firing until cooled.
- **Life Steal:** Successfully hitting an enemy restores energy (3 × bullet power).
- **Collisions:** Robots take damage when colliding with walls or other robots.
- **Radar:** Robots can scan for enemies in a 90° field of vision.

### Creating Your Own Robot

1. Create a class in the `tasks` project that implements `RobotTask<Robot>`
2. Implement the `run()` method with your robot's AI logic
3. Build the tasks project: `gradle :tasks:build`
4. Load your robot using the "LOAD" button

### Team Play

- Create a `RobotTask<TeamLeader>` to control a team
- Use command methods (`commandMove()`, `commandFire()`, etc.) to coordinate teammates
- Team leaders can broadcast messages to all teammates

## Development

### Running Tests

```bash
gradle test
```

### Project Setup

1. Copy this project to your own folder (except `api` and `libs` folders)
2. Initialize GIT: `git init --initial-branch=main`
3. Include `api` and `libs` submodules
4. Replace `examples` with your team name in `tasks/build.gradle.kts` and `tasks/src/main/java/module-info.java`

### Architecture

The project follows Clean Architecture principles:
- **Logic Layer:** Core game mechanics (api module)
- **Implementation Layer:** Concrete implementations (app module)
- **Task Layer:** Custom robot AI (tasks module)

All code follows SOLID principles and includes comprehensive unit tests.
