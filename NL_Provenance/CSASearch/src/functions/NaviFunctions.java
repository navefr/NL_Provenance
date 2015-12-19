package functions;

import java.util.ArrayList;

public class NaviFunctions 
{
	// normalize a title
	public static int hashTitle(String title)
	{
		title = stringNormalization(title.toLowerCase()); 
		String b = ""; 
		for(int i = 0; i < title.length(); i++)
		{
			if((title.charAt(i) <= 'z' && title.charAt(i) >= 'a') || (title.charAt(i) >= '0' && title.charAt(i) <= '9'))
			{
				b += title.charAt(i); 
			}
		}
		
		return b.hashCode(); 
	}
	
	public static String normalizeTitle(String title)
	{
		title = stringNormalization(title.toLowerCase()); 
		String b = ""; 
		for(int i = 0; i < title.length(); i++)
		{
			if((title.charAt(i) <= 'z' && title.charAt(i) >= 'a') || (title.charAt(i) >= '0' && title.charAt(i) <= '9'))
			{
				b += title.charAt(i); 
			}
		}
		return b; 
	}
	
	public static String stringNormalization(String a)
	{
		a = a.toLowerCase().replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"").replaceAll("&#x27;", "'").replaceAll("&#x2F;", "/"); 
		
		String b = ""; 
		for(int i = 0; i < a.length(); i++)
		{
			if((a.charAt(i) <= 'z' && a.charAt(i) >= 'a') || (a.charAt(i) <= '9' && a.charAt(i) >= '0'))
			{
				b += a.charAt(i); 
			}
			else
			{
				b += " "; 
			}
		}
		while(b.contains("  "))
		{
			b = b.replaceAll("  ", " "); 
		}
		return b; 
	}
	
	// compute the jaccard coefficient between two strings; 
	public static double jaccardToken(String a, String b)
	{
		double sim = 0; 
		String [] as = a.toLowerCase().split(" "); 
		String [] bs = b.toLowerCase().split(" "); 
		
		int a_length = as.length; 
		int b_length = bs.length; 
		
		for(int i = 0; i < as.length; i++)
		{
			if(!as[i].equals("and") && !(as[i].length() == 1) && !as[i].equals("to") && !as[i].equals("of") && !as[i].equals("an") && !as[i].equals("on")
			&& !as[i].equals("the") && !(as[i].length() == 1) && !as[i].equals("as") && !as[i].equals("sup") && !as[i].equals("sub") && !as[i].equals("for"))
			{}
			else
			{
				a_length--; 
			}
		}
		for(int i = 0; i < bs.length; i++)
		{
			if(!bs[i].equals("and") && !(bs[i].length() == 1) && !bs[i].equals("to") && !bs[i].equals("of") && !bs[i].equals("an") && !bs[i].equals("on")
			&& !bs[i].equals("the") && !(bs[i].length() == 1) && !bs[i].equals("bs") && !bs[i].equals("sup") && !bs[i].equals("sub") && !bs[i].equals("for"))
			{}
			else
			{
				b_length--; 
			}
		}
		
		for(int i = 0; i < as.length; i++)
		{
			if(!as[i].equals("and") && !(as[i].length() == 1) && !as[i].equals("to") && !as[i].equals("of") && !as[i].equals("an") && !as[i].equals("on")
			&& !as[i].equals("the") && !(as[i].length() == 1) && !as[i].equals("as") && !as[i].equals("sup") && !as[i].equals("sub") && !as[i].equals("for"))
			{
				for(int j = 0; j < bs.length; j++)
				{
					if(as[i].equals(bs[j]))
					{
						sim += 1.0; 
						break; 
					}
				}
			}
		}
		
		return sim/((double)Math.max(a_length, b_length)); 
	}
	
	// get the min array of a String; 
	public static int [] getMinArray(String input, int Q, int arraySize, ArrayList<Integer> randomNumbers)
	{
		ArrayList<Integer> QGrams = getQGrams(input, Q); 
		
		int [] minArray = new int [arraySize]; 
		for(int i = 0; i < arraySize; i++)
		{
			int a = randomNumbers.get(3*i); 
			int b = randomNumbers.get(3*i) + 1; 
			int c = randomNumbers.get(3*i) + 2;  
			
			int min = 100000000; 
			for(int j = 0; j < QGrams.size(); j++)
			{
				int curMin = (a * QGrams.get(j) + b) % c; 
				if(curMin < min)
				{
					min = curMin; 
				}
			}
			minArray[i] = min; 
		}
		
		return minArray; 
	}
	
	// get the QGrams of a String; 
	public static ArrayList<Integer> getQGrams(String input, int Q)
	{
		input = input.toLowerCase(); 
		for(int i = 0; i < Q - 1; i++)
		{
			input = "* " + input + " *"; 
		}
		String [] grams = input.split(" "); 

		ArrayList<Integer> QGrams = new ArrayList<Integer>(); 
		for(int i = 0; i+Q-1 < grams.length; i++)
		{
			String gram = grams[i]; 
			for(int j = 1; j < Q; j++)
			{
				gram +=  " " + grams[i+j]; 
			}
			QGrams.add(gram.hashCode()); 
		}
		return QGrams; 
	}

	// get a list of random numbers; 
	public static ArrayList<Integer> getRandomNumber(int Size)
	{
		ArrayList<Integer> randomNumbers = new ArrayList<Integer>(); 
		while(randomNumbers.size() < Size)
		{
			int random = ((int)(Math.random() * 100000000)); 
			if(random > 10000000)
			{
				randomNumbers.add(random); 
			}
		}
		
		return randomNumbers; 
	}
}
