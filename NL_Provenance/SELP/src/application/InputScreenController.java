package application;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.facts.IFacts;

import Basics.Program;
import Pattern.Pattern;
import Pattern.PatternNode;
import Top1.DerivationTree2;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.control.MenuItem;

public class InputScreenController implements Initializable, ControlledScreen
{
	ScreensController myController; 
	
	
	private TableView<TradeGoods> ExportTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> ExportCountryCol = new TableColumn<TradeGoods, String>();;
	
	private TableColumn<TradeGoods, String> ExportProductCol = new TableColumn<TradeGoods, String>();;
	
	private TableView<TradeGoods> ImportTable = new TableView<TradeGoods>();;
	
	private TableColumn<TradeGoods, String> ImportCountryCol = new TableColumn<TradeGoods, String>();;
	
	private TableColumn<TradeGoods, String> ImportProductCol = new TableColumn<TradeGoods, String>();;
	
	private TableView<TradeGoods> hasChildTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> parentCol = new TableColumn<TradeGoods, String>();
	
	private TableColumn<TradeGoods, String> childCol = new TableColumn<TradeGoods, String>();
	
	private TableView<TradeGoods> isMarriedToTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> husbandCol = new TableColumn<TradeGoods, String>();
	
	private TableColumn<TradeGoods, String> wifeCol = new TableColumn<TradeGoods, String>();
	
	private TableView<TradeGoods> producedTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> producerCol = new TableColumn<TradeGoods, String>();
	
	private TableColumn<TradeGoods, String> movieProdCol = new TableColumn<TradeGoods, String>();
	
	private TableView<TradeGoods> directedTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> directorCol = new TableColumn<TradeGoods, String>();
	
	private TableColumn<TradeGoods, String> movieDirCol = new TableColumn<TradeGoods, String>();
	
	private TableView<TradeGoods> wasBornInTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> personCol = new TableColumn<TradeGoods, String>();
	
	private TableColumn<TradeGoods, String> placeCol = new TableColumn<TradeGoods, String>();
	
	private TableView<TradeGoods> isLocatedInTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> cityCol = new TableColumn<TradeGoods, String>();
	
	private TableColumn<TradeGoods, String> stateCol = new TableColumn<TradeGoods, String>();
	
	private TableView<TradeGoods> createdTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> creatorCol = new TableColumn<TradeGoods, String>();
	
	private TableColumn<TradeGoods, String> createdCol = new TableColumn<TradeGoods, String>();
	
	private TableView<TradeGoods> diedInTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> deceasedCol = new TableColumn<TradeGoods, String>();
	
	private TableColumn<TradeGoods, String> placeDeathCol = new TableColumn<TradeGoods, String>();
	
	private TableView<TradeGoods> livesInTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> manCol = new TableColumn<TradeGoods, String>();

	private TableColumn<TradeGoods, String> livingCol = new TableColumn<TradeGoods, String>();
	
	private TableView<TradeGoods> isInterestedInTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> researcherCol = new TableColumn<TradeGoods, String>();
	
	private TableColumn<TradeGoods, String> subjectCol = new TableColumn<TradeGoods, String>();
	
	private TableView<TradeGoods> graduatedFromTable = new TableView<TradeGoods>();
	
	private TableColumn<TradeGoods, String> alumniCol = new TableColumn<TradeGoods, String>();
	
	private TableColumn<TradeGoods, String> uniCol = new TableColumn<TradeGoods, String>();
	
	@FXML
	private TextArea program;
	
	@FXML
	private TextField kField;
	
	@FXML
	private Button eval;
	
	@FXML
	private Pane canvas;

	@FXML
	private MenuItem close;
	
	@FXML
	private AnchorPane dbPane;
	
