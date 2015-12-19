package htmlGeneration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class JournalHomepage 
{
	public static String journalHomepage(Statement statement, int jid) throws SQLException
	{
		String html = ""; 
		
		String basicInfo = "SELECT * FROM journal WHERE jid = " + jid; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			// basic information: journal name, journal homepage, paper_count, citation_count;  
			String journalName = result.getString("name"); 
			if(!result.getString("full_name").isEmpty())
			{
				journalName += " - " + result.getString("full_name"); 
			}
			String homepage = result.getString("homepage"); 
			
			int paperCount = result.getInt("paper_count"); 
			int citationCount = result.getInt("citation_count"); 
			result.close(); 

			html += "<h3><a href=\"" + URLGen.getURL("journal", jid) + "\">" + journalName + "</a></h3>"; 
			ArrayList<String> domains = new ArrayList<String>(); 
			ArrayList<Integer> dids = new ArrayList<Integer>(); 
			result = statement.executeQuery("SELECT domain.did, domain.name FROM domain_journal, domain WHERE domain_journal.did = domain.did AND jid = " + jid); 
			while(result.next())
			{
				dids.add(result.getInt("domain.did")); 
				domains.add(result.getString("domain.name")); 
			}

			html += "A journal"; 

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
			
			// top authors in this journals; 
			ArrayList<String> authors = TopGen.topNamesGen(statement, "journalAuthor", jid, 10); 
			for(int i = 0; i < authors.size(); i++)
			{
				if(i == 0)
				{
					html += "<hr><p><b>Top Authors in " + journalName + ": </b>" + URLGen.viewListURL("(view all)", "target=authors", "page=1", "jid=" + jid) + "</p>"; 
				}
				html += "<p>" + (i+1) + ". " + authors.get(i) + "</p>"; 
			}
			result.close(); 

			// top organizations in this journals; 
			ArrayList<String> organization = TopGen.topNamesGen(statement, "journalOrganization", jid, 10); 
			for(int i = 0; i < organization.size(); i++)
			{
				if(i == 0)
				{
					html += "<hr><p><b>Top Organizations in " + journalName + ": </b>" + URLGen.viewListURL("(view all)", "target=organizations", "page=1", "jid=" + jid) + "</p>"; 
				}
				html += "<p>" + (i+1) + ". " + organization.get(i) + "</p>"; 
			}
			result.close(); 
			
			// top keywords in this journals; 
			ArrayList<String> keywords = TopGen.topNamesGen(statement, "journalKeyword", jid, 10); 
			for(int i = 0; i < keywords.size(); i++)
			{
				if(i == 0)
				{
					html += "<hr><p><b>Hottest topics in " + journalName + ": </b>" + URLGen.viewListURL("(view all)", "target=keywords", "page=1", "jid=" + jid) + "</p>"; 
				}
				html += "<p>" + (i+1) + ". " + keywords.get(i) + "</p>"; 
			}
			result.close(); 
			
			// top publications in this journals; 
			ArrayList<Integer> pids = new ArrayList<Integer>(); 
			String queryPublications = TopGen.SQLGen("journalPublication", jid, 10); 
			result = statement.executeQuery(queryPublications); 
			while(result.next())
			{
				pids.add(result.getInt("id")); 
			}
			for(int i = 0; i < pids.size(); i++)
			{
				if(i == 0)
				{
					html += "<hr><p><b>Most Cited Papers in " + journalName + ": </b>" + URLGen.viewListURL("(view all)", "target=papers", "page=1", "jid=" + jid) + "</p>"; 
				}
				html += PaperHomepage.paperBrief(statement, pids.get(i), (i+1)); 
			}
			result.close(); 
		}
		else
		{
			html = "no such journal..."; 
		}
		
		return html; 
	}
	
	public static String journalShort(Statement statement, int jid) throws SQLException
	{
		String html = ""; 
		
		String basicInfo = "SELECT * FROM journal WHERE jid = " + jid; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			String journal = result.getString("name"); 
			if(!result.getString("full_name").isEmpty())
			{
				journal += " - " + result.getString("full_name"); 
			}
			html += URLGen.addLinkage("journal", jid, journal); 
		}

		return html; 
	}
}