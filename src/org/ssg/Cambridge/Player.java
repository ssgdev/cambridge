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
	CambridgeController c; //gamepad controller
	
	Component lStickX, lStickY, rStickX, rStickY, actionButton; //gamepad buttons
	float[] pos;//ition
	float[] vel;// [xvel, yvel]
	float[] curve; //curve control for gamepad users. Is the right stick
	float velMag;
	float VELMAG;// = .5f;
	float POWERVELMAG;// = 1f;
	int[] field;//fieldWidth, fieldHeight
	int[] xyLimit;//p1 can only be on left half, p2 can only be on right half
	boolean up,down,left,right, buttonPressed, buttonReleased, button2Pressed, button2Released;
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
	float lastKickWidth;//Width of ball trail, used mainly for Puffer
	
	float stun;
	float MAXSTUN = 1000;
	float[] stunVel;//The velocity you are forced into during a stun
	float stunVelMag;
	
	Color color;
	Image slice;
	
	SoundSystem mySoundSystem;
	String slowName;//The name of the rumbling sound channel (slow1 or slow2)

	Player[] players;
	boolean slowMo;
	
	int temp;//Used whenever an int is needed temporarily
	float tempf;
	float[] tempArr;
	boolean bool;
	float[] zeroes = {0,0};
	
	public Player(int n, float[] consts, int f[], int[] c, CambridgeController c1, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc){

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
		buttonPressed = false;
		buttonReleased = false;
		button2Pressed = false;
		button2Released = false;
		kickingCoolDown = 0;
		power = 0;
		powerCoolDown = -500;
		playedPowerDing = true;

		lastKickPos = new float[]{0,0};
		lastKickBallPos = new float[]{0,0};
		lastKickAlpha = 0f;
		lastKickWidth = KICKRANGE;

		stun = 0;
		stunVel = new float[2];
		stunVelMag = 0;
		
		inputOn = false;

		mySoundSystem = ss;
		slowName = sn;

		slice = slc;
		
		players = null;
		slowMo = false;
		
		temp = 0;
		tempArr = new float[2];
	}

	/////////////////////////////////////////////////////
	public abstract void update(float delta);
	
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
//		b.setCanBeKicked(playerNum, false);
		kickingCoolDown = KICKCOOLDOWN;
	}

	////////////////////////////////////////////////////
	
	//Three methods called in most updates
	
	public void pollController(float delta){
		if (c.exists()) {
			vel[0] = c.getLeftStickX();
			vel[1] = c.getLeftStickY();
			
			if (mag(vel) < 0.28f) {
				vel[0] = 0f;
				vel[1] = 0f;
			}
			
			curve[0] = c.getRightStickX();
			curve[1] = c.getRightStickY();
			
			if (mag(curve) < 0.28f) {
				curve[0] = 0f;
				curve[1] = 0f;
			}
		}
	}
	
	public void pollKeys(float delta){
		if(left && !right){
			vel[0] = -1f;
		}else if(!left && right){
			vel[0] = 1f;
		}else{
			vel[0] = 0;
		}
		if(up && !down){
			vel[1] = -1f;
		}else if(!up && down){
			vel[1] = 1f;
		}else{
			vel[1] = 0;
		}
		
		unit(vel);
	}
	
	public void updatePos(float delta){
		pos[0] = (pos[0]+stunVel[0]*stunVelMag*delta*stun/MAXSTUN + (1f-stun/MAXSTUN)*vel[0]*velMag*delta);
		pos[1] = (pos[1]+stunVel[1]*stunVelMag*delta*stun/MAXSTUN + (1f-stun/MAXSTUN)*vel[1]*velMag*delta);

		if(pos[0]<xyLimit[0]+KICKRANGE/2){
			pos[0]=xyLimit[0]+KICKRANGE/2;
			bool = true;//Indicating a wall collision occured
		}
		if(pos[0]>xyLimit[1]-KICKRANGE/2){
			pos[0]=xyLimit[1]-KICKRANGE/2;
			bool = true;
		}
		if(pos[1]<xyLimit[2]+KICKRANGE/2){
			pos[1]=xyLimit[2]+KICKRANGE/2;
			bool = true;
		}
		if(pos[1]>xyLimit[3]-KICKRANGE/2){
			pos[1]=xyLimit[3]-KICKRANGE/2;
			bool = true;

		}
		
//		if(temp-KICKRANGE/2>=xyLimit[0] && temp+KICKRANGE/2<=xyLimit[1])
//			pos[0]=temp;

//		temp = 
//		if(temp-KICKRANGE/2>=xyLimit[2] && temp+KICKRANGE/2<=xyLimit[3])
		
		//player on player collision handling
		//TODO: modify for 4P
		for(Player otherPlayer: players){
			if(otherPlayer != this  && !(otherPlayer instanceof PlayerDummy)){//If he's not you, collide with him
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
	
	public void updateCounters(float delta){
		stun -= delta;
		if(stun<=0){
			stun = 0;
		}
		
		lastKickAlpha -= (delta)/1200f;
		if(lastKickAlpha<0){
			lastKickAlpha = 0f;
		}
		
		kickingCoolDown -= delta;
		if(kickingCoolDown<0)
			kickingCoolDown = 0;
		
		//powerCoolDown and power are set uniquely per player
		//as well as any player specific cooldowns and counters
		
	}
	
	/////////////////////////////////////////////////////////
	
	//If players have custom effects they can override individual methods
	public void render(Graphics g, float BALLSIZE, Image triangle, AngelCodeFont font_small){
		
		drawKickTrail(g);
		
		drawSlice(g);//Draws the arc sector which describes the direction of the right stick
		
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
		float dx = lastKickPos[0]-lastKickBallPos[0];
		float dy = lastKickPos[1]-lastKickBallPos[1];
		tempf = (float)Math.atan2((double)dy, (double)dx);
		g.rotate(lastKickBallPos[0], lastKickBallPos[1], 360f/2f/(float)Math.PI*tempf);
		//g.drawLine(getTrailArr()[0]-2*FIELDWIDTH,getTrailArr()[1], getTrailArr()[0]+2*FIELDWIDTH, getTrailArr()[1]);
		//g.drawLine(getTrailArr()[0]-2*1600,getTrailArr()[1], getTrailArr()[0]+2*1600, getTrailArr()[1]);
		g.fillRect(lastKickBallPos[0]-2*1600, lastKickBallPos[1]-lastKickWidth/2-5, 1600*4, lastKickWidth+10);
		g.rotate(lastKickBallPos[0], lastKickBallPos[1], -360f/2f/(float)Math.PI*tempf);
		g.setLineWidth(5f);
	}
	
	public void drawSlice(Graphics g){
		if(c.exists() && mag(curve)>0){
			tempf = 360f/2f/(float)Math.PI*(float)Math.atan2(curve[1], curve[0]);
			g.rotate(pos[0], pos[1], tempf);
			g.drawImage(slice.getScaledCopy(KICKRANGE/slice.getWidth()), pos[0]-KICKRANGE/2, pos[1]-KICKRANGE/2, getColor(.2f));
			g.rotate(pos[0], pos[1], -tempf);
		}
	}
	
	public void drawRechargeFlash(Graphics g){
		//Draw the flash for when your power kick is recharged
		if(powerCoolDown>-500 && powerCoolDown<0){
			g.setColor(getColor4().brighter());
			g.fillOval(pos[0]-getKickRange()/2f, pos[1]-getKickRange()/2f, KICKRANGE, KICKRANGE);
		}
	}
	
	public void drawKickCircle(Graphics g){
		//Draw kicking circle
		g.setColor(getColor(.5f).darker());
		g.drawOval(pos[0]-KICKRANGE/2, pos[1]-KICKRANGE/2, KICKRANGE, KICKRANGE);
		
		//Kicking circle flash when kick happens
		g.setColor(getColor2().brighter());
		g.drawOval(pos[0]-KICKRANGE/2f, pos[1]-KICKRANGE/2f, KICKRANGE, KICKRANGE);
	}
	
	public void drawPlayer(Graphics g){
		g.setColor(getColor());
		g.rotate(pos[0], pos[1], theta);
		g.drawRect(pos[0]-PLAYERSIZE/2, pos[1]-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
		g.rotate(pos[0], pos[1], -theta);
		//g.drawOval(getX()-getKickRange()/2f+BALLSIZE/2f, getY()-getKickRange()/2f+BALLSIZE/2f, getKickRange()-BALLSIZE, getKickRange()-BALLSIZE);//Draw kicking circle;
	}
	
	public void drawPowerCircle(Graphics g){
		//Draw power circle
		if(isPower()){
			g.setColor(getColor3());
			g.drawOval(pos[0]-KICKRANGE/2f-power/2f, pos[1]-KICKRANGE/2f-power/2f, KICKRANGE+power, KICKRANGE+power);
			g.setColor(Color.white);
		}
	}
	
	public void drawNameTag(Graphics g, Image triangle, AngelCodeFont font_small){
		g.drawImage(triangle, pos[0]-triangle.getWidth()/2, pos[1]-getKickRange()/2-25, color);
		g.setColor(color);
		g.setFont(font_small);
		g.drawString("P"+(playerNum+1), pos[0]-font_small.getWidth("P"+(playerNum+1))/2, pos[1]-font_small.getHeight("P")-KICKRANGE/2-30);
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
	
	public float getVelMag(){
		return velMag;
	}
	
	public void setStunned(float n, float[] v, float vm){
		if(stun == 0){
			stun = n;
			stunVel = v;
			unit(stunVel);
			stunVelMag = vm;
		}
	}
	
	public boolean stunned(){
		return stun>0;
	}
	
	//Used to add velocity component of player to kick
	public float[] getKick(){
		return vel;
	}
	
	public float[] getCurve() {
		if(c.exists())
			return curve;
		return zeroes;
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
		if(mag(vel)==0){
			return .5f;
		}else if(power>0){
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
		lastKickWidth = KICKRANGE;
	}

	public float[] getTrailArr(){
		return new float[]{lastKickBallPos[0], lastKickBallPos[1], lastKickPos[0], lastKickPos[1]};
	}

	//Empty, only overriden for Back and TwoTouch
	public void setLockCoolDown(boolean b){
		
	}
	
	@Override
	public void keyPressed(int input, char arg1) {
		if (!c.exists()) {
			if(input == controls[0]){
				up = true;
			}else if(input == controls[1]){
				down = true;
			}else if(input == controls[2]){
				left = true;
			}else if(input == controls[3]){
				right = true;
			}else if(input == controls[4]){
				buttonPressed = true;
			}else if(input == controls[5]){
				button2Pressed = true;
			}
		}
	}

	@Override
	public void keyReleased(int input, char arg1) {
		if (!c.exists()) {
			if(input == controls[0]){
				up = false;
			}else if(input == controls[1]){
				down = false;
			}else if(input == controls[2]){
				left = false;
			}else if(input == controls[3]){
				right = false;
			}else if(input == controls[4]){
				buttonReleased = true;
			}else if(input == controls[5]){
				button2Released = true;
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
	
	public float dot(float[] a, float[] b){
		return a[0]*b[0]+a[1]*b[1];
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
	
	public float[] normal(float[] v, float[] w){
		if(mag(w)>0){
			tempf = dot(v,w)/mag(w);
			return new float[]{v[0]-tempf*w[0], v[1]-tempf*w[1]};
		}else{
			return new float[]{0,0};
		}
	}
	
	public boolean sameDir(float vx, float dir){
		if(vx == 0)
			return false;
		return vx/Math.abs(vx) == dir/Math.abs(dir);
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
