package bataillenavale;

import logique.Coordonnee;
import logique.GrilleNavale;
import logique.Navire;

/**
 * Petite batterie de tests unitaires rudimentaires pour vérifier que les
 * fonctionnalités de base fonctionnent toujours après les améliorations.
 * On utilise des assertions simples sans bibliothèque de tests externe.
 */
public class TestBatailleNavale {
    public static void main(String[] args) {
        testCoordonnee();
        testNavire();
        testGrilleNavale();
        System.out.println("Tous les tests réussis !");
    }

    private static void testCoordonnee() {
        System.out.println("Test de Coordonnee...");
        Coordonnee c1 = new Coordonnee("A1");
        assert c1.getLigne() == 0 : "La ligne doit être 0 pour A1";
        assert c1.getColonne() == 0 : "La colonne doit être 0 pour A1";

        Coordonnee c2 = new Coordonnee(1, 2);
        assert c2.toString().equals("C2") : "La représentation en chaîne doit être C2";

        assert c1.voisine(new Coordonnee(0, 1)) : "A1 doit être voisine de B1";
        assert !c1.voisine(new Coordonnee(2, 2)) : "A1 ne doit pas être voisine de C3";
        assert c1.equals(new Coordonnee("A1")) : "A1 doit être égale à une autre A1";
        assert !c1.equals(c2) : "A1 ne doit pas être égale à C2";
        System.out.println("Tests Coordonnee réussis !");
    }

    private static void testNavire() {
        System.out.println("Test de Navire...");
        Navire navire = new Navire(new Coordonnee(0, 0), 3, true); // vertical
        assert navire.getDebut().equals(new Coordonnee(0, 0)) : "Le début doit être A1";
        assert navire.getFin().equals(new Coordonnee(2, 0)) : "La fin doit être A3";
        assert navire.contient(new Coordonnee(1, 0)) : "Le navire doit contenir B1";
        assert !navire.contient(new Coordonnee(3, 0)) : "Le navire ne doit pas contenir D1";
        assert navire.recoitTir(new Coordonnee(1, 0)) : "B1 doit être un coup valide";
        assert !navire.recoitTir(new Coordonnee(1, 0)) : "B1 ne doit pas être enregistré comme touche à nouveau";
        assert navire.estTouche() : "Le navire doit être marqué comme touché après un tir";
        assert !navire.estCoule() : "Le navire ne doit pas être coulé pour l'instant";
        navire.recoitTir(new Coordonnee(0, 0));
        navire.recoitTir(new Coordonnee(2, 0));
        assert navire.estCoule() : "Le navire doit être coulé après que toutes les parties aient été touchées";
        System.out.println("Tests Navire réussis !");
    }

    private static void testGrilleNavale() {
        System.out.println("Test de GrilleNavale...");
        GrilleNavale grille = new GrilleNavale(5);
        Navire navire1 = new Navire(new Coordonnee(0, 0), 3, true);
        Navire navire2 = new Navire(new Coordonnee(1, 1), 2, false);
        assert grille.ajouteNavire(navire1) : "Navire1 doit être ajouté avec succès";
        assert !grille.ajouteNavire(navire1) : "Navire1 ne doit pas être ajouté de nouveau";
        assert grille.ajouteNavire(navire2) : "Navire2 doit être ajouté avec succès";
        assert !grille.ajouteNavire(new Navire(new Coordonnee(0, 0), 4, true)) : "Les navires qui se chevauchent ne doivent pas être ajoutés";
        assert grille.estDansGrille(new Coordonnee(4, 4)) : "4,4 doit être à l'intérieur de la grille";
        assert !grille.estDansGrille(new Coordonnee(5, 5)) : "5,5 doit être en dehors de la grille";
        assert grille.recoitTir(new Coordonnee(0, 0)) : "A1 doit être un coup valide";
        assert !grille.recoitTir(new Coordonnee(0, 0)) : "Un deuxième tir sur A1 doit retourner false";
        assert !grille.recoitTir(new Coordonnee(0, 1)) : "A2 doit être un tir à l'eau";
        assert !grille.perdu() : "La grille ne doit pas être perdue encore";
        grille.recoitTir(new Coordonnee(1, 0));
        grille.recoitTir(new Coordonnee(2, 0)); // sink navire1
        grille.recoitTir(new Coordonnee(1, 1));
        grille.recoitTir(new Coordonnee(1, 2)); // sink navire2
        assert grille.perdu() : "La grille doit être perdue après que tous les navires soient coulés";
        System.out.println("Tests GrilleNavale réussis !");
    }
}