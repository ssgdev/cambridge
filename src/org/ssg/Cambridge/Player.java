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

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class Player implements KeyListener {

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
	float omega;//Angular velocity � spins faster when strong shot is charged
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

	Player otherPlayer;
	
	int temp;//Used whenever an int is needed temporarily

	public Player(int n, float[] consts, int f[], int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Player op){

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
		omega = 1f;

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

		otherPlayer = op;
		
		temp = 0;
	}

	/////////////////////////////////////////////////////
	public void update(int delta){
		//Each player has a different one of these
	}
	
	public void activatePower(){
		//Each player has a different one of these
	}
	
	//Most players might have a basic version of this and some modifications as necessary\
	//If we want to do this it can be less ugly
	public void render(Graphics g, Image triangle, float BALLSIZE, AngelCodeFont font_small){
		g.setColor(getColor5());
		g.setLineWidth(100f);
//		tempTrailArr = p.getTrailArr();//{bx, by, px, py}
		float dx = getTrailArr()[2]-getTrailArr()[0];
		float dy = getTrailArr()[3]-getTrailArr()[1];
		float thetaTemp = (float)Math.atan2((double)dy, (double)dx);
		g.rotate(getTrailArr()[2], getTrailArr()[3], 360f/2f/(float)Math.PI*thetaTemp);
		//g.drawLine(getTrailArr()[0]-2*FIELDWIDTH,getTrailArr()[1], getTrailArr()[0]+2*FIELDWIDTH, getTrailArr()[1]);
		g.drawLine(getTrailArr()[0]-2*1600,getTrailArr()[1], getTrailArr()[0]+2*1600, getTrailArr()[1]);
		g.rotate(getTrailArr()[2], getTrailArr()[3], -360f/2f/(float)Math.PI*thetaTemp);
		g.setLineWidth(5f);

		//Draw the flash for when your power kick is recharged
		if(getPowerCoolDown()>-500 && getPowerCoolDown()<0){
			g.setColor(getColor4().brighter());
			g.fillOval(getX()-getKickRange()/2f, getY()-getKickRange()/2f, getKickRange(), getKickRange());
		}
		
		//Draw kicking circle
		g.setColor(getColor(.5f).darker());
		g.drawOval(getX()-getKickRange()/2, getY()-getKickRange()/2, getKickRange(), getKickRange());
		
		//Kicking circle flash when kick happens
		g.setColor(getColor2().brighter());
		g.drawOval(getX()-getKickRange()/2f, getY()-getKickRange()/2f, getKickRange(), getKickRange());
		
		g.setColor(getColor());
		g.rotate(getX(), getY(), getTheta());
		g.drawRect(getX()-PLAYERSIZE/2, getY()-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
		g.rotate(getX(), getY(), -getTheta());
		//g.drawOval(getX()-getKickRange()/2f+BALLSIZE/2f, getY()-getKickRange()/2f+BALLSIZE/2f, getKickRange()-BALLSIZE, getKickRange()-BALLSIZE);//Draw kicking circle;

		//Draw power circle
		if(isPower()){
			g.setColor(getColor3());
			g.drawOval(getX()-getKickRange()/2f-getPower()/2f, getY()-getKickRange()/2f-getPower()/2f, getKickRange()+getPower(), getKickRange()+getPower());
			g.setColor(Color.white);
		}
		
		g.drawImage(triangle, getX()-triangle.getWidth()/2, getY()-getKickRange()/2-25, getColor());
		g.setColor(getColor());
		g.setFont(font_small);
		g.drawString("P"+(getPlayerNum()+1), getX()-font_small.getWidth("P"+(getPlayerNum()+1))/2+1, getY()-font_small.getHeight("P")-getKickRange()/2-30);
		
	}
	////////////////////////////////////////////////////
	
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

	public float[] getVel(){
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

	public boolean isKicking(){
		return true;
	}

	public void setKicking(int a){
		kickingCoolDown = KICKCOOLDOWN;
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
	
	public boolean flashKick(){
		return isPower();
	}
	
	public float getPower(){
		return power;
	}

	public void setPower(){
		power = 0;//
		velMag = VELMAG;
		if(mySoundSystem.playing(slowName))
			mySoundSystem.pause(slowName);
	}

	public float getPowerCoolDown(){
		return powerCoolDown;
	}

	public void setLastKick(float bx, float by, float px, float py, float lka){//ball pos, player pos, was it a power kick
		lastKickBallPos[0] = bx;
		lastKickBallPos[1] = by;
		lastKickPos[0] = px;
		lastKickPos[1] = py;
		lastKickAlpha = lka;
	}

	public float[] getTrailArr(){
		return new float[]{lastKickBallPos[0], lastKickBallPos[1], lastKickPos[0], lastKickPos[1]};
	}

	public void setOtherPlayer(Player p){
		otherPlayer = p;
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

	public void powerKeyReleased(){
		//Not always used
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
	
	
}