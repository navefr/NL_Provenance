package Measurements;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import Basics.*;
import Derivation.SemiNaive;
import Derivation.TopDown;
import Parsing.*;
import Pattern.*;
import TopK.EquationTopK;
import au.com.bytecode.opencsv.CSVWriter;

public class OnlineMeasurements 
{
	TopDown td;
	
	IntersectWithProgramOnline iwp;
	
	int k;
	
	public OnlineMeasurements () {}

	
	
	/*************************************************************************************************************/
	/** Title: MeasureAndWriteCSV																	
	/** Description: Apply's the function of the class and writes the result in CSV file 						
	/*************************************************************************************************************/
	
	public void MeasureAndWriteCSV (String path, int i, CSVWriter writer) throws IOException
	{
		/*FileWriter fw = new FileWriter(path);
		CSVWriter writer = new CSVWriter(fw);
		ArrayList<String[]> data = new ArrayList<String[]>();
		data.add(new String[] {"Size of initial DB", "Size of original program", "Size of pattern", "Number of rules added in intersection", 
				"Duration for intersection in seconds", "Size of prov. After intersection", "Size of DB After Iteration", "Time for topk"});
		
		writer.writeAll(data);
		writer.flush();*/
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		/*int i = 0;
		while (i < 950000) 
		{
			if (i >= 50000) 
			{
				i += 50000;
			}
			else
			{
				i += 100;
			}*/
			this.k = 2;
			MemDB.getInstance().Reset();
			Provenance.getInstance().Reset();
			KeyMap.getInstance().Reset();
			/*DB.getInstance().DropTableIfExists("imports");
			DB.getInstance().DropTableIfExists("exports");
			DB.getInstance().DropTableIfExists("dealsWith");*/
			ParseDB rfDB = new ParseDB ("yago2core_facts.clean.notypes.tsv", i);//C:\\Users\\amirgilad\\Downloads\\
			int dbSize = MemDB.getInstance().Size();
			
			String[] array1 = this.MeasureIntersectionWithOriginalProgram();
			String[] array2 = this.MeasureTopDown();
			String[] array3 = this.MeasureTopK(k);
			String[] array123 = new String[array1.length + array2.length + array3.length + 1];
			System.arraycopy(array1, 0, array123, 1, array1.length);
			System.arraycopy(array2, 0, array123, array1.length + 1, array2.length);
			System.arraycopy(array3, 0, array123, array1.length + array2.length + 1, array3.length);
			array123[0] = Integer.toString(dbSize);
			writer.writeNext(array123);
			writer.flush();
			System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("Last Iteration Finished at: " + dateFormat.format(Calendar.getInstance().getTime()));
			System.out.println(String.format("Size of initial DB: %d\nSize of original program: %s\nSize of pattern: %s\nNumber of rules added in intersection: %s\n" 
				+ "Duration for intersection in seconds: %s\nDuration of prov. generation After intersection: %s\nSize of prov. After intersection: %s"
				+ "\nTime for topk: %s\nSize of DB After Iteration: %s", dbSize, array1[0], array1[1], array1[2], array1[3], array2[0], array2[1], array3[0], array2[2]));
			System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
		//}
		
	    //writer.close();
	}
	
	
	/*************************************************************************************************************/
	/** Title: MeasureIntersectionWithOriginalProgram																	
	/** Description: Measures the time and size of added rules due to Intersection With the Original Program						
	/*************************************************************************************************************/
	
	public String[] MeasureIntersectionWithOriginalProgram ()
	{
		Pattern pattern = MakePattern();
		
		//Program p = MakeProgram();
		
		ParseDbRules rules = new ParseDbRules ("amie_yago2_rb_confidence.tsv", 140);
		
		Program p = rules.getProgram();
		
		iwp = new IntersectWithProgramOnline (p, pattern);
		int sizeOfOrigProgram = iwp.getP().getRules().size();
		long startTime = System.currentTimeMillis();
		//iwp.getPattern().Renaming(true);
		iwp.IntersectNoTransitives();
		long endTime = System.currentTimeMillis();
		
		int sizeDiffProgram = iwp.getP().getRules().size() - sizeOfOrigProgram;
		double intersectionTime = (endTime - startTime)/1000;
		
		return new String[] {Integer.toString(p.getRules().size()), 
				Integer.toString(iwp.getPattern().getSize()),
				Integer.toString(sizeDiffProgram), 
				Double.toString(intersectionTime)};
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MeasureTopDown																	
	/** Description:  Measures the time and size of selective Prov. via topDown						
	/*************************************************************************************************************/
	
	public String[] MeasureTopDown ()
	{
		this.td = new TopDown(iwp.getP());
		iwp.getPattern().getPatternVec().firstElement().get(0).setName(iwp.getPattern().getPatternVec().firstElement().get(0).getNewName());
		Atom root = new Atom (iwp.getPattern().getPatternVec().firstElement().get(0));
		SemiNaive sn = new SemiNaive(1, iwp.getP(), root);
		
		long startTime = System.currentTimeMillis();
		sn.Run(false, false, true);
		//td.Run(root);	//top-down on root of intersection
		long endTime = System.currentTimeMillis();
		
		double durationFullProv = (endTime - startTime);
		
		long numOfAtoms =  Provenance.getInstance().GetProvSize();
		
		
		return new String[] {Double.toString(durationFullProv), 
				Long.toString(numOfAtoms), 
				Long.toString(MemDB.getInstance().Size())};//DB.getInstance().Size()
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MeasureTopK																					
	/** Description: 									
	/*************************************************************************************************************/
	
	public String[] MeasureTopK (int k)
	{
		EquationTopK topk = new EquationTopK(k);
		long startTime = System.currentTimeMillis();
		//topk.TopK();
		long endTime = System.currentTimeMillis();
		double topKTime = (endTime - startTime)/1000;
	
		return new String[] {Double.toString(topKTime)};
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MakePattern																	
	/** Description: Builds the pattern   						
	/*************************************************************************************************************/
	
	public Pattern MakePattern ()
	{
		////////////////////////////////////////////////
		Constant Canada = new Constant("Canada", "Country");
		Constant Andorra = new Constant("Andorra", "Country");
		PatternNode root = new PatternNode ("dealsWith",false, Canada, Andorra);
		PatternNode child1 = new PatternNode ("dealsWith",false, Andorra, Canada);
		root.setChildren(child1);
		child1.setParent(root); 

		Vector<PatternNode> rootVec = new Vector<PatternNode> ();
		rootVec.add(root);

		Vector<PatternNode> childVec = new Vector<PatternNode> ();
		childVec.add(child1);

		Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
		pattern.add(rootVec); 
		pattern.add(childVec); 
		////////////////////////////////////////////////
		
		return new Pattern(pattern);
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: MakeProgram																					
	/** Description: Builds the program out of string									
	/*************************************************************************************************************/
	
	public Program MakeProgram ()
	{
		/*String input = "dealsWith(a, b):- imports(a, c), exports(b, c) & 0.8"
				+ " \n dealsWith(a, b):- dealsWith(a, f), dealsWith(f, b) & 0.5 "
				+ "\n dealsWith(a, b):- dealsWith(b, a) & 1";
		ParseProgram pp = new ParseProgram(input);*/
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
		
		
		Rule r1 = new Rule (dealsWith1_1, 0.8, imported, exported);
		Rule r2 = new Rule (dealsWith1_2, 0.5, dealsWith2, dealsWith3);
		Rule r3 = new Rule (dealsWith1_3, 1, dealsWith4);
		return new Program(r1,r2,r3);
	}
}
