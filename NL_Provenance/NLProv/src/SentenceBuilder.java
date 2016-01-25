import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import simplenlg.framework.DocumentElement;
import simplenlg.framework.NLGElement;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.realiser.english.Realiser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by nfrost on 11/30/2015
 */
public class SentenceBuilder {

    private static SentenceBuilder instance = null;

    private NLGFactory nlgFactory;
    private Realiser realiser;

    public static SentenceBuilder getInstance(){
        if (instance == null) {
            instance = new SentenceBuilder();
        }
        return instance;
    }

    private SentenceBuilder() {
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        nlgFactory = new NLGFactory(lexicon);
        realiser = new Realiser(lexicon);
    }

    public String buildSentence(ParseTree parseTree) {

        ArrayList<ParseTreeNode> orderedTreeNodes = new ArrayList<>();
        for (ParseTreeNode originalParseTreeNode : parseTree.allNodes) {
            orderedTreeNodes.add(originalParseTreeNode);
        }
        Collections.sort(orderedTreeNodes, new Comparator<ParseTreeNode>() {
            public int compare(ParseTreeNode o1, ParseTreeNode o2) {
                return o1.wordOrder - o2.wordOrder;
            }
        });

        StringBuilder sentence = new StringBuilder();
        for (ParseTreeNode parseTreeNode : orderedTreeNodes) {
            if (parseTreeNode.parent != null && !parseTreeNode.tokenType.equals("CMT")) {
                sentence.append(parseTreeNode.label).append(" ");
            }
        }
        sentence.deleteCharAt(sentence.length() - 1);
        return sentence.toString();
    }

    public String buildSentence(ParseTree parseTree, Map<Integer, String> wordReplacementMap) {
        parseTree = copyTree(parseTree);
        deleteReturnPhrase(parseTree);

        ArrayList<ParseTreeNode> orderedTreeNodes = new ArrayList<>();
        for (ParseTreeNode originalParseTreeNode : parseTree.allNodes) {
            orderedTreeNodes.add(originalParseTreeNode);
        }
        Collections.sort(orderedTreeNodes, new Comparator<ParseTreeNode>() {
            public int compare(ParseTreeNode o1, ParseTreeNode o2) {
                return o1.wordOrder - o2.wordOrder;
            }
        });

        DocumentElement sentence = nlgFactory.createSentence();
        for (ParseTreeNode parseTreeNode : orderedTreeNodes) {
            if (parseTreeNode.parent != null && !parseTreeNode.tokenType.equals("CMT")) {
                String word = getCurrentWord(parseTreeNode, wordReplacementMap);

                NLGElement phraseElement = null;
                if (parseTreeNode.pos.startsWith("VB")) {
                    phraseElement = nlgFactory.createVerbPhrase(word);
                } else if (parseTreeNode.pos.startsWith("JJ")) {
                    phraseElement = nlgFactory.createAdjectivePhrase(word);
                } else if (parseTreeNode.pos.startsWith("NN")) {
                    phraseElement = nlgFactory.createNounPhrase(word);
                } else if (parseTreeNode.pos.startsWith("RB")) {
                    phraseElement = nlgFactory.createAdverbPhrase(word);
                } else {
                    phraseElement = nlgFactory.createStringElement(word);
                }
                sentence.addComponent(phraseElement);
            }
        }

        return realiser.realiseSentence(sentence);
    }


    public ParseTree buildSubstituteTree(ParseTree parseTree, Map<Integer, String> wordReplacementMap) {
        ParseTree substituteTree = copyTree(parseTree);
        deleteReturnPhrase(substituteTree);
        for (ParseTreeNode parseTreeNode : substituteTree.allNodes) {
            int wordOrder = parseTreeNode.wordOrder;
            if (wordReplacementMap.containsKey(wordOrder)) {
                parseTreeNode.label = getCurrentWord(parseTreeNode, wordReplacementMap);

                if (parseTreeNode.tokenType.startsWith("VT")) {
                    if (parseTreeNode.parent != null && parseTreeNode.parent.tokenType.equals("OT")) {
                        parseTreeNode.parent.label = "=";
                    }
                }
            }
        }

        return substituteTree;
    }

    public ParseTree buildAppendTree(ParseTree parseTree, Map<Integer, String> wordReplacementMap) {
        ParseTree appendTree = copyTree(parseTree);
        deleteReturnPhrase(appendTree);
        int newWordOrder = 10000;
        ArrayList<ParseTreeNode> allNodes = new ArrayList<>(appendTree.allNodes);
        for (ParseTreeNode parseTreeNode : allNodes) {
            int wordOrder = parseTreeNode.wordOrder;
            if (wordReplacementMap.containsKey(wordOrder)) {
                appendTree.buildNode(new String[]{String.valueOf(newWordOrder), "is", "NA", String.valueOf(wordOrder), "NA"});
                newWordOrder++;

                appendTree.buildNode(new String[]{String.valueOf(newWordOrder), wordReplacementMap.get(wordOrder), "NA", String.valueOf(wordOrder), "NA"});
                newWordOrder++;
            }
        }
        return appendTree;
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
        if (parseTreeNode1 != null && parseTreeNode1.label.equalsIgnoreCase("return")) {
            parseTree.deleteNode(parseTreeNode1);
            if (parseTreeNode2 != null && parseTreeNode2.label.equalsIgnoreCase("the")) {
                parseTree.deleteNode(parseTreeNode2);
            } else if (parseTreeNode2 != null && parseTreeNode2.label.equalsIgnoreCase("me")) {
                parseTree.deleteNode(parseTreeNode2);
                if (parseTreeNode3 != null && parseTreeNode3.label.equalsIgnoreCase("the")) {
                    parseTree.deleteNode(parseTreeNode3);
                }
            }
        }
    }

    private String getCurrentWord(ParseTreeNode parseTreeNode, Map<Integer, String> wordReplacementMap) {
        int wordOrder = parseTreeNode.wordOrder;
        if (wordReplacementMap.containsKey(wordOrder)) {
            return wordReplacementMap.get(wordOrder);
        } else {
            return  parseTreeNode.label;
        }
    }
}
