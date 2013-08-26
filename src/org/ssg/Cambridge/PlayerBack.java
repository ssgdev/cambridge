package org.ssg.Cambridge;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Polygon;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerBack extends Player {

	Image slice_wide;

	Ball ball;

	float[] prevVel;
	
	boolean lockCoolDown;//True if you can't lock ball
	
	//only used during lock
	float angle;//Atan2 returns from pi to -pi
	float angleTarget;
	//float angle2;//Versions of angle and angleTarget respecified from 0 to 2pi
	float prevAngleTarget;
	float prevAngle;
	int rotateDir;

	boolean turning;
	
	float radius;//Used for drawing the shoulders
	float RADIUS;
	
	public PlayerBack(int n, int tN, float[] consts, int[] f, int[] c, CambridgeController c1, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc, Image slc_w, Ball b) {
		super(n, tN, consts, f, c, c1, p, xyL, se, ss, sn, slc);
		
		tempf = PLAYERSIZE/5f;
		poly = new Polygon(new float[]{
				tempf*2, tempf*2,
				tempf*2, -tempf*2,
				tempf, -tempf*2,
				tempf, -tempf*3,
				-tempf, -tempf*3,
				-tempf, -tempf*2,
				-tempf*2, -tempf*2,
				-tempf*2, tempf*2,
				-tempf, tempf*2,
				-tempf, tempf*3,
				tempf, tempf*3,
				tempf, tempf*2});
		
		NORMALKICK *= .2f;
		POWERKICK *= .2f;
		
		slice_wide = slc_w;
		
		ball = b;
		
		buttonPressed = false;
		
		prevVel = new float[2];
		
		lockCoolDown = false;
		
		angle = 0f;
		angleTarget = 0f;
		//angle2 = 0f;
		prevAngleTarget = 0f;
		prevAngle = 0f;
		rotateDir = 1;
		
		turning = false;
		
		RADIUS = 20f;
		radius = RADIUS;
	}

	@Override
	public void drawSlice(Graphics g){
		if(buttonPressed){
			if(ball.locked(playerNum)){
				tempf = 360f/2f/(float)Math.PI*angle + 90f;
				tempArr[0] = .5f + kickingCoolDown/KICKCOOLDOWN*.5f;
			}else{
				tempf = 360f/2f/(float)Math.PI*angleTarget + 90f;
				tempArr[0] = .5f;
			}
			g.rotate(pos[0], pos[1], tempf);
			g.drawImage(slice_wide.getScaledCopy(KICKRANGE/slice_wide.getWidth()), pos[0]-KICKRANGE/2+radius, pos[1]-KICKRANGE/2, getColor(tempArr[0]));
			g.rotate(pos[0], pos[1], 180f);
			g.drawImage(slice_wide.getScaledCopy(KICKRANGE/slice_wide.getWidth()), pos[0]-KICKRANGE/2+radius, pos[1]-KICKRANGE/2, getColor(tempArr[0]));
			g.rotate(pos[0], pos[1], -180f);
			g.rotate(pos[0], pos[1], -tempf);
		}else if(c.exists() && mag(curve)>0){
			tempf = 360f/2f/(float)Math.PI*(float)Math.atan2(curve[1], curve[0]);
			g.rotate(pos[0], pos[1], tempf);
			g.drawImage(slice.getScaledCopy(KICKRANGE/slice.getWidth()), pos[0]-KICKRANGE/2, pos[1]-KICKRANGE/2, getColor(.5f));
			g.rotate(pos[0], pos[1], -tempf);
		}
	}
	
	@Override
	public void update(float delta) {
		
//		System.out.println(ball.locked(playerNum));

		prevVel[0] = vel[0];
		prevVel[1] = vel[1];
		
		if(c.exists()){
			
			pollController(delta);
			
			if(c.getAction()){
				activatePower();
				buttonPressed = true;
			}else{
				powerKeyReleased();
				buttonPressed = false;
			}
			
			if (c.getAction2()){

			} else {

			}
		}else{
			
			pollKeys(delta);

			if(buttonPressed){
				activatePower();
			}
			if(buttonReleased){
				powerKeyReleased();
				buttonPressed = false;
				buttonReleased = false;
			}
			
		}
		
		updatePos(delta);		
		
		//For if the ball gets knocked out of your hands
		if(dist(pos[0],pos[1],ball.getX(),ball.getY())>=KICKRANGE/2f+3f+velMag*delta+1f || ball.scored()){
			ball.setLocked(playerNum, false);
			lockCoolDown = false;
			radius = RADIUS;
			turning = false;
//			buttonPressed = false;
//			power = 0;
		}
		
		//Entering Lock
		if(!lockCoolDown && power>0 && !ball.locked(playerNum) && dist(pos[0],pos[1],ball.getX(),ball.getY())<KICKRANGE/2f && !ball.scored()){
			ball.clearLocked();
			ball.cancelAcc();
			ball.setLocked(playerNum, true);
//			ball.setCanBeKicked(playerNum, true);
			ball.setLastKicker(teamNum);
//			ball.setVel(new float[]{ball.getVelX(),ball.getVelY()},ball.getVelMag()/4f);
//			ball.slowDown(0, ball.getVelMag()/24f, 0);
			//Sets vel to be relative position to ball, so you don't jump on contact
			tempArr[0] = ball.getX()-pos[0];
			tempArr[1] = ball.getY()-pos[1];
			angle = (float)Math.atan2(tempArr[1], tempArr[0]);
			angleTarget = (float)Math.atan2(tempArr[1], tempArr[0]);
			mySoundSystem.quickPlay( true, slowMo? "BackLockSlow.ogg" : "BackLock.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			kickingCoolDown = KICKCOOLDOWN;//visual effect
		}

		
		//Is technically part of the lock statement, but left outside so the shoulders can be drawn outside of lock
		if(mag(vel)!=0){
			unit(vel);	
			angleTarget = (float)Math.atan2(vel[1],vel[0]);
		}
		
		if(ball.locked(playerNum) && !ball.scored()){
			
			//ball.setLastKicker(teamNum);
			
			radius-=delta/2f;
			if(radius<0)
				radius = 0;

			//angle = (float)Math.atan2(ball.getY()-pos[1], ball.getX()-pos[0]);
//			System.out.println(angle/2/Math.PI*360);
			
			prevAngleTarget = (float)Math.atan2(prevVel[1], prevVel[0]);
					
//			System.out.println(angle/2/Math.PI*360+"->  "+angleTarget/2/Math.PI*360);

			//Determine direction of stick rotation
			if(angleDist(angleTarget, prevAngleTarget) > .1f){//If you've moved the stick a non negligible amount
				tempf = (float)(Math.cos(angleTarget)*Math.sin(prevAngleTarget)-Math.cos(prevAngleTarget)*Math.sin(angleTarget));
				tempf/= -Math.abs(tempf);
				rotateDir = (int)tempf;
			}
			
			tempf = delta/60f;//The step interval
			if(angle != angleTarget){
				if(angleDist(angle, angleTarget)>.1f){//If it's actual turning and not a microscopic slip of the finger
					if(rotateDir > 0){
							angle+=tempf;
//						System.out.println(angle/2/Math.PI*360+"->  "+angleTarget/2/Math.PI*360);
					}else if(rotateDir < 0){
							angle-=tempf;
					}
					velMag -= tempf*delta/10f;	
					if(velMag < 0)
						velMag = 0;
				}else{
					velMag -= angleDist(angle, angleTarget)*delta/80f;			
					if(velMag < 0)
						velMag = 0;
					angle = angleTarget;
				}
			}
			
//			System.out.println(velMag);	
			
			ball.setPos(pos[0]+(float)Math.cos(angle)*(KICKRANGE/2f+2f), pos[1]+(float)Math.sin(angle)*(KICKRANGE/2f+2f));
			ball.setVel(new float[]{(float)Math.cos(angle), (float)Math.sin(angle)}, 0f);
//			System.out.println(ball.getVelX()+", "+ball.getVelY()+":"+ball.getVelMag());
			
			//Prevent ball going out of bounds
			if(!ball.betweenGoals(ball.getX(), ball.getY(), ball.getVel())){
				if(ball.getX()<0){
					tempf = ball.getX()-1;
					shiftX(-tempf);
					ball.shiftX(-tempf);
				}
				if(ball.getX()>field[0]){
					tempf = ball.getX()+1;
					shiftX(field[0]-tempf);
					ball.shiftX(field[0]-tempf);
				}
				if(ball.getY()<0){
					tempf = ball.getY()-1;
					shiftY(-tempf);
					ball.shiftY(-tempf);
				}
				if(ball.getY()>xyLimit[3]){
					tempf = ball.getY()+1;
					shiftY(xyLimit[3]-tempf);
					ball.shiftY(xyLimit[3]-tempf);
				}
			}
			
			//Keeps angle between -pi and pi for next round of calculations
			if(angle>(float)Math.PI)
				angle-=(float)Math.PI*2f;
			if(angle<-(float)Math.PI)
				angle+=(float)Math.PI*2f;
			
//			System.out.println("-> "+angle/(float)Math.PI/2f*360f);
			
		}
		
//		if(power==0)
//			System.out.println(ball.getVelX()+" -- "+ball.getVelY());
//		
		
		updateCounters(delta);
		
		if(velMag<VELMAG)
			velMag = approachTarget(velMag, VELMAG, delta/400f);

		if(power>0){
			theta = angleTarget;
		}else{
			theta+= omega*delta/60f*(float)Math.PI;
			if(theta>2f*(float)Math.PI) theta-=2f*(float)Math.PI;
		}
		
		System.out.println(ball.getLastKicker());
	}
	
	@Override
	public void setLockCoolDown(boolean l) {
		lockCoolDown = l;
	}

	@Override
	public void activatePower() {
		if(power == 0)
			mySoundSystem.quickPlay( true, slowMo? "BackActivateSlow.ogg":"BackActivate.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		power = 1;
	}

	@Override
	public void powerKeyReleased() {
		power = 0;
		if(ball.locked(playerNum)){
			
//			System.out.println(rotateDir+", "+angleTarget*180/Math.PI+": "+prevAngleTarget*180/Math.PI);
			
			ball.setLocked(playerNum, false);
			lockCoolDown = false;
			
			if(mag(vel)>0 && angleDist(angle, prevAngle)!=0){
				//Should send the ball off tangentially
//				tempf/=Math.abs(tempf);
				tempArr[0] = (float)Math.cos(angle+(float)Math.PI/2f*(float)rotateDir);
				tempArr[1] = (float)Math.sin(angle+(float)Math.PI/2f*(float)rotateDir);
//				
//				System.out.println(tempf);
//				System.out.println(angle/(float)Math.PI/2f*360f+": "+prevAngle/(float)Math.PI/2f*360f);
//				System.out.println(tempArr[0]+", "+tempArr[1]);
//				
				unit(tempArr);
				ball.setVel(new float[]{tempArr[0], tempArr[1]}, .6f);
//				ball.setVel(new float[]{vel[0], vel[1]}, .5f);
			}else{
				ball.setVel(new float[]{ball.getX()-pos[0], ball.getY()-pos[1]}, .1f);
			}
			kickingCoolDown = KICKCOOLDOWN;
		}
	}

	@Override
	public boolean isKicking() {
		//return false;
		if(buttonPressed)
			return false;
		return kickingCoolDown == 0;
	}
	
	@Override
	public float kickStrength(){
		if(mag(vel)==0){
			return .1f;
		}else if(power>0){
			return POWERKICK;
		}else{
			return NORMALKICK;
		}
	}
	
	@Override
	public boolean flashKick() {
		return false;
	}

	@Override
	public void setPower() {

	}

}
