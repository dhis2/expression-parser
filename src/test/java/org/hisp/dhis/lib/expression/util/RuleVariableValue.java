package org.hisp.dhis.lib.expression.util;

import lombok.Builder;
import lombok.Value;
import org.hisp.dhis.lib.expression.spi.ValueType;
import org.hisp.dhis.lib.expression.spi.VariableValue;

import java.util.List;

@Value
@Builder(toBuilder = true, builderMethodName = "of")
public class RuleVariableValue implements VariableValue {

    ValueType valueType;
    String value;
    List<String> candidates;
    String eventDate;

    @Override
    public String value() {
        return value;
    }

    @Override
    public Object valueOrDefault() {
        switch (valueType) {
            case NUMBER: return 0d;
            case DATE: return "2010-01-01";
            case BOOLEAN: return false;
            default: return "";
        }
    }

    @Override
    public List<String> candidates() {
        return candidates;
    }

    @Override
    public String eventDate() {
        return eventDate;
    }

    @Override
    public ValueType valueType() {
        return valueType;
    }
}
