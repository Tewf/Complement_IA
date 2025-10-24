package joueurs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import interfacegraphique.GrilleNavaleGraphique;
import logique.Coordonnee;

/**
 * SmartBot : bot combinant deux approches :
 * - Hunt & Target : si on touche, on explore U, D, L, R pour confirmer l'axe,
 *   puis on tire en ligne droite jusqu’à COULE ; si A_L_EAU, on repart
 *   dans le sens opposé.
 * - Probabilité (heatmap) : quand pas de cible prioritaire, on calcule une
 *   carte de placements restants et on tire sur la case la plus probable.
 *
 * Règle proba demandée : invalider toute case déjà tirée (à l’eau ou partie d’un navire coulé).
 */
public class SmartBot extends Bot {
    private final Deque<Coordonnee> cibles = new ArrayDeque<>();
    private final Random rng = new Random();

    // Données pour la stratégie de probabilité
    private final int N;
    private final boolean[][] toucheNonCoule; // touches connues mais pas encore coulées
    private final List<Integer> naviresRestants;

    // État pour le mode 'target' (hunt -> target)
    private Coordonnee targetStart = null; // borne min connue (ligne/colonne)
    private Coordonnee targetEnd = null;
    private Coordonnee previous = null;
    private Coordonnee direction = null; // direction confirmée (dr,dc)
    

    // flags et variables auxiliaires pour la stratégie hunt
    private int dirR = 0, dirC = 0;
    private boolean preferForward = true, triedForward = false, triedBackward = false;

    // liste des touches en cours (cluster non encore identifié comme coulé)
    private final List<Coordonnee> currentHits = new ArrayList<>();

    /** Constructeur avec flotte par défaut. */
    public SmartBot(GrilleNavaleGraphique gng) {
        this(gng, List.of(5, 4, 3, 3, 2, 2));
    }

