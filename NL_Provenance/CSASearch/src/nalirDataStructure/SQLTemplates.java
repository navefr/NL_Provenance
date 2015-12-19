package nalirDataStructure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class SQLTemplates 
{
	public ArrayList<SQLTemplate> templates; 
	
	@SuppressWarnings("resource")
	public SQLTemplates() throws IOException
	{
		templates = new ArrayList<SQLTemplate>(); 
		File file = new File("/Users/lifei/Dropbox/workspace/csasearch/src/zfiles/templates"); 
		Scanner scan = new Scanner(file); 
		for(int count = 1; scan.hasNextLine(); count++)
		{
			scan.nextLine(); 
			String nlq = scan.nextLine(); 
			String sql = scan.nextLine(); 
			
			SQLTemplate template = new SQLTemplate(count, sql, nlq); 
			templates.add(template); 
		}
	}
}
