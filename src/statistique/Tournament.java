package statistique;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import bataillenavale.BatailleNavale;
import joueurs.Bot;

import bataillenavale.BatailleNavale;
import joueurs.Bot;
// no direct use of joueurs.Joueur here; MatchResult used below

/**
 * Simple tournament runner that plays all available bot types against each other
 * (including self-play) N times, writes a CSV summary and draws a histogram PNG
 * of win rates per bot.
 */
public class Tournament {
    public static void main(String[] args) throws Exception {
        int N = 1000; // games per pairing
        int taille = 10;
        if (args.length > 0) {
            try { N = Integer.parseInt(args[0]); } catch (NumberFormatException ex) { /* ignore */ }
        }
        if (args.length > 1) {
            try { taille = Integer.parseInt(args[1]); } catch (NumberFormatException ex) { /* ignore */ }
        }

        final int[] FLOTTE = {5, 4, 3, 3, 2, 2};
        final String[] botTypes = {"uniform", "markov", "montecarlo", "smart"};
        final String[] labels = {"Uniform", "Markov", "MonteCarlo", "Smart"};

        final int B = botTypes.length;
        final int[][] wins = new int[B][B]; // wins[i][j] = times bot i beat bot j

        System.out.println("Tournament: " + B + " bots, " + N + " games per pairing, grid=" + taille);

        File outDir = new File("Results");
        outDir.mkdirs();

        // play each unordered pairing once (including self-play)
        for (int i = 0; i < B; i++) {
            for (int j = i; j < B; j++) {
                System.out.printf("Playing %s vs %s (%d games)...\n", labels[i], labels[j], N);
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
        int gamesPerBot = N * B; // plays each opponent (including self) N times
        for (int i = 0; i < B; i++) {
            int s = 0;
            for (int j = 0; j < B; j++) s += wins[i][j];
            totalWins[i] = s;
        }

        double[] rate = new double[B];
        double[] stderr = new double[B];
        for (int i = 0; i < B; i++) {
            rate[i] = (double) totalWins[i] / (double) gamesPerBot;
            stderr[i] = Math.sqrt(rate[i] * (1.0 - rate[i]) / (double) gamesPerBot);
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

        System.out.println("Wrote pairwise CSV to: " + pairCsv.getAbsolutePath());
        System.out.println("Wrote summary CSV to: " + sumCsv.getAbsolutePath());

        // write a human-readable pairwise table and ranking
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
            pw.println("Ranking:");
            for (int pos = 0; pos < B; pos++) {
                int i = idx[pos];
                pw.printf(Locale.ROOT, "%d. %s — wins=%d, win_rate=%.4f (stderr=%.4f)\n", pos + 1, labels[i], totalWins[i], rate[i], stderr[i]);
            }
        }
        System.out.println("Wrote pairwise table to: " + table.getAbsolutePath());

        // Also print ranking to console
        System.out.println("Ranking:");
        for (int pos = 0; pos < B; pos++) {
            int i = idx[pos];
            System.out.printf("%d. %s — win_rate=%.4f (stderr=%.4f)\n", pos + 1, labels[i], rate[i], stderr[i]);
        }
    }
}