    /** Constructeur principal : on fournit les longueurs des navires. */
    public SmartBot(GrilleNavaleGraphique gng, List<Integer> longueursInitiales) {
        super(gng);
        this.N = gng.getTaille();
        this.toucheNonCoule = new boolean[N][N];
        this.naviresRestants = new ArrayList<>(longueursInitiales);
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
            // largeur removed: use computeCurrentClusterLength() on demand
            updateTargetBoundsAndDirectionFromHits();

        } else if (etat == COULE) {
            // marquer le cluster comme coulé
            currentHits.add(c);
            int len = computeCurrentClusterLength();
            removeShipLength(len);
            // purge du cluster
            clearClusterAround(c);
            clearCurrentTargetState();

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

        // 2) Sinon : heatmap (invalide toute case déjà tirée)
        int[][] heat = computeProbabilityMatrix();
        int best = -1;
        List<Coordonnee> candidates = new ArrayList<>();
        for (int rr = 0; rr < N; rr++) {
            for (int cc = 0; cc < N; cc++) {
                if (!tirsEnvoyes[rr][cc]) {
                    int v = heat[rr][cc];
                    if (v > best) {
                        best = v;
                        candidates.clear();
                        candidates.add(new Coordonnee(rr, cc));
                    } else if (v == best) {
                        candidates.add(new Coordonnee(rr, cc));
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            for (int rr = 0; rr < N; rr++)
                for (int cc = 0; cc < N; cc++)
                    if (!tirsEnvoyes[rr][cc])
                        candidates.add(new Coordonnee(rr, cc));
        }
        Coordonnee choix = candidates.get(rng.nextInt(candidates.size()));
        tirsEnvoyes[choix.getLigne()][choix.getColonne()] = true;
        return choix;
    }

    /* ===================== HUNT ===================== */
    private Coordonnee hunt() {
        // Nothing to hunt
        if (currentHits.isEmpty())
            return null;

        // Ensure bounds and axis are up-to-date
        updateTargetBoundsAndDirectionFromHits();

        // If axis is confirmed, try forward then backward along the axis
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

            // If both sides blocked, try any unfired neighbor around the cluster
            Coordonnee around = findUnfiredNeighborAroundCluster();
            if (around != null) {
                tirsEnvoyes[around.getLigne()][around.getColonne()] = true;
                return around;
            }

            // Nothing usable — abandon this target
            clearCurrentTargetState();
            cibles.clear();
            return null;
        }

        // No confirmed axis: consume queued U/D/L/R neighbors first
        while (!cibles.isEmpty()) {
            Coordonnee next = cibles.removeFirst();
            if (isValidUnfired(next)) {
                tirsEnvoyes[next.getLigne()][next.getColonne()] = true;
                return next;
            }
        }

        // If queue is empty, populate it with neighbors around the cluster (UDLR for each hit)
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

        // Nothing to do — clear state and let probability layer take over
        clearCurrentTargetState();
        cibles.clear();
        return null;
    }

    /* ===================== Heatmap (proba) ===================== */
    private int[][] computeProbabilityMatrix() {
        int[][] sum = new int[N][N];
        if (naviresRestants.isEmpty()) return sum;

        // 1) Base heat: sum placements for each remaining ship length
        for (int L : naviresRestants) {
            int[][] h = heatmapForShipLength(L);
            add(sum, h);
        }

        // 2) Parity optimization: prefer cells congruent modulo smallest ship length
        int minLen = Integer.MAX_VALUE;
        for (int L : naviresRestants) if (L < minLen) minLen = L;
        if (minLen > 1) {
            // Apply a soft preference: boost matching parity, reduce others
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    if (sum[r][c] == 0) continue; // keep zeros zero
                    if (((r + c) % minLen) == 0) {
                        sum[r][c] = sum[r][c] * 3 / 2 + 1; // modest boost
                    } else {
                        sum[r][c] = sum[r][c] / 2; // de-prioritize
                    }
                }
            }
        }

        // 3) If we have active hits, compute a constrained heatmap (placements covering hits)
        //    and give those cells additional weight so we prioritize finishing targets.
        if (!currentHits.isEmpty()) {
            int[][] constrained = new int[N][N];
            for (int L : naviresRestants) {
                int[][] h = heatmapForShipLengthConstrained(L);
                add(constrained, h);
            }
            // dynamic boost factor: larger when smallest ship is larger
            int boost = Math.max(3, Math.min(8, minLen * 2));
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    sum[r][c] += constrained[r][c] * boost;
                }
            }
        }

        return sum;
    }

    // heatmap variant that only counts placements which include at least one currentHit
    private int[][] heatmapForShipLengthConstrained(int L) {
        int[][] map = new int[N][N];
        if (currentHits.isEmpty()) return map;
        // placements horizontaux
        for (int rr = 0; rr < N; rr++) {
            for (int cc = 0; cc + L - 1 < N; cc++) {
                if (placementValide(rr, cc, L, false) && placementCoversAnyHit(rr, cc, L, false))
                    incrSegment(map, rr, cc, L, false);
            }
        }
        // placements verticaux
        for (int rr = 0; rr + L - 1 < N; rr++) {
            for (int cc = 0; cc < N; cc++) {
                if (placementValide(rr, cc, L, true) && placementCoversAnyHit(rr, cc, L, true))
                    incrSegment(map, rr, cc, L, true);
            }
        }
        return map;
    }

    private boolean placementCoversAnyHit(int r, int c, int L, boolean vertical) {
        for (Coordonnee h : currentHits) {
            int hr = h.getLigne(), hc = h.getColonne();
            for (int k = 0; k < L; k++) {
                int rr = vertical ? r + k : r;
                int cc = vertical ? c : c + k;
                if (rr == hr && cc == hc) return true;
            }
        }
        return false;
    }

    private int[][] heatmapForShipLength(int L) {
        int[][] map = new int[N][N];
        // placements horizontaux
        for (int rr = 0; rr < N; rr++) {
            for (int cc = 0; cc + L - 1 < N; cc++) {
                if (placementValide(rr, cc, L, false))
                    incrSegment(map, rr, cc, L, false);
            }
        }
        // placements verticaux
        for (int rr = 0; rr + L - 1 < N; rr++) {
            for (int cc = 0; cc < N; cc++) {
                if (placementValide(rr, cc, L, true))
                    incrSegment(map, rr, cc, L, true);
            }
        }
        return map;
    }

    // Règle proba demandée : invalider toute case déjà tirée (à l’eau OU segment coulé)
    private boolean placementValide(int r, int c, int L, boolean vertical) {
        for (int k = 0; k < L; k++) {
            int rr = vertical ? r + k : r;
            int cc = vertical ? c : c + k;
            if (!in(rr, cc))
                return false;
            if (tirsEnvoyes[rr][cc])
                return false; // invalide toute case tirée
        }
        return true;
    }

    private void incrSegment(int[][] map, int r, int c, int L, boolean vertical) {
        for (int k = 0; k < L; k++) {
            int rr = vertical ? r + k : r;
            int cc = vertical ? c : c + k;
            if (!tirsEnvoyes[rr][cc])
                map[rr][cc]++;
        }
    }

    private void add(int[][] a, int[][] b) {
        for (int rr = 0; rr < N; rr++)
            for (int cc = 0; cc < N; cc++)
                a[rr][cc] += b[rr][cc];
    }

    /* ===================== Utilitaires communs ===================== */
    private boolean in(int r, int c) {
        return r >= 0 && r < N && c >= 0 && c < N;
    }

    // U, D, L, R en file (sans doublon / déjà tiré)
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

    /* ===================== Aide direction ===================== */
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

    // small Coordonnee arithmetic helpers
   

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
