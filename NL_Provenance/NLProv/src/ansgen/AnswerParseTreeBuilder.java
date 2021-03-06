//import dataStructure.ParseTree;
//import dataStructure.ParseTreeNode;
//
//import java.util.*;
//
///**
// * Created by nfrost on 12/19/2015
// */
//public class AnswerParseTreeBuilder {
//
//
//    private static AnswerParseTreeBuilder instance = null;
//
//    public static AnswerParseTreeBuilder getInstance(){
//        if (instance == null) {
//            instance = new AnswerParseTreeBuilder();
//        }
//        return instance;
//    }
//
//
//    private Map<String, String> prepMap;
//    private Set<String> logicalOperators;
//
//    private AnswerParseTreeBuilder() {
//        prepMap = new HashMap<>();
//        prepMap.put("after", "in");
//        prepMap.put("before", "in");
//        prepMap.put("farther", "is");
//        prepMap.put("closer", "is");
//
//        logicalOperators = new HashSet<>();
//        logicalOperators.add("and");
//        logicalOperators.add("or");
//
//    }
//
//    public ParseTree initialize(ParseTree parseTree) {
//        ParseTree answerTree = copyTree(parseTree);
//        deleteReturnPhrase(answerTree);
//
//        return answerTree;
//    }
//
//    private void checkPrep(ParseTree answerTree, ParseTreeNode node) {
//        if (node.relationship.equals("prep")) {
//            if (!node.function.equals("NA")) {
//                if (prepMap.containsKey(node.label.toLowerCase())) {
//                    ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(node.wordOrder);
//                    if (grandchildInAnswerTree != null) {
//                        grandchildInAnswerTree.label = prepMap.get(node.label.toLowerCase());
//                    }
//                }
//            }
//        }
//    }
//
//
//    private void handleLogicalOperators(ParseTree answerTree, ParseTreeNode node, factorization.WordMappings wordReplacementMap) {
//        ParseTreeNode logicalOperatorNode = null;
//        String value = null;
//        for (ParseTreeNode child : node.children) {
//            if (logicalOperators.contains(child.label)) {
//                logicalOperatorNode = child;
//            } else if (wordReplacementMap.contains(0, child.wordOrder)) {
//                value = wordReplacementMap.get(0, child.wordOrder);
//            }
//        }
//
//        if (logicalOperatorNode != null && value != null) {
//            Collection<ParseTreeNode> nodeSiblings = getSiblings(node);
//            Collection<ParseTreeNode> siblingsToRemove = new ArrayList<>();
//            for (ParseTreeNode nodeSibling : nodeSiblings) {
//                for (ParseTreeNode siblingChild : nodeSibling.children) {
//                    if (wordReplacementMap.contains(0, siblingChild.wordOrder) && value.equals(wordReplacementMap.get(0, siblingChild.wordOrder))) {
//                        siblingsToRemove.add(nodeSibling);
//                    }
//                }
//            }
//
//            for (ParseTreeNode siblingToRemove : siblingsToRemove) {
//                ParseTreeNode siblingToRemoveInAnswerTree = answerTree.searchNodeByOrder(siblingToRemove.wordOrder);
//                answerTree.deleteSubTree(siblingToRemoveInAnswerTree);
//            }
//            if (!siblingsToRemove.isEmpty()) {
//                ParseTreeNode logicalOperatorNodeInAnswerTree = answerTree.searchNodeByOrder(logicalOperatorNode.wordOrder);
//                answerTree.deleteSubTree(logicalOperatorNodeInAnswerTree);
//            }
//        }
//
//    }
//
//    private Collection<ParseTreeNode> getSiblings(ParseTreeNode node) {
//        Collection<ParseTreeNode> siblings = new ArrayList<>();
//        for (ParseTreeNode possibleSibling : node.parent.children) {
//            if (!possibleSibling.equals(node)) {
//                siblings.add(possibleSibling);
//            }
//        }
//        return siblings;
//    }
//
//    public ParseTree buildSingleDerivationAnswerParseTree(ParseTree parseTree, factorization.WordMappings wordReplacementMap) {
//        // Initialize
//        ParseTree answerTree = initialize(parseTree);
//        ParseTreeNode objectNode = answerTree.root.children.get(0);
//
//        // Algorithm
//        ParseTreeNode objectNodeInParseTree = parseTree.searchNodeByOrder(objectNode.wordOrder);
//        boolean isNoModifierOrVerbChild = true;
//        for (ParseTreeNode child : objectNodeInParseTree.children) {
//            if (isModifier(child) || isVerb(child)) {
//                isNoModifierOrVerbChild = false;
//            }
//        }
//
//        if (isNoModifierOrVerbChild) {
//            if (wordReplacementMap.contains(0, objectNode.wordOrder)) {
//                String objectValue = wordReplacementMap.get(0, objectNode.wordOrder);
//                answerTree.buildNode((new String[]{String.valueOf(-1), objectValue, "NA", String.valueOf(objectNode.wordOrder), "NA"}));
//                answerTree.buildNode((new String[]{String.valueOf(-1), "is", "NA", String.valueOf(objectNode.wordOrder), "NA"}));
//                answerTree.buildNode((new String[]{String.valueOf(-1), "the", "NA", String.valueOf(objectNode.wordOrder), "NA"}));
//                for (ParseTreeNode child : objectNodeInParseTree.children) {
//                    for (ParseTreeNode grandchild : child.children) {
//                        if (wordReplacementMap.contains(0, grandchild.wordOrder)) {
//                            ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
//                            if (grandchildInAnswerTree != null) {
//                                grandchildInAnswerTree.label = wordReplacementMap.get(0, grandchild.wordOrder);
//                            }
//                        }
//                        // Check Prep
//                        checkPrep(answerTree, child);
//                    }
//                }
//            }
//        } else {
//            if (wordReplacementMap.contains(0, objectNode.wordOrder)) {
//                objectNode.label = wordReplacementMap.get(0, objectNode.wordOrder);
//            }
//
//            for (ParseTreeNode child : objectNodeInParseTree.children) {
//                if (isModifier(child) && isVerb(child)) {
//                    for (ParseTreeNode grandchild : child.children) {
//                        if (!grandchild.relationship.equals("nsubj")) {
//                            if (grandchild.children.isEmpty()) {
//                                if (wordReplacementMap.contains(0, grandchild.wordOrder)) {
//                                    ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
//                                    grandchildInAnswerTree.label = wordReplacementMap.get(0, grandchild.wordOrder);
//                                }
//                            } else {
//                                for (ParseTreeNode greatGrandchild : grandchild.children) {
//                                    if (wordReplacementMap.contains(0, greatGrandchild.wordOrder)) {
//                                        ParseTreeNode greatGrandchildInAnswerTree = answerTree.searchNodeByOrder(greatGrandchild.wordOrder);
//                                        if (greatGrandchildInAnswerTree != null) {
//                                            greatGrandchildInAnswerTree.label = wordReplacementMap.get(0, greatGrandchild.wordOrder);
//                                        }
//                                    }
//                                    // Check Prep
//                                    checkPrep(answerTree, grandchild);
//                                }
//
//                                if (grandchild.children.size() > 1) {
//                                    // Handle Logical Operators
//                                    handleLogicalOperators(answerTree, grandchild, wordReplacementMap);
//                                }
//                            }
//                        } else {
//                            ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
//                            if (grandchildInAnswerTree != null) {
//                                answerTree.deleteSubTree(grandchildInAnswerTree);
//                            }
//                        }
//                    }
//                } else {
////                    ParseTreeNode childInAnswerTree = answerTree.searchNodeByOrder(child.wordOrder);
////                    if (childInAnswerTree != null) {
////                        answerTree.deleteSubTree(childInAnswerTree);
////                    }
//                }
//            }
//        }
//
//        return answerTree;
//    }
//
//    public ParseTree buildMultipleDerivationAnswerParseTree(ParseTree parseTree, factorization.WordMappings wordReplacementMap) {
//        // Initialize
//        ParseTree answerTree = initialize(parseTree);
//        ParseTreeNode objectNode = answerTree.root.children.get(0);
//
//        // Algorithm
//        ParseTreeNode objectNodeInParseTree = parseTree.searchNodeByOrder(objectNode.wordOrder);
//        boolean isNoModifierOrVerbChild = true;
//        for (ParseTreeNode child : objectNodeInParseTree.children) {
//            if (isModifier(child) || isVerb(child)) {
//                isNoModifierOrVerbChild = false;
//            }
//        }
//
//        if (isNoModifierOrVerbChild) {
//            String objectValue = getWordValueMultipleDerivation(wordReplacementMap, objectNode.wordOrder);
//            if (objectValue != null) {
//                answerTree.buildNode((new String[]{String.valueOf(-1), objectValue, "NA", String.valueOf(objectNode.wordOrder), "NA"}));
//                // Todo Nave - in case there are several values it should be are
//                answerTree.buildNode((new String[]{String.valueOf(-1), "is", "NA", String.valueOf(objectNode.wordOrder), "NA"}));
//                answerTree.buildNode((new String[]{String.valueOf(-1), "the", "NA", String.valueOf(objectNode.wordOrder), "NA"}));
//                for (ParseTreeNode child : objectNodeInParseTree.children) {
//                    for (ParseTreeNode grandchild : child.children) {
//                        String grandchildValue = getWordValueMultipleDerivation(wordReplacementMap, grandchild.wordOrder);
//                        if (grandchildValue != null) {
//                            ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
//                            if (grandchildInAnswerTree != null) {
//                                grandchildInAnswerTree.label = grandchildValue;
//                            }
//                        }
//                        // Check Prep
//                        checkPrep(answerTree, child);
//                    }
//                }
//            }
//        } else {
//            String objectValue = getWordValueMultipleDerivation(wordReplacementMap, objectNode.wordOrder);
//            if (objectValue != null) {
//                objectNode.label = objectValue;
//            }
//
//            for (ParseTreeNode child : objectNodeInParseTree.children) {
//                if (isModifier(child) && isVerb(child)) {
//                    for (ParseTreeNode grandchild : child.children) {
//                        if (!grandchild.relationship.equals("nsubj")) {
//                            if (grandchild.children.isEmpty()) {
//                                String grandchildValue = getWordValueMultipleDerivation(wordReplacementMap, grandchild.wordOrder);
//                                if (grandchildValue!= null) {
//                                    ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
//                                    grandchildInAnswerTree.label = grandchildValue;
//                                }
//                            } else {
//                                for (ParseTreeNode greatGrandchild : grandchild.children) {
//                                    String greatGrandchildValue = getWordValueMultipleDerivation(wordReplacementMap, greatGrandchild.wordOrder);
//                                    if (greatGrandchildValue!= null) {
//                                        ParseTreeNode greatGrandchildInAnswerTree = answerTree.searchNodeByOrder(greatGrandchild.wordOrder);
//                                        if (greatGrandchildInAnswerTree != null) {
//                                            greatGrandchildInAnswerTree.label = greatGrandchildValue;
//                                        }
//                                    }
//                                    // Check Prep
//                                    checkPrep(answerTree, grandchild);
//                                }
//
//                                if (grandchild.children.size() > 1) {
//                                    // Handle Logical Operators
//                                    handleLogicalOperators(answerTree, grandchild, wordReplacementMap);
//                                }
//                            }
//                        } else {
//                            ParseTreeNode grandchildInAnswerTree = answerTree.searchNodeByOrder(grandchild.wordOrder);
//                            if (grandchildInAnswerTree != null) {
//                                answerTree.deleteSubTree(grandchildInAnswerTree);
//                            }
//                        }
//                    }
//                } else {
////                    ParseTreeNode childInAnswerTree = answerTree.searchNodeByOrder(child.wordOrder);
////                    if (childInAnswerTree != null) {
////                        answerTree.deleteSubTree(childInAnswerTree);
////                    }
//                }
//            }
//        }
//
//        return answerTree;
//    }
//
//    private String getWordValueMultipleDerivation(factorization.WordMappings wordReplacementMap, int wordOrder) {
//        Set<String> mappings = getSetOfMappings(wordReplacementMap, wordOrder);
//        if (mappings.size() == 1) {
//            return mappings.iterator().next();
//        } else if (mappings.size() > 1) {
//            StringBuilder objectValue = new StringBuilder();
//            for (String mapping : mappings) {
//                String quoatedMapping = null;
//                if (mapping.startsWith("'") && mapping.endsWith("'")) {
//                    quoatedMapping = mapping;
//                } else if (mapping.startsWith("\"") && mapping.endsWith("\"")) {
//                    quoatedMapping = mapping;
//                } else {
//                    quoatedMapping = "\"" + mapping + "\"";
//                }
//                objectValue.append(quoatedMapping).append(", ");
//            }
//            objectValue.deleteCharAt(objectValue.length() - 1);
//            objectValue.deleteCharAt(objectValue.length() - 1);
//            return objectValue.toString();
//        }
//        return null;
//    }
//
//    private Set<String> getSetOfMappings(factorization.WordMappings wordReplacementMap, int wordOrder) {
//        Set<String> mappings = new HashSet<>();
//        for (int i = 0; i < wordReplacementMap.getLastDerivation() + 1; i++) {
//            if (wordReplacementMap.contains(i, wordOrder)) {
//                mappings.add(wordReplacementMap.get(i, wordOrder));
//            }
//        }
//        return mappings;
//    }
//
//
//    private ParseTree copyTree(ParseTree parseTree) {
//        ParseTree copyTree = new ParseTree();
//        for (ParseTreeNode node : parseTree.allNodes) {
//            if (node.parent != null) {
//                copyTree.buildNode(new String[]{String.valueOf(node.wordOrder), node.label, node.pos, String.valueOf(node.parent.wordOrder), node.relationship});
//                copyTree.searchNodeByOrder(node.wordOrder).tokenType = node.tokenType;
//            }
//        }
//        return copyTree;
//    }
//
//    private void deleteReturnPhrase(ParseTree parseTree) {
//        ParseTreeNode parseTreeNode1 = parseTree.searchNodeByOrder(1);
//        ParseTreeNode parseTreeNode2 = parseTree.searchNodeByOrder(2);
//        ParseTreeNode parseTreeNode3 = parseTree.searchNodeByOrder(3);
//        if (parseTreeNode1.label.equalsIgnoreCase("return")) {
//            parseTree.deleteNode(parseTreeNode1);
//            if (parseTreeNode2.label.equalsIgnoreCase("the")) {
//                parseTree.deleteNode(parseTreeNode2);
//            } else if (parseTreeNode2.label.equalsIgnoreCase("me")) {
//                parseTree.deleteNode(parseTreeNode2);
//                if (parseTreeNode3.label.equalsIgnoreCase("the")) {
//                    parseTree.deleteNode(parseTreeNode3);
//                }
//            }
//        }
//    }
//
//    private boolean isModifier(ParseTreeNode node) {
//        return node.relationship.equals("rcmod");
//    }
//
//    private boolean isVerb(ParseTreeNode node) {
//        return node.pos.startsWith("VB");
//    }
//}
