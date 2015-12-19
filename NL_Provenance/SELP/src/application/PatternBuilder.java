package application;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import Pattern.Pattern;
import Pattern.PatternNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;


public class PatternBuilder
{ 
	private static ConMenu patternRoot;
	

	
	
	/*************************************************************************************************************/
	/** Title: MakeNode																				
	/** Description: Creates a new textField in the right position in the GUI and subsequently, a new pattern node 			
	/*************************************************************************************************************/
	
	public static TextField MakeNode (final double x , final double y, final Pane canvas, final ConMenu parentMenu, boolean trans)
	{
		TextField textfield = new TextField ("");
    	textfield.setLayoutX(x);
    	textfield.setLayoutY(y);
    	textfield.setMaxSize(160, 50);
    	textfield.setFont(new Font(12));
    	textfield.setAlignment(Pos.CENTER);
    	
    	MenuItem m1 = new MenuItem("Add Regular Child");
	    MenuItem m2 = new MenuItem("Add Transitive Child");
	    MenuItem m3 = new MenuItem("Delete Node");
	    final ConMenu cm = new ConMenu();
    	cm.getItems().addAll(m1, m2, m3);
    	textfield.setContextMenu(cm);
    	cm.setParent(textfield);
    	cm.setParentTrans(trans);
    	
    	if (null == parentMenu) 
    	{
    		patternRoot = cm;
		}
    	
    	else
    	{
    		parentMenu.addMenuChild(cm);
    	}
    	
    	m1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) 
			{
				MakeNode(x, y + 90, canvas, cm, false);
				AddLine(canvas, cm);
			}
    		
    	});
    	
    	m2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) 
			{
				MakeNode(x, y + 90, canvas, cm, true);
				AddLine(canvas, cm);
			}
    		
    	});
    	
    	m3.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) 
			{
				DeleteNode(canvas, cm, parentMenu);
			}
    		
    	});
    	
    	canvas.getChildren().add(textfield);
    	return textfield;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: AddLine																				
	/** Description: Creates a line between two nodes. If trans -> dashed line. If not trans -> regular line 			
	/*************************************************************************************************************/
	
	private static void AddLine (final Pane root, ConMenu cm)
	{
		root.getChildren().removeAll(cm.getAttachedLines());
		cm.getAttachedLines().clear();
		
		for (ConMenu child : cm.getMenuChildren()) 
		{
			final Line line = new Line();
			line.setStartX(cm.getParent().getLayoutX() + 80);
		    line.setStartY(cm.getParent().getLayoutY() + 30);
		    line.setEndX(child.getParent().getLayoutX() + 80);
		    line.setEndY(child.getParent().getLayoutY());
		    
		    if (true == child.isParentTrans())
		    {
		    	line.getStrokeDashArray().addAll(15d, 10d);
		    }
		    
		    child.setLineToParent(line);
		    cm.addAttachedLine(line);
		    root.getChildren().add(line);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: DeleteNode																				
	/** Description: Delete a node from the pattern. 			
	/*************************************************************************************************************/
	
	private static void DeleteNode (final Pane root, ConMenu cm, ConMenu parentMenu)
	{
		List<ConMenu> copy = new ArrayList<ConMenu>(cm.getMenuChildren());
		for (ConMenu child : copy)
		{
			DeleteNode(root, child, cm);
		}
		
		if (null != parentMenu) //not deleting the root of the pattern 
		{
			parentMenu.getMenuChildren().remove(cm);
		}
		
		root.getChildren().remove(cm.getLineToParent());
		root.getChildren().removeAll(cm.getAttachedLines());
		root.getChildren().remove(cm.getParent());
	}

	
	
	/*************************************************************************************************************/
	/** Title: getPattern																				
	/** Description: Parses the user generated pattern and creates a Pattern object. 			
	/*************************************************************************************************************/
	
	public static Pattern getPattern ()
	{
		Vector<Vector<PatternNode>> pattern = new Vector<Vector<PatternNode>> ();
		Vector<PatternNode> rootVec = new Vector<PatternNode> ();
		
		if (patternRoot != null && patternRoot.getParent() != null) 
		{
			PatternNode root = ParseProgram.BuildPatternNode( patternRoot.getParent().getText() );
			rootVec.add( root );
			pattern.add(rootVec);
			
			pattern.add( getSubPattern(root, patternRoot) );
		}
		
		return new Pattern (pattern);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: getSubPattern																				
	/** Description: The recursive method to help getPattern() parse the GUI pattern. 			
	/*************************************************************************************************************/
	
	private static Vector<PatternNode> getSubPattern (PatternNode parent, ConMenu parentMenu)
	{
		Vector<PatternNode> childV = new Vector<PatternNode> ();
		
		for (ConMenu child : parentMenu.getMenuChildren()) 
		{
			PatternNode patternChild = ParseProgram.BuildPatternNode( child.getParent().getText() );
			patternChild.setParent(parent);
			parent.AddPatternChild(patternChild);
			patternChild.setTransChild( child.isParentTrans() );
			
			childV.add(patternChild);
			childV.addAll( getSubPattern(patternChild, child) );
		}
		
		return childV;
	}
}
