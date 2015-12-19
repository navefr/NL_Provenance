package Pattern;

import java.util.Vector;

import Basics.*;

public class PatternNode extends Atom 
{
	PatternNode parent;
	
	Vector<PatternNode> patternChildren;
	
	boolean isTransChild = false;

	int wildCardIdx = -1; //-1 means no wild card

	String newName;
	
	
	public PatternNode () {}
	
	
	
	public PatternNode (String iName, boolean iFact, Proton ... iparams) 
	{
		super (iName, true, iFact, iparams);
		this.parent = new PatternNode();
		this.parent.setChildren(this);
	}
	
	
	
	public PatternNode (Atom atom) 
	{
		super (atom);
		this.parent = new PatternNode();
		this.parent.setChildren(this);
	}
	
	
	
	public PatternNode getParent() 
	{
		return parent;
	}


	
	public void setParent(PatternNode parent) 
	{
		this.parent = parent;
	}

	

	public Vector<PatternNode> getPatternChildren() 
	{
		return patternChildren;
	}

	
	public void AddPatternChild (PatternNode child)
	{
		if (null == this.patternChildren) 
		{
			this.patternChildren = new Vector<PatternNode>();
		}
		
		this.patternChildren.add(child);
	}

	
	public void setChildren(Atom ... ichildren) 
	{
		if (null == this.children) 
		{
			this.children = new Vector<Atom>();
		}
		
		for (Atom child : ichildren) 
		{
			this.children.add(child);
		}
	}
	
	
	public boolean isTransChild() 
	{
		return isTransChild;
	}



	public void setTransChild(boolean isTransChild) 
	{
		this.isTransChild = isTransChild;
	}



	public int getWildCardIdx()
	{
		return wildCardIdx;
	}



	public void setWildCardIdx(int wildCardIdx)
	{
		this.wildCardIdx = wildCardIdx;
	}
	
	
	

	public String getNewName() 
	{
		return newName;
	}



	public void setNewName(String newName) 
	{
		this.newName = newName;
	}
	
	
	public boolean isLeaf ()
	{
		return ( patternChildren == null );
	}
	
	/*************************************************************************************************************/
	/** Title: IsNodeRelationEdb																				
	/** Description: checks if the relation can be derived by one of the program rules 			
	/*************************************************************************************************************/
	
	public boolean IsNodeRelationEdb(Program p) 
	{
		boolean retVal = true;
		for (Rule r : p.getRules()) 
		{
			//check if relation can be derived
			if ( true == r.getHead().getName().equals( this.getName() ) )
			{
				retVal = false;
			}
		}
		
		return retVal;
	}
	
	
	
	public String toString ()
	{
		return super.toString();
	}



	/*@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((patternChildren == null) ? 0 : patternChildren.hashCode());
		result = prime * result + (isTransChild ? 1231 : 1237);
		result = prime * result + ((newName == null) ? 0 : newName.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + wildCardIdx;
		return result;
	}*/



	/*	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PatternNode other = (PatternNode) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (isTransChild != other.isTransChild)
			return false;
		if (newName == null) {
			if (other.newName != null)
				return false;
		} else if (!newName.equals(other.newName))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (wildCardIdx != other.wildCardIdx)
			return false;
		return true;
	}*/




}
