package bataillenavale;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import interfacegraphique.GrilleGraphique;
import interfacegraphique.GrilleNavaleGraphique;
import joueurs.Bot;
import joueurs.JoueurGraphique;
import joueurs.SmartBot;

/**
 * Classe de lancement pour le jeu de bataille navale.  Le jeu peut se
 * dérouler à deux joueurs humains ou opposer un joueur humain à un
 * bot (aléatoire ou intelligent).  La taille de la grille ainsi que
 * la configuration de la flotte peuvent être paramétrées via les
 * arguments de la ligne de commande.
 */
public class BatailleNavale {
    /** taille par défaut de la grille */
    private static final int DEFAULT_TAILLE = 10;
    /** flotte par défaut : longueurs des navires */
    private static final int[] DEFAULT_FLOTTE = {5, 4, 3, 3, 2, 2};

    /**
     * Initialise et affiche la fenêtre pour un joueur humain.  Deux
     * grilles graphiques sont placées côte à côte : celle des tirs et
     * celle des navires du joueur.
     */
    public static void initFenetre(final String titreFenetre, final GrilleGraphique grilleTir, final GrilleGraphique grilleJeu) {
        SwingUtilities.invokeLater(() -> {
            JFrame fenetre = new JFrame(titreFenetre);
            fenetre.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            fenetre.getContentPane().setLayout(new GridLayout(1, 2));
            grilleTir.setBorder(BorderFactory.createTitledBorder("Grille de tirs"));
            grilleJeu.setBorder(BorderFactory.createTitledBorder("Grille de jeu"));
            // on empêche l'utilisateur de cliquer sur sa propre grille
            grilleJeu.setClicActive(false);
            fenetre.getContentPane().add(grilleTir);
            fenetre.getContentPane().add(grilleJeu);
            fenetre.pack();
            fenetre.setVisible(true);
        });
    }

    /**
     * Initialise un joueur humain avec son interface graphique et
     * positionne automatiquement ses navires.
     */
    public static JoueurGraphique initJoueur(String nomJoueur, int taille, int[] flotte) {
        GrilleGraphique grilleAttaque = new GrilleGraphique(taille);
        GrilleNavaleGraphique grilleJoueur = new GrilleNavaleGraphique(taille);
        grilleJoueur.placementAuto(flotte);
        initFenetre(nomJoueur, grilleAttaque, grilleJoueur.getGrilleGraphique());
        return new JoueurGraphique(grilleJoueur, grilleAttaque);
    }

    /**
     * Initialise un bot intelligent ou aléatoire.  Ses navires sont
     * placés automatiquement.  Aucun affichage n'est réalisé pour le bot.
     */
    public static Bot initBot(int taille, int[] flotte, boolean intelligent) {
        GrilleNavaleGraphique grilleBot = new GrilleNavaleGraphique(taille);
        grilleBot.placementAuto(flotte);
        if (intelligent) {
            return new SmartBot(grilleBot);
        } else {
            return new Bot(grilleBot);
        }
    }

    /**
     * Point d'entrée principal.  Les arguments permettent de choisir le
     * mode de jeu et la taille de la grille :
     * <pre>
     *   java fr.uga.miashs.inff3.bataillenavale.BatailleNavale         # 2 joueurs humains
     *   java fr.uga.miashs.inff3.bataillenavale.BatailleNavale 12      # 2 joueurs sur une grille 12×12
     *   java fr.uga.miashs.inff3.bataillenavale.BatailleNavale bot     # humain vs. bot intelligent
     *   java fr.uga.miashs.inff3.bataillenavale.BatailleNavale bot 8   # humain vs. bot sur une grille 8×8
     * </pre>
     */
    public static void main(String[] args) {
        int taille = DEFAULT_TAILLE;
        int[] flotte = DEFAULT_FLOTTE;
        boolean vsBot = false;
        // parse arguments
        if (args != null && args.length > 0) {
            if (args[0].equalsIgnoreCase("bot")) {
                vsBot = true;
                if (args.length > 1) {
                    try {
                        taille = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ex) {
                        System.err.println("Taille de grille invalide : " + args[1] + ". Utilisation de la taille par défaut.");
                    }
                }
            } else {
                try {
                    taille = Integer.parseInt(args[0]);
                } catch (NumberFormatException ex) {
                    System.err.println("Argument inconnu : " + args[0] + ". Ignoré.");
                }
            }
        }
        // adapter la flotte si la grille est trop petite
        flotte = adapteFlottePourTaille(flotte, taille);
        if (vsBot) {
            JoueurGraphique joueur = initJoueur("Joueur", taille, flotte);
            Bot bot = initBot(taille, flotte, true); // always use SmartBot
            joueur.jouerAvec(bot);
        } else {
            JoueurGraphique j1 = initJoueur("Joueur 1", taille, flotte);
            JoueurGraphique j2 = initJoueur("Joueur 2", taille, flotte);
            j1.jouerAvec(j2);
        }
    }

    /**
     * Adapte un tableau de longueurs de navires pour une taille de grille
     * donnée.  Si certains navires sont trop longs pour tenir dans la
     * grille, ils sont supprimés.  Il est préférable de conserver un
     * minimum de navires pour que le jeu reste intéressant.
     */
    private static int[] adapteFlottePourTaille(int[] base, int taille) {
        int count = 0;
        for (int l : base) {
            if (l <= taille) {
				count++;
			}
        }
        int[] result = new int[count];
        int idx = 0;
        for (int l : base) {
            if (l <= taille) {
                result[idx++] = l;
            }
        }
        return result;
    }
}