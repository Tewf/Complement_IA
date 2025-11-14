# Projet Bataille Navale — Compléments statistiques

Ce dépôt contient un projet Java d'une implémentation de la Bataille Navale
avec plusieurs bots (aléatoire, heuristiques) et des utilitaires statistiques
que j'ai ajoutés pour automatiser des expérimentations et produire des
résultats reproductibles.

Ce README (en français) explique la structure du projet, comment compiler et
lancer les outils `Tournament` et `Performance`, où trouver les résultats et
comment interpréter les statistiques produites.

---

**Table des matières**

- Présentation rapide
- Structure du projet
- Compilation et exécution
- Outils statistiques ajoutés
  - `Tournament` (tournoi bots vs bots)
  - `Performance` (self-play, distribution des `moves` gagnants)
- Format des fichiers de sortie (`Results/`)
- Explications statistiques (moyenne, écart-type, erreur standard)
- `bin` / `bin_backup_*` / `bin2` — notes pratiques
- Conseils pour de grosses expérimentations
- Prochaines améliorations possibles

---

**Présentation rapide**

Le code de jeu est dans `src/` et contient la logique de la grille, les
implémentations de bots et une interface graphique. J'ai ajouté un package
`src/statistique` qui fournit deux utilitaires pour lancer des expériences en
mode headless (automatique) et collecter des statistiques.

**Structure importante (extraits)**

- `src/` : sources Java
  - `bataillenavale/` : classes d'amorçage (UI, `Main`, etc.)
  - `logique/` : `GrilleNavale`, `Navire`, `Coordonnee`
  - `joueurs/` : `Joueur`, `Bot`, `SmartBot`, `MatchResult`
  - `heuristic/` : heuristiques (Markov, MonteCarlo, Uniform)
  - `statistique/` : `Tournament.java`, `Performance.java` (ajouts)
