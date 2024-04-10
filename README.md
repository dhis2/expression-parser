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

For JavaScript the main entry point to the API is called `ExpressionJs`.

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

## Development
This library implements the semantic release setup, which means that version numbers are not manually maintained but 
derived from the commit/PR history.

Branches:
- `main`: a push to `main` branch will trigger a new production release (both Maven and NPMJS).
- `beta`: a push to `beta` branch will trigger a SNAPSHOT release in Maven and a new beta release in NPMJS.

Version number are determined by the presence of commits with these suffixes:
- `fit:`: it will increase the patch number.
- `feat:`: it will increase the minor version number.
- `feat!:`: it will increase the major version number.

If there is not any commit with any of this tags between the previous version and the current commit, nothing will be published.

Typical workflow:
1. Do work in a feature branch. There is no need to add tags to the commits.
2. Create a PR to `beta` branch including a tag in the PR title depending on the kind of changes.
3. Merge the PR using **Squash and merge**. It will publish a SNAPSHOT/BETA release if there is a version change.
4. Create a PR to `main` branch. Once merged, it will publish a production release. 
