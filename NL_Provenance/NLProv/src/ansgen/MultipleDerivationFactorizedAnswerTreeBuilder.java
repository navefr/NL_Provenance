package ansgen;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import factorization.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by nfrost on 1/25/2016
 */
public class MultipleDerivationFactorizedAnswerTreeBuilder extends AbstractAnswerParseTreeBuilder {

    private Factorizer factorizer;

    public MultipleDerivationFactorizedAnswerTreeBuilder(Factorizer factorizer) {
        this.factorizer = factorizer;
    }

    @Override
    public AnswerTreeBuilderResult buildParseTree(ParseTree parseTree, WordMappings wordReplacementMap) {
        Expression factorizeExpression = factorizer.factorize(wordReplacementMap);

        ParseTree answerParseTree = super.buildParseTree(parseTree, wordReplacementMap).getParseTree();

        FactorizedAnswerTreeBuilder factorizedAnswerTreeBuilder = new FactorizedAnswerTreeBuilder(answerParseTree);
        ParseTree factorizedAnswerParseTree = factorizedAnswerTreeBuilder.handleExpression(factorizeExpression);
        return new AnswerTreeBuilderResult(factorizedAnswerParseTree, factorizedAnswerTreeBuilder.getNodeMapping());
    }


    @Override
    protected String getNodeValue(WordMappings wordReplacementMap, ParseTreeNode node) {
        if (wordReplacementMap.contains(0, node.wordOrder)) {
            return wordReplacementMap.get(0, node.wordOrder);
        } else {
            return null;
        }
    }
}
