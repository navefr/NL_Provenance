package nalirTools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class BasicFunctions 
{	
	@SuppressWarnings("resource")
	public static void main(String [] args) throws IOException
	{
		String fileName = "/Users/lifei/Dropbox/workspace/CSASearch/src/zfiles/testingSet"; 
		Scanner scan = new Scanner(readFile(fileName));
		while(scan.hasNextLine())
		{
			String line = scan.nextLine(); 
			if(!line.isEmpty() && line.charAt(0) > '0' && line.charAt(0) <= '9')
			{
				line = line.substring(line.indexOf(". ") + 2); 
				line = line.replaceAll("\"", "\\\\\""); 
				line = "\tcommand = \"##2_query## " + line + "\"; "; 
				System.out.println(line);
			}
		}
	}
	
	public static String readFile(String fileName) throws IOException //read file to a String
	{
		String result = ""; 
		
        File file = new File(fileName);
        if(file.exists())
        {
        	FileInputStream fi = new FileInputStream(file);
        	InputStreamReader isr = new InputStreamReader(fi, "GBk");
        	BufferedReader bfin = new BufferedReader(isr);
        	String rLine = "";
        	while((rLine = bfin.readLine()) != null)
        	{
        		result += rLine + "\n";
        	}		
        	bfin.close(); 
        }
        else
        {
        	System.out.println("no such file");
        }     
		return result; 
	}
	
	public static void writeFile(String fileName, String result, boolean append) throws IOException
	{
		FileWriter writer = new FileWriter(fileName, append);  
        writer.write(result);  
        writer.close();  
	}
	
	public static Object depthClone(Object srcObj)
	{  
        Object cloneObj = null;  
        try {  
            ByteArrayOutputStream out = new ByteArrayOutputStream();  
            ObjectOutputStream oo = new ObjectOutputStream(out);  
            oo.writeObject(srcObj);  
              
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());  
            ObjectInputStream oi = new ObjectInputStream(in);  
            cloneObj = oi.readObject();           
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (ClassNotFoundException e) {  
            e.printStackTrace();  
        }  
        return cloneObj;  
    }  
	
	public static boolean isNumeric(String str)
	{ 
		try 
		{ 
			Integer.parseInt(str); 
			return true; 
		} 
		catch (NumberFormatException e) 
		{ 
			return false; 
		} 
	}
}
