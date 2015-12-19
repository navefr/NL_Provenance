package TopKBasics;

import java.util.Set;
import java.util.Vector;

import Top1.DerivationTree;

public class Body 
{
	
	Vector<Atom> atoms = new Vector<Atom>();
	
	//int derivedInlevel = 0;
	
	//Rule ruleUsed;
	
	double ruleWeight;
	
	boolean isTopKUpdated;
	
	public Body () {}
	
	
	public Body (Vector<Atom> iatoms) 
	{
		this.atoms = iatoms;
	}
	
	
	public Body (Atom ... iatoms) 
	{
		for (Atom atom : iatoms) 
		{
			this.atoms.add(atom);
		}
	}
	

	/*************************************************************************************************************/
	/** Title: Body																				
	/** Description: Copy constructor				
	/*************************************************************************************************************/
	
	public Body (Body other)
	{
		for (Atom atom : other.getAtoms()) 
		{
			this.atoms.add(atom);
		}
		
		//this.derivedInlevel = other.getDerivedInlevel();
		//this.ruleUsed = other.getRuleUsed();
		this.ruleWeight = other.getRuleWeight();
	}

	
	
	/*public int getDerivedInlevel() 
	{
		return derivedInlevel;
	}


	public void setDerivedInlevel(int derivedInlevel) 
	{
		this.derivedInlevel = derivedInlevel;
	}*/


	public Vector<Atom> getAtoms() 
	{
		return atoms;
	}


	public void setAtoms(Vector<Atom> atoms) 
	{
		this.atoms = atoms;
	}
	
	
	public void addAtom(Atom atom) 
	{
		this.atoms.add( atom );
	}


	/*public Rule getRuleUsed() 
	{
		return ruleUsed;
	}


	public void setRuleUsed(Rule ruleUsed) 
	{
		this.ruleUsed = ruleUsed;
	}*/
	
	
	public double getRuleWeight() 
	{
		return ruleWeight;
	}


	public void setRuleWeight(double ruleWeight) 
	{
		this.ruleWeight = ruleWeight;
	}

	
	
	public boolean isTopKUpdated() 
	{
		this.isTopKUpdated = ( true == this.isTopKUpdated ) ? true : CheckTopKUpdated();
		return isTopKUpdated;
		}
	
	
	

	public void setTopKUpdated (boolean isTopKUpdated) 
	{
		this.isTopKUpdated = isTopKUpdated;
	}


	/*************************************************************************************************************/
	/** Title: CheckTopKUpdated																	
	/** Description: Checks if TopK of each body atom is Updated  						
	/*************************************************************************************************************/
	
