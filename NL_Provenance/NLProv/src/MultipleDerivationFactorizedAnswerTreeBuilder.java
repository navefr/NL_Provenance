import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;

import java.util.*;

/**
 * Created by nfrost on 1/25/2016
 */
public class MultipleDerivationFactorizedAnswerTreeBuilder extends AbstractAnswerParseTreeBuilder {


    private static MultipleDerivationFactorizedAnswerTreeBuilder instance = null;


    public static MultipleDerivationFactorizedAnswerTreeBuilder getInstance(){
        if (instance == null) {
            instance = new MultipleDerivationFactorizedAnswerTreeBuilder();
        }
        return instance;
    }

    @Override
    public ParseTree buildParseTree(ParseTree parseTree, WordMappings wordReplacementMap) {
        WordMappings.Expression factorizeExpression = wordReplacementMap.createFactorizeExpression();
        ParseTree answerParseTree = super.buildParseTree(parseTree, wordReplacementMap);

        ParseTree finalAnswerTree = copyTree(answerParseTree);

        Map<Integer, Integer> wordOrderToNodeId = new HashMap<>();
        for (ParseTreeNode node : finalAnswerTree.allNodes) {
            wordOrderToNodeId.put(node.wordOrder, node.nodeID);
        }

        Collection<ParseTreeNode> processedNodeVariables = new ArrayList<>();
        Set<ParseTreeNode> nodesForDeletion = new HashSet<>();
        handleExpression(finalAnswerTree, answerParseTree, factorizeExpression, processedNodeVariables, nodesForDeletion, wordOrderToNodeId, true);

        for (ParseTreeNode node : nodesForDeletion) {
            if (finalAnswerTree.searchNodeByID(node.nodeID) != null) {
                finalAnswerTree.deleteSubTree(node);
            }
        }

        return finalAnswerTree;
    }

    private void handleExpression(ParseTree finalAnswerTree, ParseTree initialAnswerTree, WordMappings.Expression expression, Collection<ParseTreeNode> processedNodeVariables, Set<ParseTreeNode> nodesForDeletion, Map<Integer, Integer> wordOrderToNodeId, boolean firstAnd) {
        for (WordMappings.Variable variable : expression.getVariables()) {
            ParseTreeNode variableNode = getOriginalNodeByWordOrder(finalAnswerTree, variable.getWordOrder(), wordOrderToNodeId);
            ParseTreeNode variableNodeSubTreeRoot = variableNode;
            if (variableNode.parent.relationship.equals("prep") && !variableNode.parent.label.equals("ROOT")) {
                variableNodeSubTreeRoot = variableNode.parent;
            }

            Collection<ParseTreeNode> siblings = getSiblings(variableNodeSubTreeRoot);
            for (ParseTreeNode processedNodeVariable : processedNodeVariables) {
                siblings.remove(processedNodeVariable);
            }

            if (!siblings.isEmpty()) {
                int firstWordOrderInSiblingsSubTrees = getFirstWordOrderInSubTrees(siblings);
                attachCopyOfSubTreeBefore(finalAnswerTree, variableNodeSubTreeRoot.parent, variableNodeSubTreeRoot, Collections.emptyMap(), firstWordOrderInSiblingsSubTrees);
                nodesForDeletion.add(variableNodeSubTreeRoot);
            }
            variableNode.label = variable.getValue();
            processedNodeVariables.add(variableNodeSubTreeRoot);
        }

        Collection<WordMappings.Expression> nestedSubExpressions = new ArrayList<>();
        Collection<WordMappings.Expression> unNestedSubExpressions = new ArrayList<>();
        for (WordMappings.Expression subExpression : expression.getExpressions()) {
            if (subExpression.getExpressions().isEmpty()) {
                unNestedSubExpressions.add(subExpression);
            } else {
                nestedSubExpressions.add(subExpression);
            }
        }

        Collection<WordMappings.Expression> singleVariableUnNestedSubExpressions = new ArrayList<WordMappings.Expression>();
        Collection<WordMappings.Expression> multiVariableUnNestedSubExpressions = new ArrayList<WordMappings.Expression>();
        for (WordMappings.Expression unNestedSubExpression : unNestedSubExpressions) {
            if (unNestedSubExpression.getVariables().size() == 1) {
                singleVariableUnNestedSubExpressions.add(unNestedSubExpression);
            } else {
                multiVariableUnNestedSubExpressions.add(unNestedSubExpression);
            }
        }

        Map<Integer, List<String>> singleVariableWordOrderToValues = new HashMap<Integer, List<String>>();
        for (WordMappings.Expression singleVariableUnNestedSubExpression : singleVariableUnNestedSubExpressions) {
            WordMappings.Variable variable = singleVariableUnNestedSubExpression.getVariables().iterator().next();
            List<String> wordOrderValues = singleVariableWordOrderToValues.get(variable.getWordOrder());
            if (wordOrderValues == null) {
                wordOrderValues = new ArrayList<String>();
                singleVariableWordOrderToValues.put(variable.getWordOrder(), wordOrderValues);
            }
            wordOrderValues.add(variable.getValue());
        }
        for (Map.Entry<Integer, List<String>> wordOrderToValues : singleVariableWordOrderToValues.entrySet()) {
            ParseTreeNode node = getOriginalNodeByWordOrder(finalAnswerTree, wordOrderToValues.getKey(), wordOrderToNodeId);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < wordOrderToValues.getValue().size(); i++) {
                if (i != 0) {
                    if (i == wordOrderToValues.getValue().size() - 1) {
                        sb.append(" and ");
                    } else {
                        sb.append(", ");
                    }
                }
                sb.append(getQuoatedString(wordOrderToValues.getValue().get(i)));
            }

            node.label = sb.toString();
        }

