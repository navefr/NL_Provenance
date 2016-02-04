package factorization;

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

    public Map<Integer, Map<Integer, String>> getWordMappingByDerivation() {
        return wordMappingByDerivation;
    }
}
