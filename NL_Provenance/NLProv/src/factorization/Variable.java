package factorization;

/**
 * Created by nfrost on 2/4/2016
 */
public class Variable {
    private int wordOrder;
    private String value;

    public Variable(int wordOrder, String value) {
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

    public Variable deepCopy() {
        return new Variable(wordOrder, value);
    }

}
