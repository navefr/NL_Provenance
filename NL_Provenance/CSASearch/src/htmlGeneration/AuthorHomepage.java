package htmlGeneration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class AuthorHomepage 
{
	public static String authorHomepage(Statement statement, int aid) throws SQLException
	{
		String html = ""; 

		String basicInfo = "SELECT * FROM author WHERE aid = " + aid; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			// 1. basic information: author name, homepage photo, #paper, #citations, organizations; 
			String authorName = result.getString("name"); 
			String homepage = result.getString("homepage"); 
			String photo = result.getString("photo"); 
			
			int oid = result.getInt("oid"); 
			int paperCount = result.getInt("paper_count"); 
			int citationCount = result.getInt("citation_count"); 
			result.close(); 

			String organizationName = ""; 
			result = statement.executeQuery("SELECT * FROM organization WHERE oid = " + oid); 
			if(result.next())
			{
				organizationName = result.getString("name"); 
			}
			result.close(); 
			
			if(photo != null && !photo.isEmpty())
			{
				html += "<img src=\"" + photo + "\" style=\"height:200px\">\n"; 
			}
			
			html += "<p>" + URLGen.addLinkage("author", aid, authorName); 

			if(organizationName != null && !organizationName.isEmpty())
			{
				html += ", from " + URLGen.addLinkage("organization", oid, organizationName);  
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
			
			// 2. research interest: 
			String researchInterests = TopGen.SQLGen("authorDomain", aid, 3); 
			result = statement.executeQuery(researchInterests); 
			if(result.next())
			{
				html += "<b>Main Research Interests: </b>" + URLGen.addLinkage("domain", result.getInt("id"), result.getString("name")); 
			}
			if(result.next())
			{
				html += ", " + URLGen.addLinkage("domain", result.getInt("id"), result.getString("name")); 
			}
			if(result.next())
			{
				html += " and " + URLGen.addLinkage("domain", result.getInt("id"), result.getString("name")); 
			}
			result.close();

			// 3. recent publications: 
			ArrayList<Integer> pids = new ArrayList<Integer>(); 
			result = statement.executeQuery("SELECT writes.pid FROM writes, publication WHERE aid = " + aid + " AND publication.pid = writes.pid"
				+ " ORDER BY year DESC, citation_count DESC; "); 
			
			while(result.next())
			{
				pids.add(result.getInt("pid")); 
			}
			result.close(); 
			
			if(pids.size() > 0)
			{
				html += "<hr><p><b>Recent Publications: </b>" + URLGen.viewListURL("(view all " + paperCount + ")", "target=papers", "page=1", "aid=" + aid) + "</p>"; 
			}
			
			for(int i = 0; i < pids.size() && i < 10; i++)
			{
				html += PaperHomepage.paperBrief(statement, pids.get(i), (i+1)) + "\n"; 
			}
			
			// 4. publication distributions on conferences/journals; 
			ArrayList<String> topConferences = TopGen.topNamesGen(statement, "authorConference", aid, 6); 
			ArrayList<String> topJournals = TopGen.topNamesGen(statement, "authorJournal", aid, 6); 
			
			if(topConferences.size() > 0 || topJournals.size() > 0)
			{
				html += "<hr><p><b>Publication Distributed on: </b></p>"; 
				if(topConferences.size() > 0)
				{
					html += "<p><b>Conferences: </b>"; 
					for(int i = 0; i < 5 && i < topConferences.size(); i++)
					{
						if(i > 0) { html += ", "; }
						html += topConferences.get(i); 
					}
					if(topConferences.size() > 5)
					{
						html += ", and " + URLGen.viewListURL("<b>others</b>", "target=conferences", "page=1", "aid=" + aid) + ".</p>"; 
					}
					html += "</p>"; 
				}
				if(topJournals.size() > 0)
				{
					html += "<p><b>Journals: </b>"; 
					for(int i = 0; i < 5 && i < topJournals.size(); i++)
					{
						if(i > 0) { html += ", "; }
						html += topJournals.get(i); 
					}
					if(topJournals.size() > 5)
					{
						html += ", and " + URLGen.viewListURL("<b>others</b>", "target=journals", "page=1", "aid=" + aid) + ".</p>"; 
					}
					html += "</p>"; 
				}				
			}
			
			// 5. coauthors; 
			ArrayList<String> topCoauthors = TopGen.topNamesGen(statement, "coauthor", aid, 6); 
			if(topCoauthors.size() > 0)
			{
				html += "<hr><p><b>Coauthors: </b><p>"; 
				for(int i = 0; i < 5 && i < topCoauthors.size(); i++)
				{
					if(i > 0) { html += ", "; }
					html += topCoauthors.get(i); 
				}
				if(topCoauthors.size() > 5)
				{
					html += ", and " + URLGen.viewListURL("<b>others</b>", "target=authors", "page=1", "aid=" + aid) + ".</p>"; 
				}
			}
			
			// 6. publication by keywords; 
			ArrayList<String> topKeywords = TopGen.topNamesGen(statement, "authorKeyword", aid, 6); 
			if(topKeywords.size() > 0)
			{
				html += "<hr><p><b>Publications on the Topics of: </b><p>"; 
				for(int i = 0; i < 5 && i < topKeywords.size(); i++)
				{
					if(i > 0) { html += ", "; }
					html += topKeywords.get(i); 
				}
				if(topKeywords.size() > 5)
				{
					html += ", and " + URLGen.viewListURL("<b>others</b>", "target=keywords", "page=1", "aid=" + aid) + ". </p>"; 
				}
			}			
		}
		else
		{
			html = "no such author..."; 
		}
		
		return html; 
	}
	
	public static String authorBrief(Statement statement, int aid) throws SQLException
	{
		String html = ""; 
		String basicInfo = "SELECT * FROM author WHERE aid = " + aid; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			// 1. basic information: author name, homepage photo, #paper, #citations, organizations; 
			String authorName = result.getString("name"); 
			String photo = result.getString("photo"); 
			
			int oid = result.getInt("oid"); 
			int paperCount = result.getInt("paper_count"); 
			int citationCount = result.getInt("citation_count"); 
			result.close(); 

			String organizationName = ""; 
			result = statement.executeQuery("SELECT * FROM organization WHERE oid = " + oid); 
			if(result.next())
			{
				organizationName = result.getString("name"); 
			}
			result.close(); 
			
			if(photo != null && !photo.isEmpty())
			{
				html += "<div><div style = \"float:left\"><img src=\"" + photo + "\" style=\"width:80px\"></div>"; 
			}
			
			html += "<div style=\"padding:1px 5px 5px 90px\"><p>" + URLGen.addLinkage("author", aid, authorName); 

			if(organizationName != null && !organizationName.isEmpty())
			{
				html += ", from " + URLGen.addLinkage("organization", oid, organizationName);  
			}
			if(paperCount != 0)
			{
				html += ", has " + paperCount + " papers"; 
			}
			if(citationCount != 0)
			{
				html += " with " + citationCount + " total citations. "; 
			}
			html += "</p>"; 

			// 2. research interest: 
			String researchInterests = TopGen.SQLGen("authorDomain", aid, 3); 
			result = statement.executeQuery(researchInterests); 
			if(result.next())
			{
				html += "Interests: " + URLGen.addLinkage("domain", result.getInt("id"), result.getString("name")); 
			}
			if(result.next())
			{
				html += ", " + URLGen.addLinkage("domain", result.getInt("id"), result.getString("name")); 
			}
			if(result.next())
			{
				html += " and " + URLGen.addLinkage("domain", result.getInt("id"), result.getString("name")); 
			}
			
			html += "</div></div><div style = \"clear:both\"></div>"; 
			result.close();
		}

		return html; 
	}

	public static String authorShort(Statement statement, int aid) throws SQLException
	{
		String html = ""; 
		String basicInfo = "SELECT * FROM author WHERE aid = " + aid; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			String authorName = result.getString("name"); 
			int oid = result.getInt("oid"); 
			result.close(); 

			String organizationName = ""; 
			result = statement.executeQuery("SELECT * FROM organization WHERE oid = " + oid); 
			if(result.next())
			{
				organizationName = result.getString("name"); 
			}
			result.close(); 
						
			html += "<a href=\"" + URLGen.getURL("author", aid) + "\">" + authorName + "</a>"; 

			if(organizationName != null && !organizationName.isEmpty())
			{
				html += " (" + organizationName + ")"; 
			}
		}

		return html; 
	}
}
