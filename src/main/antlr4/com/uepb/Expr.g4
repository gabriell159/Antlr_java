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
    : 'var' ID '=' expr ';'     #VarDeclComValor
    | 'var' ID ';'              #VarDeclSemValor
    ;

assign
    : ID '=' expr ';'           #Atribuicao
    ;

ifStmt
    : 'if' '(' cond ')' block   #If
    ;

whileStmt
    : 'while' '(' cond ')' block   #While
    ;

printStmt
    : 'print' '(' STRING ')' ';'   #PrintTexto
    | 'print' '(' expr ')' ';'     #PrintExpr
    ;

inputStmt
    : 'input' '(' ID ')' ';'       #Input
    ;

block
    : '{' stmt* '}'
    ;

// --------------------
// CONDIÇÕES (SEM AMBIGUIDADE)
// --------------------
cond
    : cond 'or' cond2         #CondOr
    | cond2                   #CondBase
    ;

cond2
    : cond2 'and' cond3       #CondAnd
    | cond3                   #CondBase2
    ;

cond3
    : '(' cond ')'            #CondParenteses
    | expr condOp expr        #CondComparacao
    | 'true'                  #CondTrue
    | 'false'                 #CondFalse
    ;

// --------------------
// OPERADORES
// --------------------
condOp
    : '<'
    | '>'
    | '<='
    | '>='
    | '=='
    | '!='
    ;

// --------------------
// EXPRESSÕES (COM PRECEDÊNCIA)
// --------------------
expr
    : expr '+' termo          #Soma
    | expr '-' termo          #Subtracao
    | termo                   #ExprBase
    ;

termo
    : termo '*' fator         #Multiplicacao
    | termo '/' fator         #Divisao
    | fator                   #TermoBase
    ;

fator
    : '-' fator               #NegacaoUnaria
    | base '^' fator          #Potencia
    | base                    #FatorBase
    ;

base
    : '(' expr ')'            #ExprParenteses
    | NUMBER                  #Numero
    | ID                      #UsoVariavel
    ;

// --------------------
// LÉXICO
// --------------------
NUMBER
    : [0-9]+ ('.' [0-9]+)?
    ;

STRING
    : '"' (~["\r\n])* '"'
    ;

ID
    : [a-zA-Z_] [a-zA-Z_0-9]*
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

COMMENT
    : '//' ~[\r\n]* -> skip
    ;