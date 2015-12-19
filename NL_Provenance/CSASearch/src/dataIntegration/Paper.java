package dataIntegration;

import functions.NaviFunctions;

public class Paper implements Comparable<Paper>
{
	int pid; 
	String title; 
	int year; 

	public Paper(int pid, String title, int year)
	{
		this.pid = pid; 
		this.title = title; 
		this.year = year; 
	}
	
	public double similarity(Paper paper_b)
	{
		if(year != paper_b.year)
		{
			return 0; 
		}
		else
		{
			 return(NaviFunctions.jaccardToken(title, paper_b.title)); 
		}
	}

	public int compareTo(Paper o) 
	{
		return(this.title.compareTo(o.title)); 
	}
}
