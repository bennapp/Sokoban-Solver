import java.io.*;
import java.util.*;

public class Map{
	public Hashtable<Coord, String> tiles = new Hashtable<Coord, String>();
	public int[] dimensions;
	public int mapHeight = 0;
	public Coord playerCoord = new Coord(-1, -1);
	public String stringVersion;
	public LinkedList<String> moves;
	public int cost;
	public int uCSCost;	
	public LinkedList<Coord> boxCoords;
	public LinkedList<Coord> targetCoords;

	Map(Hashtable<Coord, String> tiles, int[] dimensions, int mapHeight, Coord playerCoord,
	 String stringVersion, LinkedList<String> moves, int cost, int uCSCost, LinkedList<Coord> boxCoords, LinkedList<Coord> targetCoords){
		this.tiles = tiles;
		this.dimensions = dimensions;
		this.mapHeight = mapHeight;
		this.playerCoord = playerCoord;
		this.stringVersion = stringVersion;
		this.moves = moves;
		this.cost = cost;
		this.uCSCost = uCSCost;
		this.boxCoords = boxCoords;
		this.targetCoords = targetCoords;
	}

	public static Map init(String file){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			int mapHeight = Integer.parseInt(br.readLine());
			int [] dimensions = new int[mapHeight];
			Hashtable<Coord, String> tiles = new Hashtable<Coord, String>();
			Coord playerCoord = null;
			String stringVersion = "";
			LinkedList<String> moves = new LinkedList<String>();
			LinkedList<Coord> boxCoords = new LinkedList<Coord>();
			LinkedList<Coord> targetCoords = new LinkedList<Coord>();
			boolean foundPlayer = false;

			for(int i=0; i<mapHeight; i++){
				String readLine = br.readLine();
				stringVersion = stringVersion + readLine + "\n";
				String [] mapLine =  readLine.split("");
				String [] line = new String [mapLine.length-1];
				for(int k=0; k<line.length; k++){
					line[k] = mapLine[k+1];
				}
				dimensions[i] = line.length;
				for(int j=0; j<line.length; j++){
					if(line[j].equals("@") || line[j].equals("+")){
						playerCoord = new Coord(i, j);
						foundPlayer = true;
					}
					if(line[j].equals("$") || line[j].equals("*")){
						boxCoords.push(new Coord(i, j));
					}
					if(line[j].equals(".") || line[j].equals("*") || line[j].equals("+")){
						targetCoords.push(new Coord(i, j));
					}
					tiles.put(new Coord(i, j), line[j]);
				}
			}
		if (!foundPlayer){
			System.err.println("This map does not have a player and cannot be solved.");
		}
        return new Map(tiles, dimensions, mapHeight, playerCoord, stringVersion, moves, 0, 0, boxCoords, targetCoords);
		} catch (Exception e){
            e.printStackTrace();
            System.err.println("Cannot read file.");
        }
        return null;
	}

	public static Coord getMove(String move){
		Coord delta = new Coord(0, 0);
		if(move.equals("u")){
			delta.x += -1;
		}else if(move.equals("d")){
			delta.x += 1;
		}else if(move.equals("r")){
			delta.y += 1;
		}else if(move.equals("l")){
			delta.y += -1;
		}else{
			System.err.println("Ivalid move direction");
		}
		return delta;
	}

	//the normal will be positive turning clockwise. So up's normal is right, right's normal is down etc.
	public static Coord getNormalMove(String move){ 
		Coord delta = new Coord(0, 0);
		if(move.equals("u")){
			delta.y += 1;
		}else if(move.equals("d")){
			delta.y += -1;
		}else if(move.equals("r")){
			delta.x += 1;
		}else if(move.equals("l")){
			delta.x += -1;
		}else{
			System.err.println("Invalid normal move direction");
		}
		return delta;
	}
	
	public boolean validMove(String move){
		Coord delta = getMove(move);
		Coord normalDelta = getNormalMove(move);
		Coord v = new Coord(this.playerCoord.x + delta.x, this.playerCoord.y + delta.y);
		Coord v2 = new Coord(this.playerCoord.x + (2*delta.x), this.playerCoord.y + (2*delta.y));
		Coord v3 = new Coord(this.playerCoord.x + (3*delta.x), this.playerCoord.y + (3*delta.y));
		Coord v2PN = new Coord(this.playerCoord.x + (2*delta.x), this.playerCoord.y + (2*delta.y));
		Coord v2NN = new Coord(this.playerCoord.x + (2*delta.x), this.playerCoord.y + (2*delta.y));
		Coord v2PNC = new Coord(this.playerCoord.x + (2*delta.x) + normalDelta.x, this.playerCoord.y + (2*delta.y) + normalDelta.y);
		Coord v2NNC = new Coord(this.playerCoord.x + (2*delta.x) - normalDelta.x, this.playerCoord.y + (2*delta.y) - normalDelta.y);
		String nextTile = this.tiles.get(v);
		String nextNextTile = this.tiles.get(v2);
		String nextNextNextTile = this.tiles.get(v3);
		String nextNextPositiveNormalTile = this.tiles.get(v2PN);
		String nextNextNegativeNormalTile = this.tiles.get(v2NN);
		String nextNextPositiveNormalTileC = this.tiles.get(v2PNC);
		String nextNextNegativeNormalTileC = this.tiles.get(v2NNC);

		if(nextTile.equals("#")){
			return false;
		}
		if(nextTile.equals("*") || nextTile.equals("$")){ //Player must not be going towards a wall now so we can deal with a null v2
			if(nextNextTile.equals("$") || nextNextTile.equals("*") || nextNextTile.equals("#")){
				return false;
			}
			//  # <--nextNextPositiveNormalTile
			//@$ #
			//  # <--nextNextNegativeNormalTile
			if(nextNextNextTile.equals("#") && !nextNextTile.equals(".")){ 
				if(nextNextNegativeNormalTileC.equals("#") || nextNextPositiveNormalTileC.equals("#")){ //Deadlock case you are pushing a box into a corner without a target in it... You can never get it out
					return false;
				}
				//This deadlock case decrease nodesGenerated for puzzle2, sokoin2.txt from 8k nodes to 3k
					//  ##
					//   #
					//   #
					//@$ # There is no goal along the line of walls
					//   #
					//  ##
				boolean goalPositiveNormal = false;
				boolean goalNegativeNormal = false;
				boolean allWalls = true;
				Coord v3PN = new Coord(this.playerCoord.x + (3*delta.x), this.playerCoord.y + (3*delta.y)); //setting them equal to v3 at first
				Coord v3NN = new Coord(this.playerCoord.x + (3*delta.x), this.playerCoord.y + (3*delta.y));
				String nextNextNextPositiveNormalTile = "";
				String nextNextNextNegativeNormalTile = "";
				while(!nextNextPositiveNormalTile.equals("#") && !goalPositiveNormal && allWalls){
					v2PN.x += normalDelta.x;
					v2PN.y += normalDelta.y;
					v3PN.x += normalDelta.x;
					v3PN.y += normalDelta.y;
					nextNextPositiveNormalTile = this.tiles.get(v2PN);
					nextNextNextPositiveNormalTile = this.tiles.get(v3PN);
					if(nextNextPositiveNormalTile.equals(".")){
						goalPositiveNormal = true;
					}
					if(!nextNextNextPositiveNormalTile.equals("#")){
						allWalls = false;
					}
				}
				while(!nextNextNegativeNormalTile.equals("#") && !goalNegativeNormal && allWalls){
					v2NN.x -= normalDelta.x;
					v2NN.y -= normalDelta.y;
					v3NN.x -= normalDelta.x;
					v3NN.y -= normalDelta.y;
					nextNextNegativeNormalTile = this.tiles.get(v2NN);
					nextNextNextNegativeNormalTile = this.tiles.get(v3NN);
					if(nextNextNegativeNormalTile.equals(".")){
						goalNegativeNormal = true;
					}
					if(!nextNextNextNegativeNormalTile.equals("#")){
						allWalls = false;
					}
				}
				if(!goalPositiveNormal && !goalNegativeNormal && allWalls){
					return false;
				}
			
			}
		}
		return true;
	}

	public Map move(String move){
		Map movedMap = this.deepCopy();
		Coord delta = getMove(move);
		Coord v = new Coord(movedMap.playerCoord.x + delta.x, movedMap.playerCoord.y + delta.y);
		Coord v2 = new Coord(movedMap.playerCoord.x + (2*delta.x), movedMap.playerCoord.y + (2*delta.y));
		
		String currentTile = movedMap.tiles.get(movedMap.playerCoord);
		String nextTile = movedMap.tiles.get(v);
		String nextNextTile = movedMap.tiles.get(v2);

		if(currentTile.equals("@")){
			movedMap.tiles.put(movedMap.playerCoord, " ");
		} else if (currentTile.equals("+")) {
			movedMap.tiles.put(movedMap.playerCoord, ".");
		} else {
			System.err.println("standing no where");
		}

		if(nextTile.equals(" ")){ //Next tile is a space
			movedMap.tiles.put(v, "@");
		} else if(nextTile.equals("$") || nextTile.equals("*")){ //If the next tile is a box
			movedMap.uCSCost ++;
			movedMap.boxCoords.push(v2);
			movedMap.boxCoords.remove(v);
			if(nextTile.equals("$")){
				movedMap.tiles.put(v, "@");
			} else if(nextTile.equals("*")){
				movedMap.tiles.put(v, "+");
			} else {
				System.err.println("nextTile changed!");
			}

			//What about the tile after the box? is it an empty space or is a goal?
			if(nextNextTile.equals(" ")){ 
				movedMap.tiles.put(v2, "$");
			} else if (nextNextTile.equals(".")){
				movedMap.tiles.put(v2, "*");
			} else {
				System.err.println("Something weird happened pushing boxes");
			}
		} else if(nextTile.equals(".")){
			movedMap.tiles.put(v, "+");
		}
		movedMap.cost++;
		movedMap.uCSCost++;
		movedMap.updateStringVersion();
		movedMap.playerCoord = v;
		return movedMap;
	}

	public LinkedList<Map> adjEdges(){
		LinkedList<Map> edges = new LinkedList<Map>();
		String[] cart = {"u", "r", "d", "l"};
		for(int i=0; i<cart.length; i++){
			if(this.validMove(cart[i])){
				Map edge = this.move(cart[i]);
				edge.moves.add(cart[i]);
				edges.push(edge);
			}
		}
		return edges;
	}

	//For the Minimized Manhattan distance
	public int netBoxCost(){
		int netBoxCost = 0;
		int dy = 0;
		int dx = 0;
		Iterator i = this.boxCoords.iterator();
		while(i.hasNext()){
			Coord currentBox = (Coord)i.next();
			Iterator j = this.targetCoords.iterator();
			int distanceFromBox = 10000000;
			int netDistance = 0;	
			Coord currentTarget;
			while(j.hasNext()){
				currentTarget = (Coord)j.next();
				dy = currentTarget.y - currentBox.y > 0 ? currentTarget.y - currentBox.y : currentBox.y - currentTarget.y;
				dx = currentTarget.x - currentBox.x > 0 ? currentTarget.x - currentBox.x : currentBox.x - currentTarget.x;
				netDistance = dy + dx;
				if(netDistance < distanceFromBox){
					distanceFromBox = netDistance;
				}
			}
			netBoxCost += distanceFromBox;
		}
		return netBoxCost;
	}

	public int targetsLeft(){
		return this.targetCoords.size() - (this.stringVersion.split("\\*").length -1);
	}

	public boolean isSolved(){
		return ! (this.tiles.contains(".") || this.tiles.contains("+"));
	}

	public void print(){
		for(int i=0; i<this.mapHeight; i++){
        	for(int j=0; j<this.dimensions[i]; j++){
        		System.out.print(this.tiles.get(new Coord(i, j)));
        	}
        	System.out.println();
        }
	}


	public Map deepCopy(){
		int[] dimensions;
		int mapHeight = 0;
		Coord playerCoord = new Coord(-1, -1);
		Hashtable<Coord, String> tiles = new Hashtable<Coord, String>();
		String stringVersion;
		
		int cost;
		int uCSCost;
        for (Enumeration<Coord> e = this.tiles.keys(); e.hasMoreElements();){
        	Coord coord = e.nextElement();
        	tiles.put(coord, this.tiles.get(coord));
        }
        @SuppressWarnings("unchecked")
        LinkedList<String> moves = (LinkedList<String>)this.moves.clone();
        @SuppressWarnings("unchecked")
        LinkedList<Coord> boxCoords = (LinkedList<Coord>)this.boxCoords.clone();
        @SuppressWarnings("unchecked")
        LinkedList<Coord> targetCoords = (LinkedList<Coord>)this.targetCoords.clone();

		stringVersion = new String(this.stringVersion);
		dimensions = this.dimensions;
		mapHeight = this.mapHeight;
		playerCoord = this.playerCoord;
		cost = this.cost;
		uCSCost = this.uCSCost;

		return new Map(tiles, dimensions, mapHeight, playerCoord, stringVersion, moves, cost, uCSCost, boxCoords, targetCoords);
	}

	public void updateStringVersion(){
		String updatedStringVersion = "";
    	for(int i=0; i<mapHeight; i++){
    		for(int j=0; j<this.dimensions[i]; j++){
       			updatedStringVersion = updatedStringVersion + this.tiles.get(new Coord(i, j));
    		}
    	}
    	this.stringVersion = updatedStringVersion;
    }

    public String prettyEdges(){
    	String prettyEdges = "";
    	for(int i=0; i<this.moves.size(); i++){
    		prettyEdges = prettyEdges + ", " +  this.moves.get(i);
    	}

    	return prettyEdges.substring(2);
    }

	@Override public boolean equals(Object other){
		if (other instanceof Map) {
			Map that = (Map) other;
			return this.stringVersion.equals(that.stringVersion);
		}
		return false;
	}
}