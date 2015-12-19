package htmlGeneration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class KeywordHomepage 
{
	public static String keywordHomepage(Statement statement, int kid) throws SQLException
	{
		String html = ""; 
		
		String basicInfo = "SELECT * FROM keyword WHERE kid = " + kid; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			// basic information: keyword, short name;  
			String name = result.getString("keyword");
			String name_short = result.getString("keyword_short"); 			
			result.close(); 

			html += "<h3><a href=\"" + URLGen.getURL("keyword", kid) + "\">" + name + "</a></h3>"; 
			ArrayList<String> domains = new ArrayList<String>(); 
			ArrayList<Integer> dids = new ArrayList<Integer>(); 
			result = statement.executeQuery("SELECT domain.did, domain.name FROM domain_keyword, domain WHERE domain_keyword.did = domain.did AND kid = " + kid); 
			while(result.next())
			{
				dids.add(result.getInt("domain.did")); 
				domains.add(result.getString("domain.name")); 
			}

			if(!name_short.isEmpty())
			{
				html += "Often short for " + name_short + ", ";  
			}
			
			if(domains.size() > 0)
			{
				html += "is a keyword in the area of " + URLGen.addLinkage("domain", dids.get(0), domains.get(0)); 
				for(int i = 1; i < domains.size(); i++)
				{
					html += ", " + URLGen.addLinkage("domain", dids.get(i), domains.get(i)); 
				}
			}
			html += "</p>\n"; 
			
			// top authors in this keyword; 
			ArrayList<String> authors = TopGen.topNamesGen(statement, "keywordAuthor", kid, 10); 
			for(int i = 0; i < authors.size(); i++)
			{
				if(i == 0)
				{
					html += "<hr><p><b>Top Authors related to the topic of " + name + ": </b>" + URLGen.viewListURL("(view all)", "target=authors", "page=1", "kid=" + kid) + "</p>"; 
				}
				html += "<p>" + (i+1) + ". " + authors.get(i) + "</p>"; 
			}
			result.close(); 

			// top publications in this conferences; 
			ArrayList<Integer> pids = new ArrayList<Integer>(); 
			String queryPublications = TopGen.SQLGen("keywordPublication", kid, 10); 
			
			result = statement.executeQuery(queryPublications); 
			while(result.next())
			{
				pids.add(result.getInt("id")); 
			}
			for(int i = 0; i < pids.size(); i++)
			{
				if(i == 0)
				{
					html += "<hr><p><b>Most Cited Papers on the topic of " + name + ": </b>" + URLGen.viewListURL("(view all)", "target=papers", "page=1", "kid=" + kid) + "</p>"; 
				}
				html += PaperHomepage.paperBrief(statement, pids.get(i), (i+1)); 
			}
			result.close(); 
		}
		else
		{
			html = "no such conference..."; 
		}

		return html; 
	}
	
	public static String keywordShort(Statement statement, int kid) throws SQLException
	{
		String html = ""; 
		
		String basicInfo = "SELECT * FROM keyword WHERE kid = " + kid; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			String keyword = result.getString("keyword"); 
			html += URLGen.addLinkage("keyword", kid, keyword); 
		}

		return html; 
	}
}
