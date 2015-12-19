package application;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.facts.IFacts;
import org.deri.iris.storage.IRelation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TableDisplayer 
{
	
	/*************************************************************************************************************/
	/** Title: refreshTableView																				
	/** Description: Refreshes the tables in the input screen.
	/*************************************************************************************************************/
	
	static <T,U> void refreshTableView(String predicate, TableView<T> tableView, List<TableColumn<T,U>> columns, Map<String, List<T>> rows) 
	{
	    tableView.getColumns().clear();
	    tableView.getColumns().addAll(columns);
	    if (true == rows.containsKey(predicate)) 
	    {
	    	ObservableList<T> list = FXCollections.observableArrayList(rows.get(predicate));
		    tableView.setItems(list);
		}
	    
	    tableView.getSelectionModel().clearSelection();
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: refreshTableView																				
	/** Description: Refreshes the tables in the input screen.
	/*************************************************************************************************************/
	
	static <T,U> void refreshTableView(TableView<T> tableView, List<TableColumn<T,U>> columns, List<T> rows) 
	{        
	    tableView.getColumns().clear();
	    tableView.getColumns().addAll(columns);

	    ObservableList<T> list = FXCollections.observableArrayList(rows);
	    tableView.setItems(list);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: parseDemoFile																				
	/** Description: Parses the demo file to display on the input screen in tables.
	/*************************************************************************************************************/
	
	public static Map<String, List<String>> parseDemoFile (String fileName)//, String predicate)
	{
		Map<String, List<String>> retVal = new HashMap<String, List<String>>();
		List<String> predicates = new ArrayList<String>() {{
			add("imports"); add("exports"); add("dealsWith"); add("hasChild"); add("isMarriedTo"); add("produced"); add("directed"); add("wasBornIn"); add("isLocatedIn"); add("created"); 
			add("diedIn"); add("livesIn"); add("isInterestedIn"); add("graduatedFrom"); add("isPoliticianOf"); add("isCitizenOf"); add("worksAt");}};

		for (String string : predicates) 
		{
			retVal.put(string, new ArrayList<String>());
		}
			
		String demoFile = fileName + ".iris";
		BufferedReader br = null;
		String line = "";
		try 
		{
			br = new BufferedReader(new FileReader(demoFile));
			while ((line = br.readLine()) != null) 
			{
				String pred = line.split("\\(")[0];
				if (retVal.containsKey(pred)) 
				{
					retVal.get(pred).add( line.substring(0, line.length() - 1) );
				}
			}
		}
		
		catch (FileNotFoundException e) 
		{
			System.out.println("TableDisplayer::parseDemoFile:: Could not find file: " + demoFile);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			if (br != null) 
			{
				try 
				{
					br.close();
				} 
				catch (IOException e) 
				{
					System.out.println("TableDisplayer::parseDemoFile:: Could not close file");
				}
			}
		}
		
		System.out.println("Done Parsing " + fileName + ".iris");
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: genNewStats																				
	/** Description: Generate stats in the right format for the tables 'imports' and 'exports' in the input screen.
	/*************************************************************************************************************/
	
	static void genNewStats (Map<String, List<String>> facts, String predicate, String fileName, TableView<TradeGoods> table, TableColumn<TradeGoods, String> ... cols) throws NumberFormatException, IOException
	{
		List<TradeGoods> newRows = new ArrayList<TradeGoods>();
		
		for (String fact : facts.get(predicate)) 
		{
			String[] nameParams = fact.split("\\(\\'");
			nameParams[1] = "('" + nameParams[1];
			
			Pattern pattern = Pattern.compile("\\('([^']+)'.*'([^']+)'\\)");
			Matcher matcher = pattern.matcher(nameParams[1]);
			while (matcher.find())
			{
				newRows.add(new TradeGoods (matcher.group(1), matcher.group(2)));
			}
		}
		
		refreshTableView(table, Arrays.asList(cols), newRows);
	}
	
	
	/*************************************************************************************************************/
	/** Title: genNewStats																				
	/** Description: Generate stats in the right format for the tables 'dealsWith' in the output screen.
	/*************************************************************************************************************/
	
	static void genNewStats (IFacts facts, Map<String, List<TradeGoods>> newRows)
	{
		List<String> predicates = new ArrayList<String>() {{
			add("dealsWith"); add("hasChild"); add("isMarriedTo"); add("produced"); add("directed"); add("wasBornIn"); add("isLocatedIn"); add("created"); 
			add("diedIn"); add("livesIn"); add("isInterestedIn"); add("graduatedFrom"); add("isPoliticianOf"); add("isCitizenOf"); add("worksAt");}};
		for ( IPredicate pred : facts.getPredicates() ) 
		{
			if (predicates.contains(pred.toString()))//if (pred.toString().equals("dealsWith")) 
			{
				if (false == newRows.containsKey(pred)) 
				{
					newRows.put(pred.toString(), new ArrayList<TradeGoods>());
				}
				
				IRelation rel = facts.get(pred);
				for (int i = 0; i < rel.size(); i++) 
				{
					Pattern pattern = Pattern.compile("\\('([^']*?)'.+'([^']*?)'\\)");
					Matcher matcher = pattern.matcher(rel.get(i).toString());
					while (matcher.find()) 
					{
						newRows.get( pred.toString() ).add( new TradeGoods (matcher.group(1), matcher.group(2)) );
					}
				}
			}
		}
	}
}
