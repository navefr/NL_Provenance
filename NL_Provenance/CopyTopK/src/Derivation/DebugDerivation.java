package Derivation;

import java.util.Vector;

import Basics.*;
import Parsing.ParseDB;
import Pattern.IntersectWithProgramOnline;
import Pattern.PatternNode;

public class DebugDerivation 
{
	public static void main (String [] args)
	{
		/*DB.getInstance().DropTableIfExists("import");
		DB.getInstance().DropTableIfExists("export");
		DB.getInstance().DropTableIfExists("dealWith");*/
		
		Var a = new Var("a", "Country");
		Var b = new Var("b", "Country");
		Var c = new Var("c", "Product");
		Var f = new Var("f", "Country");
		
		Atom dealsWith1_1 = new Atom("dealsWith", a,b);
		Atom dealsWith1_2 = new Atom("dealsWith", a,b);
		Atom dealsWith1_3 = new Atom("dealsWith", a,b);
		Atom dealsWith2 = new Atom("dealsWith", a,f);
		Atom dealsWith3 = new Atom("dealsWith", f,b);
		Atom dealsWith4 = new Atom("dealsWith", b,a);
		Atom exported = new Atom("exports", b,c);
		Atom imported = new Atom("imports", a,c);
		
		Rule r1 = new Rule (dealsWith1_1, 1, imported, exported);
		Rule r2 = new Rule (dealsWith1_2, 1, dealsWith2, dealsWith3);
		Rule r3 = new Rule (dealsWith1_3, 1, dealsWith4);
		
		Constant Cuba = new Constant("Cuba", "Country");
		Constant France = new Constant("France", "Country");
		Constant Mexico = new Constant("Mexico", "Country");
		Constant cigars = new Constant("cigars", "Product");
		Constant wine = new Constant("wine", "Product");
		Constant weapon = new Constant("weapon", "Product");
		Constant Israel = new Constant("Israel", "Country");
		Constant Palestine = new Constant("Palestine", "Country");
		
		Atom imPw = new Atom ("import", true, true, Palestine, weapon);
		Atom exIw = new Atom ("export", true, true, Israel, weapon);
		Atom exFw = new Atom ("export", true, true, France, wine);
		Atom exCc = new Atom ("export", true, true, Cuba, cigars);
		Atom imCw = new Atom ("import", true, true, Cuba, wine);
		Atom imMw = new Atom ("import", true, true, Mexico, wine);
		Atom imMc = new Atom ("import", true, true, Mexico, cigars);
		Atom imFc = new Atom ("import", true, true, France, cigars);
		
		
		/*DB.getInstance().DropTableIfExists("import");
		DB.getInstance().DropTableIfExists("export");
		
		DB.getInstance().Update(imPw);
		DB.getInstance().Update(exIw);
		DB.getInstance().Update(exFw);
		DB.getInstance().Update(exCc);
		DB.getInstance().Update(imCw);
		DB.getInstance().Update(imMw);
		DB.getInstance().Update(imMc);
		DB.getInstance().Update(imFc);*/
		/*SemiNaive sn = new SemiNaive(r1, r2, r3);//DB.getInstance(), 
		sn.Run(false, false, false);*/
		
		/*Atom dwCF = new Atom ("dealsWith", true ,false, Cuba, France);
		Atom dwFF = new Atom ("dealsWith", true ,false, France, France);*/
		
		/*Atom dwIP = new Atom ("dealWith", true ,false, Palestine, Israel);
		TopDown td = new TopDown (r1, r2, r3);
		td.Run(dwIP);*/
		
		//td.ClearProvenanceFromIrrelevantBodies();
		//System.out.println(sn.getParser().getC().getCircuit());
		
		
		//System.out.println(sn.parser.getConstantsProgram());
		/*DB.getInstance().DropTableIfExists("import");
		DB.getInstance().DropTableIfExists("export");
		DB.getInstance().DropTableIfExists("dealWith");*/
		//System.out.println(td.WaysToBeDerived(dwCF));
		//Vector<Atom> v = td.GetPartlyInstAtomsThatNeedToBeDerived(td.WaysToBeDerived(dwCF));
		
		Constant Canada = new Constant("Canada", "Country");
		Constant Andorra = new Constant("Andorra", "Country");
		//Constant electric = new Constant("wordnet_electricity_111449907", "Product");
		PatternNode root = new PatternNode ("dealsWith",false, Canada, Andorra);
		PatternNode child1 = new PatternNode ("dealsWith",false, Andorra, Canada);
		PatternNode grandchild1 = new PatternNode ("dealsWith",false, Canada, Canada);
		root.setChildren(child1);
		child1.setParent(root); 
		child1.setChildren(grandchild1); 
		grandchild1.setParent(child1);
		
		
		Vector<PatternNode> rootVec = new Vector<PatternNode> ();
		rootVec.add(root);

		Vector<PatternNode> childVec = new Vector<PatternNode> ();
		childVec.add(child1);
		
		Vector<PatternNode> grandchildVec = new Vector<PatternNode> ();
		grandchildVec.add(grandchild1);

		Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
		pattern.add(rootVec); 
		pattern.add(childVec);
		pattern.add(grandchildVec);
		Program p = new Program (r1, r2, r3);
		IntersectWithProgramOnline iwp = new IntersectWithProgramOnline (p, pattern);
		iwp.IntersectNoTransitives();
		
		Provenance.getInstance().Reset();
		KeyMap.getInstance().Reset();
		MemDB.getInstance().Reset();
		ParseDB rfDB = new ParseDB ("yago2core_facts.clean.notypes.tsv", 300);
		TopDown td = new TopDown (iwp.getP());
		//Atom dwFC = new Atom ("dealWith", true ,false, France, Cuba);
		iwp.getPattern().getPatternVec().firstElement().get(0).setName(iwp.getPattern().getPatternVec().firstElement().get(0).getNewName());
		Atom root1 = new Atom (iwp.getPattern().getPatternVec().firstElement().get(0));
		td.Run(root1);
		//System.out.println(Provenance.getInstance().getProvenance());
	}

}
