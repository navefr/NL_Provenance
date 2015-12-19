package keywordSearch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class KeywordSearch 
{
	public static void main(String [] args) throws SQLException, ClassNotFoundException
	{
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306/";
		String user = "root";
		String password = "";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(db_url, user, password);
		Statement statement = conn.createStatement();
		
		String keywords = "h. v. jagadish"; 
		ArrayList<ItemList> listList = search(keywords, statement); 
		for(int i = 0; i < listList.get(0).items.size(); i++)
		{
			System.out.println(listList.get(0).items.get(i).id + " " + listList.get(0).items.get(i).relevance);
		}
	}
	
	public static ArrayList<ItemList> search(String keywords, Statement statement) throws SQLException
	{
		keywords = keywords.replaceAll("\"", ""); 
		keywords = keywords.replaceAll("\\+", " "); 
		
		ArrayList<ItemList> listList = new ArrayList<ItemList>(); 
		listList.add(searchAuthor(keywords, statement)); 
		listList.add(searchPaper(keywords, statement)); 
		listList.add(searchOrganization(keywords, statement)); 
		listList.add(searchConference(keywords, statement)); 
		listList.add(searchJournal(keywords, statement)); 
		listList.add(searchKeyword(keywords, statement)); 
		listList.add(searchDomain(keywords, statement)); 
		
		for(int i = 0; i < listList.size(); i++)
		{
			if(listList.get(i).items.isEmpty())
			{
				listList.remove(i); 
				i--; 
			}
		}
		
		for(int i = 0; i < listList.size(); i++)
		{
			int max_pos = i; 
			double max_sim = listList.get(i).items.get(0).relevance; 
			
			for(int j = i+1; j < listList.size(); j++)
			{
				if(listList.get(j).items.get(0).relevance > max_sim)
				{
					max_pos = j; 
					max_sim = listList.get(j).items.get(0).relevance; 
				}
			}
			
			ItemList list = listList.get(max_pos); 
			listList.set(max_pos, listList.get(i)); 
			listList.set(i, list); 
		}
		
		if(listList.size() > 0)
		{
			double threshold = listList.get(0).items.get(0).relevance/2; 
			for(int i = 0; i < listList.size(); i++)
			{
				if(listList.get(i).items.get(0).relevance < threshold)
				{
					listList.remove(i); 
					i--; 
				}
			}
		}
		
		for(int i = 0; i < listList.size(); i++)
		{
			double max_sim = listList.get(i).items.get(0).relevance; 
			for(int j = 0; j < listList.get(i).items.size(); j++)
			{
				if(listList.get(i).items.get(j).relevance < max_sim/2)
				{
					listList.get(i).items.remove(j); 
					j--; 
				}
			}
		}
		
		return listList; 
	}
	
	public static ItemList searchAuthor(String keywords, Statement statement) throws SQLException
	{
		ItemList authorList = new ItemList("author"); 
		ResultSet rs = statement.executeQuery("SELECT aid, name, importance FROM dblp_plus.author WHERE MATCH(name) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 
		
		while(rs.next())
		{
			authorList.addItem(rs.getInt("aid"), keywords, rs.getString("name"), rs.getInt("importance")); 
		}
		rs.close();
		
		rs = statement.executeQuery("SELECT aid, name, organization, importance FROM dblp_plus.author WHERE MATCH(name, organization) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 
		while(rs.next())
		{
			authorList.addItem(rs.getInt("aid"), keywords, rs.getString("name") + "" + rs.getString("organization"), rs.getInt("importance")); 
		}
		rs.close();
		return authorList; 
	}

	public static ItemList searchPaper(String keywords, Statement statement) throws SQLException
	{
		ItemList authorList = new ItemList("publication"); 
		ResultSet rs = statement.executeQuery("SELECT pid, title, importance FROM dblp_plus.publication WHERE MATCH(title) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 
		while(rs.next())
		{
			authorList.addItem(rs.getInt("pid"), keywords, rs.getString("title"), rs.getInt("importance")); 
		}
		rs.close();
		
		rs = statement.executeQuery("SELECT pid, title, conference_journal, importance FROM dblp_plus.publication WHERE MATCH(title, conference_journal) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 
		while(rs.next())
		{
			authorList.addItem(rs.getInt("pid"), keywords, rs.getString("title") + "" + rs.getString("conference_journal"), rs.getInt("importance")); 
		}
		rs.close();
		return authorList; 
	}

	public static ItemList searchOrganization(String keywords, Statement statement) throws SQLException
	{
		ItemList authorList = new ItemList("organization"); 
		ResultSet rs = statement.executeQuery("SELECT oid, name, name_short, importance FROM dblp_plus.organization WHERE MATCH(name) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 
		while(rs.next())
		{
			authorList.addItem(rs.getInt("oid"), keywords, rs.getString("name"), rs.getInt("importance")); 
		}
		rs.close();
		
		rs = statement.executeQuery("SELECT oid, name, name_short, importance FROM dblp_plus.organization WHERE MATCH(name_short) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 
		while(rs.next())
		{
			authorList.addItem(rs.getInt("oid"), keywords, rs.getString("name_short"), rs.getInt("importance")); 
		}
		rs.close();

		return authorList; 
	}

	public static ItemList searchConference(String keywords, Statement statement) throws SQLException
	{
		ItemList authorList = new ItemList("conference"); 
		ResultSet rs = statement.executeQuery("SELECT cid, name, full_name, importance FROM dblp_plus.conference WHERE MATCH(name) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 
		while(rs.next())
		{
			authorList.addItem(rs.getInt("cid"), keywords, rs.getString("name"), rs.getInt("importance")); 
		}
		rs.close();
		
		rs = statement.executeQuery("SELECT cid, name, full_name, importance FROM dblp_plus.conference WHERE MATCH(full_name) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 
		while(rs.next())
		{
			authorList.addItem(rs.getInt("cid"), keywords, rs.getString("full_name"), rs.getInt("importance")); 
		}
		rs.close();

		return authorList; 
	}

	public static ItemList searchJournal(String keywords, Statement statement) throws SQLException
	{
		ItemList authorList = new ItemList("journal"); 
		ResultSet rs = statement.executeQuery("SELECT jid, name, full_name, importance FROM dblp_plus.journal WHERE MATCH(name) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 		
		while(rs.next())
		{
			authorList.addItem(rs.getInt("jid"), keywords, rs.getString("name"), rs.getInt("importance")); 
		}
		rs.close();
		
		rs = statement.executeQuery("SELECT jid, name, full_name, importance FROM dblp_plus.journal WHERE MATCH(full_name) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 		
		while(rs.next())
		{
			authorList.addItem(rs.getInt("jid"), keywords, rs.getString("full_name"), rs.getInt("importance")); 
		}
		rs.close();
		
		return authorList; 
	}

	public static ItemList searchKeyword(String keywords, Statement statement) throws SQLException
	{
		ItemList authorList = new ItemList("keyword"); 
		ResultSet rs = statement.executeQuery("SELECT kid, keyword, keyword_short, importance FROM dblp_plus.keyword WHERE MATCH(keyword) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 		
		while(rs.next())
		{
			authorList.addItem(rs.getInt("kid"), keywords, rs.getString("keyword"), rs.getInt("importance")); 
		}
		rs.close();		
		
		rs = statement.executeQuery("SELECT kid, keyword, keyword_short, importance FROM dblp_plus.keyword WHERE MATCH(keyword_short) AGAINST (\"" + keywords +  "\") LIMIT 0, 100; "); 		
		while(rs.next())
		{
			authorList.addItem(rs.getInt("kid"), keywords, rs.getString("keyword_short"), rs.getInt("importance")); 
		}
		rs.close();		
		
		return authorList; 
	}
	
	public static ItemList searchDomain(String keywords, Statement statement) throws SQLException
	{
		ItemList authorList = new ItemList("domain"); 
		ResultSet rs = statement.executeQuery("SELECT did, name, importance FROM dblp_plus.domain LIMIT 0, 100; "); 
		
		while(rs.next())
		{
			authorList.addItem(rs.getInt("did"), keywords, rs.getString("name"), rs.getInt("importance")); 
		}
		rs.close();		
		return authorList; 
	}
}
