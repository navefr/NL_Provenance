package Basics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.mysql.jdbc.DatabaseMetaData;

public class DB 
{
	private static DB db = null;
	
	Connection conn = null;
	
	Statement stmt = null;
	
	public Map<Triplet, String> categoryMap = new HashMap<Triplet, String>(); 
	
	public Vector<String> tableNames = new Vector<String>();
	
	
	private DB()
	{
		try 
		{
			//this.conn = DriverManager.getConnection("jdbc:mysql://mysqlsrv.cs.tau.ac.il/amirgilad?user=amirgilad&password=am51378");
			this.conn = DriverManager.getConnection("jdbc:mysql://localhost/selP?user=root");
		    this.stmt = this.conn.createStatement();
		} 
		catch (SQLException ex) 
		{
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public static DB getInstance() 
	{
		if(db == null) 
		{
			db = new DB();
		}
		return db;
	}
	
	
	public Connection getConn() 
	{
		return conn;
	}
	
	
	public Statement getStmt() 
	{
		return stmt;
	}
	
	
	/*************************************************************************************************************/
	/** Title: Reset																				
	/** Description: Resets the tableNames and categoryMap collections			
	/*************************************************************************************************************/
	
	public void Reset ()
	{
		/*DropTables();
		this.tableNames.clear();
		this.categoryMap.clear();*/
		try
		{
			Thread.sleep(1000);
		}
		catch (Exception e)
		{
			
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: DropTables																				
	/** Description: drops the entire DB			
	/*************************************************************************************************************/
	
	public void DropTables ()
	{
		for (String tableName : tableNames) 
		{
			DropTableIfExists(tableName);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: CreateDB																				
	/** Description: Creates new DB			
	/*************************************************************************************************************/
	
	public void CreateDB()
	{
		String sql = "CREATE DATABASE selP";
		try 
		{
			ResultSet resultSet = conn.getMetaData().getCatalogs();
			while (resultSet.next()) 
			{
				if (resultSet.getString(1).equals("selp"))
				{
					return;
				}
			}
			this.conn = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "");
			this.stmt = this.conn.createStatement();
			stmt.executeUpdate(sql);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: Update																				
	/** Description: add new fact to DB			
	/*************************************************************************************************************/
	
	public void Update (Atom atom)
	{
		CreateTableIfNotExist(atom.getName(), atom.getParams().elementAt(0).getCategory(), 
				atom.getParams().elementAt(1).getCategory());
		if (false == ContainedInTable(atom)) 
		{
			atom.setStable(true);
			atom.isFullyInst();
			List<String> vals = new ArrayList<String>(); 
			for (Proton param : atom.getParams()) 
			{
				vals.add(param.getName());
			}
			
			InsertToTable(atom.getName(), vals);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetAllConstantsInCategory																				
	/** Description: Gets all the atoms in the DB by proton category			
	/*************************************************************************************************************/
	
	public Vector<Proton> GetAllConstantsInCategory (Proton p)
	{
		Vector<Proton> relevants = new Vector<Proton>();
		List<String> DbCategories = GetDbCategoriesByProgCategory(p.getCategory());
		String query = "";
		try 
		{
			for (String tableName : this.tableNames) 
			{
				for (String cat : DbCategories) 
				{
					DatabaseMetaData md = (DatabaseMetaData) conn.getMetaData();
					ResultSet columnChecker = md.getColumns(null, null, tableName, cat);
					 if (columnChecker.next())
					{
						query = "SELECT " + cat + " FROM " + tableName;
						ResultSet rs = db.stmt.executeQuery(query);
						while (rs.next()) 
						{
							Proton relevant = new Constant (rs.getString(1), p.getCategory());
							if (false == relevants.contains(relevant)) 
							{
								relevants.add(relevant);
							}
						}
					}
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return relevants;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetDbCategoriesByProgCategory																				
	/** Description: Gets all the DB Categories that are mapped to the given program category			
	/*************************************************************************************************************/
	
	public List<String> GetDbCategoriesByProgCategory (String cat)
	{
		List<String> dbCategories = new ArrayList<String>();
		for (Triplet key : this.categoryMap.keySet()) 
		{
			if (key.getProgramCategory().equals(cat)) 
			{
				dbCategories.add(this.categoryMap.get(key));
			}
		}
		
		return dbCategories;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetProgramCategoryByDBCategoryAndTableName																				
	/** Description: Gets all the DB Categories that are mapped to the given IdxAnd + TableName			
	/*************************************************************************************************************/
	
	public String GetProgramCategoryByDBCategoryAndTableName (String tableName, String dbCategory)
	{
		String retVal = "";
		for (Triplet key : this.categoryMap.keySet()) 
		{
			if (this.categoryMap.get(key).equals(dbCategory) && key.getTableName().equals(tableName)) 
			{
				retVal = key.getProgramCategory();
			}
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetProgramCategoryByDBCategoryAndTableName																				
	/** Description: Gets all the DB Categories that are mapped to the given IdxAnd + TableName			
	/*************************************************************************************************************/
	
	public List<String> GetDBCategoryByProgramCategoryAndTableName (String tableName, String progCategory)
	{
		List<String> dbCategories = new ArrayList<String>();
		for (Triplet key : this.categoryMap.keySet()) 
		{
			if (key.getProgramCategory().equals(progCategory) && key.getTableName().equals(tableName)) 
			{
				dbCategories.add(this.categoryMap.get(key));
			}
		}
		
		return dbCategories;
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: GetProgramCategoryByDBCategoryAndTableName																				
	/** Description: Gets all the DB Categories that are mapped to the given IdxAnd + TableName			
	/*************************************************************************************************************/
	
	public String GetDBCategoryByProgramCategoryAndIdx (String tableName, int idx)
	{
		String retVal = "";
		for (Triplet key : this.categoryMap.keySet()) 
		{
			if (key.getIdx() == idx && key.getTableName().equals(tableName)) 
			{
				retVal = key.getProgramCategory();
			}
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: RetrieveProgramCategoryByParamIndex																				
	/** Description: Retrieves the category of the param By it's Index and the atoms name. used in parsing 			
	/*************************************************************************************************************/
	
	public String RetrieveProgramCategoryByParamIndex (String tableName, int idx)
	{
		String name = "";
		String query = "";
		try 
		{
			query = "SELECT * FROM " + tableName;
			ResultSet rs = db.stmt.executeQuery(query);
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			name = GetProgramCategoryByDBCategoryAndTableName(tableName, rsmd.getColumnName(idx));
		} 
		catch (SQLException e) 
		{
			System.out.println(query);
			name = "Country";//"Undefined";
		}
		
		return name;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: InsertToTable																				
	/** Description: inserts values to table in DB			
	/*************************************************************************************************************/

	public void InsertToTable (String tableName, List<String> values)
	{
		String vals = " VALUES (";
		for (String string : values) 
		{
			vals += "'" + string + "'" + ", ";
		}
		
		vals = vals.substring(0, vals.length()-2) + ")";
		String sql = "INSERT INTO " + tableName + vals;
		
		try 
		{
			db.stmt.execute(sql);
		} 
		catch (SQLException e) 
		{
			//e.printStackTrace();
			//System.out.println("Error with query: " + sql);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: SelectTable																				
	/** Description: Returns an iterator to go through all the requested table			
	/*************************************************************************************************************/
	
	public ResultSet SelectTable (String tableName)
	{
		try 
		{
			return db.stmt.executeQuery("SELECT * FROM " + tableName);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: ContainedInTable																				
	/** Description: Checks if atom is contained in the DB			
	/*************************************************************************************************************/
	
	public boolean ContainedInTable (Atom atom)
	{
		boolean retVal = false;
		if (true == tableNames.contains(atom.getName())) 
		{
			String params = "";
			for (int i = 0; i < atom.getParams().size(); i++)
			{
				String dbCat = this.categoryMap.get(new Triplet(i, atom.getParams().elementAt(i).getCategory(), atom.getName()));
				params += " " + dbCat + " = '" + atom.getParams().elementAt(i).getName() + "' and";
			}
			try 
			{
				params = params.substring(0, params.length()-4);
				//System.out.println("SELECT * FROM " + atom.getName() + " WHERE " + params);
				ResultSet rs = db.stmt.executeQuery("SELECT * FROM " + atom.getName() + " WHERE " + params);
				if (true == rs.next()) 
				{
					retVal = true;
					atom.setStable(true);
					atom.isFullyInst();
				}
			}
		
			catch (SQLException e) 
			{
				//e.printStackTrace();
				return retVal;
			}	
		}
		
		else
		{
			CreateTableIfNotExist (atom.getName(), atom.getParams().elementAt(0).getCategory(), 
					atom.getParams().elementAt(1).getCategory());
			retVal = false;
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: CreateTableIfNotExist																				
	/** Description: Checks if there is no table in this name in DB and if there isn't, make one			
	/*************************************************************************************************************/
	
	public void CreateTableIfNotExist (String tableName, String cat1, String cat2)
	{
		String cat1DB = cat1;
		String cat2DB = cat2;
		if (cat1.equals(cat2)) 
		{
			cat1DB = cat1 + "_a";
			cat2DB = cat2 + "_b";
		}
		
		UpdateCategoryMap(tableName, cat1, cat1DB, 0);
		UpdateCategoryMap(tableName, cat2, cat2DB, 1);
		
		String query = "CREATE TABLE IF NOT EXISTS " + tableName + 
				" ( " + cat1DB + " varchar(100), " + cat2DB + " varchar(100) )";
		try 
		{
			db.stmt.execute(query);
			if (false == this.tableNames.contains(tableName)) 
			{
				this.tableNames.add(tableName); //add to tableNames
			}
		} 
		catch (SQLException e) 
		{
			//e.printStackTrace();
			System.out.println("Error with query: " + query);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: UpdateCategoryMap																				
	/** Description: 			
	/*************************************************************************************************************/
	
	public void UpdateCategoryMap (String tableName, String cat, String catDB, int idx)
	{
		Triplet triplet = new Triplet(idx, cat, tableName);
		if (false == this.categoryMap.containsKey(triplet)) 
		{
			this.categoryMap.put(triplet, catDB);
		}
		
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetRelevantFactsFromDB																				
	/** Description: Finds all facts that are compatible with the inst. of partlyInstAtom 			
	/*************************************************************************************************************/
	
	public Vector<Atom> GetRelevantFactsFromDB (Atom partlyInstAtom, Program p)
	{
		Vector<Atom> retVec = new Vector<Atom>();
		String bindedDBCategory = "";
		String constName = "";
		CreateTableIfNotExist (partlyInstAtom.getName(), partlyInstAtom.getParams().elementAt(0).getCategory(), 
				partlyInstAtom.getParams().elementAt(1).getCategory());
		
		for (int i = 0; i < partlyInstAtom.getParams().size(); i++)
		{
			Proton param = partlyInstAtom.getParams().elementAt(i);
			if (param instanceof Constant) 
			{
				constName = param.getName();
				bindedDBCategory = this.categoryMap.get(new Triplet(i, param.getCategory(), partlyInstAtom.getName()));
			}
		}
		
		try 
		{
			ResultSet rs;
			
			String cat1 = RetrieveProgramCategoryByParamIndex(partlyInstAtom.getName(), 1);
			String cat2 = RetrieveProgramCategoryByParamIndex(partlyInstAtom.getName(), 2);
			
			if (false == bindedDBCategory.equals("")) 
			{
				rs = db.stmt.executeQuery("SELECT * " + " FROM " + partlyInstAtom.getName() + 
						" WHERE " + bindedDBCategory + " = '" + constName + "'");
			}
			else
			{
				rs = SelectTable(partlyInstAtom.getName());
			}
			
			while (rs.next()) 
			{
				Proton const1 = new Constant (rs.getString(1), cat1);
				Proton const2 = new Constant (rs.getString(2), cat2);
				Atom relevant = new Atom (partlyInstAtom.getName(), const1, const2);
				relevant.setStable(true);
				if (true == relevant.IsAtomRelationEdb(p)) 
				{
					relevant.setFact(true);
				}
				
				relevant.isFullyInst();
	            retVec.add(relevant);
			}
		}
		
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return retVec;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: DropTableIfExists																				
	/** Description: Drops the specified table if it exists 			
	/*************************************************************************************************************/

	public void DropTableIfExists (String tableName)
	{
		String dropTable = String.format("DROP TABLE IF EXISTS %s", tableName);
		try 
		{
			db.stmt.execute(dropTable);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: Size																				
	/** Description: Returns the size of the entire DB 			
	/*************************************************************************************************************/
	
	public int Size()
	{
		int size = 0;
		ResultSet rs;
		try 
		{
			for (String name : tableNames) 
			{
				String query = "SELECT COUNT(*) tables FROM " + name;
				rs = db.stmt.executeQuery(query);
				if(rs.next())
				{
					size += rs.getInt(1);  
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return size;
	}

}
