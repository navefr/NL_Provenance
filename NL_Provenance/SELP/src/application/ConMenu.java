package application;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;
import javafx.scene.shape.Line;


public class ConMenu extends ContextMenu 
{
	private TextField parent;

	private List<ConMenu> menuChildren = new ArrayList<ConMenu>();
	
	private List<Line> attachedLines = new ArrayList<Line>();
	
	private boolean isParentTrans;
	
	private Line lineToParent;
	
	
	
	public ConMenu ()
	{
		super();
	}

	
	public boolean isParentTrans() 
	{
		return isParentTrans;
	}



	public void setParentTrans(boolean isParentTrans) 
	{
		this.isParentTrans = isParentTrans;
	}
	
	
	
	public TextField getParent() 
	{
		return parent;
	}

	
	
	public void setParent(TextField parent) 
	{
		this.parent = parent;
	}
	
	

	public List<Line> getAttachedLines() 
	{
		return attachedLines;
	}


	
	public void addAttachedLine(Line attachedLine) 
	{
		attachedLines.add(attachedLine);
	}
	
	
	
	public List<ConMenu> getMenuChildren() 
	{
		return menuChildren;
	}

	
	public void addMenuChild(ConMenu child) 
	{
		menuChildren.add(child);
		realign();
		
		for (ConMenu menuChild : menuChildren) 
		{
			menuChild.realign();
		}
	}



	private void realign()
	{
		int offset = 1 - menuChildren.size();
		for (ConMenu child : menuChildren) 
		{
			child.getParent().setLayoutX(parent.getLayoutX() + 90 * offset);
			offset = offset + 2;
		}
	}
	
	
	public Line getLineToParent() 
	{
		return lineToParent;
	}


	public void setLineToParent(Line lineToParent) 
	{
		this.lineToParent = lineToParent;
	}
}
