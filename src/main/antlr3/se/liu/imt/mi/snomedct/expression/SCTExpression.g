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
  | expression
  ;
  
querySet
  : query (',' query)* -> query+
  ;
  
expressionOrQuery
  : query 
  ;

expression
  : concept ('+' concept)* (':' refinements)? -> ^(TOP_AND ^(GENUS concept+) ^(DIFF refinements?))
  ;
  
concept
  : SCTID 
  | SCTID TERM -> SCTID
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

  
