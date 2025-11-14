package heuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import interfacegraphique.GrilleNavaleGraphique;
import logique.Coordonnee;
import logique.GrilleNavale;
import logique.Navire;

/**
 * MonteCarlo heuristic: generate many random full placements consistent with
 * observed information (known hits in `currentHits`, and fired-but-not-hit
 * cells inferred from `tirsEnvoyes`) and return the unfired cell with the
 * highest number of occurrences across samples.
 *
 * Usage: new MonteCarlo(nSamples).choisir(tirsEnvoyes, gng, naviresRestants, currentHits)
 */
public class MonteCarlo implements Heuristic {
    private final Random rng = new Random();
    private final int samples;

    public MonteCarlo() {
        this(1000);
    }

    public MonteCarlo(int samples) {
        this.samples = Math.max(1, samples);
    }

    @Override
    public Coordonnee choisir(boolean[][] tirsEnvoyes, GrilleNavaleGraphique gng, List<Integer> naviresRestants,
            List<Coordonnee> currentHits) {
        int N = gng.getTaille();
        if (naviresRestants == null || naviresRestants.isEmpty()) {
            // fallback to uniform if no ship information
            return new Uniform().choisir(tirsEnvoyes, gng, naviresRestants, currentHits);
        }

        // compute fired-miss cells: fired but not listed in currentHits
        Set<String> hitSet = new HashSet<>();
        if (currentHits != null) {
            for (Coordonnee h : currentHits) hitSet.add(key(h));
        }
        List<Coordonnee> firedMisses = new ArrayList<>();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (tirsEnvoyes[r][c] && !hitSet.contains(r + ":" + c)) {
                    firedMisses.add(new Coordonnee(r, c));
                }
            }
        }

        int[][] counts = new int[N][N];
        int[] sizes = naviresRestants.stream().mapToInt(Integer::intValue).toArray();

        for (int s = 0; s < samples; s++) {
            GrilleNavale sample = new GrilleNavale(N);
            boolean ok = true;

            // randomize ship order to diversify samples
            List<Integer> sizesList = new ArrayList<>();
            for (int L : sizes) sizesList.add(L);
            Collections.shuffle(sizesList, rng);

            for (int L : sizesList) {
                // generate all possible placements for this ship given current sample state
                List<Navire> options = new ArrayList<>();
                for (int r = 0; r < N; r++) {
                    for (int c = 0; c + L - 1 < N; c++) {
                        Navire n = new Navire(new Coordonnee(r, c), L, false);
                        if (sample.ajouteNavire(n)) {
                            // ajout temporaire succeeded — but ajouteNavire mutated sample; undo by removing
                            sample.getNavires().remove(n);
                            // check not overlapping fired misses
                            if (!placementTouchesAny(n, firedMisses)) options.add(n);
                        }
                    }
                }
                for (int r = 0; r + L - 1 < N; r++) {
                    for (int c = 0; c < N; c++) {
                        Navire n = new Navire(new Coordonnee(r, c), L, true);
                        if (sample.ajouteNavire(n)) {
                            sample.getNavires().remove(n);
                            if (!placementTouchesAny(n, firedMisses)) options.add(n);
                        }
                    }
                }

                if (options.isEmpty()) { ok = false; break; }
                // pick a random placement among options and commit it
                Navire choice = options.get(rng.nextInt(options.size()));
                boolean placed = sample.ajouteNavire(choice);
                if (!placed) { ok = false; break; }
            }

            if (!ok) continue;

            // verify sample covers all currentHits
            boolean coversAll = true;
            if (currentHits != null) {
                for (Coordonnee h : currentHits) {
                    if (sample.estALEau(h)) { coversAll = false; break; }
                }
            }
            if (!coversAll) continue;

            // also ensure none of the firedMisses are occupied
            boolean violatesMiss = false;
            for (Coordonnee m : firedMisses) {
                if (!sample.estALEau(m)) { violatesMiss = true; break; }
            }
            if (violatesMiss) continue;

            // accept sample: increment counts for every ship cell
            for (Navire n : sample.getNavires()) {
                for (Coordonnee cc : segmentOf(n)) counts[cc.getLigne()][cc.getColonne()]++;
            }
        }

        // choose best unfired cell
        int best = -1;
        List<Coordonnee> candidates = new ArrayList<>();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (!tirsEnvoyes[r][c]) {
                    int v = counts[r][c];
                    if (v > best) {
                        best = v; candidates.clear(); candidates.add(new Coordonnee(r, c));
                    } else if (v == best) candidates.add(new Coordonnee(r, c));
                }
            }
        }
        if (candidates.isEmpty()) return new Uniform().choisir(tirsEnvoyes, gng, naviresRestants, currentHits);
        return candidates.get(rng.nextInt(candidates.size()));
    }

    private boolean placementTouchesAny(Navire n, List<Coordonnee> firedMisses) {
        if (firedMisses == null || firedMisses.isEmpty()) return false;
        for (Coordonnee cc : segmentOf(n)) {
            for (Coordonnee m : firedMisses) {
                if (cc.getLigne() == m.getLigne() && cc.getColonne() == m.getColonne()) return true;
            }
        }
        return false;
    }

    private String key(Coordonnee c) { return c.getLigne() + ":" + c.getColonne(); }

    private List<Coordonnee> segmentOf(Navire n) {
        List<Coordonnee> seg = new ArrayList<>();
        Coordonnee d = n.getDebut();
        Coordonnee f = n.getFin();
        if (d.getLigne() == f.getLigne()) {
            int r = d.getLigne();
            for (int c = d.getColonne(); c <= f.getColonne(); c++) seg.add(new Coordonnee(r, c));
        } else if (d.getColonne() == f.getColonne()) {
            int c = d.getColonne();
            for (int r = d.getLigne(); r <= f.getLigne(); r++) seg.add(new Coordonnee(r, c));
        }
        return seg;
    }
}
