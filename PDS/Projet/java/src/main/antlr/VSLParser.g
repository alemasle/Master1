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
    : (b=bloc)* { $out = new ASD.Program($b.out); }
    ;
    
bloc returns [ASD.Bloc out]
	: i=instructions e=expression { $out = new ASD.Bloc($i.out, $e.out); }
    ;

instructions returns [ASD.Instruction out]
	: a=affect* {$out = new ASD.AffectInstruction($a.out); }
	;

affect returns [ASD.Instruction out]
	: i=id AFFECT e=expression { $out = new ASD.Instruction($i.out,$e.out); }
	;

expression returns [ASD.Expression out]
    : l=expression PLUS r=expression2 { $out = new ASD.AddExpression($l.out, $r.out); }    
	| l=expression SOUS r=expression2 { $out = new ASD.SousExpression($l.out, $r.out); }
	| e=expression2 { $out = $e.out; }
    ;
    
expression2 returns [ASD.Expression out]
	: l=expression2 MULT r=factor  { $out = new ASD.MultExpression($l.out, $r.out); }
	| l=expression2 DIV r=factor  { $out = new ASD.DivExpression($l.out, $r.out); }
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
    ;
    
id returns [ASD.Affectable out]
    : IDENT {$out = new ASD.AffectIdent($IDENT.text); }
    ;
