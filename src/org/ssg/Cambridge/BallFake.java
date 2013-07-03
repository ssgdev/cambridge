//Is identical to ball except it bounces off goals

package org.ssg.Cambridge;

import org.ini4j.*;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class BallFake extends Ball {
	
	public BallFake(float[] consts, int[] f, Goal[] g, float[] p, int gw, SoundSystem ss) {
		super(consts, f, g, p, gw, ss);
	}

	@Override
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
			if(scored || (tempX>0 && tempX<(float)field[0] && tempY>0 && tempY<(float)field[1])){//If it's in bounds or between goalposts
				pos[0]=tempX;
				pos[1]=tempY;
				vDelta = 0;
			}else{
				if(soundCoolDown<=0 && !scored){
					if(slowOn){
						mySoundSystem.quickPlay( true, "BallBounceSlow.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
					}else{
						mySoundSystem.quickPlay( true, "BallBounce.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
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
				curveAcc[0]=0f;//Take off curve after first ricochet
				curveAcc[1]=0f;
				curveMag=0f;
//				cancelAcc();
				velMag-=BOUNCEDAMP;
				if(velMag<0){
					velMag = .1f;
				}
				
			}

		}

//		velMag -= (float)delta / 1000f;//uncomment this because it's funny
		vel[0]+=curveAcc[0]*(float)delta*curveMag;
		vel[1]+=curveAcc[1]*(float)delta*curveMag;
		unit(vel);
		
		if(velMag>0) velMag -= velMag*(float)delta * FLOORFRICTION;

		theta+=velMag*delta;
	}

}
