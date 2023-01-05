package org.hisp.dhis.expression.ast;

import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.DataItemModifiers;
import org.hisp.dhis.expression.spi.DataItemType;
import org.hisp.dhis.expression.spi.ID;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * Implements the different {@link Node} types in the AST.
 *
 * @author Jan Bernitt
 */
public interface Nodes {

    abstract class AbstractNode<T> implements Node<T> {

        final NodeType type;
        final String rawValue;
        final T value;

        AbstractNode(NodeType type, String rawValue, Function<String, T> converter) {
            this(type, rawValue, converter, (val, ex) -> ex);
        }

        AbstractNode(NodeType type, String rawValue, Function<String, T> converter, BiFunction<String,RuntimeException,RuntimeException> rethrowAs) {
            this.type = type;
            this.rawValue = rawValue;
            try {
                this.value = converter.apply(rawValue);
            } catch (RuntimeException ex) {
                throw rethrowAs.apply(rawValue,ex);
            }
        }

        static <E extends Enum<E>> BiFunction<String,RuntimeException,RuntimeException> rethrowAs(Class<E> valueType, Function<E,String> toText) {
            return (rawValue, ex) -> new IllegalArgumentException(format("Invalid %s option: '%s'%n\toptions are: %s",
                    valueType.getSimpleName(), rawValue, Stream.of(valueType.getEnumConstants()).map(toText).collect(toList())));
        }

        @Override
        public final NodeType getType() {
            return type;
        }

        @Override
        public final String getRawValue() {
            return rawValue;
        }

        @Override
        public final T getValue()
        {
            return value;
        }

        @Override
        public void visit(Consumer<Node<?>> visitor, Predicate<Node<?>> filter) {
            if (filter.test(this)) {
                visitor.accept(this);
            }
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            toString(str, "");
            return str.toString();
        }

        void toString(StringBuilder str, String indent) {
            str.append(indent).append(getClass().getSimpleName())
                    .append('[').append(getType().name()).append(" ").append(getRawValue()).append("]\n");
        }
    }

    abstract class ComplexNode<T> extends AbstractNode<T> {

        private List<Node<?>> children = new ArrayList<>();

        ComplexNode(NodeType type, String rawValue, Function<String, T> converter) {
            super(type, rawValue, converter);
        }

        ComplexNode(NodeType type, String rawValue, Function<String, T> converter, BiFunction<String, RuntimeException, RuntimeException> rethrowAs) {
            super(type, rawValue, converter, rethrowAs);
        }

        @Override
        public final int size() {
            return children.size();
        }

        @Override
        public final Node<?> child(int index) {
            return children.get(index);
        }

        @Override
        public Stream<Node<?>> children() {
            return children.stream();
        }

        @Override
        public final void addChild(Node<?> child) {
            children.add(child);
        }

        @Override
        public final void transform(BiFunction<Node<?>,List<Node<?>>,List<Node<?>>> transformer) {
            children = transformer.apply(this, children);
            children.forEach(c -> c.transform(transformer));
        }

        @Override
        public final void visit(Consumer<Node<?>> visitor, Predicate<Node<?>> filter) {
            super.visit(visitor, filter);
            children.forEach(child -> child.visit(visitor, filter));
        }

        @Override
        final void toString(StringBuilder str, String indent) {
            super.toString(str, indent);
            for (Node<?> c : children)
                ((AbstractNode<?>)c).toString(str, indent+"  ");
        }
    }

    final class ParenthesesNode extends ComplexNode<String> {

        public ParenthesesNode(NodeType type, String rawValue) {
            super(type, rawValue, Function.identity());
        }

        @Override
        public ValueType getValueType() {
            return child(0).getValueType();
        }
    }

    final class ArgumentNode extends ComplexNode<Integer> {

        public ArgumentNode(NodeType type, String rawValue) {
            super(type, rawValue, Integer::valueOf);
        }

        @Override
        public ValueType getValueType() {
            return size() == 1 ? child(0).getValueType() : ValueType.UNKNOWN;
        }
    }

    final class FunctionNode extends ComplexNode<NamedFunction> {

        public FunctionNode(NodeType type, String rawValue) {
            super(type, rawValue, NamedFunction::fromName, rethrowAs(NamedFunction.class, NamedFunction::getName));
        }

