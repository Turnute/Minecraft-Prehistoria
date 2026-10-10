# Minecraft Prehistoria

**Minecraft Prehistoria** est un mod Minecraft développé dans le cadre d'une **game jam étudiante sur le thème des dinosaures**.

Le mod cherche à apporter une touche de préhistoire à Minecraft en enrichissant l'exploration, la faune et l'archéologie du jeu, tout en conservant un attrait "vanilla".

### Fonctionnalités

- **Jungle Dino Dino** : une nouvelle structure abritant des créatures préhistoriques uniques.
- **Créatures** : de nouvelles créatures interactibles à rencontrer au cours de l'exploration.
- **Archéologie** : une table de fouille et des mécaniques dédiées à la découverte et à l'étude.
- **Archéologue** : un nouveau métier de villageois, avec ses propres échanges.

## Développement

### Setup dev

1. Installer un JDK compatible avec le projet. Le template actuel cible **Java 25**.
2. Ouvrir le dossier dans IntelliJ IDEA ou VS Code.
3. Importer le projet Gradle.
4. Lancer le client Minecraft :

```bash
gradlew.bat runClient
```

### Structure du projet

- `turnute.mcprehistoria.MinecraftPrehistoria` : point d'entrée du mod.
- `registry/` : items, blocks, entities, loot hooks.
- `village/` : archéologue, trades, POI.
- `archaeology/` : logique de table de fouille et descriptions.
- `world/` : génération de la Jungle Dino Dino et règles de spawn.
- `entity/dino/` : mobs de la jungle.
- `entity/aquatic/` : calmar géant et logique liée aux océans.
- `docs/` : roadmap, conventions et tâches simples.
