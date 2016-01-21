package Top1;
import TopKBasics.Atom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.rules.compiler.CompiledRule;

import java.util.*;

public class EquationTopK2 
{	
	static Set<Atom> treesUpdatedLastIter = new HashSet<Atom>();
	
	static int k = 100;
	
	boolean factsUpdated = false;
	
	static boolean online = true;
	

	public EquationTopK2 () {}
	
	
	
	public EquationTopK2 (int ik)
	{
		k = ik;
	}
	
	
	
	public EquationTopK2 (int ik, boolean on)
	{
		k = ik;
		online = on;
	}
	
	

	public Set<Atom> getTreesUpdatedLastIter() 
	{
		return treesUpdatedLastIter;
	}


	public void setTreesUpdatedLastIter(Set<Atom> itreesUpdatedLastIter) 
	{
		treesUpdatedLastIter = itreesUpdatedLastIter;
	}



	public void addToTreesUpdatedLastIter(Atom atom) 
	{
		treesUpdatedLastIter.add(atom);
	}



	/*************************************************************************************************************/
	/** Title: TopK																					
	/** Description: Calculate top-k deriv. trees for each atom in the prov.									
	/*************************************************************************************************************/
	
	/*public void TopK ()
	{
		Set<Atom> previous;
		//MarkAtomsTreesChangedLastIteration();
		if (false == this.factsUpdated)
		{
			UpdateFactAtoms();
		}
		
		while (false == this.treesUpdatedLastIter.isEmpty())
		{
			previous = new HashSet<Atom> (this.treesUpdatedLastIter);
			TopKIteration();
			SetAtomsToNotUpdatedLastIteration(previous);
		}
		
		for (Atom atom : KeyMap.getInstance().Values()) {
			if (atom.toString().equals("dealsWith1(Canada,Andorra)") && atom.getTrees() != null) {
				System.out.println(atom.getTrees().get(0).getFactPointers());
			}
		}
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: SetAtomsToNotUpdatedLastIteration																					
	/** Description: One iteration of the top-k algorithm on the prov.									
	/*************************************************************************************************************/
	
	/*public void SetAtomsToNotUpdatedLastIteration (Set<Atom> previous) 
	{
		for (Atom atom : previous) 
		{
			atom.setTreesChangedLastIteration(false);
		}
		
		treesUpdatedLastIter.removeAll(previous);
	}*/
	
	

	/*************************************************************************************************************/
	/** Title: UpdateFactAtoms																					
	/** Description: Makes the trees for the fact atoms int the prov. 									
	/*************************************************************************************************************/
	
	/*public void UpdateFactAtoms () 
	{
		for (Atom atom : KeyMap.getInstance().Values()) 
		{
			if (true == atom.isFact() && false == atom.isTopKUpdated() ) 
			{
				SetTreeForFact(atom);
				this.treesUpdatedLastIter.add(atom);
			}
		}
		
		this.factsUpdated = true;
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: TopKIteration																					
	/** Description: One iteration of the top-k algorithm on the prov.									
	/*************************************************************************************************************/
	
	/*public void TopKIteration () 
	{
		for (Atom key : KeyMap.getInstance().Values()) 
		{
			FindTopKTrees(key);
		}
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: FindTopKTrees																					
	/** Description: Given an atom, goes through all derivations of this atom to find the top k trees 									
	/*************************************************************************************************************/
	
	/*public void FindTopKTrees(Atom key)
	{
		List<DerivationTree> topKTrees = new Vector<DerivationTree>();
		
		if (null != key.getTrees()) 
		{
			topKTrees.addAll(key.getTrees());
		}
		
		for (Body body : Provenance.getInstance().Get(key)) 
		{
			if (true == body.AllAtomsHaveTopK() && true == body.HasAtomInTreesUpdatedLastIter()) 
			{
				GetTopKTreesForBody(key, body, topKTrees);
			}
		}
		
		SiftTopKTrees(key, topKTrees);
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: SameWeightTrees																					
	/** Description: Checks if prevTrees and curTrees have the same weight 									
	/*************************************************************************************************************/
	
