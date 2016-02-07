import Top1.DerivationTree2;
import ansgen.*;
import dataStructure.Block;
import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import factorization.Expression;
import factorization.GreedyFactorizer;
import factorization.SimpleFactorizer;
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
    private Map<String, ParseTreeNode> literalToParseTreeNode = new HashMap<>();

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
        predicateSymbolMapping.put("EQUAL", "=");

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
        ParseTree singleDerivationAnswerTree = SingleDerivationAnswerTreeBuilder.getInstance().buildParseTree(queryOriginalParseTree, wordReplacementMap);
        System.out.println(singleDerivationAnswerTree);
        System.out.println();
        System.out.println("Single Derivation Answer Sentence");
        System.out.println(SentenceBuilder.getInstance().buildSentence(singleDerivationAnswerTree));
        System.out.println();
        System.out.println("Multiple Derivation Answer Tree");
        ParseTree multipleDerivationAnswerTree = MultipleDerivationAnswerTreeBuilder.getInstance().buildParseTree(queryOriginalParseTree, wordReplacementMap);
        System.out.println(multipleDerivationAnswerTree);
        System.out.println();
        System.out.println("Multiple Derivation Answer Sentence");
        System.out.println(SentenceBuilder.getInstance().buildSentence(multipleDerivationAnswerTree));
        System.out.println();
        System.out.println("Multiple Derivation Summarized Answer Tree");
        ParseTree multipleDerivationSummarizedAnswerTree = MultipleDerivationSummarizedAnswerTreeBuilder.getInstance().buildParseTree(queryOriginalParseTree, wordReplacementMap);
        System.out.println(multipleDerivationSummarizedAnswerTree);
        System.out.println("Multiple Derivation Summarized Answer Sentence");
        System.out.println(SentenceBuilder.getInstance().buildSentence(multipleDerivationSummarizedAnswerTree));
        System.out.println();
        System.out.println("Multiple Derivation Simple Factorization");
        Expression factorizeExpression = SimpleFactorizer.getInstance().factorize(wordReplacementMap);
        System.out.println(factorizeExpression);
        System.out.println();
        System.out.println("Multiple Derivation Simple Factorized Answer Tree");
        ParseTree simpleFactorizedAnswerTree = new MultipleDerivationFactorizedAnswerTreeBuilder(new SimpleFactorizer()).buildParseTree(queryOriginalParseTree, wordReplacementMap);
        System.out.println(simpleFactorizedAnswerTree);
        System.out.println("Multiple Derivation Simple Factorized Answer Sentence");
        System.out.println(SentenceBuilder.getInstance().buildSentence(simpleFactorizedAnswerTree));
        System.out.println();
        System.out.println("Multiple Derivation Simple Factorization");
        factorizeExpression = new GreedyFactorizer(queryOriginalParseTree).factorize(wordReplacementMap);
        System.out.println(factorizeExpression);
        System.out.println("Multiple Derivation Greedy Factorized Answer Tree");
        ParseTree greedyFactorizedAnswerTree = new MultipleDerivationFactorizedAnswerTreeBuilder(new GreedyFactorizer(queryOriginalParseTree)).buildParseTree(queryOriginalParseTree, wordReplacementMap);
        System.out.println(greedyFactorizedAnswerTree);
        System.out.println("Multiple Derivation Greedy Factorized Answer Sentence");
        System.out.println(SentenceBuilder.getInstance().buildSentence(greedyFactorizedAnswerTree));

        return "";
    }
}
