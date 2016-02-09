package factorization;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import treedist.BasicTree;
import treedist.ComparisonZhangShasha;
import treedist.OpsZhangShasha;
import treedist.Transformation;

import java.util.*;

/**
 * Created by nfrost on 2/7/2016
 */
public class ParseTreeScorer {
    private ParseTree queryTree;
    private ParseTree answerTree;
    private Map<ParseTreeNode, Collection<ParseTreeNode>> nodeMappings;
    private Map<ParseTreeNode, ParseTreeNode> nodeMappingsReverse;

    public ParseTreeScorer(ParseTree queryTree, ParseTree answerTree, Map<ParseTreeNode, Collection<ParseTreeNode>> nodeMappings) {
        copyTrees(queryTree, answerTree, nodeMappings);
        deleteUseless();
    }

    public double score() {
        ComparisonZhangShasha treeCorrector = new ComparisonZhangShasha();
        OpsZhangShasha costs = new OpsZhangShasha();

        Pair<BasicTree, BasicTree> basicTrees = getBasicTrees();
        Transformation transform = treeCorrector.findDistance(basicTrees.getLeft(), basicTrees.getRight(), costs);

        return -transform.getCost();
    }


    private Pair<BasicTree, BasicTree> getBasicTrees() {
        Map<ParseTreeNode, String> queryNodeToIdentifier = new HashMap<ParseTreeNode, String>();
        Map<ParseTreeNode, String> answerNodeToIdentifier = new HashMap<ParseTreeNode, String>();

        Hashtable<String, ArrayList<String>> queryTreeTable = new Hashtable<String, ArrayList<String>>();
        for (ParseTreeNode node : queryTree.allNodes) {
            ArrayList<String> nodeChildren = new ArrayList<String>();
            for (ParseTreeNode child : node.children) {
                nodeChildren.add(getQueryNodeIdentifier(child, queryNodeToIdentifier));
            }
            queryTreeTable.put(getQueryNodeIdentifier(node, queryNodeToIdentifier), nodeChildren);
        }
        BasicTree queryBasicTree = new BasicTree(queryTreeTable, getQueryNodeIdentifier(queryTree.root, queryNodeToIdentifier), BasicTree.POSTORDER);

        Hashtable<String, ArrayList<String>> answerTreeTable = new Hashtable<String, ArrayList<String>>();
        for (ParseTreeNode node : answerTree.allNodes) {
            ArrayList<String> nodeChildren = new ArrayList<String>();
            for (ParseTreeNode child : node.children) {
                nodeChildren.add(getAnswerNodeIdentifier(child, queryNodeToIdentifier, answerNodeToIdentifier));
            }
            answerTreeTable.put(getAnswerNodeIdentifier(node, queryNodeToIdentifier, answerNodeToIdentifier), nodeChildren);
        }
        BasicTree answerBasicTree = new BasicTree(answerTreeTable, getAnswerNodeIdentifier(answerTree.root, queryNodeToIdentifier, answerNodeToIdentifier), BasicTree.POSTORDER);

        return new ImmutablePair<BasicTree, BasicTree>(queryBasicTree, answerBasicTree);
    }

    private String getQueryNodeIdentifier(ParseTreeNode node, Map<ParseTreeNode, String> queryNodeToIdentifier) {
        if (queryNodeToIdentifier.containsKey(node)){
            return queryNodeToIdentifier.get(node);
        }
        String identifier = node.label + node.wordOrder;
        queryNodeToIdentifier.put(node, identifier);
        return identifier;
    }

    private String getAnswerNodeIdentifier(ParseTreeNode node, Map<ParseTreeNode, String> queryNodeToIdentifier, Map<ParseTreeNode, String> answerNodeToIdentifier) {
        if (answerNodeToIdentifier.containsKey(node)){
            return answerNodeToIdentifier.get(node);
        }

        String identifier;
        ParseTreeNode queryNode = this.nodeMappingsReverse.get(node);
        if (queryNodeToIdentifier.containsKey(queryNode)){
            // TODO nave - not remove
            identifier = queryNodeToIdentifier.remove(queryNode);
        } else {
            identifier = node.label + node.wordOrder;
        }
        answerNodeToIdentifier.put(node, identifier);
        return identifier;
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
        this.nodeMappingsReverse = new HashMap<>();
        for (Map.Entry<ParseTreeNode, Collection<ParseTreeNode>> nodeMappingEntry : nodeMappings.entrySet()) {
            ParseTreeNode oldQueryNode = nodeMappingEntry.getKey();
            ParseTreeNode newQueryNode = queryTreeToCopyMapping.get(oldQueryNode);
            Collection<ParseTreeNode> oldAnswerNodes = nodeMappingEntry.getValue();
            Collection<ParseTreeNode> newAnswerNodes = new HashSet<>();
            for (ParseTreeNode oldAnswerNode : oldAnswerNodes) {
                newAnswerNodes.add(answerTreeToCopyMapping.get(oldAnswerNode));
            }
            this.nodeMappings.put(newQueryNode, newAnswerNodes);
            for (ParseTreeNode newAnswerNode : newAnswerNodes) {
                this.nodeMappingsReverse.put(newAnswerNode, newQueryNode);
            }

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
