package htmlGeneration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Homepage 
{
	public static String homepage(Statement statement) throws SQLException
	{
		String html = ""; 
		ArrayList<Integer> dids = new ArrayList<Integer>(); 
		ResultSet rs = statement.executeQuery("SELECT * FROM dblp_plus.domain ORDER BY name; "); 
		while(rs.next())
		{
			dids.add(rs.getInt(1)); 
		}
		
		for(int i = 0; i < 24; i++)
		{
			html += addBlock(statement, dids.get(i), i+1); 
			if((i+1) % 2 == 0)
			{
				html += "<div style=\"clear:both\"></div>"; 
			}
		}
		
		return html; 
	}
	
	public static String addBlock(Statement statement, int did, int order) throws SQLException
	{
		ResultSet rs = statement.executeQuery("SELECT * FROM dblp_plus.domain WHERE did = " + did); 
		String domain_name = ""; 
		if(rs.next())
		{
			domain_name = rs.getString(2); 
		}
		rs.close();
		
		String block = ""; 
		String float_left = ""; 
		int margin_left = 0; 
		int margin_top = 0; 
		
		if(order % 2 == 1)
		{
			margin_left = 5; 
			float_left = "float:left; "; 
		}
		else
		{
			margin_left = 490; 
		}
		
		if(order < 3)
		{
			margin_top = 0; 
		}
		else
		{
			margin_top = 20; 
		}
		
		block += "<div style=\"" + float_left + "width:430px; position:relative; margin-left:" + margin_left + "px; margin-top:" + margin_top + "px; padding:1px 10px 1px 10px; background-color:#ebebeb\">"; 
		block += "<div style=\"font-size:17px; margin-top:10px; margin-bottom:8px\"><b>" + URLGen.addLinkage("domain", did, domain_name) + ": </b></div>"; 
		
		rs = statement.executeQuery("SELECT * FROM dblp_plus.top_authors WHERE did = " + did + " ORDER BY rank LIMIT 0, 6; "); 
		while(rs.next())
		{
			int aid = rs.getInt(2); 
			String photoURL = rs.getString(4); 
			String photo = URLGen.addLinkage("author", aid, "<img src=\"" + photoURL + "\" style=\"height:72px; width:66px; margin-right:5px\">"); 
			block += photo; 
		}
		rs.close(); 
		
		block += "<div style=\"margin-top:10px; margin-bottom:5px\">See top " + URLGen.viewListURL("Conferences", "target=conferences", "page=1", "did=" + did); 
		block += " &amp; "; 
		block += URLGen.viewListURL("Journals", "target=journals", "page=1", "did=" + did) + " in this Area.</div>"; 
		block += "</div>"; 
		return block; 
	}
}
