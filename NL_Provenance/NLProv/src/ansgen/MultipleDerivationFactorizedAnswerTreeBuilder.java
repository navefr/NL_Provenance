package ansgen;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import factorization.Expression;
import factorization.Factorizer;
import factorization.WordMappings;
import utils.StringUtil;

import java.util.Collection;
import java.util.Map;

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

        AnswerTreeBuilderResult answerTreeBuilderResult = super.buildParseTree(parseTree, wordReplacementMap);
        ParseTree answerParseTree = answerTreeBuilderResult.getParseTree();
        Map<ParseTreeNode, Collection<ParseTreeNode>> nodeMappings = answerTreeBuilderResult.getNodeMappings();

        FactorizedAnswerTreeBuilder factorizedAnswerTreeBuilder = new FactorizedAnswerTreeBuilder(answerParseTree, nodeMappings);
        ParseTree factorizedAnswerParseTree = factorizedAnswerTreeBuilder.handleExpression(factorizeExpression);
        return new AnswerTreeBuilderResult(factorizedAnswerParseTree, factorizedAnswerTreeBuilder.getNodeMapping());
    }


    @Override
    protected String getNodeValue(WordMappings wordReplacementMap, ParseTreeNode node) {
        if (wordReplacementMap.contains(0, node.wordOrder)) {
            return StringUtil.getQuoatedString(wordReplacementMap.get(0, node.wordOrder));
        } else {
            return null;
        }
    }
}
