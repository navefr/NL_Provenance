package ansgen;

import dataStructure.ParseTreeNode;
import factorization.WordMappings;
import org.apache.commons.lang3.math.NumberUtils;
import utils.StringUtil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by nfrost on 1/25/2016
 */
public class MultipleDerivationSummarizedAnswerTreeBuilder extends AbstractAnswerParseTreeBuilder {


    private static MultipleDerivationSummarizedAnswerTreeBuilder instance = null;

    public static MultipleDerivationSummarizedAnswerTreeBuilder getInstance(){
        if (instance == null) {
            instance = new MultipleDerivationSummarizedAnswerTreeBuilder();
        }
        return instance;
    }

    @Override
    protected String getNodeValue(WordMappings wordReplacementMap, ParseTreeNode node) {
        Set<String> mappings = getSetOfMappings(wordReplacementMap, node.wordOrder);
        Iterator<String> mappingsIterator = mappings.iterator();
        if (mappings.size() == 1) {
            return StringUtil.getQuoatedString(mappingsIterator.next());
        } else if (mappings.size() == 2) {
            String first = StringUtil.getQuoatedString(mappingsIterator.next());
            String second = StringUtil.getQuoatedString(mappingsIterator.next());
            return String.format("%s and %s", first, second);
        } else if (mappings.size() > 2) {
            if (allNumeric(mappingsIterator)) {
                return handleNumericValues(mappings);
            } else {
                return handleStringValues(node, mappings);
            }
        }
        return null;
    }

    private boolean allNumeric(Iterator<String> mappingsIterator) {
        while (mappingsIterator.hasNext()) {
            if (!NumberUtils.isNumber(mappingsIterator.next())) {
                return false;
            }
        }
        return true;
    }

    private String handleNumericValues(Set<String> mappings) {
        String min = null;
        double minValue = Double.MAX_VALUE;
        String max = null;
        double maxValue = Double.MIN_VALUE;
        for (String mapping : mappings) {
            Double aDouble = Double.valueOf(mapping);
            if (aDouble < minValue) {
                minValue = aDouble;
                min = mapping;
            }
            if (aDouble > maxValue) {
                maxValue = aDouble;
                max = mapping;
            }
        }

        return String.format("%s - %s", min, max);
    }

    private String handleStringValues(ParseTreeNode node, Set<String> mappings) {
        Iterator<String> mappingsIterator = mappings.iterator();
        String first = StringUtil.getQuoatedString(mappingsIterator.next());
        String second = StringUtil.getQuoatedString(mappingsIterator.next());
        String firstAndSecond = String.format("%s and %s", first, second);
        return String.valueOf(mappings.size()) + " " + node.label + " such as " + firstAndSecond;
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
