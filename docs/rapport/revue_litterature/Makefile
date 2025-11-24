TEX = pdflatex
BIB = bibtex

all: main.pdf

main.pdf: main.tex bib/revuerefs.bib
	$(TEX) main.tex
	$(BIB) main
	$(TEX) main.tex
	$(TEX) main.tex

clean:
	rm -f *.aux *.log *.bbl *.blg *.fdb_latexmk *.fls *.out *.toc *.synctex.gz
