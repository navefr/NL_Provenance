package dataViewGeneration;

import java.util.ArrayList;

public class Tuple 
{
	public float weight = 1000; 
	public float nextWeight = 0; 
	public ArrayList<Tuple> neighbors = new ArrayList<Tuple>(); 
	
	public String toString()
	{
		String result = ""; 
		result += weight; 
		
		return result; 
	}
}
