# Robot Wars Battle Engine

Simulateur de batailles de robots pour le cours de POO avancée (4e année ENSIBS).
Des équipes de robots s’affrontent sur un champ de bataille avec gestion d’énergie, dégâts et ciblage.
Exécutable via Docker (recommandé) ou Java/Gradle, avec interface graphique.
Projet modulaire : API, application, tâches (IA des équipes) et librairies partagées.

## Démonstration

Vous pouvez visionner une courte démonstration du jeu ci-dessous :

<video src="./demo-jeu-java.mp4" controls width="720">
Votre navigateur ne supporte pas la balise vidéo. Téléchargez la vidéo ici : [demo-jeu-java.mp4](./demo-jeu-java.mp4).
</video>

## Structure

This project is composed of 4 folders:

- `api`: Contains interfaces and base classes that define the contracts for robots and game components. This is a GIT submodule.
- `app`: Contains the main application launcher and implementations of the interfaces defined in the api module.
- `tasks`: Contains robot task implementations. Each team implements their own robot AI logic here.
- `libs`: Contains shared utilities and team JAR files that are loaded at runtime. This is a GIT submodule.

## Requirements

- Java 17 or higher
- Gradle 8 or higher

**OR**

- Docker (for the easiest setup - no Java/Gradle installation needed!)

## Quick Start

### 🐳 Docker Run (Easiest - Recommended!)

**Just run this from the robots/ folder:**

```bash
./run-docker.sh
```

That's it! The script will:
- ✅ Check if Docker is installed and running
- ✅ Set up X11 forwarding for GUI
- ✅ Build the Docker image (first time only)
- ✅ Run the application with all dependencies included

**What you need:**
- Docker installed
- That's it! No Java or Gradle needed.

**Note:** Make sure you're running from a graphical session (X11).

### Standard Run (Requires Java 17+ and Gradle)

To build the project:

```bash
gradle build
```

To run the application locally:

```bash
gradle :app:run
```

## Docker Details

The Docker setup includes:
- Java 17 (Eclipse Temurin)
- Gradle 8.5
- All project dependencies
- X11 libraries for GUI display
- Automatic class version patching (supports JARs compiled with Java 21)

**Files:**
- `Dockerfile` - Builds the Docker image
- `run-docker.sh` - Simple script to run the app
- `docker-compose.yml` - Alternative way to run (optional)
- `.dockerignore` - Excludes unnecessary files from build

**Troubleshooting:**
- If GUI doesn't appear: Make sure X11 forwarding is set up (`xhost +local:docker` on Linux)
- If teams don't load: Check that `libs/` directory contains team JAR files
- First run is slow: Docker needs to download base image and build everything (subsequent runs are fast)

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
