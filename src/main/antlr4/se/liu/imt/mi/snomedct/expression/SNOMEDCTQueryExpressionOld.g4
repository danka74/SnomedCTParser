grammar SNOMEDCTQueryExpressionOld;

options {
	language = Java;
}

import SNOMEDCTExpression;

query
  : 'Descendants' '(' query ')'
  | 'DescendantsAndSelf' '('  query ')'
  | 'Union' '(' querySet ')'
  | 'All'
  | expression
  ;

querySet
  : query (',' query)*
  ;