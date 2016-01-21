package TopK;

import Top1.DerivationTree2;
import Top1.ModifiedDerivationTrees;
import Top1.ModifiedDerivationTrees.ModifiedTreesNode;
import TopKBasics.KeyMap2;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.rules.compiler.ICompiledRule;
import org.deri.iris.rules.compiler.RuleElement;

import java.util.*;

public class TopKFinder 
{	
	private final List<ICompiledRule> mRules;
	
	private final ModifiedDerivationTrees treeMaxHeap;
	
	private final int mK;
	
	
	public TopKFinder (List<ICompiledRule> rules, int k) 
	{
		mRules = rules;
		treeMaxHeap = new ModifiedDerivationTrees();
		mK = k;
	}
		
	
	/*************************************************************************************************************/
	/** Title: Topk																				
	/** Description: Returns the top-k trees for a relation name.			
	/*************************************************************************************************************/
	
	public Map<ITuple, List<DerivationTree2>> Topk (String patternRoot)
	{
		Collection<ITuple> roots = KeyMap2.getInstance().Get(patternRoot);
		//List<DerivationTree2> allTrees = CollectTrees(roots);
		//Collections.sort(allTrees);
		//int limit = Math.min(mK - 1, allTrees.size());
		//List<DerivationTree2> topkTrees = new ArrayList<DerivationTree2>();
		Map<ITuple, List<DerivationTree2>> topkTrees =  new HashMap<ITuple, List<DerivationTree2>>();
		
		for (ITuple root : roots) 
		{ 
			DerivationTree2 curTree = root.getTrees().iterator().next();
			List<DerivationTree2> rootTopK = TopkFromTree(curTree);
			Collections.sort(rootTopK);
			topkTrees.put( root, rootTopK );
			treeMaxHeap.clear();
		}
		
		
		/*DerivationTree2 curTree = allTrees.get(0);
		topkTrees.addAll( TopkFromTree(curTree) );
		*/
		//Collections.sort(topkTrees);
		return topkTrees;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: Topk																				
	/** Description: Returns the top-k trees for a specific fact.			
	/*************************************************************************************************************/
	
	public List<DerivationTree2> Topk (String relation, String rootTuple)
	{
		ITuple root = KeyMap2.getInstance().Get(relation, rootTuple);
		if (null != root) 
		{
			DerivationTree2 curTree = root.getTrees().iterator().next();
			treeMaxHeap.AddLeaf( new ModifiedTreesNode(root, GetBodyFromTrees(curTree), curTree.getRulePointer(), curTree.getWeight(), curTree) );
			return TopkFromTree(treeMaxHeap.PopMax().getTreeNode().getTrees().iterator().next());
		}
		
		else
		{
			System.out.println(relation + rootTuple + ": NO SUCH FACT WAS DERIVED");
			System.exit(0);
			return null;
		}
	}
	
	
	/*************************************************************************************************************/
	/** Title: CollectTrees																				
	/** Description: 				
	/*************************************************************************************************************/
	
	private List<DerivationTree2> CollectTrees (Collection<ITuple> roots)
	{
		List<DerivationTree2> trees = new ArrayList<DerivationTree2>();
		for (ITuple iTuple : roots) 
		{
			trees.add( iTuple.getTrees().iterator().next() );
		}
		
		return trees;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: TopkFromTree																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public List<DerivationTree2> TopkFromTree (DerivationTree2 top1)
	{
		List<DerivationTree2> topkTrees = new ArrayList<DerivationTree2>();
		DerivationTree2 tree = new DerivationTree2(top1);
		topkTrees.add(top1);
		
		for (int i = 0; i < mK-1; i++) 
		{
			tree = FindNextFromCurrent(tree, topkTrees);
			topkTrees.add( tree );
		}
		
		return topkTrees;
	}
	
	
	/*************************************************************************************************************/
	/** Title: FindNextFromCurrent																				
	/** Description: Gets the best tree that can be derived from curTree with one change				
	/*************************************************************************************************************/
	
	private DerivationTree2 FindNextFromCurrent (DerivationTree2 curTree, List<DerivationTree2> topkTrees)
	{
		DerivationTree2 copy = new DerivationTree2(curTree);
		Set<ITuple> nodes = CollectNodesRec(copy);
		
		for (ITuple node : nodes) 
		{
			FindDifferentTrees(node, copy);
		}
		
		//update tree
		DerivationTree2 retVal = new DerivationTree2(curTree); //there is more than one tree for fact
		
		while ( ( retVal.isPathWithDuplicateFact(new ArrayList<ITuple>()) || topkTrees.contains(retVal) ) && false == treeMaxHeap.isEmpty() )
		{
			retVal = GetBestTree(topkTrees);
		}
		
		if (retVal.isPathWithDuplicateFact(new ArrayList<ITuple>())) 
		{
			System.out.println("the system tree still contains a duplicate in a path!");
			System.out.println("tree number " + (topkTrees.size() + 1));
		}
		
		return new DerivationTree2(retVal);
	}
	
	
	/*************************************************************************************************************/
	/** Title: GetBestTree																				
	/** Description: Get the best tree from the treeMaxHeap				
	/*************************************************************************************************************/
	
	private boolean treeWithSameWeightInList (List<DerivationTree2> topKTrees, double weight)
	{
		boolean retVal = false;
		for (DerivationTree2 tree : topKTrees) 
		{
			if ( tree.getWeight() == weight )
			{
				retVal = true;
				break;
			}
		}
		
		return retVal;
	}
	
	
	/*************************************************************************************************************/
	/** Title: GetBestTree																				
	/** Description: Get the best tree from the treeMaxHeap				
	/*************************************************************************************************************/
	
	private DerivationTree2 GetBestTree (List<DerivationTree2> topKTrees)
	{
		ModifiedTreesNode best = treeMaxHeap.PopMax();
		/*while (true == treeWithSameWeightInList(topKTrees, best.getWeight())) 
		{
			 best = treeMaxHeap.PopMax();
		}*/
		
		best.getTreeNode().getTrees().iterator().next().setRulePointer( best.getRule() );
		//best.getTreeNode().getTrees().setWeight( best.getWeight() );
		best.getTreeNode().getTrees().iterator().next().setChildren( GetTreesFromBody(best.getRule(), best.getBody()) );
		best.getTree().setWeight(best.getWeight());
		
		return best.getTree();
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: FindDifferentTrees																				
	/** Description: change tree by one rule and add to treeMaxHeap				
	/*************************************************************************************************************/
	
	private void FindDifferentTrees (ITuple node, DerivationTree2 curTree)
	{
		for (ICompiledRule rule : mRules) 
		{
			String ruleHead = rule.headPredicate().getPredicateSymbol();
			String nodeRel = node.getTrees().iterator().next().getRulePointer().headPredicate().getPredicateSymbol();
			if (ruleHead.equals(nodeRel))//node.getTrees().getRulePointer() != rule &&
			{				
				List<ITuple> curBody = ArgMax ( FindAllRelInst (rule, node), node );
				if (curBody != null) 
				{
					double curw = curTree.getWeight() * BodyWeight(curBody) * rule.getWeight();
					curw = curw / ( node.getTrees().iterator().next().getRulePointer().getWeight() * GetChildrensWeight(node) );
					if (curw <= curTree.getWeight()) //the change makes sense because can't improve tree. only worsen it.
					{
						treeMaxHeap.AddLeaf( new ModifiedTreesNode(node, curBody, rule, curw, curTree) );
					}
					
					//treeMaxHeap.CheckAndAddLeaf( new ModifiedTreesNode(node, curBody, rule, curw), mK);
				}
			}
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: CollectNodesRec																				
	/** Description: Collects all nodes from tree recursively.				
	/*************************************************************************************************************/
	
	private Set<ITuple> CollectNodesRec (DerivationTree2 curTree)
	{
		Set<ITuple> nodes = new HashSet<ITuple>();
		
		if (false == curTree.getDerivedFact().isFact()) 
		{
			for (DerivationTree2 child : curTree.getChildren()) 
			{
				nodes.addAll(CollectNodesRec(child));
			}
			
			nodes.add(curTree.getDerivedFact());
		}
		
		return nodes;
	}
	
	

	/*************************************************************************************************************/
	/** Title: BodyWeight																				
	/** Description: Calculates the combined weight of all the trees of the elements on the body				
	/*************************************************************************************************************/
	
	private double BodyWeight (List<ITuple> relInst)
	{
		double retVal = 1;
		for (ITuple instTuple : relInst) // multiply all top-1 weights in new rule's body 
		{
			retVal *= instTuple.getTrees().iterator().next().getWeight();
			//retVal = Math.min(retVal, instTuple.getTrees().getWeight());
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: ArgMax																				
	/** Description: Returns the body with maximum weight from the list of bodies 				
	/*************************************************************************************************************/
	
	private List<ITuple> ArgMax (List<List<ITuple>> relInst, ITuple node)
	{
		double maxw = 0;
		List<ITuple> argMax = null;
		for (List<ITuple> list : relInst) 
		{
			double instw = 1;
			for (ITuple tuple : list) 
			{
				instw *= tuple.getTrees().iterator().next().getWeight();
				//instw = Math.min(instw, tuple.getTrees().getWeight());
			}
			
			List<ITuple> body = node.getTrees().iterator().next().getBodyOfRoot();
			if (instw > maxw && false == list.equals(body)) 
			{
				argMax = list;
				maxw = instw; 
			}
		}
		
		return argMax;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetTreesFromBody																				
	/** Description: Gets the trees of all elements in body 				
	/*************************************************************************************************************/
	
	private List<DerivationTree2> GetTreesFromBody (ICompiledRule rule, List<ITuple> body)
	{
		List<DerivationTree2> trees = new ArrayList<DerivationTree2>();
		for (int i = 0; i < body.size(); i++) 
		{
			ITuple tuple = body.get(i);
			RuleElement element = rule.getElements().get(i);
			trees.add( KeyMap2.getInstance().Get(element.getPredicate().getPredicateSymbol(), tuple).getTrees().iterator().next());
		}
		
		return trees;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetBodyFromTrees																				
	/** Description: Gets the body of the rule that fact was derived from 				
	/*************************************************************************************************************/
	
	private List<ITuple> GetBodyFromTrees (DerivationTree2 tree)
	{
		List<ITuple> body = new ArrayList<ITuple>();
		if (null != tree.getChildren()) 
		{
			for (DerivationTree2 child : tree.getChildren()) 
			{
				body.add( child.getDerivedFact() );
			}
		}
		
		return body;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: FindAllRelInst																				
	/** Description: Finds a list to match the partly inst body atoms: [[body 1], [body 2], ...]  				
	/*************************************************************************************************************/
	
	private List<List<ITuple>> FindAllRelInst (ICompiledRule rule, ITuple node)
	{
		List<List<ITuple>> bodies = new ArrayList<List<ITuple>>();
		List<List<ITuple>> illegalBodies = new ArrayList<List<ITuple>>();
		List<ITuple> partlyInstTuples = GetInstTuples(rule, node);
		HandleFirstTuple(rule, node, partlyInstTuples, bodies);
		
		for (int i = 1; i < partlyInstTuples.size(); i++) 
		{
			RuleElement element = rule.getElements().get(i);
			ITuple partlyInstTuple = partlyInstTuples.get(i);
			for (int j = 0; j < bodies.size(); j++) 
			{
				ITuple instTuple = MakeInstTuple(bodies.get(j).get(0), partlyInstTuples.get(0) ,partlyInstTuple);
				if ( true == KeyMap2.getInstance().Contains(element.getPredicate().getPredicateSymbol(), instTuple) ) 
				{
					bodies.get(j).add(KeyMap2.getInstance().Get(element.getPredicate().getPredicateSymbol(), instTuple));
				}
				
				else
				{
					illegalBodies.add(bodies.get(j));
				}
			}
		}
		
		bodies.removeAll(illegalBodies);
		return bodies;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: HandleFirstTuple																				
	/** Description: Finds a list to match the partly inst body atoms
	/** [[relevant for first tuple in body], [relevant for second tuple in body], ...]  				
	/*************************************************************************************************************/
	
	private void HandleFirstTuple (ICompiledRule rule, ITuple node, List<ITuple> instTuples, List<List<ITuple>> bodies)
	{
		RuleElement firstElement = rule.getElements().get(0);
		KeyMap2.getInstance().GetAll(firstElement.getPredicate().getPredicateSymbol(), instTuples.get(0), bodies);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetInstTuples																				
	/** Description: Gets the partly inst. body tuples to later match them to tuples from DB 				
	/*************************************************************************************************************/
	
	private List<ITuple> GetInstTuples (ICompiledRule rule, ITuple node)
	{
		RuleElement head = rule.getElements().get(rule.getElements().size() - 1);
		List<ITuple> body = new ArrayList<ITuple>();
		for (int i = 0; i < rule.getElements().size() - 2; i ++) 
		{
			RuleElement element = rule.getElements().get(i);
			body.add( MakeInstTuple(element, head, node) );
		}
		
		return body;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MakeInstTuple																				
	/** Description: Inst. in element constants from instTuple according to vars in toMatch 				
	/*************************************************************************************************************/
	
	private ITuple MakeInstTuple (RuleElement element, RuleElement toMatch, ITuple instTuple)
	{
		List<ITerm> terms = new ArrayList<ITerm>();
		
		for (ITerm iTerm : element.getView()) 
		{
			terms.add(iTerm);
		}
		/*
		for (int i : element.getIndices()) 
		{
			for (int j : toMatch.getIndices()) 
			{
				if (i == j) 
				{
					Integer [] indicesElt = ToIntegerArr(element.getIndices());
					Integer [] indicesMatch = ToIntegerArr(toMatch.getIndices());
					int curIndex = Arrays.asList(indicesElt).indexOf(i);
					ITerm term = instTuple.get(Arrays.asList(indicesMatch).indexOf(i));
					terms.set(curIndex, term);
				}
			}
		}
		*/

		/*List<Integer> elementList =  MakeIntegerList( element.getIndices() );
		List<Integer> matchList =  MakeIntegerList( toMatch.getIndices() );
		elementList.retainAll(matchList);
		
		for (Integer i : elementList) 
		{
			Integer [] indicesElt = ToIntegerArr(element.getIndices());
			Integer [] indicesMatch = ToIntegerArr(toMatch.getIndices());
			int curIndex = Arrays.asList(indicesElt).indexOf(i);
			ITerm term = instTuple.get(Arrays.asList(indicesMatch).indexOf(i));
			terms.set(curIndex, term);
		}*/
		
		/*List<Integer> varIdxInElement =  GetVariableIndices( element );
		List<Integer> varIdxInMatch =  GetVariableIndices( toMatch );
		
		for (int i = 0; i < varIdxInMatch.size(); i++) 
		{
			terms.set(varIdxInElement.get(i), instTuple.getTerms()[varIdxInMatch.get(i)]);
		}*/
		
		for (ITerm eltTerm : element.getView()) 
		{
			for (ITerm matchTerm : toMatch.getView()) 
			{
				if (eltTerm instanceof IVariable && matchTerm.equals(eltTerm))
				{
					int idx = Arrays.asList(element.getView().getTerms()).indexOf(eltTerm);
					ITerm term = instTuple.getTerms()[Arrays.asList(toMatch.getView().getTerms()).indexOf(matchTerm)];
					terms.set(idx, term);
				}
			}
		}
		
		return Factory.BASIC.createTuple(terms);
	}
	
	
	/*************************************************************************************************************/
	/** Title: GetVariableIndices																				
	/** Description: gets the indices of the variables in the rule element 				
	/*************************************************************************************************************/
	
	private List<Integer> GetVariableIndices (RuleElement elt)
	{
		List<Integer> intList = new ArrayList<Integer>();

		for ( ITerm term : elt.getView().getTerms() )
		{
			if (term instanceof IVariable) 
			{
				intList.add( Arrays.asList(elt.getView().getTerms()).indexOf(term) );
			}
		}
		
		return intList;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MakeIntegerList																				
	/** Description: convert int[] to List<Integer 				
	/*************************************************************************************************************/
	
	private List<Integer> MakeIntegerList (int [] arr)
	{
		List<Integer> intList = new ArrayList<Integer>();
	    for (int index = 0; index < arr.length; index++)
	    {
	        intList.add(arr[index]);
	    }
	    
	    return intList;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MakeInstTuple																				
	/** Description: Inst. in element constants from instTuple according to vars in toMatch 				
	/*************************************************************************************************************/
	
	private ITuple MakeInstTuple (ITuple instTuple, ITuple partlyInstTuple, ITuple tupleToInst)
	{
		List<ITerm> terms = new ArrayList<ITerm>();
		for (ITerm iTerm : tupleToInst) 
		{
			terms.add(iTerm);
		}

		for (int i = 0; i < tupleToInst.size(); i++) 
		{
			ITerm term = tupleToInst.get(i);
			if (term instanceof IVariable) 
			{
				terms.set(i, instTuple.get( partlyInstTuple.indexOf(term) ));
			}
		}
		
		return Factory.BASIC.createTuple(terms);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetChildrensWeight																				
	/** Description: Gets the combined weight of the children of the tree of node				
	/*************************************************************************************************************/
	
	private double GetChildrensWeight(ITuple node)
	{
		double retVal = 1;
		for (DerivationTree2 tree : node.getTrees().iterator().next().getChildren())
		{
			retVal *= tree.getWeight();
			//retVal = Math.min(retVal, tree.getWeight());
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: ToIntegerArr																				
	/** Description: converts int [] to Integer []				
	/*************************************************************************************************************/
	
	private static Integer[] ToIntegerArr(int[] intArray) 
	{	 
		Integer[] result = new Integer[intArray.length];
		for (int i = 0; i < intArray.length; i++) 
		{
			result[i] = Integer.valueOf(intArray[i]);
		}
		return result;
	}
}
