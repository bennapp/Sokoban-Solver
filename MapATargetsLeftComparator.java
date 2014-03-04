import java.util.Comparator;
public class MapATargetsLeftComparator implements Comparator<Map>{

    @Override
    public int compare(Map x, Map y){
    	int xCNBC = x.cost + x.targetsLeft();
    	int yCNBC = y.cost + y.targetsLeft();
    	return xCNBC < yCNBC ? -1 : xCNBC > yCNBC ? 1 : 0;
    }
}