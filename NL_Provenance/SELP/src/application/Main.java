package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.stage.Stage;


public class Main extends Application  
{
	@Override
	public void start(Stage primaryStage) throws IOException 
	{
		try 
		{
			ScreensFramework app = new ScreensFramework();
			app.start(primaryStage);
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
	
}
