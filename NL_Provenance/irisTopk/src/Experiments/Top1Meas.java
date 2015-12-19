package Experiments;

import TopK.TopKFinder;
import TopKBasics.KeyMap2;
import au.com.bytecode.opencsv.CSVWriter;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.compiler.Parser;
import org.deri.iris.evaluation.stratifiedbottomup.seminaive.SemiNaiveEvaluator;
import org.deri.iris.facts.Facts;
import org.deri.iris.facts.IFacts;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.optimisations.rulefilter.RuleFilter;
import org.deri.iris.rules.compiler.ICompiledRule;
import org.deri.iris.rules.compiler.RuleCompiler;
import org.deri.iris.storage.IRelation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class Top1Meas 
{
	
	public static void main(String[] args) throws Exception 
	{
		for (int l = 0; l < 3; l++) 
		{
			String results = "";
			
			FileWriter fw_online = new FileWriter("meas_topk_number_" + l +".csv");
			CSVWriter writer = new CSVWriter(fw_online);
			String [] headLines = new String[] {"Program number", "Size of initial DB", "Duration of top1 in milliseconds", "Size of DB After Iteration", "Duration of Top 3", "Memory used"};
			writer.writeNext(headLines);
			writer.flush();
			for (int i = 1; i <= 1; i+=1)
			{
				String result = measSN(i);
				results += "\n" + result + "\n\n";
				String [] resultArr = result.split("\n");
				
				for (int j = 0; j < resultArr.length; j++) 
				{
					resultArr[j] = resultArr[j].replaceAll( "[^(\\d)+\\.(\\d)+]", "" );
				}
				
				writer.writeNext(resultArr);
				writer.flush();
			}
			
			//Mail.SendMail(results);
			writer.close();
		}
		
	}
	
	
	
	private static String measSN (int num) throws Exception 
	{
		KeyMap2.getInstance().Reset();

		// Create a Reader on the Datalog program file.
		File program = new File("..\\query1.iris");
		//File program = new File("tc_program_1100.iris");
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

		int initialSize = 0;
		for( IPredicate predicate : facts.getPredicates() )
			initialSize += facts.get( predicate ).size();

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		// Evaluate all queries over the knowledge base.
		long startTime = System.currentTimeMillis();
		List<ICompiledRule> cr = compile(rules, facts, configuration);
		SetWeights(cr);
		SemiNaiveEvaluator sn = new SemiNaiveEvaluator();
		sn.evaluateRules(cr, facts, configuration);
		long endTime = System.currentTimeMillis();
		long durationFullProv = (endTime - startTime);
		
		//KeyMap2.getInstance().Print();
		//int memSize = KeyMap2.getInstance().Size();
		//KeyMap2.getInstance().ChackAllAtomsHaveTop1();
		/*
		String [] maxtup = KeyMap2.getInstance().FindLargestTree();
		System.out.println(maxtup[0] + maxtup[1]);
		*/
		startTime = System.currentTimeMillis();
		TopKFinder topk = new TopKFinder(cr, 3);

		//topk.Topk("dealsWith_p_1", "('Paraguay', 'Kuwait')");//, "('99', '55')");//, "('abcd0', 'abcd0', 'abcd0', 'abcd0', 'abcd0')");
		//topk.Topk("q");//("TC", "('54', '99')");//, "(Nicaragua', 'Slovakia')");

		endTime = System.currentTimeMillis();
		long durationTop3 = (endTime - startTime);
		long memUsed = ( KeyMap2.getInstance().Memory() );// / 1024L;//(MemoryUtil.deepMemoryUsageOf(facts) + MemoryUtil.memoryUsageOf(KeyMap2.getInstance())  ) / 1024L;//
		
		int endSize = 0;
		for( IPredicate predicate : facts.getPredicates() )
		{
			endSize += facts.get( predicate ).size();
			//System.out.println(predicate + ": " + facts.get( predicate ));
		}
		
		String separator = "---------------------------------------------------------------------------------------------------------------------------------";
		String result = String.format("Program number: %d\nSize of initial DB: %d\nDuration of top one: %d\n" 
				+ "Size of DB After Iteration: %d\nDuration of Top three: %d\nMemory used: %d", num, initialSize, durationFullProv, endSize, durationTop3, memUsed);
		System.out.println("Last Iteration Finished at: " + dateFormat.format(Calendar.getInstance().getTime()));
		System.out.println(separator + "\n" + result + "\n" + separator);
		
		/*
		Writer writer = null;
		String [] legal = {
				"dealsWith_p_1(?a,?b) :- dealsWith_pt_2(?a,?f), dealsWith(?f,?b)",
				"dealsWith_p_1(?a,?b) :- dealsWith_pt_2(?f,?b), dealsWith(?a,?f).",
				"dealsWith_p_1(?a,?b) :- dealsWith_pt_2(?b,?a)."
				};
		
		try
		{
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("derive_root_TC_" + num + ".iris"), "utf-8"));
			//writer = new PrintWriter(new BufferedWriter(new FileWriter("derive_root_" + num + ".iris", true)));
			for( IPredicate predicate : facts.getPredicates() )
			{
				if (predicate.toString().equals("E") || predicate.toString().equals("TC")) 
				{
					for (int i = 0; i < facts.get( predicate ).size(); i++) 
					{
						ITuple tuple = facts.get( predicate ).get( i );
						writer.write(predicate.toString() + tuple + ".\n");
						//String pred = predicate.toString().substring(0, predicate.toString().length()-1) + "3";
						//writer.write(pred + tuple + ".\n");
					}
				}
			}
			
			for (String string : legal) 
			{
				writer.write(string + "\n");
			}
			
		}
		
		catch (IOException ex) 
		{
		  // report
			System.out.println("ERROR");
		} 
		finally 
		{
		   try {writer.close();} catch (Exception ex) {}
		}
		*/
		
		return result;
	}
	
	
	
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
	
	
	private static void SetWeights( List<ICompiledRule> rules )
	{
		double [] weights = {/*0.5, 0.9, 0.5, 0.9, 0.5, 0.9,*/ 0.5, 1, 0.9};
		/*
		for (int i = 0; i < weights.length; i++) 
		{
			((CompiledRule)rules.get(i)).setWeight( weights[i] );
		}
		*/
	}

}
