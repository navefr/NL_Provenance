import Top1.DerivationTree2;
import ansgen.*;
import dataStructure.Block;
import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import factorization.Expression;
import factorization.QueryBasedFactorizer;
import factorization.WordMappings;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import rdbms.SchemaElement;

import java.util.*;

/**
 * User: NAVE-FROST
 * Date: 28/09/15
 * Time: 22:21
 */
public class NaturalLanguageProvenanceCreator {

    private String querySentence;
    private Block block;
    private ParseTree queryOriginalParseTree;


    private Collection<ParseTreeNode> freeQueryNodes = new ArrayList<>();
    private Map<String, ParseTreeNode> literalToParseTreeNode = new HashMap<String, ParseTreeNode>();

    private Map<String, String> predicateSymbolMapping = new HashMap<>();

    public NaturalLanguageProvenanceCreator(String querySentence, Block block, ParseTree queryOriginalParseTree) {
        this.querySentence = querySentence;
        this.block = block;
        this.queryOriginalParseTree = queryOriginalParseTree;

        for (Map.Entry<ParseTreeNode, String> nodeToLiteralEntry : block.nodeToLiteral.entrySet()) {
            ParseTreeNode node = nodeToLiteralEntry.getKey();
            String literal = nodeToLiteralEntry.getValue();
            literalToParseTreeNode.put(literal, node);
        }

        for (ParseTreeNode queryNode : block.allNodes) {
            if (queryNode.mappedElements.size() > 0) {
                if (!block.nodeToLiteral.containsKey(queryNode)) {
                    freeQueryNodes.add(queryNode);
                }
            }
        }

        // TODO NAVE - add more predicates
        predicateSymbolMapping.put("GREATER", ">");
        predicateSymbolMapping.put("LESS", "<");
//        predicateSymbolMapping.put("EQUAL", "=");

    }

    private void extractTreeNodes(DerivationTree2 node, Set<DerivationTree2> nodes) {
        nodes.add(node);

        if (node.getChildren() != null) {
            for (DerivationTree2 children : node.getChildren()) {
                extractTreeNodes(children, nodes);
            }

        }
    }

