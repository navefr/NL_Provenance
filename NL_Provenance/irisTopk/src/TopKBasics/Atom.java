package TopKBasics;
import java.util.List;
import java.util.Vector;

import org.deri.iris.api.basics.ITuple;
import Top1.DerivationTree;

public class Atom
{
	//Vector<Proton> params = new Vector<Proton>();
	
	ITuple tuple;
	
	String name;
    
	//boolean isFullyInst = false;
	
	boolean isFact;// = false
	
	boolean isTopKUpdated;

	//protected Vector<Atom> children;
	
	//Vector<Atom> parents;
	
	DerivationTree tree;
	
	boolean foundTop1;
	
	public Atom () {}
	
	
	
	/*public Atom (String iName, boolean iInst, boolean iFact, Proton ... iparams)
	{
		this.name = iName;
		this.isFact = iFact;
		this.isFullyInst = iInst;
		for (Proton param : iparams) 
		{
			this.params.add(param);
		}
	}

	
	
	public Atom (String iName, boolean iFact, Proton ... iparams)
	{
		this.name = iName;
		this.isFact = iFact;
		for (Proton param : iparams) 
		{
			this.params.add(param);
		}
	}
	
	
	
	public Atom (String iName, Proton ... iparams)
	{
		this.name = iName;
		for (Proton param : iparams) 
		{
			this.params.add(param);
		}
	}
	
	
	
	public Atom (String iName)
	{
		this.name = iName;
	}*/

	
	public Atom (String name, ITuple tuple)
	{
		this.name = name;
		/*for (ITerm tupleMem : tuple) 
		{
			String paramName = tupleMem.toString();
			Constant param = new Constant(paramName, "");
			this.params.add(param);
		}*/
		this.tuple = tuple;
		//this.isFullyInst = true;
		this.isFact = false;
	}
	
	
	/*************************************************************************************************************/
	/** Title: Atom																				
	/** Description: Copy constructor				
	/*************************************************************************************************************/
	
	/*public Atom (Atom other)
	{
		this.name = other.name;
		for (Proton param : other.params) 
		{
			this.params.add(param);
		}
		
		this.isFullyInst = other.isFullyInst;
		this.isFact = other.isFact;
		//this.derivedInLevel = other.derivedInLevel;
	}*/
	
	
	
	/*public boolean isFullyInst()
	{
		//this.CheckFullyInst();
		return isFullyInst;
	}
	
	
	public boolean getIsFullyInst()
	{
		return isFullyInst;
	}*/
	
	
	public boolean isFact()
	{
		return isFact;
	}
	
	

	public void setFact(boolean isFact)
	{
		this.isFact = isFact;
	}

	/*public Vector<Proton> getParams() 
	{
		return params;
	}*/

	
	
	/*public void setParams(Proton ... iparams) 
	{
		for (Proton param : iparams) 
		{
			this.params.add(param);
		}
	}*/
	
	
	
	public String getName() 
	{
		return name;
	}
	
	
	
	public void setName(String name) 
	{
		this.name = name;
	}

	

	
	/*public boolean isStable() 
	{
		return isStableTopDown;
	}



	public void setStable(boolean isStable) 
	{
		this.isStableTopDown = isStable;
	}
	
	
	public boolean isRelevantForDerivationTopDown() 
	{
		return isRelevantForDerivationTopDown;
	}



	public void setRelevantForDerivationTopDown(boolean isRelevantForDerivationTopDown) 
	{
		this.isRelevantForDerivationTopDown = isRelevantForDerivationTopDown;
		if (null != this.keyInProv && isRelevantForDerivationTopDown != this.keyInProv.isRelevantForDerivationTopDown()) 
		{
			this.keyInProv.setRelevantForDerivationTopDown(isRelevantForDerivationTopDown);
		}	
	}*/


	/*public Vector<Atom> getParents() 
	{
		return parents;
	}


	public void AddParent (Atom parent)
	{
		if (null == this.parents) 
		{
			this.parents  = new Vector<Atom>();
		}
		this.parents.add(parent);
	}*/
	

	public DerivationTree getTree() 
	{
		return tree;
	}



