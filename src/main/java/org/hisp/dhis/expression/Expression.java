package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.eval.CalcNodeInterpreter;
import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.ExpressionBackend;
import org.hisp.dhis.expression.spi.IllegalExpressionException;
import org.hisp.dhis.expression.spi.ParseException;
import org.hisp.dhis.expression.syntax.ExpressionGrammar;
import org.hisp.dhis.expression.syntax.Parser;

import java.util.Map;
import java.util.Set;

/**
 * Facade API for working with DHIS2 expressions.
 *
 * @author Jan Bernitt
 */
public final class Expression {

    public enum Mode {
        //TODO each mode has it defined list of fragments to use
        // mode is added as a constructor argument for the expression
    }

    //add all direct interaction to this

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
        return evaluate(item -> null, Map.of(), Map.of());
    }

    public Object evaluate(ExpressionBackend backend, Map<String, Object> programRuleVariableValues, Map<DataItem, Object> dataItemValues) throws IllegalExpressionException {
        return root.eval(new CalcNodeInterpreter(backend, programRuleVariableValues, dataItemValues));
    }

    @Override
    public String toString() {
        return expression;
    }
}