- `bin/` : répertoire des `.class` compilés (utilisé pour l'exécution)
- `Results/` : répertoire de sortie pour CSV / PNG / tables produit par les outils

Description détaillée des dossiers et sous-dossiers

Voici une description plus complète de chaque dossier visible dans le dépôt, pour vous repérer rapidement :

- `src/` : code source Java. Contient tout le projet organisé par packages.
  - `bataillenavale/` : classes d'entrée et d'interface utilisateur.
    - `Main.java` : lanceur graphique (fenêtre permettant de configurer et démarrer une partie).
    - `BatailleNavale.java` : utilitaires d'initialisation (création de joueurs, bots, placement auto, helpers graphiques).
    - `TestBatailleNavale.java` : classes de test/sandbox éventuelles.

  - `logique/` : logique pure du jeu (indépendante de l'UI).
    - `GrilleNavale.java` : représentation de la grille, placement automatique des navires, réception des tirs.
    - `Navire.java` : représentation d'un navire, méthodes de collision / coulé / réception de tir.
    - `Coordonnee.java` : structure simple (ligne, colonne) utilisée dans tout le code.

  - `interfacegraphique/` : couche graphique légère.
    - `GrilleGraphique.java` : composant Swing pour afficher une grille et capter les clics.
    - `GrilleNavaleGraphique.java` : sous-classe de `GrilleNavale` qui met à jour l'affichage lors des événements (tirs, ajouts de navires).

  - `joueurs/` : implémentations des joueurs (humain et bots).
    - `Joueur.java` : abstraction commune (méthodes `choisirAttaque`, `defendre`, `jouerAvec`).
    - `JoueurGraphique.java` : joueur humain basé sur l'UI (boîtes de dialogue, clics).
    - `Bot.java` : bot aléatoire simple.
    - `SmartBot.java` : bot avancé (hunt/target + heuristique probabiliste) ; peut utiliser `heuristic/*`.
    - `MatchResult.java` : petit conteneur contenant le gagnant et le nombre de tirs (moves) — utilisé par les outils statistiques.

  - `heuristic/` : heuristiques utilisées par `SmartBot`.
    - `Heuristic.java` : interface de la heuristique.
    - `Uniform.java`, `Markov.java`, `MonteCarlo.java` : implémentations différentes de la stratégie probabiliste.

  - `statistique/` : outils d'expérimentation expérimentaux ajoutés.
    - `Tournament.java` : lance un tournoi round-robin (bots vs bots), écrit `Results/tournament_*`.
    - `Performance.java` : lance des self-plays pour chaque bot, calcule moyenne/erreur standard et trace un overlay Gaussian, écrit `Results/performance_*`.

- `bin/` : répertoire de classes compilées (`.class`) prêt pour l'exécution via la JVM et le module system.
  - Attention : lors des manipulations j'ai parfois créé un `bin_backup_<timestamp>` pour sauvegarder l'état précédent de `bin` avant de le remplacer.

- `bin2/` : (optionnel, temporaire) dossier que j'ai parfois utilisé pour compiler sans toucher `bin`. Vous pouvez le supprimer si inutile.

- `Results/` : dossier de sortie centralisé (créé automatiquement par les outils statistiques).
  - `tournament_pairwise.csv` : matrice CSV des victoires (ligne=gagnant, colonne=opposant).
  - `tournament_summary.csv` : résumé par bot (games_played,wins,win_rate,standard_error,rank).
  - `tournament_pairwise_table.txt` : table lisible + classement.
  - `performance_summary.csv` : résumé des runs self-play (mean_moves, std_error).
  - `performance_gaussian_overlay.png` : figure PNG des Gaussiennes superposées.
  - `README.md` : petit fichier d'aide dans `Results/` décrivant ces fichiers.

- `README.md` (à la racine) : ce fichier, contenant instructions et explications générales.

Autres fichiers utiles

- `module-info.java` : déclaration du module Java (nom `ComplementIA`) — utile pour l'exécution modulaire.

Si vous voulez, je peux aussi ajouter un script `run_experiments.sh` pour lancer automatiquement une série d'expériences et stocker chaque exécution dans `Results/YYYY-MM-DD_HH-MM-SS/`.


Compilation et exécution

Recommandation : compiler dans un dossier propre `bin` (ou utiliser `bin2` si
vous préférez garder un backup). Exemple :

```bash
# compile tous les fichiers .java dans bin
javac -d bin $(find src -name "*.java")

# exécuter Tournament : premier arg = N jeux par pairing (optionnel)
java --module-path bin -m ComplementIA/statistique.Tournament 1000

# exécuter Performance : premier arg = N essais self-play par bot (optionnel)
java --module-path bin -m ComplementIA/statistique.Performance 100
```

Remarque : le projet contient un `module-info.java`. La compilation ci‑dessus
produit un « module explosé » dans `bin` et j'exécute ensuite avec
`--module-path bin -m ComplementIA/...`.


Outils statistiques ajoutés

1) Tournament
- Localisation : `src/statistique/Tournament.java`
- But : jouer tous les bots (ensemble défini dans la classe) en mode round‑robin
  (y compris self-play), N parties par pairing. Produire un tableau pairwise
  (qui gagne contre qui) et un résumé avec classement.
- Sorties :
  - `Results/tournament_pairwise.csv` — matrice CSV (ligne = gagnant, colonne = adversaire)
  - `Results/tournament_summary.csv` — résumé par bot : jeux joués, victoires, taux de victoire, erreur standard, rang
  - `Results/tournament_pairwise_table.txt` — version lisible (table + classement)

Utilisation rapide :
```bash
# 1000 parties par pairing
java --module-path bin -m ComplementIA/statistique.Tournament 1000
```

2) Performance
- Localisation : `src/statistique/Performance.java`
- But : pour chaque bot, lancer N parties self-play (bot vs le même type),
  collecter le nombre d'attaques effectuées par le gagnant ("moves"), calculer
  la moyenne et l'erreur standard de la moyenne, et tracer une courbe Gaussienne
  estimée (fit normal) superposée pour chaque bot.
- Sorties :
  - `Results/performance_summary.csv` — par bot : `bot,trials,mean_moves,std_error`
  - `Results/performance_gaussian_overlay.png` — PNG montrant les densités normales

Utilisation rapide :
```bash
# 100 essais self-play par bot
java --module-path bin -m ComplementIA/statistique.Performance 100
```


