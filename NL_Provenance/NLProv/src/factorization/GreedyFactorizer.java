package factorization;

import ansgen.AnswerTreeBuilderResult;
import ansgen.FactorizedAnswerTreeBuilder;
import ansgen.SingleDerivationAnswerTreeBuilder;
import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by nfrost on 2/4/2016
 */
public class GreedyFactorizer implements Factorizer {

    private ParseTree parseTree;

    public GreedyFactorizer(ParseTree parseTree) {
        this.parseTree = parseTree;
    }

    @Override
    public Expression factorize(WordMappings wordMappings){
        Expression expression = new Expression(wordMappings);
        return factorize(wordMappings, expression);
    }

    private Expression factorize(WordMappings wordMappings, Expression initialExpression) {
        Expression expression = initialExpression.deepCopy();
        Collection<Pair<Expression, Variable>> candidatesForFactorization = getCandidatesForFactorization(expression);

        if (candidatesForFactorization.isEmpty()) {
            return expression;
        } else {
            double bestScore = -Double.MAX_VALUE;
            Expression bestExpression = null;

            for (Pair<Expression, Variable> currCandidate : candidatesForFactorization) {
                Expression candidateExpression = currCandidate.getLeft();
                Variable candidateVariable = currCandidate.getRight();
                Expression candidateExpressionFactorized = factorizeExpressionByVariable(expression, candidateExpression, candidateVariable);
                double score = calculateExpressionScore(wordMappings, candidateExpressionFactorized);

                if (score > bestScore) {
                    bestScore = score;
                    bestExpression = candidateExpressionFactorized;
                }
            }

            return factorize(wordMappings, flatten(bestExpression));
        }
    }

    private double calculateExpressionScore(WordMappings wordMappings, Expression expression) {
        AnswerTreeBuilderResult singleDerivationAnswerTreeResult = SingleDerivationAnswerTreeBuilder.getInstance().buildParseTree(parseTree, wordMappings);
        ParseTree singleDerivationAnswerTree = singleDerivationAnswerTreeResult.getParseTree();
        FactorizedAnswerTreeBuilder factorizedAnswerTreeBuilder = new FactorizedAnswerTreeBuilder(singleDerivationAnswerTree, singleDerivationAnswerTreeResult.getNodeMappings());
        ParseTree answerTree = factorizedAnswerTreeBuilder.handleExpression(expression);

        Map<ParseTreeNode, Collection<ParseTreeNode>> queryToSingleDerivationAnswerTreeNodeMappings = singleDerivationAnswerTreeResult.getNodeMappings();
        Map<ParseTreeNode, Collection<ParseTreeNode>> singleDerivationToFactorizedAnswerTreeNodeMappings = factorizedAnswerTreeBuilder.getNodeMapping();
        Map<ParseTreeNode, Collection<ParseTreeNode>> queryToFactorizedAnswerTreeNodeMappings = getQueryToFactorizedAnswerTreeNodeMappings(queryToSingleDerivationAnswerTreeNodeMappings, singleDerivationToFactorizedAnswerTreeNodeMappings);

        return new ParseTreeScorer(parseTree, answerTree, queryToFactorizedAnswerTreeNodeMappings).score();
    }

