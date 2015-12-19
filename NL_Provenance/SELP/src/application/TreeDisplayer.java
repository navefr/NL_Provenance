package application;

import java.util.List;
import java.util.Vector;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import Pattern.Pattern;
import Pattern.PatternNode;
import Top1.DerivationTree2;
import Basics.Atom;

public class TreeDisplayer 
{
	
	public static double highetOfNextTree = 50;
	
	
	/*************************************************************************************************************/
	/** Title: fitsPattern																				
	/** Description: checks if the current node is part of the pattern 			
	/*************************************************************************************************************/
	
	private static boolean fitsPattern (String fact, Pattern pattern)
	{
		boolean retVal = false;
		Atom a = ParseProgram.BuildAtom(fact);
		for (Vector<PatternNode> vec : pattern.getPatternVec()) 
		{
			for (PatternNode node : vec) 
			{
				node.setName(node.getName().split("_")[0]);
				if (true == node.FittsPartialInst(a)) 
				{
					retVal = true;
					vec.remove(node);
					break;
				}
			}
		}
		
		return retVal;
	}
	
	
	/*************************************************************************************************************/
	/** Title: MakeWeightNode																				
	/** Description: Creates a new textField in the right position in the GUI and subsequently, a new pattern node 			
	/*************************************************************************************************************/
	
	private static Label MakeWeightNode (final double x , final double y, DerivationTree2 tree, List<Node> labels)//Pane pane)//
	{		
		String text = "Weight: " + tree.getWeight();
		
		Label node = new Label ( text );
    	node.setLayoutX(x);
    	node.setLayoutY(y);
    	node.setMaxSize(330, 100);
    	node.setFont(Font.font(null, FontWeight.BOLD, 13));
    	node.setAlignment(Pos.CENTER);
    	
    	//pane.getChildren().add(node);
    	labels.add(node);
    	return node;
	}
	
	
	/*************************************************************************************************************/
	/** Title: MakeNode																				
	/** Description: Creates a new textField in the right position in the GUI and subsequently, a new pattern node 			
	/*************************************************************************************************************/
	
	private static Label MakeNode (final double x , final double y, DerivationTree2 tree, List<Node> labels, Pattern pattern)//Pane pane)//
	{		
		String name = (null == tree.getRulePointer()) ? tree.getDerivedFact().getPredicate().split("_")[0] + tree.getDerivedFact().toString() : 
			tree.getRulePointer().headPredicate().toString().split("_")[0] + tree.getDerivedFact().toString();
		
		Label node = new Label ( name );
    	node.setLayoutX(x);
    	node.setLayoutY(y);
    	node.setMaxSize(330, 100);
    	node.setFont(Font.font(null, FontWeight.BOLD, 13));
    	node.setAlignment(Pos.CENTER);
    	
    	if (fitsPattern(name, pattern)) 
		{
    		node.setTextFill(Color.web("#0076a3"));
		}
    	
    	//pane.getChildren().add(node);
    	labels.add(node);
    	return node;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: AddLine																				
	/** Description: Creates a line between two nodes. 			
	/*************************************************************************************************************/
	
	private static void AddLine (Label child, Label parent, List<Node> labels)//Pane pane)//
	{
			final Line line = new Line();
			line.setStartX(parent.getLayoutX() + 60);
		    line.setStartY(parent.getLayoutY() + 25);
		    line.setEndX(child.getLayoutX() + 60);
		    line.setEndY(child.getLayoutY());
		    
		    //pane.getChildren().add(line);
		    labels.add(line);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: getPattern																				
	/** Description: Parses the user generated pattern and creates a Pattern object. 			
	/*************************************************************************************************************/
	
	public static void drawTree (double x , double y, DerivationTree2 tree, List<Node> labels, Pattern pattern)//Pane pane)//
	{
		highetOfNextTree = 50;
		Pattern p = new Pattern(pattern);
		Label root = MakeNode(x, y, tree, labels, p);//pane);
		int depth = findDepth(tree);
		MakeWeightNode(x + 270, y, tree, labels);
		//root.setText(root.getText() + "		Weight: " + tree.getWeight());
		drawSubTree(tree, root, depth, labels, p);//pane
	}
	
	
	/*************************************************************************************************************/
	/** Title: drawSubTree																				
	/** Description: The recursive method to help getPattern() parse the GUI pattern.			
	/*************************************************************************************************************/
	
	private static void drawSubTree (DerivationTree2 tree, Label node, int depth, List<Node> labels, Pattern pattern)//Pane pane, 
	{
		if (null == tree.getChildren())
		{
			highetOfNextTree = ( highetOfNextTree < node.getLayoutY() + 100 ) ? node.getLayoutY() + 100 : highetOfNextTree; 
			return;
		}
		
		int offset = 1 - tree.getChildren().size();
		for (DerivationTree2 child : tree.getChildren()) 
		{
			Label labelChild = MakeNode(node.getLayoutX() + 100 * offset * depth, node.getLayoutY() + 70, child, labels, pattern);//pane);
			AddLine(labelChild, node, labels);//pane);
			drawSubTree(child, labelChild, depth - 1, labels, pattern);//pane, 
			offset = offset + 2;
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: findDepth																				
	/** Description: Finds the depth of the derivation tree.			
	/*************************************************************************************************************/
	
	private static int findDepth (DerivationTree2 tree)
	{
		if (null == tree.getChildren()) 
		{
			return 0;
		}
		
		int maxChildDepth = 0;
		for (DerivationTree2 child : tree.getChildren()) 
		{
			int curDepth = findDepth(child);
			if (maxChildDepth < curDepth) 
			{
				maxChildDepth = curDepth;
			}
		}
		
		return 1 + maxChildDepth;
	}
	
	
	/*************************************************************************************************************/
	/** Title: getMaxWidth																				
	/** Description: get Max Width of tree from tree list.			
	/*************************************************************************************************************/
	
	public static int getMaxWidth (List<DerivationTree2> treesForFact)
	{
		int width = 0;
		for (DerivationTree2 tree : treesForFact) 
		{
			width = Math.max(width, getMaxWidth(tree));
		}
		
		return width;
	}
	
	
	/*************************************************************************************************************/
	/** Title: getMaxWidth																				
	/** Description: get Width of a given tree.			
	/*************************************************************************************************************/
	
	public static int getMaxWidth (DerivationTree2 tree)
	{
		int maxWdth = 0;
		int depth = findDepth(tree);
				
		for (int i = depth; i >= 0; i--) 
		{
			int width = getWidth(tree, i);
			if(width > maxWdth) 
				maxWdth  = width;
		}
		
		return maxWdth;
	}
	
	
	/*************************************************************************************************************/
	/** Title: getWidth																				
	/** Description: get Width of a given level.			
	/*************************************************************************************************************/
	
	public static int getWidth (DerivationTree2 tree, int level)
	{
		if (level == 0) 
			return 1;  
		else if (tree.getChildren() != null)
		{
			int width = 0;
			for (DerivationTree2 child : tree.getChildren()) 
			{
				width += getWidth(child, level-1);
			}
			
			return width;
		}
		
		return 0;
	}
	
	
}
