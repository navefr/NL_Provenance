package Derivation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import TopK.DerivationTree;
import TopK.EquationTopK;
import Basics.*;

public class DeriveByProgramBottomUp extends DeriveByProgram 
{	
	EquationTopK topk;
	
	Set<Atom> addedInstAtomsInLastIteration = new HashSet<Atom>();
	
	//Set<Atom> addedFullProv = new HashSet<Atom>();
	
	Set<Atom> atomsForDbOnline;
	
	Set<DerivationTree> treesWaiting = new HashSet<DerivationTree>();
	
	Map<RelevantKey, Vector<Atom>> previouslyFoundRelevants = new HashMap<RelevantKey, Vector<Atom>>();
	
	DerivationTree bestTreeThisStep;
	
	String suffix = "";
	
	int level = 0;
	
	int beginIdx = 0;
	
	int startTrees = 0;
	
	double bestWeight = 0;
	
	
	
	public DeriveByProgramBottomUp (Rule ... irs)
	{
		super(irs);
	}
	
	
	public DeriveByProgramBottomUp (EquationTopK topk, Rule ... irs)
	{
		super(irs);
		this.topk = topk;
	}
	
	
	public DeriveByProgramBottomUp (Program p, EquationTopK topk, Set<Atom> atomSet)
	{
		super(p);
		this.topk = topk;
		this.atomsForDbOnline = atomSet;
	}
	
	
	
	public DeriveByProgramBottomUp (Program p)
	{
		super(p);
	}
	


	public Set<Atom> getAddedInstAtomsInLastIteration() 
	{
		return addedInstAtomsInLastIteration;
	}


	/*************************************************************************************************************/
	/** Title: DeriveFromProgramIterationBottomUp																				
	/** Description: Finds all Derivations for the rule according to db 				
	/*************************************************************************************************************/
	
	public void DeriveFromProgramIterationBottomUp (boolean forIntersection, boolean online)
	{
		level++;
		//Reset();
		//long startTime = System.currentTimeMillis();
		//previouslyFoundRelevants.clear();
		Set<Atom> oldAtoms = new HashSet<Atom>(KeyMap.getInstance().Values());
		
		for (int i = beginIdx; i < this.p.getRules().size(); i++)
		{
			Rule rule = this.p.getRules().elementAt(i);
			if (false == forIntersection) 
			{
				if (false == IsBodyEDB(rule) || true == this.addedInstAtomsInLastIteration.isEmpty()) //if body of rule relies on derived facts.
				{
					InstantiateAllRelevantsInRule(rule, forIntersection, online);
				}
			}
			
			else if (rule.getDerivedInlevel() == level)
			{
				InstantiateAllRelevantsInRule(rule, forIntersection, online);
			}
			
			else
			{
				beginIdx = i;
				break;
			}
		}
		
		/*long end = System.currentTimeMillis();
		if ((end-startTime) > 100) 
		{
			System.out.println("DeriveFromProgramIterationBottomUp:: time for iteration over all rules: " + (end-startTime));
			System.out.println("DeriveFromProgramIterationBottomUp:: num of atoms added last iter = " + this.addedInstAtomsInLastIteration.size());
		}*/
		
		//UpdateAddedAtomsMap ();
		if (true == online && false == this.treesWaiting.isEmpty()) 
		{
			List<DerivationTree> list = new ArrayList<DerivationTree>(this.treesWaiting);
			Collections.sort(list);
			if (bestTreeThisStep == null)
			{
				for (DerivationTree derivationTree : list) 
				{
					if ( derivationTree.getDerivedFact().getName().contains(this.suffix) ) 
					{
						this.bestTreeThisStep = derivationTree;
						suffix += "_p_1";
						break;
					}
				}
			}
			
			bestWeight = list.get(0).getWeight();
			//double worstWeight = list.get(list.size()-1).getWeight();
			/*for (DerivationTree tree : list)//this.treesThisStep.values()) 
			{
				if (tree.getDerivedFact().toString().equals("dealsWith(Brazil,Botswana)")) {
					System.out.println("dealsWith(Brazil,Botswana) tree weight: " + tree.getWeight());
				}
			}
			int numTimes = 0;
			double brokeW = 0;*/
			startTrees = 0;
			for (DerivationTree tree : list)
			{
				if (tree.getWeight() >= bestWeight && false == tree.getDerivedFact().didFindTop1())
				{
					tree.getDerivedFact().setFoundTop1(true);
					this.atomsForDbOnline.add(tree.getDerivedFact());
					//numTimes++;
				}
				
				else
				{
					this.startTrees = list.indexOf(tree);
					//brokeW = tree.getWeight();
					break;
				}
			}
			
			//System.out.println("num of times got in cond: " + numTimes + " max weight: " + bestWeight + " break weight: " + brokeW);
			//System.out.println("size of remaining list: " + (list.size() - numTimes));
			//this.treesThisStep.subList(0, firstRemove).clear();
			if (0 == startTrees) this.treesWaiting = new HashSet<DerivationTree>();
			else this.treesWaiting = new HashSet<DerivationTree>(list.subList(startTrees, list.size()));
		}
		
		if (false == online) 
		{
			this.addedInstAtomsInLastIteration.addAll(KeyMap.getInstance().Values());// = new HashSet<Atom>(KeyMap.getInstance().Values());
			this.addedInstAtomsInLastIteration.removeAll(oldAtoms);
			//System.out.println("derived this time: " + this.addedInstAtomsInLastIteration.size());

		}
		
		else
		{
			this.addedInstAtomsInLastIteration = new HashSet<Atom>(this.atomsForDbOnline);
			//System.out.println("derived this time: " + this.addedInstAtomsInLastIteration.size());
		}
		
		UpdateRelevants ();
		//System.out.println("atoms for DB size: " + this.addedInstAtomsInLastIteration.size());
	}
	
	
	/*************************************************************************************************************/
	/** Title: UpdateAddedAtomsMap																				
	/** Description:   				
	/*************************************************************************************************************/
	
