import ansgen.MultipleDerivationFactorizedAnswerTreeBuilder;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Created by nfrost on 4/1/2016
 */
public class Experiments3 {

    private static Map<IPredicate, IRelation> factMap = null;

    public static void main(String [] args) throws Exception {
        LexicalizedParser lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        RDBMS db = new RDBMS("mas");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document tokens = builder.parse(new File("NL_Provenance/NaLIRWeb/src/zfiles/tokens.xml"));

        String query1 = "return the homepage of SIGMOD. ";
        String query3 = "return the authors who published papers in SIGMOD after 2005. ";
        String query4 = "return me the authors from \"Tel Aviv University\" who published papers in VLDB. ";
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
        querySentences.put("query03", query3);
        querySentences.put("query04", query4);
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
                Map<ITuple, WordMappings> resultsAndWordMappings = getResultsAndWordMappings(queryName);

                int multipleIterations = 3;
                long factorizationTime = 0;
                long startMultipleTime = System.currentTimeMillis();
                for (int i = 0; i < multipleIterations; i++) {
                    for (Map.Entry<ITuple, WordMappings> resultsAndWordMappingsEntry : resultsAndWordMappings.entrySet()) {
                        WordMappings wordMappings = resultsAndWordMappingsEntry.getValue();
                        factorizationTime += new MultipleDerivationFactorizedAnswerTreeBuilder(new QueryBasedFactorizer(query.originalParseTree)).buildParseTree(query.originalParseTree, wordMappings).getFactorizationTime();
                    }
                }
                long endMultipleTime = System.currentTimeMillis();
                factorizationTime = factorizationTime / multipleIterations;
                long multipleTime = (endMultipleTime - startMultipleTime) / multipleIterations;

                System.out.println();
                System.out.println("Factorization: " + factorizationTime);
                System.out.println("Multiple: " + multipleTime);
                System.out.println();
            }
        }
    }

    private static Map<ITuple, WordMappings> getResultsAndWordMappings(String queryName) throws Exception {
        Map<ITuple, WordMappings> result = new TreeMap<ITuple, WordMappings>();

        if (queryName.equals("query00_init")) {
            WordMappings wordMappings = new WordMappings();
            wordMappings.add(0, 3, "ans");
            ITuple tuple = new Tuple(Arrays.<ITerm>asList(new StringTerm("ans")));
            result.put(tuple, wordMappings);
        } else {
            String fileName = "";
            switch (queryName) {
                case "query03":
                    fileName = "q3.txt";
                    break;
                case "query04":
                    fileName = "q4.txt";
                    break;
                case "query06":
                    fileName = "q6.txt";
                    break;
                case "query07":
                    fileName = "q7.txt";
                    break;
                case "query08":
                    fileName = "q8.txt";
                    break;
                case "query09":
                    fileName = "q9.txt";
                    break;
                case "query10":
                    fileName = "q10.txt";
                    break;
                case "query11":
                    fileName = "q11.txt";
                    break;
                case "query12":
                    fileName = "q12.txt";
                    break;
                case "query13":
                    fileName = "q13.txt";
                    break;
            }
            List<Object> lines = Files.lines(Paths.get("NL_Provenance\\resources\\experiments\\" + fileName)).collect(Collectors.toList());

            int i = 0;
            for (Object line : lines) {
                Integer integer = Integer.valueOf((String) line);

                ITuple tuple = new Tuple(Arrays.<ITerm>asList(new StringTerm("ans" + i)));
                WordMappings wordMappings = new WordMappings();
                result.put(tuple, wordMappings);
                for (int j = 0; j < integer; j++) {
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
                        case "query12":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 6, "paper" + j);
                            wordMappings.add(j, 9, "year" + j);
                            wordMappings.add(j, 11, "author" + j);
                            wordMappings.add(j, 13, "org" + j);
                            break;
                        case "query13":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 5, "paper" + j);
                            wordMappings.add(j, 8, "author" + j);
                            wordMappings.add(j, 10, "org" + j);
                            break;
                    }
                }
                i++;
            }
        }
        return result;
    }
}
