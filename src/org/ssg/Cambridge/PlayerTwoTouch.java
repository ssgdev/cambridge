/**
TODO:
gravity well
slowmo version of activation sound
 */

package org.ssg.Cambridge;

import net.java.games.input.Controller;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerTwoTouch extends Player{

	boolean buttonPressed;//The power button, that is
	
	float DEFAULTKICK;
	float EXTRAKICK;
	float[] ballPos;
	Ball ball;
	
	//flag used for knocking the ball out of the locked zone, true when player can't lock ball
	boolean lockCoolDown;
	
	//only used during lock
	float angle;//Atan2 returns from pi to -pi
	float angleTarget;
	float angle2;//Versions of angle and angleTarget respecified from 0 to 2pi
	float angleTarget2;
	float curveFactor;
	
	//only used during lock for prediction
	int predictionCount;
	int PREDICTIONCOUNT = 150;
	float[] predictionPos, predictionVel, predictionCurveAcc;
	float predictionVelMag, predictionCurveMag;
	float predictionVDelta, predictionDelta;
	float predictionTempX, predictionTempY;
	
	public PlayerTwoTouch(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc, Ball b) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, slc);

		DEFAULTKICK = NORMALKICK;
		EXTRAKICK = NORMALKICK * 1.5f;
		POWERKICK = 0;

		angleTarget = 0;
		angleTarget2 = 0;
		angle = 0;
		
		ball = b;
		ballPos = new float[2];
		
		MAXPOWER = 10;
		
		curveFactor = 5;
		
		lockCoolDown = false;
		
		predictionCount = 0;
		
		predictionPos = new float[2];
		predictionVel = new float[2];
		predictionCurveAcc = new float[2];
		
		predictionVelMag = 0f;
		predictionCurveMag = 0f;
		predictionVDelta = 0f;
		predictionDelta = 0f;
		predictionTempX = 0f;
		predictionTempY = 0f;
	}
	
	@Override
	public void drawPowerCircle(Graphics g){
		//Draw power circle
		if(ball.locked(playerNum)){
			g.setColor(getColor(mag(vel)/velMag+.5f));
			g.drawOval(ball.getX()-KICKRANGE-power/2f, ball.getY()-KICKRANGE-power/2f, (KICKRANGE+power/2f)*2, (KICKRANGE+power/2f)*2);
			g.setColor(Color.white);
			drawBallPrediction(g);
		}else if(isPower()){
			g.setColor(getColor3());
			g.drawOval(getX()-getKickRange()/2f-getPower()/2f, getY()-getKickRange()/2f-getPower()/2f, getKickRange()+getPower(), getKickRange()+getPower());
			g.setColor(Color.white);
		}
	};
	
	//Calculates and draws dotted trail for ball prediction
	public void drawBallPrediction(Graphics g) {
		g.setColor(getColor());
		predictionCount = PREDICTIONCOUNT;

		predictionPos[0] = ball.getX();
		predictionPos[1] = ball.getY();
		
		predictionVel[0] = (ball.getPrevX()-pos[0]);
		predictionVel[1] = (ball.getPrevY()-pos[1]);
		unit(predictionVel);
		predictionVel[0] += vel[0];
		predictionVel[1] += vel[1];
		unit(predictionVel);
		
		if (mag(predictionVel) == 0)
			return;
		
		predictionVelMag = EXTRAKICK*0.5f + (mag(vel) > 0 ? mag(vel)*0.5f : 0f);
		
		predictionCurveAcc = normal(cExist ? curve : predictionVel, predictionVel);

		if (ball.CURVESCALE == 0)
			predictionCurveMag = 0;
		else
			predictionCurveMag = mag(predictionCurveAcc) * ball.CURVESCALE;
		
		unit(predictionCurveAcc);
		
		while (predictionCount > 0) {
			predictionVDelta = predictionDelta;
			
			while(predictionVDelta>0){

				predictionTempX = predictionPos[0]+(predictionVelMag*predictionVel[0]*predictionVDelta);
				predictionTempY = predictionPos[1]+(predictionVelMag*predictionVel[1]*predictionVDelta);

				//System.out.println(vel[0]);
				//goalArr is {goal x, goal y, goal width, goal thickness, direction to go in
				if((predictionTempX>0 && predictionTempX<(float)field[0] && predictionTempY>0 && predictionTempY<(float)field[1])
						|| ball.betweenGoals(predictionTempX, predictionTempY, predictionVel)){//If it's in bounds or between goalposts
					predictionPos[0]=predictionTempX;
					predictionPos[1]=predictionTempY;
					predictionVDelta = 0;
					
					//ending trail if out of bounds -> only possible if through goals due to collision code
					if (predictionPos[0] > (float)field[0] || predictionPos[0] < 0 
							|| predictionPos[1] > (float)field[1] || predictionPos[1] < 0) {
						predictionCount = 0;
					}
				}else{
					if(predictionTempX<=0 && ball.sameDir(predictionVel[0], -1)){
						predictionPos[0] = 0;
						predictionVel[0]*=-1;
						predictionVDelta -= -1f*predictionPos[0]/(predictionVelMag*predictionVel[0]);
					}else if(predictionTempX>=(float)field[0] && ball.sameDir(predictionVel[0], 1)){
						predictionPos[0]=(float)field[0];
						predictionVel[0]*=-1;
						predictionVDelta -= ((float)field[0]-predictionPos[0])/(predictionVelMag*predictionVel[0]);
					}else if(predictionTempY<=0 && ball.sameDir(predictionVel[1], -1)){
						predictionPos[1]=0;
						predictionVel[1]*=-1;
						predictionVDelta -= -1f*predictionPos[1]/(predictionVelMag*predictionVel[1]);
					}else if(predictionTempY>=(float)field[1] && ball.sameDir(predictionVel[1], 1)){
						predictionPos[1]=(float)field[1];
						predictionVel[1]*=-1;
						predictionVDelta -= ((float)field[1]-predictionPos[1])/(predictionVelMag*predictionVel[1]);
					}
					predictionCurveAcc[0]=0f;//Take off curve after first ricochet
					predictionCurveAcc[1]=0f;
					predictionCurveMag=0f;
					predictionVelMag-=ball.BOUNCEDAMP;
					if(predictionVelMag<0){
						predictionVelMag = .1f;
					} //end if-else block
				} //end vDelta while loop
				
				predictionVel[0]+=predictionCurveAcc[0]*(float)predictionDelta*predictionCurveMag;
				predictionVel[1]+=predictionCurveAcc[1]*(float)predictionDelta*predictionCurveMag;
				unit(predictionVel);
				
				if (predictionCount % 5 == 0 && predictionCount > 0) {
//					System.out.println("Beep! " + vel[0] + " " + vel[1] + " " + (mag(vel) > 0 ? mag(vel)*0.5f : 0f));
					g.setColor(getColor((float)predictionCount/(float)PREDICTIONCOUNT));
					g.fillRect(predictionPos[0]-3, predictionPos[1]-3, 6, 6);
				}
			} //end while predictionCount loop
			
			predictionCount--;
		}
	}
	
	@Override
	public void update(int delta){
		
		if (cExist) {
			
			pollController(delta);
			
			if (actionButton.getPollData() == 1.0){
				if(!buttonPressed){
					activatePower();
					buttonPressed = true;
				}
			}else if(buttonPressed){
					powerKeyReleased();
					buttonPressed = false;
			}
		
		}
		
		predictionVDelta = delta;
		predictionDelta = delta;
		
		//Entering Lock
		if(!lockCoolDown && power>0 && !ball.locked(playerNum) && dist(pos[0],pos[1],ball.getX(),ball.getY())<KICKRANGE/2f && !ball.scored()){
			ball.setLocked(playerNum, true);
			ball.setCanBeKicked(playerNum, true);
			ball.setLastKicker(playerNum);
			ball.setVel(new float[]{ball.getVelX(),ball.getVelY()},ball.getVelMag()/4f);
			ball.slowDown(0, ball.getVelMag()/24f, 0);
			ballPos = new float[]{ball.getX(),ball.getY()};//TODO: Unused
			//Sets vel to be relative position to ball, so you don't jump on contact
			tempArr[0] = ball.getX()-pos[0];
			tempArr[1] = ball.getY()-pos[1];
			angle = (float)Math.atan2(tempArr[1], tempArr[0]);
			angleTarget = (float)Math.atan2(tempArr[1], tempArr[0]);
			NORMALKICK = EXTRAKICK;
			mySoundSystem.quickPlay( true, "TwoTouchLockOn.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		}
		
		if(!ball.locked(playerNum)){
			updatePos(delta);
		}else{
			
			angle = (float)Math.atan2(ball.getY()-pos[1], ball.getX()-pos[0]);
			
			if(mag(vel)!=0){
				unit(vel);	
				angleTarget = (float)Math.atan2(vel[1],vel[0]);
			}
			
			angle2 = angle;
			if(angle2<0)
				angle2 += 2f*(float)Math.PI;
			angleTarget2 = angleTarget;
			if(angleTarget2<0)
				angleTarget2 += 2f*(float)Math.PI;

			tempf = angle;//Store the angle in case we need to roll back, in case of the rotation putting you into a wall
			//Choose the direction of shortest rotation
			if(Math.abs(angleTarget-angle)-Math.abs(angleTarget2-angle2) >= 0){
				angle = approachTarget(angle2, angleTarget2, (float)delta/120f);
			}else{
				angle = approachTarget(angle, angleTarget, (float)delta/120f);
			}
			
			pos[0] = ball.getX()-(float)Math.cos(angle)*(KICKRANGE/2-1);
			pos[1] = ball.getY()-(float)Math.sin(angle)*(KICKRANGE/2-1);
			
			if(pos[0]<xyLimit[0]+KICKRANGE/2f){
				tempf = pos[0];
				shiftX(xyLimit[0]+KICKRANGE/2f-tempf);
				ball.shiftX(xyLimit[0]+KICKRANGE/2f-tempf);
			}
			if(pos[0]>xyLimit[1]-KICKRANGE/2f){
				tempf = pos[0];
				shiftX(xyLimit[1]-KICKRANGE/2f-tempf);
				ball.shiftX(xyLimit[1]-KICKRANGE/2f-tempf);
			}
			if(pos[1]<xyLimit[2]+KICKRANGE/2f){
				tempf = pos[1];
				shiftY(xyLimit[2]+KICKRANGE/2f-tempf);
				ball.shiftY(xyLimit[2]+KICKRANGE/2f-tempf);
			}
			if(pos[1]>xyLimit[3]-KICKRANGE/2f){
				tempf = pos[1];
				shiftY(xyLimit[3]-KICKRANGE/2f-tempf);
				ball.shiftY(xyLimit[3]-KICKRANGE/2f-tempf);
			}

//			//If that rotation made you out of bounds, roll it back//No longer used: now you get shoved away from wall if you rotate into it
//			if(pos[0]-KICKRANGE/2<xyLimit[0] || pos[0]+KICKRANGE/2>xyLimit[1] || pos[1]-KICKRANGE/2<xyLimit[2] || pos[1]+KICKRANGE/2>xyLimit[3]){
//				angle = tempf;
//				vel[0] = (float)Math.cos(angle);
//				vel[1] = (float)Math.sin(angle);
//			}
//			
			//Keeps angle between -pi and pi for next round of calculations
			if(angle>(float)Math.PI)
				angle-=(float)Math.PI*2f;
			
		}
		
		//For if the ball gets knocked out of your hands
		if(dist(pos[0],pos[1],ball.getX(),ball.getY())>=KICKRANGE/2f){
			ball.setLocked(playerNum, false);
			lockCoolDown = false;
			NORMALKICK = DEFAULTKICK;
		}
		
		updateCounters(delta);
		
		theta+= (1f-(power/MAXPOWER*.8f))*omega*(float)delta;
		if(theta>360) theta-=360;
		
	}
	
	public void setLockCoolDown(boolean l) {
		lockCoolDown = l;
	}

	@Override
	public void activatePower(){
		power = MAXPOWER;
		velMag = VELMAG * 0.5f;
		mySoundSystem.quickPlay( true, "TwoTouchActivate.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
	}
	
	@Override
	public void powerKeyReleased(){
		power = 0;
		velMag = VELMAG;
	}
	
	@Override
	public void setPower(){
	
	}

	//Used to add velocity component of player to kick
	//In power, TwoTouch will not move the ball
	@Override
	public float[] getKick(){
		if(power>0)
			return new float[]{0,0};
		return vel;
	}
	
	@Override
	public boolean isKicking() {
		if(power>0)
			return false;
		return true;
	}
	
	//I just kicked (power or regular kick) the ball now what
	@Override
	public void setKicking(Ball b){
		if(power>0 && !ball.locked(playerNum)){
			//Do nothing
		}else{
			b.setCanBeKicked(playerNum, false);
			kickingCoolDown = KICKCOOLDOWN;
			if(ball.locked(playerNum)){
				ball.setLocked(playerNum, false);
				NORMALKICK = DEFAULTKICK;
				if(mag(vel)>0)
					lastKickAlpha = 1f;
			}
		}
	}

	//Return if the kick should flash and make the power kick sound
	@Override
	public boolean flashKick(){
		return ball.locked(playerNum) && mag(vel)>0;
	}
	
	@Override
	public void setLastKick(float bx, float by, float px, float py, float lka){//ball pos, player pos, was it a power kick
		lastKickBallPos[0] = bx;
		lastKickBallPos[1] = by;
		lastKickPos[0] = px;
		lastKickPos[1] = py;
		//TwoTouch doesn't have the last kick alpha
	}

}
