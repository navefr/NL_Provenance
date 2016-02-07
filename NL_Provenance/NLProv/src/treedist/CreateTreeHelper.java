package treedist;

import java.util.Hashtable;
import java.util.ArrayList;


/* 
 * INSERT-LICENCE-INFO
 */
public class CreateTreeHelper {

    /* This takes a String describing a tree and converts it into a
     * TreeDefinition.  The format of the string is a series of edges
     * represented as pairs of string separated by semi-colons.  Each
     * pair is comma separated.  The first substring in the pair is
     * the parent, the second is the child.  The first edge parent
     * must be the root of the tree.  
     *
     * For example: "a-b;a-c;c-d;c-e;c-f;"
     */
    public static TreeDefinition makeTree(String treeSpec)  {
	return makeTree(treeSpec, null);
    }

    
    /* This takes a String describing a tree and converts it into a
     * TreeDefinition.  The format of the string is a series of edges
     * represented as pairs of string separated by semi-colons.  Each
     * pair is comma separated.  The first substring in the pair is
     * the parent, the second is the child.  
     *
     * For example: "a-b;a-c;c-d;c-e;c-f;"
     */
    public static TreeDefinition makeTree(String treeSpec, String rootID)  {
	
	//A Tree
	Hashtable<String, ArrayList<String>> aTree 
	    = new Hashtable<String, ArrayList<String>>();
	
	String root = rootID;

	String[] edges = treeSpec.split(";");
	for (String edge: java.util.Arrays.asList(edges)) {

	    System.out.println("CreateTreeHelper: Examining edge: "+edge);

	    String[] nodes = edge.split("-");
	    addEdge(nodes[0], nodes[1], aTree);
	    if (root == null) {
		root = nodes[0];
	    }
	}

	BasicTree aBasicTree = 
	    new BasicTree(aTree, root, BasicTree.POSTORDER);

	return aBasicTree;
    }

    /** This adds the edge (and nodes if necessary) to the tree
     * definition .
     */
    protected static void addEdge(String parentLabel, String childLabel, 
			 Hashtable<String, ArrayList<String>> treeStructure) {
	//Add Parent node, edge and child
	if (!treeStructure.containsKey(parentLabel)) {
	    treeStructure.put(parentLabel, new ArrayList<String>());
	}
	
	treeStructure.get(parentLabel).add(childLabel);

	//Add child if not already there
	if (!treeStructure.containsKey(childLabel)) {
	    treeStructure.put(childLabel, new ArrayList<String>());
	}
    }

    
}