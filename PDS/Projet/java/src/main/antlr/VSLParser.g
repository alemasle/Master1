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
    : l=factor PLUS r=expression  { $out = new ASD.AddExpression($l.out, $r.out); }    
    | l=factor SOUS r=expression  { $out = new ASD.SousExpression($l.out, $r.out); }
    | l=factor MULT r=expression  { $out = new ASD.MultExpression($l.out, $r.out); }
    | l=factor DIV r=expression  { $out = new ASD.DivExpression($l.out, $r.out); }    
    | f=factor { $out = $f.out; }
    ;


factor returns [ASD.Expression out]
    : p=primary { $out = $p.out; }
    | LP e=expression RP { $out = $e.out; }
    ;

primary returns [ASD.Expression out]
    : INTEGER { $out = new ASD.IntegerExpression($INTEGER.int); }
    ;
