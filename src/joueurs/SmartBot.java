package joueurs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import heuristic.Heuristic;
import heuristic.Markov;
import heuristic.MonteCarlo;
import heuristic.Uniform;

import interfacegraphique.GrilleNavaleGraphique;
import logique.Coordonnee;

/**
 * SmartBot : bot combinant deux approches principales :
 *
 * <ul>
 * <li>Hunt &amp; Target : lorsqu'un hit est détecté, explorer Haut/Bas/Gauche/Droite
 *     pour confirmer l'axe, puis tirer le long de l'axe confirmé jusqu'à ce
 *     que le navire soit coulé ; en cas de miss, tester la direction opposée.</li>
 * <li>Probabilités (heatmap) : quand il n'y a pas de cible active, estimer
 *     les placements restants et tirer sur la case la plus probable.</li>
 * </ul>
 *
 * La heuristique probabiliste exclut toute case déjà tirée (miss ou case
 * faisant partie d'un navire coulé).
 */
public class SmartBot extends Bot {
    private final Deque<Coordonnee> cibles = new ArrayDeque<>();
    private Heuristic heuristic;

    // Données pour la stratégie probabiliste
    private final int N;
    private final boolean[][] toucheNonCoule; // touches connues mais pas encore marquées comme coulées
    private final List<Integer> naviresRestants;

    // État pour le mode 'target' (hunt -> target)
    private Coordonnee targetStart = null; // borne min connue (ligne/colonne)
    private Coordonnee targetEnd = null;
    private Coordonnee previous = null;
    private Coordonnee direction = null; // direction confirmée (dr, dc)
    

    // flags et variables auxiliaires pour la stratégie hunt
    private int dirR = 0, dirC = 0;
    private boolean preferForward = true, triedForward = false, triedBackward = false;

    // liste des touches en cours (cluster non encore identifié comme coulé)
    private final List<Coordonnee> currentHits = new ArrayList<>();

    /** Constructeur avec flotte par défaut. */
    public SmartBot(GrilleNavaleGraphique gng) {
        this(gng, List.of(5, 4, 3, 3, 2, 2));
    }

    /**
     * Constructeur avec nom d'heuristique (optionnel).
     */
    public SmartBot(GrilleNavaleGraphique gng, String heuristicName) {
        this(gng);
        initHeuristic(heuristicName);
    }

    /** Constructeur principal : fournir les longueurs des navires. */
    public SmartBot(GrilleNavaleGraphique gng, List<Integer> longueursInitiales) {
        super(gng);
        this.N = gng.getTaille();
        this.toucheNonCoule = new boolean[N][N];
        this.naviresRestants = new ArrayList<>(longueursInitiales);
        initHeuristic(null);
    }

    /**
     * Constructeur avec longueurs explicites et nom d'heuristique.
     */
    public SmartBot(GrilleNavaleGraphique gng, List<Integer> longueursInitiales, String heuristicName) {
        this(gng, longueursInitiales);
        initHeuristic(heuristicName);
    }

    private void initHeuristic(String name) {
        if (name == null) {
            this.heuristic = new Uniform();
            return;
        }
        String n = name.trim().toLowerCase();
        switch (n) {
            case "montecarlo":
            case "monte-carlo":
                this.heuristic = new MonteCarlo();
                break;
            case "markov":
            case "markow":
                this.heuristic = new Markov();
                break;
            case "uniform":
            default:
                this.heuristic = new Uniform();
                break;
        }
    }

