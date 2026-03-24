grammar Expr;

prog
    : stmt* EOF
    ;

// Instrução
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

// if (cond) {}
ifStmt
    : 'if' '(' COND=cond ')' BLOCO=block    #If
    ;

// while (cond) {}
whileStmt
    : 'while' '(' COND=cond ')' BLOCO=block     #While
    ;

printStmt
    : 'print' '(' STRING ')' ';'    #PrintTexto
    | 'print' '(' expr ')' ';'      #PrintExpr
    ;

inputStmt
    : 'input' '(' ID ')' ';'        #Input
    ;

// bloco de instruções entre chaves
block
    : '{' stmt* '}'
    ;

// condiçoes if e while
cond
    : E1=cond 'or' E2=cond              #CondOr
    | E1=cond 'and' E2=cond             #CondAnd
    | '(' COND_INTERNA=cond ')'         #CondParenteses
    | E1=expr OP=condOp E2=expr         #CondComparacao
    | 'true'                            #CondTrue
    | 'false'                           #CondFalse
    ;

// comparação
condOp
    : '<'       #OpMenor
    | '>'       #OpMaior
    | '<='      #OpMenorIgual
    | '>='      #OpMaiorIgual
    | '=='      #OpIgual
    | '!='      #OpDiferente
    ;

// expressão aritmetica
expr
    : <assoc=right> BASE=expr '^' EXP=expr      #Potencia
    | O1=expr OP='*' O2=expr                    #Multiplicacao
    | O1=expr OP='/' O2=expr                    #Divisao
    | O1=expr OP='+' O2=expr                    #Soma
    | O1=expr OP='-' O2=expr                    #Subtracao
    | SINAL='-' VALOR=expr                       #NegacaoUnaria
    | '(' EXPR_INTERNA=expr ')'                  #ExprParenteses
    | NUMBER                                     #Numero
    | ID                                         #UsoVariavel
    ;


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