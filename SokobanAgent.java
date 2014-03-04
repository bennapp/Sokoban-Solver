import java.io.*;
import java.util.*;

public class SokobanAgent{

	public static void print(Object stringOrMore){
		System.out.println(stringOrMore);
	}

	public static Solution bFS(Map map){
		long startTime = System.currentTimeMillis();
		int nodesGenerated = 1;
		int uniqueNodes = 1;
		LinkedList<Map> mapStack = new LinkedList<Map>();
		Set<String> visted = new HashSet<String>();
		long endTime;
		mapStack.add(map);
		visted.add(map.stringVersion);
		while(!mapStack.isEmpty()){
			Map temp = mapStack.pop();
			LinkedList<Map> maps = temp.adjEdges();
			int i = 0;
			nodesGenerated += maps.size();
			while(!maps.isEmpty()){
				Map adjTemp = maps.pop();
				if(! visted.contains(adjTemp.stringVersion)){
					uniqueNodes++;
					visted.add(adjTemp.stringVersion);
					mapStack.add(adjTemp);
					if(adjTemp.isSolved()){
						endTime = System.currentTimeMillis();
						return new Solution(adjTemp, nodesGenerated, (nodesGenerated - uniqueNodes), mapStack.size(), uniqueNodes, (endTime - startTime));
					}
				}
			}
		}
		endTime = System.currentTimeMillis();
		return new Solution(null, nodesGenerated, (nodesGenerated - uniqueNodes), mapStack.size(), uniqueNodes, (endTime - startTime));
	}

	public static Solution dFS(Map m){
		long startTime = System.currentTimeMillis();
		Set<String> discovered = new HashSet<String>();
		Set<String> explored = new HashSet<String>();
		LinkedList<Map> mapStack = new LinkedList<Map>();
		int nodesGenerated = 1;
		int uniqueNodes = 1;
		long endTime;
		discovered.add(m.stringVersion);
		mapStack.add(m);
		while(!mapStack.isEmpty()){
			Map temp = mapStack.pop();
			while(explored.contains(temp.stringVersion)){
				temp = mapStack.pop();
			}
			if(temp.isSolved()){
				endTime = System.currentTimeMillis();
				return new Solution(temp, nodesGenerated, (nodesGenerated - uniqueNodes), mapStack.size(), explored.size(), (endTime - startTime));
			}
			LinkedList<Map> maps = temp.adjEdges();
			nodesGenerated += maps.size();
			explored.add(temp.stringVersion);
			while(!maps.isEmpty()){
				Map adjTemp = maps.removeLast();
				if( !(discovered.contains(adjTemp.stringVersion)) ){
					uniqueNodes += 1;
					mapStack.add(adjTemp);
					discovered.add(adjTemp.stringVersion);
				}
			}
		}
		endTime = System.currentTimeMillis();
		return new Solution(null, nodesGenerated, (nodesGenerated - uniqueNodes), mapStack.size(), explored.size(), (endTime - startTime)); 
	}

	public static Solution uCS(Map m){
		long startTime = System.currentTimeMillis();
		Set<String> explored = new HashSet<String>();
		Comparator<Map> comparator = new MapCostComparator();
		PriorityQueue<Map> frontier = new PriorityQueue<Map>(10000000, comparator); //must initialize max capacity when overriding comparator.
		int nodesGenerated = 1;
		int prevNodes = 0;
		long endTime;
		frontier.add(m);
		while(true){
			if(frontier.isEmpty()){
				endTime = System.currentTimeMillis();
				return new Solution(null, nodesGenerated, prevNodes, frontier.size(), explored.size(), (endTime - startTime));
			}
			Map temp = frontier.peek();
			frontier.remove(temp);
			if(temp.isSolved()){
				endTime = System.currentTimeMillis();
				return new Solution(temp, nodesGenerated, prevNodes, frontier.size(), explored.size(), (endTime - startTime));
			}
			explored.add(temp.stringVersion);
			LinkedList<Map> adjEdges = temp.adjEdges();
			nodesGenerated += adjEdges.size();
			while(!adjEdges.isEmpty()){
				Map adjEdge = adjEdges.pop();
				if(!(explored.contains(adjEdge.stringVersion) || frontier.contains(adjEdge))){
					frontier.add(adjEdge);
				} else if(frontier.contains(adjEdge)){
					prevNodes++;
					Iterator i = frontier.iterator();
					Map adjEdgeInFront = null;
					while(i.hasNext()){
						adjEdgeInFront = (Map)i.next();
						if(adjEdge.stringVersion.equals(adjEdgeInFront.stringVersion)){
							break;
						}
					}
					if(adjEdgeInFront.uCSCost > adjEdge.uCSCost){
						frontier.remove(adjEdgeInFront);
						frontier.add(adjEdge);
					}
				}
			}
		}
	}

