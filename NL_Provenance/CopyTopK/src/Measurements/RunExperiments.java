package Measurements;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import Basics.KeyMap;
import Basics.MemDB;
import Basics.Provenance;
import Basics.Rule;
import au.com.bytecode.opencsv.CSVWriter;

public class RunExperiments 
{
	public static void main (String [] args) throws IOException
	{
		FileWriter fw_online = new FileWriter("meas041114_onlineTop1.csv");
		CSVWriter writer_online = new CSVWriter(fw_online);
		ArrayList<String[]> data = new ArrayList<String[]>();
		data.add(new String[] {"Size of initial DB", "Size of original program", "Size of pattern", "Number of rules added in intersection", 
				"Duration for intersection in seconds", "Size of prov. After intersection", "Size of DB After Iteration", "Time for topk"});
		writer_online.writeAll(data);
		writer_online.flush();
		
		
		FileWriter fw_offline = new FileWriter("meas041114_seminaive.csv");
		CSVWriter writer_offline = new CSVWriter(fw_offline);
		ArrayList<String[]> data2 = new ArrayList<String[]>();
		data2.add(new String[] {"Size of initial DB", "k",  "Duration of prov. generation in milliseconds",
				"Duration of intersection in seconds", "Size of full provenance", "Size of intersected provenance", "Size of full circuit", "size Of intersected circuit", "Size of DB After Iteration", "Time For Top-k"});
		
		writer_offline.writeAll(data);
		writer_offline.flush();
		
		for (int i = 1; i < 12; i++) 
		{	
			System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("FULL PROVENANCE MEASUREMENTS");
			System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
			OfflineMeasurements offline = new OfflineMeasurements();
			offline.MeasureAndWriteCSV("FullProv_070315.csv", i, writer_offline);
			
			try 
			{
				Thread.sleep(4000);
				MemDB.getInstance().Reset();
				Provenance.getInstance().Reset();
				KeyMap.getInstance().Reset();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
			/*System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("ONLINE TOP-1 MEASUREMENTS");
			System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
			OnlineMeasurements online = new OnlineMeasurements();
			online.MeasureAndWriteCSV("meas021114_onlineTop1.csv", i, writer_online);*/

		}
		
		//writer_online.close();
		writer_offline.close();
	}
	
	
	private static boolean dwRule (Rule rule)
	{
		boolean b = false;
		String hStr = rule.getHead().getName();
		String rStr = rule.toString();
		String [] legalRules = new String [] {
				"dealsWith(?a,?b) :- dealsWith(?a,?f), dealsWith(?f,?b).",
				"dealsWith(?a,?b) :- dealsWith(?b,?a).",
				"dealsWith(?a,?b) :- imports(?a,?c), exports(?b,?c)."};
		
		if (hStr.equals("dealsWith") && Arrays.asList(legalRules).contains(rStr))
			b = true;
		
		return b;
	}
}
