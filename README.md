# Minecraft Prehistoria

## Setup dev

1. Installer un JDK compatible avec le projet. Le template actuel cible **Java 25**.
2. Ouvrir le dossier dans IntelliJ IDEA ou VS Code.
3. Importer le projet Gradle.
4. Lancer :
``` bash
   gradlew.bat runClient
```
## Structure rapide

* `turnute.mcprehistoria.MinecraftPrehistoria` : point d'entrée du mod.
* `registry/` : items, blocks, entities, loot hooks.
* `village/` : archéologue, trades, POI.
* `archaeology/` : logique de table de fouille et descriptions.
* `world/` : structure de la Jungle Dino Dino, biome interne et worldgen.
* `entity/insect/` : Megameli et futurs insectes préhistoriques.
* `entity/aquatic/` : calmar géant et logique liée aux océans.
* `docs/` : roadmap, conventions, tâches simples.