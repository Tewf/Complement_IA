package interfacegraphique;

import java.awt.Color;

import logique.Coordonnee;
import logique.GrilleNavale;
import logique.Navire;

/**
 * Couche graphique au-dessus de la logique de la grille.
 *
 * Lorsqu'un navire est ajouté ou qu'un tir est reçu, la grille graphique est
 * colorée afin de refléter l'état courant (vert = navire, rouge = touché,
 * bleu = manqué). Cette classe délègue la logique métier à `GrilleNavale`
 * et se limite à la mise à jour de l'affichage.
 */
public class GrilleNavaleGraphique extends GrilleNavale {
    private final GrilleGraphique gg;

    public GrilleNavaleGraphique(int taille) {
        super(taille);
        this.gg = new GrilleGraphique(taille);
    }

    /**
     * Retourne la grille graphique associée, utilisée par l'interface pour
     * l'affichage et les interactions utilisateur.
     */
    public GrilleGraphique getGrilleGraphique() {
        return gg;
    }

    @Override
    public boolean ajouteNavire(Navire n) {
        // Délégation à la logique : si l'ajout réussit, colorier la zone
        boolean success = super.ajouteNavire(n);
        if (success) {
            gg.colorie(n.getDebut(), n.getFin(), Color.GREEN);
        }
        return success;
    }

    @Override
    public boolean recoitTir(Coordonnee c) {
        // Enregistrer le tir via la logique, puis mettre à jour l'affichage
        boolean hit = super.recoitTir(c);
        if (hit) {
            gg.colorie(c, Color.RED);
        } else {
            gg.colorie(c, Color.BLUE);
        }
        return hit;
    }

    /**
     * Indique si un navire est coulé à la coordonnée donnée (utile pour
     * adapter l'affichage ou déclencher une animation). Retourne vrai si
     * la coordonnée appartient à un navire désormais coulé.
     */
    public boolean estCoule(Coordonnee c) {
        return super.estCoule(c);
    }

    /**
     * Indique si une coordonnée est de l'eau (aucun navire ne l'occupe).
     */
    public boolean estALEau(Coordonnee c) {
        return super.estALEau(c);
    }
}