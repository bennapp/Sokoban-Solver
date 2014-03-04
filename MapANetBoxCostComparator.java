import java.util.Comparator;
public class MapANetBoxCostComparator implements Comparator<Map>{

    @Override
    public int compare(Map x, Map y){
    	int xCNBC = x.cost + x.netBoxCost();
    	int yCNBC = y.cost + y.netBoxCost();
    	return xCNBC < yCNBC ? -1 : xCNBC > yCNBC ? 1 : 0;
    }
}