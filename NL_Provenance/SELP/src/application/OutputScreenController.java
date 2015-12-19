package application;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.facts.IFacts;

import Pattern.Pattern;
import Pattern.PatternNode;
import Top1.DerivationTree2;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.util.Callback;

public class OutputScreenController implements Initializable, ControlledScreen 
{
	ScreensController myController; 
	
	private static Map<ITuple, List<Node>> labels = new HashMap<ITuple, List<Node>>();
	
	private static  Map<ITuple, Integer> maxWidth = new HashMap<ITuple, Integer>();
	
	private static Map<String, List<TradeGoods>> newRows = new HashMap<String, List<TradeGoods>>();
	
	private int w = 0;
	
	@FXML
	private Pane outputMainPane;
	
	@FXML
	private static TableView<TradeGoods> ExportTable = new TableView<TradeGoods>();
	
	@FXML
	private static TableColumn<TradeGoods, String> ExportCountryCol = new TableColumn<TradeGoods, String>();;
	
	@FXML
	private static TableColumn<TradeGoods, String> ExportProductCol = new TableColumn<TradeGoods, String>();;
	
	@FXML
	private static TableView<TradeGoods> ImportTable = new TableView<TradeGoods>();
	
	@FXML
	private static TableColumn<TradeGoods, String> ImportCountryCol = new TableColumn<TradeGoods, String>();;
	
	@FXML
	private static TableColumn<TradeGoods, String> ImportProductCol = new TableColumn<TradeGoods, String>();;
	
	@FXML
	private static TableView<TradeGoods> DealsWithTable = new TableView<TradeGoods>();
	
	@FXML
	private static TableColumn<TradeGoods, String> DealsWithCountryC1 = new TableColumn<TradeGoods, String>();;
	
	@FXML
	private static TableColumn<TradeGoods, String> DealsWithCountryC2 = new TableColumn<TradeGoods, String>();;
	
	private static TableView<TradeGoods> hasChildTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> parentCol = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> childCol = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> isMarriedToTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> husbandCol = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> wifeCol = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> producedTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> producerCol = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> movieProdCol = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> directedTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> directorCol = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> movieDirCol = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> wasBornInTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> personCol = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> placeCol = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> isLocatedInTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> cityCol = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> stateCol = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> createdTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> creatorCol = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> createdCol = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> diedInTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> deceasedCol = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> placeDeathCol = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> livesInTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> manCol = new TableColumn<TradeGoods, String>();

	private static TableColumn<TradeGoods, String> livingCol = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> isInterestedInTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> researcherCol = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> subjectCol = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> graduatedFromTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> alumniCol = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> uniCol = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> isPoliticianOfTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> politicianCol1 = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> politicianCol2 = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> isCitizenOfTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> citizenCol1 = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> citizenCol2 = new TableColumn<TradeGoods, String>();
	
	private static TableView<TradeGoods> worksAtTable = new TableView<TradeGoods>();
	
	private static TableColumn<TradeGoods, String> worksCol1 = new TableColumn<TradeGoods, String>();
	
	private static TableColumn<TradeGoods, String> worksCol2 = new TableColumn<TradeGoods, String>();
	
	@FXML
	private Button Back;
	
	@FXML
	private MenuItem close;
	
	@FXML
	private Button prov;
	
	@FXML
	private ScrollPane scrollPane;
	
	
	@FXML
	private static Pane treePane = new Pane();
	
	@FXML
	private AnchorPane dbPane;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		SystemCommands.fillCategories();
		int yLayout = 30;
		Map<String, List<String>> facts = TableDisplayer.parseDemoFile("demo1");
		yLayout = LoadTable(facts, "dealsWith", DealsWithTable, DealsWithCountryC1, DealsWithCountryC2, yLayout);
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
		yLayout = LoadTable(facts, "isPoliticianOf", isPoliticianOfTable, politicianCol1, politicianCol2, yLayout);
		yLayout = LoadTable(facts, "isCitizenOf", isCitizenOfTable, citizenCol1, citizenCol2, yLayout);
		yLayout = LoadTable(facts, "worksAt", worksAtTable, worksCol1, worksCol2, yLayout);
		
