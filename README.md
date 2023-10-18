# Expression Parser

A parser for the DHIS2 expression language.

The parser is implemented as a kotlin multiplatform project in order to yield
a Java and JS library that can be used in client and server of DHIS2.

The parsing follows the PEG approach. 
Essentially this means for each building block there is a corresponding function implementing its
parsing which has this shape:

```kotlin
// e.g. to deal with the 'expr' fragment of the grammar
fun expr(expr: Expr, ctx: ParseContext) {
    // ...
}
```

Each such method accepts two arguments:
1. the `Expr` representing the parsed input expression and the position in it
2. the `ParseContext` to emit AST nodes and lookup named fragments

The parsing is implemented on 4 levels (high to low):
1. `ExpressionGrammar`: high level composition of named fragments
2. `Expr`: specific hard coded fragments (like `expr`), operators and brackets
3. `Literals`: terminals of the language; string, number, date literals etc.
4. `Chars`: named character sets of the language as used by `Literals`

For an equivalent of a (ANTLR) grammar definition look at `ExpressionGrammar`.

### API
The public API of the library is encapsulated by `Expression` which refers to types 
in the `org.hisp.dhis.lib.expression.spi` package.

### Modes
DHIS2 has several expression language, each supporting a different set of functions.
To select which expression is used the `Expression` is parameterized with a `Mode`.

* `VALIDATION_RULE_EXPRESSION`: Computes a number for a validation rule
* `VALIDATION_RULE_RESULT_TEST`: Computes a boolean for a validation rule result
* `PREDICTOR_GENERATOR_EXPRESSION`: Computes a number or string data value for a predictor expression
* `PREDICTOR_SKIP_TEST`: Computes a boolean to test if a data section should be skipped when evaluating predictor expressions
* `INDICATOR_EXPRESSION`: Computes a number for an indicator expression (ad-hoc aggregate data computation)
* `PROGRAM_INDICATOR_EXPRESSION`: Computes a number for a program indicator expression (persisted aggregate data computation)
* `RULE_ENGINE_CONDITION`: Computes a boolean to check if a rule applies
* `RULE_ENGINE_ACTION`: Computes a boolean, string, number or date from a rule

### Quirks & Irregularities
Syntax choices that make the grammar hard to parse and represent as AST in a
way that is easy to maintain:

- `d2:relationshipCount` is the only function expecting a quoted `UID`
- a `programRuleStringVariableName` is any string and only identifiable having
  a special meaning by its position as argument to certain functions
- `PS_EVENTDATE:` is a tag for a `UID` for a dataResolver value but does not use the
  `#{...}` wrapper and can therefore easily be confused for a named function
- functions accepting dataResolver item values do not accept all dataResolver item value types
  that can occur on top level.
- the `de:*`-functions contain `:` which is hard to distinguish from a tag
- `orgUnit.*`-functions contain `.` which is hard to distinguish from a method