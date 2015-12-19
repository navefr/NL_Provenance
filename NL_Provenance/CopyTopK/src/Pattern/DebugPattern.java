package Pattern;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.omg.CORBA.TCKind;

import au.com.bytecode.opencsv.CSVWriter;
import Basics.Atom;
import Basics.Constant;
import Basics.DB;
import Basics.KeyMap;
import Basics.MemDB;
import Basics.Program;
import Basics.Proton;
import Basics.Provenance;
import Basics.Rule;
import Basics.Var;
import Derivation.SemiNaive;
import Parsing.ParseDB;
import Parsing.ParseDbRules;
import TopK.DerivationTree;

public class DebugPattern 
{
	public static void main (String [] args) throws IOException
	{
		/*DB.getInstance().CreateDB();
		DB.getInstance().DropTableIfExists("imports");
		DB.getInstance().DropTableIfExists("exports");
		DB.getInstance().DropTableIfExists("dealsWith");*/
		MemDB.getInstance().Reset();
		KeyMap.getInstance().Reset();
		
		/*ra('abcd0',?B,?C,?D,?E) :- p('abcd0'),p(?B),p(?C),p(?D),p(?E).
		rb('abcd0',?B,?C,?D,?E) :- p('abcd0'),p(?B),p(?C),p(?D),p(?E).
		r('abcd0',?B,?C,?D,?E) :- ra('abcd0',?B,?C,?D,?E),rb('abcd0',?B,?C,?D,?E).
		q(?A) :- r(?A,?B,?C,?D,?E).
		q(?B) :- r(?A,?B,?C,?D,?E).
		q(?C) :- r(?A,?B,?C,?D,?E).
		q(?D) :- r(?A,?B,?C,?D,?E).
		q(?E) :- r(?A,?B,?C,?D,?E).
		*/
		
		Var A = new Var("a", "Country");
		Var B = new Var("b", "Country");
		Var C = new Var("c", "Country");
		Var D = new Var("d", "Country");
		Var E = new Var("e", "Country");
	
		Constant abcd0 = new Constant("abcd0", "Country");
		Constant abcd1 = new Constant("abcd1", "Country");
		Constant abcd2 = new Constant("abcd2", "Country");
		Constant abcd3 = new Constant("abcd3", "Country");
		Constant abcd4 = new Constant("abcd4", "Country");
		
		
		Atom ra = new Atom("ra", A,B,C,D,E);
		Atom rb = new Atom("rb", A,B,C,D,E);
		Atom r = new Atom("r", A,B,C,D,E);
		Atom q_1 = new Atom("q", A);
		Atom q_2 = new Atom("q", B);
		Atom q_3 = new Atom("q", C);
		Atom q_4 = new Atom("q", D);
		Atom q_5 = new Atom("q", E);
		Atom p_1 = new Atom("p", A);
		Atom p_2 = new Atom("p", B);
		Atom p_3 = new Atom("p", C);
		Atom p_4 = new Atom("p", D);
		Atom p_5 = new Atom("p", E);
	
		Rule r1 = new Rule (ra, 1, p_1, p_2, p_3, p_4, p_5);
		Rule r2 = new Rule (rb, 1, p_1, p_2, p_3, p_4, p_5);
		Rule r8 = new Rule (r, 1, ra,rb);
		Rule r3 = new Rule (q_1, 1, r);
		Rule r4 = new Rule (q_2, 1, r);
		Rule r5 = new Rule (q_3, 1, r);
		Rule r6 = new Rule (q_4, 1,r);
		Rule r7 = new Rule (q_5, 1, r);
		
		

		Program prog = new Program(r1, r2, r3, r4,r5,r6,r7, r8);
		/*IntersectWithProgramOnline iwp = new IntersectWithProgramOnline(prog, pattern); 
		iwp.IntersectWithTransitives();*/
		
		
		
		
		
		
		
		
		
		
		
		
		
		Var a = new Var("a", "Country");
		Var b = new Var("b", "Country");
		Var c1 = new Var("c", "Country");
		Var d = new Var("d", "Country");
		Var e = new Var("e", "Country");
		Var g = new Var("g", "Country");
		Var h = new Var("h", "Country");
		Var i = new Var("i", "Country");
		Var j = new Var("j", "Country");
		Var k = new Var("k", "Country");
		Var l = new Var("l", "Country");
		Var m = new Var("m", "Country");
		Var n = new Var("n", "Country");
		Var c = new Var("c", "Product");
		Var f = new Var("f", "Country");
		
		Atom dealsWith1_1 = new Atom("dealsWith", a,b);
		Atom dealsWith1_2 = new Atom("dealsWith", a,b);
		Atom dealsWith1_4 = new Atom("dealsWith", a,b);
		Atom dealsWith1_5 = new Atom("dealsWith", a,c1);
		Atom dealsWith1_6 = new Atom("dealsWith", a,d);
		Atom dealsWith1_7 = new Atom("dealsWith", a,e);
		Atom dealsWith1_8 = new Atom("dealsWith", a,g);
		Atom dealsWith1_9 = new Atom("dealsWith", a,h);
		Atom dealsWith1_10 = new Atom("dealsWith", a,i);
		Atom dealsWith1_11 = new Atom("dealsWith", a,j);
		Atom dealsWith1_12 = new Atom("dealsWith", a,k);
		Atom dealsWith2 = new Atom("dealsWith", a,f);
		Atom dealsWith3 = new Atom("dealsWith", f,b);
		Atom dealsWith4 = new Atom("dealsWith", b,a);
		Atom exported = new Atom("exports", b,c);
		Atom imported = new Atom("imports", a,c);
		
		Atom [] dw = {dealsWith1_1, dealsWith1_2, dealsWith1_12, dealsWith1_4, dealsWith1_5,
				dealsWith1_6, dealsWith1_7, dealsWith1_8, dealsWith1_9, dealsWith1_10, dealsWith1_11};
		
		/*Rule r1 = new Rule (dealsWith1_1, 1, imported, exported);
		Rule r2 = new Rule (dealsWith1_2, 1, dealsWith2, dealsWith3);
		Rule r3 = new Rule (dealsWith1_4, 1, dealsWith4);
		*/
		
		
		
		Constant Cuba = new Constant("Cuba", "Country");
		Constant France = new Constant("France", "Country");
		Constant Nicaragua = new Constant("Nicaragua", "Country");
		Constant Slovakia = new Constant("Slovakia", "Country");
		Constant Wendys = new Constant("Wendys", "resturant");
		Constant cigars = new Constant("cigars", "Product");

		Constant Mexico = new Constant("Mexico", "Country");

		Constant wine = new Constant("wine", "Product");
		Constant weapon = new Constant("weapon", "Product");
		Constant Israel = new Constant("Israel", "Country");
		Constant Palestine = new Constant("Palestine", "Country");
		
		
		Constant Gearbox = new Constant("Gearbox", "Part");
		Constant Engine = new Constant("Engine", "Part");
		Var Cost = new Var("Cost", "Cost");
		Constant Quantity = new Constant("156", "Cost");
		Var Part = new Var("Part", "Part");
		
		//Constant Albania = new Constant("Albania", "Country");
		//Constant Benin = new Constant("Benin", "Country");
		/*Atom exFw = new Atom ("exports",true, true, France, wine);
		Atom exCc = new Atom ("exports",true, true, Cuba, cigars);
		Vector<Atom> v1 = new Vector<Atom>(); 
		v1.add(exFw);
		v1.add(exCc);
		
		Atom imCw = new Atom ("imports",  true, true, Cuba, wine);
		Atom imMw = new Atom ("imports",  true, true, Mexico, wine);
		Atom imMc = new Atom ("imports",  true, true, Mexico, cigars);
		Atom imFc = new Atom ("imports", true, true, France, cigars);
		Vector<Atom> v2 = new Vector<Atom>(); 
		v2.add(imCw);
		v2.add(imMw);
		v2.add(imMc);
		v2.add(imFc);
		
		Map<String, Vector<Atom>> d = new HashMap<String, Vector<Atom>>();
		d.put("exports", v1);
		d.put("imports", v2);*/
		
		//PatternNode root = new PatternNode ("dealWith",false, Palestine, Israel);
		
		//PatternNode child1 = new PatternNode ("dealsWith",false, France, Cuba);
		
		
		
		
		/*PatternNode grandchild1 = new PatternNode ("exports", false, Cuba, cigars);
		grandchild1.setTransChild(true);
		PatternNode child1 = new PatternNode ("dealsWith",false, France, Cuba);
		child1.setTransChild(true);
		
		root.AddPatternChild(child1);
		child1.setParent(root); 
		child1.AddPatternChild(grandchild1);
		grandchild1.setParent(child1);
		
		
		Vector<PatternNode> rootVec = new Vector<PatternNode> ();
		rootVec.add(root);
		
		Vector<PatternNode> childVec = new Vector<PatternNode> ();
		childVec.add(child1);
		
		Vector<PatternNode> grandChildVec = new Vector<PatternNode> ();
		grandChildVec.add(grandchild1);
	
		Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
		pattern.add(rootVec); 
		pattern.add(childVec); 
		pattern.add(grandChildVec);*/
		//Program p = new Program(r1, r2, r3);
		
		FileWriter fw_online = new FileWriter("meas_" + "_intersection_binary_notrans.csv");
		CSVWriter writer = new CSVWriter(fw_online);
		String [] headLines = new String[] {"pattern size", "intersection time", "Number of rules added", "Clean program time"};
		writer.writeNext(headLines);
		writer.flush();
				
		
		//ZERO EXPERIMENT: CHAIN PATTERN IRIS PROGRAM
		/*Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
		PatternNode root = new PatternNode ("r",false, abcd0, B,C,D,E);
		PatternNode parent = root;
		Vector<PatternNode> rootVec = new Vector<PatternNode> ();
		rootVec.add(root);
		pattern.add(rootVec);
		Program p = prog;
		PatternNode child = new PatternNode ("ra",false, abcd0, B,C,D,E);
		//PatternNode child2 = new PatternNode ("rb",false, abcd0, B, C, D, E);
		Vector<PatternNode> childV = new Vector<PatternNode> ();
		child.setParent(parent);
		parent.AddPatternChild(child);
		childV.add(child);
		child2.setParent(parent);
		parent.AddPatternChild(child2);
		child.setTransChild(true);
		child2.setTransChild(true);
		childV.add(child2);
		pattern.add(childV);
		parent = child;
		PatternNode child3 = new PatternNode ("p",false, abcd0);
		PatternNode child4 = new PatternNode ("p",false, abcd1);
		PatternNode child5 = new PatternNode ("p",false, abcd2);
		PatternNode child6 = new PatternNode ("p",false, abcd3);
		PatternNode child7 = new PatternNode ("p",false, abcd4);
		
		Vector<PatternNode> childV2 = new Vector<PatternNode> ();
		child3.setParent(parent);
		parent.AddPatternChild(child3);
		child3.setTransChild(true);
		childV2.add(child3);
		
		child4.setParent(parent);
		parent.AddPatternChild(child4);
		child4.setTransChild(true);
		childV2.add(child4);
		
		child5.setParent(parent);
		parent.AddPatternChild(child5);
		child5.setTransChild(true);
		childV2.add(child5);
		
		child6.setParent(parent);
		parent.AddPatternChild(child6);
		child6.setTransChild(true);
		childV2.add(child6);
		
		child7.setParent(parent);
		parent.AddPatternChild(child7);
		child7.setTransChild(true);
		childV2.add(child7);
		
		pattern.add(childV2);
		IntersectWithProgramOnline iwp = new IntersectWithProgramOnline(p, pattern); 
		long cleanTime = iwp.IntersectWithTransitives();
		System.out.println(iwp.getP());*/
		
		//FIRST EXPERIMENT: CHAIN PATTERN
		
		for (int x = 2; x < 4; x+= 1) 
		{
			
			Program p = TCProgram();
			Constant node_55 = new Constant("55", "edge");
			Constant node_56 = new Constant("56", "edge");
			Constant node_2 = new Constant("2", "edge");
			Constant node_99 = new Constant("99", "edge");
			
			Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
			PatternNode root = new PatternNode ("TC",false, node_56, node_99);
			PatternNode parent = root;
			Vector<PatternNode> rootVec = new Vector<PatternNode> ();
			rootVec.add(root);
			pattern.add(rootVec);
			
			ParseDbRules rules = new ParseDbRules ("amie_yago2_rb_confidence.tsv", 140);
			//ParseDbRules rules = new ParseDbRules ("C:\\Users\\amirgilad\\WORKSPACESVN\\CopyTopK\\TC\\prog.iris", 100);
			//Program p = rules.getProgram();
			
			/*
			Program p = new Program();
			for (Rule rule : rules.getProgram().getRules()) 
			{
				if (FortyBestRules(rule)) 
				{
					p.addRule(rule);
				}
			}*/
			//System.out.println(p);
			for (int t = 1; t < x; t++) 
			{
				Proton re = (t == 1) ? node_56 : f;
				PatternNode child = new PatternNode ("TC", false, re, b);
				Vector<PatternNode> childV = new Vector<PatternNode> ();
				child.setParent(parent);
				parent.AddPatternChild(child);
				child.setTransChild(true);
				childV.add(child);
				pattern.add(childV);
				parent = child;
			}
			/*
			PatternNode child = new PatternNode ("imports",false, a, c);
			Vector<PatternNode> childV = new Vector<PatternNode> ();
			child.setParent(parent);
			parent.AddPatternChild(child);
			child.setTransChild(true);
			childV.add(child);
			pattern.add(childV);
			parent = child;
			*/
			
			int patternSize = 0;
			for (Vector<PatternNode> vec : pattern) 
			{
				patternSize += vec.size();
			}
			
			int oldsize = p.getRules().size();
			long startTime = System.currentTimeMillis();
			
			IntersectWithProgramOnline iwp = new IntersectWithProgramOnline(p, pattern); 
			long cleanTime = iwp.IntersectWithTransitives();
			
			long endTime = System.currentTimeMillis();
			long durationFullProv = (endTime - startTime);
			int addedRules = (iwp.getP().getRules().size() - oldsize);
			String result = String.format("pattern size: %d\nintersection time: %d\nNumber of rules added: %d\ntime to clean program: %d", 
					patternSize, durationFullProv, addedRules, cleanTime);
			System.out.println(result);
			System.out.println("----------------------------------------------------------------------------------------------------");
			
			String [] resultArr = result.split("\n");
			
			for (int w = 0; w < resultArr.length; w++) 
			{
				resultArr[w] = resultArr[w].replaceAll( "[^(\\d)+\\.(\\d)+]", "" );
			}
			
			writer.writeNext(resultArr);
			writer.flush();
			System.out.println(iwp.getP());
		}
		
		
		//SECOND EXPERIMENT: FULL BINARY TREE PATTERN
		
		for (int x = 2; x < 3; x+= 1) //number of layers in binary tree 
		{
			Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
			//PatternNode root = new PatternNode ("r",false, abcd0, B,C,D,E);
			/*Constant node_99 = new Constant("99", "edge");
			Constant node_50 = new Constant("50", "edge");
			Constant node_56 = new Constant("56", "edge");*/
			PatternNode root = new PatternNode ("volatile",false, b);
			Vector<PatternNode> rootVec = new Vector<PatternNode> ();
			rootVec.add(root);
			pattern.add(rootVec);
			
			ParseDbRules rules = new ParseDbRules ("amie_yago2_rb_confidence.tsv", 140);
			Collections.sort(rules.getProgram().getRules());
			
			
			//List<Rule> lst = new ArrayList<Rule>(rules.getProgram().getRules().subList(40, rules.getProgram().getRules().size()));
			//rules.getProgram().getRules().removeAll( lst );
			//Program p = rules.getProgram();
			//System.out.println(p);
			/*
			Program p = new Program();
			for (Rule rule : rules.getProgram().getRules()) 
			{
				if (dwRule2(rule)) 
				{
					p.addRule(rule);
				}
			}*/
			Program p = ExplainProgram();//TCProgram();
			//System.out.println(p);
			for (int t = 1; t < x; t++) 
			{
				Vector<PatternNode> childV = new Vector<PatternNode> ();
				for (PatternNode patternNode : pattern.lastElement()) 
				{
					PatternNode child = new PatternNode ("close", false, a, b, c);//("assembly", false, Engine, Gearbox, Quantity);
					child.setParent(patternNode);
					patternNode.AddPatternChild(child);
					//child.setTransChild(true);
					childV.add(child);
					
					PatternNode child2 = new PatternNode ("close", false, d, b, e);//("b_o_m", false, Gearbox, Cost);
					child2.setParent(patternNode);
					patternNode.AddPatternChild(child2);
					//child2.setTransChild(true);
					childV.add(child2);
				}
				
				pattern.add(childV);
			}
			
			int patternSize = 0;
			for (Vector<PatternNode> vec : pattern) 
			{
				patternSize += vec.size();
			}
			
			int oldsize = p.getRules().size();
			long startTime = System.currentTimeMillis();
			
			IntersectWithProgramOnline iwp = new IntersectWithProgramOnline(p, pattern); 
			long cleanTime = iwp.IntersectWithTransitives();
			
			long endTime = System.currentTimeMillis();
			long durationFullProv = (endTime - startTime);
			long addedRules = (iwp.getP().getRules().size() - oldsize);
			String result = String.format("pattern size: %d\nintersection time: %d\nNumber of rules added: %d\ntime to clean program: %d", 
					patternSize, durationFullProv, addedRules, cleanTime);
			System.out.println(result);
			System.out.println("----------------------------------------------------------------------------------------------------");
			System.out.println(iwp.getP());
			String [] resultArr = result.split("\n");
			
			for (int w = 0; w < resultArr.length; w++) 
			{
				resultArr[w] = resultArr[w].replaceAll( "[^(\\d)+\\.(\\d)+]", "" );
			}
			
			writer.writeNext(resultArr);
			writer.flush();
		}
		
		
		//THIRD EXPERIMENT: FULL BINARY TREE PATTERN up to last layer
		
		/*for (int x = 2; x < 6; x+= 1) //number of layers in binary tree 
		{
			Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
			PatternNode root = new PatternNode ("dealsWith",false, a, b);
			Vector<PatternNode> rootVec = new Vector<PatternNode> ();
			rootVec.add(root);
			pattern.add(rootVec);
			ParseDbRules rules = new ParseDbRules ("amie_yago2_rb_confidence.tsv", 140);
			//Program p = rules.getProgram();
			Program p = new Program();
			
			for (Rule rule : rules.getProgram().getRules()) 
			{
				if (FortyBestRules(rule)) 
				{
					p.addRule(rule);
				}
			}
			
			for (int t = 1; t < x-1; t++) 
			{
				Vector<PatternNode> childV = new Vector<PatternNode> ();
				for (PatternNode patternNode : pattern.lastElement()) 
				{
					PatternNode child = new PatternNode ("dealsWith",false, f, b);
					child.setParent(patternNode);
					patternNode.AddPatternChild(child);
					child.setTransChild(true);
					childV.add(child);

					PatternNode child2 = new PatternNode ("dealsWith",false, a, f);
					child2.setParent(patternNode);
					patternNode.AddPatternChild(child2);
					child2.setTransChild(true);
					childV.add(child2);
				}
				
				pattern.add(childV);
			}
			
			Vector<PatternNode> childV = new Vector<PatternNode> ();
			for (int r = 0; r < pattern.lastElement().size() - 1; r++)
			{
				PatternNode patternNode = pattern.lastElement().get(r);
				PatternNode child = new PatternNode ("dealsWith",false, a, f);
				child.setParent(patternNode);
				patternNode.AddPatternChild(child);
				child.setTransChild(true);
				childV.add(child);

				PatternNode child2 = new PatternNode ("dealsWith",false, f, b);
				child2.setParent(patternNode);
				patternNode.AddPatternChild(child2);
				child2.setTransChild(true);
				childV.add(child2);
			}
			
			PatternNode patternNode = pattern.lastElement().get(pattern.lastElement().size() - 1);
			PatternNode child2 = new PatternNode ("dealsWith",false, f, b);
			child2.setParent(patternNode);
			patternNode.AddPatternChild(child2);
			child2.setTransChild(true);
			childV.add(child2);
			
			pattern.add(childV);
			
			int patternSize = 0;
			for (Vector<PatternNode> vec : pattern) 
			{
				patternSize += vec.size();
			}
			
			//System.out.println("pattern size: " + patternSize);
			//System.out.println(pattern);
			int oldsize = p.getRules().size();
			long startTime = System.currentTimeMillis();
			
			IntersectWithProgramOnline iwp = new IntersectWithProgramOnline(p, pattern); 
			long cleanTime = iwp.IntersectWithTransitives();
			
			long endTime = System.currentTimeMillis();
			long durationFullProv = (endTime - startTime);
			
			long addedRules = (iwp.getP().getRules().size() - oldsize);
			String result = String.format("pattern size: %d\nintersection time: %d\nNumber of rules added: %d\ntime to clean program: %d", 
					patternSize, durationFullProv, addedRules, cleanTime);
			System.out.println(result);
			System.out.println("----------------------------------------------------------------------------------------------------");
			
			String [] resultArr = result.split("\n");
			
			for (int w = 0; w < resultArr.length; w++) 
			{
				resultArr[w] = resultArr[w].replaceAll( "[^(\\d)+\\.(\\d)+]", "" );
			}
			
			writer.writeNext(resultArr);
			writer.flush();
		}
		*/
		//FORTH EXPERIMENT: STAR GRAPH PATTERN
		
		/*for (int x = 1; x < 6; x+= 1) //number of layers in binary tree 
		{
			Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
			PatternNode root = new PatternNode ("dealsWith",false, a, b);
			Vector<PatternNode> rootVec = new Vector<PatternNode> ();
			rootVec.add(root);
			pattern.add(rootVec);
			ParseDbRules rules = new ParseDbRules ("amie_yago2_rb_confidence.tsv", 140);
			Program p = rules.getProgram();
			
			for (Rule rule : p.getRules()) 
			{
				if (rule.getHead().getName().equals("dealsWith")) 
				{
					for (int z = 0; z < 5; z++) 
					{
						rule.getBody().getAtoms().add(dw[z]);
					}
				}
			}
			
			Vector<PatternNode> childV = new Vector<PatternNode> ();
			for (int t = 1; t < x; t++) 
			{
				PatternNode child = new PatternNode ("dealsWith",false, a, b);
				child.setParent(root);
				root.AddPatternChild(child);
				//child.setTransChild(true);
				childV.add(child);
			}
			
			pattern.add(childV);
			
			int patternSize = 0;
			for (Vector<PatternNode> vec : pattern) 
			{
				patternSize += vec.size();
			}
			
			
			int oldsize = p.getRules().size();
			long startTime = System.currentTimeMillis();
			
			IntersectWithProgramOnline iwp = new IntersectWithProgramOnline(p, pattern); 
			long cleanTime = iwp.IntersectWithTransitives();
			
			long endTime = System.currentTimeMillis();
			long durationFullProv = (endTime - startTime);
			int addedRules = (iwp.getP().getRules().size() - oldsize);
			
			String result = String.format("pattern size: %d\nintersection time: %d\nNumber of rules added: %d\ntime to clean program: %d", 
					patternSize, durationFullProv, addedRules, cleanTime);
			System.out.println(result);
			System.out.println("----------------------------------------------------------------------------------------------------");
			
			String [] resultArr = result.split("\n");
			
			for (int w = 0; w < resultArr.length; w++) 
			{
				resultArr[w] = resultArr[w].replaceAll( "[^(\\d)+\\.(\\d)+]", "" );
			}
			
			writer.writeNext(resultArr);
			writer.flush();
		}*/
		
		/*long startTime = System.currentTimeMillis();
		SemiNaive sn = new SemiNaive(1, p, root);
		sn.Run(false, false, true);
		long endTime = System.currentTimeMillis();
		double durationFullProv = (endTime - startTime);
		System.out.println("top-1: " + durationFullProv);
		DerivationTree t = sn.getBestTreeThisStep();
		System.out.println(t.getDerivedFact());
		Pattern pattern = new Pattern();
		pattern.ConvertTreeToPattern(sn.getBestTreeThisStep());
		IntersectWithProgramOnline iwp = new IntersectWithProgramOnline(p, pattern);
		iwp.IntersectNagation();
		
		p = iwp.getP();
		//System.out.println(p);
		//SemiNaive sn2 = new SemiNaive(1, p, root);
		startTime = System.currentTimeMillis();
		sn.Run(false, false, true);
		endTime = System.currentTimeMillis();
		durationFullProv = (endTime - startTime);
		System.out.println("top-2: " + durationFullProv);
		t = sn.getBestTreeThisStep();
		System.out.println(t.getDerivedFact());
		
		pattern.Reset();
		pattern.ConvertTreeToPattern(sn.getBestTreeThisStep());
		IntersectWithProgramOnline iwp2 = new IntersectWithProgramOnline(p, pattern);
		iwp2.IntersectNagation();
		//System.out.println(iwp2.getP());
		
		p = iwp2.getP();
		sn.Run(false, false, true);
		t = sn.getBestTreeThisStep();
		System.out.println(t.getDerivedFact());*/
		
		
		//FIFTH EXPERIMENT: INTERSECT NEGATION 
		
		/*for (int x = 1; x < 8; x+= 1) 
		{
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			MemDB.getInstance().Reset();
			KeyMap.getInstance().Reset();
			Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
			PatternNode root = new PatternNode ("dealsWith",false, a, b);
			Vector<PatternNode> rootVec = new Vector<PatternNode> ();
			rootVec.add(root);
			pattern.add(rootVec);
			ParseDbRules rules = new ParseDbRules ("amie_yago2_rb_confidence.tsv", 140);
			ParseDB rfDB = new ParseDB ("yago2core_facts.clean.notypes.tsv", 100000);//C:\\Users\\amirgilad\\Downloads\\
			int SIZEdb = MemDB.getInstance().Size();
			Program p = rules.getProgram();
			List<Rule> remove = new ArrayList<Rule>();
			for (Rule rule : p.getRules()) 
			{
				if (!dwRule2(rule)) 
				{
					remove.add(rule);
				}
			}
			
			p.getRules().removeAll(remove);
			
			int origSize = p.getRules().size();
			
			Pattern patt = new Pattern();
			//PatternNode root = new PatternNode ("dealsWith",false, a, b);
			SemiNaive sn = new SemiNaive(1, p, root);
			int finalSize = 0;
			double interTime = 0;
			double durationFullProv = 0;
			for (int t = 0; t < x; t++) 
			{
				long startTime = System.currentTimeMillis();
				sn.Run(false, false, true);
				long endTime = System.currentTimeMillis();
				durationFullProv += (endTime - startTime);
				//System.out.println("time for sn: "+durationFullProv);
				//System.out.println("size of DB this step: " + MemDB.getInstance().Size());
				//Pattern pattern = new Pattern();
				if (t < x - 1) 
				{
					patt.ConvertTreeToPattern(sn.getBestTreeThisStep());
					startTime = System.currentTimeMillis();
					IntersectWithProgramOnline iwp = new IntersectWithProgramOnline(p, patt);
					iwp.IntersectNagation();
					endTime = System.currentTimeMillis();
					interTime += (endTime - startTime);
					finalSize = iwp.getP().getRules().size();
				}
			}
			
			int patternSize = 0;
			for (Vector<PatternNode> vec : pattern) 
			{
				patternSize += vec.size();
			}
			
			System.out.println("pattern size: " + patternSize);
			
			finalSize= finalSize - origSize;
			System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("Last Iteration Finished at: " + dateFormat.format(Calendar.getInstance().getTime()));
			System.out.println(String.format("Size of initial DB: %d\nk: %d\nSize of original program: %d\nNumber of rules added in intersection: %d\n" 
					+ "Duration for intersection: %s\nDuration of prov. generation After intersection: %s"
					+ "\nSize of DB After Iteration: %d",SIZEdb, x, origSize, finalSize, Double.toString(interTime), Double.toString(durationFullProv), MemDB.getInstance().Size()));
			System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
		}*/
		
		
		
		//FIFTH EXPIREMENT: TC BINARY
		/*
				for (int x = 2; x < 3; x+= 1) 
				{
					Program p = TCProgram();
					Constant node_1 = new Constant("1", "edge");
					Constant node_2 = new Constant("2", "edge");
					Constant node_99 = new Constant("99", "edge");
					Constant node_50 = new Constant("50", "edge");
					Constant node_56 = new Constant("56", "edge");
					Constant node_55 = new Constant("55", "edge");
					Atom e_1 = new Atom("E", node_1,node_2);
					Atom tc_1 = new Atom("TC", node_2,B);
					Atom tc_2 = new Atom("TC", node_1,node_99);
					
					
					Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
					PatternNode root = new PatternNode ("TC",false, node_99,node_55);
					PatternNode parent = root;
					Vector<PatternNode> rootVec = new Vector<PatternNode> ();
					rootVec.add(root);
					pattern.add(rootVec);
					
					Vector<PatternNode> childV = new Vector<PatternNode> ();
					
					PatternNode child = new PatternNode ("TC",false, C, B);
					child.setParent(parent);
					parent.AddPatternChild(child);
					//child.setTransChild(true);
					childV.add(child);
					
					parent = child;
					
					PatternNode child2 = new PatternNode ("E",false, C, B);
					child2.setParent(parent);
					parent.AddPatternChild(child2);
					//child2.setTransChild(true);
					childV.add(child2);
					pattern.add(childV);
					
					pattern.add(childV);
					
					
					int patternSize = 0;
					for (Vector<PatternNode> vec : pattern) 
					{
						patternSize += vec.size();
					}
					
					int oldsize = p.getRules().size();
					long startTime = System.currentTimeMillis();
					
					IntersectWithProgramOnline iwp = new IntersectWithProgramOnline(p, pattern); 
					long cleanTime = iwp.IntersectWithTransitives();
					
					long endTime = System.currentTimeMillis();
					long durationFullProv = (endTime - startTime);
					int addedRules = (iwp.getP().getRules().size() - oldsize);
					String result = String.format("pattern size: %d\nintersection time: %d\nNumber of rules added: %d\ntime to clean program: %d", 
							patternSize, durationFullProv, addedRules, cleanTime);
					System.out.println(result);
					System.out.println("----------------------------------------------------------------------------------------------------");
					
					String [] resultArr = result.split("\n");
					
					for (int w = 0; w < resultArr.length; w++) 
					{
						resultArr[w] = resultArr[w].replaceAll( "[^(\\d)+\\.(\\d)+]", "" );
					}
					
					writer.writeNext(resultArr);
					writer.flush();
					System.out.println(iwp.getP());
				}
		
		
		*/
		
		//FIFTH EXPIREMENT: TC BINARY
	}
	
	
	private static boolean dwRule (Rule rule)
	{
		boolean b = false;
		String hStr = rule.getHead().getName();
		String [] legalHeads = new String [] {"dealsWith", "hasChild", "isMarriedTo", "directed", "participatedIn", "isPoliticianOf"};
		
		if (Arrays.asList(legalHeads).contains(hStr))
			b = true;
		
		return b;
	} 
	
	
	
