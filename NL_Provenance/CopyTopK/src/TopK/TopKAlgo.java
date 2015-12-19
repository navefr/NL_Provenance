package TopK;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import Basics.Atom;
import Pattern.Pattern;
import Pattern.PatternNode;



public class TopKAlgo 
{
	
	
	public TopKAlgo () {}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetChildrenTrees																					
	/** Description: Returns a vector of the derivation trees of all children of a circuit node														
	/*************************************************************************************************************/
	
	public Vector<DerivationTree> GetChildrenTrees (Atom orNode)
	{
		Vector<DerivationTree> treeArr = new Vector<DerivationTree>();
		
		if (null != orNode.getChildren()) 
		{
			for (Atom child : orNode.getChildren()) //can delete all "AND" nodes because every later node will have sons from "OR" node only 
			{
				treeArr.addAll(child.getTrees());
				((Atom)child).getTrees().clear();
			}
		}
		
        return treeArr;
    }
	
	
	/*************************************************************************************************************/
	/** Title: GetChildrenTreeVectors																					
	/** Description: Returns a vector of the derivation trees of all children of a circuit node														
	/*************************************************************************************************************/
	
	public Vector<List<DerivationTree>> GetChildrenTreeVectors (Atom andNode)
	{
		Vector<List<DerivationTree>> childrenTrees = new Vector<List<DerivationTree>>();
		
		for (Atom child : andNode.getChildren()) 
		{
			childrenTrees.add(child.getTrees());
		}
        
        return childrenTrees;
    }
	
	

	/*************************************************************************************************************/
	/** Title: Handle_OR_Node																					
	/** Description: Handles OR type node in the circuit														
	/*************************************************************************************************************/
	
	public void HandleORNode (Atom orNode, int k)
	{
		Vector<DerivationTree> treeArr = GetChildrenTrees(orNode);
		Collections.sort(treeArr); //sort in descending order
		treeArr = (treeArr.size() > k) ? SubVector(treeArr, 0 , k) : treeArr;
		orNode.setTrees(treeArr);
    }
	
	
	
	/*************************************************************************************************************/
	/** Title: SubVector																						 
	/** Description: Returns a sub vector of the input vector, starting from start index until end index		
	/*************************************************************************************************************/
	
	public Vector<DerivationTree> SubVector (Vector<DerivationTree> vector, int start, int end)
	{
		Vector<DerivationTree> subVector = new Vector<DerivationTree>();
		for (int i = start; i < end; i++) 
		{
			if (null != vector.get(i)) 
			{
				subVector.add( vector.get(i) );
			}
		}
		
		return subVector;
	}
	
	
	/*************************************************************************************************************/
	/** Title: Handle_AND_Node																					
	/** Description: Handles AND type node in the circuit														
	/*************************************************************************************************************/
	
	public void HandleANDNode (Atom v, int k)
	{
		Vector<List<DerivationTree>> arr = GetChildrenTreeVectors(v);
		ComparissonTree.CompareNode root = new ComparissonTree.CompareNode();
		int [] indeices = new int[arr.size()];
		Arrays.fill(indeices, 0);
		root.setIndices(indeices);
		
		ComparissonTree compareTree = new ComparissonTree (root);
		Vector<DerivationTree> treeArr = new Vector<DerivationTree>();
        ComparissonTree.CompareNode chosenNode = new ComparissonTree.CompareNode();
        
        for (int i = 0; i < k; i++) 
        {
			for (ComparissonTree.CompareNode u : compareTree.getLeaves())
			{
				InitializeCompareNode(arr, u, v);
			}
			
			// select the best node of the leaves
			chosenNode = compareTree.getLeaves().get(0);
			
			/*if (chosenNode.getTree() != null) 
			{
				treeArr.add(chosenNode.getTree());
				compareTree.BuildLeaves(chosenNode);
			}*/
		}
        
        v.setTrees(treeArr);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: InitializeCompareNode																			
	/** Description: Initialize the parameters of CompareNode for  AND type CompareNode							
	/*************************************************************************************************************/
	
	public void InitializeCompareNode (Vector<List<DerivationTree>> arr, ComparissonTree.CompareNode v, Atom circuitNode) 
	{
		DerivationTree curTree = new DerivationTree();
		double weight = circuitNode.getRuleUsed().getWeight();
		
		for (int i = 0; i < arr.size(); i++) 
		{
			if (arr.get(i).size() > v.getIndices()[i])
			{
				weight *= arr.get(i).get( v.getIndices()[i] ).getWeight();
				curTree.addFactPointer(arr.get(i).get( v.getIndices()[i] ).getDerivedFact());
				curTree.addChild(arr.get(i).get( v.getIndices()[i] ));
				arr.get(i).get( v.getIndices()[i] ).addParent(curTree);
			}
		}
		
		if (false == curTree.getChildren().isEmpty()) //confirm it is not empty tree 
		{
			curTree.setWeight(weight);
			curTree.setDerivedFact(circuitNode);		
			//v.setTree(curTree);;
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: HandleFactNode																					
	/** Description: Handles Fact type node in the circuit														
	/*************************************************************************************************************/
	
	public static void HandleFactNode (Atom fact)
	{
		DerivationTree curTree = new DerivationTree();
		curTree.setWeight(1);
		curTree.setDerivedFact(fact);
		fact.AddTree(curTree);
	}
	
	
	/*************************************************************************************************************/
	/** Title: CiruitTopKAlgo																					
	/** Description: Updates Top-k derivation trees for a node in the circuit									
	/*************************************************************************************************************/

	public void QueryTopKOneNode (Atom v, int k)
	{
		if ( false == v.isTopKUpdated() )
		{
			if (null != v.getChildren())
			{
				for ( Atom child : v.getChildren() ) 
				{
					QueryTopKOneNode(child, k);
				}
			}
			
			if (true == v.isFact()) 
			{
				HandleFactNode(v);
			}
			
			else if (v.getType().equals( "OR" ))//null == v.getType() || 
			{		
				HandleORNode(v, k);
			}
		
			else if (v.getType().equals( "AND" ))
			{	        
				HandleANDNode(v, k);
			}
			
			v.setTopKUpdated(true);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: RunTopKForAtom																					
	/** Description: 									
	/*************************************************************************************************************/
	
	public void RunTopKForAtom (Atom v, int k, Vector<HashMap<String, Atom>> provenance)
	{
		Atom dummyParent = new Atom (v);
		dummyParent.setType("OR");
		Vector<Atom> relevantsInCircuit = new Vector<Atom>();
		for (HashMap<String, Atom> layer : provenance) 
		{
			if (layer.containsKey(v.toString())) 
			{
				Atom relevant = layer.get(v.toString());
				QueryTopKOneNode(relevant, k);
				relevantsInCircuit.add(relevant);
				dummyParent.AddChild(relevant);
			}
		}
		
		HandleORNode(dummyParent, k);
		v.setTrees(dummyParent.getTrees());
		for (Atom relevant : relevantsInCircuit) 
		{
			relevant.setTrees(dummyParent.getTrees());
			relevant.setTopKUpdated(true);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: RunTopKForPattern																					
	/** Description: 									
	/*************************************************************************************************************/
	
	public void RunTopKForPattern (Pattern p, int k, Vector<HashMap<String, Atom>> provenance)
	{
		ListIterator<Vector<PatternNode>> patternIter = p.getPatternVec().listIterator(p.getPatternVec().size());
		while (patternIter.hasPrevious())
		{
			for (PatternNode node : patternIter.previous()) 
			{
				RunTopKForAtom(node, k, provenance);
			}
		}
	}
}