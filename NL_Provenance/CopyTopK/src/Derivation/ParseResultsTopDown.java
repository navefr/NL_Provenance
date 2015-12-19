package Derivation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.Set;

import Basics.*;

public class ParseResultsTopDown 
{
	DeriveByProgramTopDown programDeriver;
	
	Set<Atom> atomsToDerive = new HashSet<Atom>();
	
	public ParseResultsTopDown (Rule ... irs)
	{
		this.programDeriver = new DeriveByProgramTopDown (this.atomsToDerive, irs);
	}
	
	
	
	public ParseResultsTopDown (Program p)
	{
		this.programDeriver = new DeriveByProgramTopDown (this.atomsToDerive, p);
	}
	
	
	
	public DeriveByProgramTopDown getProgramDeriver() 
	{
		return programDeriver;
	}



	public void setProgramDeriver(DeriveByProgramTopDown programDeriver) 
	{
		this.programDeriver = programDeriver;
	}



	public Set<Atom> getAtomsToDerive() 
	{
		return atomsToDerive;
	}



	public void Set(Set<Atom> atomsToDerive) 
	{
		this.atomsToDerive = atomsToDerive;
	}



	/*public Map<Atom, HashSet<Body>> getProvenance() 
	{
		return provenance;
	}*/


	
	
	/*************************************************************************************************************/
	/** Title: ParseResults																				
	/** Description: One iteration of top down with one atom			
	/*************************************************************************************************************/
	
