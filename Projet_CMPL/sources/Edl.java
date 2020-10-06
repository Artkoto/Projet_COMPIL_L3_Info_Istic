import java.io.*;

/**
 * 
 * @author Akoto_Arnaud_Jacquemoud_Edgar_Douady_Paris
 * @version 2020
 *
 */

public class Edl {

	// nombre max de modules, taille max d'un code objet d'une unite
	static final int MAXMOD = 5, MAXOBJ = 1000;
	// nombres max de references externes (REF) et de points d'entree (DEF)
	// pour une unite
	private static final int MAXREF = 10, MAXDEF = 10;

	// typologie des erreurs
	private static final int FATALE = 0, NONFATALE = 1;

	// valeurs possibles du vecteur de translation
	private static final int TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;

	// table de tous les descripteurs concernes par l'edl
	static Descripteur[] tabDesc = new Descripteur[MAXMOD + 1];

	// declarations de variables
	static int ipo = 0, nMod = 0, nbErr = 0, totalGlobales = 0;
	static String nomProg;
	static String[] nomFichiers = new String[MAXMOD + 1];
	
	static int[] transDon = new int[MAXMOD + 1];
	static int[] transCode = new int[MAXMOD + 1];

	static class EltDicoDef {
		String nomProc;
		int adPo, nbParam;
	}

	static EltDicoDef[] dicoDef = new EltDicoDef[(MAXMOD + 1) * MAXDEF + 1];
	static int dicoDefTaille = 0;
	
	static int[][] adFinale = new int[MAXMOD + 1][MAXREF + 1];

	static class FichierObj {
		int[] po;
		int[] transExtType;
		int[] transExtValeurs;
	}
	
	// Recherche dans le dictionnaire
	static int presentDico(String nomProc) {
		for(int i = 1 ; i <= dicoDefTaille ; i++) {
			if (dicoDef[i].nomProc.equals(nomProc)) {
				return i;
			}
		}
		return 0;
	}
	
	// Ajout dans le dictionnaire
	static void ajoutDicoDef(EltDicoDef elt) {
		dicoDef[dicoDefTaille+1] = elt;
		dicoDefTaille += 1;
	}
	
	static FichierObj lire_po(String nom_fichier, int taille_code, int nb_transext) {
		FichierObj fo = new FichierObj();
		fo.po = new int[taille_code+1];
		fo.transExtType = new int[nb_transext];
		fo.transExtValeurs = new int[nb_transext];
		
		InputStream stream = Lecture.ouvrir(nom_fichier + ".obj");
		for(int i = 0 ; i < nb_transext ; i++) {
			fo.transExtValeurs[i] = Lecture.lireInt(stream);
			fo.transExtType[i] = Lecture.lireInt(stream);
		}
		
		for(int i = 1 ; i <= taille_code ; i++) {
			fo.po[i] = Lecture.lireInt(stream);
		}
		Lecture.fermer(stream);
		
		return fo;
	}
	
	
	// utilitaire de traitement des erreurs
	// ------------------------------------
	static void erreur(int te, String m) {
		System.out.println(m);
		if (te == FATALE) {
			System.out.println("ABANDON DE L'EDITION DE LIENS");
			System.exit(1);
		}
		nbErr = nbErr + 1;
	}

	// utilitaire de remplissage de la table des descripteurs tabDesc
	// --------------------------------------------------------------
	static void lireDescripteurs() {
		String s;
		System.out.println("les noms doivent etre fournis sans suffixe");
		System.out.print("nom du programme : ");
		s = Lecture.lireString();
		tabDesc[0] = new Descripteur();
		tabDesc[0].lireDesc(s);
		nomFichiers[0] = s;
		if (!tabDesc[0].getUnite().equals("programme"))
			erreur(FATALE, "programme attendu");
		nomProg = s;

		nMod = 0;
		while (!s.equals("") && nMod < MAXMOD) {
			System.out.print("nom de module " + (nMod + 1) + " (RC si termine) ");
			s = Lecture.lireString();
			if (!s.equals("")) {
				nMod = nMod + 1;
				tabDesc[nMod] = new Descripteur();
				tabDesc[nMod].lireDesc(s);
				nomFichiers[nMod] = s;

				if (!tabDesc[nMod].getUnite().equals("module"))
					erreur(FATALE, "module attendu");
			}
		}
	}

