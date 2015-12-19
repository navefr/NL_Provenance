package Parsing;

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
import java.util.stream.IntStream;

import Basics.Atom;
import Basics.MemDB;
import Basics.Proton;
import Basics.Rule;

public class MakeIrisFile 
{
	public static void main (String [] args)
	{
		/*for (int i = 1; i < 6; i++) 
		{
			writeIrisProg(i*2000, i+15);
		}
		
		for (int i = 1; i < 6; i++) 
		{
			writeIrisProg(i*20000, i+20);
		}
		
		for (int i = 1; i < 6; i++) 
		{
			writeIrisProg(i*200000, i+25);
		}*/
		
		/*for (int i = 1; i < 6; i++) 
		{
			writeIrisProg(i*200000, i+30, i);
		}*/
		
		/*for (int i = 1; i <= 5; i++) 
		{
			writeIrisProg(1000000, i, 0);
		}*/
		
		/*for (int i = 100; i <= 2600; i+=100) 
		{*/
			writeIrisProg(1000000, 1, 0);
		//}
	}
	
	
	private static void writeIrisProg (int factsLim, int progNum, int dupNum)
	{
		Random rand = new Random();
		Writer writer = null;

		try 
		{
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("seminaive_dw_" + progNum + ".iris"), "utf-8"));
		    ParseDB rfDB = new ParseDB ("yago2core_facts.clean.notypes.tsv", factsLim);
		    ParseDbRules rules = new ParseDbRules ("amie_yago2_rb_confidence.tsv", 140);
		    
		   //List<Atom> connected = new ArrayList<Atom>();
		   List<Integer> from = new ArrayList<Integer>();
		   List<Integer> to = new ArrayList<Integer>();
		  
		   /*int [] instArrRel = {209291, 55426, 14480, 21965, 1454};//, 100000};
		   int [] instArrOther = {6265, 11755, 11293, 31293, 182735, 19020, 2416, 11630};//, 160000};
		   	int dbSize = 300000;*/
		   	//double divisorOfRel = ( IntStream.of(instArrRel).sum() / (dbSize * (0.1*progNum) ));
		   	//System.out.println("divisorOnRel: " + divisorOfRel);
		   	//double divisorOfOther = (10 == progNum) ? 0 : ( IntStream.of(instArrOther).sum() / (dbSize * (0.1*(10 - progNum)) ));
		   	//System.out.println("divisorOnOther: " + divisorOfOther);
		   for (HashSet<Atom> set : MemDB.getInstance().getFacts().values()) 
		    {
		    	int countOthers = 0;
		    	int countRel = 0;
		    	int relativeSizeRel = (int) ( ( set.size() ) / ( 1 ) );
		    	//int relativeSizeOther = (int) ( (divisorOfOther == 0) ? (set.size() * 0.05) : ( set.size() ) / ( divisorOfOther ) ); 
		    	for (Atom fact : set) 
				{
					if (legalFact(fact))
					{
						/*if (relativeSizeRel > 0) 
						{*/
							if (true == fullCircleFact(fact))// && countRel < relativeSizeRel) 
							{
								/*for (int i = 0; i < progNum; i++) 
								{*/
									writer.write(fact.toString() + "\n");
								//}
								//writer.write(fact.toString() + "\n");
								/*countRel ++;
								
								if (rand.nextDouble() > 0.5 && fact.getName().equals("exports")) 
								{
									connected.add(fact);
								}*/
							}
							
							/*if (irrelIsPoliticianOf(fact))
							{
								writer.write(fact.toString() + "\n");
							}*/
						//}
						
						/*else
						{
							if (true == relDealsWith(fact) && limRe < set.size()) 
							{
								for (int i = 0; i < dupNum; i++) 
								{
									writer.write(fact.toString(i) + "\n");
								}
								
								
								
								limRe ++;
							}
						}*/
						
						/*if (true == irrelIsPoliticianOf(fact) && countOthers < relativeSizeOther)
						{
							writer.write(fact.toString() + "\n");
							countOthers ++;
						}*/
					}
				}
			}
		    
		    /*for (Atom atom : connected) 
		    {
				for (int i = 1; i < progNum; i++) 
				{
					writer.write(atom.toString(0, i) + "\n");
				}
			}*/
		    
		   // Collections.sort(rules.getProgram().getRules());
		    
		    for (Rule rule : rules.getProgram().getRules()) 
		    {
		    	if (fullCircleRule(rule))
		    	{
		    		/*for (int i = 0; i < progNum; i++) 
		    		{*/
		    			writer.write(rule.toString() + "\n");
					//}
				}
			}
		   /* for (int i = 0; i < progNum; i++) 
    		{*/
		   
		   
		   //GRAPH MECHANISEM
		  /* for (int i = 1; i <= progNum; i++) 
		   {
			   writer.write("E('" + (i-1) + "','" + i + "')." + "\n");
			   if (rand.nextDouble() > 0.8) 
			   {
				   from.add(i);
			   }
			   
			   if (rand.nextDouble() > 0.8) 
			   {
				   to.add(i);
			   }
		   }
		   
		   int size = Math.min(to.size(), from.size());
		   for (int j = 0; j < size; j++)
		   {
			   writer.write("E('" + from.get(j) + "','" + to.get(j) + "')." + "\n");
		   }
		   
		   String [] rls = TcRule(0);
		   for (String r : rls) 
		   {
			   writer.write(r + "\n");
		   }*/
    			
