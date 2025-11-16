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
 * Interface de lancement pour configurer la taille de la grille et
 * choisir le mode de jeu (Humain vs Humain ou Humain vs Ordinateur)
 * avant de démarrer une partie.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JTextField tailleField = new JTextField(String.valueOf(10));
            String[] opponents = {"Humain", "Ordinateur (Uniforme)", "Ordinateur (Markov)", "Ordinateur (MonteCarlo)"};
            JComboBox<String> opponentCombo = new JComboBox<>(opponents);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Taille de la grille (entier) :"));
            panel.add(tailleField);
            panel.add(new JLabel("Adversaire :"));
            panel.add(opponentCombo);

            int res = JOptionPane.showConfirmDialog(null, panel, "Bataille Navale - Configuration",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) {
                return; // annulé
            }

            int taille = 10;
            try {
                taille = Integer.parseInt(tailleField.getText().trim());
                if (taille < 2) taille = 2;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Taille invalide, utilisation de la valeur par défaut : 10.");
                taille = 10;
            }

            String opponent = opponentCombo.getSelectedItem().toString();
            boolean vsBot = !opponent.equalsIgnoreCase("Humain");
            final int[] flotte = {5, 4, 3, 3, 2, 2};

            // Adapter la flotte à la taille choisie : supprimer les navires plus longs que la grille
            int count = 0;
            for (int l : flotte) if (l <= taille) count++;
            if (count == 0) {
                // Aucun navire ne tient : proposer d'augmenter la grille à la plus grande longueur ou annuler
                int maxLen = 0;
                for (int l : flotte) if (l > maxLen) maxLen = l;
                int choice = JOptionPane.showConfirmDialog(null,
                    "Aucun navire ne peut tenir dans une grille de " + taille + "x" + taille + ".\n" +
                        "Augmenter la taille de la grille à " + maxLen + "x" + maxLen + " pour utiliser la flotte par défaut ?",
                    "Grille trop petite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.OK_OPTION) {
                    JOptionPane.showMessageDialog(null, "Abandon : choisissez une grille plus grande la prochaine fois.");
                    return;
                }
                taille = maxLen;
                count = flotte.length;
            }

            // Construire un tableau de flotte adapté contenant uniquement les navires qui tiennent
            int[] flotteAdapted = new int[count];
            int idx = 0;
            for (int l : flotte) {
                if (l <= taille) {
                    flotteAdapted[idx++] = l;
                }
            }

            // Faire des copies locales effectivement finales pour le thread de jeu
            final int tailleFinal = taille;
            final boolean vsBotFinal = vsBot;
            final int[] flotteFinal = flotteAdapted;

            // Démarrer la boucle de jeu dans un thread en arrière-plan pour ne pas bloquer l'EDT
            new Thread(() -> {
                if (vsBotFinal) {
                    JoueurGraphique joueur = BatailleNavale.initJoueur("Joueur", tailleFinal, flotteFinal);
                    // Mapper l'adversaire sélectionné vers un type de bot (chaine interne)
                    String botType = "smart";
                    if (opponent.equalsIgnoreCase("Ordinateur (Markov)")) botType = "markov";
                    else if (opponent.equalsIgnoreCase("Ordinateur (MonteCarlo)")) botType = "montecarlo";
                    else if (opponent.equalsIgnoreCase("Ordinateur (Uniforme)")) botType = "uniform";
                    Bot bot = BatailleNavale.initBot(tailleFinal, flotteFinal, botType);
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