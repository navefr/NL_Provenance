package ansgen;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import factorization.WordMappings;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import utils.ParseTreeUtil;

import java.util.*;

/**
 * Created by nfrost on 1/25/2016
 */
public abstract class AbstractAnswerParseTreeBuilder {

    private Map<String, String> prepMap;
    private Set<String> logicalOperators;

    protected AbstractAnswerParseTreeBuilder() {
        prepMap = new HashMap<>();
        prepMap.put("after", "in");
        prepMap.put("before", "in");
        prepMap.put("farther", "is");
        prepMap.put("closer", "is");

        logicalOperators = new HashSet<>();
        logicalOperators.add("and");
        logicalOperators.add("or");
    }

    public Pair<ParseTree, Map<ParseTreeNode, ParseTreeNode>> initialize(ParseTree parseTree) {
        Pair<ParseTree, Map<ParseTreeNode, ParseTreeNode>> copyTreeResult = ParseTreeUtil.copyTree(parseTree);
        ParseTree answerTree = copyTreeResult.getLeft();
        deleteReturnPhrase(answerTree);

        Map<ParseTreeNode, ParseTreeNode> copyTreeMapping = copyTreeResult.getRight();

        return new ImmutablePair<ParseTree, Map<ParseTreeNode, ParseTreeNode>>(answerTree, copyTreeMapping );
    }

    private void checkPrep(ParseTree answerTree, ParseTreeNode queryNode, Map<ParseTreeNode, ParseTreeNode> queryToAnswerNodeMapping) {
        if (queryNode.relationship.equals("prep")) {
            if (!queryNode.function.equals("NA")) {
                if (prepMap.containsKey(queryNode.label.toLowerCase())) {
                    replaceNodeValue(answerTree, queryNode, prepMap.get(queryNode.label.toLowerCase()), queryToAnswerNodeMapping);
                }
            }
        }
    }

    private void handleLogicalOperators(ParseTree answerTree, ParseTreeNode node, WordMappings wordReplacementMap, Map<ParseTreeNode, ParseTreeNode> queryToAnswerNodeMapping) {
        ParseTreeNode logicalOperatorNode = null;
        String value = null;
        for (ParseTreeNode child : node.children) {
            if (logicalOperators.contains(child.label)) {
                logicalOperatorNode = child;
            } else {
                value = getNodeValue(wordReplacementMap, child);
            }
        }

        if (logicalOperatorNode != null && value != null) {
            Collection<ParseTreeNode> nodeSiblings = ParseTreeUtil.getSiblings(node);
            Collection<ParseTreeNode> siblingsToRemove = new ArrayList<>();
            for (ParseTreeNode nodeSibling : nodeSiblings) {
                for (ParseTreeNode siblingChild : nodeSibling.children) {
                    String siblingChildValue = getNodeValue(wordReplacementMap, siblingChild);
                    if (siblingChildValue != null && value.equals(siblingChildValue)) {
                        siblingsToRemove.add(nodeSibling);
                    }
                }
            }

            for (ParseTreeNode siblingToRemove : siblingsToRemove) {
                ParseTreeNode siblingToRemoveInAnswerTree = queryToAnswerNodeMapping.get(siblingToRemove);
                answerTree.deleteSubTree(siblingToRemoveInAnswerTree);
            }
            if (!siblingsToRemove.isEmpty()) {
                ParseTreeNode logicalOperatorNodeInAnswerTree = queryToAnswerNodeMapping.get(logicalOperatorNode);
                answerTree.deleteSubTree(logicalOperatorNodeInAnswerTree);
            }
        }
    }

