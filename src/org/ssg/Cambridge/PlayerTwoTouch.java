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
	
	//only used during lock
	float angle;//Atan2 returns from pi to -pi
	float angleTarget;
	float angle2;//Versions of angle and angleTarget respecified from 0 to 2pi
	float angleTarget2;

	//lock occurs when you touch the ball in power: the ball freezes, and you're limited to circling around it, until power is released
	boolean lock;
	
	public PlayerTwoTouch(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Ball b) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn);

		DEFAULTKICK = NORMALKICK;
		EXTRAKICK = NORMALKICK * 1.5f;
		POWERKICK = 0;
		
		lock = false;
		angleTarget = 0;
		angleTarget2 = 0;
		angle = 0;
		
		ballPos = new float[2];
		
		MAXPOWER = 10;
	}
	
	@Override
	public void drawPowerCircle(Graphics g){
		//Draw power circle
		if(lock){
			g.setColor(getColor(mag(vel)/velMag+.5f));
			g.drawOval(ballPos[0]-KICKRANGE-power, ballPos[1]-KICKRANGE-power, (KICKRANGE+power)*2, (KICKRANGE+power)*2);
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
		
		if(!lock){
			updatePos(delta);
		}else{
			unit(vel);
			
			if(mag(vel)!=0){
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
			
			pos[0] = ballPos[0]-(float)Math.cos(angle)*(KICKRANGE/2-1);
			pos[1] = ballPos[1]-(float)Math.sin(angle)*(KICKRANGE/2-1);
			
			//If that rotation made you out of bounds, roll it back
			if(pos[0]-KICKRANGE/2<xyLimit[0] || pos[0]+KICKRANGE/2>xyLimit[1] || pos[1]-KICKRANGE/2<xyLimit[2] || pos[1]+KICKRANGE/2>xyLimit[3]){
				angle = tempf;
				vel[0] = (float)Math.cos(angle);
				vel[1] = (float)Math.sin(angle);
			}
			
			//Keeps angle between -pi and pi for next round of calculations
			if(angle>(float)Math.PI)
				angle-=(float)Math.PI*2f;
			
		}
		
		lastKickAlpha -= (float)(delta)/2400f;
		if(lastKickAlpha<0){
			lastKickAlpha = 0f;
		}

		kickingCoolDown -= (float)delta;
		if(kickingCoolDown<0)
			kickingCoolDown = 0;

		theta+= (1f-(power/MAXPOWER*.8f))*omega*(float)delta;
		if(theta>360) theta-=360;
		
	}

	@Override
	public void activatePower(){
		power = MAXPOWER;
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
		if(lock && power>0)
			return false;
		return true;
	}
	
	//I just kicked (power or regular kick) the ball now what
	@Override
	public void setKicking(Ball b){
		if(power>0 && !lock){
			lock = true;
			ballPos = new float[]{b.getX(),b.getY()};
			//Sets vel to be relative position to ball, so you don't jump on contact
			tempArr[0] = b.getX()-pos[0];
			tempArr[1] = b.getY()-pos[1];
			angle = (float)Math.atan2(tempArr[1], tempArr[0]);
			NORMALKICK = EXTRAKICK;
		}else{
			b.setCanBeKicked(playerNum, false);
			kickingCoolDown = KICKCOOLDOWN;
			if(lock){
				lock = false;
				NORMALKICK = DEFAULTKICK;
				if(mag(vel)>0)
					lastKickAlpha = 1f;
			}
		}
	}

	//Return if the kick should flash and make the power kick sound
	@Override
	public boolean flashKick(){
		return lock && mag(vel)>0;
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
