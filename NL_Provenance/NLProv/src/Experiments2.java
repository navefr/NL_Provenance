import ansgen.MultipleDerivationFactorizedAnswerTreeBuilder;
import ansgen.MultipleDerivationSummarizedAnswerTreeBuilder;
import ansgen.SingleDerivationAnswerTreeBuilder;
import dataStructure.Query;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import factorization.Expression;
import factorization.QueryBasedFactorizer;
import factorization.WordMappings;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.basics.Tuple;
import org.deri.iris.storage.IRelation;
import org.deri.iris.terms.StringTerm;
import org.w3c.dom.Document;
import rdbms.RDBMS;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by nfrost on 4/1/2016
 * Calculate runtime vs percent of shared values
 */
public class Experiments2 {

    private static Map<IPredicate, IRelation> factMap = null;

    public static void main(String [] args) throws Exception {
        LexicalizedParser lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        RDBMS db = new RDBMS("mas");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document tokens = builder.parse(new File("NL_Provenance/NaLIRWeb/src/zfiles/tokens.xml"));

        String query1 = "return the homepage of SIGMOD. ";
        String query2 = "return the conferences in database area. ";
        String query3 = "return the authors who published papers in SIGMOD after 2005. ";
        String query4 = "return me the authors from \"Tel Aviv University\" who published papers in VLDB. ";
        String query5 = "return the papers whose title contains \"OASSIS\"";
        String query6 = "return the authors who published papers in SIGMOD before 2015 and after 2005. ";
        String query7 = "return the papers which were published in conferences in database area";
        String query8 = "return the area of conferences";
        String query9 = "return the authors who published papers in database conferences. ";
        String query10 = "return the authors who published papers in database conferences after 2005. ";
        String query11 = "return the organization of authors who published papers in database conferences after 2005.";
        String query12 = "Return the conferences that presented papers published in 2005 by authors from organization";
        String query13 = "Return the years of papers published by authors from IBM";

        Map<String, String> querySentences = new TreeMap<>();
        querySentences.put("query00_init", query1);
//        querySentences.put("query01", query1);
//        querySentences.put("query02", query2);
        querySentences.put("query03", query3);
        querySentences.put("query04", query4);
//        querySentences.put("query05", query5);
        querySentences.put("query06", query6);
//        querySentences.put("query07", query7);
        querySentences.put("query08", query8);
        querySentences.put("query09", query9);
        querySentences.put("query10", query10);
        querySentences.put("query11", query11);
        querySentences.put("query12", query12);
        querySentences.put("query13", query13);

        for (Map.Entry<String, String> queryEntry : querySentences.entrySet()) {

            String queryName = queryEntry.getKey();
            String querySentence = queryEntry.getValue();

            System.out.println(queryName);

            Query query = new Query(querySentence, db.schemaGraph);

            components.StanfordNLParser.parse(query, lexiParser);
            components.NodeMapper.phraseProcess(query, db, tokens);
            components.EntityResolution.entityResolute(query);
            components.TreeStructureAdjustor.treeStructureAdjust(query, db);
            components.Explainer.explain(query);
            components.SQLTranslator.translate(query, db);

            if (query.blocks.size() == 1) {
                Map<ITuple, Pair<WordMappings, Integer>> resultsAndWordMappings = getResultsAndWordMappings(queryName);

                System.out.println(String.format("%20s\t%20s\t%20s\t%20s\t%20s\t%20s\t%20s\t%20s\t%20s", "#UniqueValues", "%SharedValues", "#Derivations", "#Elements", "SingleTime", "MultipleTime", "FactorizationTime", "SummarizedTime", "FactorizationSize"));
                for (Map.Entry<ITuple, Pair<WordMappings, Integer>> resultsAndWordMappingsEntry : resultsAndWordMappings.entrySet()) {
                    WordMappings wordMappings = resultsAndWordMappingsEntry.getValue().getLeft();
                    Integer uniqueValues = resultsAndWordMappingsEntry.getValue().getRight();

                    long startSingleTime = System.currentTimeMillis();
                    SingleDerivationAnswerTreeBuilder.getInstance().buildParseTree(query.originalParseTree, wordMappings).getParseTree();
                    long endSingleTime = System.currentTimeMillis();

                    int multipleIterations = 3;
                    long factorizationTime = 0;
                    long startMultipleTime = System.currentTimeMillis();
                    for (int i = 0; i < multipleIterations; i++) {
                        factorizationTime += new MultipleDerivationFactorizedAnswerTreeBuilder(new QueryBasedFactorizer(query.originalParseTree)).buildParseTree(query.originalParseTree, wordMappings).getFactorizationTime();
                    }
                    long endMultipleTime = System.currentTimeMillis();
                    factorizationTime = factorizationTime / multipleIterations;
                    long multipleTime = (endMultipleTime - startMultipleTime) / multipleIterations;

                    long startSummarizedTime = System.currentTimeMillis();
                    MultipleDerivationSummarizedAnswerTreeBuilder.getInstance().buildParseTree(query.originalParseTree, wordMappings).getParseTree();
                    long endSummarizedTime = System.currentTimeMillis();

                    Expression factorize = new QueryBasedFactorizer(query.originalParseTree).factorize(wordMappings);

                    int derivations = wordMappings.getLastDerivation() + 1;
                    System.out.println(String.format("%20d\t%20s\t%20d\t%20d\t%20d\t%20d\t%20d\t%20d\t%20d", uniqueValues, 100 * (derivations - uniqueValues) / derivations + "%", derivations, wordMappings.getWordMappingByDerivation().get(0).size(), endSingleTime - startSingleTime, multipleTime, factorizationTime, endSummarizedTime - startSummarizedTime, getExpressionSize(factorize)));
                }
                System.out.println();
            }
        }
    }

