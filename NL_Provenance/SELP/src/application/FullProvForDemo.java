package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import application.TableDisplayer;
import application.ParseProgram;
import TopK.DerivationTree;
import TopK.EquationTopK;
import edu.uci.ics.jung.graph.util.EdgeType;
import Basics.Atom;
import Basics.Body;
import Basics.MemDB;
import Basics.Program;
import Basics.Provenance;
import Derivation.SemiNaive;


public class FullProvForDemo
{
	
	private static List<Integer> arr = new ArrayList<Integer>();
	
	private static int k = 0;
	
	
	public static boolean fullProv ()
	{
		Map<String,List<String>> facts = TableDisplayer.parseDemoFile("demo1");
		for (List<String> lst : facts.values()) 
		{
			for (String fact : lst) 
			{
				/*if (fact.contains("imports") || fact.contains("exports")) 
				{*/
					MemDB.getInstance().Update( ParseProgram.BuildAtom(fact), true );
				//}
			}
		}
		
		evaluate(2, "");
		return true;
	}
	
	
	
	public static void evaluate (int inputK, String user)
	{
		Program p = getFullProgram();
		SemiNaive sn = new SemiNaive(p);
		
		long startTime = System.currentTimeMillis();
		sn.Run(false, false, false);
		long endTime = System.currentTimeMillis();
		
		double durationFullProv = (endTime - startTime);
		System.out.println("duration full prov: " + durationFullProv);
		System.out.println("size of DB after: " + MemDB.getInstance().Size());
		GraphDisplayer sgv = new GraphDisplayer();
		
		boolean filter = (user.equals("")) ? false : true;
		
		int edgeCnt = 1;
		
		for (Atom key : Provenance.getInstance().getProvenance().keySet()) 
		{
			if ( key.getName().equals("dealsWith") && ((true == filter && key.getParams().get(0).getName().equals(user)) || false == filter) )
			{
				sgv.getGraph().addVertex(key.toString());
				
				for (Body body :  Provenance.getInstance().getProvenance().get(key)) 
				{
					for (Atom atom : body.getAtoms()) 
					{
						sgv.getGraph().addVertex(atom.toString());
						sgv.getGraph().addEdge(Integer.toString(edgeCnt), key.toString(), atom.toString(), EdgeType.DIRECTED);
						edgeCnt++;						
					}
					
					//edgeCnt *= 2;
				}
			}
		}
		
		k = inputK;
		sgv.display(inputK, false);
	}
	
	
	public static void topk (String vertex)
	{
		EquationTopK topk = new EquationTopK(k);
		topk.TopK();
		GraphDisplayer sgv = new GraphDisplayer();
		
		int edgeCnt = 0;
		
		for (Atom key : Provenance.getInstance().getProvenance().keySet()) 
		{
			if (key.toString().equals(vertex)) 
			{
				List<DerivationTree> trees = key.getTrees();
				for (DerivationTree tree :  trees) 
				{
					makeEdges(sgv, tree, edgeCnt);
				}
				
				break;
			}
		}
		
		sgv.display(k, true);
	}
	
	
	
	private static void makeEdges (GraphDisplayer sgv, DerivationTree tree, int edgeCnt)
	{
		String vertexName = tree.getDerivedFact().toString() + " weight: " + tree.getWeight();
		sgv.getGraph().addVertex(vertexName);
		if (null == tree.getChildren()) 
		{
			return;
		}
		
		for (DerivationTree child : tree.getChildren()) 
		{
			String childName = child.getDerivedFact().toString() + " weight: " + child.getWeight();
			sgv.getGraph().addVertex(childName);
			
			if (true == arr.contains(edgeCnt)) 
			{
				edgeCnt = genNewNum();
			}
			 
			sgv.getGraph().addEdge(Integer.toString(edgeCnt), vertexName, childName, EdgeType.DIRECTED);
			//Integer.toString(vertexName.toString().hashCode()*childName.toString().hashCode())
			arr.add(edgeCnt);
			makeEdges(sgv, child, edgeCnt);
			
		}
	}
	
	
	
	private static int genNewNum ()
	{
		return (int)(Math.random()*1000);
	}
	
	
	
	private static Program getSmallProgram()
	{
		String strProg = "dealsWith(a,b) :- imports(a,f), exports(b,f) & 0.6 "
				+ "\ndealsWith(a,b) :- dealsWith(a,f), dealsWith(f,b) &0.9 "
				+ "\ndealsWith(a,b) :- dealsWith(b,a) & 0.9";
		
		return ParseProgram.BuildProgram(strProg);
	}
	
	
	private static Program getFullProgram()
	{
		String strProg = "dealsWith(a,b) :- imports(a,f), exports(b,f) & 0.6 "
				+ "\ndealsWith(a,b) :- dealsWith(a,f), dealsWith(f,b) & 0.9"
				+ "\ndealsWith(a,b) :- dealsWith(b,a) & 0.9\n"
				+ "hasChild(a,b) :- isMarriedTo(e,a), hasChild(e,b) & 0.5\n"
				+"hasChild(a,b) :- isMarriedTo(a,f), hasChild(f,b) & 0.5\n"
				+"isMarriedTo(a,b) :- isMarriedTo(b,a) & 0.5\n"
				+"isMarriedTo(a,b) :- hasChild(a,c), hasChild(b,c) & 0.5\n"
				+"produced(a,b) :- directed(a,b) & 0.5\n"
				+"influences(a,b) :- influences(a,f), influences(f,b) & 0.5\n"
				+"isCitizenOf(a,b) :- wasBornIn(a,f), isLocatedIn(f,b) & 0.5\n"
				+"diedIn(a,b) :- wasBornIn(a,b) & 0.5\n"
				+"directed(a,b) :- created(a,b) & 0.5\n"
				+"influences(a,b) :- influences(a,f), influences(b,f) & 0.5\n"
				+"isPoliticianOf(a,b) :- diedIn(a,f), isLocatedIn(f,b) & 0.5\n"
				+"isPoliticianOf(a,b) :- livesIn(a,f), isLocatedIn(f,b) & 0.5\n"
				+"isInterestedIn(a,b) :- influences(a,f), isInterestedIn(f,b) & 0.5\n"
				+"worksAt(a,b) :- graduatedFrom(a,b) & 0.5\n"
				+"influences(a,b) :- influences(e,a), influences(e,b) & 0.5\n"
				+"isInterestedIn(a,b) :- isInterestedIn(e,b), influences(e,a) & 0.5\n"
				+"produced(a,b) :- created(a,b) & 0.5\n"
				+"isPoliticianOf(a,b) :- wasBornIn(a,f), isLocatedIn(f,b) & 0.5";
		
		return ParseProgram.BuildProgram(strProg);
	}
}
