package Measurements;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import com.javamex.classmexer.MemoryUtil;

import au.com.bytecode.opencsv.CSVWriter;
import Basics.*;
import Circuit.Circuit;
import Derivation.SemiNaive;
import Parsing.*;
import Pattern.*;
import TopK.TopKAlgo;

import com.javamex.classmexer.MemoryUtil;
public class OfflineMeasurements 
{	
	Pattern pattern;
	
	Circuit intersectedCircuit;
	
	public OfflineMeasurements () {}

	
	
	/*************************************************************************************************************/
	/** Title: MeasureAndWriteCSV																	
	/** Description: Apply's the function of the class and writes the result in CSV file  						
	/*************************************************************************************************************/
	
	public void MeasureAndWriteCSV (String path, int i, CSVWriter writer) throws IOException
	{
		//DB.getInstance().Reset();
		/*DB.getInstance().DropTableIfExists("imports");
		DB.getInstance().DropTableIfExists("exports");
		DB.getInstance().DropTableIfExists("dealsWith");*/
		
		/*FileWriter fw = new FileWriter(path);
		CSVWriter writer = new CSVWriter(fw);
		ArrayList<String[]> data = new ArrayList<String[]>();
		data.add(new String[] {"Size of initial DB", "k",  "Duration of prov. generation in milliseconds",
				"Duration of intersection in seconds", "Size of full provenance", "Size of intersected provenance", "Size of full circuit", "size Of intersected circuit", "Size of DB After Iteration", "Time For Top-k"});
		
		writer.writeAll(data);*/
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		/*for (int k = 2; k <3; k++)
		{*/
		int k = 0;
		MemDB.getInstance().Reset();
		Provenance.getInstance().Reset();
		KeyMap.getInstance().Reset();

		//AtomsDerived.getInstance().Reset();


		/*DB.getInstance().Reset();
				DB.getInstance().DropTableIfExists("imports");
				DB.getInstance().DropTableIfExists("exports");
				DB.getInstance().DropTableIfExists("dealsWith");*/
		//ParseDB rfDB = new ParseDB ("yago2core_facts.clean.notypes.tsv", i);//C:\\Users\\amirgilad\\Downloads\\
		Program p = LoadExplainProgram ();//SettingChooser.writeProg(i);//IrisProgram();
		//LoadTcDB(100*i);
		LoadExplainDB(i);
		
		int dbSize = MemDB.getInstance().Size();
		String[] array1 = this.MeasureFullProv(p);
		String[] array2 = this.MeasureIntersectionWithFullProv(i, k);
		//String[] array3 = this.MeasureTopK(k);
		String[] array1and2and3 = new String[array1.length + array2.length + 2];//array3.length + 2];
		System.arraycopy(array1, 0, array1and2and3, 2, array1.length);
		System.arraycopy(array2, 0, array1and2and3, array1.length + 2, array2.length);
		//System.arraycopy(array3, 0, array1and2and3, array1.length + array2.length + 2, array3.length);
		array1and2and3[0] = Integer.toString(dbSize);
		array1and2and3[1] = Integer.toString(k);
		writer.writeNext(array1and2and3);
		writer.flush();
		
		long memUsed = Provenance.getInstance().Memory() + KeyMap.getInstance().Memory(); /*MemoryUtil.deepMemoryUsageOf(Provenance.getInstance()) / 1024L;
		memUsed += MemoryUtil.deepMemoryUsageOf(KeyMap.getInstance()) / 1024L;
		memUsed += MemoryUtil.deepMemoryUsageOf(MemDB.getInstance()) / 1024L;*/
		
		//Provenance.getInstance().Print();
		
		System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
		System.out.println("Last Iteration Finished at: " + dateFormat.format(Calendar.getInstance().getTime()));
		System.out.println(String.format("Size of initial DB: %d\nk: %d\nDuration of prov. generation in seconds: %s\nDuration of intersection in seconds: %s\n" 
				+ "Number of derivations stored in initial provenance: %s\nSize of DB After Iteration: %s\nMemory used: %d",//Top-k time: %s\n
				dbSize, k, array1[0], array2[0], array2[1], array2[3], memUsed));//array3[0], 
		System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

			//DB.getInstance().Reset();
		//}
		
		//writer.close();
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MeasureFullProv																					
	/** Description: Measures the time and size of Full Prov. via semiNaive									
	/*************************************************************************************************************/
	
	public String[] MeasureFullProv (Program p)
	{
		//Program p = MakeProgram();
		
		//ParseDbRules rules = new ParseDbRules ("amie_yago2_rb_confidence.tsv", 140);
		
		//Program p = rules.getProgram();
		SemiNaive sn = new SemiNaive(p);
		
		long startTime = System.currentTimeMillis();
		sn.Run(false, false, false);
		long endTime = System.currentTimeMillis();
		
		double durationFullProv = (endTime - startTime);
		System.out.println("duration full prov: " + durationFullProv);
		System.out.println("size of DB after: " + MemDB.getInstance().Size());
		return new String[] {Double.toString(durationFullProv)};
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MeasureIntersectionWithFullProv																					
	/** Description: Measures the time of Intersection With Full Prov. and the size of intersected rules									
	/*************************************************************************************************************/
	
	public String[] MeasureIntersectionWithFullProv (int sizeOfDB, int k)
	{
		pattern = MakePattern();
		IntersectWithProgramOffline iwp = new IntersectWithProgramOffline (pattern, k);
		long sizeOfFullProv = Provenance.getInstance().GetProvSize();
		long sizeOfFullCircuit = 0;//this.sn.getParser().getC().Size();
		
		/*this.sn.getParser().getC().getCircuit().clear();
		this.sn.getParser().getC().getMostRecentOrNodes().clear();*/
		
		long startTime = System.currentTimeMillis();
		System.out.println("size of prov: " + Provenance.getInstance().GetProvSize());
		//int DBSize = DB.getInstance().Size();
		int DBSize = MemDB.getInstance().Size();
		//iwp.IntersectNoTransitives(sizeOfDB);
		long endTime = System.currentTimeMillis();
		
		long sizeOfIntersectedProv = iwp.sizeOfIntersectedProvenance;
		//intersectedCircuit = iwp.getC();
		
		double intersectionTime = (endTime - startTime)/1000;
		long sizeOfIntersectedCircuit = 0;//iwp.getC().Size();
		
		long memUsed = 0;/*MemoryUtil.deepMemoryUsageOf(Provenance.getInstance()) / 1024L;
		memUsed += MemoryUtil.deepMemoryUsageOf(KeyMap.getInstance()) / 1024L;
		memUsed += MemoryUtil.deepMemoryUsageOf(MemDB.getInstance()) / 1024L;*/
		
		System.out.println("Memory used: " + memUsed);
		
		return new String[] {Double.toString(intersectionTime), Long.toString(sizeOfFullProv), Long.toString(sizeOfIntersectedProv), 
				Integer.toString(DBSize)};//Long.toString(sizeOfFullCircuit), Long.toString(sizeOfIntersectedCircuit), 
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: MeasureTopK																					
	/** Description: 									
	/*************************************************************************************************************/
	
	public String[] MeasureTopK (int k)
	{
		TopKAlgo topk = new TopKAlgo();
		long startTime = System.currentTimeMillis();
		//topk.RunTopKForAtom(pattern.getPatternVec().get(0).firstElement(), k, intersectedCircuit.getCircuit());
		topk.RunTopKForPattern(pattern, k, intersectedCircuit.getCircuit());
		long endTime = System.currentTimeMillis();
		double topKTime = (endTime - startTime)/1000;
	
		return new String[] {Double.toString(topKTime)};//, Long.toString(DBSize)
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
		//PatternNode grandchild1 = new PatternNode ("dealsWith",false, Canada, Canada);
		root.setChildren(child1);
		child1.setParent(root); 
		/*child1.setChildren(grandchild1); 
		grandchild1.setParent(child1); */
		
		
		Vector<PatternNode> rootVec = new Vector<PatternNode> ();
		rootVec.add(root);

		Vector<PatternNode> childVec = new Vector<PatternNode> ();
		childVec.add(child1);
		
		/*Vector<PatternNode> grandchildVec = new Vector<PatternNode> ();
		grandchildVec.add(grandchild1);*/

		Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
		pattern.add(rootVec); 
		pattern.add(childVec);
		//pattern.add(grandchildVec);
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
		ParseProgram pp = new ParseProgram(input);
		return pp.getProgram();*/
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
		return new Program(r1, r2 ,r3);
	}
	
	
	private Program IrisProgram ()
	{
		Var A = new Var("a", "Country");
		Var B = new Var("b", "Country");
		Var C = new Var("c", "Country");
		Var D = new Var("d", "Country");
		Var E = new Var("e", "Country");		
		
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
		Rule r3 = new Rule (q_1, 1, r);
		Rule r4 = new Rule (q_2, 1, r);
		Rule r5 = new Rule (q_3, 1, r);
		Rule r6 = new Rule (q_4, 1,r);
		Rule r7 = new Rule (q_5, 1, r);
		Rule r8 = new Rule (r, 1, ra,rb);
	
		return new Program(r1, r2, r3, r4,r5,r6,r7, r8);
	}
	
	
	private void LoadIrisDB (int size)
	{
		for (int i = 0; i < size; i++) 
		{
			MemDB.getInstance().InsertToTable("p", new ArrayList<String>(
				    Arrays.asList("abdc" + i)));
		}
	}
	
	
	private Program TCProgram ()
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
	
	
	private void LoadTcDB (int size)
	{
		for (int i = 0; i < size; i++) 
		{
			MemDB.getInstance().InsertToTable("E", new ArrayList<String>(
				    Arrays.asList(Integer.toString(i), Integer.toString(i+1))));
			//System.out.println("E(" + Integer.toString(i) + "," + Integer.toString(i+1) + ")");
			/*MemDB.getInstance().InsertToTable("E", new ArrayList<String>(
				    Arrays.asList(Integer.toString(i+1), Integer.toString(i))));*/
		}
		
		MemDB.getInstance().InsertToTable("E", new ArrayList<String>(
			    Arrays.asList(Integer.toString(size), Integer.toString(0))));
	}
	
	

	private Program LoadExplainProgram ()
	{
		String str = "bom(P, C) :- subpart_cost(P, S, C) & 1"
				+ "\nsubpart_cost(P, P, C) :- basic_part(P, C) & 0.8"
				+ "\nsubpart_cost(P, S, C) :- assembly(P, S, Q), bom(S, T), Mul(Q, T, C) LEQ(C, 100000000000) & 0.5";
		return ParseProgram.BuildProgram(str);
	}
	
	
	private void LoadExplainDB (int size)
	{
		String demoFile = "C:\\Users\\amirgilad\\WORKSPACESVN\\CopyTopK\\explain_prog_rec_" + size + ".iris";
		BufferedReader br = null;
		String line = "";

		try 
		{
			br = new BufferedReader(new FileReader(demoFile));
			while ((line = br.readLine()) != null) 
			{
				MemDB.getInstance().Update(ParseProgram.BuildAtom(line), true);
			}
		}
		
		catch (FileNotFoundException e) 
		{
			System.out.println("OfflineMeasurements::LoadExplainDB:: Could not find file: " + demoFile);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			if (br != null) 
			{
				try 
				{
					br.close();
				} 
				catch (IOException e) 
				{
					System.out.println("OfflineMeasurements::LoadExplainDB:: Could not close file");
				}
			}
		}
		
		System.out.println("Done Parsing explain_prog_rec_" + size + ".iris");
	}
}
