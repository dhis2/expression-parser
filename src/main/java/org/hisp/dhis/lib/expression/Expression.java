package org.hisp.dhis.lib.expression;

import org.hisp.dhis.lib.expression.ast.Node;
import org.hisp.dhis.lib.expression.ast.VariableType;
import org.hisp.dhis.lib.expression.eval.Evaluate;
import org.hisp.dhis.lib.expression.eval.NodeValidator;
import org.hisp.dhis.lib.expression.spi.DataItem;
import org.hisp.dhis.lib.expression.spi.DataItemType;
import org.hisp.dhis.lib.expression.spi.ExpressionData;
import org.hisp.dhis.lib.expression.spi.ExpressionFunctions;
import org.hisp.dhis.lib.expression.spi.ID;
import org.hisp.dhis.lib.expression.spi.IllegalExpressionException;
import org.hisp.dhis.lib.expression.spi.ParseException;
import org.hisp.dhis.lib.expression.spi.ValueType;
import org.hisp.dhis.lib.expression.spi.Variable;
import org.hisp.dhis.lib.expression.spi.VariableValue;
import org.hisp.dhis.lib.expression.syntax.ExpressionGrammar;
import org.hisp.dhis.lib.expression.syntax.Fragment;
import org.hisp.dhis.lib.expression.syntax.Parser;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Facade API for working with DHIS2 expressions.
 *
 * @author Jan Bernitt
 */
public final class Expression {

    public enum Mode {
        // analyses data values for validity
        VALIDATION_RULE_EXPRESSION(ExpressionGrammar.ValidationRuleExpressionMode, ValueType.NUMBER),
        VALIDATION_RULE_RESULT_TEST(ExpressionGrammar.SimpleTestMode, ValueType.BOOLEAN),

        // data value generators
        PREDICTOR_GENERATOR_EXPRESSION(ExpressionGrammar.PredictorExpressionMode, ValueType.NUMBER, ValueType.STRING),
        // do a section in the data needs skipping (ignore)
        PREDICTOR_SKIP_TEST(ExpressionGrammar.PredictorSkipTestMode, ValueType.BOOLEAN),

        // ad-hoc calculated (no DB)
        // query analytics to compute some aggregate value
        INDICATOR_EXPRESSION(ExpressionGrammar.IndicatorExpressionMode, ValueType.NUMBER),

        // always SQL for entire expression
        // query analytics to compute some aggregate value
        PROGRAM_INDICATOR_EXPRESSION(ExpressionGrammar.ProgramIndicatorExpressionMode, ValueType.NUMBER),

        // never SQL (also we need JS)
        // PROGRAM_RULE_EXPRESSION
        RULE_ENGINE_CONDITION(ExpressionGrammar.RuleEngineMode, NodeValidator.RuleEngineMode, ValueType.BOOLEAN),
        RULE_ENGINE_ACTION(ExpressionGrammar.RuleEngineMode, NodeValidator.RuleEngineMode, ValueType.BOOLEAN, ValueType.STRING, ValueType.NUMBER, ValueType.DATE);

        final EnumSet<ValueType> resultTypes;
        final List<Fragment> fragments;
        final List<NodeValidator> validators;

        Mode(List<Fragment> fragments, ValueType... resultTypes) {
            this(fragments, List.of(), resultTypes);
        }
        Mode(List<Fragment> fragments, List<NodeValidator> validators, ValueType... resultTypes) {
            this.resultTypes = EnumSet.of(resultTypes[0], resultTypes);
            this.fragments = fragments;
            this.validators = validators;
        }
    }

    private final Mode mode;
    private final String expression;
    private final Node<?> root;

    public Expression(String expression) throws ParseException {
        this(expression, Mode.PREDICTOR_GENERATOR_EXPRESSION);
    }

    public Expression(String expression, Mode mode) throws ParseException {
        this.mode = mode;
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

    public String describe(Map<String, String> displayNames) {
        return Evaluate.describe(root, displayNames);
    }

    public void validate(Set<String> displayNamesKeys) {
        Map<String, VariableValue> programRuleVariableValues = new HashMap<>();
        displayNamesKeys.forEach(key -> programRuleVariableValues.put(key, null));
        ExpressionData data = ExpressionData.builder()
                .programRuleVariableValues(programRuleVariableValues)
                .build();
        Evaluate.validate(root, data, mode.validators, mode.resultTypes);
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
