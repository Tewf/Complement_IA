package heuristic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import interfacegraphique.GrilleNavaleGraphique;
import logique.Coordonnee;

/**
 * Uniform heuristic: pick uniformly at random among unfired cells.
 */
public class Uniform implements Heuristic {
    private final Random rng = new Random();

    @Override
    public Coordonnee choisir(boolean[][] tirsEnvoyes, GrilleNavaleGraphique gng, List<Integer> naviresRestants,
            List<Coordonnee> currentHits) {
        int N = gng.getTaille();
        List<Coordonnee> candidates = new ArrayList<>();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (!tirsEnvoyes[r][c]) candidates.add(new Coordonnee(r, c));
            }
        }
        if (candidates.isEmpty()) return null;
        Coordonnee choix = candidates.get(rng.nextInt(candidates.size()));
        return choix;
    }
}