        for (WordMappings.Expression multiVariableUnNestedSubExpression : multiVariableUnNestedSubExpressions) {
            Collection<ParseTreeNode> nodesInExpression = new ArrayList<>();
            Map<Integer, String> nodeValues = new HashMap<>();
            for (WordMappings.Variable variable : multiVariableUnNestedSubExpression.getVariables()) {
                ParseTreeNode node = getOriginalNodeByWordOrder(finalAnswerTree, variable.getWordOrder(), wordOrderToNodeId);
                nodeValues.put(node.nodeID, variable.getValue());
                nodesInExpression.add(node);
            }
            ParseTreeNode jointParent = getJointParent(nodesInExpression);
            int lastWordOrderInParentSubTree = getLastWordOrderInSubTree(jointParent);

            Collection<ParseTreeNode> nodesInExpressionSubTreeRoots = new HashSet<>();
            for (ParseTreeNode potentialSubTreeRoots : jointParent.children) {
                Collection<ParseTreeNode> nodesInSubTree = new HashSet<>();
                getNodesInSubTree(nodesInSubTree, potentialSubTreeRoots);
                for (ParseTreeNode node : nodesInExpression) {
                    if (nodesInSubTree.contains(node)) {
                        nodesInExpressionSubTreeRoots.add(potentialSubTreeRoots);
                        nodesForDeletion.add(potentialSubTreeRoots);
                    }
                }
            }

            ParseTreeNode parentNode;
            if (firstAnd) {
                parentNode = jointParent;
                firstAnd = false;
            } else {
                parentNode = finalAnswerTree.buildNode((new String[]{String.valueOf(lastWordOrderInParentSubTree + 1), "and\n", "NA", String.valueOf(jointParent.wordOrder), "NA"}));
            }
            for (ParseTreeNode node : nodesInExpressionSubTreeRoots) {
                attachCopyOfSubTree(finalAnswerTree, parentNode, node, nodeValues, lastWordOrderInParentSubTree + 2);
            }
        }

