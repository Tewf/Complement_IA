package joueurs;

import java.util.ArrayDeque;
import java.util.Deque;

import interfacegraphique.GrilleNavaleGraphique;
import logique.Coordonnee;

/**
 * Bot intelligent qui applique une stratégie “hunt & target”.  Lorsqu'un
 * tir touche un navire sans le couler, le bot ajoute les cases voisines à
 * une file d'attente de cibles prioritaires.  Tant que cette file n'est
 * pas vide, le bot l'utilise avant de choisir aléatoirement une nouvelle
 * case.  Lorsque le navire ciblé est coulé, la file est vidée.
 */
public class SmartBot extends Bot {
    private final Deque<Coordonnee> cibles = new ArrayDeque<>();

    public SmartBot(GrilleNavaleGraphique gng) {
        super(gng);
    }

    @Override
    protected void retourAttaque(Coordonnee c, int etat) {
        if (etat == TOUCHE || etat == COULE || etat == GAMEOVER) {
            // remplir la liste des cibles adjacentes si l'on a touché quelque chose
            int[][] directions = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };
            for (int[] d : directions) {
                int nl = c.getLigne() + d[0];
                int nc = c.getColonne() + d[1];
                if (nl >= 0 && nl < gng.getTaille() && nc >= 0 && nc < gng.getTaille()) {
                    if (!tirsEnvoyes[nl][nc]) {
                        cibles.addLast(new Coordonnee(nl, nc));
                    }
                }
            }
            // si on a coulé le navire, on vide la liste car les cibles sont obsolètes
            if (etat == COULE || etat == GAMEOVER) {
                cibles.clear();
            }
        }
    }

    @Override
    public Coordonnee choisirAttaque() {
        // tant qu'il reste des cibles en file d'attente, on les joue en priorité
        while (!cibles.isEmpty()) {
            Coordonnee next = cibles.removeFirst();
            if (!tirsEnvoyes[next.getLigne()][next.getColonne()]) {
                tirsEnvoyes[next.getLigne()][next.getColonne()] = true;
                return next;
            }
        }
        // sinon utiliser la stratégie aléatoire du bot de base
        return super.choisirAttaque();
    }
}