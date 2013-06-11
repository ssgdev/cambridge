package org.ssg.Cambridge;

import net.java.games.input.Controller;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Transform;

import paulscode.sound.SoundSystem;

public class PlayerCharge extends Player{

	//The vector between Charge and the ball, divided into components || and normal to Charge's velocity
	float[] ballParallel, ballOrth;
	float TRAILRANGE;
	
	Polygon poly;
	//Theta is between -pi and pi, theta2 is 0 to 2pi
	float theta2, thetaTarget, thetaTarget2;
	boolean buttonPressed;
	
	Ball ball;
	
	public PlayerCharge(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Ball b) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn);

		MAXPOWER = 100;
		TRAILRANGE = KICKRANGE+50;

		poly = new Polygon(new float[]{0,0,-PLAYERSIZE/3, -PLAYERSIZE/2, PLAYERSIZE*2/3, 0, -PLAYERSIZE/3, PLAYERSIZE/2});
		ballParallel = new float[2];
		ballOrth = new float[2];
		theta2 = theta;
		thetaTarget = theta;
		thetaTarget2 = theta;
		
		buttonPressed = false;
		
		ball = b;
	}

	@Override
	public void drawPlayer(Graphics g){
		g.setColor(getColor());
		g.setLineWidth(2f);
		g.translate(pos[0],pos[1]);
		poly = (Polygon) poly.transform(Transform.createRotateTransform(theta));
		g.draw(poly);
		poly = (Polygon) poly.transform(Transform.createRotateTransform(-theta));
		g.translate(-pos[0], -pos[1]);
		g.setLineWidth(5f);
		
		//Debugging
//		g.setColor(getColor());
//		g.drawLine(pos[0], pos[1], pos[0]+ballParallel[0], pos[1]+ballParallel[1]);
//		g.drawLine(pos[0], pos[1], pos[0]+ballOrth[0], pos[1]+ballOrth[1]);
		
	}
	
	@Override
	public void update(int delta) {

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

		updatePos(delta);

		lastKickAlpha -= (float)(delta)/2400f;
		if(lastKickAlpha<0){
			lastKickAlpha = 0f;
		}
		
		kickingCoolDown -= (float)delta;
		if(kickingCoolDown<0)
			kickingCoolDown = 0;
		
		if(power>0){
			power -= (float)delta/12f;
			if(power<=0){
				powerKeyReleased();
				//In this case commandeered to draw the dash trail
				setLastKick(pos[0],pos[1],pos[0]+vel[0],pos[1]+vel[1],1f);
				
				parallelComponent(new float[] {ball.getX()-pos[0], ball.getY()-pos[1]}, vel, ballParallel);
				ballOrth[0] = ball.getX()-pos[0]-ballParallel[0];
				ballOrth[1] = ball.getY()-pos[1]-ballParallel[1];
				
				if(mag(ballOrth)<TRAILRANGE/2){
					if(sameDir(vel[0],ballParallel[0])&&sameDir(vel[1],ballParallel[1])){//If it's behind you, backkick relative to distance
					
					}else{//Push it aside
						
					}
				}
				
			}
		}
		
		//Angle code
		if(power>0){
			if(mag(vel)!=0)
				thetaTarget = (float)Math.atan2(vel[1],vel[0]);
			
			theta2 = theta;
			if(theta2<0)
				theta2 += 2f*(float)Math.PI;
			thetaTarget2 = thetaTarget;
			if(thetaTarget2<0)
				thetaTarget2 += 2f*(float)Math.PI;
	
			//Choose the direction of shortest rotation
			if(Math.abs(thetaTarget-theta)-Math.abs(thetaTarget2-theta2) >= 0){
				theta = approachTarget(theta2, thetaTarget2, (float)delta/120f);
			}else{
				theta = approachTarget(theta, thetaTarget, (float)delta/120f);
			}
			
			//Set theta between -pi and pi, for the next round of calculation
			if(theta>(float)Math.PI)
				theta-=(float)Math.PI*2f;
			
		}else{
			theta += omega*(float)delta/60f*Math.PI;
			if(theta>2f*(float)Math.PI)
				theta-=2f*(float)Math.PI;
		}
	}

	@Override
	public void activatePower() {
		power = MAXPOWER;
		velMag = 0;
	}

	@Override
	public void powerKeyReleased() {
		power = 0;
		velMag = VELMAG;
	}

	@Override
	public boolean isKicking() {
		return true;
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
	
	public boolean sameDir(float vx, float dir){
		if(vx == 0)
			return false;
		return vx/Math.abs(vx) == dir/Math.abs(dir);
	}
	
}
