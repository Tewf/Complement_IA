# Projet : Bataille Navale — Extensions statistiques

Ce dépôt contient une implémentation Java du jeu « Bataille Navale » accompagnée
d'outils pour évaluer automatiquement des stratégies (bots) et produire des
résultats expérimentaux réutilisables.

Fonctionnalités principales

- Moteur de jeu complet (grille, navires, gestion des tirs).
- Plusieurs joueurs automatisés : `Uniforme`, `Markov`, `MonteCarlo`, `Smart`.
- Outils statistiques pour exécuter des tournois et synthétiser les résultats
  (CSV, rapport lisible, graphiques PNG).

Prérequis

- JDK 11 ou supérieur.
- Shell POSIX (ex. `bash`).

Compilation

```bash
# Compiler les sources dans `bin`
javac -d bin $(find src -name "*.java")
```

Exemples d'exécution

- Interface graphique :

```bash
java --module-path bin -m ComplementIA/bataillenavale.Main
```

- Lancer un tournoi (ex. 1000 parties par confrontation) :

```bash
java --module-path bin -m ComplementIA/statistique.Tournament 1000
```

- Exécuter l'outil de performance (ex. 100 essais self-play par bot) :

```bash
java --module-path bin -m ComplementIA/statistique.Performance 100
```

Sorties produites

Les fichiers générés sont écrits dans le dossier `Results/` :

- `tournament_pairwise.csv` — matrice des victoires (ligne = vainqueur, colonne = adversaire).
- `tournament_summary.csv` — résumé par bot (games_played, wins, win_rate, standard_error, rank).
- `tournament_pairwise_table.txt` — table lisible et classement.
- `performance_summary.csv` — résumé des essais self-play (moyenne de coups, erreur-type, ...).
- `performance_gaussian_overlay.png` — visualisation PNG des distributions.

Ces fichiers sont encodés en UTF-8 et exploitables par R, Python, Excel, etc.

Structure du projet

- `src/` — sources Java organisées par package :
  - `bataillenavale/` : points d'entrée et UI (`Main`, `BatailleNavale`).
  - `logique/` : coeur du moteur (`GrilleNavale`, `Navire`, `Coordonnee`).
  - `joueurs/` : interfaces et implémentations de joueurs (`Joueur`, `Bot`, `SmartBot`).
  - `heuristic/` : interface heuristique et implémentations (`Heuristic`, `Uniforme`, `Markov`, `MonteCarlo`).
  - `interfacegraphique/` : composants Swing.
  - `statistique/` : utilitaires d'expérimentation (`Tournament`, `Performance`).
- `bin/` — classes compilées (résultat de `javac -d bin`).
- `Results/` — sorties des expérimentations.

Contribuer

- Forkez le dépôt et créez une branche pour votre fonctionnalité.
- Respectez les conventions Java et fournissez JavaDoc pour les classes publiques.
- Pour les changements significatifs, ajoutez un exemple ou un test.
- Ouvrez une pull request avec une description claire des modifications.

Licence

Le dépôt est fourni avec le fichier `LICENSE` (MIT) à la racine.

Notes techniques

- Le projet utilise le système de modules Java (`module-info.java`).
- Les identifiants et noms de packages sont principalement en français.
- Les messages affichés à l'utilisateur sont en français.

Automatisation

Des scripts d'automatisation (`run_experiments.sh`) et un `CONTRIBUTING.md`
plus détaillé peuvent être ajoutés ultérieurement.

---

Merci d'avoir consulté ce dépôt. Pour toute question, ouvrez une issue.


**Build**

