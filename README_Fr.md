# Bataille Navale — Extensions statistiques

Ce dépôt contient une implémentation Java du jeu de la bataille navale ainsi que des outils pour évaluer des stratégies automatisées (bots) et produire des résultats expérimentaux reproductibles.

<<<<<<< Updated upstream
Fonctionnalités principales

- Moteur de jeu complet (grille, navires, gestion des tirs).
- Plusieurs joueurs automatisés : `Uniforme`, `Markov`, `MonteCarlo`, `Smart`.
- Outils statistiques pour exécuter des tournois et synthétiser les résultats
  (CSV, rapport lisible, graphiques PNG).

Prérequis

- JDK 11 ou supérieur.
- Shell POSIX (ex. `bash`).

Compilation
=======
## Fonctionnalités

- Implémentation Java modulaire du moteur de jeu (grille, navires, tirs).
- Plusieurs implémentations de bots pour l'évaluation : `Uniform`, `Markov`, `MonteCarlo`, `Smart`.
- Interface graphique simple (Swing) pour jeu interactif.
- Outils statistiques pour exécuter des tournois et des expériences, produisant des résumés CSV et des visualisations PNG.

## Documentation et rapport

- La documentation du projet (référence API et notes développeur) se trouve dans le répertoire `docs/`. La référence API générée par `javadoc` est accessible via `docs/api/index.html`.
- Un rapport académique (sources LaTeX et instructions de compilation) est disponible dans `rapport/`. Le rapport décrit le jeu, les heuristiques, le protocole expérimental et contient des sections d'analyse des résultats.

## Prérequis

- JDK 11 ou version plus récente.
- Shell POSIX compatible (`bash` pour les exemples).

## Compilation

```bash
# Compiler les sources dans le répertoire `bin`
javac -d bin $(find src -name "*.java")
```

## Exécution des exemples

- Lancer l'interface graphique :

```bash
java --module-path bin -m ComplementIA/bataillenavale.Main
```

- Lancer un tournoi (ex. 1000 matchs par affrontement) :

```bash
java --module-path bin -m ComplementIA/statistique.Tournament 1000
```

- Lancer l'outil de performance (ex. 100 auto-parties par bot) :

```bash
java --module-path bin -m ComplementIA/statistique.Performance 100
```

## Consulter la documentation générée

- Ouvrir l'API générée dans votre navigateur :
>>>>>>> Stashed changes

```bash
xdg-open docs/api/index.html
```

- Ou servir le répertoire `docs/` via un serveur HTTP simple (utile selon le navigateur) :

```bash
cd $(git rev-parse --show-toplevel)
python3 -m http.server 8000 --directory docs
# puis ouvrir http://localhost:8000/api/ dans votre navigateur
```

## Compilation du rapport académique

Les sources LaTeX du rapport se trouvent dans `rapport/`. Depuis la racine du dépôt :

```bash
cd rapport
make
# sortie : rapport/main.pdf
```

Si vous n'avez pas une distribution LaTeX complète, installez TeX Live ou utilisez un service LaTeX en ligne.

## Sorties

Toutes les sorties sont écrites dans `Results/` :

- `tournament_pairwise.csv` — matrice de victoires pairwise.
- `tournament_summary.csv` — résumé par bot (`games_played`, `wins`, `win_rate`, `std_error`, `rank`).
- `tournament_pairwise_table.txt` — tableau pairwise lisible et classement.
- `performance_summary.csv` — résumé des auto-parties (moyenne de coups, erreur standard, ...).
- `performance_gaussian_overlay.png` — visualisation PNG des distributions.

## Structure du projet

- `src/` — packages source Java :
  - `bataillenavale/` — points d'entrée et UI (`Main`, `BatailleNavale`).
  - `logique/` — logique centrale (`GrilleNavale`, `Navire`, `Coordonnee`).
  - `joueurs/` — joueurs et implémentations (`Joueur`, `Bot`, `SmartBot`).
  - `heuristic/` — interface et heuristiques (`Heuristic`, `Uniform`, `Markov`, `MonteCarlo`).
  - `interfacegraphique/` — composants Swing.
  - `statistique/` — utilitaires expérimentaux (`Tournament`, `Performance`).
- `bin/` — classes compilées (résultat de `javac -d bin`).
- `docs/` — documentation générée (HTML API et autres docs).
  - `rapport/` — sources LaTeX et fichiers du rapport (compilez pour obtenir `rapport/main.pdf`).
- `Results/` — sorties des expériences.

## Contribuer

Forkez le dépôt et ouvrez une pull request. Merci d'inclure :

- JavaDoc pour les classes publiques affectées.
- Un exemple / test de régression lorsque le comportement change.
- Les seeds et la commande exacte utilisées pour toute expérience afin d'assurer la reproductibilité.

## Licence

Voir le fichier `LICENSE` à la racine du dépôt (MIT).

<<<<<<< Updated upstream
- Le projet utilise le système de modules Java (`module-info.java`).
- Les identifiants et noms de packages sont principalement en français.
- Les messages affichés à l'utilisateur sont en français.

Automatisation

Des scripts d'automatisation (`run_experiments.sh`) et un `CONTRIBUTING.md`
plus détaillé peuvent être ajoutés ultérieurement.

---

Merci d'avoir consulté ce dépôt. Pour toute question, ouvrez une issue.
=======
>>>>>>> Stashed changes
