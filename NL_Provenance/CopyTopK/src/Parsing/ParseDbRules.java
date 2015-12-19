package Parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Basics.*;

public class ParseDbRules 
{
	Program program;
	
	Map<String, String []> relTocat = new HashMap<String, String []>(); 

	/*************************************************************************************************************/
	/** Title: ParseProgram																				
	/** Description: Takes YAGO file and turns it into a Program.		
	/*************************************************************************************************************/
	
	public ParseDbRules (String path, int rowNumLimit)
	{
		this.program = new Program();
		FillRelTocat();
		try 
		{
			System.out.println("Loading DB rules...");
			this.ReadFileToProgram(path, rowNumLimit);
			System.out.println("Finished loading DB rules");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		//System.out.println(this.program);
	}
	
	
	
	public ParseDbRules ()
	{
		FillRelTocat();
	}
	
	
	/*************************************************************************************************************/
	/** Title: ReadFileToProgram																				
	/** Description: Takes a path of tsv. file and convert it into DATALOG rules 			
	/*************************************************************************************************************/
	
	public void ReadFileToProgram (String path, int rowNumLimit) throws Exception
	{
		//?a  <dealsWith>  ?f  ?f  <dealsWith>  ?b   => ?a  <dealsWith>  ?b
		//==>
		//dealsWith(a,b) :- dealsWith(a,f) , dealsWith(f,b).
		
		BufferedReader bReader = new BufferedReader(new FileReader(path));
		String line;
		
		String regular1 = "[?](.+)\\s+<(.+)>.+[?](.+)\\s+[?](.+)<(.+)>.+[?](.+)=>\\s+[?](.+)<(.+)>\\s\\s[?](\\w)\\t\\d+\\.\\d+\\t(\\d+\\.\\d+).+";
		String regular2 = "[?](.+)\\s+<(.+)>.+[?](.+)=>\\s+[?](.+)<(.+)>\\s\\s[?](\\w)\\t\\d+\\.\\d+\\t(\\d+\\.\\d+).+";
		String regular3 = "(.+)\\s+\\((.+),?(.+)";
		Pattern pattern1 = Pattern.compile(regular1);
		Pattern pattern2 = Pattern.compile(regular2);
		Matcher m1, m2;
		
		int rowNum = 0;
		while ((line = bReader.readLine()) != null && rowNum < rowNumLimit) 
		{
			m1 = pattern1.matcher(line);
			m2 = pattern2.matcher(line);
			if (m1.find()) 
			{
				Var a = new Var(m1.group(1).trim(), relTocat.get(m1.group(2))[0]);
				Var b = new Var(m1.group(3).trim(), relTocat.get(m1.group(2))[1]);
				Var c = new Var(m1.group(4).trim(), relTocat.get(m1.group(5))[0]);
				Var d = new Var(m1.group(6).trim(), relTocat.get(m1.group(5))[1]);
				Var e = new Var(m1.group(7).trim(), relTocat.get(m1.group(8))[0]);
				Var f = new Var(m1.group(9).trim(), relTocat.get(m1.group(8))[1]);
				Atom bodyAtom_1 = new Atom(m1.group(2), a,b);
				Atom bodyAtom_2 = new Atom(m1.group(5).trim(), c,d);
				Atom head = new Atom(m1.group(8), e,f);
				this.program.addRule(new Rule (head, Double.parseDouble(m1.group(10)), bodyAtom_1, bodyAtom_2));
				rowNum++;
			}
			
			else if (m2.find()) 
			{
				Var a = new Var(m2.group(1).trim(), relTocat.get(m2.group(2))[0]);
				Var b = new Var(m2.group(3).trim(), relTocat.get(m2.group(2))[1]);
				Var c = new Var(m2.group(4).trim(), relTocat.get(m2.group(5))[0]);
				Var d = new Var(m2.group(6).trim(), relTocat.get(m2.group(5))[1]);
				Atom bodyAtom = new Atom(m2.group(2), a,b);
				Atom head = new Atom(m2.group(5).trim(), c,d);
				this.program.addRule(new Rule (head, Double.parseDouble(m2.group(7)), bodyAtom));
				rowNum++;
			}
		}
		
		bReader.close();
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: ReadFileToProgram																				
	/** Description: Takes a path of tsv. file and convert it into DATALOG rules 			
	/*************************************************************************************************************/
	
	public void ReadFileToProgram2 (String path, int rowNumLimit) throws Exception
	{
		//?a  <dealsWith>  ?f  ?f  <dealsWith>  ?b   => ?a  <dealsWith>  ?b
		//==>
		//dealsWith(a,b) :- dealsWith(a,f) , dealsWith(f,b).
		
		BufferedReader bReader = new BufferedReader(new FileReader(path));
		String line;
		
		String regular1 = "<(.+)>\\s+<(.+)>\\s+<(.+)>";
		Pattern pattern1 = Pattern.compile(regular1);
		Matcher m1;
		
		int rowNum = 0;
		while ((line = bReader.readLine()) != null && rowNum < rowNumLimit) 
		{
			m1 = pattern1.matcher(line);
			if (m1.find()) 
			{
				Var a = new Var(m1.group(1).trim(), relTocat.get(m1.group(2))[0]);
				Var b = new Var(m1.group(3).trim(), relTocat.get(m1.group(2))[1]);
				Var c = new Var(m1.group(4).trim(), relTocat.get(m1.group(5))[0]);
				Var d = new Var(m1.group(6).trim(), relTocat.get(m1.group(5))[1]);
				Var e = new Var(m1.group(7).trim(), relTocat.get(m1.group(8))[0]);
				Var f = new Var(m1.group(9).trim(), relTocat.get(m1.group(8))[1]);
				Atom bodyAtom_1 = new Atom(m1.group(2), a,b);
				Atom bodyAtom_2 = new Atom(m1.group(5).trim(), c,d);
				Atom head = new Atom(m1.group(8), e,f);
				this.program.addRule(new Rule (head, Double.parseDouble(m1.group(10)), bodyAtom_1, bodyAtom_2));
				rowNum++;
			}
		}
		
		bReader.close();
	}

	/*************************************************************************************************************/
	/** Title: FillRelTocat																				
	/** Description: 		
	/*************************************************************************************************************/
	
	public void FillRelTocat ()
	{
		relTocat.put("isMarriedTo", new String [] {"person", "person"});
		relTocat.put("hasChild", new String [] {"person", "child"});
		relTocat.put("directed", new String [] {"director", "movie"});
		relTocat.put("created", new String [] {"director", "movie"});
		relTocat.put("produced", new String [] {"director", "movie"});
		relTocat.put("actedIn", new String [] {"director", "movie"});
		relTocat.put("livesIn", new String [] {"person", "place"});
		relTocat.put("dealsWith", new String [] {"Country", "Country"});	
		relTocat.put("hasCapital", new String [] {"Country", "city"});
		relTocat.put("isLocatedIn", new String []  {"city", "Country"});
		relTocat.put("hasOfficialLanguage", new String []  {"Country", "language"}); 
		relTocat.put("worksAt", new String []  {"person", "university"});  
		relTocat.put("hasAcademicAdvisor", new String []  {"person", "person"}); 
		relTocat.put("isCitizenOf", new String []  {"person", "Country"}); 
		relTocat.put("influences", new String []  {"person", "person"}); 
		relTocat.put("graduatedFrom", new String []  {"person", "university"}); 
		relTocat.put("wasBornIn", new String []  {"person", "city"});  
		relTocat.put("diedIn", new String []  {"person", "city"}); 
		relTocat.put("imports", new String []  {"Country", "Product"}); 
		relTocat.put("exports", new String []  {"Country", "Product"}); 
		relTocat.put("participatedIn", new String []  {"figure" , "event"}); 
		relTocat.put("hasCurrency", new String []  {"region", "currency"}); 
		relTocat.put("isPoliticianOf", new String []  {"person", "state"}); 
		relTocat.put("isLeaderOf", new String []  {"person", "place"}); 
		relTocat.put("isInterestedIn", new String []  {"person", "subject"}); 
		relTocat.put("hasWonPrize", new String []  {"person", "prize"});
		relTocat.put("isKnownFor", new String []  {"person", "prize"});
		relTocat.put("hasGeonamesId", new String []  {"Country", "id"});
		relTocat.put("hasLanguageCode", new String []  {"Language", "code"});
		//????????
		relTocat.put("isLocatedIn", new String []  {"place", "place"}); 
		relTocat.put("TC", new String []  {"edge", "edge"});
		relTocat.put("E", new String []  {"edge", "edge"});
		relTocat.put("p", new String []  {"Country"});
		relTocat.put("q", new String []  {"Country"});
		relTocat.put("ra", new String []  {"Country", "Country", "Country", "Country", "Country"});
		relTocat.put("rb", new String []  {"Country", "Country", "Country", "Country", "Country"});
		relTocat.put("r", new String []  {"Country", "Country", "Country", "Country", "Country"});
		relTocat.put("isInterestedIn", new String []  {"User", "Subject"});
		relTocat.put("follows", new String []  {"User", "User"});
		relTocat.put("numFollowers", new String []  {"User", "Number"});
		relTocat.put("tweet", new String []  {"User", "Tweet"});
		relTocat.put("RT", new String []  {"User", "Tweet"});
		relTocat.put("basicpart", new String []  {"Part", "Price"});
		relTocat.put("assembly", new String []  {"Part", "Part", "Price"});
		relTocat.put("subpartcost", new String []  {"Part", "Part", "Price"});
		relTocat.put("bom", new String []  {"Part", "Price"});
		relTocat.put("Mul", new String []  {"numder", "numder", "numder"});
		relTocat.put("LEQ", new String []  {"numder", "numder"});
	}
	
	public Program getProgram() 
	{
		return program;
	}


	
	public void setProgram(Program program) 
	{
		this.program = program;
	}


	
	public Map<String, String[]> getRelTocat() {
		return relTocat;
	}



	public void setRelTocat(Map<String, String[]> relTocat) {
		this.relTocat = relTocat;
	}
	
	
}
