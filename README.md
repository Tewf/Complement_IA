
# Battleship — Statistical Extensions

This repository contains a Java implementation of the Battleship game together
with tools to evaluate automated strategies (bots) and produce reproducible
experimental results. For a French-oriented presentation see `README_Fr.md` at
the project root (version française). The English version is provided in
`README.md`.

What this project provides

- A modular Java implementation of the game engine (grid, ships, shots).
- Several bot implementations for evaluation: Uniform, Markov, MonteCarlo, Smart.
- A simple Swing-based graphical interface for interactive play.
- Statistical tools to run tournaments and per-bot performance experiments,
  producing CSV summaries and PNG visualizations.

Requirements

- JDK 11 or newer.
- POSIX-compatible shell (examples use `bash`).

Build

```bash
# Compile sources into the `bin` directory
javac -d bin $(find src -name "*.java")
```

Run examples

- Start the GUI:

```bash
java --module-path bin -m ComplementIA/bataillenavale.Main
```

- Run a tournament (e.g. 1000 matches per pairing):

```bash
java --module-path bin -m ComplementIA/statistique.Tournament 1000
```

- Run the performance tool (e.g. 100 self-play trials per bot):

```bash
java --module-path bin -m ComplementIA/statistique.Performance 100
```

Outputs

All outputs are written to the `Results/` directory:

- `tournament_pairwise.csv` — pairwise win counts matrix.
- `tournament_summary.csv` — per-bot summary (games_played, wins, win_rate, std_error, rank).
- `tournament_pairwise_table.txt` — human-readable pairwise table and ranking.
- `performance_summary.csv` — self-play summary (mean moves, std_error, ...).
- `performance_gaussian_overlay.png` — PNG visualization of distribution overlays.

Project structure

- `src/` — Java source packages:
  - `bataillenavale/` — entry points and UI (`Main`, `BatailleNavale`).
  - `logique/` — core game logic (`GrilleNavale`, `Navire`, `Coordonnee`).
  - `joueurs/` — player abstractions and implementations (`Joueur`, `Bot`, `SmartBot`).
  - `heuristic/` — heuristic interface and implementations (`Heuristic`, `Uniform`, `Markov`, `MonteCarlo`).
  - `interfacegraphique/` — Swing components.
  - `statistique/` — experiment utilities (`Tournament`, `Performance`).
- `bin/` — compiled classes (result of `javac -d bin`).
- `Results/` — experiment outputs.

Contributing

Fork the repository and open a pull request. Please include JavaDoc for public
classes and a short example or test when behaviour changes.

License

See the `LICENSE` file at the repository root (MIT).

