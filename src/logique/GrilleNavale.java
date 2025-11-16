package logique;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Gestion logique d'une grille de Bataille Navale. La grille contient une
 * liste de navires placés et un ensemble de tirs reçus. Cette classe est
 * indépendante de l'interface graphique et ne manipule que les données de
 * domaine. La sous-classe `GrilleNavaleGraphique` ajoute le comportement
 * d'affichage.
 */
public class GrilleNavale {

    protected final int taille;
    protected final List<Navire> navires;
    protected final Set<Coordonnee> tirsRecus;
    private final Random rnd = new Random();

    /**
     * Crée une grille carrée de la taille indiquée.
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
     * Ajoute un navire à la grille s'il ne dépasse pas les limites, ne chevauche
     * pas un navire existant et ne touche pas (pas de navires adjacents).
     * Retourne vrai si l'ajout a réussi.
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
     * Place automatiquement une liste de navires aux tailles données. Chaque
     * navire est positionné aléatoirement tant que les contraintes de
     * placement sont respectées. Cette méthode garantit que tous les navires
     * sont placés avant de retourner.
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
     * Traite un tir sur la grille. Si la coordonnée a déjà été attaquée,
     * retourne faux. Sinon, enregistre le tir et retourne vrai si un navire
     * a été touché.
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
     * Indique si la coordonnée est à l'eau (aucun navire ne l'occupe).
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
     * Indique si un navire contenant la coordonnée donnée est coulé.
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
     * Renvoie la liste des navires présents sur la grille. Utilisé par les
     * composants d'interface pour le rendu.
     */
    public List<Navire> getNavires() {
        return navires;
    }

    /**
     * Indique si la coordonnée a déjà été attaquée. Utile pour les joueurs
     * automatiques qui doivent éviter de répéter un tir.
     */
    public boolean aDejaTire(Coordonnee c) {
        return tirsRecus.contains(c);
    }
}