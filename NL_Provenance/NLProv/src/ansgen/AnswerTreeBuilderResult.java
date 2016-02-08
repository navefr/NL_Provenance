package ansgen;


import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;

import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: NAVE-FROST
 * Date: 08/02/16
 * Time: 20:04
 */
public class AnswerTreeBuilderResult {

    private ParseTree parseTree;
    private Map<ParseTreeNode, Collection<ParseTreeNode>> nodeMappings;

    public AnswerTreeBuilderResult(ParseTree parseTree, Map<ParseTreeNode, Collection<ParseTreeNode>> nodeMappings) {
        this.parseTree = parseTree;
        this.nodeMappings = nodeMappings;
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public Map<ParseTreeNode, Collection<ParseTreeNode>> getNodeMappings() {
        return nodeMappings;
    }
}
