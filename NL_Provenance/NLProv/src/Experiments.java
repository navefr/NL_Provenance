import ansgen.MultipleDerivationFactorizedAnswerTreeBuilder;
import ansgen.MultipleDerivationSummarizedAnswerTreeBuilder;
import ansgen.SingleDerivationAnswerTreeBuilder;
import dataStructure.Query;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import factorization.QueryBasedFactorizer;
import factorization.WordMappings;
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
 */
public class Experiments {

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
        String query4 = "return the authors from \"Tel Aviv University\" who published papers in VLDB. ";
        String query5 = "return the papers whose title contains \"OASSIS\"";
        String query6 = "return the authors who published papers in SIGMOD before 2015 and after 2005. ";
        String query7 = "return the papers which were published in conferences in database area";
        String query8 = "return the area of conferences";
        String query9 = "return the authors who published papers in database conferences. ";
        String query10 = "return the authors who published papers in database conferences after 2005. ";
        String query11 = "return the organization of authors who published papers in database conferences after 2005. ";

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
                Map<ITuple, WordMappings> resultsAndWordMappings = getResultsAndWordMappings(queryName);

                System.out.println(String.format("%20s\t%20s\t%20s\t%20s\t%20s\t%20s", "#Derivations", "#Elements", "SingleTime", "MultipleTime", "FactorizationTime", "SummarizedTime"));
                for (Map.Entry<ITuple, WordMappings> resultsAndWordMappingsEntry : resultsAndWordMappings.entrySet()) {
                    WordMappings wordMappings = resultsAndWordMappingsEntry.getValue();

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

                    System.out.println(String.format("%20d\t%20d\t%20d\t%20d\t%20d\t%20d", wordMappings.getLastDerivation() + 1, wordMappings.getWordMappingByDerivation().get(0).size(), endSingleTime - startSingleTime, multipleTime, factorizationTime, endSummarizedTime - startSummarizedTime));
                }
                System.out.println();
            }
        }
    }

    private static Map<ITuple, WordMappings> getResultsAndWordMappings(String queryName) throws Exception {
        Map<ITuple, WordMappings> result = new TreeMap<>();

        if (queryName.equals("query00_init")) {
            WordMappings wordMappings = new WordMappings();
            wordMappings.add(0, 3, "ans");
            ITuple tuple = new Tuple(Arrays.<ITerm>asList(new StringTerm("ans")));
            result.put(tuple, wordMappings);
        } else {
            for (int i = 0; i < 51; i++) {
                WordMappings wordMappings = new WordMappings();
                ITuple tuple = new Tuple(Arrays.<ITerm>asList(new StringTerm("ans" + i)));
                result.put(tuple, wordMappings);
                int derivations = i == 0 ? 1 : 100 * i;
                for (int j = 0; j < derivations; j++) {
                    switch (queryName) {
                        case "query03":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 6, "paper" + j);
                            wordMappings.add(j, 10, "year" + j);
                            break;
                        case "query04":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 8, "paper" + j);
                            break;
                        case "query06":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 6, "paper" + j);
                            wordMappings.add(j, 10, "year" + j);
                            wordMappings.add(j, 13, "year" + j);
                            break;
                        case "query07":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 8, "conference" + j);
                            break;
                        case "query08":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 5, "conference" + j);
                            break;
                        case "query09":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 6, "paper" + j);
                            wordMappings.add(j, 9, "conference" + j);
                            break;
                        case "query10":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 6, "paper" + j);
                            wordMappings.add(j, 9, "conference" + j);
                            wordMappings.add(j, 11, "year" + j);
                            break;
                        case "query11":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 5, "author" + j);
                            wordMappings.add(j, 8, "paper" + j);
                            wordMappings.add(j, 11, "conference" + j);
                            wordMappings.add(j, 13, "year" + j);
                            break;
                    }
                }
            }
        }
        return result;
    }
}
