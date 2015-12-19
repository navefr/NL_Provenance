package application;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedState;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Paint;

import org.apache.commons.collections15.Transformer;








import Basics.Atom;

import javax.swing.JFrame;



public class GraphDisplayer
{
    
	Graph<String, String> g;
	
	private String current = "";
	
	private Color [] transColors = new Color [] {new Color(0,0,128), new Color(0,255,255), new Color(173,216,230), new Color(147,112,219), new Color(218,112,214)}; //blues
	
	private Color [] symColors = new Color [] {new Color(139,0,0), new Color(255,127,80), new Color(255,165,0), new Color(128,0,0), new Color(255,215,0)}; //reds
	
	private int symIdx = 0;
	
	private int transIdx = 0;
	
	
    /** Creates a new instance of SimpleGraphView */
    public GraphDisplayer() 
    {
        // Graph<V, E> where V is the type of the vertices and E is the type of the edges
        g = new SparseMultigraph<String, String>();  
    }
    
    
    
    /**
     * @param args the command line arguments
     */
    public void display(int k, boolean isTopkScreen) 
    {
        // Layout<V, E>, VisualizationComponent<V,E>
        Layout<String, String> layout = new CircleLayout(g);
        layout.setSize(new Dimension(800,800));
        VisualizationViewer<String,String> vv = new VisualizationViewer<String,String>(layout);
        vv.setPreferredSize(new Dimension(800,800));
        // Show vertex and edge labels
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        //vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        // Create a graph mouse and add it to the visualization component
        final DefaultModalGraphMouse<String,String> gm = new DefaultModalGraphMouse<String,String>();
        vv.setGraphMouse(gm);
        
        vv.addGraphMouseListener(new GraphMouseListener<String>() {
        	
        	@Override
            public void graphClicked(String v, MouseEvent me) 
        	{
                if (me.getButton() == MouseEvent.BUTTON1 && me.getClickCount() == 2) 
                {
                	gm.setMode(ModalGraphMouse.Mode.PICKING);
                }
            }
        	
        	 @Override
             public void graphPressed(String v, MouseEvent me) {}

             @Override
             public void graphReleased(String v, MouseEvent me) 
             {
            	 gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
             }

        });
        
        
       /* final PickedState<String> pickedState = vv.getPickedVertexState();
        
        pickedState.addItemListener(new ItemListener() {
        	 
            @Override
            public void itemStateChanged(ItemEvent e) {
            Object subject = e.getItem();  
            if (subject instanceof String) 
            {
            	String vertex = (String) subject;
            	if (pickedState.isPicked(vertex)) 
            	{
            		System.out.println("Vertex " + vertex
            				+ " is now selected");
            	} 
            	
            	else
            	{
            		System.out.println("Vertex " + vertex
            				+ " no longer selected");
            	}
            }
         }
        });*/
        
        
        // Transformer maps the vertex number to a vertex property
        Transformer<String,Paint> vertexColor = new Transformer<String,Paint>() 
        {
            public Paint transform(String name) 
            {
                return name.substring(0, 2).contains("dealsWith") ? Color.BLUE : Color.RED;
            }
        };
        
        /*Transformer<String,Shape> vertexSize = new Transformer<String,Shape>()
        {
            public Shape transform(String name)
            {
            	return name.contains("RT") ? new Ellipse2D.Double() : new Ellipse2D.Double(-15, -15, 30, 30);
            }
        };*///vv.getRenderContext().setVertexShapeTransformer(vertexSize);
        
        Transformer<String, Paint> colorTransformer = new Transformer<String, Paint>()
        {
        	@Override
        	public Paint transform(String e)
        	{
        		final String s = g.getSource(e);
        		final String d = g.getDest(e);
        		if (d.contains("exports") || d.contains("imports"))
        		{
        			if (current.equals(s)) //second derivation of the same rule
        			{
        				return Color.ORANGE;
					}
        			else
        			{
        				current = s;
        				return Color.GREEN;
        			}
        		}
        		
        		Atom a = ParseProgram.BuildAtom(s);
        		Atom b = ParseProgram.BuildAtom(d);
        		if ( a.isSymmetricTo(b) )//Symmetric rule used
        		{
        			if (current.equals(s)) //second derivation of the same rule
        			{
        				symIdx %= 5;
        				return symColors[symIdx++];
					}
        			else
        			{
        				symIdx = 0;
        				current = s;
        				return Color.MAGENTA;
        			}
        		}
        		
        		else //Transitive rule used
        		{
        			if (current.equals(s)) //second derivation of the same rule
        			{
        				transIdx %= 5;
        				return transColors[transIdx++];
					}
        			else
        			{
        				transIdx = 0;
        				current = s;
        				return Color.BLUE;
        			}
        		}
        		
        		//return Color.BLACK;
        	}
        };
        
        vv.getRenderContext().setVertexFillPaintTransformer(vertexColor);
        vv.getRenderContext().setArrowFillPaintTransformer(colorTransformer);
        vv.getRenderContext().setArrowDrawPaintTransformer(colorTransformer);
        vv.getRenderContext().setEdgeDrawPaintTransformer(colorTransformer);
        
        JFrame frame;
        if (isTopkScreen) 
        {
        	frame = new JFrame("Top-k");
		}
        else
        {
        	frame = new JFrame("Full Provenance");
        }
       
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
        
        /*frame.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
        	mouseComboBoxActionPerformed(vv, gm, e);
        }
        });*/
    }
    
    
    
    /*private void mouseComboBoxActionPerformed(VisualizationViewer<String,String> vv, DefaultModalGraphMouse gm, MouseEvent e) 
    { 
        JComboBox jcb = (JComboBox) e.getSource();
        String selectedItem = (String)jcb.getSelectedItem();
        if(selectedItem.equals("Transform"))
        {
            gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        } 
        else 
        {
            gm.setMode(ModalGraphMouse.Mode.PICKING);
        }
        
        vv.repaint();
    } */
    
    
    
    public Graph<String, String> getGraph ()
    {
    	return g;
    }
}