		scrollPane.setContent(treePane);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		DealsWithTable.setRowFactory(new Callback<TableView<TradeGoods>, TableRow<TradeGoods>>() {
			@Override
			public TableRow<TradeGoods> call(TableView<TradeGoods> p) {
				final TableRow<TradeGoods> row = new TableRow<TradeGoods>();
				row.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent t) {
						IStringTerm country1 = Factory.TERM.createString (row.getItem().getCountry());
						IStringTerm country2 = Factory.TERM.createString (row.getItem().getProduct());
						ITuple selected = Factory.BASIC.createTuple(new ITerm[] {country1, country2});
						
						if (labels.containsKey(selected)) 
						{
							treePane.getChildren().clear();
							treePane.getChildren().addAll( labels.get(selected) );
							w = maxWidth.get(selected);
						}
					}
				});
				
				return row;
			}
		});
		
		Back.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() 
				{ 
					@Override
					public void handle(ActionEvent t) 
					{
						goToInputScreen(t);
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
		
		prov.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() 
		           { 
						@Override
						public void handle(ActionEvent t) 
						{
							Reset.deleteProgramFromFile("demo1");
							fullProv ();
						}
		           });
		
		
		treePane.widthProperty().addListener(
				new ChangeListener<Number>() 
				{
					@Override 
				    public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) 
					{
						if (labels.keySet().size() == 1)
							scrollPane.setHvalue(0.5);
						else if (w == 4) 
							scrollPane.setHvalue(0.7);
						else if (w == 3)
							scrollPane.setHvalue(0.5);
			        }
				});
	}
	
	
	
	public void goToInputScreen (ActionEvent event)
	{
		Reset.deleteProgramFromFile("demo1");
		newRows.clear();
		myController.setScreen(ScreensFramework.INPUT_SCREEN);
	}
	
	
	@Override
	public void setScreenParent(ScreensController screenParent)
	{
		myController = screenParent; 
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: fullProv																				
	/** Description: runs the full Prov. of the demo file.
	/*************************************************************************************************************/
	
	private void fullProv ()
	{
		myController.getScene().setCursor(Cursor.WAIT); //Change cursor to wait style
		new Thread(new Runnable(){

			@Override
			public void run(){
				boolean success = FullProvForDemo.fullProv();
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
	/** Title: displayTrees																				
	/** Description: Draws the trees on the pane in the output screen.
	/*************************************************************************************************************/
	
	public static void displayResults (Map<ITuple, List<DerivationTree2>> trees, IFacts facts, int kVal, Pattern pattern)
	{
		labels.clear();
		treePane.getChildren().clear();
		
		List<DerivationTree2> treesForFact = new ArrayList<DerivationTree2>();
		ITuple justOne = null;
		for (ITuple tuple : trees.keySet()) 
		{
			justOne = tuple;
			treesForFact = trees.get(tuple);
			labels.put(tuple, new ArrayList<Node>());
			TreeDisplayer.highetOfNextTree = 50;
			int width = TreeDisplayer.getMaxWidth(treesForFact);
			maxWidth.put(tuple, width);
			for (DerivationTree2 tree : treesForFact) 
			{
				if (width <= 2) 
				{
					TreeDisplayer.drawTree(230, TreeDisplayer.highetOfNextTree, tree, labels.get(tuple), pattern);
				}
				
				else
				{
					TreeDisplayer.drawTree(500, TreeDisplayer.highetOfNextTree, tree, labels.get(tuple), pattern);
				}
			}
		}		
		
		if (1 == trees.keySet().size()) 
		{
			treePane.getChildren().clear();
			treePane.getChildren().addAll(labels.get(justOne));
		}
		
		TableDisplayer.genNewStats(facts, newRows);
		refreshScreen();
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: refreshPane																				
	/** Description: Refreshes the treePane to show the derivation trees. 
	/*************************************************************************************************************/
	
	public static <T,U> void refreshScreen() 
	{
		TableDisplayer.refreshTableView("hasChild", hasChildTable, Arrays.asList(parentCol, childCol), newRows);
		TableDisplayer.refreshTableView("isMarriedTo", isMarriedToTable, Arrays.asList(husbandCol, wifeCol), newRows);
		TableDisplayer.refreshTableView("produced", producedTable, Arrays.asList(producerCol, movieProdCol), newRows);
		TableDisplayer.refreshTableView("directed", directedTable, Arrays.asList(directorCol, movieDirCol), newRows);
		TableDisplayer.refreshTableView("wasBornIn", wasBornInTable, Arrays.asList(personCol, placeCol), newRows);
		TableDisplayer.refreshTableView("isLocatedIn", isLocatedInTable, Arrays.asList(cityCol, stateCol), newRows);
		TableDisplayer.refreshTableView("created", createdTable, Arrays.asList(creatorCol, createdCol), newRows);
		TableDisplayer.refreshTableView("diedIn", diedInTable, Arrays.asList(deceasedCol, placeDeathCol), newRows);
		TableDisplayer.refreshTableView("livesIn", livesInTable, Arrays.asList(manCol, livingCol), newRows);
		TableDisplayer.refreshTableView("isInterestedIn", isInterestedInTable, Arrays.asList(researcherCol, subjectCol), newRows);
		TableDisplayer.refreshTableView("graduatedFrom", graduatedFromTable, Arrays.asList(alumniCol, uniCol), newRows);
		TableDisplayer.refreshTableView("isPoliticianOf", isPoliticianOfTable, Arrays.asList(politicianCol1, politicianCol2), newRows);
		TableDisplayer.refreshTableView("isCitizenOf", isCitizenOfTable, Arrays.asList(citizenCol1, citizenCol2), newRows);
		TableDisplayer.refreshTableView("worksAt", worksAtTable, Arrays.asList(worksCol1, worksCol2), newRows);
		TableDisplayer.refreshTableView("isMarriedTo", isMarriedToTable, Arrays.asList(husbandCol, wifeCol), newRows);
		TableDisplayer.refreshTableView("dealsWith", DealsWithTable, Arrays.asList(DealsWithCountryC1, DealsWithCountryC2), newRows);
		TableDisplayer.refreshTableView("produced", producedTable, Arrays.asList(producerCol, movieProdCol), newRows);
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
		colA.setMinWidth(225);
		colB.setMinWidth(225);

		table.setMaxSize(575, 160);
		table.setLayoutX(0);
		table.setLayoutY(yLayout);

		Label node = new Label ( predicate );
		node.setLayoutX(210 - predicate.length());
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
	
	
	
	/*************************************************************************************************************/
	/** Title: fillCategories																				
	/** Description: 		
	/*************************************************************************************************************/
	
	/*private static void fillCategories() 
	{
		relTocat.put("isMarriedTo", new String [] {"person", "person"});
		relTocat.put("hasChild", new String [] {"person", "child"});
		relTocat.put("directed", new String [] {"director", "movie"});
		relTocat.put("created", new String [] {"director", "movie"});
		relTocat.put("produced", new String [] {"director", "movie"});
		relTocat.put("actedIn", new String [] {"director", "movie"});
		relTocat.put("livesIn", new String [] {"person", "place"});
		relTocat.put("dealsWith", new String [] {"Country", "Country"});	
		relTocat.put("hasCapital", new String [] {"Country", "city"});
		relTocat.put("isLocatedIn", new String []  {"city", "Country"});
		relTocat.put("hasOfficialLanguage", new String []  {"Country", "language"}); 
		relTocat.put("worksAt", new String []  {"person", "university"});  
		relTocat.put("hasAcademicAdvisor", new String []  {"person", "person"}); 
		relTocat.put("isCitizenOf", new String []  {"person", "Country"}); 
		relTocat.put("influences", new String []  {"person", "person"}); 
		relTocat.put("graduatedFrom", new String []  {"person", "university"}); 
		relTocat.put("wasBornIn", new String []  {"person", "city"});  
		relTocat.put("diedIn", new String []  {"person", "city"}); 
		relTocat.put("imports", new String []  {"Country", "Product"}); 
		relTocat.put("exports", new String []  {"Country", "Product"}); 
		relTocat.put("participatedIn", new String []  {"figure" , "event"}); 
		relTocat.put("hasCurrency", new String []  {"region", "currency"}); 
		relTocat.put("isPoliticianOf", new String []  {"person", "state"}); 
		relTocat.put("isLeaderOf", new String []  {"person", "place"}); 
		relTocat.put("isInterestedIn", new String []  {"person", "subject"}); 
		relTocat.put("hasWonPrize", new String []  {"person", "prize"});
		relTocat.put("isKnownFor", new String []  {"person", "prize"});
		relTocat.put("hasGeonamesId", new String []  {"Country", "id"});
		relTocat.put("hasLanguageCode", new String []  {"Language", "code"});
	}*/

}
