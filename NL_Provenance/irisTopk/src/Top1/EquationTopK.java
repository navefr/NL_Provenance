package Top1;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import TopKBasics.*;

public class EquationTopK 
{	
	static Set<Atom> treesUpdatedLastIter = new HashSet<Atom>();
	
	static int k = 1;
	
	boolean factsUpdated = false;
	
	static boolean online = true;
	

	public EquationTopK () {}
	
	
	
	public EquationTopK (int ik)
	{
		k = ik;
	}
	
	
	
	public EquationTopK (int ik, boolean on)
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
	
	public static void GetTopKTreesForBody (Atom key, Body b, List<DerivationTree> topKTrees)
	{
		Vector<List<DerivationTree>> arr = new Vector<List<DerivationTree>>();
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
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: InitializeTreeOfCompareNode																			
	/** Description: Initialize the parameters of a CompareNode and it's derivation tree for AND type CompareNode							
	/*************************************************************************************************************/
	
	public static DerivationTree InitializeTreeOfCompareNode (Atom key, Vector<List<DerivationTree>> arr, ComparissonTree.CompareNode v, Body body) 
	{
		DerivationTree curTree = new DerivationTree();
		
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
	
	public static void InitializeCompareNode (Atom key, Vector<List<DerivationTree>> arr, ComparissonTree.CompareNode v, Body body) 
	{
		boolean validTree = true;
		double weight = body.getRuleWeight();
		
		for (int i = 0; i < arr.size(); i++) 
		{
			if (arr.get(i).size() > v.getIndices()[i])
			{
				weight *= arr.get(i).get( v.getIndices()[i] ).getWeight();
			}
			
			else // there is a child that does not participate in the tree
			{
				validTree = false;
				break;
			}
		}
		
		if (true == validTree && (false == online || key.getTree() == null || key.getTree().getWeight() < weight)) //confirm it is not empty tree
		{
			v.setWeight(weight);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetChildrenTreeVectors																					
	/** Description: Returns a vector of the derivation trees of all children of a circuit node														
	/*************************************************************************************************************/
	
	public static void GetChildrenTreeVectors (Vector<List<DerivationTree>> arr, Body b)
	{
		for (Atom child : b.getAtoms()) 
		{
			List<DerivationTree> tree = new ArrayList<DerivationTree>();
			tree.add(child.getTree());
			arr.add(tree);
		}
    }
	
	
	
	/*************************************************************************************************************/
	/** Title: ChooseTopKTrees																					
	/** Description: Handles OR type node in the circuit														
	/*************************************************************************************************************/
	
	public static void ChooseTopKTrees (List<DerivationTree> topKTrees)
	{
		Collections.sort(topKTrees); //sort in descending order
		if (topKTrees.size() > k) 
		{
			List<DerivationTree> sub = new Vector<DerivationTree>(topKTrees.subList(0, k));
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
	
	public static void UpdateTop1WhileSemiNaive(Atom key, Body body)
	{
		for (Atom bodyAtom : body.getAtoms()) 
		{
			if (true == bodyAtom.isFact() && false == bodyAtom.isTopKUpdated()) 
			{
				SetTreeForFact(bodyAtom);
				bodyAtom.setFoundTop1(true);
			}
		}
		
		if (false == key.didFindTop1()) 
		{
			if (true == body.isTopKUpdated())
			{
				List<DerivationTree> topKTrees = new Vector<DerivationTree>();
				GetTopKTreesForBody(key, body, topKTrees);
				SiftTopKTrees(key, topKTrees);
				//key.setFoundTop1(true);
			}
			
			else
			{
				System.out.println("EquationTopK::UpdateTop1WhileSemiNaive:: Error - body atom has no trees");
			}
		}
	}
	
	

	/*************************************************************************************************************/
	/** Title: SiftTopKTrees																					
	/** Description:  									
	/*************************************************************************************************************/
	
	/*for (DerivationTree derivationTree : key.getTrees()) {
	if (!Provenance.getInstance().Get(key).contains(derivationTree.getBodyInProv())) {
		Set<Body> b =  Provenance.getInstance().Get(key);
		System.out.println("problem: ");
	}
}*/
	public static void SiftTopKTrees (Atom key, List<DerivationTree> topKTrees)
	{
		if (false == topKTrees.isEmpty())
		{
			if (true == online) 
			{
				UpdateForTop1(key, topKTrees);
			}
			
			else if (null == key.getTree() || false == key.getTree().equals(topKTrees)) 
			{
				if (null != key.getTree()) 
				{
					topKTrees.add(key.getTree());
				}

				Update(key, topKTrees);
			}			
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: UpdateForTop1																					
	/** Description: Performs a series of update tasks to add trees to key 									
	/*************************************************************************************************************/
	
	public static void UpdateForTop1 (Atom key, List<DerivationTree> topKTrees)
	{
		key.setTopKUpdated(true);
		key.setTrees(topKTrees.get(0));
		treesUpdatedLastIter.add(key);
	}
	
	
	/*************************************************************************************************************/
	/** Title: Update																					
	/** Description: Performs a series of update tasks to add trees to key 									
	/*************************************************************************************************************/
	
	public static void Update (Atom key, List<DerivationTree> topKTrees)
	{
		ChooseTopKTrees(topKTrees);
		key.setTopKUpdated(true);
		key.setTrees(topKTrees.get(0));
		treesUpdatedLastIter.add(key);
		//key.setTreesChangedLastIteration(true);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: UpdateFactAtoms																					
	/** Description: Makes the trees for the fact atoms int the prov. 									
	/*************************************************************************************************************/
	
	public static void SetTreeForFact (Atom fact) 
	{
		DerivationTree curTree = new DerivationTree();
		curTree.setWeight(1);
		curTree.setDerivedFact(fact);
		fact.AddTree(curTree);
		fact.setTopKUpdated(true);
		fact.setFoundTop1(true);
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
