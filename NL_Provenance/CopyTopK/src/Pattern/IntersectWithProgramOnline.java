package Pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import Basics.Atom;
import Basics.Body;
import Basics.KeyMap;
import Basics.MemDB;
import Basics.Program;
import Basics.Proton;
import Basics.Provenance;
import Basics.Rule;
import Basics.Var;
import Derivation.DeriveByRuleTopDown;

public class IntersectWithProgramOnline {

	Pattern pattern;

	Program p;


	public IntersectWithProgramOnline () {}
	
	
	
	public IntersectWithProgramOnline (PatternNode ipattern)
	{
		this.pattern = new Pattern(ipattern);
	}
	
	
	
	public IntersectWithProgramOnline (Vector<Vector<PatternNode>> patternVec)
	{
		this.pattern = new Pattern(patternVec);
	}
	
	
	
	public IntersectWithProgramOnline (Program ip, Pattern ipattern)
	{
		this.p = ip;
		this.pattern = ipattern;
	}
	
	
	
	public IntersectWithProgramOnline (Program ip, Vector<Vector<PatternNode>> patternVec)
	{
		this.p = ip;
		this.pattern = new Pattern(patternVec);
	}

	
	public IntersectWithProgramOnline (Vector<Vector<PatternNode>> patternVec, Rule ... irs)
	{
		this.p = new Program(irs);
		this.pattern = new Pattern(patternVec);
	}

	

	public Program getP() 
	{
		return p;
	}



