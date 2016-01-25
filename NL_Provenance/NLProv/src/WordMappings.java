import java.util.*;

/**
 * Created by nfrost on 1/24/2016
 */
public class WordMappings {

    private Map<Integer, Map<Integer, String>> wordMappingByDerivation = new HashMap<>();
    private int lastDerivation = -1;

    public int getLastDerivation() {
        return lastDerivation;
    }

    public void add(int derivation, int wordOrder, String mappedValue) {
        if (derivation > lastDerivation) {
            lastDerivation = derivation;
        }
        if (!wordMappingByDerivation.containsKey(derivation)) {
            wordMappingByDerivation.put(derivation, new HashMap<>());
        }
        wordMappingByDerivation.get(derivation).put(wordOrder, mappedValue);
    }

    public boolean contains(int derivation, int wordOrder) {
        return wordMappingByDerivation.containsKey(derivation) && wordMappingByDerivation.get(derivation).containsKey(wordOrder);
    }

    public String get(int derivation, int wordOrder) {
        return wordMappingByDerivation.get(derivation).get(wordOrder);
    }



    public Expression createFactorizeExpression() {
        Expression expression = new Expression(wordMappingByDerivation);
        expression.factorize();
        return expression;
    }

    private static class Expression {
        private Set<Expression> expressions;
        private Set<Variable> variables;
        private Expression parent;

        private Expression() {
            expressions = new HashSet<>();
            variables = new HashSet<>();
            parent = null;
        }

        private Expression(Map<Integer, Map<Integer, String>> wordMappingByDerivation) {
            expressions = new HashSet<>();
            variables = new HashSet<>();
            parent = null;

            for (Map.Entry<Integer, Map<Integer, String>> derivationWordMapping : wordMappingByDerivation.entrySet()) {
                Expression expression = new Expression();
                for (Map.Entry<Integer, String> variable : derivationWordMapping.getValue().entrySet()) {
                    expression.variables.add(new Variable(variable.getKey(), variable.getValue()));
                }
                expression.parent = this;
                expressions.add(expression);
            }

        }

        private void factorize() {

            for (Expression expression : expressions) {
                expression.factorize();
            }

            Map<Variable, Integer> variableApps = new HashMap<>();
            for (Expression expression : expressions) {
                for (Variable variable : expression.variables) {
                    Integer variableCount = variableApps.get(variable);
                    if (variableCount == null) {
                        variableCount = 0;
                    }
                    variableApps.put(variable, variableCount + 1);
                }
            }

            for (Map.Entry<Variable, Integer> variableAppsEntry : variableApps.entrySet()) {
                if (variableAppsEntry.getValue() == expressions.size()) {
                    Variable variable = variableAppsEntry.getKey();
                    variables.add(variable);
                    for (Expression expression : expressions) {
                        expression.variables.remove(variable);
                    }
                }
            }

            for (Map.Entry<Variable, Integer> variableAppsEntry : variableApps.entrySet()) {
                if (variableAppsEntry.getValue() < expressions.size() && variableAppsEntry.getValue() > 1) {
                    factorizePartialVariable(variableAppsEntry.getKey());
                }
            }
        }

        private void factorizePartialVariable(Variable variable) {
            Set<Expression> newExpressions = new HashSet<>();
            Expression variableExpression = new Expression();
            variableExpression.variables.add(variable);
            variableExpression.parent = this;
            newExpressions.add(variableExpression);

            for (Expression expression : expressions) {
                if (expression.variables.contains(variable)) {
                    expression.variables.remove(variable);
                    expression.parent = variableExpression;
                    variableExpression.expressions.add(expression);
                } else {
                    newExpressions.add(expression);
                }
            }

            this.expressions = newExpressions;

        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            boolean isFirst = true;
            for (Variable variable : variables) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(" * ");
                }

                sb.append(variable);
            }

            isFirst = true;
            if (expressions.size() > 1) {
                sb.append(" * (\n");
            }
            for (Expression expression : expressions) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(" + ");
                }
                StringTokenizer st = new StringTokenizer(expression.toString(), "\n");
                while (st.hasMoreElements()) {
                    sb.append("\t").append(st.nextElement()).append("\n");
                }
            }
            if (expressions.size() > 1) {
                sb.append(")");
            }

            return sb.toString();
        }
    }

    private static class Variable {
        private int wordOrder;
        private String value;

        private Variable(int wordOrder, String value) {
            this.wordOrder = wordOrder;
            this.value = value;
        }

        public int getWordOrder() {
            return wordOrder;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "{" + wordOrder + ":'" + value + "'}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Variable variable = (Variable) o;

            if (wordOrder != variable.wordOrder) return false;
            if (!value.equals(variable.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = wordOrder;
            result = 31 * result + value.hashCode();
            return result;
        }
    }


}