        @Override
        public ValueType getValueType() {
            ValueType type = getValue().getValueType();
            return !type.isSame() ? type : child(getValue().getParameterTypes().indexOf(ValueType.SAME)).getValueType();
        }
    }

    final class ModifierNode extends ComplexNode<DataItemModifier> {

        public ModifierNode(NodeType type, String rawValue) {
            super(type, rawValue, DataItemModifier::valueOf);
        }

        @Override
        public ValueType getValueType() {
            return getValue().getValueType();
        }
    }

    final class DataItemNode extends ComplexNode<DataItemType> {

        /**
         * This list of modifiers is always empty from pure parsing.
         * It is used to aggregate the effective modifiers in AST transformation step.
         */
        private final List<Node<?>> modifiers = new ArrayList<>();

        public DataItemNode(NodeType type, String rawValue) {
            super(type, rawValue, DataItemType::fromSymbol, rethrowAs(DataItemType.class, DataItemType::getSymbol));
        }

        @Override
        public void addModifier(Node<?> mod) {
            this.modifiers.add(mod);
        }

        @Override
        public Iterable<Node<?>> modifiers() {
            return modifiers;
        }

        @Override
        public ValueType getValueType() {
            return ValueType.UNKNOWN;
        }

        @Override
        public DataItem toDataItem() {
            List<List<ID>> idGroups = new ArrayList<>(List.of(List.of(), List.of(), List.of()));
            DataItemType itemType = getValue();
            for (int i = 0; i < size(); i++) {
                Node<?> arg = child(i);
                Node<?> argC0 = arg.child(0);
                ID.Type type = argC0.getType() == NodeType.IDENTIFIER && argC0.getValue() instanceof Tag
                        ? ((Tag)argC0.getValue()).getIdType()
                        : itemType.getType(size(), i);
                idGroups.set(i, arg.children()
                        .filter(n -> n.getType() == NodeType.UID)
                        .map(n -> new ID(type, n.getRawValue()))
                        .collect(toList()));
            }
            return new DataItem(itemType, idGroups.get(0).get(0), idGroups.get(1), idGroups.get(2), dataItemModifiersOf(modifiers));
        }

        static DataItemModifiers dataItemModifiersOf(List<Node<?>> modifiers) {
            DataItemModifiers.DataItemModifiersBuilder mods = DataItemModifiers.builder();
            modifiers.forEach(mod -> {
                Supplier<Object> value = () -> mod.child(0).child(0).getValue();
                switch ((DataItemModifier)mod.getValue()) {
                        case aggregationType: mods.aggregationType( (AggregationType) value.get()); break;
                        case maxDate: mods.maxDate((LocalDate) value.get()); break;
                        case minDate: mods.minDate( (LocalDate) value.get()); break;
                        case periodOffset: mods.periodOffset( (Integer) value.get()); break;
                        case stageOffset: mods.stageOffset((Integer) value.get()); break;
                        case yearToDate: mods.yearToDate( true); break;
                        case periodAggregation: mods.periodAggregation(true); break;
                    }
                });
            return mods.build();
        }
    }

    abstract class SimpleNode<T> extends AbstractNode<T> {

        SimpleNode(NodeType type, String rawValue, Function<String, T> converter) {
            super(type, rawValue, converter);
        }

        SimpleNode(NodeType type, String rawValue, Function<String, T> converter, BiFunction<String, RuntimeException, RuntimeException> rethrowAs) {
            super(type, rawValue, converter, rethrowAs);
        }

        @Override
        public ValueType getValueType() {
            return ValueType.STRING;
        }
    }

    class TextNode extends SimpleNode<String> {

        public TextNode(NodeType type, String rawValue) {
            super(type, rawValue, Function.identity());
        }

    }

    final class VariableNode extends ComplexNode<VariableType> {

        public VariableNode(NodeType type, String rawValue) {
            super(type, rawValue, VariableType::fromSymbol);
        }

        @Override
        public ValueType getValueType() {
            return isEmpty() ? ValueType.UNKNOWN : child(0).getValueType();
        }
    }

    final class Utf8StringNode extends SimpleNode<String> {

        public Utf8StringNode(NodeType type, String rawValue) {
            super(type, rawValue, Utf8StringNode::decode);
        }

