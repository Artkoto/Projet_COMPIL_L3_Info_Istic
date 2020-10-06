
/*********************************************************************************
 * VARIABLES ET METHODES FOURNIES PAR LA CLASSE UtilLex (cf libClass_Projet)     *
 *       complement à l'ANALYSEUR LEXICAL produit par ANTLR                      *
 *                                                                               *
 *                                                                               *
 *   nom du programme compile, sans suffixe : String UtilLex.nomSource           *
 *   ------------------------                                                    *
 *                                                                               *
 *   attributs lexicaux (selon items figurant dans la grammaire):                *
 *   ------------------                                                          *
 *     int UtilLex.valEnt = valeur du dernier nombre entier lu (item nbentier)   *
 *     int UtilLex.numIdCourant = code du dernier identificateur lu (item ident) *
 *                                                                               *
 *                                                                               *
 *   methodes utiles :                                                           *
 *   ---------------                                                             *
 *     void UtilLex.messErr(String m)  affichage de m et arret compilation       *
 *     String UtilLex.chaineIdent(int numId) delivre l'ident de codage numId     *
 *     void afftabSymb()  affiche la table des symboles                          *
 *********************************************************************************/

/**
 * classe de mise en oeuvre du compilateur
 * ======================================= (verifications semantiques +
 * production du code objet)
 * 
 * @author Arnaud Akoto, Paris Douady, Jacquemoud Edgar
 *
 */

public class PtGen {

	// constantes manipulees par le compilateur
	// ----------------------------------------

	private static final int

	// taille max de la table des symboles
	MAXSYMB = 300,

			// codes MAPILE :
			RESERVER = 1, EMPILER = 2, CONTENUG = 3, AFFECTERG = 4, OU = 5, ET = 6, NON = 7, INF = 8, INFEG = 9,
			SUP = 10, SUPEG = 11, EG = 12, DIFF = 13, ADD = 14, SOUS = 15, MUL = 16, DIV = 17, BSIFAUX = 18,
			BINCOND = 19, LIRENT = 20, LIREBOOL = 21, ECRENT = 22, ECRBOOL = 23, ARRET = 24, EMPILERADG = 25,
			EMPILERADL = 26, CONTENUL = 27, AFFECTERL = 28, APPEL = 29, RETOUR = 30,

			// codes des valeurs vrai/faux
			VRAI = 1, FAUX = 0,

			// types permis :
			ENT = 1, BOOL = 2, NEUTRE = 3,

			// categories possibles des identificateurs :
			CONSTANTE = 1, VARGLOBALE = 2, VARLOCALE = 3, PARAMFIXE = 4, PARAMMOD = 5, PROC = 6, DEF = 7, REF = 8,
			PRIVEE = 9,

			// valeurs possible du vecteur de translation
			TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;

	// utilitaires de controle de type
	// -------------------------------
	/**
	 * verification du type entier de l'expression en cours de compilation (arret de
	 * la compilation sinon)
	 */
	private static void verifEnt() {
		if (tCour != ENT)
			UtilLex.messErr("expression entiere attendue");
	}

	/**
	 * verification du type booleen de l'expression en cours de compilation (arret
	 * de la compilation sinon)
	 */
	private static void verifBool() {
		if (tCour != BOOL)
			UtilLex.messErr("expression booleenne attendue");
	}

	// pile pour gerer les chaines de reprise et les branchements en avant
	// -------------------------------------------------------------------

	private static TPileRep pileRep;

	// production du code objet en memoire
	// -----------------------------------

	private static ProgObjet po;

	// COMPILATION SEPAREE
	// -------------------
	//
	/**
	 * modification du vecteur de translation associe au code produit +
	 * incrementation attribut nbTransExt du descripteur NB: effectue uniquement si
	 * c'est une reference externe ou si on compile un module
	 * 
	 * @param valeur
	 *            : TRANSDON, TRANSCODE ou REFEXT
	 */
	private static void modifVecteurTrans(int valeur) {
		if (valeur == REFEXT || desc.getUnite().equals("module")) {
			po.vecteurTrans(valeur);
			desc.incrNbTansExt();
		}
	}