	public void setP(Program p) 
	{
		this.p = p;
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
	/** Title: IntersectNoTransitives																				
	/** Description: Intersect pattern With Program no transitive children  			
	/*************************************************************************************************************/
	
	public void IntersectNoTransitives ()
	{
		this.pattern.Renaming(true);
		for (Vector<PatternNode> nodeVec : this.pattern.getPatternVec()) 
		{
			if (false == this.pattern.patternVec.lastElement().equals(nodeVec)) 
			{
				for (PatternNode patternNode : nodeVec) 
				{
					Vector<Rule> addedRules = new Vector<Rule> (); 
					for (Rule rule : this.p.getRules()) 
					{
						if (true == rule.getHead().FittsPartialInst(patternNode)) 
						{
							addedRules.addAll(FindPartialInstRules(patternNode, rule));
						}
					}
					
					this.p.addRules(addedRules);
				}
			}
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: IntersectNagation																				
	/** Description: Intersect pattern With Program no transitive children  			
	/*************************************************************************************************************/
	
	public void IntersectNagation ()
	{
		Vector<Rule> addedRules = new Vector<Rule> (); 
		this.pattern.Renaming(false);
		for (Vector<PatternNode> nodeVec : this.pattern.getPatternVec()) 
		{
			if (false == this.pattern.patternVec.lastElement().equals(nodeVec)) 
			{
				for (PatternNode patternNode : nodeVec) 
				{ 
					if (patternNode.isLeaf()) 
					{
						HandleNagationLeaf(patternNode, addedRules);
					}
					
					else 
					{
						for (Rule rule : this.p.getRules()) 
						{						
							if (true == rule.getHead().FittsPartialInst(patternNode) 
									&& rule.getBody().getAtoms().size() >= patternNode.getPatternChildren().size()) 
							{
								HandleNagationNode(patternNode, addedRules, rule);
							}

							else
							{
								Rule r = new Rule (rule);
								r.getHead().setName(r.getHead().getName() + patternNode.getNewName());
								addedRules.add(r);
							}
						}
					}
				}
			}
		}
		
		this.p.addRules(addedRules);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: HandleNagationNode																				
	/** Description: Intersect pattern With Program no transitive children  			
	/*************************************************************************************************************/
	
	private void HandleNagationNode (PatternNode patternNode, Vector<Rule> addedRules, Rule rule)
	{
		Rule r = new Rule (rule);
		r.getHead().setName(patternNode.getName() + patternNode.getNewName());
		for (PatternNode child : patternNode.getPatternChildren()) 
		{
			for (Atom bodyAtom : r.getBody().getAtoms()) 
			{
				if (bodyAtom.IsAtomRelationEdb(this.p)) 
				{
					Atom head = new Atom (bodyAtom);
					head.setName(bodyAtom.getName() + child.getNewName());
					Atom origAtom = new Atom (bodyAtom);
					Body body = new Body (origAtom);
					addedRules.add(new Rule (head, 1, body));
				}
				
				bodyAtom.setName(bodyAtom.getName() + child.getNewName());	
			}
		}
		
		addedRules.add(r);
	}
	
	
	/*************************************************************************************************************/
	/** Title: HandleNagationLeaf																				
	/** Description: Intersect pattern With Program no transitive children  			
	/*************************************************************************************************************/
	
	private void HandleNagationLeaf (PatternNode patternNode, Vector<Rule> addedRules)
	{
		Atom head = new Atom (patternNode.getName() + patternNode.getNewName());
		int cnt = 97;
		for (Proton proton : patternNode.getParams()) 
		{
			Var v = new Var (Character.toString((char)cnt), proton.getCategory());
			head.getParams().add(v);
			cnt++;
		}

		Body body = new Body (new Atom (head));
		body.getAtoms().get(0).setName(patternNode.getName());
		Rule r = new Rule (head, 1, body);
		r.setRestricted(patternNode);
		addedRules.add(r);
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: FindPartialInstRules																				
	/** Description: use in pattern to find partial inst that satisfies children in pattern  			
	/*************************************************************************************************************/
	
	public Vector<Rule> FindPartialInstRules (PatternNode headPatternNode, Rule r)
	{
		Vector<Rule> derivVec = new Vector<Rule>();
		Rule copyOfRule = new Rule(r);
		
		//find all combinations of rule according to pattern
		DeriveByRuleTopDown dr = new DeriveByRuleTopDown(copyOfRule, this.p);
		copyOfRule.getHead().setName(headPatternNode.getNewName());
		copyOfRule.SwapToInstAtomAndPartiallyInst(copyOfRule.getHead(), headPatternNode);
		dr.FindDerivationsForRuleTopDownForIntersection(DefineChildrensDB(headPatternNode)); //top-down
		Vector<Atom> children = headPatternNode.getChildren();
		
		for (Rule partlyInstRule : dr.getDerivations()) 
		{
			if (partlyInstRule.CotainedInBody(children))
			{
				for (Atom patternChild : headPatternNode.getChildren()) 
				{
					for (Atom bodyAtom : partlyInstRule.getBody().getAtoms()) 
					{
						if (patternChild.equals(bodyAtom)) 
						{
							bodyAtom.setName(( (PatternNode)patternChild) .getNewName());
						}
					}
					
					partlyInstRule.getHead().setName(headPatternNode.getNewName());
					partlyInstRule.setDerivedInlevel(r.getDerivedInlevel());
					partlyInstRule.getHead().setRuleUsed(r);
					partlyInstRule.getBody().setRuleUsed(r);
					derivVec.add(partlyInstRule);
				}		
			}
		}
		
		return derivVec;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: IntersectWithTransitives																				
	/** Description:  			
	/*************************************************************************************************************/
	
	public long IntersectWithTransitives ()
	{
		this.pattern.RenamingTrans();
		Vector<Rule> addedRules = new Vector<Rule> ();
		
		for (Vector<PatternNode> nodeVec : this.pattern.patternVec) 
		{
			for (PatternNode patternNode : nodeVec) 
			{ 
				for (Rule rule : this.p.getRules()) 
				{
					if (true == rule.getHead().FittsPartialInst(patternNode))
					{
						if (patternNode.isLeaf()) 
						{
							Rule copy = new Rule (rule);
							copy.getHead().setName(copy.getHead().getName() + patternNode.getNewName());
							copy.SwapToInstAtomAndPartiallyInst(copy.getHead(), patternNode);
							addedRules.add( copy );
						}
						
						else if (rule.getBody().getAtoms().size() >= patternNode.getPatternChildren().size())
						{
							addedRules.addAll( ExpansionsFull(rule, patternNode) );
						}
					}
				}

				HandleTransChild(patternNode, addedRules);
				HandleEDBChild(patternNode, addedRules);
			}
		}
		
		AddTransRuleForEveryPatternRule(addedRules);
		
		long startTime = System.currentTimeMillis();
		CleanProgram(addedRules);
		long endTime = System.currentTimeMillis();
		
		this.p.addRules(addedRules);
		
		return endTime - startTime;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: CleanProgram																				
	/** Description:  			
	/*************************************************************************************************************/
	
	private void CleanProgram (Vector<Rule> addedRules)
	{
		List<Rule> legalRules = new ArrayList<Rule>();
		for (Rule rule : addedRules) 
		{
			if (rule.getHead().getName().contains("p_1")) 
			{
				rule.setReachable(true);
				legalRules.add(rule);
			}
		}
		
		while (false == legalRules.isEmpty())
		{
			Rule rule = legalRules.remove(0);
			for (Atom atom : rule.getBody().getAtoms()) 
			{
				for (Rule toCheck : addedRules) 
				{
					if (toCheck.getHead().getName().equals(atom.getName()) && !toCheck.isReachable()) 
					{
						toCheck.setReachable(true);
						legalRules.add(toCheck);
					}
				}
			}
		}
		
		List<Rule> toRemove = new ArrayList<Rule>();
		for (Rule rule : addedRules) 
		{
			if (false == rule.isReachable()) 
			{
				toRemove.add(rule);
			}
		}
		
		addedRules.removeAll(toRemove);
		DeleteUnstableRules(addedRules);
		DeleteIsomorphicRules(addedRules);
		if (false == pattern.getPatternVec().get(0).get(0).IsAtomRelationEdb(p)) 
		{
			DeleteRulesWithBaseRelations(addedRules);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: DeleteRulesWithBaseRelations																				
	/** Description: Deletes Rules of the form: import_p_1(a,b) :- import(a,b). which are unnecessary. FOR THE DEMO 			
	/*************************************************************************************************************/
	
	private void DeleteRulesWithBaseRelations (Vector<Rule> addedRules)
	{
		List<Rule> toRemove = new ArrayList<Rule>();
		for (Rule rule : addedRules) 
		{
			String origName = rule.getHead().getName().split("_")[0];
			if (1 == rule.getBody().getAtoms().size() && rule.getBody().getAtoms().get(0).IsAtomRelationEdb(p) && 
					rule.getBody().getAtoms().get(0).getName().equals( origName )) 
			{
				toRemove.add(rule);
				Atom good = rule.getBody().getAtoms().get(0);
				Atom bad = rule.getHead();
				
				for (Rule r : addedRules) 
				{
					for (Atom atom : r.getBody().getAtoms()) 
					{
						if (atom.getName().equals( bad.getName() )) 
						{
							r.SwapToInstAtomAndPartiallyInst(atom, bad);
							r.getBody().ChangeNameOfAtom( bad.getName(), good.getName() );
						}
					}
				}
			}
		}
		
		addedRules.removeAll(toRemove);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: DeleteIsomorphicRules																				
	/** Description:  			
	/*************************************************************************************************************/
	
	private void DeleteIsomorphicRules (Vector<Rule> addedRules)
	{
		List<Rule> toRemove = new ArrayList<Rule>();
		for (Rule r1 : addedRules) 
		{
			for (Rule r2 : addedRules) 
			{
				if (false == r1.equals(r2) && true == r1.Isomorphic(r2) && false == toRemove.contains(r2)) 
				{
					toRemove.add(r1);
				}
			}
		}
		
		addedRules.removeAll(toRemove);
	}
	
	
	/*************************************************************************************************************/
	/** Title: AddTransRuleForEveryPatternRule																				
	/** Description:  			
	/*************************************************************************************************************/
	
	private void AddTransRuleForEveryPatternRule (Vector<Rule> addedRules)
	{
		Vector<Rule> added = new Vector<Rule> ();
		for (Rule rule : this.p.getRules()) 
		{
			if (rule.getHead().getName().contains("_p_")) //rule was added for pattern 
			{
				Rule copy = new Rule (rule);
				copy.getHead().setName( copy.getHead().getName().replace("_p_", "_pt_") );
				added.add( copy );
			}
		}
		
		this.p.addRules(added);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: HandleEDBChild																				
	/** Description:  			
	/*************************************************************************************************************/
	
	private void HandleEDBChild (PatternNode patternNode, Vector<Rule> addedRules)
	{
		if (true == patternNode.IsAtomRelationEdb(p)) 
		{
			Atom head = new Atom (patternNode);
			head.setName( head.getName() + patternNode.getNewName() );
			Rule rule = new Rule (head, 1, patternNode);
			addedRules.add(rule);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: HandleTransChild																				
	/** Description:  			
	/*************************************************************************************************************/
	
	private void HandleTransChild (PatternNode patternNode, Vector<Rule> addedRules)
	{
		if (true == patternNode.isTransChild()) 
		{
			for (Rule rule : this.p.getRules()) 
			{
				addedRules.addAll( TransExpansions(rule, patternNode) );
			}
		}
	}
	
	
	/*************************************************************************************************************/
	/** Title: Expansions																				
	/** Description:  			
	/*************************************************************************************************************/
	
	private Vector<Rule> Expansions (Rule rule, PatternNode parent)
	{
		Vector<Rule> added = new Vector<Rule> ();
		Vector<PatternNode> children = parent.getPatternChildren();
		List<Set<Atom>> subsets = GetSubsets(rule.getBody().getAtoms(), children.size());
		
		for (Set<Atom> set : subsets) 
		{
			List<Atom> lst = new ArrayList<Atom>(set);
			Rule copy = new Rule (rule);
			copy.SwapToInstAtomAndPartiallyInst(rule.getHead(), parent);
			copy.getHead().setName( copy.getHead().getName() + parent.getNewName() );
			//solve for private case: number of atoms in body = 2 
			for (int i = 0; i < lst.size(); i++) 
			{
				int childIdx = (children.size() > 1) ? i : 0;
				int index = rule.getBody().getAtoms().indexOf(lst.get(i));
				Atom atom = copy.getBody().getAtoms().get(index);
				atom.setName( atom.getName() + children.get( childIdx ).getNewName() );
			}
			
			added.add(copy);
			
			if (children.size() > 1) 
			{
				Rule copy2 = new Rule (rule);
				copy2.SwapToInstAtomAndPartiallyInst(rule.getHead(), parent);
				copy2.getHead().setName( copy2.getHead().getName() + parent.getNewName() );
				for (int i = lst.size() - 1; i >= 0; i--) 
				{
					int childIdx = (lst.size() - 1) - i;
					int index = rule.getBody().getAtoms().indexOf(lst.get(i));
					Atom atom = copy2.getBody().getAtoms().get(index);
					copy2.getBody().getAtoms().get(index).setName( atom.getName() + children.get( childIdx ).getNewName() );
				}
				
				added.add(copy2);
			}
		}
		
		return added;
	}
	
	
	/*************************************************************************************************************/
	/** Title: ExpansionsFull																				
	/** Description: For the general case  			
	/*************************************************************************************************************/
	
	private Vector<Rule> ExpansionsFull (Rule rule, PatternNode parent)
	{
		Vector<Rule> added = new Vector<Rule> ();
		Vector<PatternNode> children = parent.getPatternChildren();
		List<Set<Atom>> subsets = GetSubsets(rule.getBody().getAtoms(), children.size());
		
		for (Set<Atom> set : subsets) 
		{
			ArrayList<Atom> lst = new ArrayList<Atom>(set);
			ArrayList<ArrayList<Atom>> perms = permute(lst);
			for (ArrayList<Atom> arrayList : perms) 
			{
				Rule expen = new Rule (rule);
				expen.setBody(arrayList);
				for (Atom atom : rule.getBody().getAtoms()) 
				{
					if (!arrayList.contains(atom)) 
					{
						expen.getBody().getAtoms().add( new Atom (atom) );
					}
				}
				
				for (int i = 0; i < children.size(); i++) 
				{
					expen.getBody().getAtoms().get(i).setName(expen.getBody().getAtoms().get(i).getName() + children.get(i).getNewName());
				}
				
				expen.SwapToInstAtomAndPartiallyInst(rule.getHead(), parent);
				expen.getHead().setName( expen.getHead().getName() + parent.getNewName() );
				/*for (int i = 0; i < children.size(); i++) 
				{
					String childName = children.get(i).getName();
					String atomName = expen.getBody().getAtoms().get(i).getName();
					if (childName.equals(atomName) && children.get(i).isFullyInst()) 
					{
						expen.SwapToInstAtomAndPartiallyInst(expen.getBody().getAtoms().get(i), children.get(i));
					}
				}*/
				
				added.add(expen);
			}
		}
		
		return added;
	}
	
	
	/*************************************************************************************************************/
	/** Title: TransExpansions																				
	/** Description:  			
	/*************************************************************************************************************/
	
	private Vector<Rule> TransExpansions (Rule rule, PatternNode parent)
	{
		Vector<Rule> added = new Vector<Rule> ();
		
		for (int i = 0; i < rule.getBody().getAtoms().size(); i++) 
		{
			Rule copy = new Rule (rule);
			Atom atom = copy.getBody().getAtoms().get(i);
			copy.getHead().setName( copy.getHead().getName() + parent.getNewName());
			atom.setName( atom.getName() + parent.getNewName() );
			added.add(copy);
		}
		
		return added;
	}
	
	
    
    /*************************************************************************************************************/
	/** Title: DefineChildrensDB																				
	/** Description: create a pattern based DB to find inst with  			
	/*************************************************************************************************************/
    
	private Map<String, Vector<Atom>> DefineChildrensDB (PatternNode node)
	{
		Map<String, Vector<Atom>> childrenDB = new HashMap<String, Vector<Atom>>();
		if (node.getChildren() != null) 
		{
			for (Atom child : node.getChildren()) 
			{
				Update(child, childrenDB);
			}
		}
		
		return childrenDB;
	}
	
	
	
	
	/*************************************************************************************************************/
    /** Title: Update                                                                                                                                                               
    /** Description: add new fact to DB                     
    /*************************************************************************************************************/
    
    private void Update (Atom atom, Map<String, Vector<Atom>> childrenDB)
    {	
    	if (null == childrenDB.get(atom.getName()))
    	{
    		childrenDB.put(atom.getName(), new Vector<Atom>());
    	}

    	childrenDB.get(atom.getName()).add(atom);
    }
    
    
    
    /*************************************************************************************************************/
    /** Title: GetSubsets                                                                                                                                                               
    /** Description: GetSubsets helper                     
    /*************************************************************************************************************/
    
    private static void GetSubsets(List<Atom> superSet, int k, int idx, Set<Atom> current,List<Set<Atom>> solution) {
        //successful stop clause
        if (current.size() == k) {
            solution.add(new HashSet<>(current));
            return;
        }
        //unseccessful stop clause
        if (idx == superSet.size()) return;
        Atom x = superSet.get(idx);
        current.add(x);
        //"guess" x is in the subset
        GetSubsets(superSet, k, idx+1, current, solution);
        current.remove(x);
        //"guess" x is not in the subset
        GetSubsets(superSet, k, idx+1, current, solution);
    }

    
    
    /*************************************************************************************************************/
    /** Title: GetSubsets                                                                                                                                                               
    /** Description: Gets all subsets of size k                     
    /*************************************************************************************************************/
    
    private static List<Set<Atom>> GetSubsets(List<Atom> superSet, int k) {
        List<Set<Atom>> res = new ArrayList<>();
        GetSubsets(superSet, k, 0, new HashSet<Atom>(), res);
        return res;
    }
    
    
    
    /*************************************************************************************************************/
    /** Title: permute                                                                                                                                                               
    /** Description: Get all permutations of a list                     
    /*************************************************************************************************************/
    
    private ArrayList<ArrayList<Atom>> permute(ArrayList<Atom> num) {
    	ArrayList<ArrayList<Atom>> result = new ArrayList<ArrayList<Atom>>();
    	permute(num, 0, result);
    	return result;
    }
     
    private void permute(ArrayList<Atom> num, int start, ArrayList<ArrayList<Atom>> result) {
     
    	if (start >= num.size()) {
    		ArrayList<Atom> item = convertArrayToList(num);
    		result.add(item);
    	}
     
    	for (int j = start; j <= num.size() - 1; j++) {
    		swap(num, start, j);
    		permute(num, start + 1, result);
    		swap(num, start, j);
    	}
    }
     
    private ArrayList<Atom> convertArrayToList(ArrayList<Atom> num) {
    	ArrayList<Atom> item = new ArrayList<Atom>();
    	for (int h = 0; h < num.size(); h++) {
    		item.add(num.get(h));
    	}
    	return item;
    }
     
    private void swap(ArrayList<Atom> a, int i, int j) {
    	Atom temp = a.get(i);
    	a.set(i, a.get(j));
    	a.set(j, temp);
    }
    
    
    
	/*************************************************************************************************************/
	/** Title: LeaveOnlyPossibleDerivations																				
	/** Description: clears the final prov. vector from atoms that cannot be derived		
	/*************************************************************************************************************/
	
	private void DeleteUnstableRules(List<Rule> added)
	{
		List<Rule> toRemove = new ArrayList<Rule> ();
		int markedAtCurrentStep = 1;
		
		while (markedAtCurrentStep > 0)
		{
			markedAtCurrentStep = MarkAtomsWithStableDerivation(added);
		}
		
		for (Rule rule : added) 
		{
			if (!rule.getHead().isStable()) 
			{
				toRemove.add(rule);
			}
		}
		
		added.removeAll(toRemove);
	}
	

	
	/*************************************************************************************************************/
	/** Title: IsDerivationStable																				
	/** Description: Checks if the atom can be derived from the DB and program or just has an endless cycle in the prov.		
	/*************************************************************************************************************/
	
	private void HasStableDerivation (List<Rule> added, Atom atom)
	{
		if (atom.isInBody(p)) 
		{
			atom.setStable(true);
			return;
		}
		
		for (Rule rule : added) 
		{	
			if (true == rule.getHead().equals(atom) && rule.getBody().IsBodyStable()) //the entire body is consisted of stable atoms 
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
	
	private int MarkAtomsWithStableDerivation (List<Rule> added)
	{
		int markedAtCurrentStep = 0;
		for (Rule rule : added) 
		{
			if (rule.getBody().IsBodyStable() && false == rule.getHead().isStable()) 
			{
				rule.getHead().setStable(true);
				markedAtCurrentStep += MakeAllApearncesStable(added, rule.getHead());
			}
			
			else
			{
				for (Atom atom : rule.getBody().getAtoms()) 
				{
					if (false == atom.isStable()) 
					{
						HasStableDerivation(added, atom);
						if (true == atom.isStable()) 
						{
							markedAtCurrentStep++;
						}
					}
				}
			}
			
		}
		
		return markedAtCurrentStep;
	}
	
	
	
	private int MakeAllApearncesStable(List<Rule> added, Atom atom)
	{
		int stables = 0;
		for (Rule rule : added) 
		{
			for (Atom bAtom : rule.getBody().getAtoms()) 
			{
				if (bAtom.getName().equals(atom.getName())) 
				{
					bAtom.setStable(true);
					stables++;
				}
			}
		}
		
		return stables;
	}
	
}

