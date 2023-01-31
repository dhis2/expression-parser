package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.ProgramVariable;
import org.hisp.dhis.expression.spi.QueryModifiers;
import org.hisp.dhis.expression.spi.Variable;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableModifierExpressionTest {

    @Test
    void testStageOffset() {
        QueryModifiers expected = QueryModifiers.builder().stageOffset(12).build();
        assertEquals(Set.of(new Variable(ProgramVariable.event_count, expected)),
                evaluate("V{event_count}.stageOffset(12)"));
    }

    @Test
    void testMultiStageOffset() {
        // stage offsets add up
        QueryModifiers expected = QueryModifiers.builder().stageOffset(3).build();
        assertEquals(Set.of(new Variable(ProgramVariable.event_count, expected)),
                evaluate("(V{event_count}.stageOffset(1)).stageOffset(2)"));
    }

    private static Set<Variable> evaluate(String expression) {
        Expression expr = new Expression(expression, Expression.Mode.PROGRAM_INDICATOR_EXPRESSION);
        return expr.collectProgramVariables();
    }
}
