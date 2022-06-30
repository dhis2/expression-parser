# Expression Parser

A parser for the DHIS2 expression language.

The parser is implemented in the PEG technique.
Essentially this means each non-terminal maps to a method implementing its
parsing. Within that method other such methods are called to parse the
terminals and non-terminals that a non-terminal is composed of. 

Each such method accepts two arguments:
1. the `Expr` representing the parsed input expression and the position in it
2. the `ParseContext` to emit AST nodes and lookup named non-terminals

The parsing is implemented in 4 levels (high to low):
1. `ExprGrammar`: high level composition of non-terminals (functions, methods, constants)
2. `Expr`: non-terminals `expr` (operators, brackets) and `data`
3. `Literals`: terminals of the language; string, number, date literals etc.
4. `Chars`: named character sets of the language as used by `Literals`

An approximation of the implemented language in BNF would be
(using lower case for non-terminals, upper case for terminals):
```
expr            = expr1 method+
                | expr1 ( BINARY_OPERATOR expr1 )+
expr1           = UNARY_OPERATOR expr1
                | '(' expr ')'
                | '[' IDENTIFIER ']'
                | STRING
                | NUMBER
                | DATE
                | function
                | data-value
                | constant
function        = NAME '(' expr (',' expr )* ')'
method          = '.' NAME '(' expr (',' expr )* ')'
data-value      = NAME '{' reference '}'
reference       = uid ( '.' uid )? ( '.' uid )?
                | REF
uid             = tag? UID ('&' UID)*
tag             = IDENTIFIER ':'
constant        = 'true'
                | 'false'
                | 'null'

IDENTIFIER      = [a-zA-Z_]+
UID             = [a-zA-Z][a-zA-Z0-9]{10}
NAME            = [a-zA-Z0-9#:_.]+
REF             = [a-zA-Z0-9.-_ ]+
UNARY_OPERATOR  = '+' | '-' | '!' | 'not' | 'distinct'
BINARY_OPERATOR = '^' | '*' | '/' | '%' | '+' | '-' | '&&' | '||' 
                | '==' | '!=' | '<' | '>' | '<=' | '>='
NUMBER          = [+-]? ([0-9]+ | [0-9]* ('.' [0-9]* ([eE][+-]?[0-9]+)? ))
DATE            = [1-9] [0-9]{3} '-' [0-1]? [0-9] '-' [0-3]? [0-9]
STRING          = '"' ... '"'   // ... => its complicated, escaping, unicode
                | '\'' ... '\'' 
```

While in general functions and methods have `expr` arguments each named
function has a particular sequence of parameters which might be limited to a
case like expecting a `DATE` or a `data-value` item.

### AST
The parser builds an AST with "flat" operators. Meaning the operands are not
nested as children of the operator. 
This is so the parsing has a linear complexity.

The tree with flat operators can be made into a tree with nested operators:
```java
Node.groupOperators(root);
```