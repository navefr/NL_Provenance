package architecture;

import dataStructure.Block;
import dataStructure.ParseTreeNode;
import dataStructure.Query;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import org.w3c.dom.Document;
import rdbms.RDBMS;
import tools.PrintForCheck;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class Experiments 
{
	LexicalizedParser lexiParser; 
	RDBMS db; 
	Document tokens; 

	public static void main(String [] args) throws Exception
	{
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306/";
		String user = "root";
		String password = "admin";
		Class.forName(driver);
//		Connection conn = DriverManager.getConnection(db_url, user, password);
//
//		Statement statement = conn.createStatement();
//		ResultSet rs = statement.executeQuery("SELECT content FROM mas.history");
		ArrayList<String> history = new ArrayList<String>();
//		while(rs.next())
//		{
//			history.add(rs.getString(1));
//		}
		
		Experiments experiment = new Experiments(); 
		experiment.runExperiments(history); 
	}
	
	public Experiments() throws Exception
	{
		lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz"); 

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
    	DocumentBuilder builder = factory.newDocumentBuilder();
		tokens = builder.parse(new File("NaLIRWeb/src/zfiles/tokens.xml"));
		db = new RDBMS("mas");
	}
	
	public void runExperiments(ArrayList<String> history) throws Exception
	{
		Query query1 = new Query("return me the homepage of SIGMOD. ", db.schemaGraph);
        Query query2 = new Query("return me the conferences in database area. ", db.schemaGraph);
		Query query3 = new Query("return me the authors who published papers in SIGMOD after 2005. ", db.schemaGraph);
		Query query4 = new Query("return me the authors from \"Tel Aviv University\" who published papers in VLDB. ", db.schemaGraph);

        Query query5 = new Query("return me the publications that contains the word \"SQL\" in their title.", db.schemaGraph);
        Query query6 = new Query("return me the number of papers by \"H. V. Jagadish\" in SIGMOD. ", db.schemaGraph);
        Query query7 = new Query("return me the authors who have more publications than \"H. V. Jagadish\" in SIGMOD after 2000. ", db.schemaGraph);
        Query query8 = new Query("return me the conferences in database area, whose papers have more than 50000 total citations. ", db.schemaGraph);

        Query query = query1;

//		Query query = new Query("return me all the papers in SIGMOD after 2005. ", db.schemaGraph);
//      Query query = new Query("return me the authors who have more papers than Bob in VLDB after 2000.", db.schemaGraph);

//		Query query = new Query("return me the number of papers by \"H. V. Jagadish\" in SIGMOD in each year. ", db.schemaGraph);
//		Query query = new Query("return me the average citations of the papers in SIGMOD. ", db.schemaGraph);
//		Query query = new Query("return me the publication in SIGMOD with the most citations. ", db.schemaGraph);
//		Query query = new Query("return me the total citations of all the papers by \"H. V. Jagadish\". ", db.schemaGraph);
//		Query query = new Query("return me the authors who have more than 20 papers on SIGMOD. ", db.schemaGraph);
//		Query query = new Query("return me the average number of papers accepted in SIGMOD in each year. ", db.schemaGraph);
		
		components.StanfordNLParser.parse(query, lexiParser); 
		components.NodeMapper.phraseProcess(query, db, tokens); 
//		query.parseTree.searchNodeByID(13).choice = -1; 
		
		components.EntityResolution.entityResolute(query); 
		components.TreeStructureAdjustor.treeStructureAdjust(query, db); 
		
		components.Explainer.explain(query); 
		components.SQLTranslator.translate(query, db); 

//		for(int i = 0; i < query.adjustedTrees.size(); i++)
//		{
//			query.NLSentences.get(i).printForCheck(); 
//			System.out.println(query.adjustedTrees.get(i).toString()); 
//		}
		
		if(query.queryTree.allNodes.size() < 2)
		{
			return; 
		}
		
		query.NLSentences.get(query.queryTreeID).printForCheck(); 
		System.out.println(query.queryTree.toString()); 
		PrintForCheck.allParseTreeNodePrintForCheck(query.queryTree); 

		System.out.println(); 
		for(int i = 0; i < query.blocks.size(); i++)
		{
			query.blocks.get(i).printForCheck(); 
		}

        // TODO Nave - remove
        if (query.blocks.size() > 0) {
            String sql = query.blocks.get(0).SQL;
            for (ArrayList<String> result : db.conductSQL(sql)) {
                System.out.println(result);
            }

//            PrintWriter writer = new PrintWriter("query.iris", "UTF-8");

//            DirectedGraph graph = new DirectedAcyclicGraph<>();
//            TopologicalOrderIterator topologicalOrderIterator = new TopologicalOrderIterator<>();
//            Map<String, Collection<String>> joinValues = new HashMap<>();
//            for (Map.Entry<String, Pair<String, Map<String, String>>> elementQuery : query.blocks.get(0).DATALOGRuleSQL.entrySet()) {
//                String elementName = elementQuery.getKey();
//                String elementSQL = elementQuery.getValue().getLeft();
//                Map<String, String> dependencies = elementQuery.getValue().getRight();
//                for (ArrayList<String> result : db.conductSQL(elementSQL)) {
//                    String rule = elementName + "(";
//                    boolean isFirst = true;
//                    for (String s : result) {
//                        if (isFirst) {
//                            isFirst = false;
//                        } else {
//                            rule += ", ";
//                        }
//                        boolean isNumber = s.matches("^-?\\d+$");
//                        if (!isNumber) {
//                            rule += "'";
//                        }
//                        rule += s;
//                        if (!isNumber) {
//                            rule += "'";
//                        }
//                    }
//                    rule += ").";

//                    writer.println(rule);
//                }
//            }
//            writer.println(query.blocks.get(0).DATALOGQuery + ".");
//            writer.close();
            Block block = query.blocks.get(0);
            System.out.println();
            System.out.println("______________________________");
            System.out.println("Original Parse Tree");
            System.out.println(query.originalParseTree);
            System.out.println();
            System.out.println("Query Tree");
            System.out.println(query.queryTree);
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

        }
	}
}
