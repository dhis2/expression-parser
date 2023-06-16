package org.hisp.dhis.lib.expression;

import org.hisp.dhis.lib.expression.ast.Node;
import org.hisp.dhis.lib.expression.syntax.ExpressionGrammar;
import org.hisp.dhis.lib.expression.syntax.Parser;
import org.hisp.dhis.lib.expression.util.TNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hisp.dhis.lib.expression.ast.NodeType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that expressions with variable names are parsed into the expected AST structure.
 *
 * @author Jan Bernitt
 */
class VariableExpressionTest {

    @Test
    void testProgramVariable() {
        assertHasStructure(TNode.ofLevels(VARIABLE, IDENTIFIER),
                "V{current_date}");
    }

    @Test
    void testProgramRuleVariable_Hash() {
        assertHasStructure(TNode.ofLevels(VARIABLE, IDENTIFIER),
                "#{varname}");
    }

    @Test
    void testProgramRuleVariable_A() {
        assertHasStructure(TNode.ofLevels(VARIABLE, IDENTIFIER),
                "A{varname}");
    }

    @Test
    void testProgramRuleVariable_SingleQuoted() {
        assertHasStructure(TNode.ofLevels(FUNCTION, ARGUMENT, VARIABLE, STRING),
                "d2:maxValue('varname')");
    }

    @Test
    void testProgramRuleVariable_DoubleQuoted() {
        assertHasStructure(TNode.ofLevels(FUNCTION, ARGUMENT, VARIABLE, STRING),
                "d2:maxValue(\"varname\")");
    }

    private void assertHasStructure(TNode expected, String expression) {
        Node<?> root = Parser.parse(expression, ExpressionGrammar.ProgramIndicatorExpressionMode, true);
        assertEquals(TNode.of(PAR, List.of(expected)), TNode.of(root));
    }
}
