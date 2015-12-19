package dataIntegration;

import java.util.ArrayList;

import functions.NaviFunctions;

public class ConfJournal 
{
	int id = 0; 
	String name; 
	String name_full; 
	String papers_title; 
	int [] minArray; 
	
	public ConfJournal(int cid, String name, String name_full)
	{
		this.id = cid; 
		this.name = NaviFunctions.stringNormalization(name); 
		this.name_full = NaviFunctions.stringNormalization(name_full); 
		papers_title = ""; 
	}
	
	public void buildMinArray(int Q, int arraySize, ArrayList<Integer> randomNumbers)
	{
		minArray = NaviFunctions.getMinArray(papers_title, Q, arraySize, randomNumbers); 
	}

	// compute the similarity between two conference; 
	public double similarity(ConfJournal anotherC)
	{
		double sim = 0; 
		double nameWeight = 0.05; 
		double paperWeight = 0.8/(minArray.length); 
		
		if(name_full.isEmpty() && anotherC.name_full.isEmpty())
		{
			nameWeight = 0.2; 
		}
		else if(name_full.isEmpty() || anotherC.name_full.isEmpty())
		{
			nameWeight = 0.1; 
		}
		
		sim += nameWeight * NaviFunctions.jaccardToken(name.toLowerCase(), anotherC.name.toLowerCase()); 
		sim += nameWeight * NaviFunctions.jaccardToken(name.toLowerCase(), anotherC.name_full.toLowerCase()); 
		sim += nameWeight * NaviFunctions.jaccardToken(name_full.toLowerCase(), anotherC.name.toLowerCase()); 
		sim += nameWeight * NaviFunctions.jaccardToken(name_full.toLowerCase(), anotherC.name_full.toLowerCase()); 
		
		if(papers_title.length() < 100 || anotherC.papers_title.length() < 100)
		{
			return 0; 
		}

		for(int i = 0; i < minArray.length; i++)
		{
			if(minArray[i] == anotherC.minArray[i])
			{
				sim += paperWeight; 
			}
		}
		
		return sim; 
	}
}