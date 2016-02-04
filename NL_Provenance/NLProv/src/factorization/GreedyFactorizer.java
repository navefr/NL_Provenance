package factorization;

import dataStructure.ParseTree;
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
        return factorize(expression);
    }

    private Expression factorize(Expression initialExpression) {
        Expression expression = initialExpression.deepCopy();
        Collection<Pair<Expression, Variable>> candidatesForFactorization = getCandidatesForFactorization(expression);

        if (candidatesForFactorization.isEmpty()) {
            return expression;
        } else {
            int bestScore = Integer.MIN_VALUE;
            Pair<Expression, Variable> bestCandidate = null;
            Expression bestExpression = null;

            for (Pair<Expression, Variable> currCandidate : candidatesForFactorization) {
                Expression candidateExpression = currCandidate.getLeft();
                Variable candidateVariable = currCandidate.getRight();
                // TODO nave - this factorize only the candidate expression (which may be sub expression) and we would like to factorzie the entire expression
                Expression candidateExpressionFactorized = factorizeExpressionByVariable(candidateExpression, candidateVariable);
                int score = calculateExpressionScore(candidateExpressionFactorized);

                if (score > bestScore) {
                    bestScore = score;
                    bestCandidate = currCandidate;
                    bestExpression = candidateExpressionFactorized;
                }
            }

            return factorize(bestExpression);
        }
    }

    private int calculateExpressionScore(Expression expression) {
        //            MultipleDerivationAnswerTreeBuilder.getInstance().buildParseTree()
        return (int) (Math.random() * 1000);
    }

    private Expression factorizeExpressionByVariable(Expression expression, Variable variable) {
        boolean full = true;
        for (Expression subExpression : expression.getExpressions()) {
            if (!subExpression.getVariables().contains(variable)) {
                full = false;
            }
        }
        Expression factorizedExpression = expression.deepCopy();
        if (full) {
            factorizeExpressionByVariableFull(factorizedExpression, variable);
        } else {
            factorizeExpressionByVariablePartial(factorizedExpression, variable);
        }
        return factorizedExpression;
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
        Set<Expression> newExpressions = new HashSet<>();
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
}