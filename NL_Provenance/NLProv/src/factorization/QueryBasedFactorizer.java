package factorization;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import utils.ParseTreeUtil;

import java.util.*;

/**
 * Created by nfrost on 2/11/2016
 */
//TODO Nave - organize and delete previous implementation
public class QueryBasedFactorizer implements Factorizer {

    private ParseTree parseTree;

    public QueryBasedFactorizer(ParseTree parseTree) {
        this.parseTree = parseTree;
    }

    @Override
    public Expression factorize(WordMappings wordMappings){
        List<Collection<Integer>> wordOrdersByQueryHierarchy = getWordOrdersByQueryHierarchy(wordMappings);
        Map<Integer, Collection<Integer>> wordOrdersAncestorMap = getWordOrdersAncestorMap(wordMappings);
        Expression expression = new Expression(wordMappings);
        for (Collection<Integer> wordOrders : wordOrdersByQueryHierarchy) {
            for (Integer wordOrder : wordOrders) {
                Map<Variable, Collection<Expression>> variablesForFactorization = getVariablesByWordOrder(expression, wordOrder, wordOrdersAncestorMap);
                for (Map.Entry<Variable, Collection<Expression>> variableForFactorization : variablesForFactorization.entrySet()) {
                    for (Expression expressionForFactorizationByVariable : variableForFactorization.getValue()) {
                        factorizeExpressionByVariable(expressionForFactorizationByVariable, variableForFactorization.getKey(), wordOrdersAncestorMap);
                    }
                }
            }
        }
        return flatten(expression);
    }

    private List<Collection<Integer>> getWordOrdersByQueryHierarchy(WordMappings wordMappings) {
        List<Collection<Integer>> wordOrdersByQueryHierarchy = new LinkedList<Collection<Integer>>();
        SortedMap<Integer, Collection<ParseTreeNode>> nodesByDepth = ParseTreeUtil.getNodesByDepth(parseTree);
        for (Map.Entry<Integer, Collection<ParseTreeNode>> nodesByDepthEntry : nodesByDepth.entrySet()) {
            Collection<Integer> wordOrdersInDepth = new LinkedList<Integer>();
            for (ParseTreeNode node : nodesByDepthEntry.getValue()) {
                String value = wordMappings.get(0, node.wordOrder);
                if (value != null) {
                    wordOrdersInDepth.add(node.wordOrder);
                }
            }
            if (!wordOrdersInDepth.isEmpty()) {
                wordOrdersByQueryHierarchy.add(wordOrdersInDepth);
            }
        }

        return wordOrdersByQueryHierarchy;
    }

    private Map<Integer, Collection<Integer>> getWordOrdersAncestorMap(WordMappings wordMappings) {
        Map<Integer, Collection<Integer>> wordOrdersAncestorMap = new HashMap<>();
        for (ParseTreeNode node: parseTree.allNodes) {
            if (wordMappings.get(0, node.wordOrder) != null) {
                Collection<Integer> ancestors = new LinkedList<Integer>();
                ParseTreeNode ancestor = node.parent;
                while (ancestor != null) {
                    if (wordMappings.get(0, ancestor.wordOrder) != null) {
                        ancestors.add(ancestor.wordOrder);
                    }
                    ancestor = ancestor.parent;
                }
                wordOrdersAncestorMap.put(node.wordOrder, ancestors);
            }
        }
        return wordOrdersAncestorMap;
    }


