package logique;

/**
 * Représentation d'une coordonnée sur la grille de Bataille Navale.
 *
 * Cette classe encapsule les coordonnées ligne/colonne et fournit des
 * méthodes utilitaires pour la comparaison, le voisinage et la conversion
 * vers/depuis une notation alphabétique (par ex. "A1").
 */
public class Coordonnee {

    private final int ligne;
    private final int colonne;

    /**
     * Construit une coordonnée à partir d'une chaîne telle que "A1".
     * Les lettres peuvent être en majuscules ou minuscules. Les indices
     * ligne/colonne sont convertis en indices 0-based.
     *
     * @param s chaîne d'entrée (lettre(s) suivie(s) de chiffres)
     */
    public Coordonnee(String s) {
        char charC = s.charAt(0);
        if (charC >= 'a' && charC <= 'z') {
            this.colonne = charC - 'a';
        } else if (charC >= 'A' && charC <= 'Z') {
            this.colonne = charC - 'A';
        } else {
            throw new IllegalArgumentException("Format de colonne invalide : " + s);
        }
        try {
            // La saisie utilisateur utilise des indices de ligne 1-based
            this.ligne = Integer.parseInt(s.substring(1)) - 1;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Format de ligne invalide : " + s);
        }
    }

    /**
     * Construit une coordonnée à partir d'indices 0-based.
     *
     * @param l ligne (0-based)
     * @param c colonne (0-based)
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
        // Combinaison simple ligne/colonne pour HashMap/HashSet
        return 31 * ligne + colonne;
    }

    /**
     * Retourne vrai si la coordonnée passée est voisine immédiate
     * (haut, bas, gauche ou droite). Les diagonales ne sont pas considérées
     * comme voisines en Bataille Navale.
     */
    public boolean voisine(Coordonnee c) {
        int diffColonne = Math.abs(c.colonne - this.colonne);
        int diffLigne = Math.abs(c.ligne - this.ligne);
        return (diffLigne == 1 && diffColonne == 0) || (diffLigne == 0 && diffColonne == 1);
    }

    /**
     * Compare cette coordonnée à une autre pour un tri simple (ordre
     * lexicographique). Retourne une valeur négative, nulle ou positive.
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