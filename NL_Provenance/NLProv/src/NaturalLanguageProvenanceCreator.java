import Top1.DerivationTree2;
import dataStructure.Block;
import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
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

    }

    public String getNaturalLanguageProvenance(DerivationTree2 provenanceTree) {
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

        Map<Integer, DerivationTree2> wordIndexToProvenanceNode = new HashMap<>();
        for (String literal : literalToProvenanceNode.keySet()) {
            if (literalToParseTreeNode.containsKey(literal)) {
                ParseTreeNode parseTreeNode = literalToParseTreeNode.get(literal);
                wordIndexToProvenanceNode.put(parseTreeNode.wordOrder, literalToProvenanceNode.get(literal));
            } else {
                String literalModified = literal.replaceAll("'", "\"");
                if (literalToParseTreeNode.containsKey(literalModified)) {
                    ParseTreeNode parseTreeNode = literalToParseTreeNode.get(literalModified);
                    wordIndexToProvenanceNode.put(parseTreeNode.wordOrder, literalToProvenanceNode.get(literal));
                }
            }
        }

        Map<Integer, ITerm> wordIndexFreeQueryNodeValue = new HashMap<>();
        for (ParseTreeNode freeQueryNode : freeQueryNodesToValues.keySet()) {
            wordIndexFreeQueryNodeValue.put(freeQueryNode.wordOrder, freeQueryNodesToValues.get(freeQueryNode));
        }

        StringBuilder nlProvenance = new StringBuilder();


        ArrayList<ParseTreeNode> orderedOriginalTreeNodes = new ArrayList<>();
        for (ParseTreeNode originalParseTreeNode : queryOriginalParseTree.allNodes) {
            orderedOriginalTreeNodes.add(originalParseTreeNode);
        }
        Collections.sort(orderedOriginalTreeNodes, new Comparator<ParseTreeNode>(){
            public int compare(ParseTreeNode o1, ParseTreeNode o2){
                return o1.wordOrder - o2.wordOrder;
            }
        });

        for (ParseTreeNode originalParseTreeNode : orderedOriginalTreeNodes) {
            int wordOrder = originalParseTreeNode.wordOrder;

            // Skip the "return me the" prefix
            if (wordOrder > 2) {
                if (wordOrder != 3) {
                    nlProvenance.append(" ");
                }

                if (wordIndexToProvenanceNode.containsKey(wordOrder )) {
                    nlProvenance.append(wordIndexToProvenanceNode.get(wordOrder).getDerivedFact().get(0).toString());
                } else if (wordIndexFreeQueryNodeValue.containsKey(wordOrder)){
                    nlProvenance.append(wordIndexFreeQueryNodeValue.get(wordOrder).getValue());
                } else {
                    nlProvenance.append(originalParseTreeNode.label);
                }
            }
        }

        return nlProvenance.toString();
    }

    private void extractTreeNodes(DerivationTree2 node, Set<DerivationTree2> nodes) {
        nodes.add(node);

        if (node.getChildren() != null) {
            for (DerivationTree2 children : node.getChildren()) {
                extractTreeNodes(children, nodes);
            }

        }
    }

    public String getNaturalLanguageProvenance2(DerivationTree2 provenanceTree) {
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

        Map<Integer, String> wordReplacementMap  = new HashMap<>();
        for (String literal : literalToProvenanceNode.keySet()) {
            if (literalToParseTreeNode.containsKey(literal)) {
                ParseTreeNode parseTreeNode = literalToParseTreeNode.get(literal);
                wordReplacementMap.put(parseTreeNode.wordOrder, literalToProvenanceNode.get(literal).getDerivedFact().get(0).toString());
            } else {
                String literalModified = literal.replaceAll("'", "\"");
                if (literalToParseTreeNode.containsKey(literalModified)) {
                    ParseTreeNode parseTreeNode = literalToParseTreeNode.get(literalModified);
                    wordReplacementMap.put(parseTreeNode.wordOrder, literalToProvenanceNode.get(literal).getDerivedFact().get(0).toString());
                }
            }
        }

        for (ParseTreeNode freeQueryNode : freeQueryNodesToValues.keySet()) {
            wordReplacementMap.put(freeQueryNode.wordOrder, freeQueryNodesToValues.get(freeQueryNode).getValue().toString());
        }

        // TODO nave - Remove
        System.out.println();
        System.out.println("Substitute Tree");
        System.out.println(SentenceBuilder.getInstance().buildSubstituteTree(queryOriginalParseTree, wordReplacementMap));
        System.out.println();
        System.out.println("Append Tree");
        System.out.println(SentenceBuilder.getInstance().buildAppendTree(queryOriginalParseTree, wordReplacementMap));
        System.out.println();
        System.out.println("Answer Tree");
        ParseTree answerTree = AnswerParseTreeBuilder.getInstance().buildAnswerParseTree(queryOriginalParseTree, wordReplacementMap);
        System.out.println(answerTree);
        System.out.println();
        System.out.println("Answer Sentence");
        System.out.println(SentenceBuilder.getInstance().buildSentence(answerTree));
        System.out.println();

        return SentenceBuilder.getInstance().buildSentence(queryOriginalParseTree, wordReplacementMap);
    }
}
