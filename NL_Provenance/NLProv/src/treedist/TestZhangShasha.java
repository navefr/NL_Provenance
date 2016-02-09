package treedist;

/*
 * INSERT-LICENCE-INFO
 */
public class TestZhangShasha {

    public static void main(String argv []) throws java.io.IOException  {
//        TreeDefinition aTree = CreateTreeHelper.makeTree("a-b;a-c;c-d;c-e;c-f;");
//        System.out.println("The tree is: \n"+aTree);
//        TreeDefinition bTree = CreateTreeHelper.makeTree("x-a;a-b;a-c;a-r;r-y;c-d;c-l;c-g;");
//        System.out.println("The tree is: \n"+bTree);

        TreeDefinition aTree = CreateTreeHelper.makeTree("a-b;a-c;");
        System.out.println("The tree is: \n"+aTree);
        TreeDefinition bTree = CreateTreeHelper.makeTree("x-a;a-b;a-d;");
        System.out.println("The tree is: \n"+bTree);


        ComparisonZhangShasha treeCorrector = new ComparisonZhangShasha();
        OpsZhangShasha costs = new OpsZhangShasha();
        Transformation transform = treeCorrector.findDistance(aTree, bTree, costs);
        System.out.println("Distance: "+ transform.getCost());

    }
}