	public static Solution gBFS(Map m, boolean quick){
		long startTime = System.currentTimeMillis();
		Set<String> discovered = new HashSet<String>();
		Set<String> explored = new HashSet<String>();
		LinkedList<Map> mapStack = new LinkedList<Map>();
		int nodesGenerated = 1;
		int uniqueNodes = 1;
		long endTime;
		discovered.add(m.stringVersion);
		mapStack.add(m);
		while(!mapStack.isEmpty()){
			Map temp = mapStack.pop();
			while(explored.contains(temp.stringVersion)){
				temp = mapStack.pop();
			}
			if(temp.isSolved()){
				endTime = System.currentTimeMillis();
				return new Solution(temp, nodesGenerated, (nodesGenerated - uniqueNodes), mapStack.size(), explored.size(), (endTime - startTime));
			}
			LinkedList<Map> maps = temp.adjEdges();
			nodesGenerated += maps.size();
			explored.add(temp.stringVersion);
			while(!maps.isEmpty()){
					Iterator i = maps.iterator();
					Map greedyPick = maps.peek();
					while(i.hasNext()){
						Map nextMap = (Map)i.next();
						if(quick){
							if(greedyPick.targetsLeft() > nextMap.targetsLeft()){ //Picks the highest box cost to add to the bottom of the stack, so the lowest boxcost is at the top
								greedyPick = nextMap;
							}
						} else {
							if(greedyPick.netBoxCost() > nextMap.netBoxCost()){ 
								greedyPick = nextMap;
							}
						}
					}
					maps.remove(greedyPick);
				if(!(discovered.contains(greedyPick.stringVersion))){
					uniqueNodes += 1;
					mapStack.add(greedyPick);
					discovered.add(greedyPick.stringVersion);
				}
			}
		}
		endTime = System.currentTimeMillis();
		return new Solution(null, nodesGenerated, (nodesGenerated - uniqueNodes), mapStack.size(), explored.size(), (endTime - startTime)); 
	}


