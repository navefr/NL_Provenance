import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;

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

    public ParseTree initialize(ParseTree parseTree) {
        ParseTree answerTree = copyTree(parseTree);
        deleteReturnPhrase(answerTree);

        return answerTree;
    }

    private void checkPrep(ParseTree answerTree, ParseTreeNode node) {
        if (node.relationship.equals("prep")) {
            if (!node.function.equals("NA")) {
                if (prepMap.containsKey(node.label.toLowerCase())) {
                    ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(node.wordOrder);
                    if (grandchildInAnswerTree != null) {
                        grandchildInAnswerTree.label = prepMap.get(node.label.toLowerCase());
                    }
                }
            }
        }
    }

    private void handleLogicalOperators(ParseTree answerTree, ParseTreeNode node, WordMappings wordReplacementMap) {
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
            Collection<ParseTreeNode> nodeSiblings = getSiblings(node);
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
                ParseTreeNode siblingToRemoveInAnswerTree = answerTree.searchNodeByOrder(siblingToRemove.wordOrder);
                answerTree.deleteSubTree(siblingToRemoveInAnswerTree);
            }
            if (!siblingsToRemove.isEmpty()) {
                ParseTreeNode logicalOperatorNodeInAnswerTree = answerTree.searchNodeByOrder(logicalOperatorNode.wordOrder);
                answerTree.deleteSubTree(logicalOperatorNodeInAnswerTree);
            }
        }
    }

    protected Collection<ParseTreeNode> getSiblings(ParseTreeNode node) {
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

    public ParseTree buildParseTree(ParseTree parseTree, WordMappings wordReplacementMap) {
        // Initialize
        ParseTree answerTree = initialize(parseTree);
        ParseTreeNode objectNode = answerTree.root.children.get(0);

        // Algorithm
        ParseTreeNode objectNodeInParseTree = parseTree.searchNodeByOrder(objectNode.wordOrder);
        boolean isNoModifierOrVerbChild = true;
        for (ParseTreeNode child : objectNodeInParseTree.children) {
            if (isModifier(child) || isVerb(child)) {
                isNoModifierOrVerbChild = false;
            }
        }

        String objectValue = getNodeValue(wordReplacementMap, objectNode);
        if (isNoModifierOrVerbChild) {
            if (objectValue != null) {
                answerTree.buildNode((new String[]{String.valueOf(-1), objectValue, "NA", String.valueOf(objectNode.wordOrder), "NA"}));
                answerTree.buildNode((new String[]{String.valueOf(-1), "is", "NA", String.valueOf(objectNode.wordOrder), "NA"}));
                answerTree.buildNode((new String[]{String.valueOf(-1), "the", "NA", String.valueOf(objectNode.wordOrder), "NA"}));
                for (ParseTreeNode child : objectNode.children) {
                    handleProperties(wordReplacementMap, answerTree, child);
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
                                    ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
                                    grandchildInAnswerTree.label = grandchildValue;
                                }
                            } else {
                                handleProperties(wordReplacementMap, answerTree, grandchild);

                                if (grandchild.children.size() > 1) {
                                    // Handle Logical Operators
                                    handleLogicalOperators(answerTree, grandchild, wordReplacementMap);
                                }
                            }
                        } else {
                            ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
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

        return answerTree;
    }

    private void handleProperties(WordMappings wordReplacementMap, ParseTree answerTree, ParseTreeNode currNode) {
        for (ParseTreeNode child : currNode.children) {
            String grandchildValue = getNodeValue(wordReplacementMap, child);
            if (grandchildValue != null) {
                ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(child.wordOrder);
                if (grandchildInAnswerTree != null) {
                    grandchildInAnswerTree.label = grandchildValue;
                }
            }
        }
        // Check Prep
        checkPrep(answerTree, currNode);
    }

    protected ParseTree copyTree(ParseTree parseTree) {
        ParseTree copyTree = new ParseTree();
        for (ParseTreeNode node : parseTree.allNodes) {
            if (node.parent != null) {
                copyTree.buildNode(new String[]{String.valueOf(node.wordOrder), node.label, node.pos, String.valueOf(node.parent.wordOrder), node.relationship});
                copyTree.searchNodeByOrder(node.wordOrder).tokenType = node.tokenType;
            }
        }
        return copyTree;
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

    protected abstract String getNodeValue(WordMappings wordReplacementMap, ParseTreeNode node);

    protected String getQuoatedString(String str) {
        if (str.startsWith("'") && str.endsWith("'")) {
            return str;
        } else if (str.startsWith("\"") && str.endsWith("\"")) {
            return str;
        } else {
            return "\"" + str + "\"";
        }
    }
}
