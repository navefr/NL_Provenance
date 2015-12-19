package application;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import Basics.Program;
import Basics.Rule;
import Pattern.IntersectWithProgramOnline;
import Pattern.Pattern;
import Pattern.PatternNode;
import Top1.DerivationTree2;
import TopK.TopKFinder;
import TopKBasics.KeyMap2;

public class SystemCommands 
{
	
	static Map<String, String []> relTocat = new HashMap<String, String []>();
	
	/*************************************************************************************************************/
	/** Title: Intersect																				
	/** Description: Intersects the given pattern and program. 			
	/*************************************************************************************************************/
	
	public static Program Intersect (Pattern pattern, Program prog)
	{
		IntersectWithProgramOnline iwp = new IntersectWithProgramOnline(prog, pattern); 
		iwp.IntersectWithTransitives();
		return iwp.getP();
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: WriteToIrisFile																				
	/** Description: Given a pattern and a program, writes the intersected program to the file specified.			
	/*************************************************************************************************************/
	
	public static Program WriteToIrisFile (Pattern pattern, Program prog, String fileName)
	{
		Program res = prog;
		if (false == pattern.getPatternVec().isEmpty())
		{
			res = Intersect(pattern, prog);
		}
		
		Writer writer = null;
		try
		{

			writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
			for( Rule rule : res.getRules() )
			{
				writer.write(rule + "\n");
			}
		}

		catch (IOException ex) 
		{
			// report
			System.out.println("SystemCommands::WriteToIrisFile:: Could not write program to iris file");
		} 
		finally 
		{
			try 
			{
				writer.close();
			} 
			catch (Exception ex) 
			{
				System.out.println("SystemCommands::WriteToIrisFile:: Could not close file");
			}
		}
		 
		return res;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: Topk																				
	/** Description: Run the top-k algorithm on the DB and program in the file. 			
	/*************************************************************************************************************/
	
	public static IFacts Topk (int k, Program prog, String fileName, PatternNode root, Map<ITuple, List<DerivationTree2>> trees) throws Exception
	{
		KeyMap2.getInstance().Reset();

		// Create a Reader on the Datalog program file.
		File program = new File(fileName);
		Reader reader = new FileReader(program);

		// Parse the Datalog program.
		Parser parser = new Parser();
		parser.parse(reader);

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
		SetWeights(cr, prog);
		SemiNaiveEvaluator sn = new SemiNaiveEvaluator();
		sn.evaluateRules(cr, facts, configuration);
		
		TopKFinder topk = new TopKFinder(cr, k);
		
		//KeyMap2.getInstance().Print();
		
		// Parse root of pattern.
		if (null != root) 
		{
			String [] predicateAndParams = root.toString().substring(0, root.toString().length() - 1).split("\\(");
			String predicate = predicateAndParams[0];
			//get top-k trees
			Map<ITuple, List<DerivationTree2>> temp = topk.Topk(predicate);
			for (ITuple key : temp.keySet()) 
			{
				trees.put(key, temp.get(key));
			}
		}
		
		return facts;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: compile																				
	/** Description: Compiles the rules as is requiered by IRIS. 			
	/*************************************************************************************************************/
	
	private static List<ICompiledRule> compile( List<IRule> rules, IFacts facts, Configuration mConfiguration ) throws EvaluationException
	{
		assert rules != null;
		assert facts != null;
		assert mConfiguration != null;
		
		List<ICompiledRule> compiledRules = new ArrayList<ICompiledRule>();
		
		RuleCompiler rc = new RuleCompiler( facts, mConfiguration.equivalentTermsFactory.createEquivalentTerms(), mConfiguration );

		for( IRule rule : rules )
			compiledRules.add( rc.compile( rule ) );
		
		return compiledRules;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: SetWeights																				
	/** Description: Sets the weights of the IRIS rules to be the weights of the intersected program. 			
	/*************************************************************************************************************/
	
	private static void SetWeights( List<ICompiledRule> rules, Program p )
	{
		for (int i = 0; i < rules.size(); i++) 
		{
			((CompiledRule)rules.get(i)).setWeight( p.getRules().get(i).getWeight() );
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: fillCategories																				
	/** Description: 		
	/*************************************************************************************************************/
	
	static void fillCategories() 
	{
		relTocat.put("isMarriedTo", new String [] {"person", "person"});
		relTocat.put("hasChild", new String [] {"person", "child"});
		relTocat.put("directed", new String [] {"director", "movie"});
		relTocat.put("created", new String [] {"director", "movie"});
		relTocat.put("produced", new String [] {"director", "movie"});
		relTocat.put("actedIn", new String [] {"director", "movie"});
		relTocat.put("livesIn", new String [] {"person", "place"});
		relTocat.put("dealsWith", new String [] {"Country", "Country"});	
		relTocat.put("hasCapital", new String [] {"Country", "city"});
		relTocat.put("isLocatedIn", new String []  {"city", "Country"});
		relTocat.put("hasOfficialLanguage", new String []  {"Country", "language"}); 
		relTocat.put("worksAt", new String []  {"person", "university"});  
		relTocat.put("hasAcademicAdvisor", new String []  {"person", "person"}); 
		relTocat.put("isCitizenOf", new String []  {"person", "Country"}); 
		relTocat.put("influences", new String []  {"person", "person"}); 
		relTocat.put("graduatedFrom", new String []  {"person", "university"}); 
		relTocat.put("wasBornIn", new String []  {"person", "city"});  
		relTocat.put("diedIn", new String []  {"person", "city"}); 
		relTocat.put("imports", new String []  {"Country", "Product"}); 
		relTocat.put("exports", new String []  {"Country", "Product"}); 
		relTocat.put("participatedIn", new String []  {"figure" , "event"}); 
		relTocat.put("hasCurrency", new String []  {"region", "currency"}); 
		relTocat.put("isPoliticianOf", new String []  {"person", "state"}); 
		relTocat.put("isLeaderOf", new String []  {"person", "place"}); 
		relTocat.put("isInterestedIn", new String []  {"person", "subject"}); 
		relTocat.put("hasWonPrize", new String []  {"person", "prize"});
		relTocat.put("isKnownFor", new String []  {"person", "prize"});
		relTocat.put("hasGeonamesId", new String []  {"Country", "id"});
		relTocat.put("hasLanguageCode", new String []  {"Language", "code"});
	}
}