    public String getNaturalLanguageProvenance(Collection<DerivationTree2> provenanceTrees) {
        WordMappings wordReplacementMap = new WordMappings();

        for (DerivationTree2 provenanceTree : provenanceTrees) {
            Set<DerivationTree2> provenanceTreeNodes = new HashSet<>();
            extractTreeNodes(provenanceTree, provenanceTreeNodes);

            Map<String, DerivationTree2> literalToProvenanceNode = new HashMap<>();
            for (DerivationTree2 provenanceTreeNode : provenanceTreeNodes) {
                if (provenanceTreeNode.getLiteral() != null) {
                    // TODO NAVE - modification in literal string in order to match the same format as in literalToParseTreeNode
                    IAtom atom = provenanceTreeNode.getLiteral().getAtom();
                    ITuple tuple = atom.getTuple();
                    String predicate = atom.getPredicate().getPredicateSymbol();
                    if (tuple.size() == 2 && predicateSymbolMapping.containsKey(predicate)) {
                        literalToProvenanceNode.put(String.format("%s %s %s", tuple.get(0), predicateSymbolMapping.get(predicate), tuple.get(1)), provenanceTreeNode);
                    } else {
                        literalToProvenanceNode.put(provenanceTreeNode.getLiteral().toString(), provenanceTreeNode);
                    }
                }
            }

            Map<ParseTreeNode, ITerm> freeQueryNodesToValues = new HashMap<ParseTreeNode, ITerm>();
            for (ParseTreeNode freeQueryNode : freeQueryNodes) {
                SchemaElement schemaElement = freeQueryNode.mappedElements.get(0).schemaElement;
                String relevantVariableName = schemaElement.relation.name + schemaElement.name;

                for (DerivationTree2 provenanceTreeNode : provenanceTreeNodes) {
                    DerivationTree2.Condition condition = provenanceTreeNode.getCondition();
                    ILiteral literal = provenanceTreeNode.getLiteral();
                    if (literal != null && (condition == null || !condition.getType().equalsIgnoreCase("JOIN"))) {
                        boolean foundRelevantVariableName = false;
                        int relevantVariableIndex = -1;
                        int currentIndex = 0;
                        for (IVariable iVariable : literal.getAtom().getTuple().getAllVariables()) {
                            if (iVariable.getValue().equalsIgnoreCase(relevantVariableName)) {
                                foundRelevantVariableName = true;
                                relevantVariableIndex = currentIndex;
                            }
                            currentIndex++;
                        }
                        if (foundRelevantVariableName) {
                            ITerm freeNodeTerm = provenanceTreeNode.getDerivedFact().get(relevantVariableIndex);
                            freeQueryNodesToValues.put(freeQueryNode, freeNodeTerm);
                        }
                    }
                }
            }

            int derivationIndex = wordReplacementMap.getLastDerivation() + 1;
            for (String literal : literalToProvenanceNode.keySet()) {
                if (literalToParseTreeNode.containsKey(literal)) {
                    ParseTreeNode parseTreeNode = literalToParseTreeNode.get(literal);
                    wordReplacementMap.add(derivationIndex, parseTreeNode.wordOrder, literalToProvenanceNode.get(literal).getDerivedFact().get(0).toString());
                } else {
                    String literalModified = literal.replaceAll("'", "\"");
                    if (literalToParseTreeNode.containsKey(literalModified)) {
                        ParseTreeNode parseTreeNode = literalToParseTreeNode.get(literalModified);
                        wordReplacementMap.add(derivationIndex, parseTreeNode.wordOrder, literalToProvenanceNode.get(literal).getDerivedFact().get(0).toString());
                    }
                }
            }

            for (ParseTreeNode freeQueryNode : freeQueryNodesToValues.keySet()) {
                wordReplacementMap.add(derivationIndex, freeQueryNode.wordOrder, freeQueryNodesToValues.get(freeQueryNode).getValue().toString());
            }
        }

        // TODO nave - Remove
        System.out.println();
        System.out.println("Single Derivation Answer Tree");
        ParseTree singleDerivationAnswerTree = SingleDerivationAnswerTreeBuilder.getInstance().buildParseTree(queryOriginalParseTree, wordReplacementMap).getParseTree();
        System.out.println(singleDerivationAnswerTree);
        System.out.println();
        System.out.println("Single Derivation Answer Sentence");
        System.out.println(SentenceBuilder.getInstance().buildSentence(singleDerivationAnswerTree));
        System.out.println();
        System.out.println("Multiple Derivation Answer Tree");
        ParseTree multipleDerivationAnswerTree = MultipleDerivationAnswerTreeBuilder.getInstance().buildParseTree(queryOriginalParseTree, wordReplacementMap).getParseTree();
        System.out.println(multipleDerivationAnswerTree);
        System.out.println();
        System.out.println("Multiple Derivation Answer Sentence");
        System.out.println(SentenceBuilder.getInstance().buildSentence(multipleDerivationAnswerTree));
        System.out.println();
        System.out.println("Multiple Derivation Summarized Answer Tree");
        ParseTree multipleDerivationSummarizedAnswerTree = MultipleDerivationSummarizedAnswerTreeBuilder.getInstance().buildParseTree(queryOriginalParseTree, wordReplacementMap).getParseTree();
        System.out.println(multipleDerivationSummarizedAnswerTree);
        System.out.println("Multiple Derivation Summarized Answer Sentence");
        System.out.println(SentenceBuilder.getInstance().buildSentence(multipleDerivationSummarizedAnswerTree));
        System.out.println();
//        System.out.println("Multiple Derivation Greedy Factorization");
//        Expression factorizeExpression = new GreedyFactorizer(queryOriginalParseTree).factorize(wordReplacementMap);
//        System.out.println(factorizeExpression);
//        System.out.println("Multiple Derivation Greedy Factorized Answer Tree");
//        ParseTree greedyFactorizedAnswerTree = new MultipleDerivationFactorizedAnswerTreeBuilder(new GreedyFactorizer(queryOriginalParseTree)).buildParseTree(queryOriginalParseTree, wordReplacementMap).getParseTree();
//        System.out.println(greedyFactorizedAnswerTree);
//        System.out.println("Multiple Derivation Greedy Factorized Answer Sentence");
//        System.out.println(SentenceBuilder.getInstance().buildSentence(greedyFactorizedAnswerTree));
        System.out.println("Multiple Derivation Query Based Factorization");
        Expression queryBasedFactorizeExpression = new QueryBasedFactorizer(queryOriginalParseTree).factorize(wordReplacementMap);
        System.out.println(queryBasedFactorizeExpression);
        System.out.println("Multiple Derivation Query Based Factorized Answer Tree");
        ParseTree queryBasedFactorizedAnswerTree = new MultipleDerivationFactorizedAnswerTreeBuilder(new QueryBasedFactorizer(queryOriginalParseTree)).buildParseTree(queryOriginalParseTree, wordReplacementMap).getParseTree();
        System.out.println(queryBasedFactorizedAnswerTree);
        System.out.println("Multiple Derivation Query Based Factorized Answer Sentence");
        System.out.println(SentenceBuilder.getInstance().buildSentence(queryBasedFactorizedAnswerTree));

        return "";
    }

