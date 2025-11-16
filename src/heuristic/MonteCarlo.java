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
 * Heuristique Monte Carlo pour la sélection d'un tir.
 *
 * Description :
 * Cette heuristique génère un grand nombre d'échantillons (placements complets
 * de la flotte) compatibles avec les informations observées :
 * - les coordonnées connues comme "touchées" (`currentHits`) doivent être
 *   couvertes par un navire dans l'échantillon ;
 * - les cases déjà tirées et identifiées comme "manquées" (déduites de
 *   `tirsEnvoyes` moins `currentHits`) ne doivent pas contenir de navire.
 *
 * Pour chaque échantillon valide, la méthode incrémente un compteur pour
 * chaque case occupée par un navire. Après avoir produit `samples` échantillons
 * acceptés (ou tenté `samples` itérations), l'algorithme choisit la case
 * non encore tirée qui a été la plus fréquemment occupée dans les échantillons.
 * En cas d'égalité, une case est sélectionnée aléatoirement parmi les meilleures.
 *
 * Comportements complémentaires :
 * - Si la liste `naviresRestants` est vide, l'heuristique retombe sur une
 *   stratégie uniforme (fonction `Uniform`).
 * - L'algorithme randomise l'ordre de placement des navires pour diversifier
 *   les échantillons.
 *
 * Usage recommandé : ajuster `samples` pour un compromis qualité/temps (valeur
 * par défaut : 1000).
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
    /**
     * Construire et évaluer `samples` placements aléatoires cohérents, puis
     * retourner la meilleure coordonnée non tirée.
     *
     * Paramètres :
     * - `tirsEnvoyes` : matrice (N x N) indiquant les cases déjà tirées.
     * - `gng` : grille graphique (utilisée ici pour récupérer la taille N).
     * - `naviresRestants` : longueurs des navires encore présents.
     * - `currentHits` : coordonnées des impacts détectés (touchés mais pas coulés).
     *
     * Retour : la `Coordonnee` cible choisie, ou une alternative (Uniform)
     * si aucune case pertinente n'est trouvée.
     */
    public Coordonnee choisir(boolean[][] tirsEnvoyes, GrilleNavaleGraphique gng, List<Integer> naviresRestants,
            List<Coordonnee> currentHits) {
        int N = gng.getTaille();
        if (naviresRestants == null || naviresRestants.isEmpty()) {
            // repli sur uniforme si aucune information sur les navires
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

        // Compteurs d'occupation : pour chaque échantillon valide, on incrémente
        // les cases occupées par un navire.
        int[][] counts = new int[N][N];
        int[] sizes = naviresRestants.stream().mapToInt(Integer::intValue).toArray();

        for (int s = 0; s < samples; s++) {
            // Construire un placement d'essai : placer chaque navire aléatoirement
            // parmi les options valides (sans chevauchement des tirs manqués).
            GrilleNavale sample = new GrilleNavale(N);
            boolean ok = true;

            // Randomiser l'ordre des longueurs pour diversifier les configurations
            List<Integer> sizesList = new ArrayList<>();
            for (int L : sizes) sizesList.add(L);
            Collections.shuffle(sizesList, rng);

            for (int L : sizesList) {
                // Lister toutes les positions valides pour ce navire dans l'échantillon
                List<Navire> options = new ArrayList<>();
                // positions horizontales
                for (int r = 0; r < N; r++) {
                    for (int c = 0; c + L - 1 < N; c++) {
                        Navire n = new Navire(new Coordonnee(r, c), L, false);
                        if (sample.ajouteNavire(n)) {
                            // retire l'ajout temporaire pour restaurer l'état
                            sample.getNavires().remove(n);
                            // ignorer placements qui toucheraient des "miss"
                            if (!placementTouchesAny(n, firedMisses)) options.add(n);
                        }
                    }
                }
                // positions verticales
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
                // choisir un placement au hasard parmi les options et le fixer
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

            // Echantillon accepté : incrémenter les compteurs pour chaque case
            // occupée par un navire dans cet échantillon.
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
