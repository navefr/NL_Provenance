package system;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nalirComponents.EntityResolution;
import nalirComponents.NodeMapper;
import nalirComponents.StanfordNLParser;
import nalirComponents.TreeStructureAdjustor;
import nalirDBMS.RDBMS;
import nalirDataStructure.MappedSchemaElement;
import nalirDataStructure.ParseTreeNode;
import nalirDataStructure.Query;
import nalirTools.PrintForCheck;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.umich.tbnalir.endpoint.TemplateMapper;
import org.w3c.dom.Document;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class NaLIRSystem 
{
	static int TOPJ = 10; 
	static int TOPK = 10; 

	public Statement statement; 
	LexicalizedParser lexiParser; 
	RDBMS db; 
	Document tokens; 
	TemplateMapper mapper; 
	
	Query nalirQuery; 
	
	public NaLIRSystem() throws Exception
	{
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306/";
		String user = "root";
		String password = "";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(db_url, user, password);		
		this.statement = conn.createStatement();
		statement.execute("use dblp_plus; "); 
		
		lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz"); 

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
    	DocumentBuilder builder = factory.newDocumentBuilder();
		tokens = builder.parse(new File("/Users/lifei/Dropbox/workspace/csasearch/src/zfiles/tokens.xml")); 
		mapper = new TemplateMapper(new File("/Users/lifei/Dropbox/workspace/csasearch/src/zfiles/rawTemplates")); 
	}
	
	public static void main(String [] args) throws Exception
	{
		NaLIRSystem system = new NaLIRSystem(); 
		String command = "##2_query## show me the papers on VLDB."; 
		system.conductCommand(command); 
	}

	@SuppressWarnings("unchecked")
	public String conductCommand(String command) throws Exception
	{
		JSONObject html = new JSONObject(); 
		
		db = new RDBMS("dblp_plus"); 
		JSONArray historyQueries = new JSONArray(); 
		ResultSet rs = statement.executeQuery("SELECT qid, query FROM dblp_plus.history ORDER BY qid ASC; "); 
		while(rs.next())
		{
			JSONObject query = new JSONObject(); 
			query.put("qid", rs.getInt(1)); 
			query.put("query", rs.getString(2)); 
			historyQueries.add(query); 
		}
		html.put("historyQueries", historyQueries); 
		
		System.out.println(command);
		
		if(command.startsWith("##2_query##"))
		{
			if(command.split("## ").length > 1)
			{
				String queryInput = command.split("## ")[1]; 
				nalirQuery = new Query(queryInput, db.schemaGraph); // Step 1. given a natural language and a schema graph, build a nalirQuery: including translate a sentence into an object Sentence; 
				StanfordNLParser.parse(nalirQuery, lexiParser); // Step 2. given a natural language query and a schema graph, use stanford parser to parse it into a parse tree; 
				NodeMapper.phraseProcess(nalirQuery, db, tokens); // Step 3. tokenize the parse tree; 				
				EntityResolution.entityResolute(nalirQuery); 
				TreeStructureAdjustor.treeStructureAdjust(nalirQuery, db); 

				System.out.println(nalirQuery.parseTree);
				System.out.println(PrintForCheck.allParseTreeNodePrintForCheck(nalirQuery.parseTree)); 

				String json_query = PrintForCheck.jsonOutput(nalirQuery).toString(); 
				ArrayList<Integer> mappedTemplates = mapper.map(json_query); 
				
				nalirQuery.mappedTemplates.clear();
				nalirQuery.mappedTemplates.addAll(mappedTemplates); 
				nalirQuery.instantiate(db.templates);
				
				htmlGen(nalirQuery, html); 
			}
		}
		else if(command.startsWith("##3_mapSchema##"))
		{
			if(command.split(" ").length > 2)
			{
				int id = Integer.parseInt(command.split(" ")[1]); 
				int value = Integer.parseInt(command.split(" ")[2]); 
				
				nalirQuery.parseTree.searchNodeByOrder(id).choice = value; 
				EntityResolution.entityResolute(nalirQuery); 
				TreeStructureAdjustor.treeStructureAdjust(nalirQuery, db); 

				String json_query = PrintForCheck.jsonOutput(nalirQuery).toString(); 
				ArrayList<Integer> mappedTemplates = mapper.map(json_query); 
				
				nalirQuery.mappedTemplates.clear();
				nalirQuery.mappedTemplates.addAll(mappedTemplates); 
				nalirQuery.instantiate(db.templates);
				
				htmlGen(nalirQuery, html); 
			}
		}
		else if(command.startsWith("##4_mapValue##"))
		{
			if(command.split(" ").length > 2)
			{
				int id = Integer.parseInt(command.split(" ")[1]); 
				int value = Integer.parseInt(command.split(" ")[2]); 
				
				ParseTreeNode node = nalirQuery.parseTree.searchNodeByOrder(id); 
				node.mappedElements.get(node.choice).choice = value; 
				EntityResolution.entityResolute(nalirQuery); 
				TreeStructureAdjustor.treeStructureAdjust(nalirQuery, db); 

				String json_query = PrintForCheck.jsonOutput(nalirQuery).toString(); 
				ArrayList<Integer> mappedTemplates = mapper.map(json_query); 
				
				nalirQuery.mappedTemplates.clear();
				nalirQuery.mappedTemplates.addAll(mappedTemplates); 
				nalirQuery.instantiate(db.templates);
				
				htmlGen(nalirQuery, html); 
			}
		}
		else if(command.startsWith("##5_mapTemplate##"))
		{
			if(command.split(" ").length > 1)
			{
				int select = Integer.parseInt(command.split(" ")[1]); 
				nalirQuery.selectedTemplate = select; 
				htmlGen(nalirQuery, html); 
			}
		}
		else if(command.startsWith("##6_getResult##"))
		{
			try
			{
				System.out.println(nalirQuery.translatedSQL.get(nalirQuery.selectedTemplate));
				nalirQuery.finalResult = db.conductSQL(nalirQuery.translatedSQL.get(nalirQuery.selectedTemplate)); 
			}
			catch(Exception e)
			{}
			htmlGen(nalirQuery, html); 
		}
		
		return html.toJSONString(); 
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject htmlGen(Query query, JSONObject html)
	{
		if(query != null)
		{
			JSONArray inputPhrases = new JSONArray(); // 1. show the user her input phrases; 
			for(int i = 0; i < query.sentence.outputWords.length; i++)
			{
				JSONObject phrase = new JSONObject(); 
				phrase.put("order", (i+1)); 
				phrase.put("phrase", query.sentence.outputWords[i]); 
				inputPhrases.add(phrase); 
			}
			html.put("inputPhrases", inputPhrases); 
			
			ArrayList<ParseTreeNode> deletedList = query.adjustedParseTree.deletedNodes; // 2. show the user the phrases that are not directly contribute in understanding her query; 
			for(int i = 0; i < deletedList.size(); i++)
			{
				ParseTreeNode minNode = deletedList.get(i); 
				int minId = i; 
				for(int j = i+1; j < deletedList.size(); j++)
				{
					if(deletedList.get(j).wordOrder < minNode.wordOrder)
					{
						minNode = deletedList.get(j); 
						minId = j; 
					}
				}
				ParseTreeNode temp = deletedList.get(i); 
				deletedList.set(i, minNode); 
				deletedList.set(minId, temp); 
			}
			JSONArray deletedPhrases = new JSONArray(); 
			for(int i = 0; i < deletedList.size(); i++)
			{
				JSONObject deletedPhrase = new JSONObject(); 
				deletedPhrase.put("order", deletedList.get(i).wordOrder); 
				deletedPhrase.put("phrase", deletedList.get(i).label); 
				deletedPhrases.add(deletedPhrase); 
			}
			
			ArrayList<ParseTreeNode> allNodes = query.adjustedParseTree.allNodes; // 3. show the user the mapping of all ambiguous words; 
			JSONArray phrases = new JSONArray(); 
			for(int i = 0; i < allNodes.size(); i++)
			{
				ParseTreeNode NTVT = allNodes.get(i); 
				if(NTVT.mappedElements.size() > 0)
				{
					JSONObject phrase = new JSONObject(); 
					if(NTVT.tokenType.equals("VTNUM"))
					{
						phrase.put("type", "number"); 
					}
					else
					{
						phrase.put("type", "text"); 
					}
					
					phrase.put("phrase", NTVT.label); 
					phrase.put("order", NTVT.wordOrder); 
					phrase.put("choice", NTVT.choice); 
					
					JSONArray mappings = new JSONArray(); 
					phrase.put("mappings", mappings); 
					for(int j = 0; j < NTVT.mappedElements.size() && j < TOPJ; j++)
					{
						MappedSchemaElement mappedElement = NTVT.mappedElements.get(j); 
						JSONObject mapping = new JSONObject(); 
						if(mappedElement.schemaElement.type.equals("entity") || mappedElement.schemaElement.type.equals("relationship"))
						{
							mapping.put("name", mappedElement.schemaElement.name); 
						}
						else
						{
							mapping.put("name", mappedElement.schemaElement.relation.name + "." + mappedElement.schemaElement.name); 
						}
						mappings.add(mapping); 
						
						JSONArray mappingValues = new JSONArray(); 
						mapping.put("mappingValues", mappingValues); 
						if(mappedElement.mappedValues.size() > 0 && j == NTVT.choice && NTVT.tokenType.startsWith("VT")
							&& !mappedElement.schemaElement.type.equals("number") && !mappedElement.schemaElement.type.endsWith("k"))
						{
							mapping.put("choice", mappedElement.choice); 
							
							for(int k = 0; k < mappedElement.mappedValues.size() && k < TOPK; k++)
							{
								JSONObject mappingValue = new JSONObject(); 
								mappingValue.put("value", mappedElement.mappedValues.get(k)); 
								mappingValues.add(mappingValue); 
							}
						}
					}
					phrases.add(phrase); 
				}
			}
			html.put("phrases", phrases); 
			
			ArrayList<String> intepretations = query.NLback; 
			JSONArray queryIntepretations = new JSONArray(); 
			for(int i = 0; i < intepretations.size(); i++)
			{
				JSONObject queryIntepretation = new JSONObject(); 
				queryIntepretation.put("intepretation", intepretations.get(i)); 
				queryIntepretations.add(queryIntepretation); 
			}
			html.put("intepretations", intepretations); 
			html.put("intepretationSelected", query.selectedTemplate); 
			
			if(!query.finalResult.isEmpty())
			{
				JSONArray finalResults = new JSONArray(); 
				for(int i = 0; i < query.finalResult.size(); i++)
				{
					JSONArray row = new JSONArray(); 
					for(int j = 0; j < query.finalResult.get(i).size(); j++)
					{
						JSONObject item = new JSONObject(); 
						item.put("item", query.finalResult.get(i).get(j));  
						row.add(item); 
					}
					finalResults.add(row); 
				}
				html.put("finalResults", finalResults); 
			}
		}
		
		System.out.println(html);
		
		return html; 
	}	
	
	public static String loadHistory1()
	{
		String command = "##2_query## show me the published year of \"Making database systems usable\". \n"; 
		command += "##2_query## show me the conference, which published \"Making database systems usable\". \n"; 
		command += "##2_query## show me the abstract of \"Making database systems usable\". \n"; 
		command += "##2_query## show me the authors of \"Making database systems usable\". \n"; 
		command += "##2_query## show me the keywords of \"Making database systems usable\". \n"; 
		command += "##2_query## show me the citations of \"Making database systems usable\". \n"; 
		command += "##2_query## show me the citations of \"Making database systems usable\" after 2007. \n"; 
		command += "##2_query## show me the references of \"Making database systems usable\". \n"; 
		command += "##2_query## show me the number of citations of \"Making database systems usable\". \n"; 
		command += "##2_query## show me the number of citations of \"Making database systems usable\" after 2007. \n"; 
		command += "##2_query## show me the number of citations of \"Making database systems usable\" in each year. \n"; 
		command += "##2_query## show me the citations of \"making database systems usable\" that are not cited by its authors. \n"; 
		command += "##2_query## show me the number of papers in PVLDB. \n"; 
		command += "##2_query## show me the number of papers in VLDB. \n"; 
		command += "##2_query## show me the number of papers in \"University of Michigan\". \n"; 
		command += "##2_query## show me the number of papers in each year. \n"; 
		command += "##2_query## show me the number of papers by each organization. \n"; 
		command += "##2_query## show me the number of papers in each conference. \n"; 
		command += "##2_query## show me the number of papers in each journal. \n"; 
		command += "##2_query## show me the number of papers in each domain. \n"; 
		command += "##2_query## show me the number of papers in PVLDB in each year. \n"; 
		command += "##2_query## show me the number of paper in VLDB in each year. \n"; 
		command += "##2_query## show me the number of papers in PVLDB by each organization. \n"; 
		command += "##2_query## show me the number of paper in VLDB by each organization. \n"; 
		command += "##2_query## show me the number of papers by \"University of Michigan\" in each year. \n"; 
		command += "##2_query## show me the number of papers by \"University of Michigan\" in each conference. \n"; 
		command += "##2_query## show me the number of papers by \"University of Michigan\" in each journal. \n"; 
		command += "##2_query## show me the number of papers by \"University of Michigan\" in each domain. \n"; 
		command += "##2_query## show me the number of papers by each organization after 2000. \n"; 
		command += "##2_query## show me the number of papers in each conference after 2000. \n"; 
		command += "##2_query## show me the number of papers in each journal after 2000. \n"; 
		command += "##2_query## show me the number of papers in each domain after 2000. \n"; 
		command += "##2_query## show me the number of papers in PVLDB in \"University of Michigan\" in each year. \n"; 
		command += "##2_query## show me the number of papers in VLDB in \"University of Michigan\" in each year. \n"; 
		command += "##2_query## show me the number of papers after 2000 in PVLDB in each organization. \n"; 
		command += "##2_query## show me the number of papers after 2000 in VLDB in each organization. \n"; 
		command += "##2_query## show me the number of citations of each paper in VLDB. \n"; 
		command += "##2_query## show me the number of citations of each paper in PVLDB. \n"; 
		command += "##2_query## show me the number of citations of each paper by \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the paper with the most citations. \n"; 
		
		return command; 
	}
	
	public static String loadHistory2()
	{
		String command = "##2_query## show me the papers with more than 1000 citations. \n"; 
		command += "##2_query## show me the paper in PVLDB with the most citations. \n"; 
		command += "##2_query## show me the papers in PVLDB with more than 200 citations. \n"; 
		command += "##2_query## show me the paper in VLDB with the most citations. \n"; 
		command += "##2_query## show me the papers in VLDB with more than 200 citations. \n"; 
		command += "##2_query## show me the paper after 2000 with the most citations. \n"; 
		command += "##2_query## show me the papers after 2000 with more than 200 citations. \n"; 
		command += "##2_query## show me the paper after 2000 in PVLDB with the most citations. \n"; 
		command += "##2_query## show me the papers after 2000 in PVLDB with more than 200 citations. \n"; 
		command += "##2_query## show me the paper after 2000 in VLDB with the most citations. \n"; 
		command += "##2_query## show me the papers after 2000 in VLDB with more than 200 citations. \n"; 
		command += "##2_query## show me the number of papers with more than 200 citations in each organization. \n"; 
		command += "##2_query## show me the number of papers with more than 200 citations in each conference. \n"; 
		command += "##2_query## show me the number of papers with more than 200 citations in each journal. \n"; 
		command += "##2_query## show me the paper in database area by \"H. V. Jagadish\"\n"; 
		command += "##2_query## show me the paper in database area with more than 500 citations. \n"; 
		command += "##2_query## show me the paper in database area with the most citations. \n"; 
		command += "##2_query## show me the paper in database area after 2000 with the most citations. \n"; 
		command += "##2_query## show me the paper in database area after 2000 with more than 100 citations. \n"; 
		command += "##2_query## show me the number of papers in database area in each year. \n"; 
		command += "##2_query## show me the number of papers in database area in each organization. \n"; 
		command += "##2_query## show me the number of papers in database area in \"University of Michigan\". \n"; 
		command += "##2_query## show me the number of papers in database area in \"University of Michigan\" in each year. \n"; 
		command += "##2_query## show me the homepage of \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the organization where \"H. V. Jagadish\" is in. \n"; 
		command += "##2_query## show me the domain where \"H. V. Jagadish\" is in. \n"; 
		command += "##2_query## show me the conferences, which have papers by \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the journals, which have papers by \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the number of conferences, which have papers by \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the number of journals, which have papers by \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the number of researchers in each organization. \n"; 
		command += "##2_query## show me the number of researchers in each area. \n"; 
		command += "##2_query## show me the number of researchers in database area in each organization. \n"; 
		command += "##2_query## show me the number of researchers in \"University of Michigan\" in each area. \n"; 
		command += "##2_query## show me the authors who have cooperated with \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the number of authors who have cooperated with \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the authors who are in the same organization with \"H. V. Jagadish\"\n"; 
		command += "##2_query## show me the authors who have cooperated with \"H. V. Jagadish\" in more than 10 papers. \n"; 
		command += "##2_query## show me the papers by \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the papers by \"H. V. Jagadish\" after 2000. \n"; 
		
		return command; 
	}

	public static String loadHistory3()
	{
		String command = "##2_query## show me the papers by \"H. V. Jagadish\" on PVLDB. \n"; 
		command += "##2_query## show me the papers by \"H. V. Jagadish\" on PVLDB after 2000. \n"; 
		command += "##2_query## show me the papers by \"H. V. Jagadish\" on VLDB. \n"; 
		command += "##2_query## show me the papers by \"H. V. Jagadish\" on VLDB after 2000. \n"; 
		command += "##2_query## show me the number of papers by \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the number of papers by \"H. V. Jagadish\" after 2000. \n"; 
		command += "##2_query## show me the number of papers by \"H. V. Jagadish\" on PVLDB. \n"; 
		command += "##2_query## show me the number of papers by \"H. V. Jagadish\" on PVLDB after 2000. \n"; 
		command += "##2_query## show me the number of papers by \"H. V. Jagadish\" on VLDB. \n"; 
		command += "##2_query## show me the number of papers by \"H. V. Jagadish\" on VLDB after 2000. \n"; 
		command += "##2_query## show me the number of papers written by \"H. V. Jagadish\" in each year. \n"; 
		command += "##2_query## show me the number of papers written by \"H. V. Jagadish\" on PVLDB in each year. \n"; 
		command += "##2_query## show me the number of papers written by \"H. V. Jagadish\" on VLDB in each year. \n"; 
		command += "##2_query## show me the number of papers written by \"H. V. Jagadish\" in each conference. \n"; 
		command += "##2_query## show me the number of papers written by \"H. V. Jagadish\" in each journal. \n"; 
		command += "##2_query## show me the paper by \"H. V. Jagadish\" with the most citations. \n"; 
		command += "##2_query## show me the papers by \"H. V. Jagadish\" with more than 200 citations. \n"; 
		command += "##2_query## show me the authors who have more than 200 papers. \n"; 
		command += "##2_query## show me the authors who have the most papers. \n"; 
		command += "##2_query## show me the authors who have more than 10 papers in VLDB. \n"; 
		command += "##2_query## show me the author who has the most number of papers in VLDB. \n"; 
		command += "##2_query## show me the authors who have more than 10 papers in PVLDB. \n"; 
		command += "##2_query## show me the authors who have the most number of papers in PVLDB. \n"; 
		command += "##2_query## show me the authors who have more than 30 papers with more than 30 citations for each. \n"; 
		command += "##2_query## show me the authors in database area who have more than 30 papers with more than 30 citations for each. \n"; 
		command += "##2_query## show me the authors in \"University of Michigan\" who have more than 30 papers with more than 30 citations for each. \n"; 
		command += "##2_query## show me the conferences, which have more than 10 papers by \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the journals, which have more than 10 papers by \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the author whose papers have the most total citations. \n"; 
		command += "##2_query## show me the author whose papers have more than 10000 citations. \n"; 
		command += "##2_query## show me the author in the \"University of Michigan\" whose papers have the most total citations. \n"; 
		command += "##2_query## show me the author in the \"University of Michigan\" whose papers have more than 5000 total citations. \n"; 
		command += "##2_query## show me the author in database area whose papers have the most total citations. \n"; 
		command += "##2_query## show me the author in database area whose papers have more than 5000 total citations. \n"; 
		command += "##2_query## show me the author in the \"University of Michigan\" in database area whose papers have the most total citations. \n"; 
		command += "##2_query## show me the author in the \"University of Michigan\" in database area whose papers have more than 5000 total citations. \n"; 
		command += "##2_query## show me the total citations of the papers in each organization. \n"; 
		command += "##2_query## show me the total citations of the papers in each journal. \n"; 
		command += "##2_query## show me the total citations of the papers in each conference. \n"; 
		command += "##2_query## show me the total citations of the papers in each journal in database area. \n"; 

		return command; 
	}

	public static String loadHistory4()
	{
		String command = "##2_query## show me the total citations of the papers in each conference in database area. \n"; 
		command += "##2_query## show me the papers written by \"H. V. Jagadish\" and \"Divesh Srivastava\". \n"; 
		command += "##2_query## show me the papers written by \"H. V. Jagadish\" and \"Divesh Srivastava\" before 2000. \n"; 
		command += "##2_query## show me the papers written by \"H. V. Jagadish\" and \"Yunyao Li\" after 2005. \n"; 
		command += "##2_query## show me the papers written by \"H. V. Jagadish\" and \"Yunyao Li\" on PVLDB. \n"; 
		command += "##2_query## show me the papers written by \"H. V. Jagadish\" and \"Yunyao Li\" on PVLDB after 2005. \n"; 
		command += "##2_query## show me the number of papers written by \"H. V. Jagadish\" and \"Divesh Srivastava\". \n"; 
		command += "##2_query## show me the number of papers written by \"H. V. Jagadish\" and \"Divesh Srivastava\" before 2000. \n"; 
		command += "##2_query## show me the number of papers written by \"H. V. Jagadish\" and \"Yunyao Li\" after 2000. \n"; 
		command += "##2_query## show me the papers written by \"H. V. Jagadish\" and \"Divesh Srivastava\" with more than 200 citations. \n"; 
		command += "##2_query## show me the papers written by \"H. V. Jagadish\" and \"Divesh Srivastava\" with the most number of citations. \n"; 
		command += "##2_query## show me the homepage of \"University of Michigan\". \n"; 
		command += "##2_query## show me the researchers in \"University of Michigan\". \n"; 
		command += "##2_query## show me the researchers in \"University of Michigan\" in database area. \n"; 
		command += "##2_query## show me the number of researchers in \"University of Michigan\". \n"; 
		command += "##2_query## show me the number of researchers in database area in \"University of Michigan\". \n"; 
		command += "##2_query## show me the homepage of VLDB. \n"; 
		command += "##2_query## show me the area of VLDB. \n"; 
		command += "##2_query## show me the papers on VLDB. \n"; 
		command += "##2_query## show me the papers on VLDB after 2000. \n"; 
		command += "##2_query## show me the authors who have papers in VLDB. \n"; 
		command += "##2_query## show me the papers in VLDB in \"University of Michigan\". \n"; 
		command += "##2_query## show me the papers in VLDB after 2000 in \"University of Michigan\". \n"; 
		command += "##2_query## show me the homepage of PVLDB. \n"; 
		command += "##2_query## show me the area of PVLDB. \n"; 
		command += "##2_query## show me the papers on PVLDB. \n"; 
		command += "##2_query## show me the papers on PVLDB after 2000. \n"; 
		command += "##2_query## show me the papers in PVLDB in \"University of Michigan\". \n"; 
		command += "##2_query## show me the papers in PVLDB after 2000 in \"University of Michigan\". \n"; 
		command += "##2_query## show me the number of papers on PVLDB. \n"; 
		command += "##2_query## show me the authors who have papers in PVLDB. \n"; 
		command += "##2_query## show me the keywords related to \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the papers, which contain the keyword \"Natural Language\". \n"; 
		command += "##2_query## show me the papers of \"H. V. Jagadish\" containing keyword \"User Study\". \n"; 
		command += "##2_query## show me the papers in PVLDB containing keyword \"User Study\". \n"; 
		command += "##2_query## show me the papers in VLDB containing keyword \"User Study\". \n"; 
		command += "##2_query## show me the authors in database area who have more papers than \"H. V. Jagadish\". \n"; 
		command += "##2_query## show me the authors who have more papers than \"H. V. Jagadish\" in SIGMOD. \n"; 
		command += "##2_query## show me the authors in database area who have more papers than \"H. V. Jagadish\" after 2000. \n"; 
		command += "##2_query## show me the authors who have more pappers than \"H. V. Jagadish\" in SIGMOD after 2000. "; 
		
		return command; 
	}
}