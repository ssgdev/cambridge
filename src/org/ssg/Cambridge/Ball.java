package org.ssg.Cambridge;

import org.ini4j.*;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class Ball {

	float[] pos;
	float tempX, tempY;
	float[] vel;//Unit vector direction
	float vDelta;//delta used for calculating collision
	float[] acc;
	float accMag;
	float ACCSCALE;// = .0005f; Curve acc scaling factor
	float velMag;//velocity magnitude
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
	boolean[] canBeKicked;//by player n. Hopefully prevents double tapping
	
	boolean slowOn;
	
	float tempf;

	public Ball(float[] consts, int[] f, Goal[] g, float[] p, int gw, SoundSystem ss){

		ACCSCALE = consts[0];
		BOUNCEDAMP = consts[1];
		FLOORFRICTION = consts[2];

		field = f;
		goals = g;
		GOALWIDTH = gw;
		pos = p;

		vel = new float[]{0f,0f};
		acc = new float[]{0f,0f};
		tempX = 0;
		tempY = 0;

		theta = 0f;
		accMag = 0f;
		velMag = 0f;

		vDelta = 0;

		canBeKicked = new boolean[2];//Number of players
		lastKicker = -1;

		mySoundSystem = ss;
		soundCoolDown = 0;
		scored = false;
		slowOn = false;
	}

	public void setLastKicker(int n){
		lastKicker = n;
	}

	public int getLastKicker(){
		return lastKicker;
	}

	//Can this ball be kicked by player n
	public boolean canBeKicked(int n){
		return canBeKicked[n];
	}
	
	//Sets if the ball can be kicked by player n
	public void setCanBeKicked(int n, boolean b){
		canBeKicked[n] = b;
	}
	
	public float getX(){
		return pos[0];
	}

	public float getY(){
		return pos[1];
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

	public float getTheta(){
		return theta;
	}

	public void setVel(float[] f, float mag){
		vel = f;
		unit(vel);
		velMag = mag;
	}

	public void setAcc(float[] f, float am){
		acc = f;
		unit(acc);
		if(ACCSCALE == 0){
			accMag = 0;
		}else{
			accMag = am * ACCSCALE;
		}
	}

	//Setting unscaled acceleration
	public void setAccUnscaled(float[] f, float am){
		if(!scored){
			acc = f;
			unit(acc);
			accMag = am;
		}
	}
	
	public void setScored(boolean b){
		scored = b;
	}

	public void setSoundCoolDown(int i){
		soundCoolDown = i;
	}

	public void setSlowOn(boolean b){
		slowOn = b;
	}

	public void update(int delta){

		soundCoolDown -= delta;
		if(soundCoolDown<0)
			soundCoolDown = 0;

		vDelta = (float)delta;

		while(vDelta>0){

			tempX = pos[0]+(velMag*vel[0]*vDelta);
			tempY = pos[1]+(velMag*vel[1]*vDelta);

			//System.out.println(vel[0]);
			//goalArr is {goal x, goal y, goal width, goal thickness, direction to go in
			if(scored || (tempX>0 && tempX<(float)field[0] && tempY>0 && tempY<(float)field[1])
					|| betweenGoals(tempX, tempY)){//If it's in bounds or between goalposts
				pos[0]=tempX;
				pos[1]=tempY;
				vDelta = 0;
			}else{
				if(soundCoolDown<=0 && !scored){
					if(slowOn){
						mySoundSystem.quickPlay( true, "bump2slow.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
					}else{
						mySoundSystem.quickPlay( true, "bump2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
					}
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
				acc[0]=0f;//Take off curve after first ricochet
				acc[1]=0f;
				accMag=0f;
				velMag-=BOUNCEDAMP;
				if(velMag<0){
					velMag = .1f;
				}
			}

		}

//		velMag -= (float)delta / 1000f;//uncomment this because it's funny
		vel[0]+=acc[0]*(float)delta*accMag;
		vel[1]+=acc[1]*(float)delta*accMag;
		unit(vel);

		if(velMag>0) velMag -= velMag*(float)delta * FLOORFRICTION;

		theta+=velMag*delta;
	}

	public boolean betweenGoals(float x, float y){
		for(Goal g: goals){
			if(y>(float)g.getMinY() && y<(float)g.getMaxY() && sameDir(vel[0], g.getXDir()))
					return true;
			if(x>(float)g.getMinX() && x<(float)g.getMaxX() && sameDir(vel[1], g.getYDir()))
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
