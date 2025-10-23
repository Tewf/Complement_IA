package joueurs;

import java.util.Random;

import interfacegraphique.GrilleNavaleGraphique;
import logique.Coordonnee;

/**
 * Bot basique qui attaque des cases aléatoires jusqu'à ce qu'il trouve
 * toutes les cibles.  Ce bot ne tient pas compte de l'historique des
 * touches autres que pour éviter de rejouer la même case.
 */
public class Bot extends Joueur {
    protected final GrilleNavaleGraphique gng;
    protected final boolean[][] tirsEnvoyes;
    private final Random rand = new Random();

    public Bot(GrilleNavaleGraphique gng) {
        super(gng.getTaille());
        this.gng = gng;
        int taille = gng.getTaille();
        this.tirsEnvoyes = new boolean[taille][taille];
    }

    @Override
    protected void retourAttaque(Coordonnee c, int etat) {
        // le bot basique n'a pas de retour d'information à traiter
    }

    @Override
    protected void retourDefense(Coordonnee c, int etat) {
        // idem : aucune action nécessaire après avoir été attaqué
    }

    @Override
    public Coordonnee choisirAttaque() {
        int taille = gng.getTaille();
        int ligne, colonne;
        do {
            ligne = rand.nextInt(taille);
            colonne = rand.nextInt(taille);
        } while (tirsEnvoyes[ligne][colonne]);
        tirsEnvoyes[ligne][colonne] = true;
        return new Coordonnee(ligne, colonne);
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