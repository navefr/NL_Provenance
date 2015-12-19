import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import simplenlg.framework.DocumentElement;
import simplenlg.framework.NLGElement;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.realiser.english.Realiser;

import java.util.*;

/**
 * Created by nfrost on 11/30/2015
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

    public ParseTree buildAnswerParseTree(ParseTree parseTree, Map<Integer, String> wordReplacementMap) {
        // Initialize
        ParseTree answerTree = copyTree(parseTree);
        deleteReturnPhrase(answerTree);
        ParseTreeNode objectNode = answerTree.root.children.get(0);
        if (wordReplacementMap.containsKey(objectNode.wordOrder)) {
            objectNode.label = wordReplacementMap.get(objectNode.wordOrder);
        }

        // Algorithm
        ParseTreeNode objectNodeInParseTree = parseTree.searchNodeByOrder(objectNode.wordOrder);
        for (ParseTreeNode child : objectNodeInParseTree.children) {
            if (child.relationship.equals("rcmod")) {
                if (child.pos.startsWith("VB")) {
                    for (ParseTreeNode grandchild : child.children) {
                        if (!grandchild.relationship.equals("nsubj")) {
                            if (wordReplacementMap.containsKey(grandchild.wordOrder)) {
                                ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
                                grandchildInAnswerTree.label = wordReplacementMap.get(grandchild.wordOrder);
                            }
                            // Check Prep
                            if (grandchild.relationship.equals("prep")) {
                                if (prepMap.containsKey(grandchild.label.toLowerCase())) {
                                    ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
                                    grandchildInAnswerTree.label = prepMap.get(grandchild.label.toLowerCase());
                                    ParseTreeNode childOfPrep = grandchild.children.get(0);

                                    if (wordReplacementMap.containsKey(childOfPrep.wordOrder)) {
                                        ParseTreeNode childOfPrepInAnswerTree = answerTree.searchNodeByOrder(childOfPrep.wordOrder);
                                        childOfPrepInAnswerTree.label = wordReplacementMap.get(childOfPrep.wordOrder);
                                    }
                                }
                            }
                        } else {
                            ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
                            if (grandchildInAnswerTree != null) {
                                answerTree.deleteNode(grandchildInAnswerTree);
                            }
                        }
                    }
                } else {
                    // TODO Nave - set is somewhere
                }
            } else {
                ParseTreeNode childInAnswerTree = answerTree.searchNodeByOrder(child.wordOrder);
                if (childInAnswerTree != null) {
                    answerTree.deleteNode(childInAnswerTree);
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
}