        static String decode(String rawValue) {
            if (rawValue.indexOf('\\') < 0) {
                return rawValue; // no special characters
            }
            StringBuilder str = new StringBuilder();
            char[] chars = rawValue.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == '\\') {
                    c = chars[++i];
                    if (c == 'u') {
                        str.appendCodePoint(parseInt(new String(new char[] { chars[++i], chars[++i], chars[++i], chars[++i]}), 16));
                    } else if (c >= '0' && c <= '9') {
                        str.appendCodePoint(parseInt(new String(new char[] { chars[++i], chars[++i], chars[++i] }), 8));
                    } else {
                        switch (c) {
                            case 'b': str.append('\b'); break;
                            case 't': str.append('\t'); break;
                            case 'n': str.append('\n'); break;
                            case 'f': str.append('\f'); break;
                            case 'r': str.append('\r'); break;
                            default: str.append(c); // this is the escaped character
                        }
                    }
                } else {
                    str.append(c);
                }
            }
            return str.toString();
        }
    }

    final class UnaryOperatorNode extends ComplexNode<UnaryOperator> {

        public UnaryOperatorNode(NodeType type, String rawValue) {
            super(type, rawValue, UnaryOperator::fromSymbol, rethrowAs(UnaryOperator.class, UnaryOperator::getSymbol));
        }

        @Override
        public ValueType getValueType() {
            ValueType type = getValue().getValueType();
            return type.isSame() ? child(0).getValueType() : type;
        }
    }

    final class BinaryOperatorNode extends ComplexNode<BinaryOperator> {

        public BinaryOperatorNode(NodeType type, String rawValue) {
            super(type, rawValue, BinaryOperator::fromSymbol, rethrowAs(BinaryOperator.class, BinaryOperator::getSymbol));
        }

        @Override
        public ValueType getValueType() {
            return getValue().getValueType();
        }
    }

    final class BooleanNode extends SimpleNode<Boolean> {

        public  BooleanNode(NodeType type, String rawValue) {
            super(type, rawValue, Boolean::valueOf);
        }

        @Override
        public ValueType getValueType() {
            return ValueType.BOOLEAN;
        }
    }

    final class NumberNode extends SimpleNode<Double> {

        public  NumberNode(NodeType type, String rawValue) {
            super(type, rawValue, Double::valueOf);
        }

        @Override
        public ValueType getValueType() {
            return ValueType.NUMBER;
        }
    }

    final class IntegerNode extends SimpleNode<Integer> {

        public IntegerNode(NodeType type, String rawValue) {
            super(type, rawValue, Integer::valueOf);
        }

        @Override
        public ValueType getValueType() {
            return ValueType.NUMBER;
        }
    }

    final class DateNode extends SimpleNode<LocalDate> {

        public DateNode(NodeType type, String rawValue) {
            super(type, rawValue, LocalDate::parse);
        }

        @Override
        public ValueType getValueType() {
            return ValueType.DATE;
        }
    }

    final class ConstantNode extends SimpleNode<Void> {

        public ConstantNode(NodeType type, String rawValue) {
            super(type, rawValue, str -> null);
        }

        @Override
        public ValueType getValueType() {
            return ValueType.SAME;
        }
    }

    final class ReportingRateTypeNode extends SimpleNode<ReportingRateType> {

        public ReportingRateTypeNode(NodeType type, String rawValue) {
            super(type, rawValue, ReportingRateType::valueOf, rethrowAs(ReportingRateType.class, ReportingRateType::name));
        }
    }

    final class ProgramVariableNode extends SimpleNode<ProgramVariable> {

        public ProgramVariableNode(NodeType type, String rawValue) {
            super(type, rawValue, ProgramVariable::valueOf, rethrowAs(ProgramVariable.class, ProgramVariable::name));
        }
    }

    final class NamedValueNode extends SimpleNode<NamedValue>
    {
        public NamedValueNode(NodeType type, String rawValue) {
            super(type, rawValue, NamedValue::valueOf, rethrowAs(NamedValue.class, NamedValue::name));
        }
    }

    final class TagNode extends SimpleNode<Tag> {
        public TagNode(NodeType type, String rawValue) {
            super(type, rawValue, Tag::valueOf, rethrowAs(Tag.class, Tag::name));
        }
    }

    final class AggregationTypeNode extends SimpleNode<AggregationType> {
        public AggregationTypeNode(NodeType type, String rawValue) {
            super(type, rawValue, AggregationType::valueOf, rethrowAs(AggregationType.class, AggregationType::name));
        }
    }
}
