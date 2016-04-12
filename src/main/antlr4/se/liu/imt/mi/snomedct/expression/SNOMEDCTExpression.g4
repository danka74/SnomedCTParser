grammar SNOMEDCTExpression;

options {
	language = Java;
}

EQ_TO: '===';
SC_OF: '<<<';
LPARAN: '(';
RPARAN: ')';
COLON: ':';
PLUS: '+';
COMMA: ',';
EQ: '=';
LCBRACKET: '{';
RCBRACKET: '}';

BLOCK_COMMENT
: 
	'/*' .*? '*/' -> skip
;

EOL_COMMENT 
:
	'//' ~[\r\n]* -> skip
;

statements
:
	statement
	(
		statement
	)*
;

statement
:
	LPARAN subExpression RPARAN definitionStatus LPARAN subExpression RPARAN
;

expression
:
	definitionStatus subExpression
	| subExpression
;

definitionStatus
:
	EQ_TO
	| SC_OF
;

subExpression
:
	focusConcept
	(
		COLON refinement
	)?
;

focusConcept
:
	conceptReference
	(
		PLUS conceptReference
	)*
;

conceptReference
:
	SCTID
	| SCTID TERM
;

refinement
:
	nonGroupedAttributeSet
	(
		COMMA attributeGroup
	)*
	| attributeGroup
	(
		COMMA attributeGroup
	)*
;

attributeGroup
:
	LCBRACKET attributeSet RCBRACKET
;

nonGroupedAttributeSet
:
	attribute
	(
		COMMA attribute
	)*
;

attributeSet
:
	attribute
	(
		COMMA attribute
	)*
;

attribute
:
	attributeType = conceptReference EQ attributeValue
;

attributeValue
:
	conceptReference
	| nestedExpression
	| NUMBER
	| STRING
;

nestedExpression
:
	LPARAN subExpression RPARAN
;

NUMBER
:
	'#' '-'? NONZERO DIGIT*
	(
		'.' DIGIT*
	)?
	| '#' '-'? '0.' DIGIT+
;

STRING
:
	'"'
	(
		ESCAPE_CHAR
		| ~( '"' | '\\' )
	)*? '"'
;

fragment
ESCAPE_CHAR
:
	'\\"'
	| '\\\\'
;

TERM
:
	'|' ' '* NONWSNONPIPE
	(
		' '
		| NONWSNONPIPE
	)* '|'
;

SCTID
:
	'-'? NONZERO (DIGIT|'-')*
;

fragment
DIGIT
:
	'0' .. '9'
;

fragment
NONZERO
:
	'1' .. '9'
;

fragment
NONWSNONPIPE
:
	~( '|' | '\t' | ' ' | '\r' | '\n' | '\u000C' )
;

WS
:
	(
		'\t'
		| ' '
		| '\r'
		| '\n'
		| '\u000C'
	) -> skip
;


