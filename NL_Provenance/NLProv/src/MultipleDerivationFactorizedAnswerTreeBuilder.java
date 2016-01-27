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

        Collection<ParseTreeNode> processedNodeVariables = new ArrayList<>();
        handleExpression(finalAnswerTree, factorizeExpression, processedNodeVariables);


        return finalAnswerTree;
    }

    private void handleExpression(ParseTree finalAnswerTree, WordMappings.Expression expression, Collection<ParseTreeNode> processedNodeVariables) {
        for (WordMappings.Variable variable : expression.getVariables()) {
            ParseTreeNode variableNode = finalAnswerTree.searchNodeByOrder(variable.getWordOrder());
            ParseTreeNode variableNodeSubTreeRoot = variableNode;
            if (!variableNode.parent.relationship.equals("nsubj") && !variableNode.parent.label.equals("ROOT")) {
                variableNodeSubTreeRoot = variableNode.parent;
            }

            Collection<ParseTreeNode> siblings = getSiblings(variableNodeSubTreeRoot);
            for (ParseTreeNode processedNodeVariable : processedNodeVariables) {
                siblings.remove(processedNodeVariable);
            }

            if (!siblings.isEmpty()) {
                int firstWordOrderInSiblingsSubTrees = getFirstWordOrderInSubTrees(siblings);
                setWordOrderInSubTree(variableNodeSubTreeRoot, firstWordOrderInSiblingsSubTrees - 1);
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

        Map<Integer, List<String>> singleVariableWordOrderToValues = new HashMap<>();
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
            ParseTreeNode node = finalAnswerTree.searchNodeByOrder(wordOrderToValues.getKey());
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
                ParseTreeNode node = finalAnswerTree.searchNodeByOrder(variable.getWordOrder());
                nodesInExpression.add(node);
                nodeValues.put(variable.getWordOrder(), variable.getValue());
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
                    }
                }
            }

            finalAnswerTree.buildNode((new String[]{String.valueOf(lastWordOrderInParentSubTree + 1), "and\n", "NA", String.valueOf(jointParent.wordOrder), "NA"}));
            ParseTreeNode andNode = finalAnswerTree.searchNodeByOrder(lastWordOrderInParentSubTree + 1);
            for (ParseTreeNode node : nodesInExpressionSubTreeRoots) {
                attachCopyOfSubTree(finalAnswerTree, andNode, node, nodeValues, lastWordOrderInParentSubTree + 2);
            }
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


    private void setWordOrderInSubTree(ParseTreeNode node, int wordOrder) {
        Collection<ParseTreeNode> nodesInSubTree = new HashSet<>();
        getNodesInSubTree(nodesInSubTree, node);
        for (ParseTreeNode currNode : nodesInSubTree) {
            currNode.wordOrder = wordOrder;
        }
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

    private void attachCopyOfSubTree(ParseTree parseTree, ParseTreeNode parent, ParseTreeNode subTreeRoot, Map<Integer, String> nodeValues, int lastWordOrder) {
        String nodeValue = nodeValues.get(subTreeRoot.wordOrder);
        if (nodeValue == null) {
            nodeValue = subTreeRoot.label;
        } else {
            nodeValue = getQuoatedString(nodeValue);
        }
        parseTree.buildNode((new String[]{String.valueOf(lastWordOrder + subTreeRoot.wordOrder), nodeValue, "NA", String.valueOf(parent.wordOrder), "NA"}));
        ParseTreeNode newNode = parseTree.searchNodeByOrder(lastWordOrder + subTreeRoot.wordOrder);

        for (ParseTreeNode child : subTreeRoot.children) {
            attachCopyOfSubTree(parseTree, newNode, child, nodeValues, lastWordOrder);
        }

    }
}
