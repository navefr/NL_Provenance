package htmlGeneration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class OrganizationHomepage 
{
	public static String organizationHomepage(Statement statement, int oid) throws SQLException
	{
		String html = ""; 
		
		String basicInfo = "SELECT * FROM organization WHERE oid = " + oid; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			// 1. basic information: organization name, homepage; 
			String organizationName = result.getString("name"); 
			String homepage = result.getString("homepage"); 
			int author_count = result.getInt("author_count"); 
			result.close(); 

			html += "<p><b><font style = \"font-size:25px\">" + URLGen.addLinkage("organization", oid, organizationName) + "</font>"; 
			if(homepage != null && !homepage.isEmpty())
			{
				html += " (<a href=\"" + homepage + "\">Official Hompage</a>)"; 
			}
			html += "</b></p>"; 
			
			// 2. top authors in this organization;  
			ArrayList<Integer> aids = new ArrayList<Integer>(); 
			result = statement.executeQuery("SELECT aid, citation_count FROM author WHERE oid = " + oid + " ORDER BY citation_count DESC LIMIT 0, 1000; "); 
			while(result.next())
			{
				aids.add(result.getInt("aid")); 
			}
			result.close(); 
			
			if(aids.size() > 0)
			{
				html += "<hr><p><b>Top Authors: </b>" + URLGen.viewListURL("(view all " + author_count + ")", "target=authors", "page=1", "oid=" + oid) + "</p>"; 
			}
			for(int i = 0; i < aids.size() && i < 10; i++)
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
			html = "no such organization..."; 
		}

		return html; 
	}

	public static String organizationShort(Statement statement, int oid) throws SQLException
	{
		String html = ""; 
		
		String basicInfo = "SELECT * FROM organization WHERE oid = " + oid; 
		ResultSet result = statement.executeQuery(basicInfo); 
		
		if(result.next())
		{
			String organizationName = result.getString("name"); 
			html += URLGen.addLinkage("organization", oid, organizationName); 
		}

		return html; 
	}
}