			//}
		   
		   
		} 
		catch (IOException ex) 
		{
		  // report
			System.out.println("ERROR");
		} 
		finally 
		{
		   try {writer.close();} catch (Exception ex) {}
		}
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
				//!fact.getName().equals("imports") &&
				!fact.getName().equals("exports"))
			b = false;	
		
		return b;
	}
	
	
	private static boolean relIsPoliticianOf (Atom fact)
	{
		boolean b = true;
		if(!fact.getName().equals("diedIn") && !fact.getName().equals("hasCapital"))
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
				!fact.getName().equals("imports") && 
				!fact.getName().equals("exports") &&
				!fact.getName().equals("dealsWith") &&
				!fact.getName().equals("isLocatedIn") &&
				!fact.getName().equals("livesIn") &&
				!fact.getName().equals("isLocatedIn") &&
				!fact.getName().equals("directed") &&
				!fact.getName().equals("hasCurrency") &&
				!fact.getName().equals("wasBornIn") &&
				!fact.getName().equals("isMarriedTo") &&
				!fact.getName().equals("hasChild") &&
				!fact.getName().equals("directed") &&
				!fact.getName().equals("created") &&
				!fact.getName().equals("actedIn") &&
				!fact.getName().equals("produced") &&
				!fact.getName().equals("worksAt") &&
				!fact.getName().equals("graduatedFrom") &&
				!fact.getName().equals("participatedIn")) 
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
	
	
	private static String [] TcRule (int i)
	{
		String [] legalRules = new String [] {
				"TC(?a,?b) :- TC(?a,?f), E(?f,?b).",
				"TC(?a,?b) :- E(?a,?b).",
				"TC(?a,?b) :- TC(?b,?a)."};
		
		return legalRules;
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
	
	
	private static boolean fullCircleFact (Atom fact)
	{
		boolean b = false;
		String [] legal = {
				
				"dealsWith",
				"exports",
				"imports",
				
				/*
				"participatedIn",
				"hasOfficialLanguage",
				*/
				//"influences",
				/*
				"directed",
				"created",
				"isLocatedIn",
				"isPoliticianOf",
				"wasBornIn",
				"diedIn",
				*/
				/*
				"hasChild",
				"isMarriedTo"
				*/
				};
		if (Arrays.asList(legal).contains(fact.getName())) 
		{
			b = true;
		}
		
		return b;
	}
	
	
	
	private static boolean fullCircleRule (Rule rule)
	{
		boolean b = false;
		String [] legal = {
			/*"isMarriedTo(?a,?b) :- isMarriedTo(?b,?a).",
			"hasChild(?a,?b) :- isMarriedTo(?a,?f), hasChild(?f,?b).",
			"hasChild(?a,?b) :- isMarriedTo(?e,?a), hasChild(?e,?b).",
			"isMarriedTo(?a,?b) :- hasChild(?a,?c), hasChild(?b,?c).",
			*/
			
			"imports(?a,?b) :- dealsWith(?a,?c), imports(?c,?b).",
			"dealsWith(?a,?b) :- imports(?a,?f), imports(?b,?f).",
			/*"imports(?a,?b) :- dealsWith(?a,?c), exports(?c,?b).",
			"imports(?a,?b) :- dealsWith(?c,?a), imports(?c,?b).",
			*/
			/*
			"exports(?a,?b) :- dealsWith(?a,?c), exports(?c,?b).",
			"dealsWith(?a,?b) :- exports(?a,?f), exports(?b,?f).",
			"exports(?a,?b) :- dealsWith(?e,?a), exports(?e,?b).",
			"imports(?a,?b) :- exports(?a,?b).",
			"dealsWith(?a,?b) :- imports(?a,?c), exports(?b,?c).",
			*/
			"dealsWith(?a,?b) :- dealsWith(?a,?f), dealsWith(?f,?b).",
			"dealsWith(?a,?b) :- dealsWith(?b,?a).",
				
				
			//"dealsWith(?a,?b) :- participatedIn(?a,?f), participatedIn(?b,?f).",
			/*"dealsWith(?a,?b) :- hasOfficialLanguage(?a,?f), hasOfficialLanguage(?b,?f).",
			*/	
			
			/*
			"influences(?a,?b) :- influences(?a,?f), influences(?f,?b).",
			"influences(?a,?b) :- influences(?a,?f), influences(?b,?f).",
			"isInterestedIn(?a,?b) :- influences(?b,?a).",
			*/
			/*
			"isPoliticianOf(?a,?b) :- wasBornIn(?a,?f), isLocatedIn(?f,?b).",
			"isPoliticianOf(?a,?b) :- diedIn(?a,?f), isLocatedIn(?f,?b).",
			"directed(?a,?b) :- created(?a,?b)."
			*/};
		
		if (Arrays.asList(legal).contains(rule.toString())) 
		{
			b = true;
		}
		
		return b;
	}
	
	
	
}
