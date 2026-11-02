package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.ast.BinaryOperator
import org.hisp.dhis.lib.expression.ast.FunctionParameter
import org.hisp.dhis.lib.expression.ast.NamedFunction
import org.hisp.dhis.lib.expression.ast.UnaryOperator
import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.syntax.FunctionFragment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

/**
 * Tests the listing of available functions and operators per [ExpressionMode].
 */
internal class ExpressionModeFunctionsTest {

    @Test
    fun testFunctions_RuleEngine() {
        val functions = Expression.getFunctions(ExpressionMode.RULE_ENGINE_CONDITION).map { it.function }
        assertTrue(NamedFunction.d2_zScoreWFA in functions)
        assertTrue(NamedFunction.d2_hasValue in functions)
        assertFalse(NamedFunction.sum in functions)
        assertFalse(NamedFunction.d2_relationshipCount in functions)
    }

    @Test
    fun testFunctions_ValidationRuleResultTest() {
        val functions = Expression.getFunctions(ExpressionMode.VALIDATION_RULE_RESULT_TEST).map { it.function }
        assertTrue(NamedFunction.ifThenElse in functions)
        assertFalse(NamedFunction.orgUnit_ancestor in functions)
        assertFalse(NamedFunction.d2_count in functions)
    }

    @Test
    fun testFunctions_SignatureMatchesGrammar() {
        val functions = Expression.getFunctions(ExpressionMode.PROGRAM_INDICATOR_EXPRESSION).associateBy { it.function }
        val number = FunctionParameter(ValueType.NUMBER, isOptional = false, isVarargs = false)
        val optionalNumber = FunctionParameter(ValueType.NUMBER, isOptional = true, isVarargs = false)
        assertEquals(listOf(number, optionalNumber), functions[NamedFunction.log]?.parameters)
        assertEquals(listOf(number, optionalNumber), functions[NamedFunction.d2_log]?.parameters)
        assertEquals(listOf(number, optionalNumber, optionalNumber), functions[NamedFunction.normDistCum]?.parameters)
        assertEquals(
            listOf(FunctionParameter(ValueType.STRING, isOptional = true, isVarargs = false)),
            functions[NamedFunction.d2_relationshipCount]?.parameters)
        assertEquals(
            listOf(
                FunctionParameter(ValueType.STRING, isOptional = false, isVarargs = false),
                FunctionParameter(ValueType.STRING, isOptional = false, isVarargs = true)),
            functions[NamedFunction.contains]?.parameters)
        assertEquals(ValueType.BOOLEAN, functions[NamedFunction.contains]?.getReturnType())
        assertEquals(listOf(number, number), functions[NamedFunction.d2_exponent]?.parameters)
        assertEquals(
            listOf(
                FunctionParameter(ValueType.MIXED, isOptional = false, isVarargs = false),
                FunctionParameter(ValueType.STRING, isOptional = false, isVarargs = false)),
            functions[NamedFunction.d2_countIfCondition]?.parameters)
    }

    @Test
    fun testFunctions_SignatureUsesLiteralType() {
        val condition = Expression.getFunctions(ExpressionMode.RULE_ENGINE_CONDITION)
            .first { it.function == NamedFunction.d2_condition }
        // the condition is written as a string literal (parsed as a nested boolean expression)
        assertEquals(
            listOf(ValueType.STRING, ValueType.SAME, ValueType.SAME),
            condition.parameters.map { it.type })
        assertEquals(1.0, Expression("d2:condition('1 > 0', 1, 2)", ExpressionMode.RULE_ENGINE_ACTION).evaluate())
    }

    @Test
    fun testFunctions_FoundWhenWrapped() {
        val fn = ExpressionMode.RULE_ENGINE_ACTION.fragments.first { it.name() == NamedFunction.d2_round.getName() }
        val wrapped = fn.named("alias").maybe()
        assertEquals(NamedFunction.d2_round, FunctionFragment.of(wrapped)?.function)
    }

