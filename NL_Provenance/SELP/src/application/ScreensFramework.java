package application;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class ScreensFramework extends Application { 

    public static final String INPUT_SCREEN = "Input"; 
    public static final String INPUT_SCREEN_FXML = "Input.fxml"; 
    public static final String OUTPUT_SCREEN = "Output"; 
    public static final String OUTPUT_SCREEN_FXML = "Output.fxml"; 

    @Override 
    public void start(Stage primaryStage) 
    { 
      ScreensController mainContainer = new ScreensController(); 
      mainContainer.loadScreen(ScreensFramework.INPUT_SCREEN, ScreensFramework.INPUT_SCREEN_FXML); 
      mainContainer.loadScreen(ScreensFramework.OUTPUT_SCREEN,ScreensFramework.OUTPUT_SCREEN_FXML);
      mainContainer.setScreen(ScreensFramework.INPUT_SCREEN); 

      Group root = new Group(); 
      root.getChildren().addAll(mainContainer); 
      //Scene scene = new Scene(root, 990, 600);
      Scene scene = new Scene(root, 1100, 660); 
      scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
      primaryStage.setScene(scene);
      primaryStage.setTitle("SelP");
      primaryStage.getIcons().add(new Image("/SelP_logo.png"));
      primaryStage.setResizable(false);
      primaryStage.show();
    } 
    
}