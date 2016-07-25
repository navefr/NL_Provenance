package ansgen;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import factorization.Expression;
import factorization.Variable;
import org.apache.commons.lang3.tuple.Pair;
import utils.ParseTreeUtil;
import utils.StringUtil;
import utils.SubTreeTracker;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: NAVE-FROST
 * Date: 08/02/16
 * Time: 14:08
 */
public class FactorizedAnswerTreeBuilder {
    private ParseTree initialAnswerTree;
    private Map<ParseTreeNode, Collection<ParseTreeNode>> queryToAnswerNodeMappings;
    private boolean firstAnd;

    private Collection<ParseTreeNode> nodesCreatedByExpression;

    private Map<Integer, Integer> wordOrderToNodeId;
    private Map<Integer, ParseTreeNode> nodeIdToOriginalNode;

    private Map<ParseTreeNode, Collection<ParseTreeNode>> nodeMapping;

    public FactorizedAnswerTreeBuilder(ParseTree answerTree, Map<ParseTreeNode, Collection<ParseTreeNode>> queryToAnswerNodeMappings) {
        this.initialAnswerTree = answerTree;
        this.queryToAnswerNodeMappings = queryToAnswerNodeMappings;
        this.firstAnd = true;
    }

    public Collection<ParseTreeNode> getNodesCreatedByExpression() {
        return nodesCreatedByExpression;
    }

    public Map<ParseTreeNode, Collection<ParseTreeNode>> getNodeMapping() {
        return nodeMapping;
    }

