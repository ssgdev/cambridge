package org.ssg.Cambridge;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import paulscode.sound.SoundSystem;

public class PlayerNeutron extends Player {

	Ball ball;
	
	Component actionButton2;
	boolean buttonPressed, button2Pressed;

	float targetVelMag;
	
	float GRAVRANGE;
	float gravRange;
	int gravDir;//1 for increase, -1 for decrease, 0 for stand
	
	float prevDot;//Used to tell when the ball crosses 90 degrees with the player
	boolean orbiting;
	float orbitAngle;
	float orbitOmega;
	
	boolean pushCoolDown;
	boolean pullCoolDown;
	boolean lockCoolDown;
	
	public PlayerNeutron(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc, Ball b) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, slc);

		NORMALKICK = 1f;		
		//KICKRANGE *= .6f;
		
		ball = b;
		
		if(cExist){
			actionButton2 = this.c.getComponent(Component.Identifier.Button._4); 
		}
		
		buttonPressed = false;
		button2Pressed = false;
	
		targetVelMag = VELMAG;
		
		GRAVRANGE = 300f;
		gravRange = 0;
		gravDir = 0;
		prevDot = 0;
		orbiting = false;
		orbitAngle = 0f;
		orbitOmega = 0f;
		pushCoolDown = false;
		pullCoolDown = false;
		
		lockCoolDown = false;
	}

	@Override
	public void drawKickCircle(Graphics g){
		//Draw kicking circle
		g.setColor(getColor(.5f).darker());
		g.drawOval(pos[0]-KICKRANGE/2, pos[1]-KICKRANGE/2, KICKRANGE, KICKRANGE);
		
		//Kicking circle flash when kick happens
		g.setColor(getColor2().brighter());
		g.drawOval(pos[0]-KICKRANGE/2f, pos[1]-KICKRANGE/2f, KICKRANGE, KICKRANGE);
		
		if(gravDir == 1){
			g.setColor(getColor(2f*(1f-gravRange/GRAVRANGE)));
			g.drawOval(pos[0]-gravRange/2f, pos[1]-gravRange/2f, gravRange, gravRange);
		}else if(gravRange == -1){
			g.setColor(getColor(1f));
			g.drawOval(pos[0]-gravRange/2f, pos[1]-gravRange/2f, gravRange, gravRange);
		}
//		g.setColor(getColor());
//		g.drawOval(pos[0]-GRAVRANGE/2f, pos[1]-GRAVRANGE/2f, GRAVRANGE, GRAVRANGE);

	}
	
	@Override
	public void update(float delta) {
		if (cExist) {
			pollController(delta);
			
			if (actionButton.getPollData() == 1.0 && !buttonPressed){
				buttonPressed = true;
				activatePower();
			}else if(actionButton.getPollData() == 0 && buttonPressed){
				buttonPressed = false;
				powerKeyReleased();
			}
			
			if (actionButton2.getPollData() == 1.0 && !button2Pressed){
				button2Pressed = true;
				activatePower2();
			}else if(actionButton2.getPollData() == 0.0 && button2Pressed){
				button2Pressed = false;
				powerKey2Released();
			}
		}

		updatePos(delta);

		updateCounters(delta);
		
		velMag = approachTarget(velMag, targetVelMag, .05f);
		
		if(gravDir == 1){
			gravRange += (float)delta/2f;
			if(gravRange > GRAVRANGE){
				gravDir = 0;
				gravRange = 0;
				targetVelMag = VELMAG;
//				powerKeyReleased();
			}
		}
		
		tempf = dist(ball.getX(), ball.getY(), pos[0], pos[1]);
		if(!pushCoolDown && tempf <= gravRange/2f && gravDir == 1 && !ball.scored()){//Grav Push
//			System.out.println(dist(ball.getX(), ball.getY(), pos[0], pos[1]));
			parallelComponent(ball.getVel(), new float[]{ball.getX()-pos[0], ball.getY()-pos[1]}, tempArr);
			ball.setCurve(new float[]{ball.getX()-pos[0], ball.getY()-pos[1]}, 1f*(2f-tempf/GRAVRANGE));
			//ball.speedUp(ball.getVelMag()+.3f, .05f, 0);
			if(sameDir(tempArr[0], pos[0]-ball.getX()) && sameDir(tempArr[1], pos[1]-ball.getY())){
				if(ball.getVelMag()>0.02f){
					ball.slowDown(0.01f, .002f, 0);
				}else{
					ball.setVel(new float[]{ball.getX()-pos[0], ball.getY()-pos[1]}, ball.getVelMag());
					ball.speedUp(VELMAG+NORMALKICK, .01f, 0);
				}
			}else{
//				ball.setVel(new float[]{tempArr[0],  tempArr[1]}, ball.getVelMag());
				ball.speedUp((1f-gravRange/GRAVRANGE)*NORMALKICK+ball.getVelMag(), .01f, 0);
			}
			ball.setLastKicker(playerNum);
			ball.clearLocked();
			pushCoolDown = true;
			kickingCoolDown = KICKCOOLDOWN;
		}else if(!pullCoolDown && tempf <= gravRange/2f && gravDir == -1 && !ball.scored()){//Grav Pull
//			ball.slowDown(ball.getVelMag()/2f, 0.01f, 0); //Might need to depend on speed. No point slowing down a slow ball.
			tempArr[0] = ball.getX()-pos[0];
			tempArr[1] = ball.getY()-pos[1];
			prevDot = Math.abs(dot(ball.getVel(), tempArr));
			ball.setLocked(playerNum, true);
			pullCoolDown = true;
		}
		
		if(ball.locked(playerNum)){
			if(!orbiting){
				tempArr[0] = ball.getX()-pos[0];
				tempArr[1] = ball.getY()-pos[1];
				if(!sameDir(prevDot, Math.abs(dot(ball.getVel(), tempArr)))){//When it crosses 90 degrees, seize it into circular orbit
					//Determine initial angle and direction of rotation and angular velocity based on velocity
					orbitAngle = (float)Math.atan2(tempArr[1], tempArr[0]);
					orbitOmega = ball.getVelMag()/mag(tempArr);
					orbiting = true;
				}
			}else{
				
			}
		}
		
		theta+= omega*delta;
		if(theta>360) theta-=360;
	}

	@Override
	public void activatePower() {
		gravDir = 1;
		gravRange = 0;
		targetVelMag = VELMAG/3f;
		pushCoolDown = false;
	}

	@Override
	public void powerKeyReleased() {
		//targetVelMag = VELMAG;
		//gravDir = 0;
		//gravRange = 0;
	}

	public void activatePower2(){
		gravRange = GRAVRANGE;
		gravDir = -1;
		targetVelMag = 0;
		pullCoolDown = false;
	}
	
	public void powerKey2Released(){
		gravRange = 0;
		targetVelMag = VELMAG;
		//Launch the ball if it was orbiting, resume the ball if it was locked
		orbiting = false;
	}
	
	@Override
	public boolean isKicking() {
		return !buttonPressed && kickingCoolDown == 0;
	}

	@Override
	public boolean flashKick() {
		return orbiting;
	}

	//@Override setKicking() to cancel orbit after a flash kick
	
	@Override
	public void setPower() {

	}
	
	//Parallel component of u on v, written to w
	public void parallelComponent(float[] u, float[] v, float[] w){
		tempf = (u[0]*v[0]+u[1]*v[1])/mag(v)/mag(v);
		w[0] = v[0]*tempf;
		w[1] = v[1]*tempf;		
	}

}