	/*public void UpdateAddedAtomsMap ()
	{
		for (Atom atom : this.addedInstAtomsInLastIteration) 
		{
			if (false == this.addedAtomsMap.containsKey(atom.getName())) 
			{
				this.addedAtomsMap.put(atom.getName(), new HashSet<Atom>());
			}
			
			this.addedAtomsMap.get(atom.getName()).add(atom);
		}
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: UpdateRelevants																				
	/** Description:   				
	/*************************************************************************************************************/
	
	public void UpdateRelevants ()
	{
		for (Atom atom : this.addedInstAtomsInLastIteration) 
		{				
			int size = atom.getParams().size();
			for (int i = 0; i < size && atom.isFact() == false; i++) 
			{
				RelevantKey copy = new RelevantKey (atom.getRuleUsed().getHead());
				copy.getParams().set(i, atom.getParams().get(i));
				if (true == this.previouslyFoundRelevants.containsKey(copy)) 
				{
					this.previouslyFoundRelevants.get(copy).add(atom);
				}
			}
			/*for (Atom key : this.previouslyFoundRelevants.keySet()) 
			{
				if (true == key.FittsPartialInst(atom)) 
				{
					this.previouslyFoundRelevants.get(key).add(atom);
				}
			}*/
		}
		/*System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("KEY SET");
		for (Atom key : previouslyFoundRelevants.keySet()) {
			if (key.getName().equals("hasChild")) {
				System.out.println(key);
			}
		}*/
	}
	
	
	/*************************************************************************************************************/
	/** Title: IsBodyEDB																				
	/** Description: Check if rule can derive a fact only with DB atoms  				
	/*************************************************************************************************************/
	