    public ParseTree handleExpression(Expression expression) {
        ParseTree finalAnswerTree = ParseTreeUtil.copyTree(initialAnswerTree).getLeft();
        nodesCreatedByExpression = new HashSet<>();
        nodeMapping = new HashMap<>();

        buildWordOrderNodeIdMaps(finalAnswerTree);
        Collection<Pair<String, Integer>> nodesForCreation = new ArrayList<>();

        Collection<ParseTreeNode> processedNodeVariables = new ArrayList<>();
        Set<ParseTreeNode> nodesForDeletion = new HashSet<ParseTreeNode>();
        Collection<ParseTreeNode> variableNodes = getVariableNodes(finalAnswerTree, expression);

        for (Variable variable : expression.getVariables()) {
            ParseTreeNode variableNode = getOriginalNodeByWordOrder(finalAnswerTree, variable.getWordOrder());
            if (variableNode != null) {
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
                    Collection<ParseTreeNode> nodesInSiblingSubTree = finalAnswerTree.getNodesInSubtree(sibling);
                    for (ParseTreeNode node : nodesInSiblingSubTree) {
                        if (variableNodes.contains(node)) {
                            siblingsWithVariable.add(sibling);
                        }
                    }
                }

                boolean variableMoved = false;
                if (!siblingsWithVariable.isEmpty()) {
                    int firstWordOrderInSiblingsSubTrees = ParseTreeUtil.getFirstWordOrderInSubTrees(finalAnswerTree, siblingsWithVariable);
                    if (firstWordOrderInSiblingsSubTrees < variableNode.wordOrder) {
                        Collection<ParseTreeNode> attachedNodes = attachCopyOfSubTreeBefore(finalAnswerTree, variableNodeSubTreeRoot.parent, variableNodeSubTreeRoot, Collections.singletonMap(variableNode.nodeID, variable.getValue()), firstWordOrderInSiblingsSubTrees);

                        nodesForDeletion.add(variableNodeSubTreeRoot);
                        nodesCreatedByExpression.addAll(attachedNodes);
                        variableMoved = true;
                    }
                }
                if (!variableMoved) {
                    variableNode.label = variable.getValue();
                    nodesCreatedByExpression.add(variableNode);
                    addNodeMapping(variableNode, variableNode);
                }

                processedNodeVariables.add(variableNodeSubTreeRoot);
            }
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
            if (node != null) {
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
        }

        for (Expression multiVariableUnNestedSubExpression : multiVariableUnNestedSubExpressions) {
            Collection<ParseTreeNode> nodesInExpression = new ArrayList<ParseTreeNode>();
            Map<Integer, String> nodeValues = new HashMap<Integer, String>();
            for (Variable variable : multiVariableUnNestedSubExpression.getVariables()) {
                ParseTreeNode node = getOriginalNodeByWordOrder(finalAnswerTree, variable.getWordOrder());
                if (node != null) {
                    nodeValues.put(node.nodeID, variable.getValue());
                    nodesInExpression.add(node);
                }
            }
            ParseTreeNode jointParent = ParseTreeUtil.getJointParent(finalAnswerTree, nodesInExpression);
            int lastWordOrderInParentSubTree = ParseTreeUtil.getLastWordOrderInSubTree(finalAnswerTree, jointParent);

            Collection<ParseTreeNode> nodesInExpressionSubTreeRoots = new HashSet<ParseTreeNode>();
            for (ParseTreeNode potentialSubTreeRoots : jointParent.children) {
                Collection<ParseTreeNode> nodesInSubTree = finalAnswerTree.getNodesInSubtree(potentialSubTreeRoots);
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
                parentNode = finalAnswerTree.buildNode((new String[]{String.valueOf(lastWordOrderInParentSubTree + 1), "\nand", "NA", String.valueOf(jointParent.wordOrder), "NA"}));
                nodesCreatedByExpression.add(parentNode);
            }
            for (ParseTreeNode node : nodesInExpressionSubTreeRoots) {
                Collection<ParseTreeNode> attachedNodes = attachCopyOfSubTree(finalAnswerTree, parentNode, node, nodeValues, lastWordOrderInParentSubTree + 2);
                nodesCreatedByExpression.addAll(attachedNodes);
            }
        }

        for (Expression nestedSubExpression : nestedSubExpressions) {
            FactorizedAnswerTreeBuilder factorizedAnswerTreeBuilder = new FactorizedAnswerTreeBuilder(initialAnswerTree, queryToAnswerNodeMappings);
            ParseTree recursiveParseTree = factorizedAnswerTreeBuilder.handleExpression(nestedSubExpression);
            Collection<ParseTreeNode> nodesFromSubExpressionTreeInExpression = factorizedAnswerTreeBuilder.getNodesCreatedByExpression();
            //TODO nave - add the node mappings from the rec call

            Collection<ParseTreeNode> nodesFromFinalTreeInExpression = getNodesInExpression(finalAnswerTree, nestedSubExpression);
            if (!nodesFromSubExpressionTreeInExpression.isEmpty()) {
                for (ParseTreeNode node : nodesFromSubExpressionTreeInExpression) {
                    if (node.label.startsWith("\n")) {
                        node.label = "\n\t" + node.label.substring(1);
                    }
                }

                ParseTreeNode jointParentFromFinalTree = ParseTreeUtil.getJointParent(finalAnswerTree, nodesFromFinalTreeInExpression);
                int lastWordOrderInParentSubTree = ParseTreeUtil.getLastWordOrderInSubTree(finalAnswerTree, jointParentFromFinalTree);

                ParseTreeNode jointParentFromSubExpressionTree = ParseTreeUtil.getJointParent(recursiveParseTree, nodesFromSubExpressionTreeInExpression);
                Collection<ParseTreeNode> nodesInExpressionSubTreeRoots = new HashSet<>();
                for (ParseTreeNode potentialSubTreeRoots : jointParentFromSubExpressionTree.children) {
                    Collection<ParseTreeNode> nodesInSubTree = recursiveParseTree.getNodesInSubtree(potentialSubTreeRoots);
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
                    parentNode = finalAnswerTree.buildNode((new String[]{String.valueOf(lastWordOrderInParentSubTree + 1), "\nand", "NA", String.valueOf(jointParentFromFinalTree.wordOrder), "NA"}));
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

    private void buildWordOrderNodeIdMaps(ParseTree parseTree) {
        wordOrderToNodeId = new HashMap<>();
        nodeIdToOriginalNode = new HashMap<>();
        for (Map.Entry<ParseTreeNode, Collection<ParseTreeNode>> queryToAnswerNodes : queryToAnswerNodeMappings.entrySet()) {
            int queryWordOrder = queryToAnswerNodes.getKey().wordOrder;
            Collection<ParseTreeNode> originalAnswerNodes = queryToAnswerNodes.getValue();

            assert originalAnswerNodes.size() == 1;

            ParseTreeNode originalAnswerNode = originalAnswerNodes.iterator().next();
            ParseTreeNode answerNode = parseTree.searchNodeByOrder(originalAnswerNode.wordOrder);
            wordOrderToNodeId.put(queryWordOrder, answerNode.nodeID);
            nodeIdToOriginalNode.put(answerNode.nodeID, originalAnswerNode);
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
            if (node != null) {
                nodesInExpression.add(node);
            }
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

        Collection<ParseTreeNode> nodesInSubTree = parseTree.getNodesInSubtree(subTreeRoot);
        int minWordOrder = Integer.MAX_VALUE;
        int maxWordOrder = Integer.MIN_VALUE;
        for (ParseTreeNode node : nodesInSubTree) {
            minWordOrder = Math.min(minWordOrder, node.wordOrder);
            maxWordOrder = Math.max(maxWordOrder, node.wordOrder);
        }
        int lastWordOrder = wordOrder - minWordOrder;

        ParseTreeUtil.shiftWordOrders(parseTree, nodesInSubTree, wordOrder, maxWordOrder + 1 - minWordOrder);

        ParseTreeNode newNode = parseTree.buildNodeByParentId((new String[]{String.valueOf(lastWordOrder + subTreeRoot.wordOrder), nodeValue, "NA", String.valueOf(parent.nodeID), "NA"}));
        addNodeMapping(subTreeRoot, newNode);

        Collection<ParseTreeNode> attachedNodes = new HashSet<>();
        attachedNodes.add(newNode);
        for (ParseTreeNode child : subTreeRoot.children) {
            Collection<ParseTreeNode> attachedNodesRec = attachCopyOfSubTree(parseTree, newNode, child, nodeValues, lastWordOrder);
            attachedNodes.addAll(attachedNodesRec);
        }
        return attachedNodes;
    }

    private Collection<ParseTreeNode> attachCopyOfSubTree(ParseTree parseTree, ParseTreeNode parent, ParseTreeNode subTreeRoot, Map<Integer, String> nodeValues, int lastWordOrder) {
        String nodeValue = nodeValues.get(subTreeRoot.nodeID);
        if (nodeValue == null) {
            nodeValue = subTreeRoot.label;
        } else {
            nodeValue = StringUtil.getQuoatedString(nodeValue);
        }
        ParseTreeNode newNode = parseTree.buildNodeByParentId((new String[]{String.valueOf(lastWordOrder + subTreeRoot.wordOrder), nodeValue, "NA", String.valueOf(parent.nodeID), "NA"}));
        addNodeMapping(subTreeRoot, newNode);

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
        return nodeId == null ? null : parseTree.searchNodeByID(nodeId);
    }

    private Collection<ParseTreeNode> getVariableNodes(ParseTree parseTree, Expression expression) {
        Collection<ParseTreeNode> variableNodes = new HashSet<ParseTreeNode>();

        for (Variable variable : expression.getVariables()) {
            ParseTreeNode variableNode = getOriginalNodeByWordOrder(parseTree, variable.getWordOrder());
            if (variableNode != null) {
                variableNodes.add(variableNode);
            }
        }

        for (Expression subExpression : expression.getExpressions()) {
            Collection<ParseTreeNode> subExpressionVariableNodes = getVariableNodes(parseTree, subExpression);
            variableNodes.addAll(subExpressionVariableNodes);
        }
        return variableNodes;
    }

    private void addNodeMapping(ParseTreeNode oldNode, ParseTreeNode newNode) {
        ParseTreeNode originalNode = nodeIdToOriginalNode.get(oldNode.nodeID);
        if (originalNode != null) {
            Collection<ParseTreeNode> mappings = nodeMapping.get(originalNode);
            if (mappings == null) {
                mappings = new HashSet<>();
                nodeMapping.put(originalNode, mappings);
            }
            mappings.add(newNode);
        }
    }

}
