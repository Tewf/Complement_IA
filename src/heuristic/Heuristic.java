package heuristic;

import java.util.List;

import interfacegraphique.GrilleNavaleGraphique;
import logique.Coordonnee;

/**
 * Simple heuristic interface for selecting a firing coordinate.
 */
public interface Heuristic {
    /**
     * Choose the next attack coordinate.
     *
     * @param tirsEnvoyes grid of already-fired cells (true if fired)
     * @param gng the opponent graphical grid (used for size and optional queries)
     * @param naviresRestants list of remaining ship lengths (may be empty)
     * @param currentHits list of ongoing hit coordinates (not yet sunk)
     * @return chosen coordinate (must be unfired)
     */
    Coordonnee choisir(boolean[][] tirsEnvoyes, GrilleNavaleGraphique gng, List<Integer> naviresRestants,
            List<Coordonnee> currentHits);
}
