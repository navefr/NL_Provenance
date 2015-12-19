package htmlGeneration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DomainHomepage 
{
	public static String domainHomepage(Statement statement, int did) throws SQLException
	{
		String html = ""; 

		String basicInfo = "SELECT * FROM domain WHERE did = " + did; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			// 1. domainName; 
			String domainName = result.getString("name"); 
			html += "<h3>" + URLGen.addLinkage("domain", did, domainName) + "</h3>"; 
			
			// 2. top conferences and journals: 
			String topConferences = TopGen.SQLGen("domainConference", did, 10000); 
			ArrayList<Integer> cids = new ArrayList<Integer>(); 
			ArrayList<String> conferences = new ArrayList<String>(); 
			result = statement.executeQuery(topConferences); 
			while(result.next())
			{
				cids.add(result.getInt("id")); 
				String conferenceName = result.getString("name"); 
				if(!result.getString("full").isEmpty())
				{
					conferenceName += " - " + result.getString("full"); 
				}
				conferences.add(conferenceName); 
			}
			result.close();

			if(conferences.size() > 0)
			{
				html += "<hr><p><b>Top Conferences in " + domainName + " Area: </b>" + URLGen.viewListURL("(view all " + conferences.size() + ")", "target=conferences", "page=1", "did=" + did) + "</p>"; 
			}
			for(int i = 0; i < 5 && i < conferences.size(); i++)
			{
				html += "<p>" + (i+1) + ". " + URLGen.addLinkage("conference", cids.get(i), conferences.get(i)) + "</p>"; 
			}
			
			String topJournals = TopGen.SQLGen("domainJournal", did, 10000); 
			ArrayList<Integer> jids = new ArrayList<Integer>(); 
			ArrayList<String> journals = new ArrayList<String>(); 
			result = statement.executeQuery(topJournals); 
			while(result.next())
			{
				jids.add(result.getInt("id")); 
				String journalName = result.getString("name"); 
				if(!result.getString("full").isEmpty())
				{
					journalName += " - " + result.getString("full"); 
				}
				journals.add(journalName); 
			}
			result.close();
			if(journals.size() > 0)
			{
				html += "<hr><p><b>Top Journals in " + domainName + " Area: </b>" + URLGen.viewListURL("(view all " + journals.size() + ")", "target=journals", "page=1", "did=" + did) + "</p>"; 
			}
			for(int i = 0; i < 5 && i < journals.size(); i++)
			{
				html += "<p>" + (i+1) + ". " + URLGen.addLinkage("journal", jids.get(i), journals.get(i)) + "</p>"; 
			}

			// 3. top authors: 
			ArrayList<Integer> aids = new ArrayList<Integer>(); 
			result = statement.executeQuery("SELECT author.aid, citation_count FROM author, domain_author WHERE author.aid = domain_author.aid AND rank = 1 AND did = " + did
				+ " ORDER BY citation_count DESC LIMIT 0, 12; "); 
			while(result.next())
			{
				aids.add(result.getInt("aid")); 
			}
			result.close(); 
			
			if(aids.size() > 0)
			{
				html += "<hr><p><b>Top Authors in the " + domainName + " Area: </b>" + URLGen.viewListURL("(view all)", "target=authors", "page=1", "did=" + did) + "</p>"; 
			}
			for(int i = 0; i < aids.size() && i < 12; i++)
			{
				if(i > 0)
				{
					html += "<hr>"; 
				}
				html += "<p>" + AuthorHomepage.authorBrief(statement, aids.get(i)) + "</p>"; 
			}
		}
		else
		{
			html = "no such author..."; 
		}
		
		return html; 
	}
	
	public static String domainShort(Statement statement, int did) throws SQLException
	{
		String html = ""; 
		
		String basicInfo = "SELECT * FROM domain WHERE did = " + did; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			String domain = result.getString("name"); 
			html += URLGen.addLinkage("domain", did, domain); 
		}

		return html; 
	}
}