    public AnswerTreeBuilderResult buildParseTree(ParseTree parseTree, WordMappings wordReplacementMap) {
        // Initialize
        Pair<ParseTree, Map<ParseTreeNode, ParseTreeNode>> initializeResult = initialize(parseTree);
        ParseTree answerTree = initializeResult.getLeft();
        Map<ParseTreeNode, ParseTreeNode> copyTreeMapping = initializeResult.getRight();
        Map<ParseTreeNode, ParseTreeNode> copyTreeReverseMapping = new HashMap<ParseTreeNode, ParseTreeNode>();
        for (Map.Entry<ParseTreeNode, ParseTreeNode> entry : copyTreeMapping.entrySet()) {
            copyTreeReverseMapping.put(entry.getValue(), entry.getKey());
        }

        ParseTreeNode objectNode = answerTree.root.children.get(0);

        buildAnswerTree(wordReplacementMap, answerTree, objectNode, copyTreeMapping, copyTreeReverseMapping, true);

        Map<ParseTreeNode, Collection<ParseTreeNode>> answerTreeMapping = new HashMap<ParseTreeNode, Collection<ParseTreeNode>>();
        for (ParseTreeNode node : answerTree.allNodes) {
            if (copyTreeReverseMapping.containsKey(node)) {
                Collection<ParseTreeNode> mappings = answerTreeMapping.get(copyTreeReverseMapping.get(node));
                if (mappings == null) {
                    mappings = new HashSet<ParseTreeNode>();
                    answerTreeMapping.put(copyTreeReverseMapping.get(node), mappings);
                }
                mappings.add(node);
            }
        }

        return new AnswerTreeBuilderResult(answerTree, answerTreeMapping, 0);
    }

    private void buildAnswerTree(WordMappings wordReplacementMap, ParseTree answerTree, ParseTreeNode objectNode, Map<ParseTreeNode, ParseTreeNode> queryToAnswerNodeMapping, Map<ParseTreeNode, ParseTreeNode> answerToQueryNodeMapping, boolean firstCall) {
        // Algorithm
        ParseTreeNode objectNodeInParseTree = answerToQueryNodeMapping.get(objectNode);
        boolean isNoModifierOrVerbChild = true;
        for (ParseTreeNode child : objectNodeInParseTree.children) {
            if (isModifier(child) || isVerb(child)) {
                isNoModifierOrVerbChild = false;
            }
        }

        String objectValue = getNodeValue(wordReplacementMap, objectNodeInParseTree);
        if (!objectNodeInParseTree.children.isEmpty() && isNoModifierOrVerbChild) {
            if (objectValue != null) {
                int oldWordOrder = objectNode.wordOrder;
                ParseTreeUtil.shiftWordOrders(answerTree, Collections.EMPTY_LIST, objectNode.wordOrder, objectNode.wordOrder + 3);
                ParseTreeNode newObjectNode = answerTree.buildNode((new String[]{String.valueOf(oldWordOrder), objectValue, "NA", String.valueOf(objectNode.wordOrder), "NA"}));
                answerTree.buildNode((new String[]{String.valueOf(oldWordOrder + 1), "is", "NA", String.valueOf(objectNode.wordOrder), "NA"}));
                answerTree.buildNode((new String[]{String.valueOf(oldWordOrder + 2), "the", "NA", String.valueOf(objectNode.wordOrder), "NA"}));
                for (ParseTreeNode child : objectNode.children) {
                    handleProperties(wordReplacementMap, answerTree, child, queryToAnswerNodeMapping);
                }

                answerToQueryNodeMapping.remove(objectNode);
                answerToQueryNodeMapping.put(newObjectNode, objectNodeInParseTree);

                for (ParseTreeNode child : objectNode.children) {
                    for (ParseTreeNode grandchild : child.children) {
                        buildAnswerTree(wordReplacementMap, answerTree, grandchild, queryToAnswerNodeMapping, answerToQueryNodeMapping, false);
                    }
                }
            }
        } else {
            if (objectValue != null) {
                objectNode.label = objectValue;
            }

            for (ParseTreeNode child : objectNodeInParseTree.children) {
                if (isModifier(child) && isVerb(child)) {
                    for (ParseTreeNode grandchild : child.children) {
                        if (!grandchild.relationship.equals("nsubj")) {
                            if (grandchild.children.isEmpty()) {
                                String grandchildValue = getNodeValue(wordReplacementMap, grandchild);
                                if (grandchildValue != null) {
                                    replaceNodeValue(answerTree, grandchild, grandchildValue, queryToAnswerNodeMapping);
                                }
                            } else {
                                handleProperties(wordReplacementMap, answerTree, grandchild, queryToAnswerNodeMapping);

                                if (grandchild.children.size() > 1) {
                                    // Handle Logical Operators
                                    handleLogicalOperators(answerTree, grandchild, wordReplacementMap, queryToAnswerNodeMapping);
                                }
                            }
                        } else if (firstCall && !(isNoun(grandchild) || isVerb(grandchild))) {
                            ParseTreeNode grandchildInAnswerTree = queryToAnswerNodeMapping.get(grandchild);
                            if (grandchildInAnswerTree != null) {
                                answerTree.deleteSubTree(grandchildInAnswerTree);
                            }
                        }
                    }
                } else {
//                    ParseTreeNode childInAnswerTree = answerTree.searchNodeByOrder(child.wordOrder);
//                    if (childInAnswerTree != null) {
//                        answerTree.deleteSubTree(childInAnswerTree);
//                    }
                }
            }
        }
    }