	public static Solution aStar(Map m, boolean quick){
		long startTime = System.currentTimeMillis();
		HashSet<String> explored = new HashSet<String>();
		Comparator<Map> comparator;
		if(quick){
			comparator = new MapATargetsLeftComparator();
		} else {
			comparator = new MapANetBoxCostComparator();
		}
		PriorityQueue<Map> frontier = new PriorityQueue<Map>(10000000, comparator); //must initialize max capacity when overriding comparator... bogus.
		Set<String> frontierMirror = new HashSet<String>();
		int nodesGenerated = 1;
		int prevNodes = 0;
		long endTime;
		frontier.add(m);
		frontierMirror.add(m.stringVersion);
		while(true){
			if(frontier.isEmpty()){
				endTime = System.currentTimeMillis();
				return new Solution(null, nodesGenerated, prevNodes, frontier.size(), explored.size(), (endTime - startTime));
			}
			Map temp = frontier.peek();
			frontier.remove(temp);
			frontierMirror.remove(temp.stringVersion);
			if(temp.isSolved()){
				endTime = System.currentTimeMillis();
				return new Solution(temp, nodesGenerated, prevNodes, frontier.size(), explored.size(), (endTime - startTime));
			}
			explored.add(temp.stringVersion);
			LinkedList<Map> adjEdges = temp.adjEdges();
			nodesGenerated += adjEdges.size();
			while(!adjEdges.isEmpty()){
				Map adjEdge = adjEdges.pop();
				if(!(explored.contains(adjEdge.stringVersion) || frontierMirror.contains(adjEdge.stringVersion))){
					frontier.add(adjEdge);
					frontierMirror.add(adjEdge.stringVersion);
				} else if(frontier.contains(adjEdge)){
					prevNodes++;
					Iterator i = frontier.iterator();
					Map adjEdgeInFront = null;
					boolean seekingMap = true;
					while(i.hasNext()){
						adjEdgeInFront = (Map)i.next();
						if(adjEdge.stringVersion.equals(adjEdgeInFront.stringVersion)){
							break;
						}
					}
					if(quick){
						if((adjEdgeInFront.cost + adjEdgeInFront.targetsLeft()) > (adjEdge.cost + adjEdge.targetsLeft())){
							frontier.remove(adjEdgeInFront);
							frontier.add(adjEdge);
							frontierMirror.remove(adjEdgeInFront.stringVersion);
							frontierMirror.add(adjEdge.stringVersion);
						}
					} else {
						if((adjEdgeInFront.cost + adjEdgeInFront.netBoxCost()) > (adjEdge.cost + adjEdge.netBoxCost())){
							frontier.remove(adjEdgeInFront);
							frontier.add(adjEdge);
							frontierMirror.remove(adjEdgeInFront.stringVersion);
							frontierMirror.add(adjEdge.stringVersion);
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		if(args.length != 0 && args[0].equalsIgnoreCase("test")){
			String output = "";
			for(int i=1; i<11; i++){
				output += "All Algorithms for puzzle: " + i + "\n";
				Map map = Map.init("sokotest"+ i +".txt");
				output += map.stringVersion + "\n";
				Solution solution;
				output += "BFS:";
				solution = bFS(map);
				output += solution.toString();
				output += "DFS:";
				solution = dFS(map);
				output += solution.toString();
				output += "UCS:";
				solution = uCS(map);
				output += solution.toString();
				output += "GBFS w/ q:";
				solution = gBFS(map, true);
				output += solution.toString();
				output += "GBFS w/ m:";
				solution = gBFS(map, false);
				output += solution.toString();
				output += "A* w/ q:";
				solution = aStar(map, true);
				output += solution.toString();
				output += "A* w/ m:";
				solution = aStar(map, false);
				output += solution.toString();
				output += "\n";
				try{
					PrintWriter out = new PrintWriter(new FileWriter("output.txt"), true);
      				out.write(output);
      				out.close();
				} catch (IOException e) {
					System.err.println("FileWriter error");
					System.err.println(e);
				}
			}
				print("Check output.txt");
		} else {
			String userInput;
			print("");
			print("Welcome to Sokoban Puzzle Solver");
			print("--------------------------------");
			print("Enter a puzzle number and a search algorithm");
			print("eg.");
			print("sokoin0.txt BFS");
			print("sokoin2.txt A* q");
			print("");
			print("Type puzzles to show all puzzels");
			print("Type algorithms to show all algorithms");
			print("Type file and then the file name to input any puzzle");
			print("Type script to run all Algorithms on all puzzles");
			print("Puzzles should be valid Sokoban puzzles");
			print("--closed walls with equal or less targets than boxes");
			print("");
	
			try{
				while((userInput = stdIn.readLine()) != null){
					Map userMap;
					Solution userSolution = null;
					String userAlg = "";
					String userMapString = "";
				if(userInput.equalsIgnoreCase("algorithms") || 
					userInput.equals("puzzles") || userInput.startsWith("file")|| userInput.startsWith("script")){
					if(userInput.equalsIgnoreCase("algorithms")){
						print("BFS -- Breadth First Search");
						print("DFS -- Depth First Search");
						print("UCS -- Uniform Cost Search");
						print("GBFS Q -- Greedy Best First Solution Search with quick heuristic");
						print("A* Q -- A* Search with quick heuristic");
						print("GBFS M -- Greedy Best First Solution Search with Minimized Manhattan heuristic");
						print("A* M -- A* Search with Minimized Manhattan heuristic");
						print("");
					}
					if(userInput.equals("puzzles")){
						print("Puzzle sokoin0.txt:");
						Map map0 = Map.init("sokoin0.txt");
						map0.print();
						print("");
						print("Puzzle sokoin1.txt:");
						Map map1 = Map.init("sokoin1.txt");
						map1.print();
						print("");
						print("Puzzle sokoin2.txt:");
						Map map2 = Map.init("sokoin2.txt");
						map2.print();
						print("");
					}
					if(userInput.startsWith("file")){
						String file = userInput.substring(5);
						userMap = Map.init(file);
						userMap.print();
						print("Now enter in a puzzle and a algorithm");
						print("eg. " + file + " a* q");
					}
					if(userInput.equalsIgnoreCase("script")){
						for(int i=0; i<3; i++){
							print("All Algorithms for puzzle: " + i);
							Map map = Map.init("sokoin"+ i +".txt");
							map.print();
							Solution solution;
							print("BFS:");
							solution = bFS(map);
							solution.print();
							print("DFS:");
							solution = dFS(map);
							solution.print();
							//print("UCS:");
							//solution = uCS(map);
							//solution.print();
							print("GBFS w/ q:");
							solution = gBFS(map, true);
							solution.print();
							print("GBFS w/ m:");
							solution = gBFS(map, false);
							solution.print();
							print("A* w/ q:");
							solution = aStar(map, true);
							solution.print();
							print("A* w/ m:");
							solution = aStar(map, false);
							solution.print();
							print("");
						}
					}
				} else {
					String[] userInputs = userInput.split(" ");
					String userHeuristic = "";
					userMapString = userInputs[0];
					userAlg = userInputs[1];
					if (userInputs.length == 3){
						userHeuristic = userInputs[2];
					}
					userMap = Map.init(userMapString);
					if(userAlg.equalsIgnoreCase("bfs")){
						userSolution = bFS(userMap);
						print("Breadth First Search Solution:");
					}
					if(userAlg.equalsIgnoreCase("dfs")){
						print("Depth First Solution:");
						userSolution = dFS(userMap);
					}
					if(userAlg.equalsIgnoreCase("ucs")){
						print("Uniform Cost Search Solution:");
						userSolution = uCS(userMap);
					}
					if(userAlg.equalsIgnoreCase("gbfs") && userHeuristic.equalsIgnoreCase("m")){
						print("Greedy Best First Search Solution with Minimized Manhattan heuristic:");
						userSolution = gBFS(userMap, false);
					}
					if(userAlg.equalsIgnoreCase("gbfs") && userHeuristic.equalsIgnoreCase("q")){
						print("Greedy Best First Search Solution with quick heuristic:");
						userSolution = gBFS(userMap, true);
					}
					if(userAlg.equalsIgnoreCase("a*") && userHeuristic.equalsIgnoreCase("m")){
						print("A* Search Solution with Minimized Manhattan heuristic:");
						userSolution = aStar(userMap, false);
					}
					if(userAlg.equalsIgnoreCase("a*") && userHeuristic.equalsIgnoreCase("q")){
						print("A* Search Solution with quick heuristic:");
						userSolution = aStar(userMap, true);
					}
					print("");
					userSolution.print();
				}
			}
	
			} catch (IOException e) {
				System.err.println("Invalid input.");
				System.err.println(e);
			}	
		}
						

////////TESTING:
// 		Map map = Map.init("sokotest.txt");
// 		map.print();
// 		print(map.netBoxCost());

//		//TEST the output of puzzle1
//		Map map_test = Map.init("sokoin1.txt");
//		String[] moves = { "d", "l", "u", "r", "r", "r", "d", "l", "u", "l", "l", "d", "d", "r", "u", "l", "u", "r", "u", "u", "l", "d", "r", "d", "d", "r", "r", "u", "l", "d", "l", "u", "u"};
//		map_test.print();
//		for(int i =0; i<moves.length; i++){
//			map_test = map_test.move(moves[i]);
//			print(moves[i]);
//			map_test.print();
//			print("");
//			print("");
//		}

		//Test aStar
//		print("aStar:");
//		Solution solvedaStar = aStar(map);
//		solvedaStar.print();
//		Map solvedaStarMap = solvedaStar.map;
//		solvedaStarMap.print();
//		print("nodesGenerated: " + solvedaStar.nodesGenerated);
//		print("prevNodes: " + solvedaStar.prevNodes);
//		print("fringeNodes: " + solvedaStar.fringeNodes);
//		print("exploredNodes: " + solvedaStar.exploredNodes);
//		print("runTime: " + (double)solvedaStar.runTime/1000);
//		print("edges: " + solvedaStar.map.prettyEdges());
//		print("cost: " + solvedaStar.map.cost);
//		print("");

		//Test gBFS
//		print("gBFS:");
//		Solution solvedgBFS = gBFS(map);
//		Map solvedgBFSMap = solvedgBFS.map;
//		solvedgBFSMap.print();
//		print("nodesGenerated: " + solvedgBFS.nodesGenerated);
//		print("prevNodes: " + solvedgBFS.prevNodes);
//		print("fringeNodes: " + solvedgBFS.fringeNodes);
//		print("exploredNodes: " + solvedgBFS.exploredNodes);
//		print("runTime: " + (double)solvedgBFS.runTime/1000);
//		print("edges: " + solvedgBFS.map.prettyEdges());
//		print("cost: " + solvedgBFS.map.cost);
//		print("");

//		//Test uCS
//		System.out.println("uCS:");
//		Solution solveduCS = uCS(map);
//		Map solveduCSMap = solveduCS.map;
//		print("nodesGenerated: " + solveduCS.nodesGenerated);
//		print("prevNodes: " + solveduCS.prevNodes);
//		print("fringeNodes: " + solveduCS.fringeNodes);
//		System.out.println("exploredNodes: " + solveduCS.exploredNodes);
//		print("runTime: " + (double)solveduCS.runTime/1000);
//		print("edges: " + solveduCS.map.prettyEdges());
//		print("cost: " + solveduCS.map.cost);
//		solveduCSMap.print();


		//Test dFS
//		System.out.println("dFS:");
//		Solution solveddFS = dFS(map);
		// Map solveddFSMap = solveddFS.map;
//		System.out.println("nodesGenerated: " + solveddFS.nodesGenerated);
//		System.out.println("prevNodes: " + solveddFS.prevNodes);
//		System.out.println("fringeNodes: " + solveddFS.fringeNodes);
//		System.out.println("exploredNodes: " + solveddFS.exploredNodes);
//		System.out.println("runTime: " + (double)solveddFS.runTime/1000);
//		System.out.println("edges: " + solveddFS.map.prettyEdges());
//		System.out.println("cost: " + solveddFS.map.cost);
//		solveddFSMap.print();

//		// Test bFS
//		System.out.println("bFS:");
//		Solution solved = bFS(map);
//		Map solvedMap = solved.map;
//		solvedMap.print();
//		System.out.println("nodesGenerated: " + solved.nodesGenerated);
//		System.out.println("prevNodes: " + solved.prevNodes);
//		System.out.println("fringeNodes: " + solved.fringeNodes);
//		System.out.println("exploredNodes: " + solved.exploredNodes);
//		System.out.println("runTime: " + (double)solved.runTime/1000);
//		System.out.println("edges: " + solved.map.prettyEdges());
		
//		map = map.move("u");
//		map.print();
//		print(map.cost);
//		print("");
//
//		map = map.move("d");
//		map.print();
//		print(map.cost);
//		print("");
//
//		map = map.move("d");
//		map.print();
//		print(map.cost);
//		print("");
//
//		map = map.move("r");
//		map.print();
//		print(map.cost);
//		print("");
		
		////Test stringVersion
		//System.out.println(map.stringVersion);

		////Test adjEdges
		//LinkedList<Map> maps = adjEdges(map);
		//while(!maps.isEmpty()){
		//	print(maps.pop());
		//}

		//Test move, solved first puzzle :)
		//map.print();
		//Map movedMap = moveMap(map, "r");
		//movedMap.print();
		//movedMap = moveMap(movedMap, "r");
		//movedMap.print();
		//movedMap = moveMap(movedMap, "d");
		//movedMap.print();
		//movedMap = moveMap(movedMap, "l");
		//movedMap.print();
		//movedMap = moveMap(movedMap, "d");
		//movedMap.print();
		//movedMap = moveMap(movedMap, "l");
		//movedMap.print();
		//movedMap = moveMap(movedMap, "u");
		//movedMap.print();
		//movedMap = moveMap(movedMap, "u");
		//movedMap.print();
		//movedMap = moveMap(movedMap, "u");
		//movedMap.print();
		//System.out.println("is solved? : " + isSolved(movedMap));

		////Test validMove
		// System.out.println(validMove(map, "u"));
		// System.out.println(validMove(map, "r"));
		// System.out.println(validMove(map, "d"));
		// System.out.println(validMove(map, "l"));
		// System.out.println();

		//// Tests if two Maps are equal works
		// Map map2 = init();
		// System.out.println(map.equals(map2));
		// System.out.println(map2.tiles.get(new Coord(4,3)));
		// map2.tiles.put(new Coord(4,3), " ");
		// map2.tiles.put(new Coord(5,3), "$");
		// print(map2);
		// System.out.println(map.equals(map2));

		//// Tests if map is a winner
		//Map map2 = init();
		//System.out.println(isSolved(map2));
		//System.out.println(map2.tiles.get(new Coord(1,2)));
		//map2.tiles.put(new Coord(1,2), "*");
		//map2.tiles.put(new Coord(3,2), " ");
		//print(map2);
		//System.out.println(isSolved(map2));
	}
}