	static void constMap() {
		// pour construire le code concatene de toutes les unités
		int[] po = new int[(nMod + 1) * MAXOBJ + 1];
		ipo = 1;
		for(int i = 0 ; i < nMod + 1 ; i++) {
			System.out.println("Construction de " + nomFichiers[i]);
			Descripteur d = tabDesc[i];			
			int taille_code = d.getTailleCode();
			int nb_transext = d.getNbTransExt();
			FichierObj fo = lire_po(nomFichiers[i], taille_code, nb_transext);

			// On resoud les translations
			for(int j = 0 ; j < nb_transext ; j++) {
				int v = fo.transExtValeurs[j];
				switch (fo.transExtType[j]) {
				case TRANSCODE:
					fo.po[v] += transCode[i];
					break;
				case TRANSDON:
					fo.po[v] += transDon[i];
					break;
				case REFEXT: {
					int ref_id = fo.po[v];
					fo.po[v] = adFinale[i][ref_id];
				}
					break;
				default:
					erreur(FATALE, "Type de transext inconnu");
				}
			}
			
			// On concatene
			for(int j = 1 ; j <= taille_code ; j++) {
				po[ipo] = fo.po[j];
				ipo += 1;
			}
		}

		po[2] = totalGlobales; // Resolution du reserver
		
		// f2 = fichier executable .map construit
		OutputStream f2 = Ecriture.ouvrir(nomProg + ".map");
		if (f2 == null)
			erreur(FATALE, "creation du fichier " + nomProg + ".map impossible");
		
		for(int i = 1 ; i < ipo ; i++) {
			Ecriture.ecrireInt(f2, po[i]);
			Ecriture.ecrireStringln(f2);
		}
		
		Ecriture.fermer(f2);

		// creation du fichier en mnemonique correspondant
		Mnemo.creerFichier(ipo-1, po, nomProg + ".ima");
	}

	public static void main(String argv[]) {
		System.out.println("EDITEUR DE LIENS / PROJET LICENCE");
		System.out.println("---------------------------------");
		System.out.println("");
		nbErr = 0;

		// Phase 1 de l'edition de liens
		// -----------------------------
		lireDescripteurs(); 

		int transDonSomme = 0;
		int transCodeSomme = 0;
		
		for (int i = 0 ; i < nMod+1 ; i++) {
			Descripteur d = tabDesc[i];
			
			totalGlobales += d.getTailleGlobaux();
			
			transDon[i] = transDonSomme;
			transDonSomme += d.getTailleGlobaux();
			
			transCode[i] = transCodeSomme;
			transCodeSomme += d.getTailleCode();
			
			for(int j = 1 ; j <= d.getNbDef() ; j++) {
				EltDicoDef elt = new EltDicoDef();
				elt.nomProc = d.getDefNomProc(j);
				if(presentDico(elt.nomProc) != 0) {
					erreur(FATALE, "La procedure " + elt.nomProc + " est definie deux fois");
				}

				elt.adPo = d.getDefAdPo(j) + transCode[i];
				elt.nbParam = d.getDefNbParam(j);
				ajoutDicoDef(elt);
			}
		}
		
		for(int i = 0 ; i < nMod+1 ; i++) {
			Descripteur d = tabDesc[i];
			
			for(int j = 1 ; j <= d.getNbRef() ; j++) {
				String nom = d.getRefNomProc(j);
				int def_id = presentDico(nom);
				if (def_id == 0) {
					erreur(FATALE, "procedure ref " + nom + " definie nulle part en tant que def");
				}
				EltDicoDef def = dicoDef[def_id];
				if(d.getRefNbParam(j) != def.nbParam) {
					erreur(FATALE, "procedure ref " + nom + " n'a pas le meme nombre de parametre que la def");					
				}
				adFinale[i][j] = def.adPo;
			}
		}
		
		if (nbErr > 0) {
			System.out.println("programme executable non produit");
			System.exit(1);
		}
		
		System.out.println("Calcul de transDonc, transCode et adFinale OK");

		// Phase 2 de l'edition de liens
		// -----------------------------
		constMap();
		System.out.println("Edition de liens terminee");
	}
}
