import Top1.DerivationTree2;
import TopKBasics.KeyMap2;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dataStructure.Block;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NLProvServer {

    public static void main(String[] args) throws Exception {

        LexicalizedParser lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        RDBMS db = new RDBMS("mas");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document tokens = builder.parse(new File("NL_Provenance/NaLIRWeb/src/zfiles/tokens.xml"));

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MainHandler());
        server.createContext("/answer", new AnswerHandler(lexiParser, db, tokens));
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<body>\n" +
                    "\n" +
                    "<textarea id=\"question\" rows=\"4\" cols=\"50\">\n" +
                    "Enter your question" +
                    "</textarea>" +
                    "<input type=\"submit\" value=\"Submit\" onclick=\"location.href='/answer/$('question'.value)'\">\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>\n";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }


    static class AnswerHandler implements HttpHandler {
        private LexicalizedParser lexiParser;
        private RDBMS db;
        private Document tokens;

        public AnswerHandler(LexicalizedParser lexiParser, RDBMS db, Document tokens) {
            this.lexiParser = lexiParser;
            this.db = db;
            this.tokens = tokens;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            Collection<String> values = null;
            try {
                 values = handleQuery(query);
            } catch (Exception ignored) {}
            String response = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<body>\n" +
                    "\n" +
                    "<p>" + query + "</p>\n";
            if (values != null) {
                response += "<table style=\"width:100%\">\n";
                for (String value : values) {
                    response += "  <tr>\n" +
                                "    <td>" + value + "</td>\n" +
                                "  </tr>\n";
                }
                response += "</table>\n";
            }
            response += "\n" +
                    "</body>\n" +
                    "</html>\n";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private Collection<String> handleQuery(String querySentence) throws Exception {
            Query query = new Query(querySentence, db.schemaGraph);

            components.StanfordNLParser.parse(query, lexiParser);
            components.NodeMapper.phraseProcess(query, db, tokens);
            components.EntityResolution.entityResolute(query);
            components.TreeStructureAdjustor.treeStructureAdjust(query, db);
            components.Explainer.explain(query);
            System.out.println(query.originalParseTree);
            components.SQLTranslator.translate(query, db);

            if (query.blocks.size() == 1) {
                Block block = query.blocks.get(0);
                Map<ITuple, Collection<DerivationTree2>> tupleProvenanceTrees = measSN(block.DATALOGQuery);

                Collection<String> ans = new ArrayList<>();
                for (Map.Entry<ITuple, Collection<DerivationTree2>> tupleWithProvenanceTrees : tupleProvenanceTrees.entrySet()) {
                    ans.add(tupleWithProvenanceTrees.getKey().get(0).getValue().toString());
                }

                return ans;
            } else {
                return null;
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
}