    private void handleProperties(WordMappings wordReplacementMap, ParseTree answerTree, ParseTreeNode currNode, Map<ParseTreeNode, ParseTreeNode> queryToAnswerNodeMapping) {
        for (ParseTreeNode child : currNode.children) {
            String grandchildValue = getNodeValue(wordReplacementMap, child);
            if (grandchildValue != null) {
                replaceNodeValue(answerTree, child, grandchildValue, queryToAnswerNodeMapping);
            }
        }
        // Check Prep
        checkPrep(answerTree, currNode, queryToAnswerNodeMapping);
    }

    private void replaceNodeValue(ParseTree answerTree, ParseTreeNode queryNode, String nodeValue, Map<ParseTreeNode, ParseTreeNode> queryToAnswerNodeMapping) {
        ParseTreeNode nodeInAnswerTree = queryToAnswerNodeMapping.get(queryNode);
        if (nodeInAnswerTree != null) {
            nodeInAnswerTree.label = nodeValue;

            Collection<ParseTreeNode> nodesForDeletion = new LinkedList<ParseTreeNode>();
            for (ParseTreeNode childInAnswerTree : nodeInAnswerTree.children) {
                if (childInAnswerTree.relationship.equals("nn")) {
                    nodesForDeletion.add(childInAnswerTree);
                }
            }
            for (ParseTreeNode nodeForDeletion : nodesForDeletion) {
                answerTree.deleteNode(nodeForDeletion);
            }
        }
    }

    private void deleteReturnPhrase(ParseTree parseTree) {
        ParseTreeNode parseTreeNode1 = parseTree.searchNodeByOrder(1);
        ParseTreeNode parseTreeNode2 = parseTree.searchNodeByOrder(2);
        ParseTreeNode parseTreeNode3 = parseTree.searchNodeByOrder(3);
        if (parseTreeNode1.label.equalsIgnoreCase("return")) {
            parseTree.deleteNode(parseTreeNode1);
            if (parseTreeNode2.label.equalsIgnoreCase("the")) {
                parseTree.deleteNode(parseTreeNode2);
            } else if (parseTreeNode2.label.equalsIgnoreCase("me")) {
                parseTree.deleteNode(parseTreeNode2);
                if (parseTreeNode3.label.equalsIgnoreCase("the")) {
                    parseTree.deleteNode(parseTreeNode3);
                }
            }
        }
    }

    private boolean isModifier(ParseTreeNode node) {
        return node.relationship.equals("rcmod");
    }

    private boolean isVerb(ParseTreeNode node) {
        return node.pos.startsWith("VB");
    }

    private boolean isNoun(ParseTreeNode node) {
        return node.pos.startsWith("NN");
    }

    protected abstract String getNodeValue(WordMappings wordReplacementMap, ParseTreeNode node);
}