	@FXML
	private ScrollPane scrollDbPane;
	
	
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) 
	{
		SystemCommands.fillCategories();
		int yLayout = 30;
		Map<String, List<String>> facts = TableDisplayer.parseDemoFile("demo1");
		yLayout = LoadTable(facts, "imports", ImportTable, ImportCountryCol, ImportProductCol, yLayout);
		yLayout = LoadTable(facts, "exports", ExportTable, ExportCountryCol, ExportProductCol, yLayout);
		yLayout = LoadTable(facts, "hasChild", hasChildTable, parentCol, childCol, yLayout);
		yLayout = LoadTable(facts, "isMarriedTo", isMarriedToTable, husbandCol, wifeCol, yLayout);
		yLayout = LoadTable(facts, "produced", producedTable, producerCol, movieProdCol, yLayout);
		yLayout = LoadTable(facts, "directed", directedTable, directorCol, movieDirCol, yLayout);
		yLayout = LoadTable(facts, "wasBornIn", wasBornInTable, personCol, placeCol, yLayout);
		yLayout = LoadTable(facts, "isLocatedIn", isLocatedInTable, cityCol, stateCol, yLayout);
		yLayout = LoadTable(facts, "created", createdTable, creatorCol, createdCol, yLayout);
		yLayout = LoadTable(facts, "diedIn", diedInTable, deceasedCol, placeDeathCol, yLayout);
		yLayout = LoadTable(facts, "livesIn", livesInTable, manCol, livingCol, yLayout);
		yLayout = LoadTable(facts, "isInterestedIn", isInterestedInTable, researcherCol, subjectCol, yLayout);
		yLayout = LoadTable(facts, "graduatedFrom", graduatedFromTable, alumniCol, uniCol, yLayout);
		
		scrollDbPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		
		eval.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() 
	           { 
					@Override
					public void handle(ActionEvent t) 
					{
						runLoad();
					}
	           });
		
		canvas.setOnMouseClicked(new EventHandler<MouseEvent>() 
				{
					@Override
					public void handle(MouseEvent t) 
					{
						if (canvas.getChildren().isEmpty()) 
						{
							PatternBuilder.MakeNode(185, 50, canvas, null, false);
						}
					}
				});
		
		close.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() 
	           { 
					@Override
					public void handle(ActionEvent t) 
					{
						Reset.deleteProgramFromFile("demo1");
						System.exit(0);
					}
	           });
		
		//for experiments!
		kField.setText("2");
		
		program.setText("dealsWith(a,b) :- dealsWith(b,a) & 0.9\n" 
				+"dealsWith(a,b) :- dealsWith(a,f), dealsWith(f,b) & 0.7\n"
				+"dealsWith(a,b) :- imports(a,f), exports(b,f) & 0.6 \n"
				+"hasChild(a,b) :- isMarriedTo(e,a), hasChild(e,b) & 0.5\n"
				+"hasChild(a,b) :- isMarriedTo(a,f), hasChild(f,b) & 0.5\n"
				+"isMarriedTo(a,b) :- isMarriedTo(b,a) & 0.5\n"
				+"isMarriedTo(a,b) :- hasChild(a,c), hasChild(b,c) & 0.5\n"
				+"produced(a,b) :- directed(a,b) & 0.5\n"
				//+"influences(a,b) :- influences(a,f), influences(f,b) & 0.5\n"
				+"isCitizenOf(a,b) :- wasBornIn(a,f), isLocatedIn(f,b) & 0.5\n"
				+"diedIn(a,b) :- wasBornIn(a,b) & 0.5\n"
				+"directed(a,b) :- created(a,b) & 0.5\n"
				//+"influences(a,b) :- influences(a,f), influences(b,f) & 0.5\n"
				+"isPoliticianOf(a,b) :- diedIn(a,f), isLocatedIn(f,b) & 0.5\n"
				+"isPoliticianOf(a,b) :- livesIn(a,f), isLocatedIn(f,b) & 0.5\n"
				//+"isInterestedIn(a,b) :- influences(a,f), isInterestedIn(f,b) & 0.5\n"
				+"worksAt(a,b) :- graduatedFrom(a,b) & 0.5\n"
				//+"influences(a,b) :- influences(e,a), influences(e,b) & 0.5\n"
				//+"isInterestedIn(a,b) :- isInterestedIn(e,b), influences(e,a) & 0.5\n"
				+"produced(a,b) :- created(a,b) & 0.5\n"
				+"isPoliticianOf(a,b) :- wasBornIn(a,f), isLocatedIn(f,b) & 0.5");
	}

	
	
	public void setScreenParent(ScreensController screenParent)
	{ 
		myController = screenParent; 
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: goToOutputScreen																				
	/** Description: Passes the top-k trees to a function in the OutputScreenController class that displays it and
	/** loads the output screen.
	/*************************************************************************************************************/
	
	public void goToOutputScreen (Map<ITuple, List<DerivationTree2>> trees, IFacts facts, int k, Pattern pattern) throws IOException
	{
		OutputScreenController.displayResults(trees, facts, k, pattern);
		Reset.deleteProgramFromFile("demo1");
		myController.setScreen(ScreensFramework.OUTPUT_SCREEN);
	}
	

	
	/*************************************************************************************************************/
	/** Title: runLoad																				
	/** Description: Run the algorithm on a new thread to avoid "not responding".
	/** Taken from http://stackoverflow.com/questions/16189418/show-loading-animation-during-onclick-event-in-java-swing-onclick
	/*************************************************************************************************************/
	
	private void runLoad ()
	{
		myController.getScene().setCursor(Cursor.WAIT); //Change cursor to wait style
		new Thread(new Runnable(){

			@Override
			public void run(){
				boolean success = Evaluate ();
				if(true == success)
				{
					SwingUtilities.invokeLater(new Runnable(){
						@Override public void run(){
							myController.getScene().setCursor(Cursor.DEFAULT); //Change cursor to default style
						}
					});
				}
			}

		}).start();
	}
	
	
	/*************************************************************************************************************/
	/** Title: Evaluate																				
	/** Description: Handles the evaluation process. Parses the program, pattern and k value inserted by the user,
	/** intersects the program and pattern, writes it to the iris file and runs the top-k algorithm. 			
	/*************************************************************************************************************/
	
	private boolean Evaluate ()
	{		
		if (true == checkValid()) 
		{
			Program prog = ParseProgram.BuildProgram( program.getText() );
			Pattern pattern = PatternBuilder.getPattern ();
			prog = SystemCommands.WriteToIrisFile(pattern, prog, "demo1.iris");
			
			try 
		    { 
				int k = ParseInteger(kField.getText());
				
				if (-1 != k)
				{
					//add label to pattern root.
					if (false == pattern.getPatternVec().isEmpty())
					{
						PatternNode root = pattern.getPatternVec().get(0).get(0);
						root.setName(root.getName() + root.getNewName());
						Map<ITuple, List<DerivationTree2>> trees = new HashMap<ITuple, List<DerivationTree2>>();
						IFacts facts = SystemCommands.Topk(k, prog, "demo1.iris", root, trees);
						goToOutputScreen(trees, facts, k, pattern);
					}
					
					else
					{
						Map<ITuple, List<DerivationTree2>> trees = new HashMap<ITuple, List<DerivationTree2>>();
						IFacts facts = SystemCommands.Topk(k, prog, "demo1.iris", null, trees);
						goToOutputScreen(trees, facts, k, pattern);
					}
				}
				else
				{
					System.out.println("InputScreenController ERROR:: K is not valid");
				}
		    }
			catch(Exception e) 
		    { 
				Reset.deleteProgramFromFile("demo1");
				e.printStackTrace();
		        System.out.println("InputScreenController ERROR:: Something Wrong With Topk Function");
		    }
			
			return true;
		}
		
		return false;
	}

	
	
	/*************************************************************************************************************/
	/** Title: ParseInteger																				
	/** Description: Parse the value of k. 			
	/*************************************************************************************************************/
	
	private int ParseInteger(String s) 
	{
		int res = 0;
	    try 
	    { 
	    	res = Integer.parseInt(s);
	    } 
	    catch(NumberFormatException e) 
	    { 
	    	System.out.println("InputScreenController ERROR:: K is not a number"); 
	        return -1; 
	    }
	    
	    return res;
	}
 
	
	
	/*************************************************************************************************************/
	/** Title: checkValid																				
	/** Description:  			
	/*************************************************************************************************************/
	
	private boolean checkValid ()
	{
		boolean retVal = true;
		if (program.getText().isEmpty()) 
		{
			System.out.println("InputScreenController ERROR:: No Program Entered!");
			retVal = false;
		}
		if (kField.getText().isEmpty()) 
		{
			System.out.println("InputScreenController ERROR:: Did not insert k value!");
			retVal = false;
		}
		
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: LoadTable																				
	/** Description: 		
	/*************************************************************************************************************/
	
	private int LoadTable(Map<String, List<String>> facts, String predicate, TableView<TradeGoods> table, TableColumn<TradeGoods, String> colA, TableColumn<TradeGoods, String> colB, int yLayout) 
	{
		
		colA.setCellValueFactory(new PropertyValueFactory<TradeGoods, String>("country"));
		colB.setCellValueFactory(new PropertyValueFactory<TradeGoods, String>("product"));
		colA.setText(SystemCommands.relTocat.get(predicate)[0]);
		colB.setText(SystemCommands.relTocat.get(predicate)[1]);

		colA.setMinWidth(280);
		colB.setMinWidth(280);

		table.setMaxSize(575, 150);
		table.setLayoutX(0);
		table.setLayoutY(yLayout);

		Label node = new Label ( predicate );
		node.setLayoutX(270 - predicate.length());
		node.setLayoutY(yLayout - 20);
		node.setMaxSize(250, 100);
		node.setFont(new Font(14));
		node.setAlignment(Pos.CENTER);

		dbPane.getChildren().addAll(table, node);
		
		try 
		{
			TableDisplayer.genNewStats(facts, predicate, "demo1", table, colA, colB);
		} 
		catch (NumberFormatException | IOException e) 
		{
			e.printStackTrace();
		}	
		
		return yLayout + 190;	
	}
}
