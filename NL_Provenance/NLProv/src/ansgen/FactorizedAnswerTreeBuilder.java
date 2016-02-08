package ansgen;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import factorization.Expression;
import factorization.Variable;
import utils.ParseTreeUtil;
import utils.StringUtil;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: NAVE-FROST
 * Date: 08/02/16
 * Time: 14:08
 */
public class FactorizedAnswerTreeBuilder {
    private ParseTree initialAnswerTree;
    private boolean firstAnd;

    private Collection<ParseTreeNode> nodesCreatedByExpression;

    private Map<Integer, Integer> wordOrderToNodeId;

    public FactorizedAnswerTreeBuilder(ParseTree answerTree) {
        initialAnswerTree = answerTree;
        firstAnd = true;
    }

    public Collection<ParseTreeNode> getNodesCreatedByExpression() {
        return nodesCreatedByExpression;
    }

    public ParseTree handleExpression(Expression expression) {
        ParseTree finalAnswerTree = ParseTreeUtil.copyTree(initialAnswerTree);
        nodesCreatedByExpression = new HashSet<>();

        buildWordOrderToNodeId(finalAnswerTree);

        Collection<ParseTreeNode> processedNodeVariables = new ArrayList<>();
        Set<ParseTreeNode> nodesForDeletion = new HashSet<ParseTreeNode>();
        Collection<ParseTreeNode> variableNodes = getVariableNodes(finalAnswerTree, expression);

        for (Variable variable : expression.getVariables()) {
            ParseTreeNode variableNode = getOriginalNodeByWordOrder(finalAnswerTree, variable.getWordOrder());
            ParseTreeNode variableNodeSubTreeRoot = variableNode;
            if (variableNode.parent.relationship.equals("prep") && !variableNode.parent.label.equals("ROOT")) {
                variableNodeSubTreeRoot = variableNode.parent;
            }

            Collection<ParseTreeNode> siblings = ParseTreeUtil.getSiblings(variableNodeSubTreeRoot);
            for (ParseTreeNode processedNodeVariable : processedNodeVariables) {
                siblings.remove(processedNodeVariable);
            }
            Collection<ParseTreeNode> siblingsWithVariable = new HashSet<>();
            for (ParseTreeNode sibling : siblings) {
                Collection<ParseTreeNode> nodesInSiblingSubTree = ParseTreeUtil.getNodesInSubTree(sibling);
                for (ParseTreeNode node : nodesInSiblingSubTree) {
                    if (variableNodes.contains(node)) {
                        siblingsWithVariable.add(sibling);
                    }
                }
            }


            if (!siblingsWithVariable.isEmpty()) {
                int firstWordOrderInSiblingsSubTrees = ParseTreeUtil.getFirstWordOrderInSubTrees(siblingsWithVariable);
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
            ParseTreeNode node = getOriginalNodeByWordOrder(finalAnswerTree, wordOrderToValues.getKey());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < wordOrderToValues.getValue().size(); i++) {
                if (i != 0) {
                    if (i == wordOrderToValues.getValue().size() - 1) {
                        sb.append(" and ");
                    } else {
                        sb.append(", ");
                    }
                }
                sb.append(StringUtil.getQuoatedString(wordOrderToValues.getValue().get(i)));
            }

            node.label = sb.toString();

            nodesCreatedByExpression.add(node);
        }

        for (Expression multiVariableUnNestedSubExpression : multiVariableUnNestedSubExpressions) {
            Collection<ParseTreeNode> nodesInExpression = new ArrayList<ParseTreeNode>();
            Map<Integer, String> nodeValues = new HashMap<Integer, String>();
            for (Variable variable : multiVariableUnNestedSubExpression.getVariables()) {
                ParseTreeNode node = getOriginalNodeByWordOrder(finalAnswerTree, variable.getWordOrder());
                nodeValues.put(node.nodeID, variable.getValue());
                nodesInExpression.add(node);
            }
            ParseTreeNode jointParent = ParseTreeUtil.getJointParent(nodesInExpression);
            int lastWordOrderInParentSubTree = ParseTreeUtil.getLastWordOrderInSubTree(jointParent);

            Collection<ParseTreeNode> nodesInExpressionSubTreeRoots = new HashSet<ParseTreeNode>();
            for (ParseTreeNode potentialSubTreeRoots : jointParent.children) {
                Collection<ParseTreeNode> nodesInSubTree = ParseTreeUtil.getNodesInSubTree(potentialSubTreeRoots);
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
            FactorizedAnswerTreeBuilder factorizedAnswerTreeBuilder = new FactorizedAnswerTreeBuilder(initialAnswerTree);
            factorizedAnswerTreeBuilder.handleExpression(nestedSubExpression);
            Collection<ParseTreeNode> nodesFromSubExpressionTreeInExpression = factorizedAnswerTreeBuilder.getNodesCreatedByExpression();

            Collection<ParseTreeNode> nodesFromFinalTreeInExpression = getNodesInExpression(finalAnswerTree, nestedSubExpression);
            if (!nodesFromSubExpressionTreeInExpression.isEmpty()) {
                ParseTreeNode jointParentFromFinalTree = ParseTreeUtil.getJointParent(nodesFromFinalTreeInExpression);
                int lastWordOrderInParentSubTree = ParseTreeUtil.getLastWordOrderInSubTree(jointParentFromFinalTree);

                ParseTreeNode jointParentFromSubExpressionTree = ParseTreeUtil.getJointParent(nodesFromSubExpressionTreeInExpression);
                Collection<ParseTreeNode> nodesInExpressionSubTreeRoots = new HashSet<>();
                for (ParseTreeNode potentialSubTreeRoots : jointParentFromSubExpressionTree.children) {
                    Collection<ParseTreeNode> nodesInSubTree = ParseTreeUtil.getNodesInSubTree(potentialSubTreeRoots);
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
        nodesCreatedByExpression = nodesCreatedAndNotDeletedByExpression;

        return finalAnswerTree;
    }

    private void buildWordOrderToNodeId(ParseTree parseTree) {
        wordOrderToNodeId = new HashMap<>();
        for (ParseTreeNode node : parseTree.allNodes) {
            wordOrderToNodeId.put(node.wordOrder, node.nodeID);
        }
    }

    private Collection<ParseTreeNode> getNodesInExpression(ParseTree parseTree, Expression expression) {
        Collection<ParseTreeNode> nodesInExpression = new HashSet<ParseTreeNode>();
        getNodesInExpression(parseTree, expression, nodesInExpression);
        return nodesInExpression;
    }

    private void getNodesInExpression(ParseTree parseTree, Expression expression, Collection<ParseTreeNode> nodesInExpression) {
        for (Variable variable : expression.getVariables()) {
            ParseTreeNode node = getOriginalNodeByWordOrder(parseTree, variable.getWordOrder());
            nodesInExpression.add(node);
        }
        for (Expression subExpression : expression.getExpressions()) {
            getNodesInExpression(parseTree, subExpression, nodesInExpression);
        }
    }


    private Collection<ParseTreeNode> attachCopyOfSubTreeBefore(ParseTree parseTree, ParseTreeNode parent, ParseTreeNode subTreeRoot, Map<Integer, String> nodeValues, int wordOrder) {
        String nodeValue = nodeValues.get(subTreeRoot.wordOrder);
        if (nodeValue == null) {
            nodeValue = subTreeRoot.label;
        } else {
            nodeValue = StringUtil.getQuoatedString(nodeValue);
        }

        Collection<ParseTreeNode> nodesInSubTree = ParseTreeUtil.getNodesInSubTree(subTreeRoot);
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
            nodeValue = StringUtil.getQuoatedString(nodeValue);
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

    private ParseTreeNode getOriginalNodeByWordOrder(ParseTree parseTree, int wordOrder) {
        Integer nodeId = wordOrderToNodeId.get(wordOrder);
        return parseTree.searchNodeByID(nodeId);
    }

    private Collection<ParseTreeNode> getVariableNodes(ParseTree parseTree, Expression expression) {
        Collection<ParseTreeNode> variableNodes = new HashSet<>();

        for (Variable variable : expression.getVariables()) {
            ParseTreeNode variableNode = getOriginalNodeByWordOrder(parseTree, variable.getWordOrder());
            variableNodes.add(variableNode);
        }

        for (Expression subExpression : expression.getExpressions()) {
            Collection<ParseTreeNode> subExpressionVariableNodes = getVariableNodes(parseTree, subExpression);
            variableNodes.addAll(subExpressionVariableNodes);
        }
        return variableNodes;
    }

}
