package Derivation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import TopK.DerivationTree;
import TopK.EquationTopK;
import Basics.*;

public class ParseResultsBottomUp 
{
	DeriveByProgramBottomUp programDeriver;
	
	EquationTopK topk;
	
	Set<Atom> atomsForDbOnline = new HashSet<Atom>();

	Set<Atom> addedInstAtoms = new HashSet<Atom>();
	
	DerivationTree bestTreeThisStep;

	public ParseResultsBottomUp (Rule ... irs)
	{
		this.programDeriver = new DeriveByProgramBottomUp (irs);
	}
	
	
	
	public ParseResultsBottomUp (Program p)
	{
		this.programDeriver = new DeriveByProgramBottomUp (p);
	}

	
	
	public ParseResultsBottomUp (Program p, EquationTopK itopk)
	{
		this.topk = itopk;
		this.programDeriver = new DeriveByProgramBottomUp (p, this.topk, this.atomsForDbOnline);
	}


	
	public Set<Atom> getAddedInstAtoms() 
	{
		return addedInstAtoms;
	}

	

	public DeriveByProgramBottomUp getProgramDeriver() 
	{
		return programDeriver;
	}



	public void setProgramDeriver(DeriveByProgramBottomUp programDeriver)
	{
		this.programDeriver = programDeriver;
	}



	/*************************************************************************************************************/
	/** Title: ParseResults																				
	/** Description: finds all newly derived atoms from this step and adds them to this.addedInstAtoms			
	/*************************************************************************************************************/

	public boolean ParseResults (boolean forIntersection, boolean online)
	{	
		//long startTime = System.currentTimeMillis();
		boolean retVal = false;
		//int oldSize = Provenance.getInstance().GetProvSize();
		//Set<Atom> compareVec = new HashSet<Atom> (KeyMap.getInstance().Values());//(AtomsDerived.getInstance().getAtomSet());
		this.programDeriver.DeriveFromProgramIterationBottomUp(forIntersection, online);		
		//this.addedInstAtoms = new HashSet<Atom> (KeyMap.getInstance().Values());//(AtomsDerived.getInstance().getAtomSet());
		
		/*if (false == compareVec.isEmpty() || true == this.addedInstAtoms.isEmpty()) //see if new atoms have been derived in this iteration this.programDeriver.addedInstAtomsInLastIteration.isEmpty())
		{
			retVal = (compareVec.size() == this.addedInstAtoms.size());
			if (true == online) 
			{
				retVal = this.programDeriver.treesWaiting.isEmpty() && retVal;
			}
		}
		
		
		if (false == retVal && false == online) 
		{
			this.addedInstAtoms.removeAll(compareVec); //for UpdateDB()
		}*/
		this.bestTreeThisStep = this.programDeriver.bestTreeThisStep;
		this.addedInstAtoms = new HashSet<Atom> (this.programDeriver.getAddedInstAtomsInLastIteration());
		if (false == forIntersection) 
		{
			UpdateDB(online);
		}
		
		//retVal = oldSize == Provenance.getInstance().GetProvSize();this.addedInstAtoms.isEmpty()
		retVal = this.addedInstAtoms.isEmpty();
		if (true == online) 
		{
			retVal = this.programDeriver.treesWaiting.isEmpty() && retVal;
		}
		
		
		/*System.out.println("size of map: " + KeyMap.getInstance().Size());
		if (online) {
			System.out.println("size of trees waiting: " + this.programDeriver.treesWaiting.size());
		}
		else
		{
			System.out.println("size of atoms added: " + this.programDeriver.addedInstAtomsInLastIteration.size());
		}*/
		/*if (false == online) 
		{
			//KeyMap.getInstance().ClearMap();
			AtomsDerived.getInstance().ClearSet();
			//System.out.println(KeyMap.getInstance().getMap());
		}*/
		
		
		/*long end = System.currentTimeMillis();
		if ((end-startTime) > 100) 
		{
			System.out.println("ParseResultsBottomUp:: time for seminaive layer: " + (end-startTime));
			System.out.println("ParseResultsBottomUp:: num of added inst atoms: " + this.addedInstAtoms);
		}*/
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: UpdateDB																				
	/** Description: add newly derived facts to DB			
	/*************************************************************************************************************/
	
	public void UpdateDB (boolean online)
	{
		Set<Atom> atomSet = (online) ? this.atomsForDbOnline : this.addedInstAtoms;
		for (Atom atom : atomSet) 
		{
			//DB.getInstance().Update(atom);
			MemDB.getInstance().Update(atom);
			/*if (this.programDeriver.relevantKeysForMap.containsKey(atom.getName())) 
			{
				for (Atom key : this.programDeriver.relevantKeysForMap.get(atom.getName())) 
				{
					if (key.EqualConsts(atom)) 
					{
						this.programDeriver.previouslyFoundRelevants.get(key).add(atom);
					}
				}
			}*/
		}
		
		this.atomsForDbOnline.clear();
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: Reset																				
	/** Description: resets the parameters for next step of the program			
	/*************************************************************************************************************/
	
	public void Reset ()
	{
		this.addedInstAtoms.clear();
	}
	
}
