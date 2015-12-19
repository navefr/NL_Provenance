package htmlGeneration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ConferenceHomepage 
{
	public static String conferenceHomepage(Statement statement, int cid) throws SQLException
	{
		String html = ""; 
		
		String basicInfo = "SELECT * FROM conference WHERE cid = " + cid; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			// basic information: conference name, conference homepage, paper_count, citation_count;  
			String conferenceName = result.getString("name"); 
			if(!result.getString("full_name").isEmpty())
			{
				conferenceName += " - " + result.getString("full_name"); 
			}

			String homepage = result.getString("homepage"); 
			
			int paperCount = result.getInt("paper_count"); 
			int citationCount = result.getInt("citation_count"); 
			result.close(); 

			html += "<h3><a href=\"" + URLGen.getURL("conference", cid) + "\">" + conferenceName + "</a></h3>"; 
			ArrayList<String> domains = new ArrayList<String>(); 
			ArrayList<Integer> dids = new ArrayList<Integer>(); 
			result = statement.executeQuery("SELECT domain.did, domain.name FROM domain_conference, domain WHERE domain_conference.did = domain.did AND cid = " + cid); 
			while(result.next())
			{
				dids.add(result.getInt("domain.did")); 
				domains.add(result.getString("domain.name")); 
			}

			html += "A conference"; 
			
			if(domains.size() > 0)
			{
				html += " in the area of " + URLGen.addLinkage("domain", dids.get(0), domains.get(0)); 
				for(int i = 1; i < domains.size(); i++)
				{
					html += ", " + URLGen.addLinkage("domain", dids.get(i), domains.get(i)); 
				}
			}
			if(paperCount != 0)
			{
				html += ", has " + paperCount + " papers"; 
			}
			if(citationCount != 0)
			{
				html += " with " + citationCount + " total citations. "; 
			}
			html += "</p>\n"; 
			if(homepage != null && !homepage.isEmpty())
			{
				html += "<p><a href=\"" + homepage + "\">Official Hompage</a></p>\n"; 
			}
			
			// top authors in this conferences; 
			ArrayList<String> authors = TopGen.topNamesGen(statement, "conferenceAuthor", cid, 10); 
			for(int i = 0; i < authors.size(); i++)
			{
				if(i == 0)
				{
					html += "<hr><p><b>Top Authors in " + conferenceName + ": </b>" + URLGen.viewListURL("(view all)", "target=authors", "page=1", "cid=" + cid) + "</p>"; 
				}
				html += "<p>" + (i+1) + ". " + authors.get(i) + "</p>"; 
			}
			result.close(); 

			// top organizations in this conferences; 
			ArrayList<String> organization = TopGen.topNamesGen(statement, "conferenceOrganization", cid, 10); 
			for(int i = 0; i < organization.size(); i++)
			{
				if(i == 0)
				{
					html += "<hr><p><b>Top Organizations in " + conferenceName + ": </b>" + URLGen.viewListURL("(view all)", "target=organizations", "page=1", "cid=" + cid) + "</p>"; 
				}
				html += "<p>" + (i+1) + ". " + organization.get(i) + "</p>"; 
			}
			result.close(); 
			
			// top keywords in this conferences; 
			ArrayList<String> keywords = TopGen.topNamesGen(statement, "conferenceKeyword", cid, 10); 
			for(int i = 0; i < keywords.size(); i++)
			{
				if(i == 0)
				{
					html += "<hr><p><b>Hottest topics in " + conferenceName + ": </b>" + URLGen.viewListURL("(view all)", "target=keywords", "page=1", "cid=" + cid) + "</p>"; 
				}
				html += "<p>" + (i+1) + ". " + keywords.get(i) + "</p>"; 
			}
			result.close(); 
			
			// top publications in this conferences; 
			ArrayList<Integer> pids = new ArrayList<Integer>(); 
			String queryPublications = TopGen.SQLGen("conferencePublication", cid, 10); 
			result = statement.executeQuery(queryPublications); 
			while(result.next())
			{
				pids.add(result.getInt("id")); 
			}
			for(int i = 0; i < pids.size(); i++)
			{
				if(i == 0)
				{
					html += "<hr><p><b>Most Cited Papers in " + conferenceName + ": </b>" + URLGen.viewListURL("(view all)", "target=papers", "page=1", "cid=" + cid) + "</p>"; 
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
	
	public static String conferenceShort(Statement statement, int cid) throws SQLException
	{
		String html = ""; 
		
		String basicInfo = "SELECT * FROM conference WHERE cid = " + cid; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			String conference = result.getString("name"); 
			if(!result.getString("full_name").isEmpty())
			{
				conference += " - " + result.getString("full_name"); 
			}
			html += URLGen.addLinkage("conference", cid, conference); 
		}

		return html; 
	}
}
