grammar LoqiGrammar;
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

addClassDataDecl : 'values' 'for' CLASS id ('{' propertyValueStatement* '}')? ;

classMemberDecl : propertyDecl
                | propertyValueStatement
                | relationshipDecl
                ;

propertyDecl : (CLASS | OBJ) 'prop' id ':' type metadataSection? ';'
             | (CLASS | OBJ) 'prop' id (':' type)? '=' value metadataSection? ';'
             ;

relationshipDecl : 'rel' id '(' idList ')' (':' relationshipKind)? metadataSection? ';' ;

relationshipKind : scaleType
                 | scaleType? relationshipQuantifier
                 | relationshipDependency
                 ;

relationshipQuantifier : '{' linkCount '->' linkCount '}'
                       ;
linkCount : INTEGER
          | '*'
          ;

scaleType : 'linear'
          | 'partial'
          ;

relationshipDependency : relationshipDependencyType 'to' (id | relationshipRef);
relationshipDependencyType : ('opposite' | 'transitive' | 'between' | 'closer' | 'further' ) ;

relationshipRef : id '->' id ;

propertyRef : id '.' id ;

//Енамы ---------

enumDecl : 'enum' id ('{' enumMemberList? '}')? metadataSection? ;

enumMemberList : enumMemberDecl (',' enumMemberDecl)* ','? ;

enumMemberDecl : id metadataSection? ;

// Объекты ------

objDecl: varLeftPart? OBJ? id ':' id ('{' objStatement* '}')? metadataSection? ;

objStatement : propertyValueStatement
             | relationshipLinkStatement
             ;

propertyValueStatement : id '=' value ';' ;

relationshipLinkStatement : id '(' idList ')' ';' ;

varDecl :  varLeftPart id;

varLeftPart : 'var' idList '=' ;

// Метаданные ---------

addMetaDecl: 'meta' 'for' metaRef metadataSection?;

metaRef : OBJ? id
        | CLASS id
        | propertyRef
        | relationshipRef
        | ENUM id
        | enumValueRef
        ;

metadataSection : '[' metadataPropertyDecl* ']' ;

metadataPropertyDecl : (id '.')? id ('=' value)? ';' ;

// Прочее -----------------

type : ID       //ссылка на Enum
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

value : INTEGER
      | DOUBLE
      | BOOLEAN
      | STRING
      | enumValueRef
      ;

enumValueRef : ID ':' ID ;

idList : id (',' id)* ','? ;

id : ID ;

// Ключевые слова

CLASS : 'class' ;
OBJ : 'obj' ;
ENUM : 'enum' ;

INT_TYPE : 'int' ;
DOUBLE_TYPE : 'double' ;
BOOL_TYPE : 'bool' ;
STRING_TYPE : 'string' ;


//Литералы

INTEGER : DECIMAL
        ;

DECIMAL : ( '0' | [1-9] Digit* ) ;

DOUBLE : (Digit+ '.' Digit* | '.' Digit+) ExponentPart?
       | Digit+ ExponentPart
       | DECIMAL [dD]
       ;

BOOLEAN : 'true'
        | 'false'
        ;

STRING : '"""' (~[\\] | EscapeSequence)*? '"""'
       | '\'\'\'' (~[\\] | EscapeSequence)*? '\'\'\''
       | '"' (~["\\\r\n] | EscapeSequence)* '"'
       | '\'' (~['\\\r\n] | EscapeSequence)* '\''
       ;

ID : Letter LetterOrDigit* ;

// Whitespace and comments

WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);

// Fragments

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
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
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