	public boolean CheckTopKUpdated()
	{
		boolean retVal = true;
		for (Atom atom : this.atoms) 
		{
			if (false == atom.isTopKUpdated()) 
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: LegalDerivation																	
	/** Description: Checks if TopK of each body atom is Updated  						
	/*************************************************************************************************************/
	
	public boolean LegalDerivation()
	{
		boolean retVal = true;
		for (Atom atom : atoms) 
		{
			if (null == atom) 
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetAtomNotFullyInst																	
	/** Description:  						
	/*************************************************************************************************************/
	
	/*public Atom GetAtomNotFullyInst()
	{
		Atom retVal = null;
		for (Atom atom : atoms) 
		{
			if (false == atom.isFullyInst()) 
			{
				retVal = atom;
			}
		}
		
		return retVal;
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: IsBodyStable																	
	/** Description: Checks if all atoms in body are stable  						
	/*************************************************************************************************************/
	
	/*public boolean IsBodyStable ()
	{
		boolean retVal = true;
		for (Atom bodyAtom : this.atoms) 
		{
			if (false == bodyAtom.isStable()) 
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: AllAtomsInDb																	
	/** Description: Checks if all body atoms are in DB  						
	/*************************************************************************************************************/
	
	/*public boolean AllAtomsInDb ()
	{
		boolean retVal = true;
		for (Atom atom : this.atoms) 
		{
			if (false == MemDB.getInstance().ContainedInTable(atom)) 
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: Uninstantiatated																				
	/** Description: checks if the rule is completely Uninstantiatated
	/*************************************************************************************************************/
	
	/*public boolean Uninstantiatated ()
	{
		boolean retVal = true;
		for (Atom a : this.atoms) 
		{
			if (false == a.Uninstantiatated()) 
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: AllAtomsHaveTopK																	
	/** Description: Checks that All Atoms Have TopK trees  						
	/*************************************************************************************************************/
	
	public boolean AllAtomsHaveTopK()
	{
		boolean retVal = true;
		for (Atom atom : this.atoms) 
		{
			if (null == atom.getTree())
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MakeAllAtomsRelevantAndPlaceKeyInQueue																	
	/** Description: Makes All Atoms Relevant for derivation  						
	/*************************************************************************************************************/
	
	/*public void MakeAllAtomsRelevantAndPlaceKeyInQueue(List<Atom> queue)
	{
		for (Atom atom : this.atoms) 
		{
			atom.setRelevantForDerivationTopDown(true);
			if (false == atom.isFact() && false == atom.areAllBodiesRelevant())//false == atom.getKeyInProv().areAllBodiesRelevant())
			{
				//atom.getKeyInProv().setAllBodiesRelevant(true);
				//queue.add(atom.getKeyInProv());
				atom.setAllBodiesRelevant(true);
				queue.add(atom);
			}
		}
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: MakeAllAtomsNotUpdated																	
	/** Description: Makes All Atoms to not indicate that their trees were Updated in the Last Iter  						
	/*************************************************************************************************************/
	
	/*public void MakeAllAtomsNotUpdated ()
	{
		for (Atom atom : this.atoms) 
		{
			atom.setTreesChangedLastIteration(false);
		}
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: HasAtomInTreesUpdatedLastIter																	
	/** Description: Checks if body Has Atom In treesUpdatedLastIter  						
	/*************************************************************************************************************/
	
	/*public boolean HasAtomInTreesUpdatedLastIter ()
	{
		boolean retVal = false;
		for (Atom atom : this.atoms) 
		{
			if (true == atom.isTreesChangedLastIteration())
			{
				retVal = true;
				break;
			}
		}
		
		return retVal;
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: HasNonFact																	
	/** Description: Checks if body Has Atom that is not fact  						
	/*************************************************************************************************************/
	
	public boolean HasNonFact ()
	{
		boolean retVal = false;
		for (Atom atom : this.atoms) 
		{
			if (false == atom.isFact()) 
			{
				retVal = true;
				break;
			}
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: SetTreeForFactsAtoms																	
	/** Description: Sets a derivation tree for Fact atoms  						
	/*************************************************************************************************************/
	
	public void SetTreeForFactsAtoms (Set<Atom> treesUpdatedLastIter)
	{
		for (Atom atom : this.atoms) 
		{
			if (true == atom.isFact())
			{
				DerivationTree curTree = new DerivationTree();
				curTree.setWeight(1);
				curTree.setDerivedFact(atom);
				atom.AddTree(curTree);
				atom.setTopKUpdated(true);
				treesUpdatedLastIter.add(atom);
			}
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: AreAllAtomsRelevant																	
	/** Description: Checks if All Atoms are Relevant for derivation  						
	/*************************************************************************************************************/
	
	/*public boolean AreAllAtomsRelevant ()
	{
		boolean retVal = true;
		for (Atom atom : this.atoms) 
		{
			if (false == atom.isRelevantForDerivationTopDown())
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: IdenticalAtoms																	
	/** Description: Returns true iff this has the same atoms as other  						
	/*************************************************************************************************************/
	
	public boolean IdenticalAtoms (Body other)
	{
		boolean retVal = ( this.atoms.size() == other.getAtoms().size() );
		if (true == retVal) 
		{
			for (int i = 0; i < this.atoms.size(); i++) 
			{
				if (false == this.atoms.elementAt(i).equals(other.getAtoms().elementAt(i)))
				{
					retVal = false;
					break;
				}
			}
		}
		
		return retVal;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((atoms == null) ? 0 : atoms.hashCode());
		/*result = prime * result + derivedInlevel;
		result = prime * result
				+ ((ruleUsed == null) ? 0 : ruleUsed.hashCode());*/
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
		Body other = (Body) obj;
		if (atoms == null) {
			if (other.atoms != null)
				return false;
		} else if (!atoms.equals(other.atoms))
			return false;
		/*if (derivedInlevel != other.derivedInlevel)
			return false;*/
		/*if (ruleUsed == null) {
			if (other.ruleUsed != null)
				return false;
		} else if (!ruleUsed.equals(other.ruleUsed))
			return false;*/
		return true;
	}

	
	public String toString()
	{
		return this.atoms.toString();
	}

}
