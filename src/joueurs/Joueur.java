package joueurs;

import logique.Coordonnee;

/**
 * Classe abstraite représentant un joueur.  Chaque joueur possède sa
 * propre grille et peut choisir ses attaques et se défendre face aux
 * attaques adverses.  L'algorithme de déroulement du jeu est implémenté
 * dans {@link #jouerAvec(Joueur)} et se base sur les codes de retour
 * suivants :
 * <ul>
 *   <li>{@code TOUCHE} : un navire a été touché mais pas coulé.</li>
 *   <li>{@code COULE} : un navire a été coulé mais il reste d'autres navires.</li>
 *   <li>{@code A_L_EAU} : l'attaque a manqué toutes les cibles.</li>
 *   <li>{@code GAMEOVER} : tous les navires du défenseur sont coulés.</li>
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
     * Déroulement d'une partie entre ce joueur et son adversaire.  Les tours
     * s'enchaînent jusqu'à ce qu'un joueur n'ait plus de navires.  Cette
     * méthode gère le passage de témoin entre les deux joueurs et retourne
     * le joueur vainqueur (celui qui a provoqué l'état {@code GAMEOVER}).
     *
     * @return le joueur gagnant
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
        int movesA = 0; // attacks performed by `this`
        int movesB = 0; // attacks performed by `adversaire`
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
        // cleanup lien entre joueurs
        this.adversaire = null;
        adversaire.adversaire = null;
        int total = movesA + movesB;
        int winnerMoves = (lastAttacker == this) ? movesA : movesB;
        return new MatchResult(lastAttacker, total, winnerMoves);
    }

    /**
     * Méthodes à implémenter dans les classes concrètes pour gérer la
     * réception des différents états après une attaque.
     */
    protected abstract void retourAttaque(Coordonnee c, int etat);

    /**
     * Méthodes à implémenter pour gérer les réactions après une défense.
     */
    protected abstract void retourDefense(Coordonnee c, int etat);

    /**
     * Choisit une coordonnée à attaquer.  Pour les joueurs humains, cette
     * méthode bloque jusqu'à ce qu'un clic soit effectué sur la grille.
     */
    public abstract Coordonnee choisirAttaque();

    /**
     * Défend la grille contre une attaque.  Doit retourner l'un des codes
     * définis dans cette classe.
     */
    public abstract int defendre(Coordonnee c);
}