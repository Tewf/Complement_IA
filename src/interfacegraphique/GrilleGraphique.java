package interfacegraphique;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import logique.Coordonnee;

/**
 * Représentation graphique d'une grille. Chaque case est un bouton
 * cliquable. Des couleurs sont utilisées pour indiquer les tirs à
 * l'eau, les touches et les navires placés (pour la grille du joueur).
 */
class JButtonCoordonnee extends JButton {
    private final Coordonnee c;
    public JButtonCoordonnee(Coordonnee c) {
        super();
        this.c = c;
    }
    public Coordonnee getCoordonnee() {
        return c;
    }
}

public class GrilleGraphique extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final JButton[][] cases;
    private Coordonnee coordonneeSelectionnee;

    /**
     * Construit une grille graphique de la taille donnée. La grille
     * comporte une ligne et une colonne supplémentaires pour afficher les
     * en-têtes (lettres en haut et numéros de ligne à gauche).
     */
    public GrilleGraphique(int taille) {
        this.setLayout(new GridLayout(taille + 1, taille + 1));
        // coin supérieur gauche vide
        this.add(new JLabel());
        // en‑têtes de colonnes (A, B, C, ...)
        for (int i = 0; i < taille; i++) {
            JLabel lbl = new JLabel(String.valueOf((char) ('A' + i)));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            this.add(lbl);
        }
        cases = new JButton[taille][taille];
        for (int i = 0; i < taille; i++) {
            // en‑tête de ligne (1, 2, 3, ...)
            JLabel lbl = new JLabel(String.valueOf(i + 1));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            this.add(lbl);
            for (int j = 0; j < taille; j++) {
                cases[i][j] = new JButtonCoordonnee(new Coordonnee(i, j));
                this.add(cases[i][j]);
                cases[i][j].addActionListener(this);
            }
        }
    }

    /**
     * Colore une seule case avec la couleur fournie. Une vérification de
     * bornes est effectuée pour éviter des IndexOutOfBounds.
     */
    public void colorie(Coordonnee cord, Color color) {
        int row = cord.getLigne();
        int col = cord.getColonne();
        if (row >= 0 && row < cases.length && col >= 0 && col < cases[row].length) {
            cases[row][col].setBackground(color);
        }
    }

    /**
     * Colore toutes les cases entre deux coordonnées (incluses) avec la
     * couleur fournie. Utilisé pour la visualisation du placement automatique.
     */
    public void colorie(Coordonnee debut, Coordonnee fin, Color color) {
        for (int i = debut.getLigne(); i <= fin.getLigne(); i++) {
            for (int j = debut.getColonne(); j <= fin.getColonne(); j++) {
                if (i >= 0 && i < cases.length && j >= 0 && j < cases[i].length) {
                    cases[i][j].setBackground(color);
                }
            }
        }
    }

    /**
     * Active ou désactive tous les boutons de la grille. Utilise
     * `invokeLater` pour s'assurer que les mises à jour ont lieu sur le
     * thread d'affichage (Event Dispatch Thread).
     */
    public void setClicActive(boolean active) {
        SwingUtilities.invokeLater(() -> {
            for (JButton[] ligne : cases) {
                for (JButton bt : ligne) {
                    bt.setEnabled(active);
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // désactiver immédiatement pour éviter les doubles clics
        this.setClicActive(false);
        coordonneeSelectionnee = ((JButtonCoordonnee) e.getSource()).getCoordonnee();
        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
     * Bloque jusqu'à ce qu'une case soit cliquée puis retourne la coordonnée
     * sélectionnée. La grille est réactivée automatiquement avant le blocage.
     */
    public synchronized Coordonnee getCoordonneeSelectionnee() {
        this.setClicActive(true);
        try {
            this.wait();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return coordonneeSelectionnee;
    }
}