package Top1;
import java.util.ArrayList;
import java.util.Collections;

public class ComparissonTree 
{
	public ArrayList<CompareNode> leaves;

	public ComparissonTree(CompareNode r)
	{
		leaves = new ArrayList<CompareNode>();
		leaves.add(r);
	}
	
	
	
	public ComparissonTree()
	{
		
	}
	

	
	public ArrayList<CompareNode> getLeaves() 
	{
		Collections.sort(leaves);
		return leaves;
	}

	
	
	public void setLeaves(ArrayList<CompareNode> leaves) 
	{
		this.leaves = leaves;
	}

	
	
	public void BuildLeaves (CompareNode v)
	{
		int n = v.getIndices().length;
		CompareNode [] children = new CompareNode [n]; 
		
		for (int i = 0; i < n; i++) 
		{
			int [] childIndices = v.getIndices().clone();
			childIndices[i]++;
			children[i] = new CompareNode();
			children[i].setIndices(childIndices);
			leaves.add(children[i]);
		}
		
		//v.setChildren(children);
		this.leaves.remove(v);
		//Collections.sort(leaves);
	}
	
	
	public static class CompareNode implements Comparable<CompareNode>
	{
		int indices [];
		
		double weight;
		

		public int[] getIndices() 
		{
			return indices;
		}

		
		public void setIndices(int ... indices) 
		{
			this.indices = indices;
		}
		
		
		public double getWeight() 
		{
			return weight;
		}
		
		public void setWeight(double weight) 
		{
			this.weight = weight;
		}
		
		
		@Override
		public int compareTo(CompareNode other) {
			double w1= weight;
			double w2= other.getWeight();
		    int retVal;
		    if (w1 == w2) 
		    { 
		    	retVal = 0; 
		    }
		    
		    else if (w1 > w2) 
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
