package org.hisp.dhis.lib.expression.ast;

import org.hisp.dhis.lib.expression.spi.DataItem;
import org.hisp.dhis.lib.expression.spi.DataItemType;
import org.hisp.dhis.lib.expression.spi.ID;
import org.hisp.dhis.lib.expression.spi.QueryModifiers;
import org.hisp.dhis.lib.expression.spi.ValueType;
import org.hisp.dhis.lib.expression.spi.Variable;

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
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

/**
 * Implements the different {@link Node} types in the AST.
 *
 * @author Jan Bernitt
 */
public interface Nodes {

    static void supplySubExpressionSQL(Node<?> root) {
        root.transform((node, children) -> {
            if (node.getValue() == NamedFunction.subExpression) {
                children.forEach(child -> child.visit(NodeType.DATA_ITEM, modified ->  modified
                        .addModifier(new ModifierNode(NodeType.MODIFIER, DataItemModifier.subExpression.name())
                                .addChild(new ArgumentNode(NodeType.ARGUMENT, "0")
                                        .addChild(new TextNode(NodeType.STRING,"@"+System.identityHashCode(node)))))));
            }
            return children;
        });
    }

    /**
     * Modifiers affect data items only. However, they can be applied to data items directly or indirectly.
     * Within a function or round bracket that has a modifier all data items within the bracket body are affected.
     * <p>
     * This transformation moves modifiers from being {@link Node#children()} to be added as {@link Node#addModifier(Node)}.
     * <p>
     * This transformation should only be applied when the expression should be evaluated including resolving data items to their actual value.
     *
     * @param root the node to start the transformation from.
     */
    static void propagateModifiers(Node<?> root) {
        root.transform((node, children) -> {
            Predicate<Node<?>> isModifier = child -> child.getType() == NodeType.MODIFIER;
            if (node.getValue() instanceof NamedFunction && ((NamedFunction) node.getValue()).isAggregating()) {
                children.forEach(child -> child.visit(NodeType.DATA_ITEM, modified -> modified.addModifier(
                        new ModifierNode(NodeType.MODIFIER, DataItemModifier.periodAggregation.name()))));
            }
            if (children.stream().noneMatch(isModifier)) {
                return children;
            }
            // attach any modifier found on this level to any data item in the subtree of the child before them
            for (int i = 1; i < children.size(); i++) {
                Node<?> maybeModifier = children.get(i);
                if (maybeModifier.getType() == NodeType.MODIFIER) {
                    // go back 1 (or more if node before is a modifier)
                    int target = i-1;
                    while (target >= 0 && children.get(target).getType() == NodeType.MODIFIER) target--;
                    if (target >= 0) {
                        Consumer<Node<?>> addModifier = modified -> modified.addModifier(maybeModifier);
                        children.get(target).visit(NodeType.DATA_ITEM, addModifier);
                        children.get(target).visit(addModifier,
                                n -> n.getType() == NodeType.VARIABLE && n.getValue() == VariableType.PROGRAM);
                    }
                }
            }
            return children.stream().filter(not(isModifier)).collect(toList());
        });
    }

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
        public final Node<T> addChild(Node<?> child) {
            children.add(child);
            return this;
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
            return size() == 1 ? child(0).getValueType() : ValueType.MIXED;
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

    abstract class ModifiedNode<T> extends ComplexNode<T> {
        /**
         * This list of modifiers is always empty from pure parsing.
         * It is used to aggregate the effective modifiers in AST transformation step.
         */
        private final List<Node<?>> modifiers = new ArrayList<>();

        ModifiedNode(NodeType type, String rawValue, Function<String, T> converter) {
            super(type, rawValue, converter);
        }

        ModifiedNode(NodeType type, String rawValue, Function<String, T> converter, BiFunction<String, RuntimeException, RuntimeException> rethrowAs) {
            super(type, rawValue, converter, rethrowAs);
        }

        @Override
        public final Node<T> addModifier(Node<?> mod) {
            this.modifiers.add(mod);
            return this;
        }

        @Override
        public final Iterable<Node<?>> modifiers() {
            return modifiers;
        }

        final QueryModifiers getQueryModifiers() {
            QueryModifiers.QueryModifiersBuilder mods = QueryModifiers.builder();
            java.util.function.BinaryOperator<Integer> sum = (a, b) -> a == null ? b : a + b;
            modifiers.forEach(mod -> {
                Supplier<Object> value = () -> mod.child(0).child(0).getValue();
                switch ((DataItemModifier)mod.getValue()) {
                    case aggregationType: mods.aggregationType( (AggregationType) value.get()); break;
                    case maxDate: mods.maxDate((LocalDate) value.get()); break;
                    case minDate: mods.minDate( (LocalDate) value.get()); break;
                    case periodOffset: mods.periodOffset(sum.apply(mods.build().getPeriodOffset(), (Integer) value.get())); break;
                    case stageOffset: mods.stageOffset(sum.apply(mods.build().getStageOffset(), (Integer) value.get())); break;
                    case yearToDate: mods.yearToDate( true); break;
                    case periodAggregation: mods.periodAggregation(true); break;
                    case subExpression: mods.subExpression((String) value.get()); break;
                }
            });
            return mods.build();
        }
    }

    final class DataItemNode extends ModifiedNode<DataItemType> {

        /**
         * Just to cache the result
         */
        private DataItem value;

        public DataItemNode(NodeType type, String rawValue) {
            super(type, rawValue, DataItemType::fromSymbol, rethrowAs(DataItemType.class, DataItemType::getSymbol));
        }

        @Override
        public ValueType getValueType() {
            return ValueType.MIXED;
        }

        @Override
        public DataItem toDataItem() {
            if (value != null) return value;
            List<List<ID>> idGroups = new ArrayList<>(List.of(List.of(), List.of(), List.of()));
            DataItemType itemType = getValue();
            for (int i = 0; i < size(); i++) {
                Node<?> arg = child(i);
                Node<?> argC0 = arg.child(0);
                ID.Type type = argC0.getType() == NodeType.IDENTIFIER && argC0.getValue() instanceof Tag
                        ? ((Tag)argC0.getValue()).getIdType()
                        : itemType.getType(size(), i);
                List<ID> ids = arg.children()
                        .filter(n -> n.getType() == NodeType.UID)
                        .map(n -> new ID(type, n.getRawValue()))
                        .collect(toList());
                if (ids.isEmpty() && arg.size() == 1 && arg.child(0).getType() == NodeType.IDENTIFIER) {
                    ids = List.of(new ID(type, arg.child(0).getRawValue()));
                }
                idGroups.set(i, ids);
            }
            value = new DataItem(itemType, idGroups.get(0).get(0), idGroups.get(1), idGroups.get(2), getQueryModifiers());
            return value;
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

    final class VariableNode extends ModifiedNode<VariableType> {

        public VariableNode(NodeType type, String rawValue) {
            super(type, rawValue, VariableType::fromSymbol);
        }

        @Override
        public Variable toVariable() {
            if (getValue() != VariableType.PROGRAM) {
                return null;
            }
            return new Variable((ProgramVariable) child(0).getValue(), getQueryModifiers());
        }

        @Override
        public ValueType getValueType() {
            return isEmpty() ? ValueType.MIXED : child(0).getValueType();
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
