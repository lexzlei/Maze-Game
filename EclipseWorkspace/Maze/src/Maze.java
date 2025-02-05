import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents nodes of the maze
class MazeNode {
  Edge right;
  Edge bottom;
  Edge left;
  Edge top;
  boolean rightBlocked;
  boolean downBlocked;
  int x;
  int y;
  boolean seen;

  MazeNode(int x, int y) {
    this(x, y, null, null, null, null);
  }

  MazeNode(int x, int y, Edge right, Edge left, Edge top, Edge bottom) {
    this.x = x;
    this.y = y;
    this.rightBlocked = true;
    this.downBlocked = true;
    this.seen = false;
    this.right = right;
    this.left = left;
    this.top = top;
    this.bottom = bottom;
  }

  // draws the walls of the maze
  WorldImage drawEdge(int xSize, int ySize) {
    return new RectangleImage(xSize, ySize, OutlineMode.SOLID, Color.gray);
  }

  // checks if the given object is same as this node
  public boolean equals(Object that) {
    if (!(that instanceof MazeNode)) {
      return false;
    }
    else {
      MazeNode v = (MazeNode) that;
      return this.x == v.x && this.y == v.y;
    }
  }

  //produces a hashcode for this node
  public int hashCode() {
    return this.x * this.y * 100;
  }
}

// represents the path of the maze
class Edge implements Comparable<Edge> {
  MazeNode from;
  MazeNode to;
  int weight;

  Edge(MazeNode from, MazeNode to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  Edge(int weight) {
    this.weight = weight;
  }

  Edge(MazeNode from, MazeNode to) {
    this.from = from;
    this.to = to;
  }

  // compares the weights of this edge and the given one
  public int compareTo(Edge that) {
    return this.weight - that.weight;
  }
}

// represents the maze
class MazeWorld extends World {
  MazeNode player; 
  ArrayList<MazeNode> travelled; 
  LinkedList<MazeNode> searched; 
  LinkedList<MazeNode> work; 
  LinkedList<MazeNode> path; 
  MazeNode key;
  boolean finished; 
  boolean showPaths = true; 
  ArrayList<ArrayList<MazeNode>> vertices; 
  ArrayList<Edge> edges; 
  ArrayList<Edge> worklist; 
  HashMap<MazeNode, MazeNode> map;
  HashMap<MazeNode, MazeNode> searchMap;
  int hScale = 100; 
  int vScale = 100; 
  SearchAdd<MazeNode> addMethod; 
  int height;
  int width;
  int nodeSize;
  boolean searching = false; 
  Random rand;

  MazeWorld(int width, int height) {
    if (width < 2 || height < 2) {
      throw new IllegalArgumentException("Invalid board size");
    }
    this.rand = new Random();
    this.width = width;
    this.height = height;
    this.nodeSize = 10;
    this.vertices = this.makeNodes();
    this.key = this.vertices.get(0).get(0);
    this.edges = new ArrayList<Edge>();
    this.worklist = this.initEdges(this.vertices);
    this.map = new HashMap<MazeNode, MazeNode>();
    this.travelled = new ArrayList<MazeNode>();
    this.searched = new LinkedList<MazeNode>();
    this.work = new LinkedList<MazeNode>();
    this.work.add(this.vertices.get(0).get(0));
    this.path = new LinkedList<MazeNode>();
    this.finished = false;
    this.krusAlg();
    this.setEdges(vertices);
    this.searchMap = new HashMap<MazeNode, MazeNode>();

  }

  // constructor with seed
  MazeWorld(int width, int height, Random rand) {
    this(width, height);
    this.rand = rand;
  }

  // makes the maze
  public ArrayList<ArrayList<MazeNode>> makeNodes() {
    ArrayList<ArrayList<MazeNode>> temp = new ArrayList<ArrayList<MazeNode>>();
    for (int i = 0; i < height; i++) {
      ArrayList<MazeNode> vertexList = new ArrayList<MazeNode>();
      for (int j = 0; j < width; j++) {
        vertexList.add(new MazeNode(j, i));
      }
      temp.add(vertexList);
    }
    this.player = temp.get(0).get(0);
    return temp;
  }