    @Test
    fun testFunctions_OptionalArgumentsCanBeOmitted() {
        assertEquals(1.0, Expression("log(10) / log(10)", ExpressionMode.INDICATOR_EXPRESSION).evaluate())
        assertEquals(3.0, Expression("d2:round(2.6)", ExpressionMode.RULE_ENGINE_ACTION).evaluate())
    }

    @Test
    fun testFunctions_ListedExactlyWhenAvailable() {
        for (mode in ExpressionMode.entries) {
            val listed = Expression.getFunctions(mode).map { it.function }.toSet()
            for (fn in NamedFunction.entries) {
                val e = runCatching { Expression("${fn.getName()}()", mode) }.exceptionOrNull()
                val unknown = e?.message?.contains("Unknown function or constant") == true
                assertEquals(fn !in listed, unknown, "$mode: ${fn.getName()}: ${e?.message}")
                if (!unknown && e != null) {
                    // the only acceptable failure for a listed function called without arguments is a missing argument
                    assertTrue(e.message!!.contains("Expected more arguments"), "$mode: ${fn.getName()}: ${e.message}")
                }
            }
        }
    }

    @Test
    fun testFunctions_ReturnsCopy() {
        val mode = ExpressionMode.INDICATOR_EXPRESSION
        assertNotSame(Expression.getFunctions(mode), Expression.getFunctions(mode))
        assertEquals(Expression.getFunctions(mode), Expression.getFunctions(mode))
    }

    @Test
    fun testFunctions_NamesAreUnique() {
        val duplicates = NamedFunction.entries.groupBy { it.getName() }.filterValues { it.size > 1 }.keys
        assertEquals(setOf(), duplicates)
        for (mode in ExpressionMode.entries) {
            val names = mode.fragments.mapNotNull { it.name() }
            assertEquals(names.size, names.toSet().size, "$mode has fragments with the same name")
        }
    }

    @Test
    fun testBinaryOperators() {
        val operators = Expression.getBinaryOperators()
        assertEquals(BinaryOperator.entries, operators)
        assertEquals(listOf("and"), BinaryOperator.AND.aliases)
        assertEquals(listOf("or"), BinaryOperator.OR.aliases)
        for (op in operators) {
            for (symbol in listOf(op.symbol) + op.aliases) {
                assertEquals(op, BinaryOperator.fromSymbol(symbol))
            }
        }
        assertEquals(true, Expression("1 < 2 and 2 < 3 or false", ExpressionMode.RULE_ENGINE_CONDITION).evaluate())
    }

    @Test
    fun testUnaryOperators() {
        assertEquals(listOf("not"), UnaryOperator.NOT.aliases)
        assertEquals(UnaryOperator.NOT, UnaryOperator.fromSymbol("not"))
        assertEquals(true, Expression("not false", ExpressionMode.RULE_ENGINE_CONDITION).evaluate())
    }

    @Test
    fun testUnaryOperators_DistinctOnlyWithAggregation() {
        for (mode in listOf(
            ExpressionMode.RULE_ENGINE_CONDITION,
            ExpressionMode.RULE_ENGINE_ACTION,
            ExpressionMode.ANDROID_CUSTOM_INTENT_EXPRESSION,
            ExpressionMode.VALIDATION_RULE_RESULT_TEST,
            ExpressionMode.INDICATOR_EXPRESSION)) {
            assertEquals(
                listOf(UnaryOperator.PLUS, UnaryOperator.MINUS, UnaryOperator.NOT),
                Expression.getUnaryOperators(mode), "$mode")
        }
        for (mode in listOf(
            ExpressionMode.PROGRAM_INDICATOR_EXPRESSION,
            ExpressionMode.PREDICTOR_GENERATOR_EXPRESSION,
            ExpressionMode.PREDICTOR_SKIP_TEST)) {
            assertEquals(UnaryOperator.entries, Expression.getUnaryOperators(mode), "$mode")
        }
    }
}
