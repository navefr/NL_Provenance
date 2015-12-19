package Parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Basics.*;

public class ParseDB 
{

	
	/*************************************************************************************************************/
	/** Title: ParseDB																				
	/** Description: 			
	/*************************************************************************************************************/
	
	public ParseDB (String path, int rowNumLimit)
	{
		try 
		{
			System.out.println("Loading DB...");
			this.ReadFileToDB(path, rowNumLimit);
			System.out.println("Finished loading DB");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: ReadFileToDB																				
	/** Description: Takes a path of tsv. file and inserts it to mysql DB 			
	/*************************************************************************************************************/
	
	public void ReadFileToDB (String path, int rowNumLimit) throws Exception
	{
		BufferedReader bReader = new BufferedReader(new FileReader(path));
		String line;
		String regular = "<(.+)>\\s+<(.+)>\\s+<(.+)>";//"<(.+)>\\s+<(imports|exports|dealsWith)>\\s+<(.+)>";
		Pattern pattern = Pattern.compile(regular);
		Matcher m;
		int rowNum = 0;
		while ((line = bReader.readLine()) != null && rowNum < rowNumLimit) 
		{
			m = pattern.matcher(line);
			if (m.find()) 
			{
				rowNum++;
				/*DB.getInstance().CreateTableIfNotExist(m.group(2), "Country", "Product");
				DB.getInstance().InsertToTable(m.group(2), new ArrayList<String>(
					    Arrays.asList(m.group(1), m.group(3))));*/
				MemDB.getInstance().InsertToTable(m.group(2), new ArrayList<String>(
					    Arrays.asList(m.group(1), m.group(3))));
			}
		}
		
		bReader.close();
	}
}