	// descripteur associe a un programme objet (compilation separee)
	private static Descripteur desc;

	// autres variables fournies
	// -------------------------

	// MERCI de renseigner ici un nom pour le trinome, constitue EXCLUSIVEMENT DE
	// LETTRES
	public static String trinome = "Akoto_Jacquemoud_Douady";

	private static int tCour; // type de l'expression compilee
	private static int vCour; // sert uniquement lors de la compilation d'une valeur (entiere ou boolenne)

	// TABLE DES SYMBOLES
	// ------------------
	//
	private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];

	// it = indice de remplissage de tabSymb
	// bc = bloc courant (=1 si le bloc courant est le programme principal)
	private static int it, bc;

	/**
	 * utilitaire de recherche de l'ident courant (ayant pour code
	 * UtilLex.numIdCourant) dans tabSymb
	 * 
	 * @param borneInf
	 *            : recherche de l'indice it vers borneInf (=1 si recherche dans
	 *            tout tabSymb)
	 * @return : indice de l'ident courant (de code UtilLex.numIdCourant) dans
	 *         tabSymb (O si absence)
	 */
	private static int presentIdent(int borneInf) {
		int i = it;
		while (i >= borneInf && tabSymb[i].code != UtilLex.numIdCourant)
			i--;
		if (i >= borneInf)
			return i;
		else
			return 0;
	}

	/**
	 * utilitaire de placement des caracteristiques d'un nouvel ident dans tabSymb
	 * 
	 * @param code
	 *            : UtilLex.numIdCourant de l'ident
	 * @param cat
	 *            : categorie de l'ident parmi CONSTANTE, VARGLOBALE, PROC, etc.
	 * @param type
	 *            : ENT, BOOL ou NEUTRE
	 * @param info
	 *            : valeur pour une constante, ad d'exécution pour une variable,
	 *            etc.
	 */
	private static void placeIdent(int code, int cat, int type, int info) {
		if (it == MAXSYMB)
			UtilLex.messErr("debordement de la table des symboles");
		it = it + 1;
		tabSymb[it] = new EltTabSymb(code, cat, type, info);
	}

	/**
	 * utilitaire d'affichage de la table des symboles
	 */
	private static void afftabSymb() {
		System.out.println("       code           categorie      type    info");
		System.out.println("      |--------------|--------------|-------|----");
		for (int i = 1; i <= it; i++) {
			if (i == bc) {
				System.out.print("bc=");
				Ecriture.ecrireInt(i, 3);
			} else if (i == it) {
				System.out.print("it=");
				Ecriture.ecrireInt(i, 3);
			} else
				Ecriture.ecrireInt(i, 6);
			if (tabSymb[i] == null)
				System.out.println(" reference NULL");
			else
				System.out.println(" " + tabSymb[i]);
		}
		System.out.println();
	}

	/**
	 * initialisations A COMPLETER SI BESOIN -------------------------------------
	 */
	public static void initialisations() {

		// indices de gestion de la table des symboles
		it = 0;
		bc = 1;

		// pile des reprises pour compilation des branchements en avant
		pileRep = new TPileRep();

		// programme objet = code Mapile de l'unite en cours de compilation
		po = new ProgObjet();

		// COMPILATION SEPAREE: desripteur de l'unite en cours de compilation
		desc = new Descripteur();

		// initialisation necessaire aux attributs lexicaux
		UtilLex.initialisation();

		// initialisation du type de l'expression courante
		tCour = NEUTRE;

		compteurGlobales = 0;
		compteurReserver = 0;
		compteurArgsRef = 0;
		compteurDeclProc = 0;
		compteurAppelProc = 0;
		affectId = 0;
		appelId = 0;
		appelNParams = 0;
	} // initialisations

	private static int compteurGlobales; // Compte le nombre de variables globales
	private static int compteurReserver; // Compte le nombre de variables a reserver
	private static int compteurArgsRef; // Compte le nombres d'arguments a chaque declaration de ref
	private static int compteurDeclProc; // Compte le nombre de variable locales dans une procedure
	private static int compteurAppelProc; // Compte le nombre d'arguments passés dans un appel
	private static int affectId; // id de la variable a assigner
	private static int appelId; // id de la procedure a appeler
	private static int appelNParams; // se rappel du nombre de parametres d'une procedure lors d'un appel

	/**
	 * Transforme un type en string pour les messages d'erreurs par exemple ENT ->
	 * "ent"
	 * 
	 * @param type
	 *            le type a transformer
	 * @return le type sous sa représentation en liste chainée
	 */
	private static String chaineType(int type) {
		switch (type) {
		case ENT:
			return "ent";
		case BOOL:
			return "bool";
		case NEUTRE:
			return "neutre";
		default:
			return "invalid type (" + type + ")";
		}
	}

	/**
	 * code des points de generation A COMPLETER
	 * -----------------------------------------
	 * 
	 * @param numGen
	 *            : numero du point de generation a executer
	 */
	public static void pt(int numGen) {

		switch (numGen) {
		case 0:
			initialisations();
			break;

		/* Unite programme */
		case 420:
			desc.setUnite("programme");
			break;

		/* Arret d'un programme */
		case 440:
			po.produire(ARRET);
			break;

		/* Unite module */
		case 480:
			desc.setUnite("module");
			break;

		/* BINCOND initial avant les procedures */
		case 530:
			if (desc.getUnite() == "programme") {
				po.produire(BINCOND);
				po.produire(-1);
				pileRep.empiler(po.getIpo()); // On retient le trou du premier bincond
			}
			break;

		/* resolution du bincond initial */
		case 531:
			if (desc.getUnite() == "programme") {
				po.modifier(pileRep.depiler(), po.getIpo() + 1);
			}
			break;

		/* resolution n variables globales */
		case 532:
			desc.setTailleGlobaux(compteurGlobales);
			break;
		/* partie def */
		case 570: {
			String s = UtilLex.chaineIdent(UtilLex.numIdCourant);
			if (desc.presentDef(s) != 0) {
				UtilLex.messErr("def en double: " + s);
			}
			desc.ajoutDef(s);
		}
			break;

		/* partie ref */
		case 630: {
			String s = UtilLex.chaineIdent(UtilLex.numIdCourant);
			if (desc.presentDef(s) != 0) {
				UtilLex.messErr("ref en double: " + s);
			}
			desc.ajoutRef(s);
			placeIdent(UtilLex.numIdCourant, PROC, NEUTRE, desc.getNbRef());
			placeIdent(-1, REF, NEUTRE, 0);
			compteurArgsRef = 0;
		}
			break;

		/* ref specification paramfixe */
		case 631:
			placeIdent(-1, PARAMFIXE, tCour, compteurArgsRef);
			compteurArgsRef++;
			break;

		/* ref specification parammod */
		case 640:
			placeIdent(-1, PARAMMOD, tCour, compteurArgsRef);
			compteurArgsRef++;
			break;

		/* finalisation declaration ref */
		case 641:
			desc.modifRefNbParam(desc.getNbRef(), compteurArgsRef);
			tabSymb[it - compteurArgsRef].info = compteurArgsRef;
			break;

		/* consts */
		case 670:
			placeIdent(UtilLex.numIdCourant, CONSTANTE, tCour, vCour);
			break;

		/* vars */
		case 700:
			compteurReserver = 0;
			break;

		case 701:
			if (presentIdent(bc) != 0) {
				UtilLex.messErr("Variable " + UtilLex.chaineIdent(UtilLex.numIdCourant) + " deja declaree.");
				break;
			}
			if (bc <= 1) {
				placeIdent(UtilLex.numIdCourant, VARGLOBALE, tCour, compteurGlobales);
				compteurGlobales++;
			} else {
				placeIdent(UtilLex.numIdCourant, VARLOCALE, tCour, compteurDeclProc);
				compteurDeclProc++;
			}
			compteurReserver++;
			break;

		case 702:
			if ((desc.getUnite() == "programme" || bc > 1) // On ne genere pas de reserver pour les globales des modules
					&& compteurReserver > 0) {
				po.produire(RESERVER);
				po.produire(compteurReserver);
			}
			break;

		/* type */
		case 730:
			tCour = ENT;
			break;

		case 740:
			tCour = BOOL;
			break;

		/* decproc */

		case 800: {// Initialisation de la procedure
			if (presentIdent(1) != 0) {
				UtilLex.messErr("La procedure " + UtilLex.chaineIdent(UtilLex.numIdCourant)
						+ " a un conflit de nom avec une variable/procedure deja declaree");
			}

			int debut_proc = po.getIpo() + 1;

			placeIdent(UtilLex.numIdCourant, PROC, NEUTRE, debut_proc);

			int def_id = desc.presentDef(UtilLex.chaineIdent(UtilLex.numIdCourant));
			int categorie = PRIVEE;
			if (def_id != 0) {
				desc.modifDefAdPo(def_id, debut_proc);
				categorie = DEF;
			}

			placeIdent(-1, categorie, NEUTRE, 0);

			bc = it + 1;
			compteurDeclProc = 0;
		}
			break;

		case 801: {// apres avoir declare les parametres
			tabSymb[bc - 1].info = compteurDeclProc; // Resolution du nombre de parametres

			int proc_num_id = tabSymb[bc - 2].code;

			int def_id = desc.presentDef(UtilLex.chaineIdent(proc_num_id)); // Resolution du nombre de parametres dans
																			// le cas d'une def
			if (def_id != 0) {
				desc.modifDefNbParam(def_id, compteurDeclProc);
			}

			compteurDeclProc += 2; // On prends en compte les 2 donnees de liaisons
		}
			break;

		case 802: {
			int n_params = tabSymb[bc - 1].info;

			po.produire(RETOUR);
			po.produire(n_params);

			for (int i = bc; i < bc + n_params; i++) {
				tabSymb[i].code = -1; // On masque les noms des parametres
			}

			it = bc + n_params - 1; // On effaces les symboles locaux a la procedures (constantes & variables
									// locales)
			bc = 1;
		}
			break;

		/* parametre fixe */
		case 930:
			placeIdent(UtilLex.numIdCourant, PARAMFIXE, tCour, compteurDeclProc);
			compteurDeclProc++;
			break;

		/* parametre mod */
		case 990:
			placeIdent(UtilLex.numIdCourant, PARAMMOD, tCour, compteurDeclProc);
			compteurDeclProc++;
			break;

		/* INSSI */

		case 1160:
			verifBool();
			po.produire(BSIFAUX);
			po.produire(-1);
			modifVecteurTrans(TRANSCODE);
			pileRep.empiler(po.getIpo()); // On retient le trou du bsifaux
			break;

		case 1161:
			po.produire(BINCOND);
			po.produire(-1);
			modifVecteurTrans(TRANSCODE);
			
			po.modifier(pileRep.depiler(), po.getIpo() + 1); // On resoud le bsifaux pour le else
			pileRep.empiler(po.getIpo()); // On retient le trou du bincond de la fin du si
			break;

		case 1162: // On resoud le bincond ou le bsifaux qu'il y ait un "sinon" ou pas
			po.modifier(pileRep.depiler(), po.getIpo() + 1);
			break;

		/* TTQ */

		case 1250:
			pileRep.empiler(po.getIpo() + 1);// On retient le debut des instructions de la boucle pour le bincond plus
												// tard
			break;

		case 1251:
			po.produire(BSIFAUX);
			po.produire(-1);
			modifVecteurTrans(TRANSCODE);
			
			pileRep.empiler(po.getIpo()); // on retient le trou du bsifaux

			break;

		case 1252:
			po.modifier(pileRep.depiler(), po.getIpo() + 3); // On resoud le bsifaux

			po.produire(BINCOND);
			po.produire(pileRep.depiler()); // on produit le bincond avec l'addresse du debut des instructions
			modifVecteurTrans(TRANSCODE);
			break;

		/* COND */

		case 1190:
			pileRep.empiler(0);
			break;

		case 1191: // execute apres chaque expression
			po.produire(BSIFAUX);
			po.produire(-1);
			modifVecteurTrans(TRANSCODE);
			pileRep.empiler(po.getIpo());
			break;

		case 1200: // execute apres chaque "instructions" sauf la derniere, qui est gere par le
					// fcond (1220)
			int ipo_bsifaux = pileRep.depiler();
			po.modifier(ipo_bsifaux, po.getIpo() + 3);

			int ipo_dernier_bincond = pileRep.depiler();
			po.produire(BINCOND);
			po.produire(ipo_dernier_bincond);
			modifVecteurTrans(TRANSCODE);
			
			pileRep.empiler(po.getIpo());
			break;

		case 1210: // aut, on ne genere pas de bsifaux mais il faut quand meme mettre quelque chose
					// dans la pile
			pileRep.empiler(-1);
			break;

		case 1220: // fcond, on resoud le dernier bsifaux s'il existe et on remonte la chaine de
					// bincond
			int bsifaux = pileRep.depiler();
			if (bsifaux != -1) { // Dans le cas oe on a un aut a la fin, il n'y a pas de bsifaux
				po.modifier(bsifaux, po.getIpo() + 1);
			}

			int fin = po.getIpo() + 1;
			int chaine = pileRep.depiler();
			while (chaine != 0) {
				int v = po.getElt(chaine);
				po.modifier(chaine, fin);
				chaine = v;
			}
			break;

		/* Lecture */

		case 1280: {
			int id = presentIdent(1);
			if (id == 0) {
				UtilLex.messErr(UtilLex.numIdCourant + " non present dans la table des symboles");
				break;
			}

			EltTabSymb symb = tabSymb[id];
			if (!(symb.categorie == VARGLOBALE || symb.categorie == VARLOCALE)) {
				UtilLex.messErr(UtilLex.chaineIdent(symb.code) + " n'est pas une variable");
				break;
			}

			switch (symb.type) {
			case ENT:
				po.produire(LIRENT);
				break;
			case BOOL:
				po.produire(LIREBOOL);
				break;
			default: 
				UtilLex.messErr("Type invalide: " + symb.type);
			}

			switch (symb.categorie) {
			case VARGLOBALE:
				po.produire(AFFECTERG);
				po.produire(symb.info);
				modifVecteurTrans(TRANSDON);
				break;
			case VARLOCALE:
				po.produire(AFFECTERL);
				po.produire(symb.info);
				po.produire(0);
				break;
			case PARAMMOD:
				po.produire(AFFECTERL);
				po.produire(symb.info);
				po.produire(1);
				break;
			default:
				UtilLex.messErr("Impossible de lire dans " + UtilLex.chaineIdent(symb.code) + " car sa categorie est "
						+ symb.categorie);
			}
		}
			break;

		/* Ecriture */

		case 1310:
			switch (tCour) {
			case ENT:
				po.produire(ECRENT);
				break;
			case BOOL:
				po.produire(ECRBOOL);
				break;
			default: 
				UtilLex.messErr("Type invalide: " + tCour);
			}
			break;

		/* Affectation */
		case 1350: {
			int id = presentIdent(1);
			if (id == 0) {
				UtilLex.messErr("tentative d'affectation de " + UtilLex.chaineIdent(UtilLex.numIdCourant)
						+ " non present dans la table des symboles");
				break;
			}
			affectId = id;
		}
			break;

		/* Affectation apres une expression */
		case 1351: {
			EltTabSymb symb = tabSymb[affectId];
			switch (symb.categorie) {
			case VARGLOBALE:
				po.produire(AFFECTERG);
				po.produire(symb.info);
				modifVecteurTrans(TRANSDON);
				break;
			case VARLOCALE:
				po.produire(AFFECTERL);
				po.produire(symb.info);
				po.produire(0);
				break;
			case PARAMMOD:
				po.produire(AFFECTERL);
				po.produire(symb.info);
				po.produire(1);
				break;
			default:
				UtilLex.messErr("Tentative d'assignement de " + UtilLex.chaineIdent(symb.code)
						+ " qui n'est pas une variable locale ou globale");
				break;
			}
		}
			break;

		/* Juste avant l'appel */
		case 1360: {
			int id = presentIdent(1);
			if (id == 0) {
				UtilLex.messErr("tentative d'appel de " + UtilLex.chaineIdent(UtilLex.numIdCourant)
						+ " non present dans la table des symboles");
				break;
			}

			EltTabSymb elt = tabSymb[id];

			if (elt.categorie != PROC) {
				UtilLex.messErr(
						"Appel de \"" + UtilLex.chaineIdent(elt.code) + "\" impossible car ce n'est pas une procedure");
			}

			appelId = id;
			appelNParams = tabSymb[id + 1].info;
			compteurAppelProc = 0;
		}
			break;

		/* Generation de APPEL */
		case 1361: {
			EltTabSymb symb = tabSymb[appelId];
			if (symb.categorie != PROC) {
				UtilLex.messErr(
						"Impossible d'appeller " + UtilLex.chaineIdent(symb.code) + " car ce n'est pas une procedure");
			}

			String nom_proc = UtilLex.chaineIdent(symb.code);

			int n_params = tabSymb[appelId + 1].info;
			if (n_params != compteurAppelProc) {
				UtilLex.messErr("Le nombre de parametres lors de l'appel est invalide. " + nom_proc + " s'attends a "
						+ n_params + " parametres mais " + compteurAppelProc + " parametres ont ete donnes");
			}

			po.produire(APPEL);
			po.produire(symb.info);
			if (desc.presentRef(nom_proc) != 0) {
				modifVecteurTrans(REFEXT);
			}
			po.produire(n_params);
		}
			break;

		/* Apres chaque expression en tant que parametre fixe */
		case 1400: {
			EltTabSymb proc = tabSymb[appelId];
			if (compteurAppelProc >= appelNParams) {
				UtilLex.messErr("Trop de parametres fixes donnes lors de l'appel de " + UtilLex.chaineIdent(proc.code));
			}

			int param_id = appelId + 2 + compteurAppelProc;
			EltTabSymb param = tabSymb[param_id];

			if (param.categorie == PARAMMOD) {
				UtilLex.messErr("Trop de parametres fixes donnes lors de l'appel de " + UtilLex.chaineIdent(proc.code));
			}

			if (param.type != tCour) {
				afftabSymb();
				UtilLex.messErr("Erreur de type lors de l'appel de " + UtilLex.chaineIdent(proc.code) + ": " + "le "
						+ (compteurAppelProc + 1) + "e parametre fixe " + " est de type " + chaineType(param.type)
						+ " mais l'expression est de type " + chaineType(tCour));
			}

			compteurAppelProc++;
		}
			break;

		/* Empilation des parametres mod */
		case 1430: {
			EltTabSymb proc = tabSymb[appelId];
			if (compteurAppelProc >= appelNParams) {
				UtilLex.messErr("Trop de parametres donnes lors de l'appel de " + UtilLex.chaineIdent(proc.code));
			}

			int id = presentIdent(1);
			if (id == 0) {
				UtilLex.messErr(UtilLex.numIdCourant + " non present dans la table des symboles");
				break;
			}
			EltTabSymb symb = tabSymb[id];

			switch (symb.categorie) {
			case VARGLOBALE:
				po.produire(EMPILERADG);
				po.produire(symb.info);
				break;
			case VARLOCALE:
				po.produire(EMPILERADL);
				po.produire(symb.info);
				po.produire(0);
				break;
			case PARAMMOD:
				po.produire(EMPILERADL);
				po.produire(symb.info);
				po.produire(1);
				break;
			default:
				UtilLex.messErr(
						"Errur lors de l'appel de " + UtilLex.chaineIdent(proc.code) + ": le " + (compteurAppelProc + 1)
								+ "e parametre est un parametre mod invalide, car de categorie " + symb.categorie);
			}

			int param_id = appelId + 2 + compteurAppelProc;
			EltTabSymb param = tabSymb[param_id];
			if (param.type != symb.type) {
				afftabSymb();
				UtilLex.messErr("Erreur de type lors de l'appel de " + UtilLex.chaineIdent(proc.code) + ": " + "le "
						+ (compteurAppelProc + 1) + "e parametre" + " est de type " + chaineType(param.type)
						+ " mais la variable " + UtilLex.chaineIdent(symb.code) + " est de type "
						+ chaineType(symb.type));
			}

			compteurAppelProc++;
		}
			break;

		/* Expressions */

		case 1460:
			verifBool();
			break;
		case 1461:
			po.produire(OU);
			break;

		/* exp 1 */

		case 1490:
			po.produire(ET);
			break;

		/* exp2 */
		case 1520:
			verifBool();
			po.produire(NON);
			break;

		/* exp3 */

		case 1570:
			verifEnt();
			break;
		case 1571:
			po.produire(EG);
			tCour = BOOL;
			break;
		case 1580:
			po.produire(DIFF);
			tCour = BOOL;
			break;
		case 1590:
			po.produire(SUP);
			tCour = BOOL;
			break;
		case 1600:
			po.produire(SUPEG);
			tCour = BOOL;
			break;
		case 1610:
			po.produire(INF);
			tCour = BOOL;
			break;
		case 1620:
			po.produire(INFEG);
			tCour = BOOL;
			break;

		/* exp4 */

		case 1670:
			po.produire(ADD);
			break;
		case 1680:
			po.produire(SOUS);
			break;

		/* exp5 */

		case 1730:
			po.produire(MUL);
			break;
		case 1740:
			po.produire(DIV);
			break;

		case 1780:
			po.produire(EMPILER);
			po.produire(vCour);
			break;

		/* Empilement des valeurs d'un identifiant */
		case 1790: {
			int id = presentIdent(1);
			if (id == 0) {
				UtilLex.messErr("Identifiant non declare: " + UtilLex.chaineIdent(UtilLex.numIdCourant));
				break;
			}

			EltTabSymb symb = tabSymb[id];
			switch (symb.categorie) {
			case CONSTANTE:
				po.produire(EMPILER);
				po.produire(symb.info);
				break;
			case VARGLOBALE:
				po.produire(CONTENUG);
				po.produire(symb.info);
				modifVecteurTrans(TRANSDON);
				break;
			case VARLOCALE:
			case PARAMFIXE:
				po.produire(CONTENUL);
				po.produire(symb.info);
				po.produire(0);
				break;
			case PARAMMOD:
				po.produire(CONTENUL);
				po.produire(symb.info);
				po.produire(1);
				break;
			default:
				UtilLex.messErr(UtilLex.chaineIdent(id) + " n'est pas une constante, une variable ou un parametre");
			}
			tCour = symb.type;
			break;
		}

		/* valeur */
		case 1830:
			vCour = UtilLex.valEnt;
			tCour = ENT;
			break;
		case 1840:
			vCour = UtilLex.valEnt;
			tCour = ENT;
			break;
		case 1850:
			vCour = -UtilLex.valEnt;
			tCour = ENT;
			break;
		case 1860:
			vCour = VRAI;
			tCour = BOOL;
			break;
		case 1870:
			vCour = FAUX;
			tCour = BOOL;
			break;
		case 3000: {
			// Verification que toutes les procedures dans tabDef ont etes initialisees
			for (int i = 0; i < desc.getNbDef(); i++) {
				if (desc.getDefAdPo(i) == -1) {
					UtilLex.messErr("La procedure def \"" + desc.getDefNomProc(i) + "\" n'a pas ete definie");
					break;
				}
			}

			desc.setTailleCode(po.getIpo());
			afftabSymb();
			desc.ecrireDesc(UtilLex.nomSource);
			po.constObj();
			po.constGen();
		}
			break;
		default:
			System.out.println("Point de generation non prevu dans votre liste");
			break;
		}
	}
}
