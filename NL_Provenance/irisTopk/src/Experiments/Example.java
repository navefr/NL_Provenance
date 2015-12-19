package Experiments;

import TopK.TopKFinder;
import TopKBasics.KeyMap2;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
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
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Example 
{
	public static void main(String[] args) throws Exception {
        // Create a Reader on the Datalog program file.
        File program = new File("..\\query1.iris");
        Reader reader = new FileReader(program);

        // Parse the Datalog program.
        Parser parser = new Parser();
        parser.parse(reader);

        // Retrieve the facts, rules and queries from the parsed program.
        Map<IPredicate, IRelation> factMap = parser.getFacts();
        List<IRule> rules = parser.getRules();
        List<IQuery> queries = parser.getQueries();

        // Create a default configuration.
        Configuration configuration = new Configuration();

        // Enable Magic Sets together with rule filtering.
        configuration.programOptmimisers.add(new RuleFilter());
        configuration.programOptmimisers.add(new MagicSets());

        // Convert the map from predicate to relation to a IFacts object.
        //IFacts facts = new Facts(factMap, configuration.relationFactory);

        // Create the knowledge base.
        //IKnowledgeBase knowledgeBase = new KnowledgeBase(factMap, rules, configuration);

        // Convert the map from predicate to relation to a IFacts object.
        IFacts facts = new Facts(factMap, configuration.relationFactory);
        
        // Evaluate all queries over the knowledge base.
        long startTime = System.currentTimeMillis();
        List<ICompiledRule> cr = compile(rules, facts, configuration);
        SemiNaiveEvaluator sn = new SemiNaiveEvaluator();
        sn.evaluateRules(cr, facts, configuration);
        long endTime = System.currentTimeMillis();
        double durationFullProv = (endTime - startTime);
        System.out.println("Time for seminaive: " + durationFullProv);
        TopKFinder topk = new TopKFinder(cr, 4);
        topk.TopkFromTree(KeyMap2.getInstance().Get( "dealsWith", KeyMap2.getInstance().Get("dealsWith").iterator().next() ).getTree() );
        int size = 0;
        for( IPredicate predicate : facts.getPredicates() )
			size += facts.get( predicate ).size();
        System.out.println("num of facts: " + size);
        //System.out.println(facts);
        //KeyMap.getInstance().ChackAllAtomsHaveTop1();
        /*for (IQuery query : queries) 
        {
                List<IVariable> variableBindings = new ArrayList<IVariable>();
                IRelation relation = knowledgeBase.execute(query, variableBindings);

                // Output the variables.
                System.out.println(variableBindings);

                // For performance reasons compute the relation size only once.
                int relationSize = relation.size();
                
                // Output each tuple in the relation, where the term at position i
                // corresponds to the variable at position i in the variable
                // bindings list.
                for (int i = 0; i < relationSize; i++) {
                        System.out.println(relation.get(i));
                }
        }*/
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