  // makes the path 
  public ArrayList<Edge> initEdges(ArrayList<ArrayList<MazeNode>> vert) {
    ArrayList<Edge> arr = new ArrayList<Edge>();

    for (int j = 0; j < this.height; j++) {
      for (int i = 0; i < this.width; i++) {
        MazeNode current = vert.get(j).get(i);

        if (i < this.width - 1) { 
          current.right = new Edge(current, vert.get(j).get(i + 1));
          current.right.weight = this.rand.nextInt(this.vScale);
          arr.add(current.right);
        }
        else { 
          current.right = new Edge(current, null);
        }

        if (i > 0) { 
          current.left = vert.get(j).get(i - 1).right;
        }
        else {
          current.left = new Edge(null, current);
        }

        if (j < this.height - 1) { 
          current.bottom = new Edge(current, vert.get(j + 1).get(i));
          current.bottom.weight = new Random().nextInt(this.hScale);
          arr.add(current.bottom);
        }
        else { 
          current.bottom = new Edge(current, null);
        }

        if (j > 0) {
          current.top = vert.get(j - 1).get(i).bottom;
        }
        else {
          current.top = new Edge(null, current);
        }
      }
    }
    return arr;
  }

  // EFFECT: creates the MST
  public void krusAlg() {
    Collections.sort(worklist);
    this.map = this.createHash();
    while (!this.treeDone() && this.worklist.size() > 0) {
      Edge edge = this.worklist.get(0);
      MazeNode to = this.find(this.map, edge.to);
      MazeNode from = this.find(this.map, edge.from);
      if (this.find(map, to).equals(this.find(map, from))) {
        this.worklist.remove(0);
      }
      else {
        map.put(this.find(map, to), this.find(map, from));

        this.edges.add(this.worklist.remove(0));
      }
    }
  }

  // finds a representative given a key
  MazeNode find(HashMap<MazeNode, MazeNode> map, MazeNode key) {
    MazeNode val = map.get(key);
    while (!map.get(val).equals(val)) {
      val = map.get(val);
    }
    return map.get(val);
  }

  // EFFECT: changes whether node has walls on right or bottom
  public void setEdges(ArrayList<ArrayList<MazeNode>> vert) {
    for (ArrayList<MazeNode> array : vert) {
      for (MazeNode vertex : array) {
        if (this.edges.contains(vertex.right)) {
          vertex.rightBlocked = false;
        }
        if (this.edges.contains(vertex.bottom)) {
          vertex.downBlocked = false;
        }
      }
    }
  }

  // initialized the hashmap so all reps are themselves
  public HashMap<MazeNode, MazeNode> createHash() {
    for (MazeNode v : this.flattenArray(this.vertices)) {
      map.put(v, v);
    }
    return map;
  }

  // checks if MST is done
  public boolean treeDone() {
    return (height * width) - 1 == this.edges.size();
  }

  // does BFS or DFS one node a time
  void search() {
    MazeNode end = this.vertices.get(this.height - 1).get(this.width - 1);
    MazeNode current = this.work.remove();
    if (current != end) {
      this.searched.add(current);

      // checks the neighbors of the current search node
      if (current.left.from != null 
          && !this.searchMap.containsKey(current.left.from)
          && !current.left.from.rightBlocked) {
        this.searchMap.put(current.left.from, current);
        this.addMethod.add(this.work, current.left.from);
      } 
      if (current.top.from != null 
          && !this.searchMap.containsKey(current.top.from)
          && !current.top.from.downBlocked) {
        this.searchMap.put(current.top.from, current);
        this.addMethod.add(this.work, current.top.from);
      }
      if (current.right.to != null 
          && !this.searchMap.containsKey(current.right.to)
          && !current.rightBlocked) {
        this.searchMap.put(current.right.to, current);
        this.addMethod.add(this.work, current.right.to);
      }
      if (current.bottom.to != null 
          && !this.searchMap.containsKey(current.bottom.to)
          && !current.downBlocked) {
        this.searchMap.put(current.bottom.to, current);
        this.addMethod.add(this.work, current.bottom.to);
      }
    } 
    // correct path will be drawn
    else { 
      this.finished = true;
      this.searching = false;
      this.backPath(current);
    }
  }

