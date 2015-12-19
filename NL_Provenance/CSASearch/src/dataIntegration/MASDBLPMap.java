package dataIntegration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

import functions.NaviFunctions;
import functions.SimilarityFunctions;

public class MASDBLPMap 
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

//		MapPaperThroughDOIExactly(statement, statementUpdate); 
//		MapPaperByTitleYearExactly(statement, statementUpdate); 
//		MapCJThroughPaperMap(statement, statementUpdate); 
//		MapCJByName(statement, statementUpdate); 

//		MapPaperThroughCJ(statement, statementUpdate); 
//		RefinePaperMapping(statement, statementUpdate); 
//		MapAuthorBasedOnPaper(statement, statementUpdate); 
//		ReComputeAuthorSim(statement, statementUpdate); 
		RefineAuthorMappings(statement, statementUpdate); 
	}
	
	// Step 1. map papers based on the doi number; 
	public static void MapPaperThroughDOIExactly(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
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
		
		rs = statement.executeQuery("SELECT pid, doi FROM mas.publication WHERE doi <> \"\"; "); 
		while(rs.next())
		{
			int mas_id = rs.getInt(1); 
			String mas_doi = rs.getString(2); 
			
			if(dblp_papers.containsKey(mas_doi.hashCode()))
			{
				String insert = "INSERT INTO dblp.map_paper VALUES (" + dblp_papers.get(mas_doi.hashCode()) + ", " + mas_id + "); "; 
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

	// Step 2. map papers exactly based on year and title; 
	public static void MapPaperByTitleYearExactly(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		Hashtable<Integer, Integer> dblp_papers = new Hashtable<Integer, Integer>(); 
		ResultSet rs = statement.executeQuery("SELECT pid, title, year FROM dblp.publication WHERE pid NOT IN (SELECT dblp_id FROM dblp.map_paper); "); 
		while(rs.next())
		{
			int dblp_id = rs.getInt(1); 
			int dblp_title_year = NaviFunctions.hashTitle(rs.getString(2) + " " + rs.getInt(3)); 
			
			if(!dblp_papers.containsKey(dblp_title_year))
			{
				dblp_papers.put(dblp_title_year, dblp_id); 
			}
		}
		rs.close();
		
		System.out.println("done!");
		
		rs = statement.executeQuery("SELECT pid, title, year FROM mas.publication WHERE pid NOT IN (SELECT mas_id FROM dblp.map_paper); "); 
		for(int i = 0; rs.next(); )
		{
			int mas_id = rs.getInt(1); 
			int mas_title_year = NaviFunctions.hashTitle(rs.getString(2) + " " + rs.getInt(3)); 
			
			if(dblp_papers.containsKey(mas_title_year))
			{
				i++; 
				String insert = "INSERT INTO dblp.map_paper VALUES (" + dblp_papers.get(mas_title_year) + ", " + mas_id + "); "; 
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
					System.out.println(i);
				}
			}
		}
	}
	
	// Step 3. map conferences and journals based on map_paper; 
	public static void MapCJThroughPaperMap(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet rs = statement.executeQuery("SELECT dblp.publication.cid, mas.publication.cid, count(*) num FROM dblp.publication, mas.publication, dblp.map_paper"
			+ " WHERE dblp.map_paper.dblp_id = dblp.publication.pid AND dblp.map_paper.mas_id = mas.publication.pid AND dblp.publication.cid <> 0 AND mas.publication.cid <> 0"
			+ " GROUP BY dblp.publication.cid, mas.publication.cid HAVING num > 10 ORDER BY num DESC; "); 
		while(rs.next())
		{
			String insert = "INSERT INTO dblp.map_conference VALUES (" + rs.getInt(1) + ", " + rs.getInt(2) + ", " + rs.getInt(3) + "); "; 
			statementUpdate.executeUpdate(insert); 
		}
		rs.close();
		
		rs = statement.executeQuery("SELECT DISTINCT dblp.publication.jid, mas.publication.jid, count(*) num FROM dblp.publication, mas.publication, dblp.map_paper"
			+ " WHERE dblp.map_paper.dblp_id = dblp.publication.pid AND dblp.map_paper.mas_id = mas.publication.pid AND dblp.publication.jid <> 0 AND mas.publication.jid <> 0"
			+ " GROUP BY dblp.publication.jid, mas.publication.jid HAVING num > 10 ORDER BY num DESC; "); 
		while(rs.next())
		{
			String insert = "INSERT INTO dblp.map_journal VALUES (" + rs.getInt(1) + ", " + rs.getInt(2) + ", " + rs.getInt(3) + "); "; 
			statementUpdate.executeUpdate(insert); 
		}
		rs.close();

		rs = statement.executeQuery("SELECT DISTINCT dblp.publication.cid, mas.publication.jid, count(*) num FROM dblp.publication, mas.publication, dblp.map_paper"
			+ " WHERE dblp.map_paper.dblp_id = dblp.publication.pid AND dblp.map_paper.mas_id = mas.publication.pid AND dblp.publication.cid <> 0 AND mas.publication.jid <> 0"
			+ " GROUP BY dblp.publication.cid, mas.publication.jid HAVING num > 10 ORDER BY num DESC; "); 
		while(rs.next())
		{
			String insert = "INSERT INTO dblp.map_cj VALUES (" + rs.getInt(1) + ", " + rs.getInt(2) + ", " + rs.getInt(3) + "); "; 
			statementUpdate.executeUpdate(insert); 
		}
		rs.close();

		rs = statement.executeQuery("SELECT DISTINCT dblp.publication.jid, mas.publication.cid, count(*) num FROM dblp.publication, mas.publication, dblp.map_paper"
			+ " WHERE dblp.map_paper.dblp_id = dblp.publication.pid AND dblp.map_paper.mas_id = mas.publication.pid AND dblp.publication.jid <> 0 AND mas.publication.cid <> 0"
			+ " GROUP BY dblp.publication.jid, mas.publication.cid HAVING num > 10 ORDER BY num DESC; "); 
		while(rs.next())
		{
			String insert = "INSERT INTO dblp.map_jc VALUES (" + rs.getInt(1) + ", " + rs.getInt(2) + ", " + rs.getInt(3) + "); "; 
			statementUpdate.executeUpdate(insert); 
		}
		rs.close();
	}

	// Step 4. map conferences/journals approximately through name & papers; 
	public static void MapCJByName(Statement statement, Statement statementUpdate) throws SQLException 
	{
		int Q = 2; 
		int arraySize = 200; 
		double SIMILARITY_THRESHOLD = 0.2; 
		ArrayList<Integer> randomNumbers = NaviFunctions.getRandomNumber(arraySize*3); 
		
		ArrayList<ConfJournal> DCs = new ArrayList<ConfJournal>(); // conferences in dblp; 
		ArrayList<ConfJournal> MCs = new ArrayList<ConfJournal>(); // conferences in microsoft academic search; 
		
		Hashtable<Integer, ConfJournal> DCH = new Hashtable<Integer, ConfJournal>(); // hashtable for conference in dblp; 
		Hashtable<Integer, ConfJournal> MCH = new Hashtable<Integer, ConfJournal>(); // hashtable for conference in microsoft; 
		
		// read the data dblp; 
//		ResultSet dblp_cjs = statement.executeQuery("SELECT cid, name, full_name FROM dblp.conference"
//			+ " WHERE cid NOT IN (SELECT dblp_id FROM dblp.map_conference) AND cid NOT IN (SELECT dblp_cid FROM dblp.map_cj); "); 
		ResultSet dblp_cjs = statement.executeQuery("SELECT jid, name, full_name FROM dblp.journal"
			+ " WHERE jid NOT IN (SELECT dblp_id FROM dblp.map_journal) AND jid NOT IN (SELECT dblp_jid FROM dblp.map_jc); "); 
		while(dblp_cjs.next())
		{
			int cid = dblp_cjs.getInt(1); 
			String name = dblp_cjs.getString(2); 
			String name_full = dblp_cjs.getString(3); 
			
			ConfJournal conference = new ConfJournal(cid, name, name_full); 
			DCs.add(conference); 
			DCH.put(cid, conference); 
		}
		
		System.out.println("1.");
//		ResultSet dblp_paper = statementUpdate.executeQuery("SELECT cid, title FROM dblp.publication WHERE cid > 0 AND year > 1990 AND year < 2012; ");
		ResultSet dblp_paper = statementUpdate.executeQuery("SELECT jid, title FROM dblp.publication WHERE jid > 0 AND year > 1990 AND year < 2012; ");
		System.out.println("1.1");
		int count = 0; 
		while(dblp_paper.next())
		{
			if(DCH.containsKey(dblp_paper.getInt(1)))
			{
				DCH.get(dblp_paper.getInt(1)).papers_title += NaviFunctions.stringNormalization(dblp_paper.getString(2).toLowerCase()) + " "; 
				if(count % 10000 == 0)
				{
					System.out.println(count);
				}
				count++; 
			}
		}
		System.out.println("2.");
		for(int i = 0; i < DCs.size(); i++)
		{
			DCs.get(i).buildMinArray(Q, arraySize, randomNumbers); 
			if(i % 100 == 0)
			{
				System.out.println(i);
			}
		}
		System.out.println("3.");
		
		// read the data from microsoft; 
//		ResultSet microsoft_cjs = statement.executeQuery("SELECT cid, name, full_name FROM mas.conference"
//			+ " WHERE cid NOT IN (SELECT mas_id FROM dblp.map_conference) AND cid NOT IN (SELECT mas_cid FROM dblp.map_jc); "); 
		ResultSet microsoft_cjs = statement.executeQuery("SELECT jid, name, full_name FROM mas.journal"
			+ " WHERE jid NOT IN (SELECT mas_id FROM dblp.map_journal) AND jid NOT IN (SELECT mas_jid FROM dblp.map_cj); "); 
		while(microsoft_cjs.next())
		{
			int cid = microsoft_cjs.getInt(1); 
			String name = microsoft_cjs.getString(2); 
			String name_full = microsoft_cjs.getString(3); 
			
			ConfJournal conference = new ConfJournal(cid, name, name_full); 
			MCs.add(conference); 
			MCH.put(cid, conference); 
		}
		System.out.println("4.");
//		ResultSet microsoft_paper = statementUpdate.executeQuery("SELECT cid, title FROM mas.publication WHERE cid > 0 AND year > 1990 AND year < 2012; ");
		ResultSet microsoft_paper = statementUpdate.executeQuery("SELECT jid, title FROM mas.publication WHERE jid > 0 AND year > 1990 AND year < 2012; ");
		while(microsoft_paper.next())
		{
			if(MCH.containsKey(microsoft_paper.getInt(1)))
			{
				MCH.get(microsoft_paper.getInt(1)).papers_title += NaviFunctions.stringNormalization(microsoft_paper.getString(2).toLowerCase()) + " "; 
				if(count % 10000 == 0)
				{
					System.out.println(count);
				}
				count++; 
			}
		}
		System.out.println("5.");
		for(int i = 0; i < MCs.size(); i++)
		{
			MCs.get(i).buildMinArray(Q, arraySize, randomNumbers); 
			if(i % 100 == 0)
			{
				System.out.println(i);
			}
		}
		System.out.println("6.");

		// output the high similar pairs; 
		for(int i = 0; i < DCs.size(); i++)
		{
			double max_sim = 0; 
			int max_id = 0; 
			for(int j = 0; j < MCs.size(); j++)
			{
				double similarity = DCs.get(i).similarity(MCs.get(j)); 
				if(similarity > max_sim)
				{
					max_sim = similarity; 
					max_id = j; 
				}
			}
			if(max_sim > SIMILARITY_THRESHOLD)
			{
//				String command = "INSERT INTO dblp.map_conference VALUES (" + DCs.get(i).id + ", " + MCs.get(max_id).id + ", 0); "; 
				String command = "INSERT INTO dblp.map_journal VALUES (" + DCs.get(i).id + ", " + MCs.get(max_id).id + ", 0); "; 
				statement.executeUpdate(command); 
				System.out.println(command); 
				
				System.out.println("A: " + DCs.get(i).name + " - " + DCs.get(i).name_full);
				System.out.println("B: " + MCs.get(max_id).name + " - " + MCs.get(max_id).name_full);
			}
		}
	}

	// Step 5. map papers by their conferences and journals; 
	public static void MapPaperThroughCJ(Statement statement, Statement statementUpdate) throws SQLException
	{
		ResultSet results = statement.executeQuery("SELECT dblp_id, mas_id FROM dblp.map_conference WHERE dblp_id > 1630; "); 
//		ResultSet results = statement.executeQuery("SELECT dblp_id, mas_id FROM dblp.map_journal WHERE dblp_id > 35; "); 
//		ResultSet results = statement.executeQuery("SELECT dblp_cid, mas_jid FROM dblp.map_cj; "); 
//		ResultSet results = statement.executeQuery("SELECT dblp_jid, mas_cid FROM dblp.map_jc; "); 
		while(results.next())
		{
			ArrayList<String> commands = new ArrayList<String>(); 
			
			int dblp_cid = results.getInt(1); 
			int microsoft_cid = results.getInt(2); 
			ArrayList<Paper> dblp_conference_papers = new ArrayList<Paper>(); 
			ArrayList<Paper> microsoft_conference_papers = new ArrayList<Paper>(); 
			
			ResultSet CJpapers = statementUpdate.executeQuery("SELECT pid, title, year FROM dblp.publication WHERE year > 1950 AND year < 2012 AND cid = " + dblp_cid + "; "); 
//			ResultSet CJpapers = statementUpdate.executeQuery("SELECT pid, title, year FROM dblp.publication WHERE year > 1950 AND year < 2012 AND jid = " + dblp_cid + "; "); 
//			ResultSet CJpapers = statementUpdate.executeQuery("SELECT pid, title, year FROM dblp.publication WHERE year > 1950 AND year < 2012 AND cid = " + dblp_cid + "; "); 
//			ResultSet CJpapers = statementUpdate.executeQuery("SELECT pid, title, year FROM dblp.publication WHERE year > 1950 AND year < 2012 AND jid = " + dblp_cid + "; "); 
			while(CJpapers.next())
			{
				int pid = CJpapers.getInt(1); 
				Paper paper = new Paper(pid, NaviFunctions.stringNormalization(CJpapers.getString(2)), CJpapers.getInt(3)); 
				dblp_conference_papers.add(paper); 
			}
			
			CJpapers = statementUpdate.executeQuery("SELECT pid, title, year, cid FROM mas.publication WHERE year > 1950 AND year < 2012 AND cid = " + microsoft_cid + "; "); 
//			CJpapers = statementUpdate.executeQuery("SELECT pid, title, year, cid FROM mas.publication WHERE year > 1950 AND year < 2012 AND jid = " + microsoft_cid + "; "); 
//			CJpapers = statementUpdate.executeQuery("SELECT pid, title, year, cid FROM mas.publication WHERE year > 1950 AND year < 2012 AND jid = " + microsoft_cid + "; "); 
//			CJpapers = statementUpdate.executeQuery("SELECT pid, title, year, cid FROM mas.publication WHERE year > 1950 AND year < 2012 AND cid = " + microsoft_cid + "; "); 
			while(CJpapers.next())
			{
				int pid = CJpapers.getInt(1); 
				Paper paper = new Paper(pid, NaviFunctions.stringNormalization(CJpapers.getString(2)), CJpapers.getInt(3)); 
				microsoft_conference_papers.add(paper); 
			}
			
			for(int j = 0; j < dblp_conference_papers.size(); j++)
			{
				double max_sim = 0; 
				int max_pos = 0; 
				for(int k = 0; k < microsoft_conference_papers.size(); k++)
				{
					double cur_sim = dblp_conference_papers.get(j).similarity(microsoft_conference_papers.get(k)); 
					if(cur_sim > max_sim)
					{
						max_sim = cur_sim; 
						max_pos = k; 
					}
				}
				if(max_sim >= 0.7)
				{
					String command = "INSERT dblp.map_paper VALUES (" + dblp_conference_papers.get(j).pid + ", " + microsoft_conference_papers.get(max_pos).pid 
						+ ", \"" + dblp_conference_papers.get(j).title + "\", \"" + microsoft_conference_papers.get(max_pos).title + "\", " + max_sim + ")"; 
					commands.add(command); 
				}
			}
			
			for(int j = 0; j < commands.size(); j++)
			{
				try
				{
					statementUpdate.executeUpdate(commands.get(j)); 
				}
				catch(Exception e)
				{
					continue; 
				}
				System.out.println(dblp_cid + " " + commands.get(j));
			}
		}
	}

	// Step 6. refine the approximate mappings; 
	public static void RefinePaperMapping(Statement statement, Statement statementUpdate) throws SQLException
	{
		ResultSet rs = statement.executeQuery("SELECT * FROM dblp.map_paper WHERE similarity < 1; "); 
		while(rs.next())
		{
			int dblp_id = rs.getInt(1); 
			int mas_id = rs.getInt(2); 
			String dblp_title = rs.getString(3); 
			String mas_title = rs.getString(4); 
			
			double similarity = SimilarityFunctions.editDistanceSim(dblp_title, mas_title); 
			statementUpdate.executeUpdate("UPDATE dblp.map_paper SET similarity = " + similarity + " WHERE dblp_id = " + dblp_id + " AND mas_id = " + mas_id + "; "); 
		}
	}
	
	// Step 7. map authors based on their papers; 
	public static void MapAuthorBasedOnPaper(Statement statement, Statement statementUpdate) throws SQLException
	{
		String queryCommand = "SELECT dblp.writes.aid, mas.writes.aid, count(*) num FROM dblp.map_paper, dblp.writes, mas.writes "
			+ " WHERE dblp.map_paper.dblp_id = dblp.writes.pid AND dblp.map_paper.mas_id = mas.writes.pid GROUP BY dblp.writes.aid, mas.writes.aid ORDER BY dblp.writes.aid ASC, num DESC;"; 
		ResultSet rs = statement.executeQuery(queryCommand); 
		
		int pre_id = 0; 
		int pre_num = 0; 
		for(int i = 0; rs.next(); i++)
		{
			if(i % 10000 == 0)
			{
				System.out.println(i);
			}
			
			int dblp_aid = rs.getInt(1); 
			int mas_aid = rs.getInt(2); 
			int num = rs.getInt(3); 
			double similarity = 0; 
						
			if(dblp_aid == pre_id && num < pre_num)
			{
				continue; 
			}
			else if(dblp_aid != pre_id)
			{
				pre_id = dblp_aid; 
				pre_num = num; 
			}
			
			String dblp_name = ""; 
			ResultSet dblp_rs = statementUpdate.executeQuery("SELECT name FROM dblp.author WHERE aid = " + dblp_aid); 
			if(dblp_rs.next())
			{
				dblp_name = dblp_rs.getString(1); 
			}
			dblp_rs.close();
			
			String mas_name = ""; 
			ResultSet mas_rs = statementUpdate.executeQuery("SELECT name FROM mas.author WHERE aid = " + mas_aid); 
			if(mas_rs.next())
			{
				mas_name = mas_rs.getString(1); 
			}
			mas_rs.close();

			if(!dblp_name.isEmpty() && !mas_name.isEmpty())
			{
				similarity = SimilarityFunctions.editDistanceSim(dblp_name, mas_name); 
			}
					
			String insert = "INSERT INTO dblp.map_author VALUES (" + dblp_aid + ", " + mas_aid + ", \"" + dblp_name + "\", \"" + mas_name + "\", " + num + ", " + similarity + "); ";
			try
			{
				statementUpdate.executeUpdate(insert); 
			}
			catch(Exception e) {}
		}
		rs.close();
	}

	// Step 8. recompute sim for authors; 
	public static void ReComputeAuthorSim(Statement statement, Statement statementUpdate) throws SQLException
	{
		String command = "SELECT * FROM dblp.map_author;"; 
		ResultSet rs = statement.executeQuery(command); 
		for(int i = 0; rs.next(); i++)
		{
			if(i % 10000 == 0)
			{
				System.out.println(i);
			}
			int dblp_id = rs.getInt(1); 
			int mas_id = rs.getInt(2); 
			String dblp_name = rs.getString(3); 
			String mas_name = rs.getString(4); 
			
			double sim = 0; 
			sim = NaviFunctions.jaccardToken(NaviFunctions.stringNormalization(dblp_name), NaviFunctions.stringNormalization(mas_name)); 
			String update = "UPDATE dblp.map_author SET pq_sim = " + sim + " WHERE dblp_id = " + dblp_id + " AND mas_id = " + mas_id;
			if(update.contains("NaN"))
			{
				update = update.replaceAll(" NaN ", " 0 "); 
			}
			statementUpdate.executeUpdate(update); 
		}
		rs.close();
	}

	// Step 9. refine candidate mappings for authors; 
	public static void RefineAuthorMappings(Statement statement, Statement statementUpdate) throws SQLException
	{
		ArrayList<Integer> deleted_dblp = new ArrayList<Integer>(); 
		ArrayList<Integer> deleted_mas = new ArrayList<Integer>(); 
//		String command = "SELECT * FROM dblp.map_author ORDER BY dblp_id ASC, 2*similarity+pq_sim DESC;"; 
		String command = "SELECT * FROM dblp.map_author ORDER BY mas_id ASC, 2*similarity+pq_sim DESC;"; 
		ResultSet rs = statement.executeQuery(command); 

		int pre_id = 0; 
		double pre_sim = 0; 
		
		while(rs.next())
		{
			int dblp_id = rs.getInt(1); 
			int mas_id = rs.getInt(2); 
			double sim = rs.getDouble(6)*2 + rs.getDouble(7); 
			
//			if(dblp_id != pre_id)
//			{
//				pre_id = dblp_id; 
//				pre_sim = sim; 
//			}
			if(mas_id != pre_id)
			{
				pre_id = mas_id; 
				pre_sim = sim; 
			}
			else if(sim == pre_sim)
			{
				continue; 
			}
			else if(sim < pre_sim)
			{
				deleted_dblp.add(dblp_id); 
				deleted_mas.add(mas_id); 
			}
		}
		rs.close();
		
		for(int i = 0; i < deleted_dblp.size(); i++)
		{
			String command_delete = "DELETE FROM dblp.map_author WHERE dblp_id = " + deleted_dblp.get(i) + " AND mas_id = " + deleted_mas.get(i); 
			statementUpdate.executeUpdate(command_delete); 
		}		
	}
}
