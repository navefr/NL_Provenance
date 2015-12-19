package Pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import Basics.*;
import Circuit.Circuit;
import TopK.EquationTopK;

public class IntersectWithProgramOffline 
{
	Pattern pattern;
	
	//HashSet<Rule> constRules;
	
	public long sizeOfIntersectedProvenance;
	
	int k;

	
	public IntersectWithProgramOffline () {}
	
	
	public IntersectWithProgramOffline (PatternNode ipattern)
	{
		this.pattern = new Pattern(ipattern);
	}
	
	
	
	public IntersectWithProgramOffline (Vector<Vector<PatternNode>> patternVec)
	{
		this.pattern = new Pattern(patternVec);
	}
	
	
	
	public IntersectWithProgramOffline (Circuit ic, Pattern ipattern)
	{
		this.pattern = ipattern;
	}
	
	
	
	public IntersectWithProgramOffline (Circuit ic, Vector<Vector<PatternNode>> patternVec)
	{
		this.pattern = new Pattern(patternVec);
	}

	
	
	public IntersectWithProgramOffline (Pattern ipattern, HashSet<Rule> constRules)
	{
		this.pattern = ipattern;
	}
	
	//for debugging
	public IntersectWithProgramOffline (Pattern ipattern, int ik)
	{
		this.pattern = ipattern;
		this.k = ik;
	}
	
	
	
	public Pattern getPattern() 
	{
		return pattern;
	}



