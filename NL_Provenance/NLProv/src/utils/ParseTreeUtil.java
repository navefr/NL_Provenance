package utils;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: NAVE-FROST
 * Date: 08/02/16
 * Time: 14:12
 */
public class ParseTreeUtil {

    public static Pair<ParseTree, Map<ParseTreeNode, ParseTreeNode>> copyTree(ParseTree parseTree) {
        ParseTree copyTree = new ParseTree();
        Map<ParseTreeNode, ParseTreeNode> mapping = new HashMap<ParseTreeNode, ParseTreeNode>();
        for (ParseTreeNode node : parseTree.allNodes) {
            if (node.parent != null) {
                ParseTreeNode newNode = copyTree.buildNode(new String[]{String.valueOf(node.wordOrder), node.label, node.pos, String.valueOf(node.parent.wordOrder), node.relationship});
                newNode.tokenType = node.tokenType;
                mapping.put(node, newNode);
            }
        }
        return new ImmutablePair<ParseTree, Map<ParseTreeNode, ParseTreeNode>>(copyTree, mapping);
    }

    public static Collection<ParseTreeNode> getSiblings(ParseTreeNode node) {
        Collection<ParseTreeNode> siblings = new ArrayList<>();
        if (node.parent != null) {
            for (ParseTreeNode possibleSibling : node.parent.children) {
                if (!possibleSibling.equals(node)) {
                    siblings.add(possibleSibling);
                }
            }
        }
        return siblings;
    }

    public static int getFirstWordOrderInSubTrees(Collection<ParseTreeNode> nodes) {
        int firstWordOrder = Integer.MAX_VALUE;
        for (ParseTreeNode currNode : nodes) {
            firstWordOrder = Math.min(firstWordOrder, getFirstWordOrderInSubTree(currNode));
        }
        return firstWordOrder;
    }

    public static int getFirstWordOrderInSubTree(ParseTreeNode node) {
        int firstWordOrder = Integer.MAX_VALUE;
        Collection<ParseTreeNode> nodesInSubTree = new HashSet<>();
        getNodesInSubTree(nodesInSubTree, node);
        for (ParseTreeNode currNode : nodesInSubTree) {
            firstWordOrder = Math.min(firstWordOrder, currNode.wordOrder);
        }
        return firstWordOrder;
    }

    public static int getLastWordOrderInSubTree(ParseTreeNode node) {
        int lastWordOrder = Integer.MIN_VALUE;
        Collection<ParseTreeNode> nodesInSubTree = new HashSet<>();
        getNodesInSubTree(nodesInSubTree, node);
        for (ParseTreeNode currNode : nodesInSubTree) {
            lastWordOrder = Math.max(lastWordOrder, currNode.wordOrder);
        }
        return lastWordOrder;
    }

    public static Collection<ParseTreeNode> getNodesInSubTree(ParseTreeNode node) {
        Collection<ParseTreeNode> nodesInSubTree = new HashSet<ParseTreeNode>();
        nodesInSubTree.add(node);
        for (ParseTreeNode child : node.children) {
            getNodesInSubTree(nodesInSubTree, child);
        }
        return nodesInSubTree;
    }

    private static void getNodesInSubTree(Collection<ParseTreeNode> nodesInSubTree, ParseTreeNode node) {
        nodesInSubTree.add(node);
        for (ParseTreeNode child : node.children) {
            getNodesInSubTree(nodesInSubTree, child);
        }
    }

    public static ParseTreeNode getJointParent(Collection<ParseTreeNode> nodes) {
        ParseTreeNode aNode = nodes.iterator().next();
        while (!(aNode.label.equals("ROOT") || isJointParent(aNode, nodes))) {
            aNode = aNode.parent;
        }
        return aNode;
    }

    private static boolean isJointParent(ParseTreeNode potentialParent, Collection<ParseTreeNode> nodes) {
        if (nodes.contains(potentialParent)) {
            return false;
        }

        Collection<ParseTreeNode> nodesInSubTree = new HashSet<>();
        getNodesInSubTree(nodesInSubTree, potentialParent);
        for (ParseTreeNode node : nodes) {
            if (!nodesInSubTree.contains(node)) {
                return false;
            }
        }
        return true;
    }


    public static void shiftWordOrders(ParseTree parseTree, Collection<ParseTreeNode> exclude, int from, int amount) {
        for (ParseTreeNode node : parseTree.allNodes) {
            if (node.wordOrder >= from && !exclude.contains(node)) {
                node.wordOrder = node.wordOrder + amount;
            }
        }
    }
}