    /* ===================== API principale ===================== */
    @Override
    protected void retourAttaque(Coordonnee c, int etat) {
        // gestion des retours après avoir attaqué
        if (etat == TOUCHE) {
            currentHits.add(c);
            if (targetStart == null) {
                targetStart = c;
                previous = c;
            } else {
                // mettre à jour previous et direction tentative
                if (direction == null && previous != null) {
                    direction = sub(c, previous);
                }
                previous = c;
            }
            // largeur retirée : utiliser computeCurrentClusterLength() à la demande
            updateTargetBoundsAndDirectionFromHits();

            // Si un axe/direction est connu, préférer tirer le long de cet axe.
            // Sinon, empiler Haut/Bas/Gauche/Droite pour découvrir l'axe.
            if (dirR != 0 || dirC != 0) {
                // définir preferForward selon la direction tentative si disponible
                if (direction != null) {
                    int signR = Integer.signum(direction.getLigne());
                    int signC = Integer.signum(direction.getColonne());
                    if (dirR != 0) preferForward = (signR == dirR);
                    else if (dirC != 0) preferForward = (signC == dirC);
                }
                ajouterVoisinsSelonAxe(c);
            } else {
                ajouterVoisinsOrdreUDLR(c);
            }

        } else if (etat == COULE) {
            // marquer le cluster comme coulé
            currentHits.add(c);
            int len = computeCurrentClusterLength();
            removeShipLength(len);
            // purge du cluster
            clearClusterAround(c);
            // vider l'état de cible et la file de voisins pour revenir au mode heuristique
            clearCurrentTargetState();
            cibles.clear();

        } else if (etat == A_L_EAU) {
            // tir à l'eau
            if (direction != null) {
                direction = neg(direction);
                previous = targetStart;
            }
            // sinon rien de spécial

        } else if (etat == GAMEOVER) {
            cibles.clear();
            naviresRestants.clear();
            clearCurrentTargetState();
        }
    }

    @Override
    protected void retourDefense(Coordonnee c, int etat) {
        // Non utilisé par ce bot
    }

    @Override
    public Coordonnee choisirAttaque() {
        // Delegate hunting logic to hunt(); if it returns a target, use it
        Coordonnee huntTarget = hunt();
        if (huntTarget != null) return huntTarget;

        // 2) Sinon : utiliser l'heuristique configurée. Si elle est absente ou
        // ne retourne rien, revenir à un choix uniforme.
        Coordonnee choix = null;
        if (heuristic != null) {
            choix = heuristic.choisir(tirsEnvoyes, gng, naviresRestants, currentHits);
        }
        if (choix == null) {
            choix = new Uniform().choisir(tirsEnvoyes, gng, naviresRestants, currentHits);
        }
        if (choix != null) tirsEnvoyes[choix.getLigne()][choix.getColonne()] = true;
        return choix;
    }

    /* ===================== HUNT ===================== */
    private Coordonnee hunt() {
        // Aucune cible en cours
        if (currentHits.isEmpty())
            return null;

        // Mettre à jour bornes et axe à partir des touches connues
        updateTargetBoundsAndDirectionFromHits();

        // Si l'axe est confirmé, tenter d'abord l'avant puis l'arrière le long de l'axe
        if ((dirR != 0 || dirC != 0) && targetStart != null && targetEnd != null) {
            if (preferForward && !triedForward) {
                Coordonnee f = forwardCandidate();
                if (isValidUnfired(f)) {
                    tirsEnvoyes[f.getLigne()][f.getColonne()] = true;
                    return f;
                }
                triedForward = true;
                preferForward = false;
            }

            if (!preferForward && !triedBackward) {
                Coordonnee b = backwardCandidate();
                if (isValidUnfired(b)) {
                    tirsEnvoyes[b.getLigne()][b.getColonne()] = true;
                    return b;
                }
                triedBackward = true;
                preferForward = true;
            }

            // Si les deux côtés sont bloqués, essayer un voisin non tiré autour du cluster
            Coordonnee around = findUnfiredNeighborAroundCluster();
            if (around != null) {
                tirsEnvoyes[around.getLigne()][around.getColonne()] = true;
                return around;
            }

            // Rien d'utilisable — abandonner cette cible
            clearCurrentTargetState();
            cibles.clear();
            return null;
        }

        // Axe non confirmé : consommer d'abord la file Haut/Bas/Gauche/Droite
        while (!cibles.isEmpty()) {
            Coordonnee next = cibles.removeFirst();
            if (isValidUnfired(next)) {
                tirsEnvoyes[next.getLigne()][next.getColonne()] = true;
                return next;
            }
        }

        // Si la file est vide, la remplir avec les voisins autour du cluster (H/B/G/D pour chaque hit)
        for (Coordonnee h : currentHits) {
            ajouterVoisinsOrdreUDLR(h);
        }

        while (!cibles.isEmpty()) {
            Coordonnee next = cibles.removeFirst();
            if (isValidUnfired(next)) {
                tirsEnvoyes[next.getLigne()][next.getColonne()] = true;
                return next;
            }
        }

        // Rien à faire — vider l'état et laisser la couche probabiliste gérer
        clearCurrentTargetState();
        cibles.clear();
        return null;
    }

