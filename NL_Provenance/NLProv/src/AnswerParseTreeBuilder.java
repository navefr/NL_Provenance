import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by nfrost on 12/19/2015
 */
public class AnswerParseTreeBuilder {


    private static AnswerParseTreeBuilder instance = null;

    public static AnswerParseTreeBuilder getInstance(){
        if (instance == null) {
            instance = new AnswerParseTreeBuilder();
        }
        return instance;
    }


    private Map<String, String> prepMap;
    private AnswerParseTreeBuilder() {
        prepMap = new HashMap<>();
        prepMap.put("after", "in");
        prepMap.put("before", "in");
        prepMap.put("farther", "is");
        prepMap.put("closer", "is");

    }

    public ParseTree initialize(ParseTree parseTree) {
        ParseTree answerTree = copyTree(parseTree);
        deleteReturnPhrase(answerTree);

        return answerTree;
    }

    private void checkPrep(ParseTree answerTree, ParseTreeNode node, Map<Integer, String> wordReplacementMap, boolean flag) {
        if (node.relationship.equals("prep")) {
            if (flag == true) {
                if (!node.function.equals("NA")) {
                    if (prepMap.containsKey(node.label.toLowerCase())) {
                        ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(node.wordOrder);
                        grandchildInAnswerTree.label = prepMap.get(node.label.toLowerCase());
                        ParseTreeNode childOfPrep = node.children.get(0);

                        if (wordReplacementMap.containsKey(childOfPrep.wordOrder)) {
                            ParseTreeNode childOfPrepInAnswerTree = answerTree.searchNodeByOrder(childOfPrep.wordOrder);
                            childOfPrepInAnswerTree.label = wordReplacementMap.get(childOfPrep.wordOrder);
                        }
                    }
                }
            } else {
                // Todo nave - complete switch part
            }
        }
    }

    public ParseTree buildAnswerParseTree(ParseTree parseTree, Map<Integer, String> wordReplacementMap) {
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

        if (isNoModifierOrVerbChild) {
            if (wordReplacementMap.containsKey(objectNode.wordOrder)) {
                String objectValue = wordReplacementMap.get(objectNode.wordOrder);
                answerTree.buildNode((new String[]{String.valueOf(-1), objectValue, "NA", String.valueOf(objectNode.wordOrder), "NA"}));
                answerTree.buildNode((new String[]{String.valueOf(-1), "is", "NA", String.valueOf(objectNode.wordOrder), "NA"}));
                answerTree.buildNode((new String[]{String.valueOf(-1), "the", "NA", String.valueOf(objectNode.wordOrder), "NA"}));
                for (ParseTreeNode child : objectNodeInParseTree.children) {
                    checkPrep(answerTree, child, wordReplacementMap, false);
                }
            }
        } else {
            if (wordReplacementMap.containsKey(objectNode.wordOrder)) {
                objectNode.label = wordReplacementMap.get(objectNode.wordOrder);
            }

            for (ParseTreeNode child : objectNodeInParseTree.children) {
                if (isModifier(child) && isVerb(child)) {
                    for (ParseTreeNode grandchild : child.children) {
                        if (!grandchild.relationship.equals("nsubj")) {
                            if (wordReplacementMap.containsKey(grandchild.wordOrder)) {
                                ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
                                grandchildInAnswerTree.label = wordReplacementMap.get(grandchild.wordOrder);
                            }
                            // Check Prep
                            checkPrep(answerTree, grandchild, wordReplacementMap, true);
                        } else {
                            ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
                            if (grandchildInAnswerTree != null) {
                                answerTree.deleteNode(grandchildInAnswerTree);
                            }
                        }
                    }
                } else {
                    ParseTreeNode childInAnswerTree = answerTree.searchNodeByOrder(child.wordOrder);
                    if (childInAnswerTree != null) {
                        answerTree.deleteNode(childInAnswerTree);
                    }
                }
            }
        }


        return answerTree;
    }

    private ParseTree copyTree(ParseTree parseTree) {
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
}
