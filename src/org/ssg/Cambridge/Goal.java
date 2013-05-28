package org.ssg.Cambridge;

public class Goal {

	private int[] pos;
	private int[] dim;//Width, height
	private int[] dir;//Direction balls have to go in to score
	private int playerNum;//Who owns this goal
	
	public Goal(int x, int y, int wid, int hei, int dirx, int diry, int plN){
		pos = new int[]{x,y};
		dim = new int[]{wid,hei};
		dir = new int[]{dirx,diry};
		playerNum = plN;
	}
	
	public void setPlayerNum(int n){
		playerNum = n;
	}
	
	public void changeSides(){
		if(playerNum == 0){
			playerNum = 1;
		}else if(playerNum == 1){
			playerNum = 0;
		}
	}
	
	public int getX(){
		return pos[0];
	}
	
	public int getMinX(){
		return pos[0];
	}
	
	public int getMaxX(){
		return pos[0]+dim[0];
	}
	
	public int getMinY(){//Top of goalpost
		return pos[1];
	}
	
	public int getMaxY(){//Bottom of goalpost
		return pos[1]+dim[1];
	}
	
	public int getXDir(){
		return dir[0];
	}
	
	public int getYDir(){
		return dir[1];
	}
	
	public int getWidth(){
		return dim[0];
	}
	
	public int getHeight(){
		return dim[1];
	}
	
	public int getPlayer(){
		return playerNum;
	}
}