    /* La heatmap interne a été retirée : SmartBot délègue les choix hors-hunt à une Heuristic. */

    /* ===================== Utilitaires communs ===================== */
    private boolean in(int r, int c) {
        return r >= 0 && r < N && c >= 0 && c < N;
    }

    // Enfile Haut, Bas, Gauche, Droite (sans doublons / ignorer cases déjà tirées)
    private void ajouterVoisinsOrdreUDLR(Coordonnee c) {
        int r = c.getLigne(), col = c.getColonne();
        Coordonnee up = new Coordonnee(r - 1, col);
        Coordonnee down = new Coordonnee(r + 1, col);
        Coordonnee left = new Coordonnee(r, col - 1);
        Coordonnee right = new Coordonnee(r, col + 1);
        if (isValidUnfired(up))
            addUniqueCibleLast(up);
        if (isValidUnfired(down))
            addUniqueCibleLast(down);
        if (isValidUnfired(left))
            addUniqueCibleLast(left);
        if (isValidUnfired(right))
            addUniqueCibleLast(right);
    }

    /**
     * Enfile uniquement les voisins le long de l'axe confirmé (pas d'essais perpendiculaires).
     */
    private void ajouterVoisinsSelonAxe(Coordonnee c) {
        if (c == null) return;
        int r = c.getLigne(), col = c.getColonne();
        // vertical axis: add up/down
        if (dirR != 0) {
            Coordonnee up = new Coordonnee(r - 1, col);
            Coordonnee down = new Coordonnee(r + 1, col);
            if (isValidUnfired(up)) addUniqueCibleLast(up);
            if (isValidUnfired(down)) addUniqueCibleLast(down);
            return;
        }
        // horizontal axis: add left/right
        if (dirC != 0) {
            Coordonnee left = new Coordonnee(r, col - 1);
            Coordonnee right = new Coordonnee(r, col + 1);
            if (isValidUnfired(left)) addUniqueCibleLast(left);
            if (isValidUnfired(right)) addUniqueCibleLast(right);
            return;
        }
    }

    private Coordonnee findUnfiredNeighborAroundCluster() {
        int[] dr = { -1, 1, 0, 0 };
        int[] dc = { 0, 0, -1, 1 };
        for (Coordonnee h : currentHits) {
            int r = h.getLigne(), c = h.getColonne();
            for (int i = 0; i < 4; i++) {
                int rr = r + dr[i], cc = c + dc[i];
                if (in(rr, cc) && !tirsEnvoyes[rr][cc])
                    return new Coordonnee(rr, cc);
            }
        }
        return null;
    }


    private void addUniqueCibleLast(Coordonnee c) {
        if (c != null && !containsCoord(cibles, c))
            cibles.addLast(c);
    }

    private boolean containsCoord(Iterable<Coordonnee> coll, Coordonnee c) {
        for (Coordonnee e : coll) {
            if (e.getLigne() == c.getLigne() && e.getColonne() == c.getColonne())
                return true;
        }
        return false;
    }

   

