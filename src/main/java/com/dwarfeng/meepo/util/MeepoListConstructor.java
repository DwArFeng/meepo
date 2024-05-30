package com.dwarfeng.meepo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

/**
 * 适用于 Meepo 项目的列表构造器。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class MeepoListConstructor extends Constructor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeepoListConstructor.class);

    private final Class<?> listClazz;

    private final Stack<Node> nodeStack = new Stack<>();

    public MeepoListConstructor(Class<?> listClazz, LoaderOptions loadingConfig) {
        super(loadingConfig);
        this.listClazz = listClazz;
    }

    @Override
    protected Object constructObject(Node node) {
        LOGGER.debug("构造对象：{}", node);
        nodeStack.push(node);
        parseRootSequenceNode(node);
        parseMappingNode(node);
        Object result = super.constructObject(node);
        nodeStack.pop();
        return result;
    }

    private void parseRootSequenceNode(Node node) {
        if (!(node instanceof SequenceNode) || !isRootNode(node)) {
            return;
        }
        LOGGER.debug("为根节点设置列表类型 {}", listClazz);
        ((SequenceNode) node).setListType(listClazz);
    }

    private boolean isRootNode(final Node node) {
        return nodeStack.size() == 1 && Objects.equals(nodeStack.peek(), node);
    }

    private void parseMappingNode(Node node) {
        if (!(node instanceof MappingNode)) {
            return;
        }
        List<NodeTuple> parsedNodeTuples = new ArrayList<>();
        for (NodeTuple nodeTuple : ((MappingNode) node).getValue()) {
            if (!(nodeTuple.getKeyNode() instanceof ScalarNode)) {
                parsedNodeTuples.add(nodeTuple);
                continue;
            }
            final ScalarNode keyNode = (ScalarNode) nodeTuple.getKeyNode();
            if (!Objects.equals(keyNode.getTag(), Tag.STR)) {
                parsedNodeTuples.add(nodeTuple);
                continue;
            }
            String keyNodeValue = keyNode.getValue();
            String parsedKeyNodeValue = snakeCaseToCamelCase(keyNodeValue);
            LOGGER.debug("转换键值 snake_case -> camelCase: {} -> {}", keyNodeValue, parsedKeyNodeValue);
            ScalarNode parsedKeyNode = new ScalarNode(
                    Tag.STR, parsedKeyNodeValue, keyNode.getStartMark(), keyNode.getEndMark(),
                    keyNode.getScalarStyle()
            );
            NodeTuple parsedNodeTuple = new NodeTuple(parsedKeyNode, nodeTuple.getValueNode());
            parsedNodeTuples.add(parsedNodeTuple);
        }
        ((MappingNode) node).setValue(parsedNodeTuples);
    }

    private String snakeCaseToCamelCase(String value) {
        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (char c : value.toCharArray()) {
            if (c == '_') {
                upperCase = true;
                continue;
            }
            if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
