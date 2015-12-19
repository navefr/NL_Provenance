package TopK;
import java.util.Vector;

import Basics.*;
import Pattern.PatternNode;

public class DerivationTree implements Comparable<DerivationTree>
{
	Atom derivedFact;
	
	Vector<Atom> factPointers;
	
	Rule rulePointer;
	
	Body bodyInProv;
	
	Vector<DerivationTree> children; 
	
	Vector<DerivationTree> parents; 
	
	PatternNode node;
	
	double weight;
	
	
	
	
	
	
	public Body getBodyInProv() 
	{
		return bodyInProv;
	}



	public void setBodyInProv(Body bodyInProv) 
	{
		this.bodyInProv = bodyInProv;
	}



	public Vector<DerivationTree> getChildren() 
	{
		return children;
	}

	
	
	public Vector<DerivationTree> getParents() 
	{
		return parents;
	}

	
	
	public void setParents(Vector<DerivationTree> parents) 
	{
		this.parents = parents;
	}
	
	
	
	public void addParent(DerivationTree parent) 
	{
		if (null == this.parents) 
		{
			 this.parents = new Vector<DerivationTree>();
		}
		
		this.parents.add(parent);
	}

	
	
	public void setChildren(Vector<DerivationTree> children) 
	{
		this.children = children;
	}
	
	
	
	public void addChild(DerivationTree child) 
	{
		if (null == this.children) 
		{
			 this.children = new Vector<DerivationTree>();
		}
		
		this.children.add(child);
	}

	
	
	public Atom getDerivedFact() 
	{
		return derivedFact;
	}

	
	
	public void setDerivedFact(Atom derivedFact) 
	{
		this.derivedFact = derivedFact;
	}
	
	
	
	public Vector<Atom> getFactPointers() 
	{
		return factPointers;
	}
	
	
	
	public void setFactPointers(Vector<Atom> factPointers) 
	{
		this.factPointers = factPointers;
	}
	
	
	
	public void addFactPointer(Atom factPointer) 
	{
		if (null == this.factPointers) 
		{
			this.factPointers = new Vector<Atom>();
		}
		
		this.factPointers.add(factPointer);
	}
	
	
	
	public Rule getRulePointer() 
	{
		return rulePointer;
	}
	
	
	
	public void setRulePointer(Rule rulePointer) 
	{
		this.rulePointer = rulePointer;
	}
	
	
	
	public double getWeight() 
	{
		return weight;
	}
	
	
	
	public void setWeight(double weight) 
	{
		this.weight = weight;
	}

	
	
	public PatternNode getNode() 
	{
		return node;
	}



	public void setNode(PatternNode node) 
	{
		this.node = node;
	}



	public int compareTo(DerivationTree other) 
	{
		double w1= weight;
		double w2= other.getWeight();
	    int retVal;
	    if (w1 == w2) { retVal = 0; }	    	
	    else if (w1 > w2) { retVal = -1; }
	    else {retVal = 1;}
	    return retVal;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		/*result = prime * result
				+ ((children == null) ? 0 : children.hashCode());*/
		result = prime * result
				+ ((derivedFact == null) ? 0 : derivedFact.hashCode());
		/*result = prime * result
				+ ((factPointers == null) ? 0 : factPointers.hashCode());*/
		/*result = prime * result
				+ ((rulePointer == null) ? 0 : rulePointer.hashCode());*/
		/*long temp;
		temp = Double.doubleToLongBits(weight);
		result = prime * result + (int) (temp ^ (temp >>> 32));*/
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
		DerivationTree other = (DerivationTree) obj;
		
		/*if (children == null) {
			if (other.children != null)
				return false;
		} 
		else if (children.size() != other.children.size())
		{
			return false;
		}*/
		
		/*else if (!children.equals(other.children))
			return false;*/
		if (derivedFact == null) {
			if (other.derivedFact != null)
				return false;
		} else if (!derivedFact.equals(other.derivedFact))
			return false;
		
		/*if (factPointers == null) {
			if (other.factPointers != null)
				return false;
		} else if (!factPointers.equals(other.factPointers))
			return false;*/
		
		/*if (parents == null) {
			if (other.parents != null)
				return false;
		} else if (!parents.equals(other.parents))
			return false;*/
		
		/*if (rulePointer == null) {
			if (other.rulePointer != null)
				return false;
		} else if (!rulePointer.equals(other.rulePointer))
			return false;
		if (Double.doubleToLongBits(weight) != Double
				.doubleToLongBits(other.weight))
			return false;*/
		
		return true;
	}
	
	
	
	
	
}
