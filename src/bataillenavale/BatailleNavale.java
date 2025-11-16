package bataillenavale;

import java.awt.GridLayout;
import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import interfacegraphique.GrilleGraphique;
import interfacegraphique.GrilleNavaleGraphique;
import joueurs.Bot;
import joueurs.JoueurGraphique;
import joueurs.SmartBot;

/**
 * Classe utilitaire et point d'entrée pour le jeu de Bataille Navale.
 *
 * L'application supporte le jeu humain vs humain et humain vs bot. La taille
 * de la grille et la flotte par défaut peuvent être ajustées via les
 * arguments de la ligne de commande. Cette classe fournit également des
 * méthodes de commodité pour initialiser les fenêtres graphiques, les
 * joueurs humains et les bots.
 */
public class BatailleNavale {
    /** Taille par défaut de la grille. */
    private static final int DEFAULT_TAILLE = 10;
    /** Flotte par défaut : longueurs des navires. */
    private static final int[] DEFAULT_FLOTTE = {5, 4, 3, 3, 2, 2};

    /**
     * Initialise et affiche la fenêtre principale pour un joueur humain.
     * Deux composants de grille sont placés côte à côte : la grille d'attaque
     * et la grille du joueur.
     */
    public static void initFenetre(final String titreFenetre, final GrilleGraphique grilleTir, final GrilleGraphique grilleJeu) {
        SwingUtilities.invokeLater(() -> {
            JFrame fenetre = new JFrame(titreFenetre);
            fenetre.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            // panneau principal : grille de tirs + panneau droit (contrôles + grille de jeu)
            JPanel principal = new JPanel(new GridLayout(1, 2));

            // panneau droit : conteneur pour les contrôles et la grille de jeu
            JPanel droite = new JPanel(new java.awt.BorderLayout());

            // zone de contrôles en haut : checkbox pour afficher/masquer la grille
            JCheckBox voirGrille = new JCheckBox("Voir ma grille");
            voirGrille.setToolTipText("Cochez pour afficher votre grille de jeu. Par défaut la grille est masquée pour empêcher l'adversaire de voir vos navires.");
            JButton reveal3s = new JButton("Révéler 3s");

            // créer un petit panneau de contrôles
            JPanel controles = new JPanel(new GridLayout(1, 2));
            controles.add(voirGrille);
            controles.add(reveal3s);

            // conteneur pour la grille du joueur afin de pouvoir la masquer facilement
            JPanel containerJeu = new JPanel(new BorderLayout());
            containerJeu.add(grilleJeu, java.awt.BorderLayout.CENTER);

            // config visuelle
            grilleTir.setBorder(BorderFactory.createTitledBorder("Grille de tirs"));
            containerJeu.setBorder(BorderFactory.createTitledBorder("Grille de jeu"));

            // par défaut, masquer la grille de jeu (empêche de voir les positions adverses)
            containerJeu.setVisible(false);

            // empêcher les clics sur la propre grille du joueur (la sélection d'attaque se fait via la grille de tirs)
            grilleJeu.setClicActive(false);

            // actions pour les contrôles
            voirGrille.addActionListener(e -> containerJeu.setVisible(voirGrille.isSelected()));

            reveal3s.addActionListener(e -> {
                // afficher temporairement 3 secondes
                containerJeu.setVisible(true);
                // démarrer un Timer Swing pour restaurer l'état
                new javax.swing.Timer(3000, ev -> {
                    containerJeu.setVisible(voirGrille.isSelected());
                    ((javax.swing.Timer) ev.getSource()).stop();
                }).start();
            });

            // assembly
            principal.add(grilleTir);
            droite.add(controles, java.awt.BorderLayout.NORTH);
            droite.add(containerJeu, java.awt.BorderLayout.CENTER);
            principal.add(droite);

            fenetre.getContentPane().add(principal);
            fenetre.pack();
            fenetre.setVisible(true);
        });
    }

    /**
     * Initialise un joueur humain avec une interface graphique et un placement
     * automatique des navires.
     */
    public static JoueurGraphique initJoueur(String nomJoueur, int taille, int[] flotte) {
        GrilleGraphique grilleAttaque = new GrilleGraphique(taille);
        GrilleNavaleGraphique grilleJoueur = new GrilleNavaleGraphique(taille);
        grilleJoueur.placementAuto(flotte);
        initFenetre(nomJoueur, grilleAttaque, grilleJoueur.getGrilleGraphique());
        return new JoueurGraphique(grilleJoueur, grilleAttaque);
    }

    /**
     * Initialise un bot (intelligent ou uniforme). Les navires sont placés
     * automatiquement. Aucune interface graphique n'est créée pour les bots.
     */
    public static Bot initBot(int taille, int[] flotte, boolean intelligent) {
        // kept for backward compatibility: delegate to string-based init
        return initBot(taille, flotte, intelligent ? "markov" : "uniform");
    }

    /**
     * Initialise un bot à partir d'une étiquette de type : "uniform", "markov",
     * "montecarlo" ou "smart".
     */
    public static Bot initBot(int taille, int[] flotte, String botType) {
        GrilleNavaleGraphique grilleBot = new GrilleNavaleGraphique(taille);
        grilleBot.placementAuto(flotte);
        if (botType == null) botType = "uniform";
        String t = botType.trim().toLowerCase();
        switch (t) {
            case "montecarlo":
                return new SmartBot(grilleBot, "MonteCarlo");
            case "markov":
            case "markow":
                return new SmartBot(grilleBot, "Markov");
            case "smart":
                return new SmartBot(grilleBot);
            case "uniform":
            default:
                return new Bot(grilleBot);
        }
    }

    /**
     * Point d'entrée principal. Les arguments de la ligne de commande permettent
     * de choisir le mode de jeu et la taille de la grille. Exemples :
     * <pre>
     *   java bataillenavale.BatailleNavale         # deux joueurs humains
     *   java bataillenavale.BatailleNavale 12      # deux humains sur une grille 12x12
     *   java bataillenavale.BatailleNavale bot     # humain vs bot intelligent
     *   java bataillenavale.BatailleNavale bot 8   # humain vs bot sur une grille 8x8
     * </pre>
     */
    public static void main(String[] args) {
        int taille = DEFAULT_TAILLE;
        int[] flotte = DEFAULT_FLOTTE;
        boolean vsBot = true;
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
        // adapter la flotte si la grille choisie est trop petite
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
     * Adapte un tableau de longueurs de navires à une taille de grille
     * donnée. Les navires dont la longueur dépasse la taille de la grille
     * sont supprimés du tableau retourné.
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