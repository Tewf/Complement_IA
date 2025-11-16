package logique;

import java.util.HashSet;
import java.util.Set;

/**
 * Représente un navire sur la grille. Un navire est défini par ses coordonnées
 * de début et de fin ainsi que par l'ensemble des cases déjà touchées.
 * Les navires sont horizontaux ou verticaux (aucune diagonale).
 */
public class Navire {

    private final Coordonnee debut;
    private final Coordonnee fin;
    private final int longueur;
    private final Set<Coordonnee> partiesTouchees;

    /**
     * Crée un navire à partir d'une coordonnée de départ, d'une longueur et
     * d'une orientation.
     *
     * @param debut       coordonnée de départ (indexée à 0)
     * @param longueur    longueur du navire (nombre de cases)
     * @param estVertical vrai si le navire est vertical, faux s'il est horizontal
     */
    public Navire(Coordonnee debut, int longueur, boolean estVertical) {
        this.debut = debut;
        this.longueur = longueur;
        if (estVertical) {
            this.fin = new Coordonnee(debut.getLigne() + longueur - 1, debut.getColonne());
        } else {
            this.fin = new Coordonnee(debut.getLigne(), debut.getColonne() + longueur - 1);
        }
        this.partiesTouchees = new HashSet<>();
    }

    public Coordonnee getDebut() {
        return debut;
    }

    public Coordonnee getFin() {
        return fin;
    }

    /**
     * Retourne vrai si la coordonnée donnée se situe sur ce navire (touchée
     * ou non).
     */
    public boolean contient(Coordonnee c) {
        return c.getLigne() >= debut.getLigne() && c.getLigne() <= fin.getLigne()
                && c.getColonne() >= debut.getColonne() && c.getColonne() <= fin.getColonne();
    }

    /**
     * Retourne vrai si ce navire chevauche un autre navire (partage au moins
     * une case).
     */
    public boolean chevauche(Navire n) {
        return intersectionNonVide(debut.getLigne(), fin.getLigne(), n.debut.getLigne(), n.fin.getLigne())
                && intersectionNonVide(debut.getColonne(), fin.getColonne(), n.debut.getColonne(), n.fin.getColonne());
    }

    /**
     * Retourne vrai si ce navire touche un autre navire sans chevauchement
     * (adjacence horizontale ou verticale).
     */
    public boolean touche(Navire n) {
        boolean toucheHorizontalement = intersectionNonVide(debut.getLigne(), fin.getLigne(), n.debut.getLigne(), n.fin.getLigne())
                && (Math.abs(debut.getColonne() - n.fin.getColonne()) == 1 || Math.abs(fin.getColonne() - n.debut.getColonne()) == 1);
        boolean toucheVerticalement = intersectionNonVide(debut.getColonne(), fin.getColonne(), n.debut.getColonne(), n.fin.getColonne())
                && (Math.abs(debut.getLigne() - n.fin.getLigne()) == 1 || Math.abs(fin.getLigne() - n.debut.getLigne()) == 1);
        return toucheHorizontalement || toucheVerticalement;
    }

    /**
     * Retourne vrai si le navire a été touché à la coordonnée donnée.
     */
    public boolean estTouche(Coordonnee c) {
        return partiesTouchees.contains(c);
    }

    /**
     * Applique un tir à la coordonnée donnée. Retourne vrai si le tir touche
     * le navire et n'avait pas déjà été enregistré. Les tirs doublons sont
     * ignorés.
     */
    public boolean recoitTir(Coordonnee c) {
        if (contient(c) && !estTouche(c)) {
            partiesTouchees.add(c);
            return true;
        }
        return false;
    }

    /**
     * Indique si le navire a été touché au moins une fois.
     */
    public boolean estTouche() {
        return !partiesTouchees.isEmpty();
    }

    /**
     * Indique si toutes les parties du navire ont été touchées (navire coulé).
     */
    public boolean estCoule() {
        return partiesTouchees.size() == longueur;
    }

    /**
     * Calcule une distance euclidienne simple entre le début et la fin du
     * navire. Principalement utilisée pour des tests unitaires.
     */
    public double distance() {
        return Math.hypot(fin.getLigne() - debut.getLigne(), fin.getColonne() - debut.getColonne());
    }

    private static boolean intersectionNonVide(int d1, int f1, int d2, int f2) {
        return (d1 <= f2 && f1 >= d2);
    }

    @Override
    public String toString() {
        return "Navire de " + debut + " à " + fin + ", touché " + partiesTouchees.size() + " fois.";
    }
}