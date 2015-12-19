package htmlGeneration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class PaperHomepage 
{
	public static String paperHomepage(Statement statement, int pid) throws SQLException
	{
		String html = ""; 
		
		ResultSet results = statement.executeQuery("SELECT * FROM publication WHERE pid = " + pid); 
		if(results.next())
		{
			String title = results.getString("title"); 
			int cid = results.getInt("cid"); 
			int jid = results.getInt("jid"); 
			int year = results.getInt("year"); 
			String paperAbstract = results.getString("abstract"); 

			int citation_count = results.getInt("citation_count"); 
			int reference_count = results.getInt("reference_count"); 
			results.close(); 
			
			html += "<h3>" + title + "</h3>"; 

			if(cid > 0)
			{
				results = statement.executeQuery("SELECT name FROM conference WHERE cid = " + cid); 
				if(results.next())
				{
					html += "<p>Published in <a href=\"" + URLGen.getURL("conference", cid) + "\">" + results.getString("name") +  "</a>, " + year + "</p>"; 
				}
				results.close(); 
			}
			if(jid > 0)
			{
				results = statement.executeQuery("SELECT name FROM journal WHERE jid = " + jid); 
				if(results.next())
				{
					html += "<p>Published in <a href=\"" + URLGen.getURL("journal", jid) + "\">" + results.getString("name") +  "</a>, " + year + "</p>"; 
				}
				results.close(); 
			}
			
			if(cid < 1 && jid < 1 && year > 1930)
			{
				html += "<p>Published in " + year + "</p>"; 
			}
			
			if(paperAbstract != null && !paperAbstract.isEmpty())
			{
				html += "<p><b>Abstract: </b>" + paperAbstract + "</p>"; 				
			}
			
			results = statement.executeQuery("SELECT keyword.kid, keyword.keyword FROM keyword, publication_keyword"
				+ " WHERE publication_keyword.kid = keyword.kid AND publication_keyword.pid = " + pid); 
			for(int i = 0; results.next(); i++)
			{
				if(i == 0)
				{
					html += "<p><b>Keywords: </b>"; 
				}
				else
				{
					html += ", "; 
				}
				html += URLGen.addLinkage("keyword", results.getInt(1), results.getString(2)); 
			}
			html += "</p>"; 
			results.close();
			
			results = statement.executeQuery("SELECT author.aid FROM writes, author WHERE pid = " + pid + " AND writes.aid = author.aid ORDER BY rank ASC; "); 
			ArrayList<Integer> aids = new ArrayList<Integer>(); 
			while(results.next())
			{
				aids.add(results.getInt("author.aid")); 
			}
			results.close(); 
			
			if(aids.size() > 0)
			{
				html += "<hr><p><b>Authors: </b></p>"; 
			}
			for(int i = 0; i < aids.size(); i++)
			{
				html +=  "<p>" + (i+1) + ". " + AuthorHomepage.authorShort(statement, aids.get(i)) + "</p>"; 
			}
			
			if(citation_count > 0)
			{
				html += "<hr><p><b>Citations: </b>" + URLGen.viewListURL("(view all " + citation_count + ")", "target=citations", "page=1", "pid=" + pid) + "</p>"; 
				ArrayList<Integer> citations = new ArrayList<Integer>(); 				
				results = statement.executeQuery("SELECT citing FROM cite WHERE cited = " + pid); 
				while(results.next())
				{
					citations.add(results.getInt("citing")); 
				}
				results.close();
				
				for(int i = 0; i < citations.size() && i < 10; i++)
				{
					html += paperBrief(statement, citations.get(i), (i+1)); 
				}
			}

			if(reference_count > 0)
			{
				html += "<hr><p><b>References: </b>" + URLGen.viewListURL("(view all " + reference_count + ")", "target=references", "page=1", "pid=" + pid) + "</p>"; 
				ArrayList<Integer> references = new ArrayList<Integer>(); 				
				results = statement.executeQuery("SELECT cited FROM cite WHERE citing = " + pid); 
				while(results.next())
				{
					references.add(results.getInt("cited")); 
				}
				results.close();
				
				for(int i = 0; i < references.size() && i < 10; i++)
				{
					html += paperBrief(statement, references.get(i), (i+1)); 
				}
			}
			html += "</p>"; 
		}
		
		return html; 
	}
		
	public static String paperBrief(Statement statement, int pid, int order) throws SQLException
	{
		String html = ""; 
		
		ResultSet results = statement.executeQuery("SELECT * FROM publication WHERE pid = " + pid); 
		if(results.next())
		{
			String title = results.getString("title"); 
			int cid = results.getInt("cid"); 
			int jid = results.getInt("jid"); 
			int year = results.getInt("year"); 
			int citation_count = results.getInt("citation_count"); 
			results.close(); 
			
			String number = ""; 
			if(order > 0)
			{
				number = "[" + order + "] "; 
			}
			
			html += "<p>" + number + "<a href=\"" + URLGen.getURL("publication", pid) + "\">" + title + "</a>"; 
			results = statement.executeQuery("SELECT author.aid, name FROM writes, author WHERE pid = " + pid + " AND writes.aid = author.aid ORDER BY rank ASC; "); 
			
			while(results.next())
			{
				
				html += ", " + results.getString("name"); 
			}
			results.close(); 
			
			if(cid > 0)
			{
				results = statement.executeQuery("SELECT name FROM conference WHERE cid = " + cid); 
				if(results.next())
				{
					html += ", in <a href=\"" + URLGen.getURL("conference", cid) + "\">" + results.getString("name") + "</a>"; 
				}
				results.close(); 
			}
			if(jid > 0)
			{
				results = statement.executeQuery("SELECT name FROM journal WHERE jid = " + jid); 
				if(results.next())
				{
					html += ", in <a href=\"" + URLGen.getURL("journal", jid) + "\">" + results.getString("name") + "</a>"; 
				}
				results.close(); 
			}
			
			if(year > 1930)
			{
				html += ", " + year; 
			}
			
			if(citation_count > 0)
			{
				html += ". (cited by " + citation_count + " papers)"; 
			}

			html += "</p>"; 
		}
		else
		{
			html = "no such publication..."; 
		}

		return html; 
	}

}
