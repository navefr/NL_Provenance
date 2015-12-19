package application;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Basics.Atom;
import Basics.Constant;
import Basics.Program;
import Basics.Proton;
import Basics.Rule;
import Basics.Var;
import Pattern.PatternNode;



public class ParseProgram 
{
/*	public static void main (String [] args)
	{
		String str = "dealsWith(a,b) :- imports(a,f), imports(b,f) & 0.6"
				+ "\ndealsWith(a,b) :- dealsWith(a,f), dealsWith(f,b) &0.9"
				+ "\ndealsWith(a,b) :- dealsWith(b,a) & 1";
		System.out.println(BuildProgram(str));	
	}
*/

	/*************************************************************************************************************/
	/** Title: BuildProgram																				
	/** Description: Checks that the user input is of legal form 			
	/*************************************************************************************************************/
	
	public static Program BuildProgram (String str)
	{
		String [] strRules = str.split("\n");
		Rule [] rules = new Rule [strRules.length];
		for (int i = 0; i < rules.length; i++) 
		{
			rules[i] = BuildRule(strRules[i]);
		}
		
		return new Program(rules);
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: BuildRule																				
	/** Description: Takes a string and turns it into a Rule 			
	/*************************************************************************************************************/
	
	private static Rule BuildRule (String str)
	{
		String [] sepWeight = str.split("&");
		double weight = Double.parseDouble(sepWeight[1]);
		Pattern pattern = Pattern.compile("[a-zA-Z0-9].*?\\((.*?)\\)");
		Matcher matcher = pattern.matcher(sepWeight[0]);
		List<Atom> atoms = new ArrayList<Atom>();
		
		while (matcher.find()) 
		{
			String[] nameParams = matcher.group().split("\\(");
			String name = nameParams[0].replaceAll("[^A-Za-z]+", "").trim();
			nameParams[1] = nameParams[1].substring(0, nameParams[1].length()-1);
			String[] params = nameParams[1].split(",");
			atoms.add( BuildAtom (name, params) );
		}
		
		return new Rule (atoms.get(0), weight, atoms.subList(1, atoms.size()));
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: BuildAtom																				
	/** Description: Takes a name string and an array of parameters and turns it into an Atom 			
	/*************************************************************************************************************/
	
	private static Atom BuildAtom (String name, String [] params)
	{
		Proton [] vars = new Proton [params.length];
		String [] cats = SelectCategories(name);
		for (int i = 0; i < params.length; i++) 
		{
			vars[i] = ParseParam(params[i].trim(), cats[i]); 
		}
		
		return new Atom (name, vars);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: BuildPatternNode																				
	/** Description: Takes a name string and an array of parameters and turns it into an PatternNode 			
	/*************************************************************************************************************/
	
	public static PatternNode BuildPatternNode (String text)
	{
		String[] nameParams = text.split("\\(");
		String name = nameParams[0].replaceAll("[^A-Za-z]+", "");
		nameParams[1] = nameParams[1].substring(0, nameParams[1].length()-1);
		String[] params = nameParams[1].split(",");
		String [] cats = SelectCategories(name);
		
		Proton [] vars = new Proton [params.length];
		for (int i = 0; i < params.length; i++) 
		{
			vars[i] = ParseParam(params[i].trim(), cats[i]); 
		}
		
		return new PatternNode (name, false, vars);
	}
	
	
	/*************************************************************************************************************/
	/** Title: BuildAtom																				
	/** Description: Takes a name string and turns it into an Atom 			
	/*************************************************************************************************************/
	
	public static Atom BuildAtom (String text)
	{
		String[] nameParams = text.split("\\(\\'");
		String name = nameParams[0].replaceAll("[^A-Za-z]+", "");
		nameParams[1] = "'" + nameParams[1].substring(0, nameParams[1].length()-1);
		String res = nameParams[1].replaceAll("','", "''");
		Pattern pattern = Pattern.compile("'([^']+)'|\\S+");
		Matcher matcher = pattern.matcher(res);
		
		String [] cats = SelectCategories(name);
		
		Proton [] vars = new Proton [2];//hard coded. not generic.
		int i = 0;
		while (matcher.find()) 
		{
			String param = matcher.group().replace("'", "").trim();
			if (param.length() > 1) 
			{
				vars[i] = ParseParam(param, cats[i++]);
			} 
		}

		return new Atom (name, false, vars);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: SelectCategories																				
	/** Description: Returns the categories of the params according to the atom name. 			
	/*************************************************************************************************************/
	
	private static String [] SelectCategories (String atomName)
	{
		String [] retVal = new String [2];
		
		switch (atomName) 
		{
		case "dealsWith":
			retVal = new String [] {"Country", "Country"};
			break;
		case "imports":
			retVal = new String [] {"Country", "Product"};
			break;
		case "exports":
			retVal = new String [] {"Country", "Product"};
			break;
		case "isLeaderOf":
			retVal = new String [] {"person", "place"};
			break;
		case "graduatedFrom":
			retVal = new String [] {"person", "university"};
			break;
		case "isLocatedIn":
			retVal = new String [] {"place", "place"};
			break;
		case "directed":
			retVal = new String [] {"director", "movie"};
			break;
		case "isCitizenOf":
			retVal = new String [] {"person", "Country"};
			break;
		case "created":
			retVal = new String [] {"director", "movie"};
			break;
		case "wasBornIn":
			retVal = new String [] {"person", "city"};
			break;
		case "isPoliticianOf":
			retVal = new String [] {"person", "state"};
			break;
		case "produced":
			retVal = new String [] {"director", "movie"};
			break;
		case "livesIn":
			retVal = new String [] {"person", "place"};
			break;
		case "diedIn":
			retVal = new String [] {"person", "city"};
			break;
		case "hasChild":
			retVal = new String [] {"person", "child"};
			break;
		case "isMarriedTo":
			retVal = new String [] {"person", "person"};
			break;
		case "worksAt":
			retVal = new String [] {"person", "university"};
			break;
		case "influences":
			retVal = new String [] {"person", "person"};
			break;
		case "isInterestedIn":
			retVal = new String [] {"person", "subject"};
			break;
		default:
			throw new IllegalArgumentException();
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: ParseParam																				
	/** Description: Returns the correct param according to the name and category. 			
	/*************************************************************************************************************/
	
	private static Proton ParseParam (String param, String cat)
	{
		Proton retVal;
		boolean isVar = (param.length() > 1) ? false : true;
		param.trim();
		
		if (true == isVar) 
		{
			retVal = new Var (param, cat);
		}
		
		else
		{
			retVal = new Constant (param, cat);
		}
		
		return retVal;
	}
}
