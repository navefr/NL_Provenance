import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by nfrost on 1/25/2016
 */
public class MultipleDerivationFactorizedAnswerTreeBuilder extends AbstractAnswerParseTreeBuilder {


    private static MultipleDerivationFactorizedAnswerTreeBuilder instance = null;
    // todo nave - not a singleton
    private int index;


    public static MultipleDerivationFactorizedAnswerTreeBuilder getInstance(){
        if (instance == null) {
            instance = new MultipleDerivationFactorizedAnswerTreeBuilder();
        }
        return instance;
    }

    @Override
    public ParseTree buildParseTree(ParseTree parseTree, WordMappings wordReplacementMap) {
        WordMappings.Expression factorizeExpression = wordReplacementMap.createFactorizeExpression();
        Collection<ParseTree> answerParseTrees = new ArrayList<>();
        for (index = 0; index < wordReplacementMap.getLastDerivation() + 1; index++) {
            answerParseTrees.add(super.buildParseTree(parseTree, wordReplacementMap));
        }

        for (WordMappings.Variable variable : factorizeExpression.getVariables()) {

        }

        return new ParseTree();
    }

    @Override
    protected String getNodeValue(WordMappings wordReplacementMap, ParseTreeNode node) {
        if (wordReplacementMap.contains(index, node.wordOrder)) {
            return wordReplacementMap.get(index, node.wordOrder);
        } else {
            return null;
        }
    }

}
