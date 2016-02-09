package treedist;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * This is an implementation of the Zhang and Shasha algorithm as
 * described in [FIXME]
 * <p>
 * SWAN 2007-11-01: I'm pretty sure this code comes from:
 * http://www.cs.queensu.ca/TechReports/Reports/1995-372.pdf and
 * "http://www.inf.unibz.it/dis/teaching/ATA/ata7-handout-1x1.pdf"
 * INSERT-LICENCE-INFO
 */
public class ComparisonZhangShasha {

    //"Dynamic Programming" Table.
    //use function setFD to access it.
    //Each call to findDistance will change these tables.  But each
    //call is independent (and reinitialises this) so the side effect
    //has no real consequence.  ie.  There are NO public side effects.
    private Hashtable<String, Hashtable<String, Double>> forestDistance
            = null;
    private double[][] distance = null;

    public Transformation findDistance (TreeDefinition aTree, TreeDefinition bTree, OpsZhangShasha ops) {

        //This is initialised to be n+1 * m+1.  It should really be n*m
        //but because of java's zero indexing, the for loops would
        //look much more readable if the matrix is extended by one
        //column and row.  So, distance[0,*] and distance[*,0] should
        //be permanently zero.
        distance = new double[aTree.getNodeCount() + 1][bTree.getNodeCount() + 1];

        //Preliminaries
        //1. Find left-most leaf and key roots
        Hashtable<Integer, Integer> aLeftLeaf = new Hashtable<Integer, Integer>();
        Hashtable<Integer, Integer> bLeftLeaf = new Hashtable<Integer, Integer>();
        ArrayList<Integer> aTreeKeyRoots = new ArrayList<Integer>();
        ArrayList<Integer> bTreeKeyRoots = new ArrayList<Integer>();

        findHelperTables(aTree, aLeftLeaf, aTreeKeyRoots, aTree.getRootID());

        findHelperTables(bTree, bLeftLeaf, bTreeKeyRoots, bTree.getRootID());

        //Comparison
        for (Integer aKeyroot : aTreeKeyRoots) { //aKeyroot loop

            for (Integer bKeyroot : bTreeKeyRoots) { //bKeyroot loop

                //Re-initialise forest distance tables
                Hashtable<Integer, Hashtable<Integer, Double>> fD = new Hashtable<Integer, Hashtable<Integer, Double>>();

                setFD(aLeftLeaf.get(aKeyroot), bLeftLeaf.get(bKeyroot), 0.0d, fD);

                //for all descendents of aKeyroot: i
                for (int i = aLeftLeaf.get(aKeyroot); i <= aKeyroot; i++) {
                    setFD(i,
                            bLeftLeaf.get(bKeyroot) - 1,
                            getFD(i - 1, bLeftLeaf.get(bKeyroot) - 1, fD) +
                                    ops.getOp(OpsZhangShasha.DELETE).getCost(i, 0, aTree, bTree),
                            fD);
                }

                //for all descendents of bKeyroot: j
                for (int j = bLeftLeaf.get(bKeyroot); j <= bKeyroot; j++) {

                    setFD(aLeftLeaf.get(aKeyroot) - 1, j,
                            getFD(aLeftLeaf.get(aKeyroot) - 1, j - 1, fD) +
                                    ops.getOp(OpsZhangShasha.INSERT).getCost(0, j, aTree, bTree),
                            fD);
                }

                //for all descendents of aKeyroot: i
                for (int i = aLeftLeaf.get(aKeyroot); i <= aKeyroot; i++) {

                    //for all descendents of bKeyroot: j
                    for (int j = bLeftLeaf.get(bKeyroot); j <= bKeyroot; j++) {
                        //End Trace
                        double min =  //This min compares del vs ins
                                java.lang.Math.min
                                        (//Option 1: Delete node from aTree
                                                getFD(i - 1, j, fD) +
                                                        ops.getOp(OpsZhangShasha.DELETE)
                                                                .getCost(i, 0, aTree, bTree),

                                                //Option 2: Insert node into bTree
                                                getFD(i, j - 1, fD) +
                                                        ops.getOp(OpsZhangShasha.INSERT)
                                                                .getCost(0, j, aTree, bTree)
                                        );

                        if ((aLeftLeaf.get(i) == aLeftLeaf.get(aKeyroot))
                                &&
                                (bLeftLeaf.get(j) == bLeftLeaf.get(bKeyroot))) {
                            distance[i][j] =
                                    java.lang.Math.min
                                            (min,
                                                    getFD(i - 1, j - 1, fD) +
                                                            ops.getOp(OpsZhangShasha.RENAME)
                                                                    .getCost(i, j, aTree, bTree)
                                            );

                            setFD(i, j, distance[i][j], fD);
                        } else {
                            setFD(i, j,
                                    java.lang.Math.min
                                            (min,
                                                    getFD(aLeftLeaf.get(i) - 1,
                                                            bLeftLeaf.get(j) - 1, fD) +
                                                            distance[i][j]
                                            ),
                                    fD
                            );
                        }
                    }
                }
            }
        }

        Transformation transform = new Transformation();
        transform.setCost(distance[aTree.getNodeCount()][bTree.getNodeCount()]);
        return transform;

    }

    /**
     * The initiating call should be to the root node of the tree.
     * It fills in an nxn (hash) table of the leftmost leaf for a
     * given node.  It also compiles an array of key roots. The
     * integer values IDs must come from the post-ordering of the
     * nodes in the tree.
     */
    private void findHelperTables(TreeDefinition someTree, Hashtable<Integer, Integer> leftmostLeaves, ArrayList<Integer> keyroots, int aNodeID) {

        findHelperTablesRecurse(someTree, leftmostLeaves, keyroots, aNodeID);

        //add root to keyroots
        keyroots.add(aNodeID);

        //add boundary nodes
        leftmostLeaves.put(0, 0);

    }

    private void findHelperTablesRecurse(TreeDefinition someTree, Hashtable<Integer, Integer> leftmostLeaves, ArrayList<Integer> keyroots, int aNodeID) {

        //If this is a leaf, then it is the leftmost leaf
        if (someTree.isLeaf(aNodeID)) {
            leftmostLeaves.put(aNodeID, aNodeID);
        } else {
            boolean seenLeftmost = false;
            for (Integer child : someTree.getChildrenIDs(aNodeID)) {
                findHelperTablesRecurse(someTree, leftmostLeaves, keyroots, child);
                if (!seenLeftmost) {
                    leftmostLeaves.put(aNodeID, leftmostLeaves.get(child));
                    seenLeftmost = true;
                } else {
                    keyroots.add(child);
                }
            }
        }
    }

    /**
     * This returns the value in the cell of the ForestDistance table
     */
    private double getFD(int a, int b,
                         Hashtable<Integer, Hashtable<Integer, Double>>
                                 forestDistance) {

        Hashtable<Integer, Double> rows = null;
        if (!forestDistance.containsKey(a)) {
            forestDistance.put(a, new Hashtable<Integer, Double>());
        }

        rows = forestDistance.get(a);
        if (!rows.containsKey(b)) {
            rows.put(b, 0.0);
        }
        return rows.get(b);
    }


    /**
     * This sets the value in the cell of the ForestDistance table
     */
    private void setFD(int a, int b,
                       double aValue,
                       Hashtable<Integer, Hashtable<Integer, Double>> forestDistance) {

        Hashtable<Integer, Double> rows = null;
        if (!forestDistance.containsKey(a)) {
            forestDistance.put(a, new Hashtable<Integer, Double>());
        }

        rows = forestDistance.get(a);
        rows.put(b, aValue);
    }
}