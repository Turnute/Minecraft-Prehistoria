# Conventions de code

## Noms

- Mod ID / namespace : `minecraft-prehistoria`
- Package Java : `turnute.mcprehistoria`
- IDs registry : `snake_case`
- Classes Java : `PascalCase`
- Méthodes / variables : `camelCase`

Exemples :

- `dino_jungle_map`
- `excavation_table`
- `giant_squid`
- `DinoJungleStructure`
- `GiantSquidEntity`

## Organisation

Ne pas tout mettre dans la classe principale.

La classe principale doit seulement appeler :

```java
ModItems.register();
ModBlocks.register();
ModEntities.register();
```

Chaque nouveau système doit avoir son fichier dédié.
