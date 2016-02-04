package factorization;

import java.util.*;

/**
 * Created by nfrost on 2/4/2016
 */
public class Expression {
    private Set<Expression> expressions;
    private Set<Variable> variables;

    public Expression() {
        expressions = new HashSet<>();
        variables = new TreeSet<Variable>(new Comparator<Variable>() {
            @Override
            public int compare(Variable o1, Variable o2) {
                int wordOrderCompare = o1.getWordOrder() - o2.getWordOrder();
                if (wordOrderCompare != 0) {
                    return wordOrderCompare;
                } else {
                    return o1.getValue().compareTo(o2.getValue());
                }
            }
        });
    }

    public Expression(WordMappings wordMappings) {
        this();

        Map<Integer, Map<Integer, String>> wordMappingByDerivation = wordMappings.getWordMappingByDerivation();

        for (Map.Entry<Integer, Map<Integer, String>> derivationWordMapping : wordMappingByDerivation.entrySet()) {
            Expression expression = new Expression();
            for (Map.Entry<Integer, String> variable : derivationWordMapping.getValue().entrySet()) {
                expression.variables.add(new Variable(variable.getKey(), variable.getValue()));
            }
            expressions.add(expression);
        }

    }

    public void setExpressions(Set<Expression> expressions) {
        this.expressions = expressions;
    }

    public Set<Expression> getExpressions() {
        return expressions;
    }

    public Set<Variable> getVariables() {
        return variables;
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

    public Expression deepCopy() {
        Expression copy = new Expression();

        for (Variable variable : getVariables()) {
            copy.getVariables().add(variable.deepCopy());
        }

        for (Expression subExpression : getExpressions()) {
            copy.getExpressions().add(subExpression.deepCopy());
        }
        return copy;
    }
}

