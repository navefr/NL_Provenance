package Basics;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;



public class Rule implements Comparable<Rule>
{
	
	Atom Head;
	
	Body Body = new Body();
    
	double weight;
    
    boolean isFullyInst = false;
    
    int derivedInlevel = 0;
    
    Atom restricted;
    
    boolean reachable;
	
    
    
	public Rule (){}
    
	public Rule (Atom head, double weight,  Atom... body)
    {
    	this.Head = head;
    	this.weight = weight;
    	for (Atom atom : body) 
		{
			this.Body.getAtoms().add(atom);
	    }
    }
	
	
	
	public Rule (Atom head, double weight,  Vector<Atom> body)
    {
    	this.Head = head;
    	this.weight = weight;
    	this.Body.setAtoms(body);
    }
	
	
	public Rule (Atom head, double weight,  List<Atom> body)
    {
    	this.Head = head;
    	this.weight = weight;
    	this.Body.setAtoms(body);
    }
	
	
	public Rule (Atom head, double weight,  Body body)
    {
    	this.Head = new Atom(head);
    	this.weight = weight;
    	this.Body = body;//new Body(body);
    }
	
	
	
	/*************************************************************************************************************/
	/** Title: Rule																				
	/** Description: Copy constructor				
	/*************************************************************************************************************/
	
	public Rule (Rule other)
	{
		for (Atom atom : other.Body.getAtoms()) 
		{
			this.Body.getAtoms().add(new Atom(atom));
		}
		
		this.Head = new Atom(other.Head);
		this.weight = other.weight;
		this.isFullyInst = other.isFullyInst;
		this.restricted = other.restricted;
	}
	
	
	
    public Body getBody() 
    {
		return Body;
	}
    
    
    
	public void setBody(Atom... body) 
	{
		for (Atom P : body) 
		{
			Body.getAtoms().add(P);
	    }
	}
	
	public void setBody(ArrayList<Atom> body) 
	{
		Body.getAtoms().clear();
		for (Atom atom : body) 
		{
			Body.getAtoms().add(new Atom(atom));
		}
	}
	
	
	
	public Atom getHead() 
	{
		return Head;
	}
	
	
	
	public void setHead(Atom head) 
	{
		Head = head;
	}
	
	
	
	public double getWeight() 
	{
		return weight;
	}
	
	
	
	public void setWeight(double weight)
	{
		this.weight = weight;
	}
	
	
	public boolean isFullyInst() 
    {
		this.CheckFullyInst();
		return isFullyInst;
	}

	
	
	public boolean getIsFullyInst() 
    {
		return isFullyInst;
	}
	
	
	
	
	/*public boolean isUsedAlready() 
	{
		return usedAlready;
	}

	
	
	public void setUsedAlready(boolean usedAlready) 
	{
		this.usedAlready = usedAlready;
	}*/
	
	

	public int getDerivedInlevel() 
	{
		return derivedInlevel;
	}



	public void setDerivedInlevel(int derivedInlevel) 
	{
		this.derivedInlevel = derivedInlevel;
	}
	
	
	public Atom getRestricted() 
	{
		return restricted;
	}



	public void setRestricted(Atom restricted) 
	{
		this.restricted = restricted;
	}

    

	
	
	public boolean isReachable() 
	{
		return reachable;
	}

	public void setReachable(boolean reachable) 
	{
		this.reachable = reachable;
	}

	/*************************************************************************************************************/
	/** Title: CheckFullyInst																				
	/** Description: Checks if the atom is fully inst. with constants or are there still vars in it				
	/*************************************************************************************************************/
	
	public void CheckFullyInst()
	{
		boolean retVal = this.Head.isFullyInst() ? true : false;
		for (Atom atom : this.Body.getAtoms()) 
		{
			if (false == atom.isFullyInst())
			{
				retVal = false;
				break;
			}
		}
		
		this.isFullyInst = retVal;
	}

	
	
	/*************************************************************************************************************/
	/** Title: SwapVarInRule																				
	/** Description: Swaps two Protons in the same location in the inputed atom. 
	/** useful for inst. of constants in atoms
	/*************************************************************************************************************/
	
