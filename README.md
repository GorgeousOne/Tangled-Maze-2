# TangledMaze

A spigot plugin for easily creating and customizing mazes in Minecraft. With a WorldEdit like wand you can visually select areas to build a maze in. This way you have full control over the shape (and dimensions) of your mazes!

For downloads & more information:
<https://www.spigotmc.org/resources/tangled-maze.59284/>


## Features
- Building mazes from rectangles, circles and triangles
- Maze generation that adapts to height differences in the terrain
- Highly customizable maze settings for sizes of paths, walls and composition of building blocks
- Ability to set multiple entrances and exits for a maze
- Possibility to add a roof and change the floor of the maze
- Maze solving feature to see the path connecting all exits in a maze

## Development

To be able to compile the plugin with the [LootChest](https://www.spigotmc.org/resources/lootchest.61564/) dependency, you need to install the LootChest jar file in your local maven repository.
You can do this by running the following command in the directory where the LootChest.jar file is located:
```shell
mvn install:install-file -Dfile=<path/to/LootChest.jar> -DgroupId=fr.black_eyes.lootchest -DartifactId=LootChest -Dversion=2.4.0 -Dpackaging=jar
```

And - I have no slightest idea why - the library HoloEasy compiled inside LootChest.jar was not recognized until I ran:
```shell
mvn install:install-file -Dfile=<path/to/LootChest.jar> -DgroupId=org.holoeasy -DartifactId=holoeasy -Dversion=3.1.1 -Dpackaging=jar
```
