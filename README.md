# Robot Wars Battle Engine

Robot battle simulator for ENSIBS 4th year Advanced OOP course. Teams of robots fight on a battlefield following specific rules for energy, damage, and targeting.

## Structure

This project is composed of 4 folders:

- `api`: Contains interfaces and base classes that define the contracts for robots and game components. This is a GIT submodule.
- `app`: Contains the main application launcher and implementations of the interfaces defined in the api module.
- `tasks`: Contains robot task implementations. Each team implements their own robot AI logic here.
- `libs`: Contains shared utilities and team JAR files that are loaded at runtime. This is a GIT submodule.

## Requirements

- Java 17 or higher
- Gradle 8 or higher

## Build

To build the project:

```bash
gradle build
```

## Run

To run the application:

```bash
gradle :app:run
```

## Usage

1. Start the application using the run command above.
2. Click the "LOAD" button to select teams from available robot implementations.
3. Add teams to the battle by selecting them and clicking "Add to Battle".
4. Choose a color for each team.
5. Click "START" to begin the battle.
6. Watch the robots fight until one team remains or all are eliminated.

## Rules

Each team consists of one leader and multiple droids. The leader has a radar and can scan for enemies. Droids have no radar and must follow leader commands.

Robots consume energy when moving, firing, or scanning. Energy is lost when hit by bullets or collisions. Firing increases gun heat. Guns overheat if heat exceeds 1, preventing further firing until cooled.

Damage formula: 4 times power plus 2 times power minus 1 if power is greater than 1. Life steal restores 3 times power when successfully hitting an enemy.

The last team with at least one alive robot wins the battle.