    private boolean containsAncestor(Expression expression, Integer variableWordOrder, Map<Integer, Collection<Integer>> wordOrdersAncestorMap) {
        Collection<Integer> ancestors = wordOrdersAncestorMap.get(variableWordOrder);
        if (ancestors != null) {
            Set<Integer> expressionVariablesWordOrder = new HashSet<>();
            for (Variable expressionVariable : expression.getVariables()) {
                expressionVariablesWordOrder.add(expressionVariable.getWordOrder());
            }
            for (Integer ancestor : ancestors) {
                if (expressionVariablesWordOrder.contains(ancestor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void factorizeExpressionByVariable(Expression expression, Variable variable, Map<Integer, Collection<Integer>> wordOrdersAncestorMap) {
        boolean full = true;
        for (Expression subExpression : expression.getExpressions()) {
            if (!subExpression.getVariables().contains(variable) || containsAncestor(subExpression, variable.getWordOrder(), wordOrdersAncestorMap)) {
                full = false;
            }
        }

        if (full) {
            factorizeExpressionByVariableFull(expression, variable);
        } else {
            factorizeExpressionByVariablePartial(expression, variable, wordOrdersAncestorMap);
        }
    }

    private void factorizeExpressionByVariableFull(Expression expression, Variable variable) {
        expression.getVariables().add(variable);
        for (Expression subExpression : expression.getExpressions()) {
            subExpression.getVariables().remove(variable);
        }

        Set<Expression> emptyExpressions = new HashSet<>();
        for (Expression subExpression : expression.getExpressions()) {
            if (subExpression.getVariables().isEmpty() && subExpression.getExpressions().isEmpty()) {
                emptyExpressions.add(subExpression);
            }
        }
        for (Expression emptyExpression : emptyExpressions) {
            expression.getExpressions().remove(emptyExpression);
        }
    }

    private void factorizeExpressionByVariablePartial(Expression expression, Variable variable, Map<Integer, Collection<Integer>> wordOrdersAncestorMap) {
        List<Expression> newExpressions = new ArrayList<Expression>();
        Expression variableExpression = new Expression();
        variableExpression.getVariables().add(variable);
        newExpressions.add(variableExpression);

        for (Expression subExpression : expression.getExpressions()) {
            if (subExpression.getVariables().contains(variable) && !containsAncestor(subExpression, variable.getWordOrder(), wordOrdersAncestorMap)) {
                subExpression.getVariables().remove(variable);
                if (!subExpression.getVariables().isEmpty() || !subExpression.getExpressions().isEmpty()) {
                    variableExpression.getExpressions().add(subExpression);
                }
            } else {
                newExpressions.add(subExpression);
            }
        }

        expression.setExpressions(newExpressions);
    }

    private Expression flatten(Expression expression) {
        Expression flattenExpression = expression.deepCopy();
        flatten(null, flattenExpression);
        return flattenExpression;
    }

    private void flatten(Expression parent, Expression child) {
        Expression nextParent = child;
        if (child.getVariables().isEmpty() && parent != null) {
            parent.getExpressions().remove(child);
            for (Expression subExpression : child.getExpressions()) {
                parent.getExpressions().add(subExpression);
            }
            nextParent = parent;
        }

        List<Expression> childSubExpressionCopy = new ArrayList<Expression>();
        for (Expression childSubExpression : child.getExpressions()) {
            childSubExpressionCopy.add(childSubExpression);
        }

        for (Expression subExpression : childSubExpressionCopy) {
            flatten(nextParent, subExpression);
        }
    }

    private Map<Variable, Collection<Expression>> getVariablesByWordOrder(Expression expression, int wordOrder, Map<Integer, Collection<Integer>> wordOrdersAncestorMap) {
        Map<Variable, Collection<Expression>> variablesByWordOrder = new HashMap<Variable, Collection<Expression>>();
        getVariablesByWordOrder(variablesByWordOrder, expression, wordOrder, wordOrdersAncestorMap);
        return variablesByWordOrder;
    }

    private void getVariablesByWordOrder(Map<Variable, Collection<Expression>> variablesByWordOrder, Expression expression, int wordOrder, Map<Integer, Collection<Integer>> wordOrdersAncestorMap) {
        Map<Variable, Integer> variablesCount = new HashMap<>();
        for (Expression subExpression : expression.getExpressions()) {
            if (!containsAncestor(subExpression, wordOrder, wordOrdersAncestorMap)) {
                for (Variable variable : subExpression.getVariables()) {
                    if (variable.getWordOrder() == wordOrder) {
                        Integer count = variablesCount.get(variable);
                        if (count == null) {
                            count = 0;
                        }
                        variablesCount.put(variable, count + 1);
                    }
                }
            }

            getVariablesByWordOrder(variablesByWordOrder, subExpression, wordOrder, wordOrdersAncestorMap);
        }

        for (Map.Entry<Variable, Integer> variableCount : variablesCount.entrySet()) {
            if (variableCount.getValue() > 1) {
                Collection<Expression> expressions = variablesByWordOrder.get(variableCount.getKey());
                if (expressions == null) {
                    expressions = new HashSet<>();
                    variablesByWordOrder.put(variableCount.getKey(), expressions);
                }
                expressions.add(expression);
            }
        }

    }

}