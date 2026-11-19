grammar Policy;

@header {
package mrs.policy;
}

policy : expr EOF ;

expr
    : expr AND expr   # AndExpr
    | expr OR expr    # OrExpr
    | '(' expr ')'    # ParenExpr
    | ATTRIBUTE       # AttrExpr
    ;

AND : 'AND';
OR  : 'OR';

ATTRIBUTE : [a-zA-Z0-9][a-zA-Z0-9_]*;
WS : [ \t\r\n]+ -> skip ;

/*
 * ------------------------------------------------------------
 * Policy Grammar
 *
 * Supported logical operators:
 *      AND
 *      OR
 *
 * Parentheses may be used to explicitly group sub-expressions.
 *
 * Attribute names:
 *      - May start with a letter or a digit.
 *      - May contain letters, digits and underscores (_).
 *      - No spaces or special characters are allowed.
 *
 * Valid examples:
 *
 *      A
 *      Doctor
 *      Department_HR
 *      A AND B
 *      A OR B
 *      (A AND B)
 *      ((A AND B) OR C)
 *      (A AND (B OR C))
 *      ((Doctor AND Hospital) OR Nurse)
 *
 * Invalid examples:
 *      
 *      Parser Errors:
 *      A AND
 *      OR B
 *      A AND OR B
 *      ()
 *      (A AND)
 *      (A OR)
 *      
 *      Lexer Errors:
 *      Department:HR
 *      Role=Admin
 *      User Name
 *      A-B
 *      _Admin
 *
 * ------------------------------------------------------------
 */