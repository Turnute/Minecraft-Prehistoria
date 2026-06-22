# Begin Here — Minecraft Prehistoria

## Principe

Chaque système a son dossier et sa classe d'entrée :

- `ModItems` : tous les items.
- `ModBlocks` : tous les blocks.
- `ModEntities` : tous les mobs.
- `ModVillagers` : archéologue, table de métier, trades.
- `ModArchaeology` : descriptions et logique de fouille.
- `ModLoot` : loot tables / ajouts de loot.
- `ModWorldgen` : structure et spawns monde.

La classe principale appelle tout dans un ordre simple. Comme ça, chaque personne peut travailler dans son coin sans exploser le projet.

## Version mini jouable

1. Ajouter la table de fouille comme simple bloc.
2. Ajouter un premier item oeuf.
3. Ajouter une description hardcodée dans la table de fouille.
4. Ajouter un premier mob très simple.
5. Ajouter son œuf en loot de suspicious sand/gravel.
6. Ajouter l'archéologue avec 2-3 trades.
7. Ajouter la structure seulement après avoir au moins un gameplay loop testable.
