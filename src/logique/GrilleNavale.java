package logique;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Gestion logique d'une grille de bataille navale.  La grille contient
 * une liste de navires placés et un ensemble de tirs déjà reçus.  Cette
 * classe ne connaît pas l'interface graphique ; elle ne manipule que des
 * données abstraites.  La classe `GrilleNavaleGraphique` étend celle‑ci
 * pour ajouter l'affichage.
 */
public class GrilleNavale {

    protected final int taille;
    protected final List<Navire> navires;
    protected final Set<Coordonnee> tirsRecus;
    private final Random rnd = new Random();

    /**
     * Crée une grille de la taille indiquée (carrée).
     *
     * @param taille dimension de la grille (nombre de lignes et de colonnes)
     */
    public GrilleNavale(int taille) {
        this.taille = taille;
        this.navires = new ArrayList<>();
        this.tirsRecus = new HashSet<>();
    }

    public int getTaille() {
        return taille;
    }

    /**
     * Vérifie si une coordonnée est dans les limites de la grille.
     */
    public boolean estDansGrille(Coordonnee c) {
        int ligne = c.getLigne();
        int colonne = c.getColonne();
        return ligne >= 0 && ligne < taille && colonne >= 0 && colonne < taille;
    }

    /**
     * Ajoute un navire à la grille s'il ne sort pas des limites, ne chevauche
     * pas un navire existant et ne touche pas de façon adjacente (pas de
     * navires accollés).  Retourne vrai en cas de succès, faux sinon.
     */
    public boolean ajouteNavire(Navire n) {
        if (!estDansGrille(n.getDebut()) || !estDansGrille(n.getFin())) {
            return false;
        }
        for (Navire existing : navires) {
            if (n.chevauche(existing) || n.touche(existing)) {
                return false;
            }
        }
        navires.add(n);
        return true;
    }

    /**
     * Place automatiquement une liste de navires de tailles données.
     * Chaque navire est positionné aléatoirement tant qu'il respecte les
     * contraintes de placement.  Cette méthode garantit que tous les
     * navires sont placés avant de retourner.
     *
     * @param taillesNavires tableau des longueurs des navires à placer
     */
    public void placementAuto(int[] taillesNavires) {
        for (int tailleNavire : taillesNavires) {
            boolean placed = false;
            while (!placed) {
                int ligne = rnd.nextInt(taille);
                int colonne = rnd.nextInt(taille);
                boolean estVertical = rnd.nextBoolean();
                Navire n = new Navire(new Coordonnee(ligne, colonne), tailleNavire, estVertical);
                placed = ajouteNavire(n);
            }
        }
    }

    /**
     * Traite un tir sur la grille.  Si la coordonnée a déjà été attaquée
     * auparavant, on retourne faux.  Sinon, on enregistre le tir et on
     * retourne vrai si un navire est touché.
     */
    public boolean recoitTir(Coordonnee c) {
        if (tirsRecus.contains(c)) {
            return false;
        }
        tirsRecus.add(c);
        for (Navire n : navires) {
            if (n.recoitTir(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indique si une coordonnée n'est occupée par aucun navire (à l'eau).
     */
    public boolean estALEau(Coordonnee c) {
        for (Navire n : navires) {
            if (n.contient(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Indique si un navire est touché à la coordonnée donnée.
     */
    public boolean estTouche(Coordonnee c) {
        for (Navire n : navires) {
            if (n.estTouche(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indique si un navire est coulé à la coordonnée donnée.
     */
    public boolean estCoule(Coordonnee c) {
        for (Navire n : navires) {
            if (n.contient(c) && n.estCoule()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indique si tous les navires de la grille ont été coulés.
     */
    public boolean perdu() {
        for (Navire n : navires) {
            if (!n.estCoule()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Renvoie la liste des navires de la grille.  Utilisé pour l'affichage
     * dans certaines interfaces graphiques.
     */
    public List<Navire> getNavires() {
        return navires;
    }

    /**
     * Indique si une coordonnée a déjà été attaquée.  Pratique pour les
     * joueurs automatiques qui souhaitent éviter de répéter le même tir.
     */
    public boolean aDejaTire(Coordonnee c) {
        return tirsRecus.contains(c);
    }
}