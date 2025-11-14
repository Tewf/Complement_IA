package joueurs;

/**
 * Container for the result of a match: the winning player, the total
 * number of moves (attacks) played in the match, and the number of
 * moves performed by the winner.  Use {@link #getWinnerMoves()} when
 * you want the number of attacks performed by the winning player
 * (bounded by the grid size).
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

    /** Total attacks in the match (both players combined). */
    public int getTotalMoves() {
        return totalMoves;
    }

    /** Number of attacks performed by the winning player. */
    public int getWinnerMoves() {
        return winnerMoves;
    }
}