        for (WordMappings.Expression nestedSubExpression : nestedSubExpressions) {
            // todo nave - theres alot of code reuse - try to unify
            ParseTree subExpressionAnswerParseTree = copyTree(initialAnswerTree);
            Map<Integer, Integer> subExpressionWordOrderToNodeId = new HashMap<>();
            for (ParseTreeNode node : subExpressionAnswerParseTree.allNodes) {
                subExpressionWordOrderToNodeId.put(node.wordOrder, node.nodeID);
            }

            handleExpression(subExpressionAnswerParseTree, initialAnswerTree, nestedSubExpression, processedNodeVariables, nodesForDeletion, subExpressionWordOrderToNodeId, firstAnd);

            Collection<ParseTreeNode> nodesFromFinalTreeInExpression = getNodesInExpression(finalAnswerTree, wordOrderToNodeId, nestedSubExpression);
            Collection<ParseTreeNode> nodesFromSubExpressionTreeInExpression = getNodesInExpression(subExpressionAnswerParseTree, subExpressionWordOrderToNodeId, nestedSubExpression);

            ParseTreeNode jointParentFromFinalTree = getJointParent(nodesFromFinalTreeInExpression);
            int lastWordOrderInParentSubTree = getLastWordOrderInSubTree(jointParentFromFinalTree);

            ParseTreeNode jointParentFromSubExpressionTree = getJointParent(nodesFromSubExpressionTreeInExpression);
            Collection<ParseTreeNode> nodesInExpressionSubTreeRoots = new HashSet<>();
            for (ParseTreeNode potentialSubTreeRoots : jointParentFromSubExpressionTree.children) {
                Collection<ParseTreeNode> nodesInSubTree = new HashSet<>();
                getNodesInSubTree(nodesInSubTree, potentialSubTreeRoots);
                for (ParseTreeNode node : nodesFromSubExpressionTreeInExpression) {
                    if (nodesInSubTree.contains(node)) {
                        nodesInExpressionSubTreeRoots.add(potentialSubTreeRoots);
                        nodesForDeletion.add(potentialSubTreeRoots); // TODO Nave - delete??
                    }
                }
            }

            ParseTreeNode parentNode;
            if (firstAnd) {
                parentNode = jointParentFromFinalTree;
                firstAnd = false;
            } else {
                parentNode = finalAnswerTree.buildNode((new String[]{String.valueOf(lastWordOrderInParentSubTree + 1), "and\n", "NA", String.valueOf(jointParentFromFinalTree.wordOrder), "NA"}));
            }
            for (ParseTreeNode node : nodesInExpressionSubTreeRoots) {
                attachCopyOfSubTree(finalAnswerTree, parentNode, node, Collections.EMPTY_MAP, lastWordOrderInParentSubTree + 2);
            }
        }

    }

    private Collection<ParseTreeNode> getNodesInExpression(ParseTree parseTree, Map<Integer, Integer> wordOrderToNodeId, WordMappings.Expression expression) {
        Collection<ParseTreeNode> nodesInExpression = new HashSet<>();
        getNodesInExpression(parseTree, wordOrderToNodeId, expression, nodesInExpression);
        return nodesInExpression;
    }

    private void getNodesInExpression(ParseTree parseTree, Map<Integer, Integer> wordOrderToNodeId, WordMappings.Expression expression, Collection<ParseTreeNode> nodesInExpression) {
        for (WordMappings.Variable variable : expression.getVariables()) {
            ParseTreeNode node = getOriginalNodeByWordOrder(parseTree, variable.getWordOrder(), wordOrderToNodeId);
            nodesInExpression.add(node);
        }
        for (WordMappings.Expression subExpression : expression.getExpressions()) {
            getNodesInExpression(parseTree, wordOrderToNodeId, subExpression, nodesInExpression);
        }
    }

    @Override
    protected String getNodeValue(WordMappings wordReplacementMap, ParseTreeNode node) {
        if (wordReplacementMap.contains(0, node.wordOrder)) {
            return wordReplacementMap.get(0, node.wordOrder);
        } else {
            return null;
        }
    }

    private int getFirstWordOrderInSubTrees(Collection<ParseTreeNode> nodes) {
        int firstWordOrder = Integer.MAX_VALUE;
        for (ParseTreeNode currNode : nodes) {
            firstWordOrder = Math.min(firstWordOrder, getFirstWordOrderInSubTree(currNode));
        }
        return firstWordOrder;
    }

    private int getFirstWordOrderInSubTree(ParseTreeNode node) {
        int firstWordOrder = Integer.MAX_VALUE;
        Collection<ParseTreeNode> nodesInSubTree = new HashSet<>();
        getNodesInSubTree(nodesInSubTree, node);
        for (ParseTreeNode currNode : nodesInSubTree) {
            firstWordOrder = Math.min(firstWordOrder, currNode.wordOrder);
        }
        return firstWordOrder;
    }

    private int getLastWordOrderInSubTree(ParseTreeNode node) {
        int lastWordOrder = Integer.MIN_VALUE;
        Collection<ParseTreeNode> nodesInSubTree = new HashSet<>();
        getNodesInSubTree(nodesInSubTree, node);
        for (ParseTreeNode currNode : nodesInSubTree) {
            lastWordOrder = Math.max(lastWordOrder, currNode.wordOrder);
        }
        return lastWordOrder;
    }

    private void getNodesInSubTree(Collection<ParseTreeNode> nodesInSubTree, ParseTreeNode node) {
        nodesInSubTree.add(node);
        for (ParseTreeNode child : node.children) {
            getNodesInSubTree(nodesInSubTree, child);
        }
    }

    private ParseTreeNode getJointParent(Collection<ParseTreeNode> nodes) {
        ParseTreeNode aNode = nodes.iterator().next();
        while (!(aNode.label.equals("ROOT") || isJointParent(aNode, nodes))) {
            aNode = aNode.parent;
        }
        return aNode;
    }

    private boolean isJointParent(ParseTreeNode potentialParent, Collection<ParseTreeNode> nodes) {
        Collection<ParseTreeNode> nodesInSubTree = new HashSet<>();
        getNodesInSubTree(nodesInSubTree, potentialParent);
        for (ParseTreeNode node : nodes) {
            if (!nodesInSubTree.contains(node)) {
                return false;
            }
        }
        return true;
    }

    private void attachCopyOfSubTreeBefore(ParseTree parseTree, ParseTreeNode parent, ParseTreeNode subTreeRoot, Map<Integer, String> nodeValues, int wordOrder) {
        String nodeValue = nodeValues.get(subTreeRoot.wordOrder);
        if (nodeValue == null) {
            nodeValue = subTreeRoot.label;
        } else {
            nodeValue = getQuoatedString(nodeValue);
        }

        HashSet<ParseTreeNode> nodesInSubTree = new HashSet<>();
        getNodesInSubTree(nodesInSubTree, subTreeRoot);
        int minWordOrder = Integer.MAX_VALUE;
        for (ParseTreeNode node : nodesInSubTree) {
            minWordOrder = Math.min(minWordOrder, node.wordOrder);
        }
        int lastWordOrder = wordOrder - minWordOrder;

        shiftWordOrders(parseTree, nodesInSubTree, wordOrder, nodesInSubTree.size());

        ParseTreeNode newNode = parseTree.buildNode((new String[]{String.valueOf(lastWordOrder + subTreeRoot.wordOrder), nodeValue, "NA", String.valueOf(parent.wordOrder), "NA"}));

        for (ParseTreeNode child : subTreeRoot.children) {
            attachCopyOfSubTree(parseTree, newNode, child, nodeValues, lastWordOrder);
        }
    }

    private void shiftWordOrders(ParseTree parseTree, Collection<ParseTreeNode> exclude, int from, int amount) {
        for (ParseTreeNode node : parseTree.allNodes) {
            if (node.wordOrder >= from && !exclude.contains(node)) {
                node.wordOrder = node.wordOrder + amount;
            }
        }
    }

    private void attachCopyOfSubTree(ParseTree parseTree, ParseTreeNode parent, ParseTreeNode subTreeRoot, Map<Integer, String> nodeValues, int lastWordOrder) {
        String nodeValue = nodeValues.get(subTreeRoot.nodeID);
        if (nodeValue == null) {
            nodeValue = subTreeRoot.label;
        } else {
            nodeValue = getQuoatedString(nodeValue);
        }
        ParseTreeNode newNode = parseTree.buildNode((new String[]{String.valueOf(lastWordOrder + subTreeRoot.wordOrder), nodeValue, "NA", String.valueOf(parent.wordOrder), "NA"}));

        for (ParseTreeNode child : subTreeRoot.children) {
            attachCopyOfSubTree(parseTree, newNode, child, nodeValues, lastWordOrder);
        }

    }

    private ParseTreeNode getOriginalNodeByWordOrder(ParseTree parseTree, int wordOrder, Map<Integer, Integer> wordOrderToNodeId) {
        Integer nodeId = wordOrderToNodeId.get(wordOrder);
        return parseTree.searchNodeByID(nodeId);
    }

}
