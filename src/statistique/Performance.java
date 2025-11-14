package statistique;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import bataillenavale.BatailleNavale;
import joueurs.MatchResult;
import joueurs.Bot;

/**
 * Runs self-play experiments: each bot plays against itself N times.
 * Records the number of moves until the win, computes mean and standard
 * error, writes a CSV and produces a histogram PNG per bot.
 */
public class Performance {
    public static void main(String[] args) throws Exception {
        int N = 100;
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

        File outDir = new File("Results");
        outDir.mkdirs();

        // We'll collect stats for all bots first, then draw a single gaussian overlay plot
        File csv = new File(outDir, "performance_summary.csv");
        double[] means = new double[botTypes.length];
        double[] sds = new double[botTypes.length];
        double[] stderrs = new double[botTypes.length];
        int[] maxObserved = new int[botTypes.length];

        List<List<Integer>> allMoves = new ArrayList<>();
        for (int i = 0; i < botTypes.length; i++) allMoves.add(new ArrayList<>());

        for (int i = 0; i < botTypes.length; i++) {
            System.out.printf("Running self-play for %s (%d trials)...\n", labels[i], N);
            List<Integer> moves = allMoves.get(i);
            for (int t = 0; t < N; t++) {
                Bot b1 = BatailleNavale.initBot(taille, FLOTTE, botTypes[i]);
                Bot b2 = BatailleNavale.initBot(taille, FLOTTE, botTypes[i]);
                MatchResult res = b1.jouerAvec(b2);
                moves.add(res.getWinnerMoves());
            }
            double sum = 0.0;
            int max = 0;
            for (int m : moves) { sum += m; if (m > max) max = m; }
            double mean = sum / (double) moves.size();
            double var = 0.0;
            for (int m : moves) var += (m - mean) * (m - mean);
            double sd = moves.size() > 1 ? Math.sqrt(var / (moves.size() - 1)) : 0.0;
            double stderr = sd / Math.sqrt(moves.size());
            means[i] = mean; sds[i] = sd; stderrs[i] = stderr; maxObserved[i] = max;
        }

        // write CSV with summary stats
        try (PrintWriter pw = new PrintWriter(csv)) {
            pw.println("bot,trials,mean_moves,std_error");
            for (int i = 0; i < botTypes.length; i++) {
                pw.printf("%s,%d,%.6f,%.6f\n", labels[i], N, means[i], stderrs[i]);
            }
        }

        // Draw a single PNG with gaussian curves for each bot
        int overallMax = 0; for (int v : maxObserved) if (v > overallMax) overallMax = v;
        int xMax = Math.max(overallMax, taille * taille); // at least grid size
        int width = 900, height = 520;
        BufferedImage img2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img2.createGraphics();
        g2.setColor(Color.WHITE); g2.fillRect(0,0,width,height);
        int marginL = 80, marginR = 120, marginT = 60, marginB = 80;
        int plotW = width - marginL - marginR; int plotH = height - marginT - marginB;
        g2.setColor(Color.BLACK);
        g2.drawLine(marginL, marginT, marginL, marginT + plotH);
        g2.drawLine(marginL, marginT + plotH, marginL + plotW, marginT + plotH);

        // compute pdf values for each bot across x in [0..xMax]
        int samples = xMax * 4 + 1; // quarter-step resolution
        double[] xs = new double[samples];
        for (int s = 0; s < samples; s++) xs[s] = (double)s * ((double)xMax / (double)(samples-1));

        // colors for bots
        Color[] colors = { new Color(100,150,220), new Color(220,100,160), new Color(120,200,120), new Color(200,140,60) };

        // find max pdf value to scale vertically
        double maxPdf = 0.0;
        double[][] pdfs = new double[botTypes.length][samples];
        for (int i = 0; i < botTypes.length; i++) {
            double mu = means[i];
            double sd = sds[i];
            for (int s = 0; s < samples; s++) {
                double x = xs[s];
                double val = 0.0;
                if (sd > 0) {
                    double z = (x - mu) / sd;
                    val = (1.0 / (sd * Math.sqrt(2.0*Math.PI))) * Math.exp(-0.5*z*z);
                } else {
                    val = (x==mu) ? 1.0 : 0.0;
                }
                pdfs[i][s] = val;
                if (val > maxPdf) maxPdf = val;
            }
        }

        // draw y-axis ticks labels (density) as percentages of the maximum pdf
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        int yTicks = 5;
        for (int k = 0; k <= yTicks; k++) {
            double frac = (double)k / (double)yTicks;
            int yy = marginT + plotH - (int)(frac * plotH);
            g2.drawLine(marginL - 6, yy, marginL, yy);
            String lbl = String.format("%.3f", maxPdf * frac);
            int lw = g2.getFontMetrics().stringWidth(lbl);
            g2.drawString(lbl, marginL - 10 - lw, yy + (g2.getFontMetrics().getAscent()/2) - 2);
        }

        // draw x ticks (integer moves)
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        int xTickStep = Math.max(1, xMax / 10);
        for (int xv = 0; xv <= xMax; xv += xTickStep) {
            double frac = (double)xv / (double)xMax;
            int xx = marginL + (int)(frac * plotW);
            g2.drawLine(xx, marginT + plotH, xx, marginT + plotH + 6);
            String lbl = String.valueOf(xv);
            int sw = g2.getFontMetrics().stringWidth(lbl);
            g2.drawString(lbl, xx - sw/2, marginT + plotH + 20);
        }

        // draw curves
        for (int i = 0; i < botTypes.length; i++) {
            g2.setColor(colors[i % colors.length]);
            int prevX = -1, prevY = -1;
            for (int s = 0; s < samples; s++) {
                double fracX = xs[s] / (double)xMax;
                int px = marginL + (int)(fracX * plotW);
                double val = pdfs[i][s];
                int py = marginT + plotH - (int)((val / maxPdf) * plotH);
                if (s > 0) {
                    g2.drawLine(prevX, prevY, px, py);
                }
                prevX = px; prevY = py;
            }
        }

        // legend and summary text
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        int legendX = marginL + plotW + 10;
        int legendY = marginT + 10;
        for (int i = 0; i < botTypes.length; i++) {
            g2.setColor(colors[i % colors.length]);
            g2.fillRect(legendX, legendY + i*30, 18, 12);
            g2.setColor(Color.BLACK);
            String txt = String.format("%s — mean=%.2f sd=%.2f stderr=%.3f", labels[i], means[i], sds[i], stderrs[i]);
            g2.drawString(txt, legendX + 24, legendY + 12 + i*30);
        }

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.drawString("Gaussian fit of winner-move distributions (per bot)", marginL, marginT - 20);

        g2.dispose();
        File outPng = new File(outDir, "performance_gaussian_overlay.png");
        ImageIO.write(img2, "png", outPng);
        System.out.println("Wrote gaussian overlay plot to: " + outPng.getAbsolutePath());

        System.out.println("Wrote performance CSV to: " + csv.getAbsolutePath());
    }
}
