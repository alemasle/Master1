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


// TODO : other rules

program returns [ASD.Program out]
    : e=expression { $out = new ASD.Program($e.out); }
    ;

expression returns [ASD.Expression out]
    : l=expression2
	    ( PLUS r=expression  { $out = new ASD.AddExpression($l.out, $r.out); }    
	    | SOUS r=expression  { $out = new ASD.SousExpression($l.out, $r.out); }
    	)+
    | l=expression2 { $out = $l.out; }
    ;
    
expression2 returns [ASD.Expression out]
	: l=factor
		( MULT r=factor  { $out = new ASD.MultExpression($l.out, $r.out); }
	    | DIV r=factor  { $out = new ASD.DivExpression($l.out, $r.out); }
		)+
	| l=factor { $out = $l.out; }
	;
    

factor returns [ASD.Expression out]
    : p=primary { $out = $p.out; }
    | LP e=expression RP { $out = $e.out; }
    ;

primary returns [ASD.Expression out]
    : INTEGER { $out = new ASD.IntegerExpression($INTEGER.int); }
    ;
