grammar Expr;

prog
    : stmt* EOF
    ;

// --------------------
// INSTRUÇÕES
// --------------------
stmt
    : varDecl
    | assign
    | ifStmt
    | whileStmt
    | printStmt
    | inputStmt
    ;

varDecl
    : VAR ID ASSIGN expr SEMI     #VarDeclComValor
    | VAR ID SEMI                #VarDeclSemValor
    ;

assign
    : ID ASSIGN expr SEMI        #Atribuicao
    ;

ifStmt
    : IF LPAREN cond RPAREN block   #If
    ;

whileStmt
    : WHILE LPAREN cond RPAREN block   #While
    ;

printStmt
    : PRINT LPAREN STRING RPAREN SEMI   #PrintTexto
    | PRINT LPAREN expr RPAREN SEMI     #PrintExpr
    ;

inputStmt
    : INPUT LPAREN ID RPAREN SEMI       #Input
    ;

block
    : LBRACE stmt* RBRACE
    ;

// --------------------
// CONDIÇÕES
// --------------------
cond
    : cond OR cond2         #CondOr
    | cond2                 #CondBase
    ;

cond2
    : cond2 AND cond3       #CondAnd
    | cond3                 #CondBase2
    ;

cond3
    : LPAREN cond RPAREN    #CondParenteses
    | expr condOp expr      #CondComparacao
    | TRUE                  #CondTrue
    | FALSE                 #CondFalse
    ;

condOp
    : LT
    | GT
    | LE
    | GE
    | EQ
    | NEQ
    ;

// --------------------
// EXPRESSÕES
// --------------------
expr
    : expr op=(PLUS | MINUS) termo   #SomaSub
    | termo                          #ExprBase
    ;

termo
    : termo op=(MUL | DIV) fator     #MultiDiv
    | fator                          #TermoBase
    ;

fator
    : MINUS fator                    #NegacaoUnaria
    | base op=POW fator              #Potencia
    | base                           #FatorBase
    ;

base
    : LPAREN expr RPAREN             #ExprParenteses
    | NUMBER                         #Numero
    | ID                             #UsoVariavel
    ;

// --------------------
// TOKENS
// --------------------
VAR     : 'var';
IF      : 'if';
WHILE   : 'while';
PRINT   : 'print';
INPUT   : 'input';

AND     : 'and';
OR      : 'or';
TRUE    : 'true';
FALSE   : 'false';

LPAREN  : '(';
RPAREN  : ')';
LBRACE  : '{';
RBRACE  : '}';

PLUS    : '+';
MINUS   : '-';
MUL     : '*';
DIV     : '/';
POW     : '^';

ASSIGN  : '=';
SEMI    : ';';

GT      : '>';
GE      : '>=';
LT      : '<';
LE      : '<=';
EQ      : '==';
NEQ     : '!=';

ID      : [a-zA-Z_][a-zA-Z_0-9]*;
NUMBER  : [0-9]+ ('.' [0-9]+)?;

STRING  : '"' (~["\r\n])* '"';

WS      : [ \t\r\n]+ -> skip;
COMMENT : '//' ~[\r\n]* -> skip;