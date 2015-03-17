package player;

public class Coordinate{
	private int[] coord = new int[2];

	public Coordinate(){

	}

	public Coordinate(int x, int y){
		coord[0] = x;
		coord[1] = y;
	}

	public Coordinate(int[] newcoord){
		coord[0] = newcoord[0];
		coord[1] = newcoord[1];
	}
	
	public int[] getCoord(){
		return coord;
	}

	public boolean equals(int[] newcoord){
		if(newcoord[0] == coord[0] && newcoord[1] == coord[1]) return true;
		else return false;
	}
}