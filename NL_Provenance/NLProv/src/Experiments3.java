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
import java.util.stream.Stream;

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
        String query9 = "return the authors who published papers in database conferences. ";
        String query11 = "return the organization of authors who published papers in database conferences after 2005.";

        Map<String, String> querySentences = new TreeMap<>();
        querySentences.put("query00_init", query1);
        querySentences.put("query03", query3);
        querySentences.put("query09", query9);
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

                long startTime = System.currentTimeMillis();
                for (Map.Entry<ITuple, WordMappings> resultsAndWordMappingsEntry : resultsAndWordMappings.entrySet()) {
                    WordMappings wordMappings = resultsAndWordMappingsEntry.getValue();
                    new MultipleDerivationFactorizedAnswerTreeBuilder(new QueryBasedFactorizer(query.originalParseTree)).buildParseTree(query.originalParseTree, wordMappings).getFactorizationTime();
                }
                long endTime = System.currentTimeMillis();
                System.out.println(endTime - startTime);
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
            String fileName = "";
            switch (queryName) {
                case "query03":
                    fileName = "q3.txt";
                    break;
                case "query09":
                    fileName = "q9.txt";
                    break;
                case "query11":
                    fileName = "q11.txt";
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
                        case "query08":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 5, "conference" + j);
                            break;
                        case "query09":
                            wordMappings.add(j, 3, "ans" + i);
                            wordMappings.add(j, 6, "paper" + j);
                            wordMappings.add(j, 9, "conference" + j);
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
                i++;
            }
        }
        return result;
    }
}