	public void ParseResults ()
	{
		Atom instAtom = this.atomsToDerive.iterator().next();
		this.atomsToDerive.remove(instAtom);
		this.programDeriver.GetDerivationsForAtomTopDown(instAtom);
		//System.out.println("prov size: " + GetProvSize () + ", atoms to derive: " + this.atomsToDerive.size());
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: ClearProvenanceFromIrrelevantBodies																				
	/** Description: clears the prov. vector from atoms that cannot be derived		
	/*************************************************************************************************************/
	
	/*public void ClearProvenanceFromIrrelevantBodies()
	{
		Map<Atom, Vector<Body>> toBeRemoved = new HashMap<Atom, Vector<Body>> ();
		for (Atom key : KeyMap.getInstance().Values()) 
		{
			Vector<Body> bodies = new Vector<Body>();
			for (Body body : Provenance.getInstance().Get(key)) 
			{
				for (Atom atom : body.getAtoms()) 
				{
					if (true == IsNotDerivable(atom))
					{
						bodies.add(body);
					}
				}
			}
			
			toBeRemoved.put(key, bodies);
		}
		
		for (Atom key : toBeRemoved.keySet()) 
		{
			Provenance.getInstance().Get(key).removeAll(toBeRemoved.get(key));
		}
	}
*/
	
	
	/*************************************************************************************************************/
	/** Title: HasBeenDerived																				
	/** Description: Checks if atom is already in prov. or atom is already in DB  				
	/*************************************************************************************************************/
	
	/*public boolean HasBeenDerived (Atom atom)
	{
		return KeyMap.getInstance().Contains(atom) || MemDB.getInstance().ContainedInTable(atom);//DB.getInstance().ContainedInTable(atom);
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: IsNotDeriveble																				
	/** Description: Checks if atom cannot be derived  				
	/*************************************************************************************************************/
	
	/*public boolean IsNotDerivable (Atom atom)
	{
		return !MemDB.getInstance().ContainedInTable(atom) !DB.getInstance().ContainedInTable(atom) && atom.IsAtomRelationEdb(this.programDeriver.p);
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: AddToAtomsNeedToBeDerived																				
	/** Description: select atoms that need to be derived and add them to vector		
	/*************************************************************************************************************/
	
	/*public void AddToAtomsNeedToBeDerived (Set<Atom> atomsToDerive)
	{	
		for (Atom atom : atomsToDerive) 
		{
			if (false == KeyMap.getInstance().Values().contains(atom)) 
			{
				this.atomsToDerive.add(atom);
			}
		}
	}*/
	

	
	/*************************************************************************************************************/
	/** Title: LeaveOnlyPossibleDerivations																				
	/** Description: clears the final prov. vector from atoms that cannot be derived		
	/*************************************************************************************************************/
	
	public void LeaveOnlyStableDerivations()
	{
		Map<Atom, Vector<Body>> bodiesToRemove = new HashMap<Atom, Vector<Body>> ();
		Set<Atom> keysToRemove = new HashSet<Atom>();
		int markedAtCurrentStep = 1;
		
		while (markedAtCurrentStep > 0)
		{
			markedAtCurrentStep = MarkAtomsWithStableDerivation();
		}
		
		for (Atom key : KeyMap.getInstance().Values()) 
		{
			if (true == key.isStable()) 
			{
				Vector<Body> bodies = new Vector<Body>();
				for (Body body : Provenance.getInstance().Get(key)) 
				{
					if (false == body.IsBodyStable()) 
					{
						bodies.add(body);
					}
				}
				
				bodiesToRemove.put(key, bodies);
			}
			
			else
			{
				keysToRemove.add(key);
			}
		}
		
		Provenance.getInstance().KeySet().removeAll(keysToRemove);
		KeyMap.getInstance().Values().removeAll(keysToRemove);
		
		for (Atom key : bodiesToRemove.keySet()) 
		{
			Provenance.getInstance().Get(key).removeAll(bodiesToRemove.get(key));
		}
		
		//UpdateDB();
	}

	
	
	/*************************************************************************************************************/
	/** Title: UpdateDB																				
	/** Description: add newly derived facts to DB			
	/*************************************************************************************************************/
	
	public void UpdateDB ()
	{
		for (Atom atom : KeyMap.getInstance().getMap().values()) 
		{
			//DB.getInstance().Update(atom);
			MemDB.getInstance().Update(atom);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: LeaveOnlyRelevantDerivations																				
	/** Description: clears the final prov. vector from atoms that are not used in the derivation of root		
	/*************************************************************************************************************/
	
	/*public void LeaveOnlyRelevantDerivations (Atom root)
	{
		List<Atom> toBeRemoved = new ArrayList<Atom>();
		List<String> keysToBeRemoved = new ArrayList<String>();
		MarkRelevantProv(root);
		for (Atom key : KeyMap.getInstance().Values()) 
		{
			if (false == key.isRelevantForDerivationTopDown()) 
			{
				toBeRemoved.add(key);
			}
		}
		
		Provenance.getInstance().KeySet().removeAll(toBeRemoved);
		KeyMap.getInstance().KeySet().removeAll(keysToBeRemoved);
		for (Atom atom : toBeRemoved) 
		{
			this.provenance.remove(atom);
		}
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: IsDerivationStable																				
	/** Description: Checks if the atom can be derived from the DB and program or just has an endless cycle in the prov.		
	/*************************************************************************************************************/
	
	public void HasStableDerivation (Atom atom)
	{
		for (Body body : Provenance.getInstance().Get(atom)) 
		{				
			if (true == body.IsBodyStable()) //the entire body is consisted of stable atoms 
			{
				atom.setStable(true);
				break;
			}
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MarkAtomsWithStableDerivation																				
	/** Description: Checks if the atoms in prov. can be derived from the DB and program or just have an endless cycle in the prov.		
	/*************************************************************************************************************/
	
	public int MarkAtomsWithStableDerivation ()
	{
		int markedAtCurrentStep = 0;
		for (Atom key : KeyMap.getInstance().Values()) 
		{
			if (false == key.isStable()) 
			{
				HasStableDerivation(key);
				if (true == key.isStable()) 
				{
					markedAtCurrentStep++;
				}
			}
		}
		
		return markedAtCurrentStep;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MarkRelevantProvPerAtom																				
	/** Description: Marks all atoms that derive this root		
	/*************************************************************************************************************/
	
	/*public void MarkRelevantProvPerAtom (List<Atom> queue, Map<String, Atom> keyMap)
	{
		Atom toMark = queue.remove(0);
		toMark.setRelevantForDerivationTopDown(true);
		if (false == toMark.isFact() && false == keyMap.get(toMark.toString()).isRelevantForDerivationTopDown())
		{
			Atom key = keyMap.get(toMark.toString());
			key.setRelevantForDerivationTopDown(true);
			for (Body body : Provenance.getInstance().Get(key)) 
			{
				for (Atom atom : body.getAtoms()) 
				{
					if (false == atom.isRelevantForDerivationTopDown()) 
					{
						queue.add(atom);
					}
				}
			}
		}
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: MarkRelevantProv																				
	/** Description: removes all atoms that aren't relevant for the derivation tree of this fact		
	/*************************************************************************************************************/
	
	/*public void MarkRelevantProv (Atom root)
	{		
		List<Atom> queue = new ArrayList<Atom>();
		queue.add(root);
		
		Map<String, Atom> keyMap = new HashMap<String, Atom>(); 
		for (Atom key : Provenance.getInstance().KeySet()) //insert all keys to a hash map for easy search 
		{
			keyMap.put(key.toString(), key);
		}
		
		while (false == queue.isEmpty())
		{
			MarkRelevantProvPerAtom(queue, keyMap);
		}
	}*/
	
}
