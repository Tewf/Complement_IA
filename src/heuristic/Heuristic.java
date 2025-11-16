package heuristic;

import java.util.List;

import interfacegraphique.GrilleNavaleGraphique;
import logique.Coordonnee;

/**
 * Interface heuristique simple pour sélectionner une coordonnée de tir.
 */
public interface Heuristic {
        /**
         * Sélectionne la prochaine coordonnée d'attaque.
         *
         * @param tirsEnvoyes grille des cases déjà tirées (true si déjà tirée)
         * @param gng grille graphique de l'adversaire (utilisée pour la taille et des consultations éventuelles)
         * @param naviresRestants liste des longueurs de navires restant (peut être vide)
         * @param currentHits liste des coordonnées récemment touchées (cluster non encore coulé)
         * @return la coordonnée choisie (doit être une case non tirée)
         */
        Coordonnee choisir(boolean[][] tirsEnvoyes, GrilleNavaleGraphique gng, List<Integer> naviresRestants,
            List<Coordonnee> currentHits);
}
