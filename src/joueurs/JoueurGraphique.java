package joueurs;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import interfacegraphique.GrilleGraphique;
import interfacegraphique.GrilleNavaleGraphique;
import logique.Coordonnee;

/**
 * Implémentation d'un joueur humain avec interface graphique. Le joueur
 * dispose de deux grilles : une pour ses navires et une autre pour suivre
 * ses attaques contre l'adversaire. Les actions de l'adversaire et les
 * résultats d'attaque sont signalés via des boîtes de dialogue.
 */
public class JoueurGraphique extends Joueur {
    private final GrilleNavaleGraphique gng;
    private final GrilleGraphique gg;
    private final Set<Coordonnee> attaques;

    public JoueurGraphique(GrilleNavaleGraphique gng, GrilleGraphique gg) {
        super(gng.getTaille());
        this.gng = gng;
        this.gg = gg;
        this.attaques = new HashSet<>();
    }

    @Override
    protected void retourAttaque(Coordonnee c, int etat) {
        switch (etat) {
            case TOUCHE:
                gg.colorie(c, Color.RED);
                JOptionPane.showMessageDialog(gg, "Bateau touché en " + c);
                break;
            case COULE:
                gg.colorie(c, Color.RED);
                JOptionPane.showMessageDialog(gg, "Bateau coulé en " + c);
                break;
            case A_L_EAU:
                gg.colorie(c, Color.BLUE);
                JOptionPane.showMessageDialog(gg, "À l'eau en " + c);
                break;
            case GAMEOVER:
                gg.colorie(c, Color.RED);
                JOptionPane.showMessageDialog(gg, "PARTIE TERMINÉE ! Vous avez gagné !");
                break;
        }
    }

    @Override
    protected void retourDefense(Coordonnee c, int etat) {
        switch (etat) {
            case TOUCHE:
                JOptionPane.showMessageDialog(gng.getGrilleGraphique(), "Votre navire a été touché en " + c);
                break;
            case COULE:
                JOptionPane.showMessageDialog(gng.getGrilleGraphique(), "Votre navire a été coulé en " + c);
                break;
            case A_L_EAU:
                JOptionPane.showMessageDialog(gng.getGrilleGraphique(), "L'attaque a manqué en " + c);
                break;
            case GAMEOVER:
                JOptionPane.showMessageDialog(gng.getGrilleGraphique(), "PARTIE TERMINÉE ! Vous avez perdu.");
                break;
        }
    }

    @Override
    public Coordonnee choisirAttaque() {
        Coordonnee c;
        do {
            c = gg.getCoordonneeSelectionnee();
            if (attaques.contains(c)) {
                JOptionPane.showMessageDialog(gg, "Vous avez déjà attaqué " + c + ". Choisissez une autre case.");
            }
        } while (attaques.contains(c));
        attaques.add(c);
        return c;
    }

    @Override
    public int defendre(Coordonnee c) {
        if (gng.estALEau(c)) {
            gng.recoitTir(c);
            return A_L_EAU;
        } else if (gng.recoitTir(c)) {
            if (gng.estCoule(c)) {
                if (gng.perdu()) {
                    return GAMEOVER;
                }
                return COULE;
            }
            return TOUCHE;
        }
        return A_L_EAU;
    }
}