	public void SwapVarInRule (Proton oldP, Proton newP)
	{
		if ( true == this.Head.getParams().contains(oldP) )
		{
			Head.SwapInAtom(oldP, newP);
		}
		
		for (Atom atom : this.Body.getAtoms()) 
		{
			if ( true == atom.getParams().contains(oldP) ) 
			{
				atom.SwapInAtom(oldP, newP);
			}
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: SwapToInstAtomAndPartiallyInst																				
	/** Description: Use for inst. atom with constants in rule and updating the rule
	/*************************************************************************************************************/
	
	public void SwapToInstAtomAndPartiallyInst (Atom oldA, Atom newA)
	{
		if (null == this.restricted || false == newA.equals(this.restricted)) 
		{
			for (int i = 0; i < newA.getParams().size(); ++i)
			{
				if (oldA.getParams().elementAt(i) instanceof Var && newA.getParams().elementAt(i) instanceof Constant) 
				{
					Proton var = oldA.getParams().elementAt(i);
					Proton constant = newA.getParams().elementAt(i);
					this.SwapVarInRule(var, constant);
				}
			}
			
			for (Atom atom : this.Body.getAtoms()) 
			{
				atom.isFullyInst();
				if (atom.equals(newA) && newA.isFact()) 
				{
					atom.setFact(true);
				}
			}
			
			this.isFullyInst();
		}
		
		/*else
		{
			System.out.println("Cannot instantiate " + newA + ". It is restricted in rule");
		}*/
	}	
	
	
	
	/*************************************************************************************************************/
	/** Title: Uninstantiatated																				
	/** Description: checks if the rule is completely Uninstantiatated
	/*************************************************************************************************************/
	
	public boolean Uninstantiatated ()
	{
		return Head.Uninstantiatated() && Body.Uninstantiatated();	
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: Isomorphic																				
	/** Description: checks if the rule is iso to other rule
	/*************************************************************************************************************/
	
	public boolean Isomorphic (Rule other)
	{
		boolean retVal = false;
		if (Uninstantiatated() && other.Uninstantiatated()) 
		{
			retVal = IsomorphicHelper(other);
		}
		
		else if (false == Uninstantiatated() && false == other.Uninstantiatated())
		{
			retVal = IsomorphicHelper(other);
		}
		
		return retVal;
	}
	
	
	/*************************************************************************************************************/
	/** Title: IsomorphicHelper																				
	/** Description: 
	/*************************************************************************************************************/
	
	private boolean IsomorphicHelper (Rule other)
	{
		boolean retVal = false;
		if (Head.getName().equals(other.Head.getName()) && Body.IdenticalAtomNames(other.Body)) 
		{
			Rule copy = new Rule (this);
			for (int j = 0; j < copy.Head.getParams().size(); j++) 
			{
				Proton thisParam = copy.Head.getParams().get(j);
				Proton otherParam = other.Head.getParams().get(j);
				copy.SwapVarInRule(thisParam, otherParam);
			}
			
			for (int i = 0; i < copy.Body.getAtoms().size(); i++) 
			{
				for (int j = 0; j < copy.Body.getAtoms().get(i).getParams().size(); j++) 
				{
					Proton thisParam = copy.Body.getAtoms().get(i).getParams().get(j);
					Proton otherParam = other.Body.getAtoms().get(i).getParams().get(j);
					copy.SwapVarInRule(thisParam, otherParam);
				}
			}
			
			//SameVarLocations(copy, other);
			if (true == copy.equals(other)) 
			{
				retVal = true;
			}
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: IsomorphicHelper																				
	/** Description: 
	/*************************************************************************************************************/
	
	private boolean SameVarLocations (Rule copy, Rule other)
	{
		boolean retVal = true;
		for (int i = 0; i < copy.Body.getAtoms().size(); i++) 
		{
			for (int j = 0; j < copy.Body.getAtoms().get(i).getParams().size(); j++) 
			{
				Proton thisParam = copy.Body.getAtoms().get(i).getParams().get(j);
				Proton otherParam = other.Body.getAtoms().get(i).getParams().get(j);
				if (false == thisParam.equals(otherParam)) 
				{
					retVal = false;
					break;
				}
			}
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: CotainedInBody																				
	/** Description: checks if a group of atoms is contained in body of rule. use in pattern
	/*************************************************************************************************************/
	
	public boolean CotainedInBody (Vector<Atom> atomVec)
	{
		boolean retVal = true;
		if (null == atomVec) 
		{
			retVal = false;
		}
		else
		{
			for (Atom atom : atomVec) 
			{
				if (false == this.Body.getAtoms().contains(atom))
				{
					retVal = false;
				}
			}
		}
		
		return retVal;
	}
	
	
	
	public String toString ()
	{
		String retVal = this.Head.toString();
		retVal = retVal.substring(0,retVal.length()-1) + " :-";
		for (Atom atom : this.Body.getAtoms()) 
		{
			retVal += " " + atom.toString().substring(0,atom.toString().length()-1) + ",";
		}
		
		retVal = retVal.substring(0,retVal.length()-1) + ".";
		return retVal;
	}
	
	
	public String toString (int i)
	{
		String retVal = this.Head.toStringHead(i);
		retVal = retVal.substring(0,retVal.length()-1) + " :-";
		for (Atom atom : this.Body.getAtoms()) 
		{
			retVal += " " + atom.toStringHead(i).substring(0,atom.toStringHead(i).length()-1) + ",";
		}
		
		retVal = retVal.substring(0,retVal.length()-1) + ".";
		return retVal;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Body == null) ? 0 : Body.getAtoms().hashCode());
		result = prime * result + ((Head == null) ? 0 : Head.hashCode());
		result = prime * result + (isFullyInst ? 1231 : 1237);
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
		Rule other = (Rule) obj;
		if (Body == null) {
			if (other.Body != null)
				return false;
		} else if (!Body.equals(other.Body))
			return false;
		if (Head == null) {
			if (other.Head != null)
				return false;
		} else if (!Head.equals(other.Head))
			return false;
		if (isFullyInst != other.isFullyInst)
			return false;
		if (Double.doubleToLongBits(weight) != Double
				.doubleToLongBits(other.weight))
			return false;
		return true;
	}

	
	
	public int compareTo(Rule other) 
	{
		return this.weight > other.getWeight() ? -1 : this.weight < other.getWeight() ? 1 : 0;
	}

}
