package Derivation;

import java.util.Set;

import Basics.Atom;
import Basics.KeyMap;
import Basics.Program;
import Basics.Provenance;
import Basics.Rule;

public class DeriveByProgramTopDown extends DeriveByProgram 
{
	
	Set<Atom> atomsToDerive;
	
	public DeriveByProgramTopDown (Set<Atom> atoms, Rule ... irs)
	{
		super(irs);
		this.atomsToDerive = atoms;
	}
	
	
	
	public DeriveByProgramTopDown (Set<Atom> atoms, Program p)
	{
		super(p);
		this.atomsToDerive = atoms;
	}

	
	
	public Set<Atom> getAtomsToDerive() 
	{
		return atomsToDerive;
	}



	/*************************************************************************************************************/
	/** Title: GetDerivationsForAtomTopDown																				
	/** Description: Finds all Derivations for specific atom 				
	/*************************************************************************************************************/
	
	public void GetDerivationsForAtomTopDown (Atom partlyInstAtom)
	{
		KeyMap.getInstance().Update(partlyInstAtom);
		Provenance.getInstance().Update(partlyInstAtom);
		for (Rule rule : this.p.getRules()) 
		{
			//long startTime = System.currentTimeMillis();
			if (rule.getHead().getName().equals(partlyInstAtom.getName())) 
			{
				Rule copyOfRule = new Rule (rule);
				copyOfRule.SwapToInstAtomAndPartiallyInst(copyOfRule.getHead(), partlyInstAtom);
				DeriveByRuleTopDown ruleDeriver = new DeriveByRuleTopDown(copyOfRule, this.p, this.atomsToDerive);
				ruleDeriver.FindDerivationsForRuleTopDown();
			}
			/*long endTime = System.currentTimeMillis();
			double intersectionTime = (endTime - startTime);
			if (intersectionTime > 1000) 
			{
				System.out.println("time for derive iteration: " + intersectionTime);
			}*/
		}
	}
}
