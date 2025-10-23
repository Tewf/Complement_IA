package interfacegraphique;

import java.awt.Color;

import logique.Coordonnee;
import logique.GrilleNavale;
import logique.Navire;

/**
 * Couche graphique par dessus la logique de la grille.  Chaque fois qu'un
 * navire est ajouté ou qu'un tir est reçu, la grille graphique est
 * colorée pour refléter l'état (vert pour les navires, rouge pour les
 * touches, bleu pour les tirs à l'eau).  Cette classe ne fournit pas
 * directement d'interface pour récupérer les informations ; elle délègue
 * à `GrilleNavale` pour la logique.
 */
public class GrilleNavaleGraphique extends GrilleNavale {
    private final GrilleGraphique gg;

    public GrilleNavaleGraphique(int taille) {
        super(taille);
        this.gg = new GrilleGraphique(taille);
    }

    /**
     * Retourne la grille graphique associée pour l'affichage.
     */
    public GrilleGraphique getGrilleGraphique() {
        return gg;
    }

    @Override
    public boolean ajouteNavire(Navire n) {
        boolean success = super.ajouteNavire(n);
        if (success) {
            gg.colorie(n.getDebut(), n.getFin(), Color.GREEN);
        }
        return success;
    }

    @Override
    public boolean recoitTir(Coordonnee c) {
        boolean hit = super.recoitTir(c);
        if (hit) {
            gg.colorie(c, Color.RED);
        } else {
            gg.colorie(c, Color.BLUE);
        }
        return hit;
    }

    /**
     * Indique si un navire est coulé à la coordonnée donnée.  Permet de
     * différencier l'affichage dans l'interface utilisateur.
     */
    public boolean estCoule(Coordonnee c) {
        return super.estCoule(c);
    }

    /**
     * Indique si une coordonnée est à l'eau (aucun navire ne l'occupe).
     */
    public boolean estALEau(Coordonnee c) {
        return super.estALEau(c);
    }
}