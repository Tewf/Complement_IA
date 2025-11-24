PDFLATEX=pdflatex
BIBTEX=bibtex
MAIN=main

.PHONY: all pdf clean

all: pdf

pdf:
	$(PDFLATEX) $(MAIN).tex
	$(BIBTEX) $(MAIN) || true
	$(PDFLATEX) $(MAIN).tex
	$(PDFLATEX) $(MAIN).tex

clean:
	rm -f *.aux *.bbl *.blg *.log *.toc *.out $(MAIN).pdf
