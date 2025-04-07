grammar LoqiGrammar;

topLevelRule : model
             | exp
             ;

model : topDecl+ EOF
      ;

topDecl : classDecl
        | enumDecl
        | objDecl
        | addMetaDecl
        | addClassDataDecl
        | varDecl
        ;

//Классы ----------

classDecl : CLASS id (':' id)? ('{' classMemberDecl* '}')? metadataSection? ;

addClassDataDecl : VALUES FOR CLASS id ('{' propertyValueStatement* '}')? ;

classMemberDecl : propertyDecl
                | propertyValueStatement
                | relationshipDecl
                ;

propertyDecl : (CLASS | OBJ) PROP id paramsDecl? ':' type metadataSection? ';'
             | (CLASS | OBJ) PROP id (':' type)? '=' value metadataSection? ';'
             ;

relationshipDecl : REL id paramsDecl? '(' idList ')' (':' relationshipKind)? metadataSection? ';' ;

relationshipKind : scaleType
                 | scaleType? relationshipQuantifier
                 | relationshipDependency
                 ;

relationshipQuantifier : '{' linkCount '->' linkCount '}'
                       ;
linkCount : INTEGER
          | '*'
          ;

scaleType : LINEAR
          | PARTIAL
          ;

relationshipDependency : relationshipDependencyType TO (id | relationshipRef);
relationshipDependencyType : (OPPOSITE | TRANSITIVE | BETWEEN | CLOSER | FURTHER ) ;

relationshipRef : id '->' id ;

propertyRef : id '.' id ;

//Енамы ---------

enumDecl : ENUM id ('{' enumMemberList? '}')? metadataSection? ;

enumMemberList : enumMemberDecl (',' enumMemberDecl)* ','? ;

enumMemberDecl : id metadataSection? ;

// Объекты ------

objDecl: varLeftPart? OBJ? id ':' id ('{' objStatement* '}')? metadataSection? ;

objStatement : propertyValueStatement
             | relationshipLinkStatement
             ;

propertyValueStatement : id paramsValues? '=' value ';' ;

relationshipLinkStatement : id paramsValues? '(' idList ')' ';' ;

varDecl :  varLeftPart id;

varLeftPart : VAR idList '=' ;

// Метаданные ---------

addMetaDecl: META FOR metaRef metadataSection?;

metaRef : OBJ? id
        | CLASS id
        | propertyRef
        | relationshipRef
        | ENUM id
        | enumValueRef
        ;

metadataSection : '[' metadataPropertyDecl* ']' ;

metadataPropertyDecl : (id '.')? id '=' value ';' ;

// Прочее -----------------

paramsDecl : '<' (paramDecl (',' paramDecl)* ','?)? '>' ;

paramDecl : id ':' type ;

paramsValues : '<' value (',' value)* ','? '>' #orderedParamsValues
            | '<' (namedParamValue (',' namedParamValue)* ','?) ? '>' #namedParamsValues
            ;

namedParamValue : id '=' value ;

type : id       //ссылка на Enum
     | intType
     | doubleType
     | BOOL_TYPE
     | STRING_TYPE
     ;

intType : INT_TYPE intRange? ;
intRange : '[' intRangeStart ',' INTEGER? ']'
         | '{' intList '}'
         ;
intRangeStart : INTEGER? ; //костыль, чтобы в коде можно было отличить первую границу от второй
intList : INTEGER (',' INTEGER)* ','? ;

doubleType : DOUBLE_TYPE doubleRange? ;
doubleRange : '[' doubleRangeStart ',' DOUBLE? ']'
         | '{' doubleList '}'
         ;
doubleRangeStart : DOUBLE? ; //костыль, чтобы в коде можно было отличить первую границу от второй
doubleList : DOUBLE (',' DOUBLE)* ','? ;

