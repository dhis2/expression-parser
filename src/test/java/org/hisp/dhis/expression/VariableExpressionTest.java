package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.syntax.ExpressionGrammar;
import org.hisp.dhis.expression.syntax.Parser;
import org.hisp.dhis.expression.util.TNode;
import org.junit.jupiter.api.Test;

import static org.hisp.dhis.expression.ast.NodeType.ARGUMENT;
import static org.hisp.dhis.expression.ast.NodeType.FUNCTION;
import static org.hisp.dhis.expression.ast.NodeType.IDENTIFIER;
import static org.hisp.dhis.expression.ast.NodeType.PAR;
import static org.hisp.dhis.expression.ast.NodeType.STRING;
import static org.hisp.dhis.expression.ast.NodeType.VARIABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that expressions with variable names are parsed into the expected AST structure.
 *
 * @author Jan Bernitt
 */
class VariableExpressionTest {

    @Test
    void testProgramVariable() {
        assertHasStructure(TNode.ofLevels(PAR, VARIABLE, IDENTIFIER),
                "V{current_date}");
    }

    @Test
    void testProgramRuleVariable_Hash() {
        assertHasStructure(TNode.ofLevels(PAR, VARIABLE, IDENTIFIER),
                "#{varname}");
    }

    @Test
    void testProgramRuleVariable_A() {
        assertHasStructure(TNode.ofLevels(PAR, VARIABLE, IDENTIFIER),
                "A{varname}");
    }

    @Test
    void testProgramRuleVariable_SingleQuoted() {
        assertHasStructure(TNode.ofLevels(PAR, FUNCTION, ARGUMENT, VARIABLE, STRING),
                "d2:maxValue('varname')");
    }

    @Test
    void testProgramRuleVariable_DoubleQuoted() {
        assertHasStructure(TNode.ofLevels(PAR, FUNCTION, ARGUMENT, VARIABLE, STRING),
                "d2:maxValue(\"varname\")");
    }

    private void assertHasStructure(TNode expected, String expression) {
        Node<?> root = Parser.parse(expression, ExpressionGrammar.AllFragments);
        assertEquals(expected, TNode.of(root));
    }
}
