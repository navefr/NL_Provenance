import Top1.DerivationTree2;
import TopKBasics.KeyMap2;
import dataStructure.Block;
import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import dataStructure.Query;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.compiler.Parser;
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
import tools.PrintForCheck;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * User: NAVE-FROST
 * Date: 26/09/15
 * Time: 00:45
 */
public class Main {

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

        String ans1 = "\"http://www.sigmod2011.org/\" is the homepage of SIGMOD.";
        String ans2 = "VLDB is a conference in Databases area.";
        String ans3 = "\"Amr Magdy\" who published \"Exploiting Geo-tagged Tweets to Understand Localized Language Diversity.\"  in SIGMOD in 2014. ";
        String ans31 = "\"Amr Magdy\" is an author who published the paper \"Exploiting Geo-tagged Tweets to Understand Localized Language Diversity.\"  in SIGMOD in 2014 which is after 2015. ";
        String ans4 = "\"Tova Milo\" from \"Tel Aviv University\" who published \"XML Repository and Active Views Demonstration.\" in VLDB. ";
        String ans41 = "\"Tova Milo\" is an author from \"Tel Aviv University\" who published the paper \"XML Repository and Active Views Demonstration.\" in VLDB. ";

        String query100 = "return the publications that contains the word \"SQL\" in their title.";
        String query101 = "return the number of papers by \"H. V. Jagadish\" in SIGMOD. ";
        String query102 = "return the authors who have more publications than \"H. V. Jagadish\" in SIGMOD after 2000. ";
        String query103 = "return the conferences in database area, whose papers have more than 50000 total citations. ";
        String query104 = "return the author with maximum number of papers in VLDB in 2014";
        String query105 = "return the paper with minimum number of citations in SIGMOD in 2013";

        String querySentence = "Return me the papers published after 2005 by authors from \"Tel Aviv University\" whose title contains \"OASSIS\"";
        querySentence = "Return the database conferences that presented papers published after 2005 by authors from \"Tel Aviv University\"";
        querySentence = query11;

        Query query = new Query(querySentence, db.schemaGraph);

        components.StanfordNLParser.parse(query, lexiParser);
        components.NodeMapper.phraseProcess(query, db, tokens);
        components.EntityResolution.entityResolute(query);
        components.TreeStructureAdjustor.treeStructureAdjust(query, db);
        components.Explainer.explain(query);
        System.out.println(query.originalParseTree);
        components.SQLTranslator.translate(query, db);

        ParseTree queryTree = query.queryTree;
        if(queryTree.allNodes.size() < 2)
        {
            System.out.println("Query is empty");
            return;
        }

        query.NLSentences.get(query.queryTreeID).printForCheck();
        System.out.println(queryTree.toString());
        PrintForCheck.allParseTreeNodePrintForCheck(queryTree);

        System.out.println();
        for(int i = 0; i < query.blocks.size(); i++)
        {
            query.blocks.get(i).printForCheck();
        }

        // TODO Nave - remove
        if (query.blocks.size() > 0) {
//            String sql = query.blocks.get(0).SQL;
//            for (ArrayList<String> result : db.conductSQL(sql)) {
//                System.out.println(result);
//            }

            Block block = query.blocks.get(0);
            System.out.println();
            System.out.println("______________________________");
            System.out.println("Original Parse Tree");
            System.out.println(query.originalParseTree);
            System.out.println();
            System.out.println("Query Tree");
            System.out.println(queryTree);
            System.out.println();
            System.out.println("Datalog query");
            System.out.println(block.DATALOGQuery);
            System.out.println();
            System.out.println("Node to literal");
            for (Map.Entry<ParseTreeNode, String> nodeToLiteralEntry : block.nodeToLiteral.entrySet()) {
                ParseTreeNode node = nodeToLiteralEntry.getKey();
                String literal = nodeToLiteralEntry.getValue();
                System.out.println(String.format("(%d) %s -->  %s", node.nodeID, node.label, literal));
            }

            Map<ITuple, Collection<DerivationTree2>> tupleProvenanceTrees = measSN(block.DATALOGQuery);

            // ToDo Nave - return as value and remove
            System.out.println();
            System.out.println("Provenance Trees");
            for (Map.Entry<ITuple, Collection<DerivationTree2>> tupleWithProvenanceTrees : tupleProvenanceTrees.entrySet()) {
                ITuple tuple = tupleWithProvenanceTrees.getKey();
                Collection<DerivationTree2> provenanceTrees = tupleWithProvenanceTrees.getValue();
                System.out.println(tuple);
                for (DerivationTree2 provenanceTree : provenanceTrees) {
                    System.out.println(provenanceTree);
                }
            }

            System.out.println();
            NaturalLanguageProvenanceCreator nlProvenanceCreator = new NaturalLanguageProvenanceCreator(querySentence, block, query.originalParseTree);
            for (Map.Entry<ITuple, Collection<DerivationTree2>> tupleWithProvenanceTrees : tupleProvenanceTrees.entrySet()) {
                ITuple tuple = tupleWithProvenanceTrees.getKey();
                Collection<DerivationTree2> provenanceTrees = tupleWithProvenanceTrees.getValue();
                System.out.println(tuple);
                System.out.println(nlProvenanceCreator.getNaturalLanguageProvenance(provenanceTrees));
            }
        }
    }

    private static Map<ITuple, Collection<DerivationTree2>> measSN (String query) throws Exception {
        KeyMap2.getInstance().Reset();

        // Create a Reader on the Datalog program file.
        Stream<String> lines = Files.lines(Paths.get("NL_Provenance\\resources\\mas_db_subset.iris"));
        String masDbSubset = lines.map(s -> s).collect(Collectors.joining("\n"));


        // Parse the Datalog program.
        Parser parser = new Parser();
        parser.parse(masDbSubset + query + '.');

        // Retrieve the facts, rules and queries from the parsed program.
        Map<IPredicate, IRelation> factMap = parser.getFacts();
        List<IRule> rules = parser.getRules();

        // Create a default configuration.
        Configuration configuration = new Configuration();

        // Enable Magic Sets together with rule filtering.
        configuration.programOptmimisers.add(new RuleFilter());
        configuration.programOptmimisers.add(new MagicSets());

        // Convert the map from predicate to relation to a IFacts object.
        IFacts facts = new Facts(factMap, configuration.relationFactory);

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