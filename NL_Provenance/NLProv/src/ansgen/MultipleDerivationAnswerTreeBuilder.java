package ansgen;

import dataStructure.ParseTreeNode;
import factorization.WordMappings;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nfrost on 1/25/2016
 */
public class MultipleDerivationAnswerTreeBuilder extends AbstractAnswerParseTreeBuilder {


    private static MultipleDerivationAnswerTreeBuilder instance = null;

    public static MultipleDerivationAnswerTreeBuilder getInstance(){
        if (instance == null) {
            instance = new MultipleDerivationAnswerTreeBuilder();
        }
        return instance;
    }

    @Override
    protected String getNodeValue(WordMappings wordReplacementMap, ParseTreeNode node) {
        Set<String> mappings = getSetOfMappings(wordReplacementMap, node.wordOrder);
        if (mappings.size() == 1) {
            return mappings.iterator().next();
        } else if (mappings.size() > 1) {
            StringBuilder objectValue = new StringBuilder();
            for (String mapping : mappings) {
                String quoatedMapping = null;
                if (mapping.startsWith("'") && mapping.endsWith("'")) {
                    quoatedMapping = mapping;
                } else if (mapping.startsWith("\"") && mapping.endsWith("\"")) {
                    quoatedMapping = mapping;
                } else {
                    quoatedMapping = "\"" + mapping + "\"";
                }
                objectValue.append(quoatedMapping).append(", ");
            }
            objectValue.deleteCharAt(objectValue.length() - 1);
            objectValue.deleteCharAt(objectValue.length() - 1);
            return objectValue.toString();
        }
        return null;
    }

    private Set<String> getSetOfMappings(WordMappings wordReplacementMap, int wordOrder) {
        Set<String> mappings = new HashSet<>();
        for (int i = 0; i < wordReplacementMap.getLastDerivation() + 1; i++) {
            if (wordReplacementMap.contains(i, wordOrder)) {
                mappings.add(wordReplacementMap.get(i, wordOrder));
            }
        }
        return mappings;
    }
}
