# Maze Generation and Solver

This project implements a maze generation and solving game in Java using Kruskal's algorithm for generating the maze and Breadth-First Search (BFS) and Depth-First Search (DFS) for solving it. The project includes graphical visualization and user interaction.

## Table of Contents
- [Features](#features)
- [Technologies](#technologies)
- [How to Run](#how-to-run)
- [Controls](#controls)
- [Customization](#customization)
- [Maze Game Preview](#maze-game-preview)

## Features
- **Maze Generation**: Uses Kruskal's algorithm to generate random mazes of customizable size.
- **Maze Solving**: Implements both BFS and DFS algorithms to solve the maze and trace the solution.
- **Graphical Interface**: The maze and its solution are visualized using the `javalib.worldimages` library, with the player and maze paths displayed in different colors.
- **Custom Controls**: Users can interact with the maze using keyboard inputs, including resetting, solving, and switching algorithms.
- **Randomized Mazes**: Mazes can be generated with random weights, or seeded for reproducible results.

## Technologies
- **Java**: Core programming language used to build the project.
- **Kruskal's Algorithm**: For maze generation using Minimum Spanning Tree (MST).
- **BFS and DFS**: For solving the maze with different strategies.
- **Java Libraries**: 
  - `javalib.worldimages` and `javalib.impworld` for graphical visualization.
  - `tester.jar` for unit testing.

## How to Run
### Prerequisites
- Java Development Kit (JDK) 8 or higher installed.
- Git (optional, but recommended).
- Eclipse IDE is downloaded.

### Steps
1. Pull this repository, and open Eclipse. Ensure EclipseJars is in the same directory as your eclipse workspace.
2. Import Maze into your workspace and add the jar files to the build path.
3. In the Eclipse run configurations, open the Java Application tab and set the Main class to tester.Main. In the Arguments tab add "ExamplesMaze" to the Program Arguments.
4. Run the program, a GUI window should pop up displaying the game board.

## Controls
- Use arrow keys to move through the maze.
- r: Resets the maze.
- n: Generates a new maze with random weights.
- b: Solves the maze using BFS
- d: Solves the maze using DFS
- h: Generates a maze with horizontal edges.
- v: Generates a maze with vertical edges.
- s: Toggles path visualization on and off.

## Customization
- The maze size can be adjusted by modifying the width and height parameters when creating a new MazeWorld instance within the code.

## Maze Game Preview

![30x30 Maze Game Preview](screenshots/30x30.jpg)

*30x30 board*

![60x60 Maze Game Preview](screenshots/60x60.jpg)

*60x60 board*

![Game In Progress](screenshots/in-progress.jpg)

*As player moves, the already traveled path will remain highlighted red"

![DFS](screenshots/dfs.jpg)

*Game solved using depth first search*

![BFS](screenshots/bfs.jpg)

*Game solved using breadth first search*
