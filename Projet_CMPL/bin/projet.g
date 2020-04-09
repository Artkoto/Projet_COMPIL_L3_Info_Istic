// Grammaire du langage PROJET
// CMPL L3info 
// Arnaud Akoto, Paris Douady, Jacquemoud Edgar
// il convient d'y inserer les appels a {PtGen.pt(k);}
// relancer Antlr apres chaque modification et raffraichir le projet Eclipse le cas echeant

// attention l'analyse est poursuivie apres erreur si l'on supprime la clause rulecatch

grammar projet;

options {
  language=Java; k=1;
 }

@header {           
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;
} 


// partie syntaxique :  description de la grammaire //
// les non-terminaux doivent commencer par une minuscule


@members {

 
// variables globales et methodes utiles a placer ici
  
}
// la directive rulecatch permet d'interrompre l'analyse a la premiere erreur de syntaxe
@rulecatch {
catch (RecognitionException e) {reportError (e) ; throw e ; }}


unite  :   unitprog  EOF
      |    unitmodule  EOF
  ;
  
unitprog
  : 'programme' ident ':'  
     declarations  
     corps { System.out.println("succes, arret de la compilation "); PtGen.pt(3000); }
  ;
  
unitmodule
  : 'module' ident ':' 
     declarations   
  ;
  
declarations
  : partiedef? partieref? consts? vars? ({PtGen.pt(530);} decprocs {PtGen.pt(531);})? 
  ;
  
partiedef
  : 'def' ident  (',' ident )* ptvg
  ;
  
partieref: 'ref'  specif (',' specif)* ptvg
  ;
  
specif  : ident  ( 'fixe' '(' type  ( ',' type  )* ')' )? 
                 ( 'mod'  '(' type  ( ',' type  )* ')' )? 
  ;
  
consts  : 'const' ( ident  '=' valeur {PtGen.pt(670); } ptvg  )+ 
  ;
  
vars  : 'var' {PtGen.pt(700); } ( type ident {PtGen.pt(701); } ( ','  ident {PtGen.pt(701); } )* ptvg  )+ {PtGen.pt(702);}
  ;
  
type  : 'ent'  {PtGen.pt(730); }
  |     'bool' {PtGen.pt(740); }
  ;
  
decprocs: (decproc ptvg)+
  ;
  
decproc :  'proc'  ident { PtGen.pt(800); } parfixe? parmod? {PtGen.pt(801);} consts? vars? corps {PtGen.pt(802);}
  ;
  
ptvg  : ';'
  | 
  ;
  
corps : 'debut' instructions 'fin'
  ;
  
parfixe: 'fixe' '(' pf ( ';' pf )* ')'
  ;
  
pf  : type ident {PtGen.pt(930);} ( ',' ident {PtGen.pt(930);} )*  
  ;

parmod  : 'mod' '(' pm ( ';' pm)* ')'
  ;
  
pm  : type ident {PtGen.pt(990);} ( ',' ident {PtGen.pt(990);} )*
  ;
  
instructions
  : instruction ( ';' instruction)*
  ;
  
instruction
  : inssi
  | inscond
  | boucle
  | lecture
  | ecriture
  | affouappel
  |
  ;
  
inssi : 'si' expression { PtGen.pt(1160); } 'alors' instructions ('sinon' { PtGen.pt(1161); }  instructions)? 'fsi' { PtGen.pt(1162); }
  ;
  
inscond : 'cond' { PtGen.pt(1190); }  expression  { PtGen.pt(1191); } ':' instructions
          (','  { PtGen.pt(1200); }  expression { PtGen.pt(1191); } ':' instructions  )* 
          ({ PtGen.pt(1200); } 'aut' { PtGen.pt(1210); } instructions |  ) 
          'fcond' { PtGen.pt(1220); }
  ;
  
boucle  : 'ttq' { PtGen.pt(1250); } expression { PtGen.pt(1251); } 'faire' instructions 'fait' { PtGen.pt(1252); } 
  ;
  
lecture: 'lire' '(' ident {PtGen.pt(1280);} ( ',' ident {PtGen.pt(1280);} )* ')' 
  ;
  
ecriture: 'ecrire' '(' expression {PtGen.pt(1310);} ( ',' expression {PtGen.pt(1310);} )* ')'
   ;
  
