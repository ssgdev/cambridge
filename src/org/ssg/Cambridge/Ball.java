package org.ssg.Cambridge;

import java.util.ArrayList;

import org.ini4j.*;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class Ball {
	
	int ballID;
	
	ArrayList<Player> players;

	float[] pos;
	float tempX, tempY;
	float[] vel;//Unit vector direction
	float vDelta;//delta used for calculating collision
	float[] curveAcc;
	float curveMag;
	float CURVESCALE;// = .0005f; Curve curveAcc scaling factor
	float velMag;//velocity magnitude
	
	float velTarget;
	float accMag;
	float accDelta;
	boolean speedingUp;
	boolean slowingDown;
	boolean gustReady;
	boolean[] locked;//Is it locked down by a TwoTouch or Back
	
	int[] assistTwin;//{playerNum, twinNum} of the twin who is set up the assist
	
	float theta;
	float BOUNCEDAMP;// = .5f;//How much speed is lost on ricochet
	float FLOORFRICTION;//How much speed is lost by the ball traveling
	int[] field;
	Goal[] goals;
	int GOALWIDTH;

	int lastKicker;

	SoundSystem mySoundSystem;

	boolean scored;//Used to control playing of sounds
	int soundCoolDown;
//	boolean[] canBeKicked;//by player n. Hopefully prevents double tapping
	
	boolean slowOn;
	
	float tempf;

	public Ball(int bID, float[] consts, int[] f, Goal[] g, float[] p, int gw, SoundSystem ss){

		ballID = bID;//0 for real, 123 etc for fakes
		
		CURVESCALE = consts[0];
		BOUNCEDAMP = consts[1];
		FLOORFRICTION = consts[2];

		field = f;
		goals = g;
		GOALWIDTH = gw;
		pos = p;

		vel = new float[]{0f,0f};
		curveAcc = new float[]{0f,0f};
		tempX = 0;
		tempY = 0;

		accMag = 0;
		accDelta = 0;;
		velTarget = 0;
		speedingUp = false;
		slowingDown = false;
		gustReady = false;
		locked = new boolean[4];
		
		assistTwin = new int[2];
		assistTwin[0] = -1;
		assistTwin[1] = -1;
		
		theta = 0f;
		curveMag = 0f;
		velMag = 0f;

		vDelta = 0;

//		canBeKicked = new boolean[2];//Number of players
		lastKicker = -1;

		mySoundSystem = ss;
		soundCoolDown = 0;
		scored = false;
		slowOn = false;
	}
	
	public void setPlayers(ArrayList<Player> p) {
		players = p;
	}

	public void setLastKicker(int n){
		lastKicker = n;
	}

	public int getLastKicker(){
		return lastKicker;
	}

//	//Can this ball be kicked by player n
//	public boolean canBeKicked(int n){
//		return canBeKicked[n];
//	}
//	
//	//Sets if the ball can be kicked by player n
//	public void setCanBeKicked(int n, boolean b){
//		canBeKicked[n] = b;
//	}
	
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
	
	public float getPrevX(){
		return pos[0]-12f*vel[0]*velMag;
	}

	public float getPrevY(){
		return pos[1]-12f*vel[1]*velMag;
	}

	public void setPos(float x, float y){
		pos[0] = x;
		pos[1] = y;
	}

	public float getVelX(){
		return vel[0];
	}

	public float getVelY(){
		return vel[1];
	}

	public float getVelMag(){
		return velMag;
	}
	
	public float[] getVel(){
		return vel;
	}
	
	public float getTheta(){
		return theta;
	}

	public void setVel(float[] f, float mag){
		vel[0] = f[0];
		vel[1] = f[1];
		unit(vel);
		velMag = mag;
	}

	public void setCurve(float[] f, float cm){
		curveAcc[0] = f[0];
		curveAcc[1] = f[1];
		unit(curveAcc);
		if(CURVESCALE == 0){
			curveMag = 0;
		}else{
			curveMag = cm * CURVESCALE;
		}
	}

	//Setting unscaled acceleration
	public void setAccUnscaled(float[] f, float am){
		if(!scored){
			curveAcc = f;
			unit(curveAcc);
			curveMag = am;
		}
	}
	
	//Used by PlayerTwoTouch
	public void setLocked(int n, boolean b){
		locked[n] = b;
	}
	
	//Is it being locked by a PlayerTwoTouch
	public boolean locked(int n){
		return locked[n];
	}
	
	public void clearLocked(){
		for(int i=0;i<players.size();i++){
			locked[i] = false;
			players.get(i).setLockCoolDown(true);
		}
	}
	
	public int[] assistTwin(){
		return assistTwin;
	}
	
	public void setAssistTwin(int player, int twin){
		assistTwin[0] = player;
		assistTwin[1] = twin;
	}
	
	public void speedUp(float velTarg, float acc, float accD){
		velTarget = velTarg;
		accDelta = accD;
		accMag = acc;
		speedingUp = true;
		slowingDown = false;
	}
	
	public void slowDown(float velTarg, float acc, float accD){
		velTarget = velTarg;
		accDelta = accD;
		accMag = acc;
		speedingUp = false;
		slowingDown = true;
	}
	
	//Called when ball is kicked, ?bounced?, or scored.
	//Cancels any acceleration or upcoming gusting from PlayerCharge.
	public void cancelAcc(){
		speedingUp = false;
		slowingDown = false;
	}
	
	public void setReadyForGust(boolean b){
		gustReady = b;
	}
	
	public boolean gustReady(){
		return gustReady;
	}
	
	public void setScored(boolean b){
		scored = b;
	}
	
	public boolean scored(){
		return scored;
	}

	public void setSoundCoolDown(int i){
		soundCoolDown = i;
	}

	public void setSlowOn(boolean b){
		slowOn = b;
	}

	public void update(float delta){

		soundCoolDown -= delta;
		if(soundCoolDown<0)
			soundCoolDown = 0;

		vDelta = delta;
		
		while(vDelta>0){

			tempX = pos[0]+(velMag*vel[0]*vDelta);
			tempY = pos[1]+(velMag*vel[1]*vDelta);

			//System.out.println(vel[0]);
			//goalArr is {goal x, goal y, goal width, goal thickness, direction to go in
			if(scored || (tempX>0 && tempX<(float)field[0] && tempY>0 && tempY<(float)field[1])
					|| betweenGoals(tempX, tempY, vel)){//If it's in bounds or between goalposts
				pos[0]=tempX;
				pos[1]=tempY;
				vDelta = 0;
			}else{
				if(soundCoolDown<=0 && !scored && ballID == 0){
					mySoundSystem.quickPlay( true, slowOn ?"BallBounceSlow.ogg":"BallBounce.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}
				if(tempX<=0 && sameDir(vel[0], -1)){
					pos[0] = 0;
					vel[0]*=-1;
					vDelta -= -1f*pos[0]/(velMag*vel[0]);
				}else if(tempX>=(float)field[0] && sameDir(vel[0], 1)){
					pos[0]=(float)field[0];
					vel[0]*=-1;
					vDelta -= ((float)field[0]-pos[0])/(velMag*vel[0]);
				}else if(tempY<=0 && sameDir(vel[1], -1)){
					pos[1]=0;
					vel[1]*=-1;
					vDelta -= -1f*pos[1]/(velMag*vel[1]);
				}else if(tempY>=(float)field[1] && sameDir(vel[1], 1)){
					pos[1]=(float)field[1];
					vel[1]*=-1;
					vDelta -= ((float)field[1]-pos[1])/(velMag*vel[1]);
				}
				curveAcc[0]=0f;//Take off curve after first ricochet
				curveAcc[1]=0f;
				curveMag=0f;
				cancelAcc();
				//setAssistTwin(-1,-1);
				if(inCorner() && velMag < .5f){
					velMag = .5f;
				}else{
					velMag-=BOUNCEDAMP;
				}

				if(velMag<.1f){
					velMag = .1f;
				}
				
				System.out.println(velMag);
				
			}	
		}
		
		if(!slowingDown && velTarget>=0){//Don't curve if you're going backwards 
			vel[0]+=curveAcc[0]*delta*curveMag;
			vel[1]+=curveAcc[1]*delta*curveMag;
			unit(vel);
		}
		
		if(speedingUp){
			if(velMag<=velTarget){
				velMag+=accMag;
				accMag += accDelta;
				if(velMag > velTarget){
					velMag = velTarget;
					speedingUp = false;
				}
			}
		}else if(slowingDown){
			if(velMag>=velTarget){
				velMag-=accMag;
				accMag += accDelta;
				if(velMag < velTarget){
					velMag = velTarget;
					slowingDown = false;
				}
				if(velMag<0 && velTarget<0){
					velMag*=-1;
					vel[0]*=-1;
					vel[1]*=-1;
					velTarget*=-1;
					slowingDown = false;
					speedingUp = true;
				}
			}
		}
		
		
		if(velMag>0) velMag -= velMag * delta * FLOORFRICTION;

		theta+=velMag*delta;
	}

	//Whether the ball is in the corner
	public boolean inCorner(){
		float dist;
		float cornerX = 0;
		float cornerY = 0;
		if(pos[0] > field[0]/2)
			cornerX = field[0];
		if(pos[1] > field[1]/2)
			cornerY = field[1];
		float tempX = pos[0] - cornerX;
		float tempY = pos[1] - cornerY;
		dist = (float)Math.sqrt(tempX*tempX + tempY*tempY);
		if(dist > 200.0f)
			return false;
		return true;
	}
	
	public boolean betweenGoals(float x, float y, float[] velocity){
		for(Goal g: goals){
			if(y>(float)g.getMinY() && y<(float)g.getMaxY() && sameDir(velocity[0], g.getXDir()))
					return true;
			if(x>(float)g.getMinX() && x<(float)g.getMaxX() && sameDir(velocity[1], g.getYDir()))
					return true;
		}
		return false;
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

	public boolean sameDir(float v, int dir){
		if(v == 0)
			return false;
		return v/Math.abs(v) == (float)dir;
	}

}
