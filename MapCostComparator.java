import java.util.Comparator;
public class MapCostComparator implements Comparator<Map>{

    @Override
    public int compare(Map x, Map y){
    	return x.uCSCost < y.uCSCost ? -1 : x.uCSCost > y.uCSCost ? 1 : 0;
    }
}