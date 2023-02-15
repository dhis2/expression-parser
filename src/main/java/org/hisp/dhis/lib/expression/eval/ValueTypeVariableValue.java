package org.hisp.dhis.lib.expression.eval;

import lombok.Value;
import org.hisp.dhis.lib.expression.spi.ValueType;
import org.hisp.dhis.lib.expression.spi.VariableValue;

import java.util.List;

@Value
public class ValueTypeVariableValue implements VariableValue {

    ValueType valueType;

    @Override
    public String value() {
        return null;
    }

    @Override
    public Object valueOrDefault() {
        return null;
    }

    @Override
    public List<String> candidates() {
        return null;
    }

    @Override
    public String eventDate() {
        return null;
    }

    @Override
    public ValueType valueType() {
        return valueType;
    }
}
