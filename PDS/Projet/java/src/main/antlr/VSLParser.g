parser grammar VSLParser;

options {
  language = Java;
  tokenVocab = VSLLexer;
}

@header {
  package TP2;

  import java.util.stream.Collectors;
  import java.util.Arrays;
}


program returns [ASD.Program out]
    : ACCOLADEG b=bloc ACCOLADED EOF { $out = new ASD.Program($b.out); }
    | b=bloc EOF { $out = new ASD.Program($b.out); }
    ;
	
bloc returns [ASD.Bloc out]
    : { List<ASD.Statement> ls = new ArrayList<ASD.Statement>(); ASD.Declaration decl = null;} 
    (d=declarations {decl = $d.out; })?
    (s=statements { ls.add($s.out); } )+
      { $out = new ASD.Bloc(decl, ls); }
    ;
    
declarations returns [ASD.Declaration out]
	: { List<ASD.DeclVar> lvar = new ArrayList<ASD.DeclVar>(); }
	DECINT (d=declid { lvar.add($d.out); } (VIRGULE d=declid { lvar.add($d.out); } )* )
	  { $out = new ASD.Declaration(lvar); }
	;    
    
statements returns [ASD.Statement out]
	: i=instructions { $out = new ASD.Statement($i.out); }
	| e=expression { $out = new ASD.Statement($e.out); }
	;

instructions returns [ASD.Instructions out]
	: a=affect { $out = $a.out; } 
	| r=retour { $out = $r.out; }
	| i=ifinstr	{ $out = $i.out; }
	;

affect returns [ASD.Instructions out]
	: i=identificateur AFFECT e=expression { $out = new ASD.AffectInstructions($i.out,$e.out); }
	;
	
retour returns [ASD.Instructions out]
	: RETURN e=expression { $out = new ASD.ReturnInstructions($e.out); }
	;
	
ifinstr returns [ASD.Instructions out]
	: {ASD.Bloc ex = null; } 
	IF c=condition
	THEN  { ASD.depth ++; } th=bloc { ASD.depth --;}
	(ELSE { ASD.depth ++;} el=bloc { ASD.depth --;} { ex=$el.out; } )? 
	FI
	  { $out = new ASD.IfInstruction($c.out, $th.out, ex); }
	;

condition returns [ASD.Expression out]
	: r=expression { $out = new ASD.CondExpression($r.out); }
	;


expression returns [ASD.Expression out]
    : l=expression 
    	( PLUS r=expression2 { $out = new ASD.AddExpression($l.out, $r.out); }
		| SOUS r=expression2 { $out = new ASD.SousExpression($l.out, $r.out); } 
		)
	| e=expression2 { $out = $e.out; }
    ;

expression2 returns [ASD.Expression out]
	: l=expression2 
		( MULT r=factor  { $out = new ASD.MultExpression($l.out, $r.out); }
		| DIV r=factor  { $out = new ASD.DivExpression($l.out, $r.out); }
		)
	| f=factor { $out = $f.out; }
	;

factor returns [ASD.Expression out]
    : p=primary { $out = $p.out; }
    | LP e=expression RP { $out = $e.out; }
    ;

primary returns [ASD.Expression out]
    : {boolean unitaire = true; }
      (PLUS|SOUS {unitaire = !unitaire;})* INTEGER { if(unitaire) $out = new ASD.IntegerExpression($INTEGER.int);
    											     else $out = new ASD.IntegerExpression(0 - $INTEGER.int); }
    | IDENT {$out = new ASD.exprIdent($IDENT.text); }
    ;

declid returns [ASD.DeclVar out]
    : IDENT { $out = new ASD.DeclVar(new ASD.IntType(), $IDENT.text); }
    ;
    
identificateur returns [ASD.IdentVar out]
	: IDENT { $out = new ASD.IdentVar(new ASD.IntType(), $IDENT.text); }
	;