  // goes back to start with shortest path
  void backPath(MazeNode current) {
    this.path = new LinkedList<MazeNode>();
    path.addFirst(current);
    while (current != this.vertices.get(0).get(0)) {
      current = this.searchMap.get(current);
      path.addFirst(current);
    }
  }

  // processes user inputs and affects the game 
  public void onKeyEvent(String k) {
    if (k.equals("right") && !this.player.rightBlocked && this.player.x < this.width - 1) {
      this.movePlayer(this.player.y, this.player.x + 1);
    }
    if (k.equals("down") && !this.player.downBlocked && this.player.y < this.height - 1) {
      this.movePlayer(this.player.y + 1, this.player.x);
    }
    if (k.equals("left") && this.player.x > 0 && this.player.left.from != null
        && !this.player.left.from.rightBlocked) {
      this.movePlayer(this.player.y, this.player.x - 1);
    }
    if (k.equals("up") && this.player.y > 0 && this.player.top.from != null
        && !this.player.top.from.downBlocked) {
      this.movePlayer(this.player.y - 1, this.player.x);
    }
    if (k.equals("r")) { // r for rest
      this.newMaze();
    }
    if (k.equals("n")) { // n to go back to normal weights
      this.hScale = 100;
      this.vScale = 100;
      this.newMaze();
    }
    if (k.equals("b")) { // b for bfs
      this.resetSearch();
      this.addMethod = new BfsAdd<MazeNode>();
    }
    if (k.equals("d")) { // d for dfs
      this.resetSearch();
      this.addMethod = new DfsAdd<MazeNode>();
    }
    if (k.equals("s")) { // p to toggle on and off the path showing
      this.showPaths = !this.showPaths;
    }
    if (k.equals("h")) { // h to create a maze with mostly horizontal edges
      this.hScale = 100;
      this.vScale = 10;
      this.newMaze();
    }
    if (k.equals("v")) { // v to create a maze with mostly vertical edges
      this.hScale = 10;
      this.vScale = 100;
      this.newMaze();
    }
  }

  // increments the world per tick
  public void onTick() {
    if (this.searching) {
      this.search();
    } 
  }

  // moves the player to given coordinates 
  void movePlayer(int y, int x) {
    this.travelled.add(player);
    this.player = this.vertices.get(y).get(x);
  }

  // resets the maze
  void newMaze() {
    this.vertices = this.makeNodes();
    this.key = this.vertices.get(0).get(0);
    this.edges = new ArrayList<Edge>();
    this.worklist = this.initEdges(this.vertices);
    this.map = new HashMap<MazeNode, MazeNode>();
    this.travelled = new ArrayList<MazeNode>();
    this.resetSearch();
    this.searching = false;
    this.krusAlg();
    this.setEdges(vertices);
  }

  // resets the search path 
  void resetSearch() {
    this.searched = new LinkedList<MazeNode>();
    this.work = new LinkedList<MazeNode>();
    this.work.add(this.vertices.get(0).get(0));
    this.path = new LinkedList<MazeNode>();
    this.searching = true;
    this.finished = false;
    this.addMethod = new DfsAdd<MazeNode>();
    this.searchMap = new HashMap<MazeNode, MazeNode>();
    for (MazeNode v : this.flattenArray(this.vertices)) {
      v.seen = false;
    }
    MazeNode start = this.vertices.get(0).get(0);
    this.searchMap.put(start, null);
    this.work.add(start);
  }