Format des fichiers de sortie (exemples)

- `tournament_pairwise.csv` :
  - En-tête : `bot,<bot1>,<bot2>,...`
  - Chaque cellule (i,j) = nombre de fois où `bot_i` a battu `bot_j`.

- `tournament_summary.csv` : colonnes
  - `bot` : nom du bot
  - `games_played` : total de parties jouées (par bot)
  - `wins` : nombre total de victoires
  - `win_rate` : proportion de victoires (wins / games_played)
  - `standard_error` : erreur standard associée au taux (approx. binomial)
  - `rank` : classement par taux de victoire décroissant

- `performance_summary.csv` : colonnes
  - `bot` : nom
  - `trials` : nombre d'essais
  - `mean_moves` : moyenne des `moves` observés (attaques par le gagnant)
  - `std_error` : erreur standard de la moyenne (sd / sqrt(n))


Explication des calculs statistiques

- Pour le tournoi (taux de victoire) j'utilise l'approximation binomiale :
  `stderr = sqrt(p * (1-p) / n)` où `p` est la proportion observée et `n`
  le nombre de parties (par bot).

- Pour la performance (mesure continue : nombre de moves) :
  - Moyenne : `mean = sum(x_i) / n`
  - Écart-type (échantillon) : `sd = sqrt( sum((x_i - mean)^2) / (n - 1) )`
  - Erreur standard de la moyenne : `stderr = sd / sqrt(n)`

Ces estimations sont les plus simples. Si vous voulez des intervalles de
confiance plus robustes (bootstrap, t-stats), je peux les ajouter.


`bin`, `bin_backup_*` et `bin2` — que signifient-ils ?

- `bin` : répertoire où sont placés les `.class` compilés (utilisé par
  l'exécution via `--module-path bin`). C'est l'endroit « canonique » pour
  lancer le projet.
- `bin_backup_<timestamp>` : copie de sauvegarde automatique que j'ai créée
  avant d'écraser `bin`, au cas où vous voudriez restaurer l'ancienne
  compilation. Vous pouvez supprimer ces backups si vous n'en avez plus
  besoin.
- `bin2` : un dossier temporaire que j'ai utilisé plus tôt pour éviter de
  toucher `bin`. Vous pouvez l'effacer si vous n'en avez plus besoin.

Commandes utiles :
```bash
# lister les backups (s'ils existent)
ls -ld bin_backup_*

# supprimer les backups (irréversible)
rm -rf bin_backup_*
```


Conseils pour grosses expérimentations

- Temps : jouer 1000 parties par pairing pour 4 bots (16,000 parties) peut
  prendre plusieurs minutes selon votre machine. Lancez cela en tâche de
  fond si nécessaire.
- CPU : ces expériences sont CPU-bound ; vous pouvez paralléliser les
  répétitions en modifiant le code pour exécuter des threads si besoin.
- Répétabilité : si vous voulez résultats reproduisibles, il faut
  contrôler les seeds `Random` (actuellement certains RNG sont internes aux
  classes). Dites-moi si vous voulez une option `--seed` pour forcer les
  mêmes placements/choix aléatoires.


Prochaines améliorations possibles

- Ajouter un paramètre CLI pour sélectionner la liste de bots à tester.
- Produire des intervalles de confiance bootstrap (pour `Performance`).
- Plus d'analyses : matrice de confusion détaillée, temps moyen par partie,
  histogrammes bruts en plus des fits Gaussiens.
- Sauvegarder chaque exécution dans `Results/YYYY-MM-DD_HH-MM-SS/` pour
  éviter les écrasements automatiques.


Questions / Actions que je peux faire pour vous

- Lancer une expérience complète (par ex. `Tournament 1000` et `Performance 100`) et
  envoyer ici les CSV et PNG générés.
- Ajouter des options CLI (`--bots`, `--seed`, `--outdir`, `--timestamped`).
- Ajouter bootstrap CI pour la `Performance` (si vous voulez des IC robustes).


Bonne exploration — dites-moi ce que vous voulez exécuter ensuite
(je peux lancer les expériences pour vous ou modifier le code selon vos
préférences).