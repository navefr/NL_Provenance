package factorization;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import treedist.*;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by nfrost on 2/7/2016
 */
public class ParseTreeScorer {

    public static double score(ParseTree queryTree, ParseTree answerTree) {
        queryTree = copyTree(queryTree);
        answerTree = copyTree(answerTree);

        deleteUseless(queryTree);
//        deleteUseless(answerTree);


        ComparisonZhangShasha treeCorrector = new ComparisonZhangShasha();
        OpsZhangShasha costs = new OpsZhangShasha();

        Transformation transform = treeCorrector.findDistance(convertTreeDS(queryTree), convertTreeDS(answerTree), costs);

        return transform.getCost();
    }

    private static BasicTree convertTreeDS(ParseTree parseTree) {
        Hashtable<String, ArrayList<String>> tree = new Hashtable<String, ArrayList<String>>();
        for (ParseTreeNode node : parseTree.allNodes) {
            ArrayList<String> nodeChildren = new ArrayList<String>();
            for (ParseTreeNode child : node.children) {
                nodeChildren.add(getNodeIdentifier(child));
            }
            tree.put(getNodeIdentifier(node), nodeChildren);
        }
        return new BasicTree(tree, getNodeIdentifier(parseTree.root), BasicTree.POSTORDER);
    }

    // TODO nave - won't work - since query and ans has diff wordOrder
    private static String getNodeIdentifier(ParseTreeNode node) {
        return node.label + node.wordOrder;
    }

    // TODO nave - reuse
    private static ParseTree copyTree(ParseTree parseTree) {
        ParseTree copyTree = new ParseTree();
        for (ParseTreeNode node : parseTree.allNodes) {
            if (node.parent != null) {
                ParseTreeNode newNode = copyTree.buildNode(new String[]{String.valueOf(node.wordOrder), node.label, node.pos, String.valueOf(node.parent.wordOrder), node.relationship});
                newNode.tokenType = node.tokenType;
            }
        }
        return copyTree;
    }

    private static void deleteUseless(ParseTree parseTree) {
        for(int i = 0; i < parseTree.allNodes.size(); i++) {
            if(parseTree.allNodes.get(i).tokenType.equals("NA") || parseTree.allNodes.get(i).tokenType.equals("QT")) {
                ParseTreeNode curNode = parseTree.allNodes.get(i);
                if(curNode.label.equals("on") || curNode.label.equals("in") || curNode.label.equals("of") || curNode.label.equals("by")) {
                    if(!curNode.children.isEmpty()) {
                        curNode.children.get(0).prep = curNode.label;
                    }
                }

                if(curNode.tokenType.equals("QT")) {
                    curNode.parent.QT = curNode.function;
                }

                parseTree.deleteNode(curNode);
                i--;
            }
        }
    }
}