exp
    : value                         #valueLiteral
    | ID                            #treeVar
    | CLASS':'ID                    #classLiteral
    | OBJ':'ID                      #objLiteral
    | '$'ID                         #variable
    | exp '->' ID paramsValuesExpr?                   #getByRelationship
    | exp '.'  ID paramsValuesExpr?                    #getProperty
    | NOT exp                       #notExp
    | exp IS exp                    #isExp
    | exp (GREATER|LESS|GTE|LTE) exp   #compareExp
    | exp (EQ|NOT_EQ) exp           #equalityExp
    | exp '.' COMPARE '(' exp ')'   #threewayCompareExp
    | '(' exp ')'                   #parenthesisExp
    | exp AS exp                   #castExp
    | exp AND exp                   #andExp
    | exp OR exp                    #orExp
    | exp '->' ID paramsValuesExpr? '(' (exp ',')* exp ')' '.' ID  #getRelationshipParamExp
    | exp '->' ID paramsValuesExpr? '(' (exp ',')* exp ')'     #checkRelationshipExp
    | exp '.' CLASS '(' ')'                     #getClassExp
    | FIND ID ID '{' exp '}'                    #findByConditionExp
    | FIND_EXTREME ID '[' exp ']' AMONG ID ID '{' exp '}'      #findExtremeExp
    | FOR_ANY ID ID ('[' exp ']')? '{' exp '}'                      #existQuantifierExp
    | FOR_ALL ID ID ('[' exp ']')? '{' exp '}'                    #forAllQuantifierExp
    | exp '+=>' ID paramsValuesExpr? '(' (exp ',')* exp ')'    #addRelationshipExp
    | <assoc=right> exp '=' exp                             #assignExp
    | <assoc=right>  exp '?' exp ':' exp                    #ternaryIfExp
    | <assoc=right> IF '(' exp ')' exp (ELSE exp )?                  #ifExp
    | '{' (exp ';')+ '}'                                    #blockExp
    ;

paramsValuesExpr : '<' (namedParamValueExpr (',' namedParamValueExpr)* ','?) ? '>' #namedParamsValuesExpr
            |'<' exp (',' exp)* ','? '>' #orderedParamsValuesExpr
            ;

namedParamValueExpr : ID '=' exp ;

value : INTEGER
      | DOUBLE
      | BOOLEAN
      | STRING
      | enumValueRef
      ;

enumValueRef : id ':' id ;

idList : id (',' id)* ','? ;

id : ID ;

//-------------ЛЕКСЕР---------------

//Литералы

INTEGER : DECIMAL ;

DECIMAL : ( '0' | [1-9] Digit* ) ;

DOUBLE : (Digit+ '.' Digit* | '.' Digit+) ExponentPart?
       | Digit+ ExponentPart
       | DECIMAL [dD]
       ;

BOOLEAN : TRUE
        | FALSE
        ;

STRING : '"""' (~[\\] | EscapeSequence)*? '"""'
       | '\'\'\'' (~[\\] | EscapeSequence)*? '\'\'\''
       | '"' (~["\\\r\n] | EscapeSequence)* '"'
       | '\'' (~['\\\r\n] | EscapeSequence)* '\''
       ;

// Ключевые слова

FIND  : 'find';
FIND_EXTREME : 'findExtreme' ;
IS   :   'is';
AS : 'as' ;
AND     :  'and';
OR   : 'or';
NOT  : 'not';
COMPARE : 'compare';
FOR_ANY  : 'forAny';
FOR_ALL : 'forAll';
AMONG    : 'among';
IF : 'if' ;
ELSE : 'else' ;
WITH : 'with' ;

CLASS : 'class' ;
OBJ : 'obj' ;
ENUM : 'enum' ;
PROP : 'prop' ;
REL : 'rel' ;

VALUES : 'values' ;
META : 'meta' ;

FOR : 'for' ;
TO : 'to' ;

LINEAR : 'linear' ;
PARTIAL : 'partial' ;

OPPOSITE : 'opposite' ;
TRANSITIVE : 'transitive' ;
BETWEEN : 'between' ;
CLOSER : 'closer' ;
FURTHER : 'further' ;

VAR : 'var' ;

INT_TYPE : 'int' ;
DOUBLE_TYPE : 'double' ;
BOOL_TYPE : 'bool' ;
STRING_TYPE : 'string' ;

TRUE : 'true' ;
FALSE : 'false' ;

EQ : '==' ;
NOT_EQ : '!=' ;
GREATER : '>' ;
LESS : '<' ;
GTE : '>=' ;
LTE : '<=';

//Идентификаторы

ID : Letter LetterOrDigit*      //См. LoqiStringUtils#isSimpleName
   | '`' NonWhiteSpace+ '`'     //Экранированный идентификатор позволяет что угодно, кроме пробелов
   ;

// Whitespace and comments

WS:                 WhiteSpace+      -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);

// Fragments

fragment WhiteSpace : [ \t\r\n\u000C] ;
fragment NonWhiteSpace : ~[ \t\r\n\u000C] ;

fragment EscapeSequence
    : '\\' [btnfr"'\\]
//    | '\\' 'u005c'? ([0-3]? [0-7])? [0-7]
//    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment LetterOrDigit
    : Letter
    | Digit
    ;

fragment Letter
    : [a-zA-Z_]
    ;

fragment Digit
    : [0-9]
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;

fragment ExponentPart
    : [eE] [+-]? Digit+
    ;