	public void setPattern(Pattern pattern) 
	{
		this.pattern = pattern;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetCircuitFromRules																				
	/** Description: Convert back to circuit from relevant rules				
	/*************************************************************************************************************/
	
	/*public void GetCircuitFromRules (Program prog, int sizeOfDB)
	{	
		Collections.sort(prog.getRules());
		if (false == prog.getRules().isEmpty()) 
		{
			SemiNaive sn = new SemiNaive(prog);
			sn.Run(false, true);
			this.c = sn.getParser().getC();
		}
		
		else
		{
			this.c = new Circuit();
		}
		
		/*for (int i = 0; i < level; i++) 
		{
			for (Atom atom : this.prov2.get(i).keySet()) 
			{
				c.AddNodeToCircuit(atom, this.prov2.get(i).get(atom), null, null, true, 0);
			}
		}
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: FromProgToProv																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void FromProgToProv (Program prog)
	{
		for (Atom key : KeyMap.getInstance().Values()) 
		{
			KeyMap.getInstance().Update(key);
			Provenance.getInstance().Update(key);
		}
		
		Collections.sort(prog.getRules());
		for (Rule rule : prog.getRules()) 
		{
			Provenance.getInstance().AddRuleToProv(rule, rule.getBody().getDerivedInlevel());
		}
		
		if (prog.getRules().size() > 100000) 
		{
			prog.getRules().clear();
		}
	}
	
	
	/*************************************************************************************************************/
	/** Title: FromProvToProg																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public Program FromProvToProg ()
	{
		Program prog = new Program();
		for (Atom key : KeyMap.getInstance().Values())
		{
			for (Body body : Provenance.getInstance().Get(key)) 
			{
				Rule temp = new Rule (key, 1, body);
				temp.setDerivedInlevel(body.getDerivedInlevel());
				//temp.getHead().setRuleUsed(body.getRuleUsed());
				//temp.getBody().setRuleUsed(body.getRuleUsed());
				prog.addRule(temp);
			}
		}
		
		Provenance.getInstance().Reset();
		return prog;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MarkAllBodiesForKey																				
	/** Description: 		
	/*************************************************************************************************************/
	
	public void MarkAllBodiesForKey (List<Atom> queue)
	{
		Atom key = queue.remove(0);
		for (Body body : Provenance.getInstance().Get(key)) 
		{
			body.MakeAllAtomsRelevantAndPlaceKeyInQueue(queue);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MarkRelevantProv 																				
	/** Description: 		
	/*************************************************************************************************************/
	
	public void MarkRelevantProv (Atom root)
	{	
		List<Atom> queue = new ArrayList<Atom>();
		
		Atom key = KeyMap.getInstance().Get(root);
		if (null != key)
		{
			queue.add(key);
			key.setRelevantForDerivationTopDown(true);
		}
		
		while (false == queue.isEmpty())
		{
			MarkAllBodiesForKey(queue);
		}
	}
		
	
	
	
	/******************* ******************************************************************************************/
	/** Title: LeaveOnlyRelevantDerivations																				
	/** Description: clears the final prov. vector from atoms that are not used in the derivation of root		
	/*************************************************************************************************************/
	
	public void LeaveOnlyRelevantDerivations ()
	{
		List<Atom> toBeRemoved = new ArrayList<Atom>();
		for (Atom key : KeyMap.getInstance().Values()) 
		{
			if (false == key.isRelevantForDerivationTopDown()) 
			{
				toBeRemoved.add(key);
			}
		}
		
		for (Atom atom : toBeRemoved) 
		{
			Provenance.getInstance().KeySet().remove(atom);
			KeyMap.getInstance().KeySet().remove(atom);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: IntersectNoTransitives																				
	/** Description: Intersect pattern With Program no transitive children  			
	/*************************************************************************************************************/
	
	public void IntersectNoTransitives (int sizeOfDB)
	{
		System.out.println("starting offline intesection...");
		long startTime = System.currentTimeMillis();
		
		
		Program prog = FromProvToProg();//new Program(constRules);//
		
		long end = System.currentTimeMillis();
		if ((end-startTime) > 500) 
		{
			System.out.println("time for FromProvToProg(): " + (end-startTime));
			//System.out.println("num of added inst atoms: " + this.programDeriver.getAddedInstAtomsInLastIteration().size());
		}
			//System.out.println("size of prog: " + prog.getRules().size());
		//startTime = System.currentTimeMillis();
		
		
		IntersectWithProgramOnline onlineIntersect = new IntersectWithProgramOnline(prog, this.pattern);
		onlineIntersect.IntersectNoTransitives ();
		
		
		//IntersectWithProgramOnline onlineIntersect = new IntersectWithProgramOnline(new Program(), this.pattern);
		
		/*end = System.currentTimeMillis();
		if ((end-startTime) > 1000) 
		{
			System.out.println("time for IntersectNoTransitives(): " + (end-startTime));
			//System.out.println("num of added inst atoms: " + this.programDeriver.getAddedInstAtomsInLastIteration().size());
		}*/
		startTime = System.currentTimeMillis();
		
		
		FromProgToProv(onlineIntersect.getP());
	
		
		end = System.currentTimeMillis();
		if ((end-startTime) > 600) 
		{
			System.out.println("time for FromProgToProv(): " + (end-startTime));
			//System.out.println("num of added inst atoms: " + this.programDeriver.getAddedInstAtomsInLastIteration().size());
		}
		
		PatternNode root = onlineIntersect.getPattern().getPatternVec().firstElement().firstElement();
		root.setName(root.getNewName());
		
		
		//startTime = System.currentTimeMillis();
		
		
		MarkRelevantProv(root);
		
		/*end = System.currentTimeMillis();
		if ((end-startTime) > 1000) 
		{
			System.out.println("time to mark relevant atoms: " + (end-startTime));
			//System.out.println("num of added inst atoms: " + this.programDeriver.getAddedInstAtomsInLastIteration().size());
		}*/
		//startTime = System.currentTimeMillis();
		
		
		LeaveOnlyRelevantDerivations();
		
		
		this.sizeOfIntersectedProvenance = Provenance.getInstance().GetProvSize();
		
		/*end = System.currentTimeMillis();
		if ((end-startTime) > 1000) 
		{
			//System.out.println("Time to remove irrelevant Derivations: " + (end-startTime));
			///System.out.println("num of added inst atoms: " + this.programDeriver.getAddedInstAtomsInLastIteration().size());
		}*/
		
		
		//System.out.println("Size of intersected hash: " + GetProvSize());
		
		
		//prog = FromProvToProg();
		
		
		//end = System.currentTimeMillis();
		
		/*end = System.currentTimeMillis();
		if ((end-startTime) > 1000) 
		{*/
			//System.out.println("time for FromProvToProg() #2: " + (end-startTime));
			//System.out.println("num of added inst atoms: " + this.programDeriver.getAddedInstAtomsInLastIteration().size());
		//}
		
		//System.out.println("size of relevant prog: " + prog.getRules().size());
		
		
		startTime = System.currentTimeMillis();
		
		
		//GetCircuitFromRules(prog, sizeOfDB);
		EquationTopK equation = new EquationTopK(this.k);
		equation.TopK();
		
		end = System.currentTimeMillis();
		System.out.println("Time for seminaive: " + (end-startTime));
	}
}
