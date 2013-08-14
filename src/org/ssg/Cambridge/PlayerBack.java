package org.ssg.Cambridge;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerBack extends Player {

	Image slice_wide;
	
	Ball ball;

	Component actionButton2;
	
	float[] prevVel;
	
	boolean lockCoolDown;//True if you can't lock ball
	
	//only used during lock
	float angle;//Atan2 returns from pi to -pi
	float angleTarget;
	//float angle2;//Versions of angle and angleTarget respecified from 0 to 2pi
	float prevAngleTarget;
	float prevAngle;
	int rotateDir;
	
	float targetVelMag;
	
	float radius;//Used for drawing the shoulders
	float RADIUS;
	
	public PlayerBack(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc, Image slc_w, Ball b) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, slc);
		
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
		
		targetVelMag = VELMAG;
		
		if (cExist) {
			actionButton2 = this.c.getComponent(Component.Identifier.Button._4); 
		}
		
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
		}else if(cExist && mag(curve)>0){
			tempf = 360f/2f/(float)Math.PI*(float)Math.atan2(curve[1], curve[0]);
			g.rotate(pos[0], pos[1], tempf);
			g.drawImage(slice.getScaledCopy(KICKRANGE/slice.getWidth()), pos[0]-KICKRANGE/2, pos[1]-KICKRANGE/2, getColor(.5f));
			g.rotate(pos[0], pos[1], -tempf);
		}
	}
	
	@Override
	public void update(float delta) {
		
//		System.out.println(ball.locked(playerNum));

		if(cExist){
			
			prevVel[0] = vel[0];
			prevVel[1] = vel[1];
			
			pollController(delta);
			
			if(actionButton.getPollData() == 1.0){
				activatePower();
				buttonPressed = true;
			}else{
				powerKeyReleased();
				buttonPressed = false;
			}
			
			if (actionButton2.getPollData() == 1.0){

			}else if(actionButton2.getPollData() == 0.0){

			}
		}
		
		updatePos(delta);
	
		//Entering Lock
		if(!lockCoolDown && power>0 && !ball.locked(playerNum) && dist(pos[0],pos[1],ball.getX(),ball.getY())<KICKRANGE/2f && !ball.scored()){
			ball.setLocked(playerNum, true);
//			ball.setCanBeKicked(playerNum, true);
			ball.setLastKicker(playerNum);
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
			
			ball.setLastKicker(playerNum);
			
			radius-=delta/2f;
			if(radius<0)
				radius = 0;
			
//			velMag = .1f;
			
			//angle = (float)Math.atan2(ball.getY()-pos[1], ball.getX()-pos[0]);
//			System.out.println(angle/2/Math.PI*360);
			
			prevAngleTarget = (float)Math.atan2(prevVel[1], prevVel[0]);
		
			//So they're in the same range (-pi to pi or 0 to 2pi) and subtraction works
			if(angleTarget< -(float)Math.PI/2f)
				angleTarget+=(float)Math.PI*2f;
			if(prevAngleTarget< -(float)Math.PI/2f)
				prevAngleTarget+=(float)Math.PI*2f;
//			if(angle < -(float)Math.PI/2f)
//				angle+=(float)Math.PI*2f;
			
//			System.out.println(angle/2/Math.PI*360+"->  "+angleTarget/2/Math.PI*360);

			if(Math.abs(angleTarget-prevAngleTarget) > .1f){//If you've moved the stick a non negligible amount
				rotateDir = (angleTarget>prevAngleTarget ? 1 : -1);
			}
			
			//Set them back to -pi to pi values so the rest of math works
			if(angleTarget>(float)Math.PI)
				angleTarget-=(float)Math.PI*2f;
			if(prevAngleTarget>(float)Math.PI)
				prevAngleTarget-=(float)Math.PI*2f;
			
			tempf = delta/80f;//The step interval
			if(angle != angleTarget){
				if(Math.abs(angle-angleTarget)>tempf){//If it's actual turning and not a microscopic slip of the finger
					if(rotateDir > 0){
						if(angle > angleTarget)
							angle-=(float)Math.PI*2f;
						if(angle + tempf >= angleTarget){
							angle = angleTarget;
						}else{
							angle+=tempf;
						}
//						System.out.println(angle/2/Math.PI*360+"->  "+angleTarget/2/Math.PI*360);
					}else if(rotateDir < 0){
						if(angle < angleTarget)
							angle += (float)Math.PI*2f;
						if(angle - tempf <= angleTarget){
							angle = angleTarget;
						}else{
							angle-=tempf;
						}
					}
				}else{
					angle = angleTarget;
				}
			}
			
			ball.setPos(pos[0]+(float)Math.cos(angle)*(KICKRANGE/2f+2f), pos[1]+(float)Math.sin(angle)*(KICKRANGE/2f+2f));
			ball.setVel(new float[]{(float)Math.cos(angle), (float)Math.sin(angle)}, 0f);
//			System.out.println(ball.getVelX()+", "+ball.getVelY()+":"+ball.getVelMag());
			
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
		//For if the ball gets knocked out of your hands
		if((dist(pos[0],pos[1],ball.getX(),ball.getY())>=KICKRANGE/2f+3f || ball.scored())){
			ball.setLocked(playerNum, false);
			lockCoolDown = false;
//			buttonPressed = false;
			power = 0;
		}
		
		updateCounters(delta);
		
		theta+= omega*delta;
		if(theta>360) theta-=360;		
	}
	
	@Override
	public void setLockCoolDown(boolean l) {
		lockCoolDown = l;
	}

	@Override
	public void activatePower() {
		power = 1;
		if(!buttonPressed)
			mySoundSystem.quickPlay( true, slowMo? "BackActivateSlow.ogg":"BackActivate.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
	}

	@Override
	public void powerKeyReleased() {
		power = 0;
		radius = RADIUS;
		if(ball.locked(playerNum)){
			ball.setLocked(playerNum, false);
			lockCoolDown = false;
			
			if(angle<0)
				angle+=2f*(float)Math.PI;
			if(prevAngle<0)
				prevAngle+=2f*(float)Math.PI;
			
			//This should keep angles subtracting in the correct direction
			if((angle<(float)Math.PI/2f || angle>(float)Math.PI*1.5f) && (prevAngle>(float)Math.PI*1.5f || prevAngle<(float)Math.PI/2f)){
				if(angle>(float)Math.PI)
					angle-=2f*(float)Math.PI;
				if(prevAngle>(float)Math.PI)
					prevAngle-=2f*(float)Math.PI;
			}
			
			tempf = (angle-prevAngle);
			if(mag(vel)>0 && tempf!=0){
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