	private static boolean dwRule2 (Rule rule)
	{
		boolean b = false;
		String hStr = rule.toString();
		String [] legalRules = new String [] {
				"dealsWith(?a,?b) :- dealsWith(?a,?f), dealsWith(?f,?b).",
				"dealsWith(?a,?b) :- dealsWith(?b,?a).",
				"dealsWith(?a,?b) :- imports(?a,?c), exports(?b,?c).", 
				"dealsWith(?a,?b) :- imports(?a,?c), imports(?b,?c).",
				"dealsWith(?a,?b) :- exports(?a,?c), exports(?b,?c)."};
		
		if (Arrays.asList(legalRules).contains(hStr))
			b = true;
		
		return b;
	} 
	
	
	private static boolean fullCircleRule (Rule rule)
	{
		boolean b = false;
		String [] legal = {
			"isMarriedTo(?a,?b) :- isMarriedTo(?b,?a).",
			"hasChild(?a,?b) :- isMarriedTo(?a,?f), hasChild(?f,?b).",
			"hasChild(?a,?b) :- isMarriedTo(?e,?a), hasChild(?e,?b).",
			"isMarriedTo(?a,?b) :- hasChild(?a,?c), hasChild(?b,?c).",
			
			"imports(?a,?b) :- exports(?a,?b).",
			
			"dealsWith(?a,?b) :- imports(?a,?c), exports(?b,?c).",
			"dealsWith(?a,?b) :- dealsWith(?a,?f), dealsWith(?f,?b).",
			"dealsWith(?a,?b) :- dealsWith(?b,?a).",
					
			"isPoliticianOf(?a,?b) :- wasBornIn(?a,?f), isLocatedIn(?f,?b).",
			"isPoliticianOf(?a,?b) :- diedIn(?a,?f), isLocatedIn(?f,?b).",
			"directed(?a,?b) :- created(?a,?b)."
			};
		
		if (Arrays.asList(legal).contains(rule.toString())) 
		{
			b = true;
		}
		
		return b;
	}
	
	
	private static boolean FortyBestRules (Rule rule)
	{
		boolean b = false;
		String [] legal = {
				"hasChild(?a,?b) :- isMarriedTo(?e,?a), hasChild(?e,?b).",
				"hasChild(?a,?b) :- isMarriedTo(?a,?f), hasChild(?f,?b).",
				"isMarriedTo(?a,?b) :- isMarriedTo(?b,?a).",
				
				"dealsWith(?a,?b) :- dealsWith(?a,?f), dealsWith(?f,?b).",
				//"dealsWith(?a,?b) :- exports(?a,?f), imports(?b,?f).",
				"dealsWith(?a,?b) :- exports(?a,?f), exports(?b,?f).",
				"dealsWith(?a,?b) :- imports(?a,?f), imports(?b,?f).",
				"dealsWith(?a,?b) :- dealsWith(?b,?a).",
				"dealsWith(?a,?b) :- imports(?a,?c), exports(?b,?c).",
				
				"isMarriedTo(?a,?b) :- hasChild(?a,?c), hasChild(?b,?c).",
				"produced(?a,?b) :- directed(?a,?b).",
				"influences(?a,?b) :- influences(?a,?f), influences(?f,?b).",
				"isCitizenOf(?a,?b) :- wasBornIn(?a,?f), isLocatedIn(?f,?b).",
				"diedIn(?a,?b) :- wasBornIn(?a,?b).",
				"directed(?a,?b) :- created(?a,?b).",
				"influences(?a,?b) :- influences(?a,?f), influences(?b,?f).",
				"isPoliticianOf(?a,?b) :- diedIn(?a,?f), isLocatedIn(?f,?b).",
				"isPoliticianOf(?a,?b) :- livesIn(?a,?f), isLocatedIn(?f,?b).",
				"isInterestedIn(?a,?b) :- influences(?a,?f), isInterestedIn(?f,?b).",
				"worksAt(?a,?b) :- graduatedFrom(?a,?b).",
				"influences(?a,?b) :- influences(?e,?a), influences(?e,?b).",
				"isInterestedIn(?a,?b) :- isInterestedIn(?e,?b), influences(?e,?a).",
				"produced(?a,?b) :- created(?a,?b).",
				"isPoliticianOf(?a,?b) :- wasBornIn(?a,?f), isLocatedIn(?f,?b)."
			};
		
		if (Arrays.asList(legal).contains(rule.toString())) 
		{
			b = true;
		}
		
		return b;
	}
	
	
	