    public String getNaturalLanguageProvenance(Collection<DerivationTree2> provenanceTrees, String type) {
        WordMappings wordReplacementMap = new WordMappings();

        for (DerivationTree2 provenanceTree : provenanceTrees) {
            Set<DerivationTree2> provenanceTreeNodes = new HashSet<>();
            extractTreeNodes(provenanceTree, provenanceTreeNodes);

            Map<String, DerivationTree2> literalToProvenanceNode = new HashMap<>();
            for (DerivationTree2 provenanceTreeNode : provenanceTreeNodes) {
                if (provenanceTreeNode.getLiteral() != null) {
                    // TODO NAVE - modification in literal string in order to match the same format as in literalToParseTreeNode
                    IAtom atom = provenanceTreeNode.getLiteral().getAtom();
                    ITuple tuple = atom.getTuple();
                    String predicate = atom.getPredicate().getPredicateSymbol();
                    if (tuple.size() == 2 && predicateSymbolMapping.containsKey(predicate)) {
                        literalToProvenanceNode.put(String.format("%s %s %s", tuple.get(0), predicateSymbolMapping.get(predicate), tuple.get(1)), provenanceTreeNode);
                    } else {
                        literalToProvenanceNode.put(provenanceTreeNode.getLiteral().toString(), provenanceTreeNode);
                    }
                }
            }

            Map<ParseTreeNode, ITerm> freeQueryNodesToValues = new HashMap<ParseTreeNode, ITerm>();
            for (ParseTreeNode freeQueryNode : freeQueryNodes) {
                SchemaElement schemaElement = freeQueryNode.mappedElements.get(0).schemaElement;
                String relevantVariableName = schemaElement.relation.name + schemaElement.name;

                for (DerivationTree2 provenanceTreeNode : provenanceTreeNodes) {
                    DerivationTree2.Condition condition = provenanceTreeNode.getCondition();
                    ILiteral literal = provenanceTreeNode.getLiteral();
                    if (literal != null && (condition == null || !condition.getType().equalsIgnoreCase("JOIN"))) {
                        boolean foundRelevantVariableName = false;
                        int relevantVariableIndex = -1;
                        int currentIndex = 0;
                        for (IVariable iVariable : literal.getAtom().getTuple().getAllVariables()) {
                            if (iVariable.getValue().equalsIgnoreCase(relevantVariableName)) {
                                foundRelevantVariableName = true;
                                relevantVariableIndex = currentIndex;
                            }
                            currentIndex++;
                        }
                        if (foundRelevantVariableName) {
                            ITerm freeNodeTerm = provenanceTreeNode.getDerivedFact().get(relevantVariableIndex);
                            freeQueryNodesToValues.put(freeQueryNode, freeNodeTerm);
                        }
                    }
                }
            }

            int derivationIndex = wordReplacementMap.getLastDerivation() + 1;
            for (String literal : literalToProvenanceNode.keySet()) {
                if (literalToParseTreeNode.containsKey(literal)) {
                    ParseTreeNode parseTreeNode = literalToParseTreeNode.get(literal);
                    wordReplacementMap.add(derivationIndex, parseTreeNode.wordOrder, literalToProvenanceNode.get(literal).getDerivedFact().get(0).toString());
                } else {
                    String literalModified = literal.replaceAll("'", "\"");
                    if (literalToParseTreeNode.containsKey(literalModified)) {
                        ParseTreeNode parseTreeNode = literalToParseTreeNode.get(literalModified);
                        wordReplacementMap.add(derivationIndex, parseTreeNode.wordOrder, literalToProvenanceNode.get(literal).getDerivedFact().get(0).toString());
                    }
                }
            }

            for (ParseTreeNode freeQueryNode : freeQueryNodesToValues.keySet()) {
                wordReplacementMap.add(derivationIndex, freeQueryNode.wordOrder, freeQueryNodesToValues.get(freeQueryNode).getValue().toString());
            }
        }

        ParseTree answerTree;
        switch (type) {
            case "single":
                answerTree = SingleDerivationAnswerTreeBuilder.getInstance().buildParseTree(queryOriginalParseTree, wordReplacementMap).getParseTree();
                break;
            case "multiple":
                answerTree = new MultipleDerivationFactorizedAnswerTreeBuilder(new QueryBasedFactorizer(queryOriginalParseTree)).buildParseTree(queryOriginalParseTree, wordReplacementMap).getParseTree();
                break;
            default:
                answerTree = MultipleDerivationSummarizedAnswerTreeBuilder.getInstance().buildParseTree(queryOriginalParseTree, wordReplacementMap).getParseTree();
                break;
        }
        return SentenceBuilder.getInstance().buildSentenceBold(answerTree);
    }
}
