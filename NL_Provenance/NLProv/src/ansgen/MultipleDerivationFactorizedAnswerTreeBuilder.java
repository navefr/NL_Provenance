package ansgen;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import factorization.Expression;
import factorization.SimpleFactorizer;
import factorization.Variable;
import factorization.WordMappings;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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
        Expression factorizeExpression = new Expression(wordReplacementMap);
        SimpleFactorizer.getInstance().factorize(factorizeExpression);

        ParseTree answerParseTree = super.buildParseTree(parseTree, wordReplacementMap);

        ParseTree finalAnswerTree = handleExpression(answerParseTree, factorizeExpression, true).getLeft();

        return finalAnswerTree;
    }

    private Pair<ParseTree, Collection<ParseTreeNode>> handleExpression(ParseTree initialAnswerTree, Expression expression, boolean firstAnd) {
        ParseTree finalAnswerTree = copyTree(initialAnswerTree);

        Map<Integer, Integer> wordOrderToNodeId = new HashMap<>();
        for (ParseTreeNode node : finalAnswerTree.allNodes) {
            wordOrderToNodeId.put(node.wordOrder, node.nodeID);
        }

        Collection<ParseTreeNode> processedNodeVariables = new ArrayList<>();
        Set<ParseTreeNode> nodesForDeletion = new HashSet<ParseTreeNode>();

        Collection<ParseTreeNode> nodesCreatedByExpression = new HashSet<>();
        Collection<ParseTreeNode> variableNodes = getVariableNodes(finalAnswerTree, expression, wordOrderToNodeId);

        for (Variable variable : expression.getVariables()) {
            ParseTreeNode variableNode = getOriginalNodeByWordOrder(finalAnswerTree, variable.getWordOrder(), wordOrderToNodeId);
            ParseTreeNode variableNodeSubTreeRoot = variableNode;
            if (variableNode.parent.relationship.equals("prep") && !variableNode.parent.label.equals("ROOT")) {
                variableNodeSubTreeRoot = variableNode.parent;
            }

            Collection<ParseTreeNode> siblings = getSiblings(variableNodeSubTreeRoot);
            for (ParseTreeNode processedNodeVariable : processedNodeVariables) {
                siblings.remove(processedNodeVariable);
            }
            Collection<ParseTreeNode> siblingsWithVariable = new HashSet<>();
            for (ParseTreeNode sibling : siblings) {
                Collection<ParseTreeNode> nodesInSiblingSubTree = new HashSet<>();
                getNodesInSubTree(nodesInSiblingSubTree, sibling);
                for (ParseTreeNode node : nodesInSiblingSubTree) {
                    if (variableNodes.contains(node)) {
                        siblingsWithVariable.add(sibling);
                    }
                }
            }


            if (!siblingsWithVariable.isEmpty()) {
                int firstWordOrderInSiblingsSubTrees = getFirstWordOrderInSubTrees(siblingsWithVariable);
                Collection<ParseTreeNode> attachedNodes = attachCopyOfSubTreeBefore(finalAnswerTree, variableNodeSubTreeRoot.parent, variableNodeSubTreeRoot, Collections.singletonMap(variableNode.nodeID, variable.getValue()), firstWordOrderInSiblingsSubTrees);

                nodesForDeletion.add(variableNodeSubTreeRoot);
                nodesCreatedByExpression.addAll(attachedNodes);
            } else {
                variableNode.label = variable.getValue();
                nodesCreatedByExpression.add(variableNode);
            }

            processedNodeVariables.add(variableNodeSubTreeRoot);
        }

        Collection<Expression> nestedSubExpressions = new ArrayList<>();
        Collection<Expression> unNestedSubExpressions = new ArrayList<>();
        for (Expression subExpression : expression.getExpressions()) {
            if (subExpression.getExpressions().isEmpty()) {
                unNestedSubExpressions.add(subExpression);
            } else {
                nestedSubExpressions.add(subExpression);
            }
        }

        Collection<Expression> singleVariableUnNestedSubExpressions = new ArrayList<Expression>();
        Collection<Expression> multiVariableUnNestedSubExpressions = new ArrayList<Expression>();
        for (Expression unNestedSubExpression : unNestedSubExpressions) {
            if (unNestedSubExpression.getVariables().size() == 1) {
                singleVariableUnNestedSubExpressions.add(unNestedSubExpression);
            } else {
                multiVariableUnNestedSubExpressions.add(unNestedSubExpression);
            }
        }

        Map<Integer, List<String>> singleVariableWordOrderToValues = new HashMap<Integer, List<String>>();
        for (Expression singleVariableUnNestedSubExpression : singleVariableUnNestedSubExpressions) {
            Variable variable = singleVariableUnNestedSubExpression.getVariables().iterator().next();
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

            nodesCreatedByExpression.add(node);
        }

        for (Expression multiVariableUnNestedSubExpression : multiVariableUnNestedSubExpressions) {
            Collection<ParseTreeNode> nodesInExpression = new ArrayList<ParseTreeNode>();
            Map<Integer, String> nodeValues = new HashMap<Integer, String>();
            for (Variable variable : multiVariableUnNestedSubExpression.getVariables()) {
                ParseTreeNode node = getOriginalNodeByWordOrder(finalAnswerTree, variable.getWordOrder(), wordOrderToNodeId);
                nodeValues.put(node.nodeID, variable.getValue());
                nodesInExpression.add(node);
            }
            ParseTreeNode jointParent = getJointParent(nodesInExpression);
            int lastWordOrderInParentSubTree = getLastWordOrderInSubTree(jointParent);

            Collection<ParseTreeNode> nodesInExpressionSubTreeRoots = new HashSet<ParseTreeNode>();
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
                nodesCreatedByExpression.add(parentNode);
            }
            for (ParseTreeNode node : nodesInExpressionSubTreeRoots) {
                Collection<ParseTreeNode> attachedNodes = attachCopyOfSubTree(finalAnswerTree, parentNode, node, nodeValues, lastWordOrderInParentSubTree + 2);
                nodesCreatedByExpression.addAll(attachedNodes);
            }
        }

        for (Expression nestedSubExpression : nestedSubExpressions) {
            // todo nave - theres alot of code reuse - try to unify
            Pair<ParseTree, Collection<ParseTreeNode>> subExpressionResult = handleExpression(initialAnswerTree, nestedSubExpression, firstAnd);

            Collection<ParseTreeNode> nodesFromFinalTreeInExpression = getNodesInExpression(finalAnswerTree, wordOrderToNodeId, nestedSubExpression);
            Collection<ParseTreeNode> nodesFromSubExpressionTreeInExpression = subExpressionResult.getRight();
            if (!nodesFromSubExpressionTreeInExpression.isEmpty()) {
                ParseTreeNode jointParentFromFinalTree = getJointParent(nodesFromFinalTreeInExpression);
                int lastWordOrderInParentSubTree = getLastWordOrderInSubTree(jointParentFromFinalTree);

                ParseTreeNode jointParentFromSubExpressionTree = getJointParent(nodesFromSubExpressionTreeInExpression);
                Collection<ParseTreeNode> nodesInExpressionSubTreeRoots = new HashSet<>();
                for (ParseTreeNode potentialSubTreeRoots : jointParentFromSubExpressionTree.children) {
                    Collection<ParseTreeNode> nodesInSubTree = new HashSet<ParseTreeNode>();
                    getNodesInSubTree(nodesInSubTree, potentialSubTreeRoots);
                    for (ParseTreeNode node : nodesFromSubExpressionTreeInExpression) {
                        if (nodesInSubTree.contains(node)) {
                            nodesInExpressionSubTreeRoots.add(potentialSubTreeRoots);
                        }
                    }
                }
                nodesForDeletion.addAll(nodesFromFinalTreeInExpression);

                ParseTreeNode parentNode;
                if (firstAnd) {
                    parentNode = jointParentFromFinalTree;
                    firstAnd = false;
                } else {
                    parentNode = finalAnswerTree.buildNode((new String[]{String.valueOf(lastWordOrderInParentSubTree + 1), "and\n", "NA", String.valueOf(jointParentFromFinalTree.wordOrder), "NA"}));
                    nodesCreatedByExpression.add(parentNode);
                }
                for (ParseTreeNode node : nodesInExpressionSubTreeRoots) {
                    Collection<ParseTreeNode> attachedNodes = attachCopyOfSubTree(finalAnswerTree, parentNode, node, Collections.EMPTY_MAP, lastWordOrderInParentSubTree + 2);
                    nodesCreatedByExpression.addAll(attachedNodes);
                }
            }
        }

        for (ParseTreeNode node : nodesForDeletion) {
            if (finalAnswerTree.searchNodeByID(node.nodeID) != null) {
                finalAnswerTree.deleteSubTree(node);
            }
        }

        // TODO Nave - why whould we delete a node we created?
        Collection<ParseTreeNode> nodesCreatedAndNotDeletedByExpression = new HashSet<ParseTreeNode>();
        for (ParseTreeNode node : nodesCreatedByExpression) {
            if (node.parent != null) {
                nodesCreatedAndNotDeletedByExpression.add(node);
            }
        }

        return new ImmutablePair<ParseTree, Collection<ParseTreeNode>>(finalAnswerTree, nodesCreatedAndNotDeletedByExpression);
    }

    private Collection<ParseTreeNode> getNodesInExpression(ParseTree parseTree, Map<Integer, Integer> wordOrderToNodeId, Expression expression) {
        Collection<ParseTreeNode> nodesInExpression = new HashSet<ParseTreeNode>();
        getNodesInExpression(parseTree, wordOrderToNodeId, expression, nodesInExpression);
        return nodesInExpression;
    }

    private void getNodesInExpression(ParseTree parseTree, Map<Integer, Integer> wordOrderToNodeId, Expression expression, Collection<ParseTreeNode> nodesInExpression) {
        for (Variable variable : expression.getVariables()) {
            ParseTreeNode node = getOriginalNodeByWordOrder(parseTree, variable.getWordOrder(), wordOrderToNodeId);
            nodesInExpression.add(node);
        }
        for (Expression subExpression : expression.getExpressions()) {
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
        if (nodes.contains(potentialParent)) {
            return false;
        }

        Collection<ParseTreeNode> nodesInSubTree = new HashSet<>();
        getNodesInSubTree(nodesInSubTree, potentialParent);
        for (ParseTreeNode node : nodes) {
            if (!nodesInSubTree.contains(node)) {
                return false;
            }
        }
        return true;
    }

    private Collection<ParseTreeNode> attachCopyOfSubTreeBefore(ParseTree parseTree, ParseTreeNode parent, ParseTreeNode subTreeRoot, Map<Integer, String> nodeValues, int wordOrder) {
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

        ParseTreeNode newNode = parseTree.buildNodeByParentId((new String[]{String.valueOf(lastWordOrder + subTreeRoot.wordOrder), nodeValue, "NA", String.valueOf(parent.nodeID), "NA"}));

        Collection<ParseTreeNode> attachedNodes = new HashSet<>();
        attachedNodes.add(newNode);
        for (ParseTreeNode child : subTreeRoot.children) {
            Collection<ParseTreeNode> attachedNodesRec = attachCopyOfSubTree(parseTree, newNode, child, nodeValues, lastWordOrder);
            attachedNodes.addAll(attachedNodesRec);
        }
        return attachedNodes;
    }

    private void shiftWordOrders(ParseTree parseTree, Collection<ParseTreeNode> exclude, int from, int amount) {
        for (ParseTreeNode node : parseTree.allNodes) {
            if (node.wordOrder >= from && !exclude.contains(node)) {
                node.wordOrder = node.wordOrder + amount;
            }
        }
    }

    private Collection<ParseTreeNode> attachCopyOfSubTree(ParseTree parseTree, ParseTreeNode parent, ParseTreeNode subTreeRoot, Map<Integer, String> nodeValues, int lastWordOrder) {
        String nodeValue = nodeValues.get(subTreeRoot.nodeID);
        if (nodeValue == null) {
            nodeValue = subTreeRoot.label;
        } else {
            nodeValue = getQuoatedString(nodeValue);
        }
        ParseTreeNode newNode = parseTree.buildNodeByParentId((new String[]{String.valueOf(lastWordOrder + subTreeRoot.wordOrder), nodeValue, "NA", String.valueOf(parent.nodeID), "NA"}));

        Collection<ParseTreeNode> attachedNodes = new HashSet<>();
        attachedNodes.add(newNode);

        for (ParseTreeNode child : subTreeRoot.children) {
            Collection<ParseTreeNode> attachedNodesRec = attachCopyOfSubTree(parseTree, newNode, child, nodeValues, lastWordOrder);
            attachedNodes.addAll(attachedNodesRec);
        }

        return attachedNodes;
    }

    private ParseTreeNode getOriginalNodeByWordOrder(ParseTree parseTree, int wordOrder, Map<Integer, Integer> wordOrderToNodeId) {
        Integer nodeId = wordOrderToNodeId.get(wordOrder);
        return parseTree.searchNodeByID(nodeId);
    }

    private Collection<ParseTreeNode> getVariableNodes(ParseTree parseTree, Expression expression, Map<Integer, Integer> wordOrderToNodeId) {
        Collection<ParseTreeNode> variableNodes = new HashSet<>();

        for (Variable variable : expression.getVariables()) {
            ParseTreeNode variableNode = getOriginalNodeByWordOrder(parseTree, variable.getWordOrder(), wordOrderToNodeId);
            variableNodes.add(variableNode);
        }

        for (Expression subExpression : expression.getExpressions()) {
            Collection<ParseTreeNode> subExpressionVariableNodes = getVariableNodes(parseTree, subExpression, wordOrderToNodeId);
            variableNodes.addAll(subExpressionVariableNodes);
        }
        return variableNodes;
    }
}
