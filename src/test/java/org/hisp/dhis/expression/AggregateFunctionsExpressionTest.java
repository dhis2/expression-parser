package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.eval.CalcNodeInterpreter;
import org.hisp.dhis.expression.syntax.ExpressionGrammar;
import org.hisp.dhis.expression.syntax.Parser;
import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.ID;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hisp.dhis.expression.spi.DataItemType.DATA_ELEMENT;
import static org.hisp.dhis.expression.spi.ID.Type.DataElementUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Jan Bernitt
 */
class AggregateFunctionsExpressionTest {

    @Test
    void testAvg() {
        Map<DataItem, Object> dataValues = Map.of(
                newDeDataItem("u1234567890"), new double[] { 0d, 10d, 5d, 3d, 7d });

        assertEquals(5.0d, evaluate("avg(#{u1234567890})", dataValues ));
    }

    @Test
    void testAvg2() {
        Map<DataItem, Object> dataValues = Map.of(
                newDeDataItem("u1234567890"), new double[] { 0d, 10d, 5d, 3d, 7d },
                newDeDataItem("v1234567890"), new double[] { 0d, 10d, 5d, 3d, 7d });

        assertEquals(10.0d, evaluate("avg(#{u1234567890} + #{v1234567890})", dataValues ));
    }

    @Test
    void testSum() {
        Map<DataItem, Object> dataValues = Map.of(
                newDeDataItem("u1234567890"), new double[] { 0d, 10d, 5d, 3d, 7d });

        assertEquals(25.0d, evaluate("sum(#{u1234567890})", dataValues ));
    }

    @Test
    void testMin() {
        Map<DataItem, Object> dataValues = Map.of(
                newDeDataItem("u1234567890"), new double[] { 0d, 10d, 5d, 3d, 7d });

        assertEquals(0d, evaluate("min(#{u1234567890})", dataValues ));
    }

    @Test
    void testMax() {
        Map<DataItem, Object> dataValues = Map.of(
                newDeDataItem("u1234567890"), new double[] { 0d, 10d, 5d, 3d, 7d });

        assertEquals(10.0d, evaluate("max(#{u1234567890})", dataValues ));
    }

    private static Double evaluate(String expression, Map<DataItem, Object> dataValues) {
        Node<?> root = Parser.parse(expression, ExpressionGrammar.Fragments);
        return (Double) root.eval(new CalcNodeInterpreter(items -> null, Map.of(), dataValues));
    }

    private static DataItem newDeDataItem(String u1234567890) {
        return new DataItem(DATA_ELEMENT, new ID(DataElementUID, u1234567890), b -> b.periodAggregation(true));
    }
}
