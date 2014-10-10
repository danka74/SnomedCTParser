grammar SCTExpression;

options {
  language = Java;
  output = AST;
}


tokens {
TOP_AND;
AND;
GENUS;
DIFF;
ROLEGROUP;
SOME;
CONCEPT;
SCTID;
UNION;
DESC;
DESC_SELF;
ALL;
EQ_TO;
SC_OF;
}

@header {
package se.liu.imt.mi.snomedct.expression;
}

@lexer::header {
package se.liu.imt.mi.snomedct.expression;
}

query
  : 'Descendants' '(' query ')' -> ^(DESC query)
  | 'DescendantsAndSelf' '('  query ')' -> ^(DESC_SELF query)
  | 'Union' '(' querySet ')' -> ^(UNION querySet)
  | 'All' -> ^(ALL)
  | axiom
  ;
  
querySet
  : query (',' query)* -> query+
  ;
  
expressionOrQuery
  : query 
  ;

axiom
  : EQ_TO expression -> ^(EQ_TO expression)
  | SC_OF expression -> ^(SC_OF expression)
  | expression
  ;
  
expression
  : concept ('+' concept)* -> ^(TOP_AND ^(GENUS concept+)) 
  | concept ('+' concept)* (':' refinements) -> ^(TOP_AND ^(GENUS concept+) ^(DIFF refinements))
  ;
    
concept
  : SCTID 
  | SCTID TERM -> ^(SCTID TERM)
  ;
  
refinements
  : attributeSet (',' attributeGroup)* -> attributeSet attributeGroup*
  | attributeGroup (',' attributeGroup)* -> attributeGroup+
  ;
  
attributeGroup
  : '{' attributeSet '}' -> ^(ROLEGROUP attributeSet)
  ;
  
attributeSet
  : attribute (',' attribute)* -> ^(AND attribute+)
  ;
  
attribute
  : concept '=' attributeValue -> ^(SOME concept attributeValue)
  ;
  
attributeValue
  : concept
  | '(' concept ('+' concept)* (':' refinements)? ')' -> ^(TOP_AND ^(GENUS concept+) ^(DIFF refinements?))
  ;


TERM
  : '|' WS* NONWSNONPIPE (' ' | NONWSNONPIPE)* '|';

SCTID 
  : '-'? NONZERO DIGIT*;

EQ_TO
  : '===';
  
SC_OF
  : '<<<';
  
fragment DIGIT
  : '0'..'9'
  ;

fragment NONZERO
  : '1'..'9'
  ;

fragment NONWSNONPIPE
  : ~('|' | '\t' | ' ' | '\r' | '\n'| '\u000C' )
  ;
  
WS
  : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )    { $channel = HIDDEN; } ;

  
