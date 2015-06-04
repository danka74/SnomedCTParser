grammar SNOMEDCTQueryExpression;

options {
	language = Java;
}

import SNOMEDCTExpression;

DESC: '<';
DESC_SELF: '<<';
MINUS: 'MINUS';
DISJ: 'OR';

unaryConstraintOp
:
	DESC
	| DESC_SELF
;

binaryConstraintOp
:
	DISJ
	| MINUS
;

query
:
	subQuery (binaryConstraintOp query)? EOF
;

subQuery
:
	unaryConstraintOp subExpression
	| subExpression
;