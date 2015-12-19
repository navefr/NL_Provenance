package Measurements;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import Basics.Atom;
import Basics.MemDB;
import Basics.Program;
import Basics.Proton;
import Basics.Rule;
import Parsing.ParseDB;
import Parsing.ParseDbRules;

public class SettingChooser 
{	
	public static Program writeProg (int progNum)
	{
		ParseDB rfDB = new ParseDB ("yago2core_facts.clean.notypes.tsv", 900000);
		ParseDbRules rules = new ParseDbRules ("amie_yago2_rb_confidence.tsv", 140);
		List<Atom> toRemove = new ArrayList<Atom>();
		for (String key : MemDB.getInstance().getFacts().keySet()) 
		{
			if (!relFortyBest(key)) 
			{
				MemDB.getInstance().getFacts().get(key).clear();
			}
			
			else
			{
				Set<Atom> set = MemDB.getInstance().getFacts().get(key);
				int relativeSizeRel = (int) ( ( set.size() ) / ( 20 - progNum ) );
				int cnt = 0;
				for (Atom fact : set) 
				{
					if (legalFact(fact))
					{
						if (true == relFortyBest(fact.getName()) && cnt < relativeSizeRel)
						{
							cnt++;
						}

						else toRemove.add(fact);
					}
					
					else toRemove.add(fact);
				}

				set.removeAll(toRemove);
				toRemove.clear();
			}
		}
		System.out.println("size of db: " + MemDB.getInstance().Size());
		List<Rule> toRemoveRule = new ArrayList<Rule>();
		for (Rule rule : rules.getProgram().getRules()) 
		{
			if (!FortyBestRules(rule))
			{
				toRemoveRule.add(rule);
			}
		}
		
		rules.getProgram().getRules().removeAll(toRemoveRule);
		
		System.out.println("program: ");
		System.out.println(rules.getProgram());
		
		return rules.getProgram();
	}
	
	
	
