package bataillenavale;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import joueurs.Bot;
import joueurs.JoueurGraphique;

/**
 * Small launcher UI to configure the grid size and choose game mode
 * (Human vs Human or Human vs Smart Bot) before starting a game.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JTextField tailleField = new JTextField(String.valueOf(10));
            String[] modes = {"Human vs Human", "Human vs Bot (Smart)"};
            JComboBox<String> modeCombo = new JComboBox<>(modes);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Grid size (integer):"));
            panel.add(tailleField);
            panel.add(new JLabel("Mode:"));
            panel.add(modeCombo);

            int res = JOptionPane.showConfirmDialog(null, panel, "Bataille Navale - Configuration",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) {
                return; // cancelled
            }

            int taille = 10;
            try {
                taille = Integer.parseInt(tailleField.getText().trim());
                if (taille < 2) taille = 2;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid size, using default 10.");
                taille = 10;
            }

            boolean vsBot = modeCombo.getSelectedIndex() == 1;
            final int[] flotte = {5, 4, 3, 3, 2, 2};

            // Adapt the fleet to the chosen grid size: remove ships longer than the grid
            int count = 0;
            for (int l : flotte) if (l <= taille) count++;
            if (count == 0) {
                // No ship fits: offer to increase the grid to the largest ship length or cancel
                int maxLen = 0;
                for (int l : flotte) if (l > maxLen) maxLen = l;
                int choice = JOptionPane.showConfirmDialog(null,
                        "No ships can fit in a " + taille + "x" + taille + " grid.\n" +
                                "Increase grid to " + maxLen + "x" + maxLen + " so default fleet can be used?",
                        "Grid too small", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.OK_OPTION) {
                    JOptionPane.showMessageDialog(null, "Aborting: choose a larger grid next time.");
                    return;
                }
                taille = maxLen;
                count = flotte.length;
            }

            // Build an adapted fleet array containing only ships that fit the chosen grid size
            int[] flotteAdapted = new int[count];
            int idx = 0;
            for (int l : flotte) {
                if (l <= taille) {
                    flotteAdapted[idx++] = l;
                }
            }

            // make local copies that are effectively final for use in the game thread
            final int tailleFinal = taille;
            final boolean vsBotFinal = vsBot;
            final int[] flotteFinal = flotteAdapted;

            // Start the game loop in a background thread to avoid blocking the EDT
            new Thread(() -> {
                if (vsBotFinal) {
                    JoueurGraphique joueur = BatailleNavale.initJoueur("Joueur", tailleFinal, flotteFinal);
                    Bot bot = BatailleNavale.initBot(tailleFinal, flotteFinal, true);
                    joueur.jouerAvec(bot);
                } else {
                    JoueurGraphique j1 = BatailleNavale.initJoueur("Joueur 1", tailleFinal, flotteFinal);
                    JoueurGraphique j2 = BatailleNavale.initJoueur("Joueur 2", tailleFinal, flotteFinal);
                    j1.jouerAvec(j2);
                }
            }, "BatailleNavale-GameThread").start();
        });
    }
}