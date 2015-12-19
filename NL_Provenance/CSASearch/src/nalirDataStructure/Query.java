package nalirDataStructure;

import java.util.ArrayList;

import nalirDBMS.SchemaGraph;

public class Query 
{
	public SchemaGraph graph; 
	
	public static int queryID = 0; 
	public Sentence sentence;

	public ArrayList<String []> treeTable = new ArrayList<String []>(); // the dependency tree table: Position, Phrase, Tag, Parent, all strings; each phrase is an entry
    public ArrayList<String> conjTable = new ArrayList<String>(); // conjunction table: a^b

	public ParseTree parseTree;
	
	public ArrayList<EntityPair> entities = new ArrayList<EntityPair>(); 

	public ParseTree adjustedParseTree; 
	public ArrayList<NLSentence> NLSentences = new ArrayList<NLSentence>(); 
	
	public int queryTreeID = 0; 
	public ParseTree queryTree = new ParseTree(); 

	public int selectedTemplate = 0; 
	public ArrayList<Integer> mappedTemplates = new ArrayList<Integer>(); 
	public ArrayList<String> translatedSQL = new ArrayList<String>();  
	public ArrayList<String> NLback = new ArrayList<String>(); 
	
	public ArrayList<ArrayList<String>> finalResult = new ArrayList<ArrayList<String>>(); 
	
	public Query(String queryInput, SchemaGraph graph)
	{
		sentence = new Sentence(queryInput); // Step1. create an object sentence for a nlq; 
		this.graph = graph; 
	}
	
	public void instantiate(SQLTemplates templates)
	{
		translatedSQL.clear(); 
		NLback.clear();

		for(int tid = 0; tid < mappedTemplates.size(); tid++)
		{
			for(int i = 0; i < templates.templates.size(); i++)
			{
				if(templates.templates.get(i).templateID == mappedTemplates.get(tid))
				{
					SQLTemplate template = templates.templates.get(i); 
					if(!SQLTemplate.getNL(template, adjustedParseTree).isEmpty())
					{
						translatedSQL.add(SQLTemplate.getSQL(template, adjustedParseTree)); 
						NLback.add(SQLTemplate.getNL(template, adjustedParseTree)); 
					}
					else
					{
						mappedTemplates.remove(tid); 
						tid--; 
					}
					break; 
				}
			}
		}
		
		for(int i = 0; i < mappedTemplates.size(); i++)
		{
			if(NLback.get(i).contains("#") || translatedSQL.get(i).contains("#"))
			{
				mappedTemplates.remove(i); 
				NLback.remove(i); 
				translatedSQL.remove(i); 
				i--; 
			}
		}
	}
}
