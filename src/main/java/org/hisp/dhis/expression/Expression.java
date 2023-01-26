package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.ValueType;
import org.hisp.dhis.expression.ast.VariableType;
import org.hisp.dhis.expression.eval.Evaluate;
import org.hisp.dhis.expression.eval.TypeCheckingConsumer;
import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.DataItemType;
import org.hisp.dhis.expression.spi.ExpressionData;
import org.hisp.dhis.expression.spi.ExpressionFunctions;
import org.hisp.dhis.expression.spi.ID;
import org.hisp.dhis.expression.spi.IllegalExpressionException;
import org.hisp.dhis.expression.spi.ParseException;
import org.hisp.dhis.expression.spi.Variable;
import org.hisp.dhis.expression.spi.VariableValue;
import org.hisp.dhis.expression.syntax.ExpressionGrammar;
import org.hisp.dhis.expression.syntax.Fragment;
import org.hisp.dhis.expression.syntax.Parser;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Facade API for working with DHIS2 expressions.
 *
 * @author Jan Bernitt
 */
public final class Expression {

    public enum Mode {
        // never SQL unless in an indicator sub-expression
        VALIDATION_RULE_EXPRESSION(ExpressionGrammar.ValidationRuleExpressionMode, ValueType.NUMBER),
        PREDICTOR_EXPRESSION(ExpressionGrammar.PredictorExpressionMode, ValueType.NUMBER, ValueType.STRING),
        INDICATOR_EXPRESSION(ExpressionGrammar.IndicatorExpressionMode, ValueType.NUMBER),
        PREDICTOR_SKIP_TEST(ExpressionGrammar.PredictorSkipTestMode, ValueType.BOOLEAN),
        SIMPLE_TEST(ExpressionGrammar.SimpleTestMode, ValueType.BOOLEAN),

        // always SQL for entire expression
        PROGRAM_INDICATOR_EXPRESSION(ExpressionGrammar.ProgramIndicatorExpressionMode, ValueType.NUMBER),

        // never SQL (also we need JS)
        // PROGRAM_RULE_EXPRESSION
        RULE_ENGINE(ExpressionGrammar.RuleEngineMode, ValueType.NUMBER);

        final EnumSet<ValueType> result;
        final List<Fragment> fragments;

        Mode(List<Fragment> fragments, ValueType... resultTypes) {
            this.result = EnumSet.of(resultTypes[0], resultTypes);
            this.fragments = fragments;
        }
    }

    private final String expression;
    private final Node<?> root;

    public Expression(String expression) throws ParseException {
        this(expression, Mode.PREDICTOR_EXPRESSION);
    }

    public Expression(String expression, Mode mode) throws ParseException {
        this.expression = expression;
        this.root = Parser.parse(expression, mode.fragments);
    }

    public Set<DataItem> collectDataItems() {
        return Evaluate.collectDataItems(root);
    }

    public Set<String> collectProgramRuleVariableNames() {
        return Evaluate.collectVariableNames(root, VariableType.PROGRAM_RULE);
    }

    public Set<String> collectProgramVariablesNames() {
        return Evaluate.collectVariableNames(root, VariableType.PROGRAM);
    }

    /**
     * For testing only.
     *
     * @see #evaluate(ExpressionFunctions, ExpressionData)
     */
    Object evaluate() {
        return evaluate(name -> null, ExpressionData.builder().build());
    }

    public Object evaluate(ExpressionFunctions functions, ExpressionData data) throws IllegalExpressionException {
        return Evaluate.evaluate(root, functions, data);
    }

    public Set<Variable> collectProgramVariables() {
        return Evaluate.collectVariables(root, VariableType.PROGRAM);
    }

    public String generateSQL(ExpressionFunctions functions, Map<Variable, String> sqlByProgramVariable) {
        return null;
    }

    /**
     * Collects all ID that are UID values.
     *
     * OBS! This does not include {@link ID}s that are not {@link ID.Type#isUID()}.
     *
     * @return A set of {@link ID}s used in the expression.
     */
    public Set<ID> collectUIDs() {
        return Evaluate.collectUIDs(root);
    }

    public String describe(Map<ID, String> displayNames) {
        return Evaluate.describe(root, displayNames);
    }

    public Set<DataItem> collectDataItemForRegenerate() {
        return Evaluate.collectDataItems(root, DataItemType.CONSTANT, DataItemType.ORG_UNIT_GROUP);
    }

    /**
     * Regenerates an expression from the parse tree where all constant IDs are substituted with their values
     * and all organisation unit groups IDs are substituted with their member count provided that are contained in
     * the given map.
     *
     * @see #collectDataItemForRegenerate()
     *
     * @param dataItemValues values for constants, member count for organisation unit groups
     * @return an expression where constant and organisation unit group data items are substituted with values
     */
    public String regenerate(Map<DataItem, Number> dataItemValues ) {
        // old: org.hisp.dhis.expression.DefaultExpressionService#regenerateIndicatorExpression (indicator only)
        return Evaluate.regenerate(root, dataItemValues);
    }

    public List<?> typeCheck() {
        //TODO use a class that is spi for the violations
        TypeCheckingConsumer typeCheck = new TypeCheckingConsumer();
        root.visit(typeCheck);
        //TODO check that the root returns the expected result type
        return typeCheck.getViolations();
    }

    /**
     * @return the expression in its normalised from (str => AST => str)
     */
    public String normalise() {
        return Evaluate.normalise(root);
    }

    /**
     * @return the expression in its original (user input) form
     */
    @Override
    public String toString() {
        return expression;
    }
}
