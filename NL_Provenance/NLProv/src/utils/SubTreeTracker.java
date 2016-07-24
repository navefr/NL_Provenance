package utils;

import dataStructure.ParseTreeNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by nfrost on 7/24/2016
 */
public class SubTreeTracker {

    private Map<ParseTreeNode, Collection<ParseTreeNode>> subTrees;

    public SubTreeTracker() {
        subTrees = new HashMap<ParseTreeNode, Collection<ParseTreeNode>>();
    }

    public Collection<ParseTreeNode> getNodesInSubTree(ParseTreeNode node) {
        if (!subTrees.containsKey(node)) {
            Collection<ParseTreeNode> nodesInSubTree = new HashSet<ParseTreeNode>();
            subTrees.put(node, nodesInSubTree);

            nodesInSubTree.add(node);
            for (ParseTreeNode child : node.children) {
                getNodesInSubTree(nodesInSubTree, child);
            }
        }
        return subTrees.get(node);
    }

    private void getNodesInSubTree(Collection<ParseTreeNode> nodesInSubTree, ParseTreeNode node) {
        nodesInSubTree.add(node);
        for (ParseTreeNode child : node.children) {
            getNodesInSubTree(nodesInSubTree, child);
        }
    }

}
