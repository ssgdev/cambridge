package org.ssg.Cambridge;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.ini4j.*;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public abstract class Player implements KeyListener {

	boolean inputOn;

	int playerNum;
	float PLAYERSIZE;
	
	int[] controls;//up down left right kick
	Controller c; //gamepad controller
	boolean cExist; //gamepad exist boolean
	Component lStickX, lStickY, rStickX, rStickY, actionButton; //gamepad buttons
	float[] pos;//ition
	float[] vel;// [xvel, yvel]
	float[] curve; //curve control for gamepad users. Is the right stick
	float velMag;
	float VELMAG;// = .5f;
	float POWERVELMAG;// = 1f;
	int[] field;//fieldWidth, fieldHeight
	int[] xyLimit;//p1 can only be on left half, p2 can only be on right half
	boolean up,down,left,right;
	float theta;//Angle at which is tilted
	float omega;//Angular velocity – spins faster when strong shot is charged
	float kickingCoolDown;
	float KICKCOOLDOWN;// = 800;
	float power;
	float powerCoolDown;
	boolean playedPowerDing;
	float MAXPOWER;// = 500;//How long slomo lasts
	float POWERCOOLDOWN;// = 2000;//How long cooldown is on slomo
	float NORMALKICK;// = .5f;//Ball velocity mag after kicking
	float POWERKICK;// = 2f;//Ball velocity mag after powerkicking
	float KICKRANGE;//Diameter of kicking circle
	
	float[] lastKickPos;
	float[] lastKickBallPos;
	float lastKickAlpha;

	Color color;

	SoundSystem mySoundSystem;
	String slowName;//The name of the rumbling sound channel (slow1 or slow2)

	Player[] players;
	boolean slowMo;
	
	int temp;//Used whenever an int is needed temporarily
	float tempf;
	float[] tempArr;
	
	public Player(int n, float[] consts, int f[], int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn){

		playerNum = n;
		PLAYERSIZE = 20;
		
		VELMAG = consts[0];
		POWERVELMAG = consts[1];
		KICKCOOLDOWN = consts[2];
		MAXPOWER = consts[3];
		POWERCOOLDOWN = consts[4];
		NORMALKICK = consts[5];
		POWERKICK = consts[6];
		KICKRANGE = consts[7];

		field = f;
		controls = c;
		this.c = c1;
		cExist = c1Exist;
		if (cExist) {
			lStickX = this.c.getComponent(Component.Identifier.Axis.X);
			lStickY = this.c.getComponent(Component.Identifier.Axis.Y);
			rStickY = this.c.getComponent(Component.Identifier.Axis.RY);
			rStickX = this.c.getComponent(Component.Identifier.Axis.RX);
			actionButton = this.c.getComponent(Component.Identifier.Button._5);
		}
		pos = p;
		xyLimit = xyL;
		color = se;
		vel = new float[]{0,0};
		curve = new float[]{0,0};
		velMag = VELMAG;
		theta = 0;
		omega = .5f;

		up = false;
		down = false;
		left = false;
		right = false;
		kickingCoolDown = 0;
		power = 0;
		powerCoolDown = -500;
		playedPowerDing = true;

		lastKickPos = new float[]{0,0};
		lastKickBallPos = new float[]{0,0};
		lastKickAlpha = 0f;

		inputOn = false;

		mySoundSystem = ss;
		slowName = sn;

		players = null;
		slowMo = false;
		
		temp = 0;
		tempArr = new float[2];
	}

	/////////////////////////////////////////////////////
	public abstract void update(int delta);
	
	public abstract void activatePower();
	
	public abstract void powerKeyReleased();
	
	//Am I able to kick?
	public abstract boolean isKicking();
	
	//Is this kick a flash kick?
	public abstract boolean flashKick();
	
	//I just performed a flash kick now what?
	public abstract void setPower();
	
	//Not an abstract method, but put here for organization purposes
	//I just kicked (any kick) the ball now what
	public void setKicking(Ball b){
		b.setCanBeKicked(playerNum, false);
		kickingCoolDown = KICKCOOLDOWN;
	}

	////////////////////////////////////////////////////
	
	//Two methods called in most updates
	
	public void pollController(int delta){
		vel[0] = lStickX.getPollData();
		vel[1] = lStickY.getPollData();
		
		if (Math.abs(vel[0]) < 0.1)
				vel[0] = 0f;
		if (Math.abs(vel[1]) < 0.1)
				vel[1] = 0f;

		
		curve[0] = -rStickX.getPollData();
		curve[1] = -rStickY.getPollData();
		
		if(Math.abs(curve[0]) < 0.1)
			curve[0] = 0;
		if(Math.abs(curve[1]) < 0.1)
			curve[1] = 0;
	}
	
	public void updatePos(int delta){
		pos[0] = (int)(pos[0]+vel[0]*velMag*(float)delta);
		if(pos[0]<xyLimit[0]+KICKRANGE/2)
			pos[0]=xyLimit[0]+KICKRANGE/2;
		if(pos[0]>xyLimit[1]-KICKRANGE/2)
			pos[0]=xyLimit[1]-KICKRANGE/2;
		pos[1]= (int)(pos[1]+vel[1]*velMag*(float)delta);
		if(pos[1]<xyLimit[2]+KICKRANGE/2)
			pos[1]=xyLimit[2]+KICKRANGE/2;
		if(pos[1]>xyLimit[3]-KICKRANGE/2)
			pos[1]=xyLimit[3]-KICKRANGE/2;
		
//		if(temp-KICKRANGE/2>=xyLimit[0] && temp+KICKRANGE/2<=xyLimit[1])
//			pos[0]=temp;

//		temp = 
//		if(temp-KICKRANGE/2>=xyLimit[2] && temp+KICKRANGE/2<=xyLimit[3])
		
		//player on player collision handling
		//TODO: modify for 4P
		for(Player otherPlayer: players){
			if(otherPlayer != this){//If he's not you, collide with him
				tempf = dist(pos[0], pos[1], otherPlayer.getX(), otherPlayer.getY());
				if(tempf < (KICKRANGE + otherPlayer.getKickRange())/2){
					tempArr[0] = otherPlayer.getX()-pos[0];
					tempArr[1] = otherPlayer.getY()-pos[1];
					unit(tempArr);
					tempArr[0]*= (KICKRANGE + otherPlayer.getKickRange())/2 - tempf;
					tempArr[1]*= (KICKRANGE + otherPlayer.getKickRange())/2 - tempf;
					
					//////X AXIS
					//Get pushed, weighted based on size
					tempf = KICKRANGE / ( KICKRANGE+ otherPlayer.getKickRange());
					shiftX(-tempArr[0]*(1-tempf));
					otherPlayer.shiftX(tempArr[0]*tempf);
					
					//Then if anyone got pushed into a wall, pop out
					if(pos[0]-KICKRANGE/2 <= xyLimit[0]){
						tempf = xyLimit[0] - pos[0] + KICKRANGE/2;
					}else if(otherPlayer.getX()-otherPlayer.getKickRange()/2 <= xyLimit[0]){
						tempf = xyLimit[0] - otherPlayer.getX()+otherPlayer.getKickRange()/2;
					}else if(pos[0]+KICKRANGE/2 >= xyLimit[1]){
						tempf = xyLimit[1] - pos[0] - KICKRANGE/2;
					}else if(otherPlayer.getX() + otherPlayer.getKickRange()/2 >= xyLimit[1]){
						tempf = xyLimit[1] - otherPlayer.getX() - otherPlayer.getKickRange()/2;
					}else{
						tempf = 0;
					}
					shiftX(tempf);
					otherPlayer.shiftX(tempf);
					
					//////Y AXIS
					tempf = KICKRANGE / ( KICKRANGE+ otherPlayer.getKickRange());
					shiftY(-tempArr[1]*(1-tempf));
					otherPlayer.shiftY(tempArr[1]*tempf);
					
					//Then if anyone got pushed into a wall, pop out
					if(pos[1]-KICKRANGE/2 <= xyLimit[2]){
						tempf = xyLimit[2] - pos[1] + KICKRANGE/2;
					}else if(otherPlayer.getY()-otherPlayer.getKickRange()/2 <= xyLimit[2]){
						tempf = xyLimit[2] - otherPlayer.getY()+otherPlayer.getKickRange()/2;
					}else if(pos[1]+KICKRANGE/2 >= xyLimit[3]){
						tempf = xyLimit[3] - pos[1] - KICKRANGE/2;
					}else if(otherPlayer.getY() + otherPlayer.getKickRange()/2 >= xyLimit[3]){
						tempf = xyLimit[3] - otherPlayer.getY() - otherPlayer.getKickRange()/2;
					}else{
						tempf = 0;
					}
					shiftY(tempf);
					otherPlayer.shiftY(tempf);
		
				}
			}
		}
	}
	
	/////////////////////////////////////////////////////////
	
	//If players have custom effects they can override individual methods
	public void render(Graphics g, float BALLSIZE, Image triangle, AngelCodeFont font_small){
		
		drawKickTrail(g);
		
		drawRechargeFlash(g);
		
		drawKickCircle(g);
		
		drawPlayer(g);
		
		drawPowerCircle(g);
		
		drawNameTag(g, triangle, font_small);		
	}
	
	public void drawKickTrail(Graphics g){
		g.setColor(getColor5());
		g.setLineWidth(KICKRANGE*1.5f);
//		tempTrailArr = p.getTrailArr();//{bx, by, px, py}
		float dx = getTrailArr()[2]-getTrailArr()[0];
		float dy = getTrailArr()[3]-getTrailArr()[1];
		float thetaTemp = (float)Math.atan2((double)dy, (double)dx);
		g.rotate(getTrailArr()[0], getTrailArr()[1], 360f/2f/(float)Math.PI*thetaTemp);
		//g.drawLine(getTrailArr()[0]-2*FIELDWIDTH,getTrailArr()[1], getTrailArr()[0]+2*FIELDWIDTH, getTrailArr()[1]);
		g.drawLine(getTrailArr()[0]-2*1600,getTrailArr()[1], getTrailArr()[0]+2*1600, getTrailArr()[1]);
		g.rotate(getTrailArr()[0], getTrailArr()[1], -360f/2f/(float)Math.PI*thetaTemp);
		g.setLineWidth(5f);
	}
	
	public void drawRechargeFlash(Graphics g){
		//Draw the flash for when your power kick is recharged
		if(getPowerCoolDown()>-500 && getPowerCoolDown()<0){
			g.setColor(getColor4().brighter());
			g.fillOval(getX()-getKickRange()/2f, getY()-getKickRange()/2f, getKickRange(), getKickRange());
		}
	}
	
	public void drawKickCircle(Graphics g){
		//Draw kicking circle
		g.setColor(getColor(.5f).darker());
		g.drawOval(getX()-getKickRange()/2, getY()-getKickRange()/2, getKickRange(), getKickRange());
		
		//Kicking circle flash when kick happens
		g.setColor(getColor2().brighter());
		g.drawOval(getX()-getKickRange()/2f, getY()-getKickRange()/2f, getKickRange(), getKickRange());
	}
	
	public void drawPlayer(Graphics g){
		g.setColor(getColor());
		g.rotate(getX(), getY(), getTheta());
		g.drawRect(getX()-PLAYERSIZE/2, getY()-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
		g.rotate(getX(), getY(), -getTheta());
		//g.drawOval(getX()-getKickRange()/2f+BALLSIZE/2f, getY()-getKickRange()/2f+BALLSIZE/2f, getKickRange()-BALLSIZE, getKickRange()-BALLSIZE);//Draw kicking circle;
	}
	
	public void drawPowerCircle(Graphics g){
		//Draw power circle
		if(isPower()){
			g.setColor(getColor3());
			g.drawOval(getX()-getKickRange()/2f-getPower()/2f, getY()-getKickRange()/2f-getPower()/2f, getKickRange()+getPower(), getKickRange()+getPower());
			g.setColor(Color.white);
		}
	}
	
	public void drawNameTag(Graphics g, Image triangle, AngelCodeFont font_small){
		g.drawImage(triangle, getX()-triangle.getWidth()/2, getY()-getKickRange()/2-25, getColor());
		g.setColor(getColor());
		g.setFont(font_small);
		g.drawString("P"+(getPlayerNum()+1), getX()-font_small.getWidth("P"+(getPlayerNum()+1))/2, getY()-font_small.getHeight("P")-getKickRange()/2-30);

	}
	
	////////////////////////////////////////////////////
	
	public void setPlayers(Player[] p){
		players = p;
	}
	
	public int getPlayerNum(){
		return playerNum;
	}

	public float getPlayerSize(){
		return PLAYERSIZE;
	}
	
	public float getX(){
		return pos[0];
	}

	public float getY(){
		return pos[1];
	}

	public void shiftX(float f){
		pos[0]+=f;
	}
	
	public void shiftY(float f){
		pos[1]+=f;
	}
	
	public float[] getVel(){
		return vel;
	}
	
	//Used to add velocity component of player to kick
	public float[] getKick(){
		return vel;
	}
	
	public float[] getCurve() {
		if(cExist)
			return curve;
		return vel;
	}

	public float getTheta(){
		return theta;
	}

	public float getOmega(){
		return omega;
	}

	public Color getColor(){
		return color;
	}

	public Color getColor(float f){
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), f);
	}

	public Color getColor2(){//Returns color of the circle
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), ((kickingCoolDown/KICKCOOLDOWN)));
	}

	public Color getColor3(){//return color of powercircle
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), ((power/MAXPOWER)));
	}

	public Color getColor4(){
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), (powerCoolDown+500f)/500f);
	}

	public Color getColor5(){//powerkick bar
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), lastKickAlpha);
	}

	public float kickStrength(){
		if(power>0){
			return POWERKICK;
		}else{
			return NORMALKICK;
		}
	}
	
	public float getKickRange(){
		return KICKRANGE;
	}
	
	public boolean isPower(){
		if(power>0)
			return true;
		return false;
	}

	public boolean isSlowMoPower(){
		return false;
	}
	
	public float getPower(){
		return power;
	}
	
	public float getPowerCoolDown(){
		return powerCoolDown;
	}

	public void setSlowMo(boolean b){
		slowMo = b;
	}
	
	public void setLastKick(float bx, float by, float px, float py, float lka){//ball pos, player pos, was it a flash kick
		lastKickBallPos[0] = bx;
		lastKickBallPos[1] = by;
		lastKickPos[0] = px;
		lastKickPos[1] = py;
		lastKickAlpha = lka;
	}

	public float[] getTrailArr(){
		return new float[]{lastKickBallPos[0], lastKickBallPos[1], lastKickPos[0], lastKickPos[1]};
	}

	@Override
	public void keyPressed(int input, char arg1) {
		if (!cExist) {
			if(input == controls[0]){
				up = true;
				vel[1] = -1;
			}else if(input == controls[1]){
				down = true;
				vel[1] = 1;
			}else if(input == controls[2]){
				left = true;
				vel[0] = -1;
			}else if(input == controls[3]){
				right = true;
				vel[0] = 1;
			}else if(input == controls[4]){//kick
				if(powerCoolDown <= 0){
					activatePower();
				}else{
					//play whiff animation
				}
			}
		}
	}

	@Override
	public void keyReleased(int input, char arg1) {
		if (!cExist) {
			if(input == controls[0]){
				up = false;
				if(down){
					vel[1] = 1;
				}else{
					vel[1] = 0;
				}
			}else if(input == controls[1]){
				down = false;
				if(up){
					vel[1] = -1;
				}else{
					vel[1] = 0;
				}
			}else if(input == controls[2]){
				left = false;
				if(right){
					vel[0] = 1;
				}else{
					vel[0] = 0;
				}
			}else if(input == controls[3]){
				right = false;
				if(left){
					vel[0] = -1;
				}else{
					vel[0] = 0;
				}
			}else if(input == controls[4]){
				powerKeyReleased();
			}
		}
	}

	@Override
	public void inputEnded() {
		inputOn = false;
	}

	@Override
	public void inputStarted() {
		inputOn = true;
	}

	@Override
	public boolean isAcceptingInput() {
		return inputOn;
	}

	@Override
	public void setInput(Input arg0) {

	}

	public float dist(float a, float b, float x, float y){
		return (float)Math.sqrt((x-a)*(x-a)+(y-b)*(y-b));
	}
	
	public void unit(float[] f){
		if(f[0]==0 && f[1]==0){
			return;
		}else if (f[0]==0 && f[1]!=0 ){
			f[1]=f[1]/Math.abs(f[1]);
		}else if( f[0]!=0 && f[1]==0){
			f[0]=f[0]/Math.abs(f[0]);
		}else{
			tempf = (float)Math.sqrt(f[0]*f[0]+f[1]*f[1]); 
			f[0]/= tempf;
			f[1]/= tempf;
		}
	}
	
	public float mag(float a, float b){
		return (float)Math.sqrt(a*a+b*b);
	}
	
	public float mag(float[] n){
		return (float)Math.sqrt(n[0]*n[0]+n[1]*n[1]);
	}

	public float mag(int[] n){
		return (float)Math.sqrt(n[0]*n[0]+n[1]*n[1]);
	}
	
	public float[] approachTargets(float[] val, float[] targets, float inc){

		for(int i=0; i<val.length;i++){
			if(val[i]<targets[i]){
				val[i]+=inc;
				if(val[i]>targets[i])
					val[i]=targets[i];
			}
			if(val[i]>targets[i]){
				val[i]-=inc;
				if(val[i]<targets[i])
					val[i]=targets[i];
			}
		}
		
		return val;
	}
	
	public float approachTarget(float val, float target, float inc){

		if(val<target){
			val+=inc;
			if(val>target)
				val=target;
		}
		if(val>target){
			val-=inc;
			if(val<target)
				val=target;
		}

		return val;
	}
}
