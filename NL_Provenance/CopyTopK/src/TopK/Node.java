package TopK;
import java.util.Vector;

public class Node 
{
	double weight;
	
	Node parent;
	
	//Vector<Node> children  = new Vector<Node>();
	Node [] children;
	
	public double getWeight() 
	{
		return weight;
	}
	
	public void setWeight(double weight) 
	{
		this.weight = weight;
	}
	
	public Node getParent() 
	{
		return parent;
	}
	
	public void setParent(Node parent) 
	{
		this.parent = parent;
	}
	
	public Node [] getChildren() 
	{
		return children;
	}
	
	public void setChildren(Node ... children) 
	{
		/*for (Node node : children) 
		{
			this.children.add(node);
		}*/
		this.children = children;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		long temp;
		temp = Double.doubleToLongBits(weight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (Double.doubleToLongBits(weight) != Double
				.doubleToLongBits(other.weight))
			return false;
		return true;
	}


	
}