	public boolean SameWeightTrees(List<DerivationTree> curTrees, List<DerivationTree> prevTrees)
	{
		boolean retVal = true;
		if (null != prevTrees) 
		{
			if (prevTrees.size() == k) 
			{
				for (int i = 0; i < k; i++) 
				{
					if (curTrees.get(i).getWeight() != prevTrees.get(i).getWeight())
					{
						retVal = false;
						break;
					}
				}
			}
			
			else
			{
				retVal = false;
			}
		}
		
		else
		{
			retVal = false;
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: HandleANDNode																					
	/** Description: Handles AND type node in the circuit														
	/*************************************************************************************************************/
	
	public static void GetTopKTreesForBody (ITuple key, List<ITuple> b, List<DerivationTree2> topKTrees)
	{
		Vector<List<DerivationTree2>> arr = new Vector<List<DerivationTree2>>();
		GetChildrenTreeVectors(arr, b);
		ComparissonTree.CompareNode root = new ComparissonTree.CompareNode();
		root.setIndices(new int[arr.size()]);

		ComparissonTree compareTree = new ComparissonTree (root);
		ComparissonTree.CompareNode chosenNode;

		for (int i = 0; i < k; i++) 
		{
			for (ComparissonTree.CompareNode u : compareTree.getLeaves())
			{
				//InitializeTreeOfCompareNode(key, arr, u, b);
				InitializeCompareNode(key, arr, u, b);
			}

			// select the best node of the leaves
			chosenNode = compareTree.getLeaves().get(0);
			
			if (chosenNode.getWeight() > 0)
			{
				// check if the tree of chosen node has enough weight to get into topKTrees
				if (topKTrees.size() < k || topKTrees.get(topKTrees.size() - 1).getWeight() < chosenNode.getWeight()) 
				{
					//InitializeTreeOfCompareNode(key, arr, chosenNode, b);
					if (topKTrees.size() >= k) 
					{
						topKTrees.remove(topKTrees.size() - 1);
					}
		
					topKTrees.add(InitializeTreeOfCompareNode(key, arr, chosenNode, b));//topKTrees.add(chosenNode.getTrees());
					compareTree.BuildLeaves(chosenNode);
					Collections.sort(topKTrees); //sort in descending order
				}
			}
		}
		/*if (topKTrees.isEmpty()) {
			System.out.println("EMPTY TREES!!! for atom " + key);
			System.out.println("where trees of body are " + b.get(0).getTrees());
		}*/
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: InitializeTreeOfCompareNode																			
	/** Description: Initialize the parameters of a CompareNode and it's derivation tree for AND type CompareNode							
	/*************************************************************************************************************/
	
	public static DerivationTree2 InitializeTreeOfCompareNode (ITuple key, Vector<List<DerivationTree2>> arr, ComparissonTree.CompareNode v, List<ITuple> body) 
	{
		DerivationTree2 curTree = new DerivationTree2();
		
		for (int i = 0; i < arr.size(); i++) 
		{
			//curTree.addFactPointer(arr.get(i).get( v.getIndices()[i] ).getDerivedFact());
			curTree.addChild(arr.get(i).get( v.getIndices()[i] ));
			arr.get(i).get( v.getIndices()[i] ).addParent(curTree);
		}
		
		curTree.setWeight(v.getWeight());
		curTree.setDerivedFact(key);
		//curTree.setBodyInProv(body);
		return curTree;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: InitializeCompareNode																			
	/** Description: Initialize the parameters of CompareNode for  AND type CompareNode							
	/*************************************************************************************************************/
	
	public static void InitializeCompareNode (ITuple key, Vector<List<DerivationTree2>> arr, ComparissonTree.CompareNode v, List<ITuple> body) 
	{
		boolean validTree = true;
		double weight = key.getCurRuleWeight();
		
		for (int i = 0; i < arr.size(); i++) 
		{
			if (arr.get(i).size() > v.getIndices()[i])
			{
				weight *= arr.get(i).get( v.getIndices()[i] ).getWeight();
				//weight = Math.min(weight, arr.get(i).get( v.getIndices()[i] ).getWeight());
			}
			
			else // there is a child that does not participate in the tree
			{
				validTree = false;
				break;
			}
		}
		
		if (validTree) //confirm it is not empty tree
		{
			v.setWeight(weight);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetChildrenTreeVectors																					
	/** Description: Returns a vector of the derivation trees of all children of a circuit node														
	/*************************************************************************************************************/
	
	public static void GetChildrenTreeVectors (Vector<List<DerivationTree2>> arr, List<ITuple> b)
	{
		for (ITuple child : b) 
		{
			List<DerivationTree2> tree = new ArrayList<DerivationTree2>();
			tree.add(child.getTrees().iterator().next());
			arr.add(tree);
		}
    }
	
	
	
	/*************************************************************************************************************/
	/** Title: ChooseTopKTrees																					
	/** Description: Handles OR type node in the circuit														
	/*************************************************************************************************************/
	
	public static void ChooseTopKTrees (List<DerivationTree2> topKTrees)
	{
		Collections.sort(topKTrees); //sort in descending order
		if (topKTrees.size() > k) 
		{
			List<DerivationTree2> sub = new Vector<DerivationTree2>(topKTrees.subList(0, k));
			//CleanProv(topKTrees);
			topKTrees.clear();
			topKTrees.addAll(sub);
		}
    }
	
	
	
	/*************************************************************************************************************/
	/** Title: CleanProv																					
	/** Description: 														
	/*************************************************************************************************************/
	
	/*public void CleanProv (List<DerivationTree> topKTrees)
	{
		if (true == this.online) 
		{
			List<DerivationTree> xor = new Vector<DerivationTree>(topKTrees.subList(this.k, topKTrees.size()));
			for (DerivationTree derivTree : xor) 
			{
				//System.out.println("size before removing: " + Provenance.getInstance().Get(derivTree.getDerivedFact()).size());
				Provenance.getInstance().Get(derivTree.getDerivedFact()).remove(derivTree.getBodyInProv());
				//System.out.println("size after removing: " + Provenance.getInstance().Get(derivTree.getDerivedFact()).size());
				if (!retVal) 
				{
					System.out.println("atom "+ topKTrees.get(0).getDerivedFact() + " doesn't contain " + derivTree.getBodyInProv());
					System.out.println("prov of atom is: " + Provenance.getInstance().Get(derivTree.getDerivedFact()));
				}
			}			
		}
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: MarkAtomsTreesChangedLastIteration																					
	/** Description: 									
	/*************************************************************************************************************/
	
	/*public void MarkAtomsTreesChangedLastIteration ()
	{
		for (Atom atom : this.treesUpdatedLastIter) 
		{
			atom.setTreesChangedLastIteration(true);
		}
	}*/

    /*************************************************************************************************************/
    /** Title: UpdateTreesWhileSemiNaive
     /** Description: Given an atom and a new derivation, Updates the Trees of this atom While Semi-Naive is running
     /*************************************************************************************************************/

    public static List<DerivationTree2> UpdateWhileSemiNaive(ITuple key, List<ITuple> body, List<DerivationTree2.Condition> conditions, List<ILiteral> literals)
    {
        for (int i = 0; i < body.size(); i++) {
            ITuple bodyAtom = body.get(i);
            DerivationTree2.Condition condition = conditions.get(i);
            ILiteral literal = literals.get(i);
            if (bodyAtom.isFact())
            {
                SetTreeForFact(bodyAtom, condition, literal);
            }
        }

        List<DerivationTree2> topKTrees = new Vector<DerivationTree2>();
        GetTopKTreesForBody(key, body, topKTrees);
        SiftTopKTrees(key, topKTrees);
        return topKTrees;
    }


    /*************************************************************************************************************/
	/** Title: UpdateTreesWhileSemiNaive																					
	/** Description: Given an atom and a new derivation, Updates the Trees of this atom While Semi-Naive is running 									
	/*************************************************************************************************************/
	
	public static void UpdateTop1WhileSemiNaive(ITuple key, List<ITuple> body, List<DerivationTree2.Condition> conditions, List<ILiteral> literals)
	{
		/*if (key.toString().equals("('Saudi_Arabia', 'Nicaragua')")) 
		{
			for (ITuple bodyAtom : body) 
			{
				System.out.println("trees of body: " + bodyAtom + " tree: " + bodyAtom.getTrees());
				System.out.println("topk found? " + bodyAtom.isTop1Found());
			}
		}*/
		

        for (int i = 0; i < body.size(); i++) {
            ITuple bodyAtom = body.get(i);
            DerivationTree2.Condition condition = conditions.get(i);
            ILiteral literal = literals.get(i);
			if (true == bodyAtom.isFact() && false == bodyAtom.isTopKUpdated())
			{
				SetTreeForFact(bodyAtom, condition, literal);
			}
		}
		
		if (false == key.isTop1Found()) 
		{
			List<DerivationTree2> topKTrees = new Vector<DerivationTree2>();
			GetTopKTreesForBody(key, body, topKTrees);
			SiftTopKTrees(key, topKTrees);
		}
		/*
		if (key.toString().equals("('Saudi_Arabia', 'Nicaragua')")) 
		{
			for (ITuple bodyAtom : body) 
			{
				System.out.println("trees of body: " + bodyAtom + " tree: " + bodyAtom.getTrees());
			}
			
			System.out.println("trees of head: " + key + " tree: " + key.getTrees());
		}*/
	}
	
	

	/*************************************************************************************************************/
	/** Title: SiftTopKTrees																					
	/** Description:  									
	/*************************************************************************************************************/
	
	public static void SiftTopKTrees (ITuple key, List<DerivationTree2> topKTrees)
	{
        for (DerivationTree2 tree : topKTrees) {
            key.setTopKUpdated(true);
            key.addTree(tree);
        }
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: UpdateForTop1																					
	/** Description: Performs a series of update tasks to add trees to key 									
	/*************************************************************************************************************/
	
	public static void UpdateForTop1 (ITuple key, List<DerivationTree2> topKTrees)
	{
		key.setTopKUpdated(true);
		key.addTree(topKTrees.get(0));
		//treesUpdatedLastIter.add(key);
	}
	
	
	/*************************************************************************************************************/
	/** Title: Update																					
	/** Description: Performs a series of update tasks to add trees to key 									
	/*************************************************************************************************************/
	
	public static void Update (ITuple key, List<DerivationTree2> topKTrees, CompiledRule r)
	{
		ChooseTopKTrees(topKTrees);
		key.setTopKUpdated(true);
		topKTrees.get(0).setRulePointer(r);
		key.addTree(topKTrees.get(0));
		//treesUpdatedLastIter.add(key);
		//key.setTreesChangedLastIteration(true);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: UpdateFactAtoms																					
	/** Description: Makes the trees for the fact atoms int the prov. 									
	/*************************************************************************************************************/
	
	public static void SetTreeForFact (ITuple fact, DerivationTree2.Condition condition, ILiteral literal)
	{
		DerivationTree2 curTree = new DerivationTree2();
		curTree.setWeight(1);
		curTree.setDerivedFact(fact);
		fact.addTree(curTree);
		fact.setTopKUpdated(true);
		fact.setTop1Found(true);
        curTree.setCondition(condition);
        curTree.setLiteral(literal);
		/*if (false == online)
		{
			fact.setTreesChangedLastIteration(true);
		}*/
	}
	
	
	/*************************************************************************************************************/
	/** Title: CreateDummyTree																					
	/** Description: Makes dummy tree for atom 									
	/*************************************************************************************************************/
	
	public void CreateDummyTree (Atom atom) 
	{
		DerivationTree curTree = new DerivationTree();
		curTree.setWeight(0);
		curTree.setDerivedFact(atom);
		atom.AddTree(curTree);
	}
}