	private static boolean legalFact (Atom fact)
	{
		boolean b = true;
		for (Proton p : fact.getParams()) 
		{
			if (p.getName().contains("'") || p.getName().contains("\"")) 
			{
				b = false;
			}
		}
		
		return b;
	}
	
	
	private static boolean relDealsWith (Atom fact)
	{
		boolean b = true;
		if(!fact.getName().equals("dealsWith") && 
				!fact.getName().equals("imports") &&
				!fact.getName().equals("exports"))
			b = false;	
		
		return b;
	}
	
	
	private static boolean relIsPoliticianOf (Atom fact)
	{
		boolean b = true;
		if(
				!fact.getName().equals("diedIn") &&
				!fact.getName().equals("wasBornIn") &&
				!fact.getName().equals("isPoliticianOf") &&
				!fact.getName().equals("livesIn") &&
				!fact.getName().equals("isLocatedIn"))
			b = false;	
		
		return b;
	}
	
	
	private static boolean irrelDealsWith (Atom fact)
	{
		boolean b = true;
		if(!fact.getName().equals("isMarriedTo") && 
				!fact.getName().equals("hasChild") &&
				!fact.getName().equals("isLocatedIn") &&
				!fact.getName().equals("Influences") &&
				!fact.getName().equals("isCitizenOf") &&
				!fact.getName().equals("isLeaderOf") &&
				!fact.getName().equals("isLocatedIn") &&
				!fact.getName().equals("hasCapitol") &&
				!fact.getName().equals("directed") &&
				!fact.getName().equals("created") &&
				!fact.getName().equals("isInterestedIn") &&
				!fact.getName().equals("produced") &&
				!fact.getName().equals("worksAt") &&
				!fact.getName().equals("graduatedFrom") &&
				!fact.getName().equals("isMarriedTo") && 
				!fact.getName().equals("hasChild") &&
				!fact.getName().equals("livesIn")) 
			b = false;
		
		return b;
	}
	
	
	private static boolean irrelIsPoliticianOf (Atom fact)
	{
		boolean b = true;
		if(
				/*!fact.getName().equals("imports") && 
				!fact.getName().equals("exports") &&
				!fact.getName().equals("dealsWith") &&
				!fact.getName().equals("isLocatedIn") &&*/
				//!fact.getName().equals("Influences") &&
				//!fact.getName().equals("isCitizenOf") &&
				!fact.getName().equals("isLeaderOf") &&
				!fact.getName().equals("diedIn") &&
				!fact.getName().equals("wasBornIn") &&
				!fact.getName().equals("isMarriedTo") &&
				!fact.getName().equals("hasChild") &&
				!fact.getName().equals("directed") &&
				!fact.getName().equals("created") &&
				!fact.getName().equals("isInterestedIn") &&
				!fact.getName().equals("produced") &&
				!fact.getName().equals("worksAt") &&
				!fact.getName().equals("graduatedFrom")) 
			b = false;
		
		return b;
	}
	
	
	private static boolean dwRule (Rule rule)
	{
		boolean b = false;
		String hStr = rule.getHead().getName();
		String rStr = rule.toString();
		String [] legalRules = new String [] {
				"dealsWith(?a,?b) :- dealsWith(?a,?f), dealsWith(?f,?b).",
				"dealsWith(?a,?b) :- dealsWith(?b,?a).",
				"dealsWith(?a,?b) :- imports(?a,?c), exports(?b,?c)."};
		
		if (hStr.equals("dealsWith") && Arrays.asList(legalRules).contains(rStr))
			b = true;
		
		return b;
	}
	
	
	private static boolean otherRelRuleDw (Rule rule)
	{
		boolean b = false;
		String [] legalRules = new String [] {"isMarriedTo(?a,?b) :- isMarriedTo(?b,?a).",
		"hasChild(?a,?b) :- isMarriedTo(?a,?f), hasChild(?f,?b).",
		"hasChild(?a,?b) :- isMarriedTo(?e,?a), hasChild(?e,?b).",
		"isPoliticianOf(?a,?b) :- diedIn(?a,?f), isLocatedIn(?f,?b).",
		"directed(?a,?b) :- created(?a,?b).",
		"influences(?a,?b) :- influences(?e,?a), influences(?e,?b).",
		"influences(?a,?b) :- influences(?a,?f), influences(?f,?b).",
		"isMarriedTo(?a,?b) :- hasChild(?a,?c), hasChild(?b,?c).",
		"isPoliticianOf(?a,?b) :- wasBornIn(?a,?f), isLocatedIn(?f,?b).",
		"isInterestedIn(?a,?b) :- isInterestedIn(?e,?b), influences(?e,?a).",
		"influences(?a,?b) :- influences(?a,?f), influences(?b,?f).",
		"isInterestedIn(?a,?b) :- influences(?a,?f), isInterestedIn(?f,?b).",
		"produced(?a,?b) :- directed(?a,?b).", 
		"produced(?a,?b) :- created(?a,?b).", 
		"worksAt(?a,?b) :- graduatedFrom(?a,?b).", 
		"isPoliticianOf(?a,?b) :- livesIn(?a,?f), isLocatedIn(?f,?b).", 
		"diedIn(?a,?b) :- wasBornIn(?a,?b).", 
		"isCitizenOf(?a,?b) :- wasBornIn(?a,?f), isLocatedIn(?f,?b)."};

		String rStr = rule.toString();
		if (Arrays.asList(legalRules).contains(rStr))
			b = true;
		
		return b;
	}
	
	
	private static boolean PoliticianRule (Rule rule)
	{
		boolean b = false;
		String [] legalRules = new String [] {
				"isPoliticianOf(?a,?b) :- diedIn(?a,?f), isLocatedIn(?f,?b).",
				"isPoliticianOf(?a,?b) :- wasBornIn(?a,?f), isLocatedIn(?f,?b).",
				"isPoliticianOf(?a,?b) :- livesIn(?a,?f), isLocatedIn(?f,?b).",
				"isPoliticianOf(?a,?b) :- livesIn(?a,?b).",
				"diedIn(?a,?b) :- wasBornIn(?a,?b)."};

		String rStr = rule.toString();
		if (Arrays.asList(legalRules).contains(rStr))
			b = true;
		
		return b;
	}
	
	
	private static boolean otherRelRulePolitician (Rule rule)
	{
		boolean b = false;
		String [] legalRules = new String [] {
		"directed(?a,?b) :- created(?a,?b).",
		
		/*"influences(?a,?b) :- influences(?e,?a), influences(?e,?b).",
		"influences(?a,?b) :- influences(?a,?f), influences(?f,?b).",
		"isInterestedIn(?a,?b) :- isInterestedIn(?e,?b), influences(?e,?a).",
		"influences(?a,?b) :- influences(?a,?f), influences(?b,?f).",
		"isInterestedIn(?a,?b) :- influences(?a,?f), isInterestedIn(?f,?b).",*/
		
		"produced(?a,?b) :- directed(?a,?b).", 
		"produced(?a,?b) :- created(?a,?b).", 
		"worksAt(?a,?b) :- graduatedFrom(?a,?b).",
		"hasAcademicAdvisor(?a,?b) :- worksAt(?a,?f), worksAt(?b,?f).",
		/*"isCitizenOf(?a,?b) :- wasBornIn(?a,?f), isLocatedIn(?f,?b).",
		"dealsWith(?a,?b) :- dealsWith(?a,?f), dealsWith(?f,?b).",
		"dealsWith(?a,?b) :- dealsWith(?b,?a).",
		"dealsWith(?a,?b) :- imports(?a,?c), exports(?b,?c).",*/
		"isMarriedTo(?a,?b) :- isMarriedTo(?b,?a).",
		"hasChild(?a,?b) :- isMarriedTo(?a,?f), hasChild(?f,?b).",
		"isMarriedTo(?a,?b) :- hasChild(?a,?c), hasChild(?b,?c)."};

		String rStr = rule.toString();
		if (Arrays.asList(legalRules).contains(rStr))
			b = true;
		
		return b;
	}
	
	
	private static boolean FortyBestRules (Rule rule)
	{
		boolean b = false;
		String [] legal = {
				"hasChild(?a,?b) :- isMarriedTo(?e,?a), hasChild(?e,?b).",
				"isMarriedTo(?a,?b) :- isMarriedTo(?b,?a).",
				"dealsWith(?a,?b) :- dealsWith(?a,?f), dealsWith(?f,?b).",
				"isMarriedTo(?a,?b) :- hasChild(?a,?c), hasChild(?b,?c).",
				"dealsWith(?a,?b) :- dealsWith(?b,?a).",
				"produced(?a,?b) :- directed(?a,?b).",
				"dealsWith(?a,?b) :- imports(?a,?c), exports(?b,?c).",
				"influences(?a,?b) :- influences(?a,?f), influences(?f,?b).",
				"influences(?a,?b) :- influences(?b,?a), influences(?b,?f).",
				"influences(?a,?b) :- influences(?e,?a), influences(?e,?b).",				
				"isCitizenOf(?a,?b) :- wasBornIn(?a,?f), isLocatedIn(?f,?b).",
				"diedIn(?a,?b) :- wasBornIn(?a,?b).",
				"dealsWith(?a,?b) :- exports(?a,?f), exports(?b,?f).",
				"dealsWith(?a,?b) :- imports(?a,?f), imports(?b,?f).",
				"directed(?a,?b) :- created(?a,?b).",
				"isPoliticianOf(?a,?b) :- diedIn(?a,?f), isLocatedIn(?f,?b).",
				"isPoliticianOf(?a,?b) :- livesIn(?a,?f), isLocatedIn(?f,?b).",
				"isInterestedIn(?a,?b) :- influences(?a,?f), isInterestedIn(?f,?b).",
				"worksAt(?a,?b) :- graduatedFrom(?a,?b).",
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
	
	
	private static boolean relFortyBest (String name)
	{
		String [] rels = {
				"isMarriedTo", 
				"hasChild",
				"isLocatedIn",
				"influences",
				"wasBornIn",
				"diedIn",
				"isCitizenOf",
				//"isLeaderOf",
				"isLocatedIn",
				//"hasCapitol",
				"directed",
				"created",
				"isInterestedIn",
				"produced",
				"worksAt",
				"graduatedFrom",
				"livesIn",
				"dealsWith",
				"imports",
				"exports"};
		
		if (Arrays.asList(rels).contains(name)) 
		{
			return true;
		}
		
		return false;
	}
}