affouappel
  : ident ({PtGen.pt(1350);} ':=' expression  {PtGen.pt(1351);}
                          | {PtGen.pt(1360);}  (effixes (effmods)?)? {PtGen.pt(1361);}
           )
  ;
  
effixes : '(' (expression {PtGen.pt(1400);} (',' expression {PtGen.pt(1400);} )*)? ')'
  ;
  
effmods :'(' (ident {PtGen.pt(1430);} (',' ident {PtGen.pt(1430);} )*)? ')'
  ; 
  
expression: (exp1)  ('ou' {PtGen.pt(1460);} exp1 {PtGen.pt(1460);}  {PtGen.pt(1461);})* 
  ;
  
exp1  : exp2  ('et' {PtGen.pt(1460);} exp2 {PtGen.pt(1460);} {PtGen.pt(1490);} )* 
  ;
  
exp2  : 'non' exp2 {PtGen.pt(1520);}
  | exp3  
  ;
  
exp3  : exp4 
  ( '='   {PtGen.pt(1570);} exp4 {PtGen.pt(1570);} {PtGen.pt(1571);}
  | '<>'  {PtGen.pt(1570);} exp4 {PtGen.pt(1570);} {PtGen.pt(1580);}
  | '>'   {PtGen.pt(1570);} exp4 {PtGen.pt(1570);} {PtGen.pt(1590);}
  | '>='  {PtGen.pt(1570);} exp4 {PtGen.pt(1570);} {PtGen.pt(1600);}  
  | '<'   {PtGen.pt(1570);} exp4 {PtGen.pt(1570);} {PtGen.pt(1610);}
  | '<='  {PtGen.pt(1570);} exp4 {PtGen.pt(1570);} {PtGen.pt(1620);} 
  ) ?
  ;
  
exp4  : exp5 
        ('+' {PtGen.pt(1570);} exp5 {PtGen.pt(1570);} {PtGen.pt(1670);} 
        |'-' {PtGen.pt(1570);} exp5 {PtGen.pt(1570);} {PtGen.pt(1680);}
        )*
  ;
  
exp5  : primaire 
        (    '*' {PtGen.pt(1570);}  primaire {PtGen.pt(1570);} {PtGen.pt(1730);} 
          | 'div'{PtGen.pt(1570);}  primaire {PtGen.pt(1570);} {PtGen.pt(1740);}
        )*
  ;
  
primaire: valeur {PtGen.pt(1780);} 
  | ident {PtGen.pt(1790);}
  | '(' expression ')'
  ;
  
valeur  : nbentier { PtGen.pt(1830); }
  | '+' nbentier { PtGen.pt(1840); }
  | '-' nbentier { PtGen.pt(1850); }
  | 'vrai' { PtGen.pt(1860); }
  | 'faux' { PtGen.pt(1870); }
  ;

// partie lexicale  : cette partie ne doit pas etre modifiee  //
// les unites lexicales de ANTLR doivent commencer par une majuscule
// Attention : ANTLR n'autorise pas certains traitements sur les unites lexicales, 
// il est alors ncessaire de passer par un non-terminal intermediaire 
// exemple : pour l'unit lexicale INT, le non-terminal nbentier a du etre introduit
 
      
nbentier  :   INT { UtilLex.valEnt = Integer.parseInt($INT.text);}; // mise a jour de valEnt

ident : ID  { UtilLex.traiterId($ID.text); } ; // mise a jour de numIdCourant
     // tous les identificateurs seront places dans la table des identificateurs, y compris le nom du programme ou module
     // (NB: la table des symboles n'est pas geree au niveau lexical mais au niveau du compilateur)
        
  
ID  :   ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ; 
     
// zone purement lexicale //

INT :   '0'..'9'+ ;
WS  :   (' '|'\t' |'\r')+ {skip();} ; // definition des "blocs d'espaces"
RC  :   ('\n') {UtilLex.incrementeLigne(); skip() ;} ; // definition d'un unique "passage a la ligne" et comptage des numeros de lignes

COMMENT
  :  '\{' (.)* '\}' {skip();}   // toute suite de caracteres entouree d'accolades est un commentaire
  |  '#' ~( '\r' | '\n' )* {skip();}  // tout ce qui suit un caractere diese sur une ligne est un commentaire
  ;

// commentaires sur plusieurs lignes
ML_COMMENT    :   '/*' (options {greedy=false;} : .)* '*/' {$channel=HIDDEN;}
    ;	  