	private static String [] tcRules ()
	{
		return new String [] {"TC(?a,?b) :- TC(?a,?f), E(?f,?b).",
			"TC(?a,?b) :- E(?a,?b).",
			"TC(?a,?b) :- TC(?b,?a)."};
	}
	

	
	private static Program TCProgram ()
	{
		Var A = new Var("a", "edge");
		Var B = new Var("b", "edge");
		Var C = new Var("c", "edge");
		
		Atom e_1 = new Atom("E", A,B);
		Atom e_2 = new Atom("E", A,C);
		Atom tc_4 = new Atom("TC", A,C);
		Atom tc_1 = new Atom("TC", A,B);
		Atom tc_2 = new Atom("TC", B,A);
		Atom tc_3 = new Atom("TC", C, B);
		
		Rule r1 = new Rule (tc_1, 1, e_1);
		Rule r2 = new Rule (tc_1, 1, e_2, tc_3);
		Rule r3 = new Rule (tc_1, 1, tc_2);
		
		//alternative!!!
		/*
		Rule r1 = new Rule (tc_1, 1, e_1);
		Rule r2 = new Rule (tc_1, 1, tc_4, tc_3);
		Rule r3 = new Rule (tc_1, 1, tc_2);
		*/
		return new Program(r1, r2, r3);
	}
	
	
	private static Program ExplainProgram ()
	{
		Var A = new Var("a", "Stock");
		Var B = new Var("b", "Price");
		Var C = new Var("c", "Price");
		Var D = new Var("d", "Day");
		Var E = new Var("e", "Day");
		Var G = new Var("g", "dif");
		Var H = new Var("h", "dif");
		Var SEVEN = new Var("7", "NUM");
		Var TWO = new Var("2", "NUM");
		Var OPO = new Var("1.1", "NUM");
		
		Atom vol = new Atom("volatile", A);
		Atom close_1 = new Atom("close", D, A, B);
		Atom close_2 = new Atom("close", E, A, C);
		Atom mul = new Atom("GREATER", D, E);
		Atom greater = new Atom("SUBTRACT", E, D, G);
		Atom week = new Atom("LESS_EQUAL", G, SEVEN);
		Atom divide = new Atom("DIVIDE", B, C, H);
		Atom check_1 = new Atom("DIVIDE", H, TWO);
		
		Atom should_sell = new Atom("should_sell", A);
		Atom movavg_1 = new Atom("movavg", D, A, B);
		Atom movavg_2 = new Atom("movavg", E, A, C);
		Atom check_2 = new Atom("DIVIDE", H, OPO);
		
		Rule r1 = new Rule (vol, 1, close_1, close_2, mul, greater, week, divide, check_1);
		Rule r2 = new Rule (should_sell, 1, movavg_1, movavg_2, mul, greater, week, divide, check_2);
		
		return new Program(r1, r2);
	}
	
	
	private static Program ExplainRecProgram ()
	{
		/*
		b_o_m(?Part, ?C) :- subpart_cost(?Part, ?SubPart, ?C).
		subpart_cost(?Part, ?Part, ?Cost) :- basic_part(?Part, ?Cost).
		subpart_cost(?Part, ?Subpart, ?Cost) :- assembly(?Part, ?Subpart, ?Quantity), b_o_m(?Subpart, ?TotalSubcost), ?Quantity  * ?TotalSubcost = ?Cost, ?Cost < 100000000.
		 */
		Var A = new Var("Part", "Part");
		Var B = new Var("SubPart", "Part");
		Var C = new Var("C", "Cost");
		Var C_2 = new Var("Cost", "Cost");
		Var Q = new Var("Quantity", "Quantity");
		Var Sc = new Var("TotalSubcost", "SubCost");
		
		Atom b_o_m = new Atom("b_o_m", A, C);
		Atom subpart_cost = new Atom("subpart_cost", A, B, C);
		Atom basic_part = new Atom("basic_part", A, C);
		Atom mul = new Atom("MULTIPLY", Q, Sc);
		Atom assembly = new Atom("assembly", A, B, Q);
		Atom b_o_m_2 = new Atom("b_o_m", B, Sc);
		Atom subpart_cost_2 = new Atom("subpart_cost", A, B, C_2);
		
		Rule r1 = new Rule (b_o_m, 1, subpart_cost);
		Rule r2 = new Rule (subpart_cost, 1, basic_part);
		Rule r3 = new Rule (subpart_cost_2, 1, assembly, b_o_m_2, mul);
				
		return new Program(r1, r2, r3);
	}
}
