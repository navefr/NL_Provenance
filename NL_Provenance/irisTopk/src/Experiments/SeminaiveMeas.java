package Experiments;

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

public class SeminaiveMeas 
{
	
	public static void main(String[] args) throws Exception 
	{
		FileWriter fw_online = new FileWriter("meas181114_top1.csv");
		CSVWriter writer = new CSVWriter(fw_online);
		String [] headLines = new String[] {"Program number", "Size of initial DB", "Duration of top1 in milliseconds", "Size of DB After Iteration"};
		writer.writeNext(headLines);
		writer.flush();
		for (int i = 1; i < 2; i++)
		{
			String result = measSN(i);
			String [] resultArr = result.split("\n");
			
			for (int j = 0; j < resultArr.length; j++) 
			{
				resultArr[j] = resultArr[j].replaceAll( "[^\\d]", "" );
			}
			
			writer.writeNext(resultArr);
			writer.flush();
		}
	}
	
	
	
	private static String measSN (int num) throws Exception 
	{
		KeyMap2.getInstance().Reset();

		// Create a Reader on the Datalog program file.
		File program = new File("..\\query7.iris");
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
		SemiNaiveEvaluator sn = new SemiNaiveEvaluator();
		sn.evaluateRules(cr, facts, configuration);
		long endTime = System.currentTimeMillis();
		double durationFullProv = (endTime - startTime);

		int endSize = 0;
		for( IPredicate predicate : facts.getPredicates() )
			endSize += facts.get( predicate ).size();

		String separator = "---------------------------------------------------------------------------------------------------------------------------------";
		String result = String.format("Program number: %d\nSize of initial DB: %d\nDuration of seminaive in milliseconds: %s\n" 
				+ "Size of DB After Iteration: %d", num, initialSize, Double.toString(durationFullProv), endSize);
		System.out.println("Last Iteration Finished at: " + dateFormat.format(Calendar.getInstance().getTime()));
		System.out.println(separator + "\n" + result + "\n" + separator);
//		Mail.SendMail(result);
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

}
