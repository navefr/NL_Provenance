package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Reset 
{
	
	/*************************************************************************************************************/
	/** Title: deleteProgramFromFile																				
	/** Description: Deletes the program from the demo file.
	/*************************************************************************************************************/
	
	static void deleteProgramFromFile (String fileName)
	{
		File inputFile = new File(fileName + ".iris");
		File tempFile = new File("tempFile.iris");

		BufferedReader reader = null;
		BufferedWriter writer = null;
		
		try 
		{
			reader = new BufferedReader(new FileReader(inputFile));
			writer = new BufferedWriter(new FileWriter(tempFile));

			String marker = ":-";
			String currentLine;

			while((currentLine = reader.readLine()) != null) 
			{
				// trim newline when comparing with lineToRemove
				String trimmedLine = currentLine.trim();
				if(trimmedLine.contains(marker))
				{
					continue;
				}
				
				writer.write(currentLine + System.getProperty("line.separator"));
			}
		}
		
		catch (FileNotFoundException e) 
		{
			System.out.println("Reset::deleteProgramFromFile:: Could not find file: " + fileName + ".iris");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		
		finally 
		{
			try
			{
				writer.close(); 
				reader.close();
				writer = null;
				reader = null;
				System.gc();
				if (false == inputFile.delete())
				{
			        System.out.println("Could not delete file");
				}
				
				if (false == tempFile.renameTo(inputFile)) 
				{
			        System.out.println("Could not rename file");
				}
			}
			catch (IOException e) 
			{
				System.out.println("Reset::deleteProgramFromFile:: Could not close file");
			}
		}
	}
	
}
