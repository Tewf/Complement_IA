package heuristic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import interfacegraphique.GrilleNavaleGraphique;
import logique.Coordonnee;

/**
 * Markov / heatmap heuristic inspired by SmartBot.computeProbabilityMatrix().
 * It returns a coordinate with maximal score among unfired cells. When several
 * coordinates tie, one is chosen randomly.
 */
public class Markov implements Heuristic {
    private final Random rng = new Random();

    @Override
    public Coordonnee choisir(boolean[][] tirsEnvoyes, GrilleNavaleGraphique gng, List<Integer> naviresRestants,
            List<Coordonnee> currentHits) {
        int N = gng.getTaille();
        int[][] sum = computeProbabilityMatrix(N, tirsEnvoyes, naviresRestants, currentHits);

        int best = -1;
        List<Coordonnee> candidates = new ArrayList<>();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (!tirsEnvoyes[r][c]) {
                    int v = sum[r][c];
                    if (v > best) {
                        best = v;
                        candidates.clear();
                        candidates.add(new Coordonnee(r, c));
                    } else if (v == best) {
                        candidates.add(new Coordonnee(r, c));
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            for (int r = 0; r < N; r++)
                for (int c = 0; c < N; c++)
                    if (!tirsEnvoyes[r][c])
                        candidates.add(new Coordonnee(r, c));
        }
        if (candidates.isEmpty()) return null;
        return candidates.get(rng.nextInt(candidates.size()));
    }

    private int[][] computeProbabilityMatrix(int N, boolean[][] tirsEnvoyes, List<Integer> naviresRestants,
            List<Coordonnee> currentHits) {
        int[][] sum = new int[N][N];
        if (naviresRestants == null || naviresRestants.isEmpty()) return sum;

        for (int L : naviresRestants) {
            int[][] h = heatmapForShipLength(N, tirsEnvoyes, L);
            add(sum, h);
        }

        int minLen = Integer.MAX_VALUE;
        for (int L : naviresRestants) if (L < minLen) minLen = L;
        if (minLen > 1) {
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    if (sum[r][c] == 0) continue;
                    if (((r + c) % minLen) == 0) {
                        sum[r][c] = sum[r][c] * 3 / 2 + 1;
                    } else {
                        sum[r][c] = sum[r][c] / 2;
                    }
                }
            }
        }

        if (currentHits != null && !currentHits.isEmpty()) {
            int[][] constrained = new int[N][N];
            for (int L : naviresRestants) {
                int[][] h = heatmapForShipLengthConstrained(N, tirsEnvoyes, L, currentHits);
                add(constrained, h);
            }
            int boost = Math.max(3, Math.min(8, minLen * 2));
            for (int r = 0; r < N; r++)
                for (int c = 0; c < N; c++)
                    sum[r][c] += constrained[r][c] * boost;
        }

        return sum;
    }

    private int[][] heatmapForShipLengthConstrained(int N, boolean[][] tirsEnvoyes, int L,
            List<Coordonnee> currentHits) {
        int[][] map = new int[N][N];
        if (currentHits == null || currentHits.isEmpty()) return map;
        for (int rr = 0; rr < N; rr++) {
            for (int cc = 0; cc + L - 1 < N; cc++) {
                if (placementValide(rr, cc, L, false, N, tirsEnvoyes) && placementCoversAnyHit(rr, cc, L, false, currentHits))
                    incrSegment(map, rr, cc, L, false, N, tirsEnvoyes);
            }
        }
        for (int rr = 0; rr + L - 1 < N; rr++) {
            for (int cc = 0; cc < N; cc++) {
                if (placementValide(rr, cc, L, true, N, tirsEnvoyes) && placementCoversAnyHit(rr, cc, L, true, currentHits))
                    incrSegment(map, rr, cc, L, true, N, tirsEnvoyes);
            }
        }
        return map;
    }

    private boolean placementCoversAnyHit(int r, int c, int L, boolean vertical, List<Coordonnee> currentHits) {
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

    private int[][] heatmapForShipLength(int N, boolean[][] tirsEnvoyes, int L) {
        int[][] map = new int[N][N];
        for (int rr = 0; rr < N; rr++) {
            for (int cc = 0; cc + L - 1 < N; cc++) {
                if (placementValide(rr, cc, L, false, N, tirsEnvoyes))
                    incrSegment(map, rr, cc, L, false, N, tirsEnvoyes);
            }
        }
        for (int rr = 0; rr + L - 1 < N; rr++) {
            for (int cc = 0; cc < N; cc++) {
                if (placementValide(rr, cc, L, true, N, tirsEnvoyes))
                    incrSegment(map, rr, cc, L, true, N, tirsEnvoyes);
            }
        }
        return map;
    }

    private boolean placementValide(int r, int c, int L, boolean vertical, int N, boolean[][] tirsEnvoyes) {
        for (int k = 0; k < L; k++) {
            int rr = vertical ? r + k : r;
            int cc = vertical ? c : c + k;
            if (rr < 0 || rr >= N || cc < 0 || cc >= N) return false;
            if (tirsEnvoyes[rr][cc]) return false;
        }
        return true;
    }

    private void incrSegment(int[][] map, int r, int c, int L, boolean vertical, int N, boolean[][] tirsEnvoyes) {
        for (int k = 0; k < L; k++) {
            int rr = vertical ? r + k : r;
            int cc = vertical ? c : c + k;
            if (!tirsEnvoyes[rr][cc]) map[rr][cc]++;
        }
    }

    private void add(int[][] a, int[][] b) {
        for (int rr = 0; rr < a.length; rr++)
            for (int cc = 0; cc < a.length; cc++)
                a[rr][cc] += b[rr][cc];
    }
}
