/**
TODO:
gravity well
slowmo version of activation sound
 */

package org.ssg.Cambridge;

import net.java.games.input.Controller;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerTwoTouch extends Player{

	boolean buttonPressed;//The power button, that is
	
	float DEFAULTKICK;
	float EXTRAKICK;
	float[] ballPos;
	Ball ball;
	
	//only used during lock
	float angle;//Atan2 returns from pi to -pi
	float angleTarget;
	float angle2;//Versions of angle and angleTarget respecified from 0 to 2pi
	float angleTarget2;
	
	public PlayerTwoTouch(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Ball b) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn);

		DEFAULTKICK = NORMALKICK;
		EXTRAKICK = NORMALKICK * 1.5f;
		POWERKICK = 0;

		angleTarget = 0;
		angleTarget2 = 0;
		angle = 0;
		
		ball = b;
		ballPos = new float[2];
		
		MAXPOWER = 10;
	}
	
	@Override
	public void drawPowerCircle(Graphics g){
		//Draw power circle
		if(ball.locked(playerNum)){
			g.setColor(getColor(mag(vel)/velMag+.5f));
			g.drawOval(ball.getX()-KICKRANGE-power/2f, ball.getY()-KICKRANGE-power/2f, (KICKRANGE+power/2f)*2, (KICKRANGE+power/2f)*2);
			g.setColor(Color.white);
		}else if(isPower()){
			g.setColor(getColor3());
			g.drawOval(getX()-getKickRange()/2f-getPower()/2f, getY()-getKickRange()/2f-getPower()/2f, getKickRange()+getPower(), getKickRange()+getPower());
			g.setColor(Color.white);
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
		
		//Entering Lock
		if(power>0 && !ball.locked(playerNum) && dist(pos[0],pos[1],ball.getX(),ball.getY())<KICKRANGE/2f && !ball.scored()){
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
			NORMALKICK = DEFAULTKICK;
		}
		
		lastKickAlpha -= (float)(delta)/2400f;
		if(lastKickAlpha<0){
			lastKickAlpha = 0f;
		}
		
		kickingCoolDown -= (float)delta;
		if(kickingCoolDown<0)
			kickingCoolDown = 0;

		stun -= (float)delta;
		if(stun<=0){
			stun = 0;
		}
		
		theta+= (1f-(power/MAXPOWER*.8f))*omega*(float)delta;
		if(theta>360) theta-=360;
		
	}

	@Override
	public void activatePower(){
		power = MAXPOWER;
		mySoundSystem.quickPlay( true, "TwoTouchActivate.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
	}
	
	@Override
	public void powerKeyReleased(){
		power = 0;
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
