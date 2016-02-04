package factorization;

import utils.MapUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by nfrost on 2/4/2016
 */
public class SimpleFactorizer implements Factorizer {

    private static SimpleFactorizer instance = null;

    public static SimpleFactorizer getInstance(){
        if (instance == null) {
            instance = new SimpleFactorizer();
        }
        return instance;
    }

    @Override
    public Expression factorize(WordMappings wordMappings){
        Expression expression = new Expression(wordMappings);
        factorize(expression);
        return expression;
    }

    private void factorize(Expression expression) {
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

        Map<Variable, Integer> variableAppsSorted = MapUtil.sortByValue(variableApps, true);
        for (Map.Entry<Variable, Integer> variableAppsEntry : variableAppsSorted.entrySet()) {
            if (variableAppsEntry.getValue() == expression.getExpressions().size()) {
                Variable variable = variableAppsEntry.getKey();
                expression.getVariables().add(variable);
                for (Expression subExpression : expression.getExpressions()) {
                    subExpression.getVariables().remove(variable);
                }
            }
        }

        for (Map.Entry<Variable, Integer> variableAppsEntry : variableAppsSorted.entrySet()) {
            if (variableAppsEntry.getValue() > 1) {
                factorizePartialVariable(expression, variableAppsEntry.getKey());
            }
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

        for (Expression subExpression : expression.getExpressions()) {
            factorize(subExpression);
        }
    }

    private void factorizePartialVariable(Expression expression, Variable variable) {
        int variableCurrentApps = 0;
        for (Expression subExpression : expression.getExpressions()) {
            if (subExpression.getVariables().contains(variable)) {
                variableCurrentApps++;
            }
        }

        if (variableCurrentApps > 1) {
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
    }
}
