package joueurs;

/**
 * Conteneur pour le résultat d'un match : le joueur vainqueur, le nombre
 * total de coups (attaques) joués dans la partie, et le nombre de coups
 * effectués par le vainqueur. Utiliser {@link #getWinnerMoves()} pour
 * obtenir le nombre d'attaques effectuées par le joueur gagnant
 * (borné par la taille de la grille).
 */
public class MatchResult {
    private final Joueur winner;
    private final int totalMoves;
    private final int winnerMoves;

    public MatchResult(Joueur winner, int totalMoves, int winnerMoves) {
        this.winner = winner;
        this.totalMoves = totalMoves;
        this.winnerMoves = winnerMoves;
    }

    public Joueur getWinner() {
        return winner;
    }

    /** Nombre total d'attaques dans la partie (les deux joueurs combinés). */
    public int getTotalMoves() {
        return totalMoves;
    }

    /** Nombre d'attaques effectuées par le joueur vainqueur. */
    public int getWinnerMoves() {
        return winnerMoves;
    }
}
