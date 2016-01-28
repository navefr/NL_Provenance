import dataStructure.ParseTreeNode;

/**
 * Created by nfrost on 1/25/2016
 */
public class SingleDerivationAnswerTreeBuilder extends AbstractAnswerParseTreeBuilder{

    private static SingleDerivationAnswerTreeBuilder instance = null;

    public static SingleDerivationAnswerTreeBuilder getInstance(){
        if (instance == null) {
            instance = new SingleDerivationAnswerTreeBuilder();
        }
        return instance;
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