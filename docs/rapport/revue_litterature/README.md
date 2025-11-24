# Revue de littérature — Bataille navale

Organisation canonique pour la revue de littérature sur la Bataille navale.

- `main.tex` : document principal (compilable avec `pdflatex` + `bibtex`).
- `bib/revuerefs.bib` : bibliographie utilisée par le document.
- `figures/` : dossier prévu pour les figures et images.

Compilation rapide depuis la racine du dossier `revue_litterature` :

```sh
pdflatex main.tex
bibtex main
pdflatex main.tex
pdflatex main.tex
```

Vous pouvez ensuite fractionner `main.tex` en sous-fichiers `tex/` si vous souhaitez éditer la revue par sections.
