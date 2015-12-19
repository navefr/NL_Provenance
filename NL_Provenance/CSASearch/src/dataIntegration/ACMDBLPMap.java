package dataIntegration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

import functions.NaviFunctions;

public class ACMDBLPMap 
{
	public static void main(String [] args) throws ClassNotFoundException, SQLException
	{
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306/";
		String user = "root";
		String password = "";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(db_url, user, password);		
		Statement statement = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Integer.MIN_VALUE);
		
		Connection connUpdate = DriverManager.getConnection(db_url, user, password);	
		Statement statementUpdate = connUpdate.createStatement();
//		mapPublicationThroughDOI(statement, statementUpdate); 
//		mapPublicationByTitle(statement, statementUpdate); 
		mapRawPublication(statement, statementUpdate); 
	}
	
	public static void mapPublicationThroughDOI(Statement statement, Statement statementUpdate) throws SQLException
	{
		Hashtable<Integer, Integer> dblp_papers = new Hashtable<Integer, Integer>(); 
		ResultSet rs = statement.executeQuery("SELECT pid, doi FROM dblp.publication WHERE doi <> \"\" AND doi LIKE \"%/10.%\"; "); 
		while(rs.next())
		{
			int dblp_id = rs.getInt(1); 
			String dblp_doi = rs.getString(2); 
			dblp_doi = dblp_doi.substring(dblp_doi.indexOf("/10.")+1); 
			
			if(!dblp_papers.containsKey(dblp_doi.hashCode()))
			{
				dblp_papers.put(dblp_doi.hashCode(), dblp_id); 
			}
		}
		rs.close();
		
		System.out.println("done!");
		
		rs = statement.executeQuery("SELECT pid, doi FROM acm.publication WHERE doi <> \"\" AND doi LIKE \"10.%\";"); 
		while(rs.next())
		{
			int acm_id = rs.getInt(1); 
			String acm_doi = rs.getString(2); 
			
			if(dblp_papers.containsKey(acm_doi.hashCode()))
			{
				String insert = "INSERT INTO acm.map_paper VALUES (" + dblp_papers.get(acm_doi.hashCode()) + ", " + acm_id + ", \"\", \"\"); "; 
				try
				{
					statementUpdate.executeUpdate(insert); 
				}
				catch(Exception e)
				{
					System.out.println(insert);
				}
			}
		}
	}
	
	public static void mapPublicationByTitle(Statement statement, Statement statementUpdate) throws SQLException
	{
		Hashtable<Integer, Integer> dblp_papers = new Hashtable<Integer, Integer>(); 
		ResultSet rs = statement.executeQuery("SELECT pid, title, year FROM dblp.publication WHERE pid NOT IN (SELECT dblp_pid FROM acm.map_paper); "); 
		while(rs.next())
		{
			int dblp_id = rs.getInt(1); 
//			int dblp_title_year = NaviFunctions.hashTitle(rs.getString(2) + " " + rs.getInt(3)); 
			int dblp_title_year = NaviFunctions.hashTitle(rs.getString(2)); 
			
			if(!dblp_papers.containsKey(dblp_title_year))
			{
				dblp_papers.put(dblp_title_year, dblp_id); 
			}
		}
		rs.close();
		
		System.out.println("done!");
		
		rs = statement.executeQuery("SELECT pid, title, year FROM acm.publication WHERE pid NOT IN (SELECT acm_pid FROM acm.map_paper); "); 
		for(int i = 0; rs.next(); )
		{
			int acm_id = rs.getInt(1); 
			String year = rs.getString(3); 
			year = year.split("/")[2]; 
//			int acm_title_year = NaviFunctions.hashTitle(rs.getString(2) + " " + year); 
			int acm_title_year = NaviFunctions.hashTitle(rs.getString(2)); 
			
			if(dblp_papers.containsKey(acm_title_year))
			{
				i++; 
				String insert = "INSERT INTO acm.map_paper VALUES (" + dblp_papers.get(acm_title_year) + ", " + acm_id + ", \"\", \"\"); "; 
				try
				{
					statementUpdate.executeUpdate(insert); 
				}
				catch(Exception e)
				{
					System.out.println(insert);
				}
				
				if(i % 1000 == 0)
				{
					System.out.println(i + ": " + insert);
				}
			}
		}
	}

	public static void mapRawPublication(Statement statement, Statement statementUpdate) throws SQLException
	{
		class Paper
		{
			int pid; 
			String title; 
			String n_title; 
			
			Paper(int pid, String title)
			{
				this.pid = pid; 
				this.title = title.replaceAll("\"", ""); 
				if(this.title.endsWith("\\"))
				{
					this.title = this.title.substring(0, this.title.length()-1); 
				}
				this.n_title = NaviFunctions.normalizeTitle(title); 
			}
		}
		
		Hashtable<Integer, ArrayList<Paper>> acm_papers = new Hashtable<Integer, ArrayList<Paper>>(); 
		for(int i = 1930; i < 2016; i++)
		{
			acm_papers.put(i, new ArrayList<Paper>()); 
		}
		
		ResultSet rs = statement.executeQuery("SELECT raw_id, text FROM acm.raw_publication WHERE raw_id NOT IN (SELECT acm_pid FROM map_paper); "); 
		while(rs.next())
		{
			int raw_id = rs.getInt(1); 
			String text = rs.getString(2); 
			
			for(int i = 1930; i < 2016; i++)
			{
				if(text.contains(""+i))
				{
					Paper raw_paper = new Paper(raw_id, text); 
					acm_papers.get(i).add(raw_paper); 
				}
			}
		}
		rs.close();
		
		System.out.println("acm done; ");
		
		Hashtable<Integer, ArrayList<Paper>> dblp_papers = new Hashtable<Integer, ArrayList<Paper>>(); 
		for(int i = 1930; i < 2016; i++)
		{
			dblp_papers.put(i, new ArrayList<Paper>()); 
		}

		rs = statement.executeQuery("SELECT pid, year, title FROM dblp.publication WHERE length(title) > 20;"); 
		while(rs.next())
		{
			int pid = rs.getInt(1); 
			int year = rs.getInt(2); 
			String title = rs.getString(3); 
			
			Paper paper = new Paper(pid, title); 
			
			if(dblp_papers.containsKey(year) && title.split(" ").length > 3)
			{
				dblp_papers.get(year).add(paper); 
			}
		}
		rs.close(); 
		
		System.out.println("dblp done; ");

		for(int i = 1930; i < 2016; i++)
		{
			ArrayList<Paper> acm_paper_list = acm_papers.get(i); 
			ArrayList<Paper> dblp_paper_list = dblp_papers.get(i); 
			
			for(int j = 0; j < acm_paper_list.size(); j++)
			{
				Paper acm_paper = acm_paper_list.get(j); 
				String acm_title = acm_paper.n_title; 
				for(int k = 0; k < dblp_paper_list.size(); k++)
				{
					Paper dblp_paper = dblp_paper_list.get(k);
					String dblp_title = dblp_paper.n_title; 
					if(acm_title.contains(dblp_title))
					{
						String insert = "INSERT INTO acm.map_paper VALUES (" + dblp_paper.pid + ", " + acm_paper.pid + ", \"" + dblp_paper.title + "\", \"" + acm_paper.title + "\"); "; 
						statementUpdate.executeUpdate(insert); 
					}
				}
				
				if(j > 0 && j % 10000 == 0)
				{
					System.out.println(j);
				}
			}
			
			System.out.println("year " + i + ": " + acm_paper_list.size());
		}
	}
}