    private Expression factorizeExpressionByVariable(Expression expression, Expression expressionForFactorization, Variable variableForFactorization) {
        boolean full = true;
        for (Expression subExpression : expressionForFactorization.getExpressions()) {
            if (!subExpression.getVariables().contains(variableForFactorization)) {
                full = false;
            }
        }
        Pair<Expression, Expression> expressionsCopy = copy(expression, expressionForFactorization);
        Expression expressionCopy = expressionsCopy.getLeft();
        Expression expressionForFactorizationCopy = expressionsCopy.getRight();

        if (full) {
            factorizeExpressionByVariableFull(expressionForFactorizationCopy, variableForFactorization);
        } else {
            factorizeExpressionByVariablePartial(expressionForFactorizationCopy, variableForFactorization);
        }
        return expressionCopy;
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

    private void factorizeExpressionByVariablePartial(Expression expression, Variable variable) {
        List<Expression> newExpressions = new ArrayList<Expression>();
        Expression variableExpression = new Expression();
        variableExpression.getVariables().add(variable);
        newExpressions.add(variableExpression);

        for (Expression subExpression : expression.getExpressions()) {
            if (subExpression.getVariables().contains(variable)) {
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

    private Collection<Pair<Expression, Variable>> getCandidatesForFactorization(Expression expression) {
        Collection<Pair<Expression, Variable>> candidates = new ArrayList<Pair<Expression, Variable>>();

        for (Variable variable : getVariableCandidateForFactorization(expression)) {
            candidates.add(new ImmutablePair<Expression, Variable>(expression, variable));
        }

        for (Expression subExpression : expression.getExpressions()) {
            candidates.addAll(getCandidatesForFactorization(subExpression));
        }
        return candidates;
    }

    private Collection<Variable> getVariableCandidateForFactorization(Expression expression) {
        Map<Variable, Integer> variableApps = new HashMap<Variable, Integer>();

        for (Expression subExpression : expression.getExpressions()) {
            for (Variable variable : subExpression.getVariables()) {
                Integer variableCount = variableApps.get(variable);
                if (variableCount == null) {
                    variableCount = 0;
                }
                variableApps.put(variable, variableCount + 1);
            }
        }

        Set<Variable> variableCandidateForFactorization = new HashSet<Variable>();
        for (Map.Entry<Variable, Integer> variableAppsEntry : variableApps.entrySet()) {
            if (variableAppsEntry.getValue() > 1 || variableAppsEntry.getValue() == expression.getExpressions().size()) {
                variableCandidateForFactorization.add(variableAppsEntry.getKey());
            }
        }

        return variableCandidateForFactorization;
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

    private Pair<Expression, Expression> copy(Expression expression, Expression subExpression) {
        Expression expressionCopy = expression.deepCopy();
        Expression subExpressionCopy = find(expressionCopy, expression, subExpression);
        return new ImmutablePair<Expression, Expression>(expressionCopy, subExpressionCopy);
    }

    private Expression find(Expression copyExpression, Expression expression, Expression subExpression) {
        if (expression.equals(subExpression)) {
            return copyExpression;
        }

        Iterator<Expression> expressionIterator = expression.getExpressions().iterator();
        Iterator<Expression> copyExpressionIterator = copyExpression.getExpressions().iterator();

        while (expressionIterator.hasNext() && copyExpressionIterator.hasNext()) {
            Expression result = find(copyExpressionIterator.next(), expressionIterator.next(), subExpression);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private Map<ParseTreeNode, Collection<ParseTreeNode>> getQueryToFactorizedAnswerTreeNodeMappings(Map<ParseTreeNode, Collection<ParseTreeNode>> queryToSingleDerivationAnswerTreeNodeMappings, Map<ParseTreeNode, Collection<ParseTreeNode>> singleDerivationToFactorizedAnswerTreeNodeMappings) {
        Map<ParseTreeNode, Collection<ParseTreeNode>> queryToFactorizedAnswerTreeNodeMappings = new HashMap<ParseTreeNode, Collection<ParseTreeNode>>();
        for (Map.Entry<ParseTreeNode, Collection<ParseTreeNode>> entry : queryToSingleDerivationAnswerTreeNodeMappings.entrySet()) {
            ParseTreeNode queryTreeNode = entry.getKey();
            Collection<ParseTreeNode> singleDerivationAnswerNodes = entry.getValue();
            for (ParseTreeNode singleDerivationAnswerNode : singleDerivationAnswerNodes) {
                if (singleDerivationToFactorizedAnswerTreeNodeMappings.containsKey(singleDerivationAnswerNode)) {
                    Collection<ParseTreeNode> factorizedAnswerTreeNodes = singleDerivationToFactorizedAnswerTreeNodeMappings.get(singleDerivationAnswerNode);
                    Collection<ParseTreeNode> queryNodeMappings = queryToFactorizedAnswerTreeNodeMappings.get(queryTreeNode);
                    if (queryNodeMappings == null) {
                        queryNodeMappings = new HashSet<ParseTreeNode>();
                        queryToFactorizedAnswerTreeNodeMappings.put(queryTreeNode, queryNodeMappings);
                    }
                    queryNodeMappings.addAll(factorizedAnswerTreeNodes);
                }
            }
        }
        return queryToFactorizedAnswerTreeNodeMappings;
    }

}