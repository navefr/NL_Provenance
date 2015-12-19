package Derivation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import TopK.DerivationTree;
import TopK.EquationTopK;
import Basics.*;

public class DeriveByRuleBottomUp extends DeriveByRule
{
	Map<RelevantKey, Vector<Atom>> previouslyFoundRelevants;
	
	EquationTopK topk;
	
	Set<DerivationTree> treesThisStep;
	
	
	public DeriveByRuleBottomUp (Rule ir, Program ip)
	{
		super(ir, ip);
	}

	
	public DeriveByRuleBottomUp (Rule ir, Program ip, EquationTopK topk, Set<DerivationTree> itreesThisStep, Map<RelevantKey, Vector<Atom>> previouslyFoundRelevants)
	{
		super(ir, ip);
		this.topk = topk;
		this.treesThisStep = itreesThisStep;
		this.previouslyFoundRelevants = previouslyFoundRelevants;
	}



	/*************************************************************************************************************/
	/** Title: FindDerivationsForRuleBottomUp																				
	/** Description: Finds all Derivations for the rule according to db 				
	/*************************************************************************************************************/
	
	public void FindDerivationsForRuleBottomUp (Set<Rule> instRuleVector, boolean forIntersection, int level) 
	{
		if (true == instRuleVector.isEmpty()) 
		{
			instRuleVector.add(new Rule(this.r));
		}
		
		
		while (false == instRuleVector.isEmpty())
		{
			instRuleVector = InstIterationBottomUp(instRuleVector, forIntersection, level);
		}
	}
	
	
	/*************************************************************************************************************/
	/** Title: InstIterationBottomUp																				
	/** Description: One iteration to update the instRuleVector in partly inst rules  				
	/*************************************************************************************************************/

	public Set<Rule> InstIterationBottomUp (Set<Rule> instRuleVector, boolean forIntersection, int level)
	{
		/*long startTime = System.currentTimeMillis();
		int size = instRuleVector.size();*/
		Vector<Atom> relevants;
		for (Rule rule : instRuleVector)
		{
			for (Atom partlyInstAtom : rule.getBody().getAtoms()) 
			{
				if (false == partlyInstAtom.isFullyInst() || true == forIntersection) 
				{
					//startTime = System.currentTimeMillis();
					
					relevants = FindRelevantInstAtoms(partlyInstAtom);
					for (Atom atom : relevants) 
					{
						Rule temp = new Rule (rule);
						temp.SwapToInstAtomAndPartiallyInst(partlyInstAtom, atom);
						if (true == temp.getIsFullyInst())
						{
							//Provenance.getInstance().AddRuleToProv(temp, level);
							//KeyMap.getInstance().UpdateRule(temp);
							temp.getBody().setRuleUsed(this.r);//added for the top-k
							temp.getHead().setRuleUsed(this.r);
							//for top-1!
							if (null == this.topk && true == temp.getBody().AllAtomsInDb()) 
							{
								/*KeyMap.getInstance().UpdateFactBody(temp.getBody());
								Body pointerBody = KeyMap.getInstance().GetPointerBodyForTop1(temp.getBody());
								if (true == pointerBody.LegalDerivation())
								{*/
								KeyMap.getInstance().Update(temp.getHead());
								//}
								//AtomsDerived.getInstance().Update(temp.getHead());
								//System.out.println(temp);
								Provenance.getInstance().AddRuleToProv(temp, level);
							}
							
							else
							{
								HandleTop1Scenario(temp);
							}
							
							
						}

						//temp isn't fully inst
						else //if (null == this.topk || temp.getRestricted() == null || false == temp.getBody().Uninstantiatated())
						{ 
							instRuleVector.add(temp);
						}
					}
					
					/*long endTime = System.currentTimeMillis();
					if ((endTime-startTime) > 50) {
						System.out.println("InstIterationBottomUp:: iteration time: "  + (endTime-startTime));
						System.out.println("num of relevants: " + relevants.size());
					}*/
				}
			}
			
			
			instRuleVector.remove(rule);
		}
		
		/*long endTime = System.currentTimeMillis();
		if ((endTime-startTime) > 100) {
			System.out.println("InstIterationBottomUp:: iteration time: "  + (endTime-startTime));
			System.out.println("DeriveByProgramBottomUp::InstantiateAllRelevantsInRule:: rule derived: "  + this.r);
			System.out.println("InstIterationBottomUp:: size of instRuleVector before iteration: " + size);
		}*/
		
		return instRuleVector;
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: HandleTop1Scenario																				
	/** Description: Handles top-1 in case of online = true			
	/*************************************************************************************************************/
	
	public void HandleTop1Scenario (Rule temp)
	{
		if (null != this.topk) 
		{
			KeyMap.getInstance().UpdateFactBody(temp.getBody());
			Body pointerBody = KeyMap.getInstance().GetPointerBodyForTop1(temp.getBody());
			if (true == pointerBody.LegalDerivation()) 
			{
				Atom head = KeyMap.getInstance().Get(temp.getHead());
				if (null == head)
				{
					KeyMap.getInstance().FoolsUpdate(temp.getHead());
					head = temp.getHead();
				}

				if (head.isFact()) 
				{
					head.setRuleUsed(temp.getHead().getRuleUsed());
				}
				
				if (false == head.didFindTop1()) 
				{
					/*KeyMap.getInstance().UpdateFactBody(temp.getBody());
					Body pointerBody = KeyMap.getInstance().GetPointerBodyForTop1(temp.getBody());
					if (false == pointerBody.getAtoms().contains(null)) 
					{*/
						this.topk.UpdateTop1WhileSemiNaive(head, pointerBody);
						this.treesThisStep.addAll(head.getTrees());
					//}
					
				}
			}
		}
		
	}
	
	
	/*************************************************************************************************************/
	/** Title: FindRelevantInstAtoms																				
	/** Description: Finds the relevant atoms for the current partial inst. in the rule 				
	/*************************************************************************************************************/
	
	public Vector<Atom> FindRelevantInstAtoms (Atom partlyInstAtom)
	{
		Vector<Atom> relevantAtoms = new Vector<Atom>();
		boolean neverAppeared = true;
		
		RelevantKey relKey = new RelevantKey(partlyInstAtom);
		relevantAtoms = previouslyFoundRelevants.get(relKey);
		/*if (relevantAtoms != null) 
		{
			neverAppeared = false;
		}*/
		
		/*for (Atom key : this.previouslyFoundRelevants.keySet()) 
		{
			if (true == key.HasSameRelevantFacts(partlyInstAtom)) 
			{
				relevantAtoms = previouslyFoundRelevants.get(key);
				neverAppeared = false;
				break;
			}
		}*/
		
		if (true == neverAppeared)
		{
			if (false == partlyInstAtom.isFullyInst()) 
			{
				//relevantAtoms = DB.getInstance().GetRelevantFactsFromDB(partlyInstAtom, this.p);
				relevantAtoms = MemDB.getInstance().GetRelevantFactsFromDB(partlyInstAtom, this.p);
				RelevantKey rel = new RelevantKey(partlyInstAtom);
				previouslyFoundRelevants.put(rel, relevantAtoms);
			}
			
			/*else
			{
				//if (DB.getInstance().ContainedInTable(partlyInstAtom))
				if (MemDB.getInstance().ContainedInTable(partlyInstAtom))
				{
					relevantAtoms.add(partlyInstAtom);
				}
			}*/
			
		}
		
		return relevantAtoms;
	}
}
