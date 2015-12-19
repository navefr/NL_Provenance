package Parsing;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.Random;

public class MakeIrisFileExplain 
{
	public static void main (String [] args)
	{
		for (int i = 11; i <= 12; i++) 
		{
			writeIrisProg(i * 100, i, true);
		}
	}
	
	
	
	private static void writeIrisProg (int factsLim, int progNum, boolean rec)
	{
		Writer writer = null;

		try 
		{
			String r = (rec) ? "rec_" : "";
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("explain_prog_" + r + progNum + ".iris"), "utf-8"));
		    for (int i = 0; i < factsLim; i++) 
		    {
		    	if (true == rec) 
			    {
		    		writer.write(genRandExplainRecFact("basic_part") + "\n");
		    		if (0 == i%2) 
		    		{
		    			writer.write(genRandExplainRecFact("assembly") + "\n");
			    		//writer.write(genRandExplainRecFact("b_o_m") + "\n");
					}
			    }
		    	
		    	else
			    {
		    		writer.write(genRandExplainFact("close") + "\n");
			    	writer.write(genRandExplainFact("movavg") + "\n");
			    }
			}
		    
		    if (true == rec) 
		    {
		    	int factor = 100000000;
		    	writer.write("b_o_m(?Part, ?C) :- subpart_cost(?Part, ?SubPart, ?C).\n");
		    	writer.write("subpart_cost(?Part, ?Part, ?Cost) :- basic_part(?Part, ?Cost).\n"); 
		    	writer.write("subpart_cost(?Part, ?Subpart, ?Cost) :- assembly(?Part, ?Subpart, ?Quantity), b_o_m(?Subpart, ?TotalSubcost), ?Quantity  * ?TotalSubcost = ?Cost, ?Cost < " + factor + "."); 
			}
		    
		    else
		    {
		    	writer.write("volatile(?stock) :- close(?x, ?stock, ?a), close(?y, ?stock, ?b), ?y > ?x, ?y - ?x  = ?k, ?k <= 7, ?a / ?b = ?c, ?c > 2.\n"); 
		    	writer.write("should_sell(?stock) :- movavg(?x, ?stock, ?a), movavg(?y, ?stock, ?b), ?y > ?x, ?y - ?x  = ?k, ?k <= 7, ?a / ?b = ?c, ?c > 1.1.");
		    }
		   
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
	
	
	
	private static String genRandExplainFact (String type)
	{
		Random rand = new Random();
		int idx = rand.nextInt(11);
		String day = Integer.toString(rand.nextInt((7 - 1) + 1) + 1);
		String price = Integer.toString(rand.nextInt((1000 - 300) + 1) + 300);
		String [] stocks = new String [] {"Wendys", "Mcdonalds", "InNOut", "Starbucks", "BurgerKing", "TacoBell", "Chipotles", "PizzaHut", "KFC", "Subway", "Dominos"};
		
		return type + "(" + day + ", '" + stocks[idx] + "', " + price + ").";
	}
	
	
	private static String genRandExplainRecFact (String type)
	{
		String retVal = "";
		Random rand = new Random();
		int i = rand.nextInt(11);
		int j = rand.nextInt(11);
		String [] parts = new String [] {"Hinge", "Battery", "Distributor", "Starter", "Engine", "Engine block", "Gearbox", "Master cylinder", "Fuel pump", "Alternator", "Fuel gauge"};
		String price = Integer.toString(rand.nextInt((1500 - 100) + 1) + 100);
		switch (type)
		{
		case "basic_part":
			retVal = type + "('" + parts[i] + "', " + price + ").";
			break;
		case "assembly":
			retVal = type + "('" + parts[i] + "', '" + parts[j] + "', " + price + ").";
			break;
		case "b_o_m":
			retVal = type +  "('" + parts[i] + "', " + price + ").";
			break;
		}
		
		return retVal;
	}
}
