package com.alibaba.p3c.pmd.lang.java.rule.util;

import java.util.List;
import java.util.SortedMap;

import net.sourceforge.pmd.lang.ast.Node;


/**
 * Node sort utils
 */
public class NodeSortUtils {

    /**
     * add node to SortedMap with sequence to determine comment location
     * 
     * @param map sorted map
     * @param nodes nodes
     */
    public static void addNodesToSortedMap(SortedMap<Integer, Node> map, List<? extends Node> nodes) {
        for (Node node : nodes) {
            map.put(generateIndex(node), node);
        }
    }

    /**
     * set order according to node begin line and begin column
     * @param node node to sort
     * @return generated index
     */
    public static int generateIndex(Node node) {
        return (node.getBeginLine() << 16) + node.getBeginColumn();
    }
}
