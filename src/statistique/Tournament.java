package statistique;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import bataillenavale.BatailleNavale;
import joueurs.Bot;

/**
 * Exécuteur de tournoi simple qui fait jouer tous les types de bots entre eux
 * (y compris en self-play) N fois, écrit un résumé CSV et génère une table
 * lisible des résultats avec le classement.
 */
public class Tournament {
    public static void main(String[] args) throws Exception {
        int N = 100; // games per pairing
        int taille = 10;
        if (args.length > 0) {
            try { N = Integer.parseInt(args[0]); } catch (NumberFormatException ex) { /* ignore */ }
        }
        if (args.length > 1) {
            try { taille = Integer.parseInt(args[1]); } catch (NumberFormatException ex) { /* ignore */ }
        }

        final int[] FLOTTE = {5, 4, 3, 3, 2, 2};
        final String[] botTypes = {"uniform", "markov", "montecarlo", "smart"};
        final String[] labels = {"Uniforme", "Markov", "MonteCarlo", "Intelligent"};

        final int B = botTypes.length;
        final int[][] wins = new int[B][B]; // wins[i][j] = nombre de victoires du bot i contre le bot j

        System.out.println("Tournoi : " + B + " bots, " + N + " parties par confrontation, grille=" + taille);

        File outDir = new File("Results");
        outDir.mkdirs();

        // jouer chaque confrontation non ordonnée une fois (sans self-play)
        for (int i = 0; i < B; i++) {
            for (int j = i + 1; j < B; j++) {
                System.out.printf("Parties %s vs %s (%d parties)...\n", labels[i], labels[j], N);
                for (int k = 0; k < N; k++) {
                    Bot b1 = BatailleNavale.initBot(taille, FLOTTE, botTypes[i]);
                    Bot b2 = BatailleNavale.initBot(taille, FLOTTE, botTypes[j]);
                    joueurs.MatchResult result = b1.jouerAvec(b2);
                    if (result.getWinner() == b1) {
                        wins[i][j]++;
                    } else {
                        wins[j][i]++;
                    }
                }
            }
        }

        int[] totalWins = new int[B];
        int gamesPerBot = N * (B - 1); // nombre de parties jouées par bot (chaque adversaire distinct N fois, sans self-play)
        for (int i = 0; i < B; i++) {
            int s = 0;
            for (int j = 0; j < B; j++) s += wins[i][j];
            totalWins[i] = s;
        }

        double[] rate = new double[B];
        double[] stderr = new double[B];
        for (int i = 0; i < B; i++) {
            if (gamesPerBot > 0) {
                rate[i] = (double) totalWins[i] / (double) gamesPerBot;
                stderr[i] = Math.sqrt(rate[i] * (1.0 - rate[i]) / (double) gamesPerBot);
            } else {
                rate[i] = 0.0;
                stderr[i] = 0.0;
            }
        }

        // ranking
        Integer[] idx = new Integer[B];
        for (int i = 0; i < B; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Double.compare(rate[b], rate[a]));
        int[] rank = new int[B];
        for (int pos = 0; pos < B; pos++) rank[idx[pos]] = pos + 1;

        // write pairwise CSV
        File pairCsv = new File(outDir, "tournament_pairwise.csv");
        try (PrintWriter pw = new PrintWriter(pairCsv)) {
            pw.print("bot");
            for (int j = 0; j < B; j++) pw.print("," + labels[j]);
            pw.println();
            for (int i = 0; i < B; i++) {
                pw.print(labels[i]);
                for (int j = 0; j < B; j++) pw.printf(",%d", wins[i][j]);
                pw.println();
            }
        }
        // write summary CSV
        File sumCsv = new File(outDir, "tournament_summary.csv");
        try (PrintWriter pw = new PrintWriter(sumCsv)) {
            pw.println("bot,games_played,wins,win_rate,standard_error,rank");
            for (int i = 0; i < B; i++) {
                pw.printf("%s,%d,%d,%.6f,%.6f,%d\n", labels[i], gamesPerBot, totalWins[i], rate[i], stderr[i], rank[i]);
            }
        }

        System.out.println("Fichier CSV pairwise écrit : " + pairCsv.getAbsolutePath());
        System.out.println("Fichier résumé CSV écrit : " + sumCsv.getAbsolutePath());

        // écrire une table pairwise lisible et le classement
        File table = new File(outDir, "tournament_pairwise_table.txt");
        try (PrintWriter pw = new PrintWriter(table)) {
            // header
            pw.printf("%20s", "");
            for (int j = 0; j < B; j++) pw.printf("%8s", labels[j]);
            pw.println();
            for (int i = 0; i < B; i++) {
                pw.printf(Locale.ROOT, "%20s", labels[i]);
                for (int j = 0; j < B; j++) pw.printf(Locale.ROOT, "%8d", wins[i][j]);
                pw.println();
            }
            pw.println();
            pw.println("Classement :");
            for (int pos = 0; pos < B; pos++) {
                int i = idx[pos];
                pw.printf(Locale.ROOT, "%d. %s — victoires=%d, taux_victoire=%.4f (stderr=%.4f)\n", pos + 1, labels[i], totalWins[i], rate[i], stderr[i]);
            }
        }
        System.out.println("Fichier table pairwise écrit : " + table.getAbsolutePath());

        // Also print ranking to console
        System.out.println("Classement :");
        for (int pos = 0; pos < B; pos++) {
            int i = idx[pos];
            System.out.printf("%d. %s — taux_victoire=%.4f (stderr=%.4f)\n", pos + 1, labels[i], rate[i], stderr[i]);
        }
    }
}
