package logique;

import java.util.HashSet;
import java.util.Set;

/**
 * Représente un navire sur la grille.  Un navire est défini par ses
 * coordonnées de début et de fin ainsi qu'un ensemble de coordonnées
 * indiquant les parties déjà touchées.  Les navires peuvent être
 * horizontaux ou verticaux et ne peuvent pas être obliques.
 */
public class Navire {

    private final Coordonnee debut;
    private final Coordonnee fin;
    private final int longueur;
    private final Set<Coordonnee> partiesTouchees;

    /**
     * Crée un navire donné sa coordonnée de départ, sa longueur et
     * son orientation.
     *
     * @param debut       la coordonnée de départ (0 basée)
     * @param longueur    la longueur du navire (nombre de cases)
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
     * Retourne vrai si la coordonnée donnée se trouve sur le navire (qu'elle
     * ait été touchée ou non).
     */
    public boolean contient(Coordonnee c) {
        return c.getLigne() >= debut.getLigne() && c.getLigne() <= fin.getLigne()
                && c.getColonne() >= debut.getColonne() && c.getColonne() <= fin.getColonne();
    }

    /**
     * Retourne vrai si ce navire chevauche un autre navire (i.e. qu'ils
     * occupent au moins une case commune).
     */
    public boolean chevauche(Navire n) {
        return intersectionNonVide(debut.getLigne(), fin.getLigne(), n.debut.getLigne(), n.fin.getLigne())
                && intersectionNonVide(debut.getColonne(), fin.getColonne(), n.debut.getColonne(), n.fin.getColonne());
    }

    /**
     * Retourne vrai si ce navire touche un autre navire sans chevaucher
     * (c'est‑à‑dire s'ils sont adjacents horizontalement ou verticalement).
     */
    public boolean touche(Navire n) {
        boolean toucheHorizontalement = intersectionNonVide(debut.getLigne(), fin.getLigne(), n.debut.getLigne(), n.fin.getLigne())
                && (Math.abs(debut.getColonne() - n.fin.getColonne()) == 1 || Math.abs(fin.getColonne() - n.debut.getColonne()) == 1);
        boolean toucheVerticalement = intersectionNonVide(debut.getColonne(), fin.getColonne(), n.debut.getColonne(), n.fin.getColonne())
                && (Math.abs(debut.getLigne() - n.fin.getLigne()) == 1 || Math.abs(fin.getLigne() - n.debut.getLigne()) == 1);
        return toucheHorizontalement || toucheVerticalement;
    }

    /**
     * Retourne vrai si une des parties du navire à la coordonnée donnée a déjà
     * été touchée.
     */
    public boolean estTouche(Coordonnee c) {
        return partiesTouchees.contains(c);
    }

    /**
     * Applique un tir sur la coordonnée donnée.  Retourne vrai si le tir
     * touche effectivement le navire (et n'avait pas déjà été enregistré),
     * faux sinon.  Les tirs en double sur une case déjà touchée sont ignorés.
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
     * Indique si toutes les parties du navire ont été touchées (coulé).
     */
    public boolean estCoule() {
        return partiesTouchees.size() == longueur;
    }

    /**
     * Calcule une distance simple entre le début et la fin du navire.  Cette
     * méthode est principalement utilisée dans les tests unitaires.
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