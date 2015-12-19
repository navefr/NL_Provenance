package dataViewGeneration;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;

public class TupleGraph 
{
	static float C = (float) 0.7; 
	public Hashtable<String, Tuple> graph = new Hashtable<String, Tuple>(); 
	
	public void buildGraph(Statement statement) throws Exception
	{
		String query = "SELECT kid FROM keyword"; 
		ResultSet result = statement.executeQuery(query); 
		while(result.next())
		{
			int id = result.getInt(1); 
			String key = "k" + id; 
			Tuple tuple = new Tuple(); 
			graph.put(key, tuple); 
		}
		System.out.println("keyword: " + graph.size()); 
		
		query = "SELECT oid FROM organization"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id = result.getInt(1); 
			String key = "o" + id; 
			Tuple tuple = new Tuple(); 
			graph.put(key, tuple); 
		}
		System.out.println("organization: " + graph.size()); 

		query = "SELECT cid FROM conference"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id = result.getInt(1); 
			String key = "c" + id; 
			Tuple tuple = new Tuple(); 
			graph.put(key, tuple); 
		}
		System.out.println("conference: " + graph.size()); 

		query = "SELECT jid FROM journal"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id = result.getInt(1); 
			String key = "j" + id; 
			Tuple tuple = new Tuple(); 
			graph.put(key, tuple); 
		}
		System.out.println("journal: " + graph.size()); 

		query = "SELECT did FROM domain"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id = result.getInt(1); 
			String key = "d" + id; 
			Tuple tuple = new Tuple(); 
			graph.put(key, tuple); 
		}
		System.out.println("domain: " + graph.size()); 

		query = "SELECT cid, did FROM domain_conference"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id1 = result.getInt(1); 
			Tuple tuple1 = graph.get("c" + id1); 
			int id2 = result.getInt(2); 
			Tuple tuple2 = graph.get("d" + id2); 
			if(tuple1 != null && tuple2 != null)
			{
				tuple1.neighbors.add(tuple2); 
				tuple2.neighbors.add(tuple1); 
			}
		}
		System.out.println("domain_conference: " + graph.size()); 
		
		query = "SELECT jid, did FROM domain_journal"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id1 = result.getInt(1); 
			Tuple tuple1 = graph.get("j" + id1); 
			int id2 = result.getInt(2); 
			Tuple tuple2 = graph.get("d" + id2); 
			if(tuple1 != null && tuple2 != null)
			{
				tuple1.neighbors.add(tuple2); 
				tuple2.neighbors.add(tuple1); 
			}
		}
		System.out.println("domain_journal: " + graph.size()); 
		
		query = "SELECT kid, did FROM domain_keyword"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id1 = result.getInt(1); 
			Tuple tuple1 = graph.get("k" + id1); 
			int id2 = result.getInt(2); 
			Tuple tuple2 = graph.get("d" + id2); 
			if(tuple1 != null && tuple2 != null)
			{
				tuple1.neighbors.add(tuple2); 
				tuple2.neighbors.add(tuple1); 
			}
		}
		System.out.println("domain_keyword: " + graph.size()); 

		query = "SELECT aid, oid FROM author"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id1 = result.getInt(1); 
			String key = "a" + id1; 
			Tuple tuple = new Tuple(); 
			int id2 = result.getInt(2); 
			if(id2 > 0)
			{
				Tuple organization = graph.get("o" + id2); 
				if(organization != null)
				{
					tuple.neighbors.add(organization); 
					organization.neighbors.add(tuple); 
				}
			}
			graph.put(key, tuple); 
		}
		System.out.println("author: " + graph.size()); 

		query = "SELECT aid, did FROM domain_author"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id1 = result.getInt(1); 
			String key = "a" + id1; 
			Tuple tuple = new Tuple(); 
			int id2 = result.getInt(2); 
			if(id2 > 0)
			{
				Tuple domain = graph.get("d" + id2); 
				if(domain != null)
				{
					tuple.neighbors.add(domain); 
					domain.neighbors.add(tuple); 
				}
			}
			graph.put(key, tuple); 
		}
		System.out.println("domain_author: " + graph.size()); 

		query = "SELECT pid, cid, jid FROM publication"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int pid = result.getInt(1); 
			String key = "p" + pid; 
			Tuple paper = new Tuple(); 
			int cid = result.getInt(2); 
			if(cid > 0)
			{
				Tuple conference = graph.get("c" + cid); 
				if(conference != null)
				{
					paper.neighbors.add(conference); 	
					conference.neighbors.add(paper); 
				}
			}
			int jid = result.getInt(3); 
			if(jid > 0)
			{
				Tuple journal = graph.get("j" + jid); 
				if(journal != null)
				{
					paper.neighbors.add(journal); 	
					journal.neighbors.add(paper);
				} 
			}
			graph.put(key, paper); 
		}
		System.out.println("publication: " + graph.size()); 
	
		query = "SELECT pid, kid FROM publication_keyword"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id1 = result.getInt(1); 
			Tuple tuple1 = graph.get("p" + id1); 
			int id2 = result.getInt(2); 
			Tuple tuple2 = graph.get("k" + id2); 
			if(tuple1 != null && tuple2 != null)
			{
				tuple1.neighbors.add(tuple2); 
				tuple2.neighbors.add(tuple1); 
			}
		}
		System.out.println("publication_keyword: " + graph.size()); 

		query = "SELECT aid, pid FROM writes"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id1 = result.getInt(1); 
			Tuple tuple1 = graph.get("a" + id1); 
			int id2 = result.getInt(2); 
			Tuple tuple2 = graph.get("p" + id2); 
			if(tuple1 != null && tuple2 != null)
			{
//				tuple1.neighbors.add(tuple2); 
				tuple2.neighbors.add(tuple1); 
			}
		}
		System.out.println("writes: " + graph.size()); 
		
		query = "SELECT citing, cited FROM cite"; 
		result = statement.executeQuery(query); 
		while(result.next())
		{
			int id1 = result.getInt(1); 
			Tuple tuple1 = graph.get("p" + id1); 
			int id2 = result.getInt(2); 
			Tuple tuple2 = graph.get("p" + id2); 
			if(tuple1 != null && tuple2 != null)
			{
				tuple1.neighbors.add(tuple2); 
			}
		}
		System.out.println("cite: " + graph.size()); 
	}

	public void weightUpdate(Statement statement) throws Exception
	{
		String query = "SELECT kid FROM keyword"; 
		weightUpdate(statement, query); 
		
		query = "SELECT oid FROM organization"; 
		weightUpdate(statement, query); 

		query = "SELECT cid FROM conference"; 
		weightUpdate(statement, query); 

		query = "SELECT jid FROM journal"; 
		weightUpdate(statement, query); 

		query = "SELECT did FROM domain"; 
		weightUpdate(statement, query); 

		query = "SELECT aid FROM author"; 
		weightUpdate(statement, query); 

		query = "SELECT pid FROM publication"; 
		weightUpdate(statement, query); 
		
		query = "SELECT kid FROM keyword"; 
		weightRound(statement, query); 
		
		query = "SELECT oid FROM organization"; 
		weightRound(statement, query); 

		query = "SELECT cid FROM conference"; 
		weightRound(statement, query); 

		query = "SELECT jid FROM journal"; 
		weightRound(statement, query); 

		query = "SELECT did FROM domain"; 
		weightRound(statement, query); 

		query = "SELECT aid FROM author"; 
		weightRound(statement, query); 

		query = "SELECT pid FROM publication"; 
		weightRound(statement, query); 
	}
	
	public void weightUpdate(Statement statement, String query) throws Exception
	{
		ResultSet result = statement.executeQuery(query); 
		while(result.next())
		{
			int id = result.getInt(1); 
			String key = "" + query.split(" ")[3].charAt(0); 
			key += id; 
			Tuple tuple = graph.get(key); 
			if(tuple != null)
			{
				float adding = C * tuple.weight/(tuple.neighbors.size()+10); 
				for(Tuple neighbor : tuple.neighbors)
				{
					neighbor.nextWeight += adding; 
				}
			}
		}
		System.out.println(query.split(" ")[3] + " done!"); 
	}

	public void weightRound(Statement statement, String query) throws Exception
	{
		ResultSet result = statement.executeQuery(query); 
		while(result.next())
		{
			int id = result.getInt(1); 
			String key = "" + query.split(" ")[3].charAt(0); 
			key += id; 
			Tuple tuple = graph.get(key); 
			if(tuple != null)
			{
				tuple.weight = tuple.nextWeight + 1000 * (1-C); 
				tuple.nextWeight = 0; 
			}
		}
		System.out.println(query.split(" ")[3] + " done!"); 
	}

	public void writeDatabase(Statement statement, Statement statementUpdate) throws Exception
	{
		String query = "SELECT kid FROM keyword"; 
		writeRelation(statement, statementUpdate, query); 
		System.out.println(query + " Done!!!!"); 
		
		query = "SELECT oid FROM organization"; 
		writeRelation(statement, statementUpdate, query); 
		System.out.println(query + " Done!!!!"); 

		query = "SELECT cid FROM conference"; 
		writeRelation(statement, statementUpdate, query); 
		System.out.println(query + " Done!!!!"); 

		query = "SELECT jid FROM journal"; 
		writeRelation(statement, statementUpdate, query); 
		System.out.println(query + " Done!!!!"); 

		query = "SELECT did FROM domain"; 
		writeRelation(statement, statementUpdate, query); 
		System.out.println(query + " Done!!!!"); 

		query = "SELECT aid FROM author"; 
		writeRelation(statement, statementUpdate, query); 
		System.out.println(query + " Done!!!!"); 

		query = "SELECT pid FROM publication"; 
		writeRelation(statement, statementUpdate, query); 
		System.out.println(query + " Done!!!!"); 
	}
	
	public void writeRelation(Statement statement, Statement statementUpdate, String query) throws Exception
	{
		String delete = "UPDATE " + query.split(" ")[3] + " SET importance = 0"; 
		statementUpdate.executeUpdate(delete); 
		
		ResultSet result = statement.executeQuery(query); 
		while(result.next())
		{
			int id = result.getInt(1); 
			String key = "" + query.split(" ")[3].charAt(0); 
			key += id; 
			Tuple tuple = graph.get(key); 
			if(tuple != null)
			{
				String update = "UPDATE " + query.split(" ")[3] + " SET importance = " + (int) (Math.log(tuple.weight+1)/Math.log(2)) + " WHERE " + query.split(" ")[1] + " = " + id; 
				statementUpdate.executeUpdate(update); 
			}
		}
	}
}