    /** Met à jour targetStart/targetEnd et la direction à partir des touches connues. */
    private void updateTargetBoundsAndDirectionFromHits() {
        if (currentHits.isEmpty())
            return;
        int minR = Integer.MAX_VALUE, maxR = Integer.MIN_VALUE;
        int minC = Integer.MAX_VALUE, maxC = Integer.MIN_VALUE;
        for (Coordonnee h : currentHits) {
            minR = Math.min(minR, h.getLigne());
            maxR = Math.max(maxR, h.getLigne());
            minC = Math.min(minC, h.getColonne());
            maxC = Math.max(maxC, h.getColonne());
        }
        targetStart = new Coordonnee(minR, minC);
        targetEnd = new Coordonnee(maxR, maxC);
        if (minR == maxR && minC == maxC) {
            dirR = 0;
            dirC = 0; // un seul hit → axe inconnu
        } else if (minR == maxR) {
            dirR = 0;
            dirC = (maxC > minC) ? 1 : -1; // aligné horizontalement
        } else if (minC == maxC) {
            dirC = 0;
            dirR = (maxR > minR) ? 1 : -1; // aligné verticalement
        } else {
            dirR = 0;
            dirC = 0; // dispersé → axe non confirmé
        }
    }

    private int computeCurrentClusterLength() {
        if (targetStart == null || targetEnd == null)
            return currentHits.size();
        if (targetStart.getLigne() == targetEnd.getLigne()) {
            return Math.abs(targetEnd.getColonne() - targetStart.getColonne()) + 1;
        } else if (targetStart.getColonne() == targetEnd.getColonne()) {
            return Math.abs(targetEnd.getLigne() - targetStart.getLigne()) + 1;
        }
        return currentHits.size();
    }

    private void removeShipLength(int len) {
        Integer toRemove = Integer.valueOf(len);
        if (naviresRestants.remove(toRemove))
            return;
        int bestIdx = -1, bestDiff = Integer.MAX_VALUE;
        for (int i = 0; i < naviresRestants.size(); i++) {
            int d = Math.abs(naviresRestants.get(i) - len);
            if (d < bestDiff) {
                bestDiff = d;
                bestIdx = i;
            }
        }
        if (bestIdx >= 0)
            naviresRestants.remove(bestIdx);
    }

    private void clearCurrentTargetState() {
        currentHits.clear();
        targetStart = null;
        targetEnd = null;
        dirR = 0;
        dirC = 0;
        preferForward = true;
        triedForward = false;
        triedBackward = false;
        previous = null;
        direction = null;
        // largeur removed
    }

    /** Purge le cluster connecté de touches autour d'une case coulée. */
    private void clearClusterAround(Coordonnee c) {
        int sr = c.getLigne(), sc = c.getColonne();
        if (!toucheNonCoule[sr][sc])
            toucheNonCoule[sr][sc] = true;
        ArrayDeque<int[]> dq = new ArrayDeque<>();
        dq.add(new int[] { sr, sc });
        int[] dr = { 1, -1, 0, 0 };
        int[] dc = { 0, 0, 1, -1 };
        while (!dq.isEmpty()) {
            int[] cur = dq.poll();
            int rr = cur[0], cc = cur[1];
            toucheNonCoule[rr][cc] = false; // résolu
            for (int d = 0; d < 4; d++) {
                int nr = rr + dr[d], nc = cc + dc[d];
                if (in(nr, nc) && toucheNonCoule[nr][nc])
                    dq.add(new int[] { nr, nc });
            }
        }
    }

    /* ===================== Direction helpers ===================== */
    private Coordonnee forwardCandidate() {
        if (targetEnd == null)
            return null;
        return new Coordonnee(targetEnd.getLigne() + dirR, targetEnd.getColonne() + dirC);
    }

    private Coordonnee backwardCandidate() {
        if (targetStart == null)
            return null;
        return new Coordonnee(targetStart.getLigne() - dirR, targetStart.getColonne() - dirC);
    }

    private boolean isValidUnfired(Coordonnee c) {
        return c != null && in(c.getLigne(), c.getColonne()) && !tirsEnvoyes[c.getLigne()][c.getColonne()];
    }

    // petites fonctions utilitaires pour l'arithmétique de Coordonnee
   

    private Coordonnee sub(Coordonnee a, Coordonnee b) {
        if (a == null || b == null)
            return null;
        return new Coordonnee(a.getLigne() - b.getLigne(), a.getColonne() - b.getColonne());
    }

    private Coordonnee neg(Coordonnee a) {
        if (a == null)
            return null;
        return new Coordonnee(-a.getLigne(), -a.getColonne());
    }
}
