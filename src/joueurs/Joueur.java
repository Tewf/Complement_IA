package joueurs;

import logique.Coordonnee;

/**
 * Classe abstraite représentant un joueur. Chaque joueur possède une
 * grille et peut choisir des attaques ainsi que défendre sa grille. La
 * boucle de jeu est implémentée dans {@link #jouerAvec(Joueur)} et s'appuie
 * sur les codes de retour suivants :
 * <ul>
 *   <li>{@code TOUCHE} : un navire a été touché mais pas coulé.</li>
 *   <li>{@code COULE} : un navire a été coulé mais d'autres navires restent.</li>
 *   <li>{@code A_L_EAU} : l'attaque a manqué (à l'eau).</li>
 *   <li>{@code GAMEOVER} : tous les navires du défenseur sont coulés.</li>
 * </ul>
 */
public abstract class Joueur {
    public static final int TOUCHE = 1;
    public static final int COULE = 2;
    public static final int A_L_EAU = 3;
    public static final int GAMEOVER = 4;

    private final int tailleGrille;
    protected Joueur adversaire;

    public Joueur(int taille) {
        this.tailleGrille = taille;
    }

    public int getTaille() {
        return tailleGrille;
    }

    /**
     * Déroule une partie entre ce joueur et l'adversaire fourni. Les tours
     * s'alternent jusqu'à ce qu'un joueur n'ait plus de navires. La méthode
     * gère le passage de témoin et retourne un {@link MatchResult}
     * décrivant le vainqueur et les statistiques de la partie.
     *
     * @return résultat du match avec le vainqueur et les compteurs
     */
    public MatchResult jouerAvec(Joueur adversaire) {
        if (this.adversaire != null || adversaire.adversaire != null) {
            throw new IllegalStateException("Un des joueurs est déjà en partie.");
        }
        this.adversaire = adversaire;
        adversaire.adversaire = this;
        Joueur current = this;
        Joueur lastAttacker = null;
        int res;
        int movesA = 0; // attaques effectuées par `this`
        int movesB = 0; // attaques effectuées par `adversaire`
        boolean isAturn = true;
        do {
            lastAttacker = current;
            Coordonnee attaque = current.choisirAttaque();
            // increment the appropriate counter depending on whose turn it is
            if (isAturn) movesA++; else movesB++;
            res = current.adversaire.defendre(attaque);
            current.retourAttaque(attaque, res);
            current.adversaire.retourDefense(attaque, res);
            // changement de joueur
            current = (current == this) ? adversaire : this;
            isAturn = !isAturn;
        } while (res != GAMEOVER);
        // nettoyage des liens entre joueurs
        this.adversaire = null;
        adversaire.adversaire = null;
        int total = movesA + movesB;
        int winnerMoves = (lastAttacker == this) ? movesA : movesB;
        return new MatchResult(lastAttacker, total, winnerMoves);
    }

    /**
     * Méthodes à implémenter dans les classes concrètes pour traiter les
     * retours suite à une attaque.
     */
    protected abstract void retourAttaque(Coordonnee c, int etat);

    /**
     * Méthodes à implémenter pour gérer les retours après défense.
     */
    protected abstract void retourDefense(Coordonnee c, int etat);

    /**
     * Choisit une coordonnée à attaquer. Pour les joueurs humains, cette
     * méthode peut bloquer en attendant un clic utilisateur.
     */
    public abstract Coordonnee choisirAttaque();

    /**
     * Défend la grille contre une attaque. Doit retourner l'un des codes
     * définis dans cette classe.
     */
    public abstract int defendre(Coordonnee c);
}