  // checks if player is at the end and produces end scene
  public WorldEnd worldEnds() {
    if (this.player.equals(this.vertices.get(this.height - 1).get(this.width - 1))) {
      WorldScene bg = this.makeScene();
      WorldImage winCondition;
      winCondition = new TextImage("You Won!", 20, Color.GREEN);
      WorldImage textBox = new RectangleImage(100, 55, OutlineMode.SOLID, Color.white);
      WorldImage display = new OverlayImage(winCondition, textBox);
      bg.placeImageXY(display, this.width * this.nodeSize / 2, this.height * this.nodeSize / 2);
      return new WorldEnd(true, bg);
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // draws the scene
  public WorldScene makeScene() {
    MazeNode end = this.vertices.get(height - 1).get(width - 1);
    WorldScene bg = new WorldScene(this.width * this.nodeSize, this.height * this.nodeSize);
    ArrayList<MazeNode> nodes = this.flattenArray(this.vertices);
    WorldImage player = new RectangleImage(this.nodeSize, this.nodeSize, OutlineMode.SOLID,
        Color.RED);
    WorldImage startPoint = new RectangleImage(this.nodeSize, this.nodeSize, OutlineMode.SOLID,
        Color.blue);
    WorldImage endPoint = new RectangleImage(this.nodeSize, this.nodeSize, OutlineMode.SOLID,
        Color.MAGENTA);
    if (this.showPaths) {
      // draws the player's path
      for (MazeNode v : this.travelled) {
        WorldImage point = new RectangleImage(this.nodeSize, this.nodeSize, OutlineMode.SOLID,
            new Color(255, 200, 221));
        bg.placeImageXY(point, v.x * this.nodeSize + this.nodeSize / 2,
            v.y * this.nodeSize + this.nodeSize / 2);
      }

      // draws the searching path
      for (MazeNode v : this.searched) {
        WorldImage point = new RectangleImage(this.nodeSize, this.nodeSize, OutlineMode.SOLID,
            new Color(189, 224, 254));
        bg.placeImageXY(point, v.x * this.nodeSize + this.nodeSize / 2,
            v.y * this.nodeSize + this.nodeSize / 2);
      }
    }

    // draws the correct solution
    if (this.finished) {
      for (MazeNode v : this.path) {
        WorldImage point = new RectangleImage(this.nodeSize, this.nodeSize, OutlineMode.SOLID,
            new Color(133, 196, 255));
        bg.placeImageXY(point, v.x * this.nodeSize + this.nodeSize / 2,
            v.y * this.nodeSize + this.nodeSize / 2);
      }
    }
    bg.placeImageXY(endPoint, (end.x + 1) * this.nodeSize - this.nodeSize / 2,
        (end.y + 1) * this.nodeSize - this.nodeSize / 2);
    bg.placeImageXY(startPoint, this.nodeSize / 2, this.nodeSize / 2);

    bg.placeImageXY(player, this.player.x * this.nodeSize + this.nodeSize / 2,
        this.player.y * this.nodeSize + this.nodeSize / 2);

    // draws walls
    while (nodes.size() > 0) { 
      MazeNode cur = nodes.get(0);
      nodes.remove(0);
      if (cur.rightBlocked) {
        bg.placeImageXY(cur.drawEdge(1, this.nodeSize), (cur.x + 1) * this.nodeSize,
            (cur.y + 1) * this.nodeSize - this.nodeSize / 2);
      }
      if (cur.downBlocked) { 
        bg.placeImageXY(cur.drawEdge(this.nodeSize, 1),
            (cur.x + 1) * this.nodeSize - this.nodeSize / 2, (cur.y + 1) * this.nodeSize);
      }
    }
    return bg;
  }

  // turns a 2d array into a 1d array
  public ArrayList<MazeNode> flattenArray(ArrayList<ArrayList<MazeNode>> nodes) {
    ArrayList<MazeNode> ans = new ArrayList<MazeNode>();
    for (ArrayList<MazeNode> array : nodes) {
      ans.addAll(array);
    }
    return ans;
  }
}

// represents a function to add an item to a linked list
interface SearchAdd<T> {
  LinkedList<T> add(LinkedList<T> list, T item);
}

// represents breadth first search for a list
class BfsAdd<T> implements SearchAdd<T> {
  public LinkedList<T> add(LinkedList<T> list, T item) {
    list.addLast(item);
    return list;
  }
}

// represents depth first search for a list
class DfsAdd<T> implements SearchAdd<T> {
  public LinkedList<T> add(LinkedList<T> list, T item) {
    list.addFirst(item);
    return list;
  }
}

// represents examples of mazes and tests program methods
class ExamplesMaze {

  MazeWorld f1;
  MazeWorld f2;
  MazeWorld f3;
  MazeWorld f4;
  MazeNode testVertex = new MazeNode(0, 0);
  Edge testEdge1 = new Edge(10);
  Edge testEdge2 = new Edge(20);

  HashMap<MazeNode, MazeNode> map = new HashMap<MazeNode, MazeNode>();

  MazeNode v0;
  MazeNode v1;
  MazeNode v2;
  MazeNode v3;
  MazeNode v4;
  MazeNode v5;

  // initialize data
  void initData() {
    f1 = new MazeWorld(10, 10);
    f2 = new MazeWorld(5, 10);
    f3 = new MazeWorld(3, 3);
    f4 = new MazeWorld(10, 10, new Random(100));

    v0 = this.f1.vertices.get(0).get(0);
    v1 = this.f1.vertices.get(1).get(0);
    v2 = this.f1.vertices.get(2).get(0);
    v3 = this.f1.vertices.get(0).get(1);
    v4 = this.f1.vertices.get(1).get(1);
    v5 = this.f1.vertices.get(2).get(1);
  }

  // tests drawEdge method
  void testDrawEdge(Tester t) {
    t.checkExpect(this.testVertex.drawEdge(1, 10),
        new RectangleImage(1, 10, OutlineMode.SOLID, Color.black));
    t.checkExpect(this.testVertex.drawEdge(10, 1),
        new RectangleImage(10, 1, OutlineMode.SOLID, Color.black));
  }

  // tests compareTo method
  void testCompareTo(Tester t) {
    t.checkExpect(this.testEdge1.compareTo(this.testEdge2), -10);
    t.checkExpect(this.testEdge1.compareTo(this.testEdge1), 0);
    t.checkExpect(this.testEdge2.compareTo(this.testEdge1), 10);
  }

  // tests makeNodes method
  void testmakeNodes(Tester t) {
    initData();
    t.checkExpect(this.f1.vertices.size(), 10);
    t.checkExpect(this.f1.vertices.get(0).size(), 10);
    t.checkExpect(this.f1.player, this.f1.vertices.get(0).get(0));
  }

  // tests constructorExpection
  void testConstructorException(Tester t) {
    t.checkConstructorException(new IllegalArgumentException("Invalid board size"),
        "MazeWorld", 0, 5);
    t.checkConstructorException(new IllegalArgumentException("Invalid board size"),
        "MazeWorld", 9, 0);
    t.checkConstructorException(new IllegalArgumentException("Invalid board size"),
        "MazeWorld", -1, 15);
    t.checkConstructorException(new IllegalArgumentException("Invalid board size"),
        "MazeWorld", 8, -3);
  }

  // tests initEdges
  void testInitEdges(Tester t) {
    initData();
    ArrayList<ArrayList<MazeNode>> array = f1.vertices;
    t.checkExpect(f1.initEdges(array).size(),
        f1.width * f1.height * 2 - 10 - 10);
    t.checkExpect(array.get(0).get(0).top.from, null);
    t.checkExpect(array.get(0).get(0).top.to, array.get(0).get(0));
    t.checkExpect(array.get(0).get(0).right.from, array.get(0).get(0));
    t.checkExpect(array.get(0).get(0).right.to, array.get(0).get(1));
    t.checkExpect(array.get(0).get(0).left.from, null);
    t.checkExpect(array.get(0).get(0).left.to, array.get(0).get(0));
    t.checkExpect(array.get(0).get(0).bottom.from, array.get(0).get(0));
    t.checkExpect(array.get(0).get(0).bottom.to, array.get(1).get(0));

    t.checkExpect(array.get(9).get(9).top.from, array.get(8).get(9));
    t.checkExpect(array.get(9).get(9).top.to, array.get(9).get(9));
    t.checkExpect(array.get(9).get(9).right.from, array.get(9).get(9));
    t.checkExpect(array.get(9).get(9).right.to, null);
    t.checkExpect(array.get(9).get(9).left.from, array.get(9).get(8));
    t.checkExpect(array.get(9).get(9).left.to, array.get(9).get(9));
    t.checkExpect(array.get(9).get(9).bottom.from, array.get(9).get(9));
    t.checkExpect(array.get(9).get(9).bottom.to, null);
  }

  // tests setEdges method
  void testSetEdges(Tester t) {
    initData();
    ArrayList<ArrayList<MazeNode>> nodes = f1.vertices;
    MazeNode vert1 = f1.vertices.get(0).get(0);
    vert1.downBlocked = true;
    vert1.rightBlocked = true;
    f1.edges = new ArrayList<Edge>();
    f1.edges.add(vert1.bottom);
    f1.setEdges(nodes);
    t.checkExpect(vert1.downBlocked, false);
    t.checkExpect(vert1.rightBlocked, true);
  }

  // tests treeDone
  void testTreeDone(Tester t) {
    initData();
    t.checkExpect(this.f1.edges.size(), 99);
    t.checkExpect(this.f1.treeDone(), true);
  }

  // tests krusAlg method
  void testKrusAlg(Tester t) {
    initData();
    t.checkExpect(this.f1.edges.size() == 99, true);
    t.checkExpect(this.f1.worklist.size() < this.f1.edges.size(), true);
    t.checkExpect(this.f1.worklist.contains(this.f1.edges.get(0)), false);
  }

  // tests flattedArray method
  void testFlattenArray(Tester t) {
    initData();
    ArrayList<ArrayList<MazeNode>> array2d = new ArrayList<ArrayList<MazeNode>>();
    ArrayList<MazeNode> array1 = new ArrayList<MazeNode>();
    ArrayList<MazeNode> array2 = new ArrayList<MazeNode>();
    ArrayList<MazeNode> array3 = new ArrayList<MazeNode>();

    array1.add(this.v0);
    array2.add(this.v1);
    array3.add(this.v2);
    array2d.add(array1);
    array2d.add(array2);
    array2d.add(array3);

    ArrayList<MazeNode> result = new ArrayList<MazeNode>();
    result.add(this.v0);
    result.add(this.v1);
    result.add(this.v2);

    t.checkExpect(this.f1.flattenArray(array2d), result);
    t.checkExpect(this.f1.flattenArray(array2d).size(), 3);
  }

  // tests bigBang
  void testGame(Tester t) {
    initData();
    f4.bigBang(f4.width * 10, f4.height * 10, 0.01);
  }

  // tests search method
  void testSearch(Tester t) {
    initData();
    f2.search();
    t.checkExpect(f2.searchMap.size() > 0, true);
    t.checkExpect(f2.finished, true);
    t.checkExpect(f2.searching, false);
    t.checkExpect(f2.searchMap.containsKey(f2.vertices.get(0).get(0)), true);
    t.checkExpect(f2.searchMap.containsKey(f2.vertices.get(f2.height - 1).get(f2.width - 1)), true);
    t.checkExpect(f2.work.size(), 0);
  }

  // tests onTick method
  void testOnTick(Tester t) {
    initData();
    f3.searching = true;
    // what should happen if search method was called properly in onTick
    t.checkExpect(f3.searchMap.size() > 0, true);
    t.checkExpect(f3.finished, true);
    t.checkExpect(f3.searching, false);
    t.checkExpect(f3.searchMap.containsKey(f3.vertices.get(0).get(0)), true);
    t.checkExpect(f3.searchMap.containsKey(f3.vertices.get(f3.height - 1).get(f3.width - 1)), true);
    t.checkExpect(f3.work.size(), 0);  
  }

  // tests onKeyEvent method
  void testOnKey(Tester t) {
    initData();
    f1.onKeyEvent("left");
    t.checkExpect(f1.player.x, 0);
    t.checkExpect(f1.player.y, 0);
    f1.onKeyEvent("right");
    t.checkExpect(f1.player.x, 0);
    t.checkExpect(f1.player.y, 0);
    f1.onKeyEvent("up");
    t.checkExpect(f1.player.x, 0);
    t.checkExpect(f1.player.y, 0);
    f1.onKeyEvent("down");
    t.checkExpect(f1.player.x, 0);
    t.checkExpect(f1.player.y, 1);
    f1.onKeyEvent("r");
    t.checkExpect(f1.map.size() > 0, true);
    t.checkExpect(f1.searched.size() == 0, true);
    t.checkExpect(f1.searching, false);
    f1.onKeyEvent("d");
    t.checkExpect(f1.searched.size() > 0, false);
    f1.onKeyEvent("b");
    t.checkExpect(f1.searched.size() > 0, false);
    f1.onKeyEvent("s");
    t.checkExpect(f1.showPaths, false);
    f1.onKeyEvent("h");
    t.checkExpect(f1.edges.get(20).weight, 2);
    f1.onKeyEvent("v");
    t.checkExpect(f1.edges.get(20).weight, 1);
    f1.onKeyEvent("n");
    t.checkExpect(f1.map.size() > 0, true);
    t.checkExpect(f1.searched.size() == 0, true);
    t.checkExpect(f1.searching, false);
  }

  // tests  worldEnds method
  void testWorldEnds(Tester t) {
    initData();
    f1.player = this.f1.vertices.get(9).get(9);
    if (f1.player.equals(this.f1.vertices.get(9).get(9))) {
      WorldScene bg = this.f1.makeScene();
      WorldImage winCondition;
      if (this.f1.width < 40) {
        winCondition = new TextImage("You Won!", 15, new Color(205, 180, 219));
      } else {
        winCondition = new TextImage("You Won!", 20, new Color(205, 180, 219));
      }
      WorldImage textBox = new RectangleImage(100, 55, OutlineMode.SOLID, Color.white);
      WorldImage display = new OverlayImage(winCondition, textBox);
      bg.placeImageXY(display, this.f1.width * this.f1.nodeSize / 2, 
          this.f1.height * this.f1.nodeSize / 2);
      //return new WorldEnd(true, bg);
      //t.checkExpect(f1.worldEnds(), bg);
    }
  }

  // tests rest method
  void testReset(Tester t) {
    initData();

    t.checkExpect(f1.key, f1.vertices.get(0).get(0));
    t.checkExpect(f1.edges.size(), 0);
    t.checkExpect(f1.map.size(), 0);
  }

  // tests resetSearch method
  void testResetSearch(Tester t) {
    initData();

    t.checkExpect(f1.searched.size(), 0);
    t.checkExpect(f1.path.size(), 0);
    t.checkExpect(f1.searched, true);
  }

  // tests backPath method
  void testBackPath(Tester t) {
    initData();
    f1.backPath(f1.vertices.get(0).get(0));
    f1.path = new LinkedList<MazeNode>();
    f1.path.addFirst(f1.vertices.get(0).get(0));
    t.checkExpect(f1.path.size(), 0);
  }

  // testsMovePlayer method
  void testMovePlayer(Tester t) {
    initData();
    f1.movePlayer(1, 1);
    t.checkExpect(f1.travelled.contains(f1.player), true);
    t.checkExpect(f1.player, f1.vertices.get(1).get(1));
  }

  // tests SearchAdd method
  void testSearchAdd(Tester t) {
    BfsAdd<String> bfs = new BfsAdd<String>();
    DfsAdd<String> dfs = new DfsAdd<String>();

    LinkedList<String> list = new LinkedList<String>();
    list.add("hello");

    list = bfs.add(list, "bye");
    t.checkExpect(list.get(1), "bye");

    list = dfs.add(list, "front");
    t.checkExpect(list.get(0), "front");
  }


  // tests makeScene method
  void testMakeScene(Tester t) {
    initData();
    MazeNode end = f1.vertices.get(f1.height - 1).get(f1.width - 1);
    WorldScene bg = new WorldScene(f1.width * f1.nodeSize, f1.height * f1.nodeSize);
    ArrayList<MazeNode> nodes = f1.flattenArray(f1.vertices);
    WorldImage player = new RectangleImage(f1.nodeSize, f1.nodeSize, OutlineMode.SOLID,
        Color.RED);
    WorldImage startPoint = new RectangleImage(f1.nodeSize, f1.nodeSize, OutlineMode.SOLID,
        Color.blue);
    WorldImage endPoint = new RectangleImage(f1.nodeSize, f1.nodeSize, OutlineMode.SOLID,
        Color.MAGENTA);
    if (f1.showPaths) {
      // draws the player's path
      for (MazeNode v : f1.travelled) {
        WorldImage point = new RectangleImage(f1.nodeSize, f1.nodeSize, OutlineMode.SOLID,
            new Color(255, 200, 221));
        bg.placeImageXY(point, v.x * f1.nodeSize + f1.nodeSize / 2,
            v.y * f1.nodeSize + f1.nodeSize / 2);
      }

      // draws the searching path
      for (MazeNode v : f1.searched) {
        WorldImage point = new RectangleImage(f1.nodeSize, f1.nodeSize, OutlineMode.SOLID,
            new Color(189, 224, 254));
        bg.placeImageXY(point, v.x * f1.nodeSize + f1.nodeSize / 2,
            v.y * f1.nodeSize + f1.nodeSize / 2);
      }
    }
    if (f1.finished) {
      for (MazeNode v : f1.path) {
        WorldImage point = new RectangleImage(f1.nodeSize, f1.nodeSize, OutlineMode.SOLID,
            new Color(133, 196, 255));
        bg.placeImageXY(point, v.x * f1.nodeSize + f1.nodeSize / 2,
            v.y * f1.nodeSize + f1.nodeSize / 2);
      }
    }
    bg.placeImageXY(endPoint, (end.x + 1) * f1.nodeSize - f1.nodeSize / 2,
        (end.y + 1) * f1.nodeSize - f1.nodeSize / 2);
    bg.placeImageXY(startPoint, f1.nodeSize / 2, f1.nodeSize / 2);

    bg.placeImageXY(player, f1.player.x * f1.nodeSize + f1.nodeSize / 2,
        f1.player.y * f1.nodeSize + f1.nodeSize / 2);

    // draws walls
    while (nodes.size() > 0) { 
      MazeNode cur = nodes.get(0);
      nodes.remove(0);
      if (cur.rightBlocked) {
        bg.placeImageXY(cur.drawEdge(1, f1.nodeSize), (cur.x + 1) * f1.nodeSize,
            (cur.y + 1) * f1.nodeSize - f1.nodeSize / 2);
      }
      if (cur.downBlocked) { 
        bg.placeImageXY(cur.drawEdge(f1.nodeSize, 1),
            (cur.x + 1) * f1.nodeSize - f1.nodeSize / 2, (cur.y + 1) * f1.nodeSize);
      }
    }

    t.checkExpect(f1.makeScene(), bg);
  }
}