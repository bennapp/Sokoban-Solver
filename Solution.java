import java.util.List;

public class Solution{
	public Map map;
	public int nodesGenerated;
	public int prevNodes;
	public int fringeNodes;
	public int exploredNodes;
	public long runTime;

	Solution(Map map, int nodesGenerated, int prevNodes, int fringeNodes, int exploredNodes, long runTime){
		this.map = map;
		this.nodesGenerated = nodesGenerated;
		this.prevNodes = prevNodes;
		this.fringeNodes = fringeNodes;
		this.exploredNodes = exploredNodes;
		this.runTime = runTime;
	}

	public String toString(){
		String output = "";
		output += "nodesGenerated: " + this.nodesGenerated;
		output += "\nprevNodes: " + this.prevNodes;
		output += "\nfringeNodes: " + this.fringeNodes;
		output += "\nexploredNodes: " + this.exploredNodes;
		output += "\nrunTime: " + (double)this.runTime/1000;
		
		if(this.map == null){
			output += "\nNo Solution Found";
		} else {
			output += "\nedges: " + this.map.prettyEdges();
			output += "\ncost: " + this.map.cost;
			output += "\n";
		}
		return output;
	}

	public void print(){
		System.out.println("nodesGenerated: " + this.nodesGenerated);
		System.out.println("prevNodes: " + this.prevNodes);
		System.out.println("fringeNodes: " + this.fringeNodes);
		System.out.println("exploredNodes: " + this.exploredNodes);
		System.out.println("runTime: " + (double)this.runTime/1000);
		
		if(this.map == null){
			System.out.println("No Solution Found");
		} else {
			System.out.println("edges: " + this.map.prettyEdges());
			System.out.println("cost: " + this.map.cost);
			//this.map.printMap();
		}
			System.out.println("");
	}
}