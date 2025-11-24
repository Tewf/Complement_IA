
Rapport LaTeX — Complement_IA

Instructions pour compiler le rapport :

1. Se placer dans le répertoire `rapport/` :

```bash
cd rapport
```

2. Compiler (Makefile) :

```bash
make
```

Ou manuellement :

```bash
pdflatex main.tex
bibtex main || true
pdflatex main.tex
pdflatex main.tex
```

Notes:

- Le document utilise `csvsimple` pour inclure des tables CSV. Installez une distribution TeX complète si nécessaire (TeX Live) avec les paquets requis (`csvsimple`, `graphicx`, `babel-french`, ...).
- Placez les fichiers de résultats et les figures dans `Results/` (par ex. `Results/*.csv`, `Results/plots/*.png`). Le document référence `../Results/` par rapport au dossier `rapport/`.

La revue de littérature relative à la bataille navale se trouve dans `rapport/revue_litterature/`.