```bash
  `stderr = sqrt(p * (1-p) / n)` où `p` est la proportion observée et `n`
 # Projet Bataille Navale — Extensions statistiques
 # Projet Bataille Navale — Extensions statistiques

 Ce dépôt contient une implémentation Java du jeu « Bataille Navale » accompagnée
 d'outils expérimentaux pour l'évaluation automatique de joueurs (bots).
 Le projet fournit : la logique de jeu, plusieurs implémentations de joueurs,
 une interface graphique minimale (Swing) et des utilitaires pour lancer des
 tournois et collecter des métriques reproductibles.

 **Langue :** toute la documentation et les commentaires du code sont en français.

 **Résumé des fonctionnalités**

 - Moteur de jeu complet (grille, navires, coordination des tirs).
 - Joueurs : joueur humain via UI et plusieurs bots (Uniforme, Markov, MonteCarlo, Smart).
 - Architecture modulaire Java (package `src/` organisé par fonctionnalités).
 - Outils statistiques :
   - `Tournament` : tournoi round-robin entre bots (génère CSV et table lisible).
   - `Performance` : self-play par bot pour estimer distributions de performance.
 - Les résultats d'expériences sont écrits dans le dossier `Results/`.


 ## Démarrage rapide (prérequis)

 - JDK 11 ou supérieur.
 - Shell POSIX (exemples fournis pour `bash`).

 Compilation :

 ```bash
 # compiler toutes les sources dans le dossier `bin`
 javac -d bin $(find src -name "*.java")
 ```

 Exécution (exemples) :

 - Lancer l'interface graphique principale :

 ```bash
 java --module-path bin -m ComplementIA/bataillenavale.Main
 ```

 - Lancer un tournoi (ex. 1000 parties par confrontation) :

 ```bash
 java --module-path bin -m ComplementIA/statistique.Tournament 1000
 ```

 - Lancer l'outil de performance (ex. 100 essais self-play par bot) :

 ```bash
 java --module-path bin -m ComplementIA/statistique.Performance 100
 ```


 ## Résultats produits

 Tous les fichiers générés sont écrits dans `Results/` :

 - `tournament_pairwise.csv` — matrice CSV des victoires (ligne = vainqueur, colonne = adversaire).
 - `tournament_summary.csv` — résumé par bot (games_played, wins, win_rate, standard_error, rank).
 - `tournament_pairwise_table.txt` — table lisible du pairwise + classement.
 - `performance_summary.csv` — résumé des essais self-play (bot, trials, mean_moves, std_error).
 - `performance_gaussian_overlay.png` — visualisation (PNG) des densités approchées par une gaussienne.

 Les CSV sont en UTF-8 et exploitables par des outils d'analyse (R, Python, Excel, etc.).


 ## Structure du projet

 - `src/` — sources Java organisées par package :
   - `bataillenavale/` : points d'entrée et classes UI (`Main`, `BatailleNavale`).
   - `logique/` : logique du jeu (`GrilleNavale`, `Navire`, `Coordonnee`).
   - `joueurs/` : abstractions et implémentations de joueurs (`Joueur`, `Bot`, `SmartBot`, ...).
   - `heuristic/` : interface heuristique et implémentations (`Heuristic`, `Uniforme`, `Markov`, `MonteCarlo`).
   - `interfacegraphique/` : composants Swing pour l'affichage des grilles.
   - `statistique/` : outils d'expérimentation (`Tournament`, `Performance`).
 - `bin/` — classes compilées (résultat de `javac -d bin`).
 - `Results/` — sorties des expérimentations.


 ## Contribuer

 - Forker le dépôt et créer une branche par fonctionnalité.
 - Respecter les conventions Java et ajouter des JavaDoc sur les classes publiques.
 - Pour des modifications significatives, ajouter un petit exemple ou un test
   démontrant le comportement attendu.
 - Ouvrir une pull request avec une description claire des changements.


 ## Licence

 Le dépôt peut être distribué sous une licence open-source au choix (par ex. MIT,
 Apache-2.0). Ajouter un fichier `LICENSE` approprié au besoin.


 ## Notes techniques

 - Le projet utilise le système de modules Java (fichier `module-info.java`).
 - Les identifiants et noms de packages sont principalement en français (par
   cohérence avec le code existant).
 - Les messages affichés à l'utilisateur via l'UI sont en français.


 ---

 Des scripts d'automatisation (`run_experiments.sh`) et un `CONTRIBUTING.md` plus
 détaillé peuvent être ajoutés ultérieurement si nécessaire.
