package factorization;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import treedist.*;

import java.util.*;

/**
 * Created by nfrost on 2/7/2016
 */
public class ParseTreeScorer {
    private ParseTree queryTree;
    private ParseTree answerTree;
    Map<ParseTreeNode, Collection<ParseTreeNode>> nodeMappings;

    public ParseTreeScorer(ParseTree queryTree, ParseTree answerTree, Map<ParseTreeNode, Collection<ParseTreeNode>> nodeMappings) {
        copyTrees(queryTree, answerTree, nodeMappings);
        deleteUseless();
    }

    public double score() {
        ComparisonZhangShasha treeCorrector = new ComparisonZhangShasha();
        OpsZhangShasha costs = new OpsZhangShasha();

        Transformation transform = treeCorrector.findDistance(convertTreeDS(queryTree), convertTreeDS(answerTree), costs);

        return -transform.getCost();
    }

    private static BasicTree convertTreeDS(ParseTree parseTree) {
        Hashtable<String, ArrayList<String>> tree = new Hashtable<String, ArrayList<String>>();
        for (ParseTreeNode node : parseTree.allNodes) {
            ArrayList<String> nodeChildren = new ArrayList<String>();
            for (ParseTreeNode child : node.children) {
                nodeChildren.add(getNodeIdentifier(child));
            }
            tree.put(getNodeIdentifier(node), nodeChildren);
        }
        return new BasicTree(tree, getNodeIdentifier(parseTree.root), BasicTree.POSTORDER);
    }

    // TODO nave - won't work - since query and ans has diff wordOrder
    private static String getNodeIdentifier(ParseTreeNode node) {
        return node.label + node.wordOrder;
    }

    private void copyTrees(ParseTree queryTree, ParseTree answerTree, Map<ParseTreeNode, Collection<ParseTreeNode>> nodeMappings) {
        this.queryTree = new ParseTree();
        Map<ParseTreeNode, ParseTreeNode> queryTreeToCopyMapping = new HashMap<ParseTreeNode, ParseTreeNode>();
        for (ParseTreeNode node : queryTree.allNodes) {
            if (node.parent != null) {
                ParseTreeNode newNode = this.queryTree.buildNode(new String[]{String.valueOf(node.wordOrder), node.label, node.pos, String.valueOf(node.parent.wordOrder), node.relationship});
                newNode.tokenType = node.tokenType;
                queryTreeToCopyMapping.put(node, newNode);
            }
        }

        this.answerTree = new ParseTree();
        Map<ParseTreeNode, ParseTreeNode> answerTreeToCopyMapping = new HashMap<ParseTreeNode, ParseTreeNode>();
        for (ParseTreeNode node : answerTree.allNodes) {
            if (node.parent != null) {
                ParseTreeNode newNode = this.answerTree.buildNode(new String[]{String.valueOf(node.wordOrder), node.label, node.pos, String.valueOf(node.parent.wordOrder), node.relationship});
                newNode.tokenType = node.tokenType;
                answerTreeToCopyMapping.put(node, newNode);
            }
        }

        this.nodeMappings = new HashMap<>();
        for (Map.Entry<ParseTreeNode, Collection<ParseTreeNode>> nodeMappingEntry : nodeMappings.entrySet()) {
            ParseTreeNode oldQueryNode = nodeMappingEntry.getKey();
            ParseTreeNode newQueryNode = queryTreeToCopyMapping.get(oldQueryNode);
            Collection<ParseTreeNode> oldAnswerNodes = nodeMappingEntry.getValue();
            Collection<ParseTreeNode> newAnswerNodes = new HashSet<>();
            for (ParseTreeNode oldAnswerNode : oldAnswerNodes) {
                newAnswerNodes.add(answerTreeToCopyMapping.get(oldAnswerNode));
            }
            this.nodeMappings.put(newQueryNode, newAnswerNodes);
        }
    }

    private void deleteUseless() {
        Collection<ParseTreeNode> usefulAnswerNodes = new HashSet<ParseTreeNode>();
        usefulAnswerNodes.add(answerTree.root);
        for(int i = 0; i < queryTree.allNodes.size(); i++) {
            if(queryTree.allNodes.get(i).tokenType.equals("NA") || queryTree.allNodes.get(i).tokenType.equals("QT")) {
                ParseTreeNode curNode = queryTree.allNodes.get(i);
                if(curNode.label.equals("on") || curNode.label.equals("in") || curNode.label.equals("of") || curNode.label.equals("by")) {
                    if(!curNode.children.isEmpty()) {
                        curNode.children.get(0).prep = curNode.label;
                    }
                }

                if(curNode.tokenType.equals("QT")) {
                    curNode.parent.QT = curNode.function;
                }

                queryTree.deleteNode(curNode);
                i--;
            } else {
                Collection<ParseTreeNode> answerNodes = nodeMappings.get(queryTree.allNodes.get(i));
                if (answerNodes != null) {
                    usefulAnswerNodes.addAll(answerNodes);
                }
            }
        }

        for(int i = 0; i < answerTree.allNodes.size(); i++) {
            if(!usefulAnswerNodes.contains(answerTree.allNodes.get(i))) {
                ParseTreeNode curNode = answerTree.allNodes.get(i);
//                if(curNode.label.equals("on") || curNode.label.equals("in") || curNode.label.equals("of") || curNode.label.equals("by")) {
//                    if(!curNode.children.isEmpty()) {
//                        curNode.children.get(0).prep = curNode.label;
//                    }
//                }
//
//                if(curNode.tokenType.equals("QT")) {
//                    curNode.parent.QT = curNode.function;
//                }
//
                answerTree.deleteNode(curNode);
                i--;
            }
        }
    }
}
