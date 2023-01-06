package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.eval.EvaluateFunction;
import org.hisp.dhis.expression.eval.NormaliseConsumer;
import org.hisp.dhis.expression.eval.TypeCheckingConsumer;
import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.ExpressionFunctions;
import org.hisp.dhis.expression.spi.IllegalExpressionException;
import org.hisp.dhis.expression.spi.ParseException;
import org.hisp.dhis.expression.syntax.ExpressionGrammar;
import org.hisp.dhis.expression.syntax.Parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Facade API for working with DHIS2 expressions.
 *
 * @author Jan Bernitt
 */
public final class Expression {

    // Parse MOdes
    // 1. Description ( subst. UID with name of items)
    // 2. Find data items to load from database (indicators, validation rules, predictors)
    // 3. evaluate the expression value (plug in 2.)
    // 4. Translate to SQL

    public enum Mode {
        //TODO each mode has it defined list of fragments to use
        // mode is added as a constructor argument for the expression
    }

    private final String expression;
    private final Node<?> root;

    public Expression(String expression) throws ParseException {
        this.expression = expression;
        this.root = Parser.parse(expression, ExpressionGrammar.AllFragments);
    }

    public Set<DataItem> collectDataItems() {
        return Node.collectDataItems(root);
    }

    public Set<String> collectProgramRuleVariables() {
        return Node.collectProgramRuleVariables(root);
    }

    public Object evaluate() {
        return evaluate(name -> null, Map.of(), Map.of());
    }

    public Object evaluate(ExpressionFunctions functions, Map<String, Object> programRuleVariableValues, Map<DataItem, Object> dataItemValues) throws IllegalExpressionException {
        return root.eval(new EvaluateFunction(functions, programRuleVariableValues, dataItemValues));
    }

    public List<?> typeCheck() {
        //TODO use a class that is spi for the violations
        TypeCheckingConsumer typeCheck = new TypeCheckingConsumer();
        root.visit(typeCheck);
        return typeCheck.getViolations();
    }

    /**
     * @return the expression in its normalised from (str => AST => str)
     */
    public String normalise() {
        return NormaliseConsumer.toExpression(root);
    }

    @Override
    public String toString() {
        return expression;
    }
}
