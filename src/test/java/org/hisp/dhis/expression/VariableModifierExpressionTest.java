package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.AggregationType;
import org.hisp.dhis.expression.ast.ProgramVariable;
import org.hisp.dhis.expression.spi.QueryModifiers;
import org.hisp.dhis.expression.spi.Variable;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableModifierExpressionTest {

    @Test
    void testAggregationType() {
        QueryModifiers expected = QueryModifiers.builder().aggregationType(AggregationType.sum).build();
        assertEquals(Set.of(new Variable(ProgramVariable.program_name, expected)),
                evaluate("V{program_name}.aggregationType(sum)"));
    }

    @Test
    void testMaxDate() {
        QueryModifiers expected = QueryModifiers.builder().maxDate(LocalDate.parse("1980-11-11")).build();
        assertEquals(Set.of(new Variable(ProgramVariable.completed_date, expected)),
                evaluate("V{completed_date}.maxDate(1980-11-11)"));
    }

    @Test
    void testMinDate() {
        QueryModifiers expected = QueryModifiers.builder().minDate(LocalDate.parse("1980-11-11")).build();
        assertEquals(Set.of(new Variable(ProgramVariable.due_date, expected)),
                evaluate("V{due_date}.minDate(1980-11-11)"));
    }

    @Test
    void testPeriodOffset() {
        QueryModifiers expected = QueryModifiers.builder().periodOffset(11).build();
        assertEquals(Set.of(new Variable(ProgramVariable.enrollment_date, expected)),
                evaluate("V{enrollment_date}.periodOffset(11)"));
    }

    @Test
    void testStageOffset() {
        QueryModifiers expected = QueryModifiers.builder().stageOffset(12).build();
        assertEquals(Set.of(new Variable(ProgramVariable.event_count, expected)),
                evaluate("V{event_count}.stageOffset(12)"));
    }

    @Test
    void testYearToDate() {
        QueryModifiers expected = QueryModifiers.builder().yearToDate(true).build();
        assertEquals(Set.of(new Variable(ProgramVariable.org_unit_count, expected)),
                evaluate("V{org_unit_count}.yearToDate()"));
    }

    @Test
    void testMultiStageOffset() {
        // stage offsets add up
        QueryModifiers expected = QueryModifiers.builder().stageOffset(3).build();
        assertEquals(Set.of(new Variable(ProgramVariable.event_count, expected)),
                evaluate("(V{event_count}.stageOffset(1)).stageOffset(2)"));
    }

    private static Set<Variable> evaluate(String expression) {
        Expression expr = new Expression(expression, Expression.Mode.INDICATOR_EXPRESSION);
        return expr.collectProgramVariables();
    }
}