	public void setTrees(DerivationTree itrees) 
	{	
		this.tree = itrees;
	}


	public void AddTree(DerivationTree tree) 
	{
		/*if (null == this.tree) 
		{
			this.tree = new DerivationTree>();
		}*/
		
		this.tree = tree;
	}
	

	/*public String getType() 
	{
		return type;
	}



	public void setType(String type)
	{
		this.type = type;
	}



	public Rule getRuleUsed() 
	{
		return ruleUsed;
	}



	public void setRuleUsed(Rule ruleUsed) 
	{
		this.ruleUsed = ruleUsed;
	}*/
	

	/*public Vector<Atom> getChildren() 
	{
		return children;
	}



	public void setChildren(Vector<Atom> children) 
	{
		this.children = children;
	}
	
	
	public void AddChild(Atom child) 
	{
		if (null == this.getChildren()) 
		{
			this.children  = new Vector<Atom>();
		}
		
		this.children.add(child);
	}*/
	

	public boolean isTopKUpdated() 
	{
		return isTopKUpdated;
	}



	public void setTopKUpdated(boolean isTopKUpdated) 
	{
		this.isTopKUpdated = isTopKUpdated;
	}
	
	
	
	/*public Atom getKeyInProv() 
	{
		return keyInProv;
	}



	public void setKeyInProv(Atom keyInProv) 
	{
		this.keyInProv = keyInProv;
	}

	
	
	public boolean areAllBodiesRelevant() 
	{
		return allBodiesRelevant;
	}



	public void setAllBodiesRelevant(boolean allBodiesRelevant) 
	{
		this.allBodiesRelevant = allBodiesRelevant;
	}
	
	
	public boolean isTreesChangedLastIteration() 
	{
		return treesChangedLastIteration;
	}



	public void setTreesChangedLastIteration(boolean treesChangedLastIteration) 
	{
		this.treesChangedLastIteration = treesChangedLastIteration;
	}*/

	
	
	public boolean didFindTop1() 
	{
		return foundTop1;
	}



	public void setFoundTop1(boolean foundTop1) 
	{
		this.foundTop1 = foundTop1;
	}

	
	
	/*public Proton getRestrictedConst() 
	{
		return restrictedConst;
	}



	public void setRestrictedConst(Proton restrictedConst) 
	{
		this.restrictedConst = restrictedConst;
	}



	public int getRestrictIdx() 
	{
		return restrictIdx;
	}



	public void setRestrictIdx(int restrictIdx) 
	{
		this.restrictIdx = restrictIdx;
	}*/

	

	/*************************************************************************************************************/
	/** Title: AddParam																				
	/** Description: Add Param to atom				
	/*************************************************************************************************************/
	
	/*public void AddParam (Proton p)
	{
		this.params.add(p);
	}*/


	/*************************************************************************************************************/
	/** Title: ParamsSameCategory																				
	/** Description: 				
	/*************************************************************************************************************/
	
	/*public boolean ParamsSameCategory ()
	{
		boolean retVal = true;
		String cat = params.get(0).getCategory();
		for (Proton p : params) 
		{
			if (false == p.getCategory().equals(cat)) 
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}*/
	

	/*************************************************************************************************************/
	/** Title: CheckFullyInst																				
	/** Description: Checks if the atom is fully inst. with constants or are there still vars in it				
	/*************************************************************************************************************/
	
	/*public void CheckFullyInst()
	{
		boolean retVal = true;
		for (Proton param : this.params) 
		{
			if (param instanceof  Var)
			{
				retVal = false;
				break;
			}
		}
		
		this.isFullyInst = retVal;
	}*/
	

	
	/*************************************************************************************************************/
	/** Title: Uninstantiatated																				
	/** Description: checks if the rule is completely Uninstantiatated
	/*************************************************************************************************************/
	
	/*public boolean Uninstantiatated ()
	{
		boolean retVal = true;
		for (Proton p : this.params) 
		{
			if (p instanceof Constant) 
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}*/
	

	/*************************************************************************************************************/
	/** Title: SwapInAtom																				
	/** Description: Swaps two Protons in the same location in the atom. useful for inst. of constants in atoms				
	/*************************************************************************************************************/
	
