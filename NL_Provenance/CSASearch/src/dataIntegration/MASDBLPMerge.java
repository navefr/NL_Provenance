package dataIntegration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class MASDBLPMerge 
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

//		mergeDomain(statement, statementUpdate); 
//		mergeKeyword(statement, statementUpdate); 
//		mergeOrganization(statement, statementUpdate); 
//		mergeConference(statement, statementUpdate); 
//		mergeJournal(statement, statementUpdate); 
//		mergePaper(statement, statementUpdate); 
		mergeAuthor(statement, statementUpdate); 
	}

	// Step 1. merge domain; 
	public static void mergeDomain(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet rs = statement.executeQuery("SELECT did, name FROM mas.domain; "); 
		while(rs.next())
		{
			int did = rs.getInt(1); 
			String name = rs.getString(2); 
			
			String update = "INSERT INTO dblp_plus.domain VALUES(" + did + ", \"" + name + "\", 0); ";  
			statementUpdate.executeUpdate(update); 
		}
	}
	
	// Step 2. merge keyword; domain_keyword; keyword variations; 
	public static void mergeKeyword(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet rs = statement.executeQuery("SELECT kid, keyword, keyword_short FROM mas.keyword; "); 
		while(rs.next())
		{
			int kid = rs.getInt(1); 
			String name = rs.getString(2); 
			String name_short = rs.getString(3); 
			
			String update = "INSERT INTO dblp_plus.keyword VALUES (" + kid + ", \"" + name + "\", \"" + name_short + "\", 0); ";  
			statementUpdate.executeUpdate(update); 
		}
		rs.close(); 
		
		rs = statement.executeQuery("SELECT kid, did, rank FROM mas.domain_keyword WHERE did IN (SELECT did FROM dblp_plus.domain) AND kid IN (SELECT kid FROM dblp_plus.keyword); "); 
		while(rs.next())
		{
			int kid = rs.getInt(1); 
			int did = rs.getInt(2); 
			int order = rs.getInt(3); 
			
			String update = "INSERT INTO dblp_plus.domain_keyword VALUES (" + kid + ", " + did + ", " + order + "); ";  
			statementUpdate.executeUpdate(update); 
		}		
		rs.close(); 
		
		rs = statement.executeQuery("SELECT kid, variation FROM mas.keyword_variations WHERE kid IN (SELECT kid FROM dblp_plus.keyword); "); 
		while(rs.next())
		{
			int kid = rs.getInt(1); 
			String variation = rs.getString(2); 
			
			String update = "INSERT INTO dblp_plus.keyword_variations VALUES (" + kid + ", \"" + variation + "\"); ";  
			statementUpdate.executeUpdate(update); 
		}		
		rs.close(); 
	}
	
	// Step 3. merge organizations; 
	public static void mergeOrganization(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet rs = statement.executeQuery("SELECT * FROM mas.organization; "); 
		while(rs.next())
		{
			int oid = rs.getInt(1); 
			String name = rs.getString(2); 
			String continent = rs.getString(3); 
			String homepage = rs.getString(4); 
			
			String update = "INSERT INTO dblp_plus.organization VALUES(" + oid + ", \"" + name + "\", \"\", \"" + continent + "\", \"" + homepage + "\", 0, 0); ";  
			statementUpdate.executeUpdate(update); 
		}
	}
	
	// Step 4. merge conference; conference_domain;  
	public static void mergeConference(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		Hashtable<Integer, ArrayList<Integer>> mas_dblp = new Hashtable<Integer, ArrayList<Integer>>(); 
		Hashtable<Integer, Integer> dblp_mas = new Hashtable<Integer, Integer>(); 
		ResultSet map = statement.executeQuery("SELECT mas_id, dblp_id FROM dblp.map_conference; "); 
		while(map.next())
		{
			dblp_mas.put(map.getInt(2), map.getInt(1)); 
			if(mas_dblp.containsKey(map.getInt(1)))
			{
				mas_dblp.get(map.getInt(1)).add(map.getInt(2)); 				
			}
			else
			{
				ArrayList<Integer> dblps = new ArrayList<Integer>(); 
				dblps.add(map.getInt(2)); 
				mas_dblp.put(map.getInt(1), dblps); 
			}
		}
		map.close(); 
		
		ResultSet domain_conference = statement.executeQuery("SELECT cid, did FROM mas.domain_conference; "); 
		while(domain_conference.next())
		{
			if(mas_dblp.containsKey(domain_conference.getInt(1)))
			{
				ArrayList<Integer> dblps = mas_dblp.get(domain_conference.getInt(1)); 
				for(int i = 0; i < dblps.size(); i++)
				{
					if(domain_conference.getInt(2) > 0)
					{
						try
						{
							statementUpdate.executeUpdate("INSERT INTO dblp_plus.domain_conference VALUES (" + dblps.get(i) + ", " + domain_conference.getInt(2) + "); "); 
						}
						catch(Exception e) {}
					}
				}
			}
		}
		domain_conference.close(); 
		
		class Conference
		{
			int cid; 
			String homepage; 
			
			Conference(int cid, String name, String homepage)
			{
				this.cid = cid; 
				this.homepage = homepage; 
			}
		}
		
		Hashtable<Integer, Conference> mas_conferences = new Hashtable<Integer, Conference>(); 
		ResultSet mas_c = statement.executeQuery("SELECT cid, name, homepage FROM mas.conference; ");
		while(mas_c.next())
		{
			mas_conferences.put(mas_c.getInt(1), new Conference(mas_c.getInt(1), mas_c.getString(2), mas_c.getString(3))); 
		}
		mas_c.close();
		
		ResultSet dblp_c = statement.executeQuery("SELECT cid, name, full_name FROM dblp.conference; ");
		while(dblp_c.next())
		{
			int cid = dblp_c.getInt(1); 
			int mas_cid = 0; 
			String name = dblp_c.getString(2); 
			String full_name = dblp_c.getString(3); 
			String homepage = ""; 
			if(dblp_mas.containsKey(cid) && mas_conferences.containsKey(dblp_mas.get(cid)))
			{
				mas_cid = mas_conferences.get(dblp_mas.get(cid)).cid; 
				homepage = mas_conferences.get(dblp_mas.get(cid)).homepage; 
			}
			String insert = "INSERT INTO dblp_plus.conference VALUES(" + cid + ", " + mas_cid + ", \"" + name + "\", \"" + full_name + "\", \"" + homepage + "\", 0, 0, 0); "; 
			statementUpdate.executeUpdate(insert); 
		}
		dblp_c.close();
	}
	
	// Step 5. merge journal; journal_domain;  
	public static void mergeJournal(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		Hashtable<Integer, ArrayList<Integer>> mas_dblp = new Hashtable<Integer, ArrayList<Integer>>(); 
		Hashtable<Integer, Integer> dblp_mas = new Hashtable<Integer, Integer>(); 
		ResultSet map = statement.executeQuery("SELECT mas_id, dblp_id FROM dblp.map_journal; "); 
		while(map.next())
		{
			dblp_mas.put(map.getInt(2), map.getInt(1)); 
			if(mas_dblp.containsKey(map.getInt(1)))
			{
				mas_dblp.get(map.getInt(1)).add(map.getInt(2)); 				
			}
			else
			{
				ArrayList<Integer> dblps = new ArrayList<Integer>(); 
				dblps.add(map.getInt(2)); 
				mas_dblp.put(map.getInt(1), dblps); 
			}
		}
		map.close(); 
		
		ResultSet domain_journal = statement.executeQuery("SELECT jid, did FROM mas.domain_journal; "); 
		while(domain_journal.next())
		{
			if(mas_dblp.containsKey(domain_journal.getInt(1)))
			{
				ArrayList<Integer> dblps = mas_dblp.get(domain_journal.getInt(1)); 
				for(int i = 0; i < dblps.size(); i++)
				{
					if(domain_journal.getInt(2) > 0)
					{
						try
						{
							statementUpdate.executeUpdate("INSERT INTO dblp_plus.domain_journal VALUES (" + dblps.get(i) + ", " + domain_journal.getInt(2) + "); "); 
						}
						catch(Exception e) {}
					}
				}
			}
		}
		domain_journal.close(); 
		
		class Journal
		{
			int jid; 
			String homepage; 
			
			Journal(int jid, String name, String homepage)
			{
				this.jid = jid; 
				this.homepage = homepage; 
			}
		}

		Hashtable<Integer, Journal> mas_journals = new Hashtable<Integer, Journal>(); 
		ResultSet mas_c = statement.executeQuery("SELECT jid, name, homepage FROM mas.journal; ");
		while(mas_c.next())
		{
			mas_journals.put(mas_c.getInt(1), new Journal(mas_c.getInt(1), mas_c.getString(2), mas_c.getString(3))); 
		}
		mas_c.close();
		
		ResultSet dblp_c = statement.executeQuery("SELECT jid, name, full_name FROM dblp.journal; ");
		while(dblp_c.next())
		{
			int jid = dblp_c.getInt(1); 
			int mas_jid = 0; 
			String name = dblp_c.getString(2); 
			String full_name = dblp_c.getString(3); 
			String homepage = ""; 
			if(dblp_mas.containsKey(jid) && mas_journals.containsKey(dblp_mas.get(jid)))
			{
				mas_jid = mas_journals.get(dblp_mas.get(jid)).jid; 
				homepage = mas_journals.get(dblp_mas.get(jid)).homepage; 
			}
			String insert = "INSERT INTO dblp_plus.journal VALUES(" + jid + ", " + mas_jid + ", \"" + name + "\", \"" + full_name + "\", \"" + homepage + "\", 0, 0, 0); "; 
			statementUpdate.executeUpdate(insert); 
		}
		dblp_c.close();
	}
	
	// Step 6. merge paper; paper_keyword; citation; 
	public static void mergePaper(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		int count = 0; 
		
		// merge paper; 
		Hashtable<Integer, Integer> dblp_mas = new Hashtable<Integer, Integer>(); 
		Hashtable<Integer, Integer> mas_dblp = new Hashtable<Integer, Integer>(); 
		ResultSet paper_map = statement.executeQuery("SELECT dblp_id, mas_id FROM dblp.map_paper; "); 
		while(paper_map.next())
		{
			dblp_mas.put(paper_map.getInt(1), paper_map.getInt(2)); 
			mas_dblp.put(paper_map.getInt(2), paper_map.getInt(1)); 
		}
		paper_map.close(); 
		
		System.out.println("1. ");
		
		Hashtable<Integer, Boolean> dblp_pids = new Hashtable<Integer, Boolean>(); 
		ResultSet dblp_paper = statement.executeQuery("SELECT pid FROM dblp.publication; "); 
		while(dblp_paper.next())
		{
			dblp_pids.put(dblp_paper.getInt(1), true); 
		}
		dblp_paper.close(); 
		
		System.out.println("2. ");

		Iterator<Integer> iter_dblp = dblp_pids.keySet().iterator(); 
		while(iter_dblp.hasNext())
		{
			int pid = iter_dblp.next(); 
			int mas_id = 0; 
			
			ResultSet dblp_p = statement.executeQuery("SELECT title, year, cid, jid, doi FROM dblp.publication WHERE pid = " + pid); 
			if(dblp_p.next())
			{
				String title = dblp_p.getString(1); 
				String paper_abstract = ""; 
				int year = dblp_p.getInt(2); 
				int cid = dblp_p.getInt(3); 
				int jid = dblp_p.getInt(4); 
				String doi = dblp_p.getString(5); 
				
				if(dblp_mas.containsKey(pid))
				{
					mas_id = dblp_mas.get(pid); 
					ResultSet mas_paper = statement.executeQuery("SELECT title, abstract FROM mas.publication WHERE pid = " + mas_id); 
					if(mas_paper.next())
					{
						paper_abstract = mas_paper.getString(2); 
					}
					mas_paper.close(); 
				}
				
				String command = "INSERT INTO dblp_plus.publication VALUES (" + pid + ", " + mas_id + ", \"" + title + "\", \"" + paper_abstract + "\", "
					+ year + ", \"" + doi + "\", " + cid + ", " + jid + ", 0, 0, 0, \"\")";
				try
				{
					statementUpdate.executeUpdate(command); 
				}
				catch(Exception e) {}
			}
			
			if(count % 10000 == 0)
			{
				System.out.println(count);
			}
			count++; 
		}
		
		System.out.println("3. ");

		// merge paper_keyword; 
		ResultSet paper_keyword = statement.executeQuery("SELECT pid, kid FROM mas.publication_keyword; "); 
		while(paper_keyword.next())
		{
			int mas_pid = paper_keyword.getInt(1); 
			int kid = paper_keyword.getInt(2); 
			if(mas_dblp.containsKey(mas_pid))
			{
				int dblp_pid = mas_dblp.get(mas_pid); 
				String command = "INSERT INTO dblp_plus.publication_keyword VALUES (" + dblp_pid + ", " + kid + "); "; 
				try
				{
					statementUpdate.executeUpdate(command); 
				}
				catch(Exception e) {}
			}
			
			if(count % 10000 == 0)
			{
				System.out.println(count);
			}
			count++; 
		}
		paper_keyword.close(); 

		System.out.println("4. ");

		// merge citation; 
		ResultSet citation = statement.executeQuery("SELECT * FROM mas.cite; "); 
		while(citation.next())
		{
			int citing = citation.getInt(1); 
			int cited = citation.getInt(2); 
			if(mas_dblp.containsKey(citing) && mas_dblp.containsKey(cited))
			{
				citing = mas_dblp.get(citing); 
				cited = mas_dblp.get(cited); 
				String command = "INSERT INTO dblp_plus.cite VALUES (" + citing + ", " + cited + "); "; 
				try
				{
					statementUpdate.executeUpdate(command); 
				}
				catch(Exception e) {}
			}
			
			if(count % 10000 == 0)
			{
				System.out.println(count);
			}
			count++; 
		}
		citation.close(); 
		
		System.out.println("5. ");
	}
	
	// merge author; author_domain;  
	public static void mergeAuthor(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		int count = 0; 
		
		// merge author; 
		Hashtable<Integer, Integer> dblp_mas = new Hashtable<Integer, Integer>(); 
		Hashtable<Integer, ArrayList<Integer>> mas_dblp = new Hashtable<Integer, ArrayList<Integer>>(); 
		ResultSet paper_map = statement.executeQuery("SELECT dblp_id, mas_id FROM dblp.map_author; "); 
		while(paper_map.next())
		{
			dblp_mas.put(paper_map.getInt(1), paper_map.getInt(2)); 
			if(mas_dblp.containsKey(paper_map.getInt(2)))
			{
				mas_dblp.get(paper_map.getInt(2)).add(paper_map.getInt(1)); 
			}
			else
			{
				ArrayList<Integer> mas_aids = new ArrayList<Integer>(); 
				mas_aids.add(paper_map.getInt(1));
				mas_dblp.put(paper_map.getInt(2), mas_aids); 
			}
		}
		paper_map.close(); 
		
		System.out.println("1. ");
		
		Hashtable<Integer, String> dblp_authors = new Hashtable<Integer, String>(); 
		ResultSet dblp_author = statement.executeQuery("SELECT aid, name FROM dblp.author; "); 
		while(dblp_author.next())
		{
			dblp_authors.put(dblp_author.getInt(1), dblp_author.getString(2)); 
		}
		dblp_author.close(); 
		
		System.out.println("2. ");

		Iterator<Integer> iter_dblp = dblp_mas.keySet().iterator(); 
		while(iter_dblp.hasNext())
		{
			int aid = iter_dblp.next(); 
			int mas_id = 0; 
			String name = dblp_authors.get(aid); 
			int oid = 0; 
			String homepage = ""; 
			String photo = ""; 
			
			if(dblp_mas.containsKey(aid))
			{
				mas_id = dblp_mas.get(aid); 
				ResultSet mas_paper = statement.executeQuery("SELECT name, oid, homepage, photo FROM mas.author WHERE aid = " + mas_id); 
				if(mas_paper.next())
				{
					oid = mas_paper.getInt(2); 
					homepage = mas_paper.getString(3); 
					photo = mas_paper.getString(4); 
				}
				mas_paper.close(); 
			}
			
			String command = "INSERT INTO dblp_plus.author VALUES (" + aid + ", " + mas_id + ", \"" + name + "\", " + oid + ", \"" + homepage + "\", \"" + photo + "\", 0, 0, 0, \"\"); "; 
			statementUpdate.executeUpdate(command); 
			
			if(count % 10000 == 0)
			{
				System.out.println(count);
			}
			count++; 
		}
		
		ResultSet unmappedAuthors = statement.executeQuery("SELECT dblp.author.* FROM dblp.author WHERE dblp.author.aid NOT IN (SELECT aid FROM dblp_plus.author)"); 
		while(unmappedAuthors.next())
		{
			statementUpdate.executeUpdate("INSERT INTO dblp_plus.author VALUES (" + unmappedAuthors.getInt(1) + ", 0, \"" + unmappedAuthors.getString(2) + "\", "
				+ "0, '', '', 0, 0, 0, ''); "); 
			if(count % 10000 == 0)
			{
				System.out.println(count);
			}
			count++; 
		}
		
		// merge author_domain; 
		ResultSet author_domain = statement.executeQuery("SELECT aid, did FROM mas.domain_author; "); 
		while(author_domain.next())
		{
			int mas_aid = author_domain.getInt(1); 
			int did = author_domain.getInt(2); 
			if(mas_dblp.containsKey(mas_aid))
			{
				for(int i = 0; i < mas_dblp.get(mas_aid).size(); i++)
				{
					int dblp_aid = mas_dblp.get(mas_aid).get(i); 
					String command = "INSERT INTO dblp_plus.domain_author VALUES (" + dblp_aid + ", " + did + ", 0, 0); "; 
					try
					{
						statementUpdate.executeUpdate(command); 
					}
					catch(Exception e) {}
				}
			}
			
			if(count % 10000 == 0)
			{
				System.out.println(count);
			}
			count++; 
		}
		author_domain.close(); 
	}
}
