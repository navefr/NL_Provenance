package Top1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.rules.compiler.ICompiledRule;

public class ModifiedDerivationTrees 
{
	private List<ModifiedTreesNode> leaves;
	//private PriorityQueue<ModifiedTreesNode> leaves;
	
	
	public ModifiedDerivationTrees ()
	{
		//leaves = new PriorityQueue<ModifiedTreesNode>();
		leaves = new ArrayList<ModifiedTreesNode>();
	}


	public /*PriorityQueue*/List<ModifiedTreesNode> getLeaves() 
	{
		return leaves;
	}


	public void AddLeaf (ModifiedTreesNode leaf) 
	{
		leaves.add(leaf);
		Collections.sort(leaves);
	}
	
	
	public ModifiedTreesNode PopMax () 
	{
		return leaves.remove(0);//leaves.poll();
	}
	
	
	public void CheckAndAddLeaf (ModifiedTreesNode node, int k) 
	{
		if (leaves.size() < k) 
		{
			AddLeaf(node);
		}
		
		else if (leaves.get(k - 1).getWeight() < node.getWeight())
		{
			leaves.remove(k - 1);
			AddLeaf(node);
		}
	}
	
	
	public boolean isEmpty () 
	{
		return leaves.isEmpty();
	}
	
	
	public void clear () 
	{
		leaves.clear();
	}
	
	
	public static class ModifiedTreesNode implements Comparable<ModifiedTreesNode>
	{
		ITuple mtreeNode;
		
		List<ITuple> mBody;
		
		ICompiledRule mRule;
		
		double mWeight;
		
		DerivationTree2 changedTree = new DerivationTree2();
		
		
		public ModifiedTreesNode() {}
		
		
		public ModifiedTreesNode(ITuple node, List<ITuple> body, ICompiledRule rule, double weight, DerivationTree2 treeChnaged) 
		{
			mtreeNode = node;
			mBody = body;
			mRule = rule;
			mWeight = weight;
			changedTree = treeChnaged;
		}
		
		
		public ITuple getTreeNode() {
			return mtreeNode;
		}


		public List<ITuple> getBody() {
			return mBody;
		}


		public ICompiledRule getRule() {
			return mRule;
		}


		public double getWeight() {
			return mWeight;
		}
		
		
		public DerivationTree2 getTree() {
			return changedTree;
		}
		
		
		public void setTreeNode(ITuple mtreeNode) {
			this.mtreeNode = mtreeNode;
		}


		public void setBody(List<ITuple> mBody) {
			this.mBody = mBody;
		}


		public void setWeight(double mWeight) {
			this.mWeight = mWeight;
		}
		
		
		public void setRule(ICompiledRule rule) {
			this.mRule = rule;
		}
		
		
		
		
		@Override
		public int compareTo(ModifiedTreesNode other) 
		{
			double w1= mWeight;
			double w2= other.getWeight();
		    int retVal;
		    if (w1 == w2) 
		    { 
		    	retVal = 0; 
		    }
		    
		    else if (w1 < w2) 
		    { 
		    	retVal = 1; 
		    }
		    
		    else 
		    {
		    	retVal = -1;
		    }
		    
		    return retVal;
		}
	}
}
