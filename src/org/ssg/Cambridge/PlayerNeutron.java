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
	int gravCircDir;//1 for increase, -1 for decrease, 0 for stand
	
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
		
		GRAVRANGE = 250f;
		gravRange = 0;
		gravCircDir = 0;

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
		
		if(gravCircDir == 1){
			g.setColor(getColor(2f*(1f-gravRange/GRAVRANGE)));
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
			}else if(actionButton2.getPollData() == 0.0 && button2Pressed){
				button2Pressed = false;
			}
		}

		updatePos(delta);

		updateCounters(delta);
		
		velMag = approachTarget(velMag, targetVelMag, .05f);
		
		if(gravCircDir != 0){
			gravRange += (float)delta/2f;
			if(gravRange > GRAVRANGE){
				gravCircDir = 0;
				gravRange = 0;
				powerKeyReleased();
			}
		} 	
		
		tempf = dist(ball.getX(), ball.getY(), pos[0], pos[1]);
		if(tempf <= gravRange && gravCircDir == 1 && !ball.scored()){
//			System.out.println(dist(ball.getX(), ball.getY(), pos[0], pos[1]));
			parallelComponent(ball.getVel(), new float[]{ball.getX()-pos[0], ball.getY()-pos[1]}, tempArr);
			//ball.setCurve(new float[]{tempArr[0], tempArr[1]}, 5f*(2f-tempf/GRAVRANGE));
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
//				ball.speedUp(VELMAG+NORMALKICK, .01f, 0);
			}
		}
		
		theta+= omega*delta;
		if(theta>360) theta-=360;
	}

	@Override
	public void activatePower() {
		gravCircDir = 1;
		gravRange = 0;
		targetVelMag = VELMAG/3f; 
	}

	@Override
	public void powerKeyReleased() {
		targetVelMag = VELMAG;
		//gravCircDir = 0;
		//gravRange = 0;
	}

	@Override
	public boolean isKicking() {
		return !buttonPressed && kickingCoolDown == 0;
	}

	@Override
	public boolean flashKick() {
		return false;
	}

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
