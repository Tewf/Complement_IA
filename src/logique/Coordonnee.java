package logique;

/**
 * Representation d'une coordonnée sur la grille de bataille navale.
 * <p>
 * Cette classe encapsule les coordonnées ligne/colonne et fournit
 * quelques méthodes utilitaires pour comparer, voisinage et conversion
 * vers/depuis une notation alphabétique.  Un constructeur à partir d'une
 * chaîne permet de convertir une notation comme « A1 » ou « c5 » en
 * indices internes (0 basé).  Un second constructeur accepte directement
 * les indices numériques.
 */
public class Coordonnee {

    private final int ligne;
    private final int colonne;

    /**
     * Construit une coordonnée à partir d'une chaîne telle que « A1 ».
     * Les lettres peuvent être en majuscules ou en minuscules.  La
     * conversion utilise un indice 0 basé pour la ligne et la colonne.
     *
     * @param s chaîne de la forme lettre(s) suivie de chiffres
     */
    public Coordonnee(String s) {
        char charC = s.charAt(0);
        if (charC >= 'a' && charC <= 'z') {
            this.colonne = charC - 'a';
        } else if (charC >= 'A' && charC <= 'Z') {
            this.colonne = charC - 'A';
        } else {
            throw new IllegalArgumentException("Format de colonne invalide : " + s);
        }
        try {
            // soustraire 1 car les lignes dans l'interface utilisateur commencent à 1
            this.ligne = Integer.parseInt(s.substring(1)) - 1;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Format de ligne invalide : " + s);
        }
    }

    /**
     * Construit une coordonnée à partir d'indices 0 basés.
     *
     * @param l la ligne (0 basé)
     * @param c la colonne (0 basé)
     */
    public Coordonnee(int l, int c) {
        this.ligne = l;
        this.colonne = c;
    }

    public int getLigne() {
        return ligne;
    }

    public int getColonne() {
        return colonne;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (o instanceof Coordonnee) {
            Coordonnee other = (Coordonnee) o;
            return this.ligne == other.ligne && this.colonne == other.colonne;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // combinaison simple ligne/colonne pour un usage dans les HashMap/HashSet
        return 31 * ligne + colonne;
    }

    /**
     * Retourne vrai si la coordonnée passée est voisine immédiate (haut, bas,
     * gauche ou droite).  Les diagonales ne sont pas considérées comme des
     * voisines dans le cadre de la bataille navale.
     */
    public boolean voisine(Coordonnee c) {
        int diffColonne = Math.abs(c.colonne - this.colonne);
        int diffLigne = Math.abs(c.ligne - this.ligne);
        return (diffLigne == 1 && diffColonne == 0) || (diffLigne == 0 && diffColonne == 1);
    }

    /**
     * Compare cette coordonnée à une autre pour un tri simple.  La valeur
     * retournée est négative, nulle ou positive suivant la position dans
     * l'ordre lexicographique.
     */
    public int compareTo(Coordonnee c) {
        int valO = ligne * 100 + colonne;
        int valC = c.ligne * 100 + c.colonne;
        return valO - valC;
    }

    @Override
    public String toString() {
        return String.valueOf((char) (colonne + 'A')) + (ligne + 1);
    }
}