	/*public void SwapInAtom (Proton oldP, Proton newP)
	{
		for (Proton param : this.params) 
		{
			if (param.equals(oldP))
			{
				int idx = this.params.indexOf(param);
				this.params.set(idx, newP);
			}
		}
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: HasSameRelevantFacts																				
	/** Description: Check that 2 atoms have the same relevant facts in DB. used for bottom up by rule 				
	/*************************************************************************************************************/
	
	/*public boolean HasSameRelevantFacts (Atom other)
	{
		boolean retVal = ( this.name.equals(other.getName()) );
		
		if (true == retVal) 
		{
			for (int i = 0; i < this.params.size(); i++) 
			{
				Proton thisP = this.params.elementAt(i);
				Proton otherP = other.getParams().elementAt(i);
				if (thisP instanceof Var && otherP instanceof Constant)
				{
					retVal = false;
					break;
				}
				if (thisP instanceof Constant && otherP instanceof Var)
				{
					retVal = false;
					break;
				}
				if (thisP instanceof Constant && otherP instanceof Constant) 
				{
					if (false == thisP.getName().equals(otherP.getName())) 
					{
						retVal = false;
						break;
					}
				}
			}
		}
			
		return retVal;
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: IsAtomRelationEdb																				
	/** Description: checks if the relation can be derived by one of the program rules 			
	/*************************************************************************************************************/
	
	/*public boolean IsAtomRelationEdb(Program p) 
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
	}*/
	
	
	
	
	/*************************************************************************************************************/
	/** Title: AllChildrenFacts																				
	/** Description:  			
	/*************************************************************************************************************/
	
	/*public boolean AllChildrenFacts()
	{
		boolean retVal = true;
		for (Atom child : this.children) 
		{
			if (false == child.isFact())
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: FittsPartialInst																				
	/** Description:  			
	/*************************************************************************************************************/
	
	/*public boolean FittsPartialInst(Atom other)
	{
		boolean retVal = true;
		retVal = ( false == this.name.equals(other.getName()) ) ? false : retVal;
		retVal = ( this.params.size() != other.getParams().size() ) ? false : retVal;
		retVal = ( false == EqualConsts(other) ) ? false : retVal;
		return retVal;
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: EqualConsts																				
	/** Description: Checks that all constant in two atoms are in the same location  			
	/*************************************************************************************************************/
	
	/*public boolean EqualConsts (Atom other)
	{
		boolean retVal = true;
		for (int i = 0; i < this.params.size(); ++i) 
		{
			if (this.params.elementAt(i) instanceof Constant)
			{
				retVal = ( this.params.elementAt(i).equals(other.getParams().elementAt(i)) == false ) ? false : retVal;
			}
		}
		
		return retVal;
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: TreesContainBody																				
	/** Description: Checks if body belongs to some derivation tree already in atom  			
	/*************************************************************************************************************/
	
	/*public boolean TreesContainBody (Body body)
	{
		boolean retVal = false;
		if (null != this.trees) 
		{
			for (DerivationTree tree : this.trees) 
			{
				if (tree.getBodyInProv().equals(body))
				{
					retVal = true;
					break;
				}
			}
		}
		
		return retVal;
	}*/
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isFact ? 1231 : 1237);
		//result = prime * result + (isFullyInst ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tuple == null) ? 0 : tuple.toString().hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		/*if (getClass() != obj.getClass())
			return false;*/
		Atom other = (Atom) obj;
		/*if (isFact != other.isFact)
			return false;*/
		/*if (isFullyInst != other.isFullyInst())
			return false;*/
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tuple == null) {
			if (other.tuple != null)
				return false;
		} else if (!tuple.toString().equals(other.tuple.toString()))
			return false;
		return true;
	}



	public String toString ()
	{
		String retVal = this.name + this.tuple + ".";
		/*for (Proton proton : this.params) 
		{
			retVal += proton.getName() + ",";
		}
		retVal = retVal.substring(0,retVal.length()-1) + ").";*/
		return retVal.intern();
	}
}
