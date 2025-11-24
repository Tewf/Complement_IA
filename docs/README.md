# Documentation

This `docs/` directory contains project-level documentation for Complement_IA.

Add guides, design notes, usage examples and any developer-facing information here. Generated API documentation is available under `docs/api/`.

## Building and previewing the documentation

Prerequisites:

- JDK 11+ (for `javac` and `javadoc`).
- `make`, `pdflatex` (optional, to build the LaTeX report in `rapport/`).
- `python3` (to serve the `docs/` folder locally).
- `xdg-open` or use your OS file manager to open generated HTML files.

1) Compile the Java sources (recommended before running `javadoc`):

```bash
# from project root
javac -d bin $(find src -name "*.java")
```

2) Generate the API reference with `javadoc`:

```bash
# generate API docs into docs/api
javadoc -d docs/api -sourcepath src -subpackages bataillenavale:heuristic:interfacegraphique:joueurs:logique:statistique
```

Notes:

- The `-subpackages` option lists the main packages; adjust if you add packages.
- If you prefer a single command that also compiles, ensure `javac` ran successfully first.

3) Build the LaTeX report (optional):

```bash
cd rapport
make
# output: rapport/main.pdf
```

If you don't have a full TeX distribution, install TeX Live or use an online service.

4) Preview the documentation locally (simple HTTP server):

```bash
# from project root
python3 -m http.server 8000 --directory docs
# then open http://localhost:8000/api/ or http://localhost:8000/ in your browser
```

Or open the generated API directly:

```bash
xdg-open docs/api/index.html
```

Platform differences:

- On Windows, replace `xdg-open` with `start` in cmd or use `explorer`.
- If your shell does not support `$(find ...)`, run the `find` command separately or compile from an IDE.
