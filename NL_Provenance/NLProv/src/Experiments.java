import Top1.DerivationTree2;
import TopKBasics.KeyMap2;
import dataStructure.Block;
import dataStructure.Query;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.deri.iris.evaluation.stratifiedbottomup.seminaive.SemiNaiveEvaluator;
import org.deri.iris.facts.Facts;
import org.deri.iris.facts.IFacts;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.optimisations.rulefilter.RuleFilter;
import org.deri.iris.rules.compiler.CompiledRule;
import org.deri.iris.rules.compiler.ICompiledRule;
import org.deri.iris.rules.compiler.RuleCompiler;
import org.deri.iris.storage.IRelation;
import org.w3c.dom.Document;
import rdbms.RDBMS;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        String query4 = "return me the authors from \"Tel Aviv University\" who published papers in VLDB. ";
        String query5 = "return the papers whose title contains \"OASSIS\"";
        String query6 = "return the authors who published papers in SIGMOD before 2015 and after 2005. ";
        String query7 = "return the papers which were published in conferences in database area";
        String query8 = "return the area of conferences";
        String query9 = "return the authors who published papers in database conferences. ";
        String query10 = "return the authors who published papers in database conferences after 2005. ";
        String query11 = "return the organization of authors who published papers in database conferences after 2005. ";

        Map<String, String> querySentences = new TreeMap<>();
        querySentences.put("query00_init", query1);
        querySentences.put("query01", query1);
        querySentences.put("query02", query2);
        querySentences.put("query03", query3);
        querySentences.put("query04", query4);
        querySentences.put("query05", query5);
        querySentences.put("query06", query6);
        querySentences.put("query07", query7);
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
                Block block = query.blocks.get(0);
                Map<ITuple, Collection<DerivationTree2>> tupleProvenanceTrees = measSN(block.DATALOGQuery);

                NaturalLanguageProvenanceCreator nlProvenanceCreator = new NaturalLanguageProvenanceCreator(querySentence, block, query.originalParseTree);
                for (Map.Entry<ITuple, Collection<DerivationTree2>> tupleWithProvenanceTrees : tupleProvenanceTrees.entrySet()) {
                    Collection<DerivationTree2> provenanceTrees = tupleWithProvenanceTrees.getValue();

                    long startSingleTime = System.currentTimeMillis();
                    nlProvenanceCreator.getNaturalLanguageProvenance(provenanceTrees, "single");
                    long endSingleTime = System.currentTimeMillis();
                    long startMultipleTime = System.currentTimeMillis();
                    nlProvenanceCreator.getNaturalLanguageProvenance(provenanceTrees, "multiple");
                    long endMultipleTime = System.currentTimeMillis();
                    long startSummarizedTime = System.currentTimeMillis();
                    nlProvenanceCreator.getNaturalLanguageProvenance(provenanceTrees, "summarized");
                    long endSummarizedTime = System.currentTimeMillis();

                    System.out.println(String.format("%d\t%d\t%d", endSingleTime - startSingleTime, endMultipleTime - startMultipleTime, endSummarizedTime - startSummarizedTime));
                    System.out.println();
                }
            }
        }
    }

    private static Map<IPredicate, IRelation> getFactMap() throws IOException, ParserException {
        if (factMap == null) {
            // Create a Reader on the Datalog program file.
            Stream<String> lines = Files.lines(Paths.get("NL_Provenance\\resources\\mas_db_subset_large.iris"));
            String masDbSubset = lines.map(s -> s).collect(Collectors.joining("\n"));


            // Parse the Datalog program.
            Parser parser = new Parser();
            parser.parse(masDbSubset);

            // Retrieve the facts, rules and queries from the parsed program.
            factMap = parser.getFacts();
        }
        Map<IPredicate, IRelation> ans = new HashMap<IPredicate, IRelation>();
        for (Map.Entry<IPredicate, IRelation> entry : factMap.entrySet()) {
            ans.put(entry.getKey(), entry.getValue());
        }
        return ans;
    }

    private static Map<ITuple, Collection<DerivationTree2>> measSN (String query) throws Exception {
        KeyMap2.getInstance().Reset();

        // Parse the query.
        Parser parser = new Parser();
        parser.parse(query + '.');
        List<IRule> rules = parser.getRules();

        // Create a default configuration.
        Configuration configuration = new Configuration();

        // Enable Magic Sets together with rule filtering.
        configuration.programOptmimisers.add(new RuleFilter());
        configuration.programOptmimisers.add(new MagicSets());

        // Convert the map from predicate to relation to a IFacts object.
        IFacts facts = new Facts(getFactMap(), configuration.relationFactory);

        // Evaluate all queries over the knowledge base.
        List<ICompiledRule> cr = compile(rules, facts, configuration);
        SemiNaiveEvaluator sn = new SemiNaiveEvaluator();
        sn.evaluateRules(cr, facts, configuration);

        Map<ITuple, Collection<DerivationTree2>> provenanceTrees = new HashMap<>();
        for (ICompiledRule compiledRule : cr) {
            for (Map.Entry<ITuple, Collection<DerivationTree2>> tupleWithTrees : ((CompiledRule) compiledRule).evaluatedProvenanceTrees.entrySet()) {
                ITuple tuple = tupleWithTrees.getKey();
                Collection<DerivationTree2> trees = tupleWithTrees.getValue();
                if (!provenanceTrees.containsKey(tuple)) {
                    provenanceTrees.put(tuple, new ArrayList<>());
                }
                provenanceTrees.get(tuple).addAll(trees);
            }

        }
        return provenanceTrees;
    }

    private static List<ICompiledRule> compile( List<IRule> rules, IFacts facts, Configuration mConfiguration ) throws EvaluationException
    {
        assert rules != null;
        assert facts != null;
        assert mConfiguration != null;

        List<ICompiledRule> compiledRules = new ArrayList<ICompiledRule>();

        RuleCompiler rc = new RuleCompiler( facts, mConfiguration.equivalentTermsFactory.createEquivalentTerms(), mConfiguration );

        for (IRule rule : rules) {
            compiledRules.add(rc.compile( rule ));
        }

        return compiledRules;
    }
}