	public boolean IsBodyEDB (Rule rule)
	{
		boolean retVal = true;
		for (Atom bodyAtom : rule.getBody().getAtoms()) 
		{
			if (false == bodyAtom.IsAtomRelationEdb(this.p)) 
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}
	
	
	/*************************************************************************************************************/
	/** Title: InstantiateAllRelevantsInRule																				
	/** Description: Tries to inst. atoms derived in previous level 				
	/*************************************************************************************************************/

	public void InstantiateAllRelevantsInRule (Rule rule, boolean forIntersection, boolean online)
	{
		//List<Rule> instRuleVector = new CopyOnWriteArrayList<Rule>();
		Set<Rule> instRuleVector = Collections.newSetFromMap(new ConcurrentHashMap<Rule, Boolean>());
		//long startTime = System.currentTimeMillis();
		if (false == forIntersection && false == rule.isFullyInst()) 
		{
			for (Atom bodyAtom : rule.getBody().getAtoms()) 
			{
				for (Atom instAtom : this.addedInstAtomsInLastIteration) 
				{
					if (instAtom.getName().equals(bodyAtom.getName()))
					{
						Atom copyOfinstAtom = new Atom (instAtom);
						Rule temp = new Rule (rule);
						temp.SwapToInstAtomAndPartiallyInst(bodyAtom, copyOfinstAtom);
						if (true == temp.getIsFullyInst())
						{
							//Provenance.getInstance().AddRuleToProv(temp, this.level);
							//KeyMap.getInstance().UpdateRule(temp);
							temp.getBody().setRuleUsed(rule);//added for the top-k
							temp.getHead().setRuleUsed(rule);
							//for top-1!
							if (false == online) 
							{
								/*KeyMap.getInstance().UpdateFactBody(temp.getBody());
								Body pointerBody = KeyMap.getInstance().GetPointerBodyForTop1(temp.getBody());
								if (true == pointerBody.LegalDerivation())
								{*/
								KeyMap.getInstance().Update(temp.getHead());
								//}
								//AtomsDerived.getInstance().Update(temp.getHead());
								Provenance.getInstance().AddRuleToProv(temp, this.level);
								//addedFullProv.add(KeyMap.getInstance().Get(temp.getHead()));
								//System.out.println(temp);
							}
							
							else
							{
								HandleTop1Scenario(temp, online);
							}
						}

						else //if (false == online || temp.getRestricted() == null || false == temp.getBody().Uninstantiatated())
						{
							instRuleVector.add(temp);
							//DeriveRule(temp, online);
						}
					}
				}
			}
			
			if (true == this.addedInstAtomsInLastIteration.isEmpty() || false == instRuleVector.isEmpty())
			{
				DeriveByRuleBottomUp ruleDeriver = new DeriveByRuleBottomUp(rule, this.p, this.topk, this.treesWaiting, this.previouslyFoundRelevants);
				ruleDeriver.FindDerivationsForRuleBottomUp(instRuleVector, forIntersection, level);
			}
		}
		
		else
		{
			if (false == online ||  true == rule.getBody().AllAtomsInDb()) 
			{
				rule.getHead().setRuleUsed(rule);
				rule.getBody().setRuleUsed(rule);//added for the top-k
				Provenance.getInstance().AddRuleToProv(rule, this.level);
				//for top-1!
				if (false == online) KeyMap.getInstance().Update(rule.getHead());
				else HandleTop1Scenario(rule, online);
			}
		}
		
		/*long endTime = System.currentTimeMillis();
		if ((endTime-startTime) > 200)
		{
			System.out.println("DeriveByProgramBottomUp::InstantiateAllRelevantsInRule:: iteration time: "  + (endTime-startTime));
			System.out.println("DeriveByProgramBottomUp::InstantiateAllRelevantsInRule:: rule derived: "  + rule);
			System.out.println("DeriveByProgramBottomUp::InstantiateAllRelevantsInRule:: num of atoms last iter: " + this.addedInstAtomsInLastIteration.size());
		}*/
	}	
	
	

	/*************************************************************************************************************/
	/** Title: Reset																				
	/** Description: resets the parameters for next step of the program			
	/*************************************************************************************************************/
	
	/*public void Reset ()
	{
		//this.addedInstAtomsInLastIteration.clear();
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: HandleTop1Scenario																				
	/** Description: Handles top-1 in case of online = true			
	/*************************************************************************************************************/
	
	public void HandleTop1Scenario (Rule temp, boolean online)
	{
		if (true == online && true == temp.getBody().LegalDerivation()) 
		{
			/*Atom head = KeyMap.getInstance().Get(temp.getHead());
			if (null == head) 
			{
				KeyMap.getInstance().FoolsUpdate(temp.getHead());
				head = temp.getHead();
			}
			
			if (false == head.didFindTop1()) 
			{
				KeyMap.getInstance().UpdateFactBody(temp.getBody());
				Body pointerBody = KeyMap.getInstance().GetPointerBodyForTop1(temp.getBody());
				this.topk.UpdateTop1WhileSemiNaive(head, pointerBody);
				this.treesWaiting.addAll(head.getTrees());
			}*/
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

				if (false == head.didFindTop1()) 
				{
					this.topk.UpdateTop1WhileSemiNaive(head, pointerBody);
					this.treesWaiting.addAll(head.getTrees());
				}
			}
			
			/*else
			{
				System.out.println(temp);
			}*/
		}
	}
}