    private static Map<ITuple, Pair<WordMappings, Integer>> getResultsAndWordMappings(String queryName) throws Exception {
        Map<ITuple, Pair<WordMappings, Integer>> result = new TreeMap<>();

        if (queryName.equals("query00_init")) {
            WordMappings wordMappings = new WordMappings();
            wordMappings.add(0, 3, "ans");
            ITuple tuple = new Tuple(Arrays.<ITerm>asList(new StringTerm("ans")));
            result.put(tuple, new ImmutablePair<WordMappings, Integer>(wordMappings, 1));
        } else {
            for (int i = 0; i < 51; i++) {
                WordMappings wordMappings = new WordMappings();
                ITuple tuple = new Tuple(Arrays.<ITerm>asList(new StringTerm("ans" + i)));
                result.put(tuple, new ImmutablePair<WordMappings, Integer>(wordMappings, i * 100));
                for (int j = 0; j < 5000; j++) {
                    int author = (int) (Math.random() * i * 100);
                    int paper = (int) (Math.random() * i * 100);
                    int year = (int) (Math.random() * i * 100);
                    int conference = (int) (Math.random() * i * 100);
                    int organization = (int) (Math.random() * i * 100);
                    switch (queryName) {
                        case "query03":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 6, "paper" + paper);
                            wordMappings.add(j, 10, "year" + year);
                            break;
                        case "query04":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 8, "paper" + paper);
                            break;
                        case "query06":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 6, "paper" + paper);
                            wordMappings.add(j, 10, "year" + year);
                            wordMappings.add(j, 13, "year" + year);
                            break;
                        case "query07":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 8, "conference" + conference);
                            break;
                        case "query08":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 5, "conference" + conference);
                            break;
                        case "query09":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 6, "paper" + paper);
                            wordMappings.add(j, 9, "conference" + conference);
                            break;
                        case "query10":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 6, "paper" + paper);
                            wordMappings.add(j, 9, "conference" + conference);
                            wordMappings.add(j, 11, "year" + year);
                            break;
                        case "query11":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 5, "author" + author);
                            wordMappings.add(j, 8, "paper" + paper);
                            wordMappings.add(j, 11, "conference" + conference);
                            wordMappings.add(j, 13, "year" + year);
                            break;
                        case "query12":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 6, "paper" + paper);
                            wordMappings.add(j, 9, "year" + year);
                            wordMappings.add(j, 11, "author" + author);
                            wordMappings.add(j, 13, "org" + organization);
                            break;
                        case "query13":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 5, "paper" + paper);
                            wordMappings.add(j, 8, "author" + author);
                            break;
                    }
                }
            }
        }
        return result;
    }


    private static int getExpressionSize(Expression expression) {
        int size = expression.getVariables().size();
        for (Expression subExpression : expression.getExpressions()) {
            size += getExpressionSize(subExpression);
        }
        return size;

    }
}
