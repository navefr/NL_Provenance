package functions;

import java.util.ArrayList;

public class SimilarityFunctions 
{
	public static void main(String [] args)
	{
		String s1 = "Ricardo Graa Abalo"; 
		String s2 = "Ricardo Grau Abalo"; 
		
		System.out.println(stringEditDistance(s1, s2));
	}
	
	public static double jaccardQGram(String string1, String string2, int Q)
	{
		string1 = string1.toLowerCase(); 
		string2 = string2.toLowerCase(); 
		
		double similarity = -1;
		
		ArrayList<String> QsetA = new ArrayList<String>();
		ArrayList<String> QsetB = new ArrayList<String>(); 
		
		for(int i = 0; i < string1.length()-Q+1; i++)
		{
			QsetA.add(string1.substring(i, i+Q));
		}
		
		for(int i = 0; i < string2.length()-Q+1; i++)
		{
			QsetB.add(string2.substring(i, i+Q));
		}
		
		QsetA.retainAll(QsetB);
		int Intersection = QsetA.size();
		double size = ((double)(string1.length() + string2.length()))/2; 
		
		similarity = (double) Intersection/size;
			
		return similarity;
	}
	
	public static double editDistanceSim(String s1, String s2)
	{
		s1 = s1.toLowerCase(); 
		s2 = s2.toLowerCase(); 
		
		int distance = stringEditDistance(s1, s2); 
		
		double sim = 1 - ((double) distance) / (s1.length() + s2.length()); 
		
		return sim; 
	}
	
	public static int stringEditDistance(String string1, String string2)
	{
		int [][] matrix = new int [string1.length()][string2.length()]; 
		matrix[0][0] = 0; 
		for(int i = 0; i < string1.length(); i++)
		{
			matrix[i][0] = i; 
		}
		for(int j = 0; j < string2.length(); j++)
		{
			matrix[0][j] = j; 
		}
		
		for(int i = 1; i < string1.length(); i++)
		{
			for(int j = 1; j < string2.length(); j++)
			{
				if(matrix[i-1][j] < matrix[i][j-1])
				{
					matrix[i][j] = matrix[i-1][j] + 1; 
				}
				else
				{
					matrix[i][j] = matrix[i][j-1] + 1; 					
				}
				
				if(string1.charAt(i) == string2.charAt(j) && matrix[i-1][j-1] < matrix[i][j])
				{
					matrix[i][j] = matrix[i-1][j-1]; 
				}
			}
		}

		return matrix[string1.length()-1][string2.length()-1]; 
	}	
}
