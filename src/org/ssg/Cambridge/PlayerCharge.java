package org.ssg.Cambridge;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Transform;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerCharge extends Player{

	Polygon poly;
	//Theta is between -pi and pi, theta2 is 0 to 2pi
	float theta2, thetaTarget, thetaTarget2;
	float[] prevPostPos;
	boolean buttonPressed;
	boolean button2Pressed;
	Component actionButton2;
	
	//The vector between Charge and the ball, divided into components || and normal to Charge's velocity
	float[] ballParallel, ballOrth;
	float TRAILRANGE;
	Image hemicircle;

	float[] dashVel;
	float dashDist;
	float gustCountDown;
	float GUSTCOUNTDOWN = 500;
	boolean shortDash;
	
	Ball ball;
	
	public PlayerCharge(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Ball b, Image hc) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn);

		MAXPOWER = 100;
		TRAILRANGE = KICKRANGE*1.4f;

		poly = new Polygon(new float[]{0,0,-PLAYERSIZE/3, -PLAYERSIZE/2, PLAYERSIZE*2/3, 0, -PLAYERSIZE/3, PLAYERSIZE/2});
		ballParallel = new float[2];
		ballOrth = new float[2];
		theta2 = theta;
		thetaTarget = theta;
		thetaTarget2 = theta;
		prevPostPos = new float[4];
		
		buttonPressed = false;
		if (cExist) {
			actionButton2 = this.c.getComponent(Component.Identifier.Button._4); 
		}
		button2Pressed = false;
		
		hemicircle = hc.getScaledCopy(KICKRANGE/hc.getHeight());
		
		dashVel = new float[2];
		powerCoolDown = 0;
		
		dashDist = 0;
		gustCountDown = 0;
		shortDash = false;
		
		ball = b;
	}

	//Show if stick is being held down during a dash charge
	@Override
	public void drawKickCircle(Graphics g){
		//Draw kicking circle
		if(power>0){
			g.setColor(getColor(.2f+mag(vel)/VELMAG/2f));
		}else{
			g.setColor(getColor(.5f).darker());
		}
		g.drawOval(getX()-getKickRange()/2, getY()-getKickRange()/2, getKickRange(), getKickRange());
		//Kicking circle flash when kick happens
		g.setColor(getColor2().brighter());
		g.drawOval(getX()-getKickRange()/2f, getY()-getKickRange()/2f, getKickRange(), getKickRange());
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
	public void drawKickTrail(Graphics g){
		g.setColor(getColor5());
//		tempTrailArr = p.getTrailArr();//{bx, by, px, py}
		float dx = prevPostPos[2]-prevPostPos[0];
		float dy = prevPostPos[3]-prevPostPos[1];
		float thetaTemp = (float)Math.atan2((double)dy, (double)dx);
		g.rotate(prevPostPos[2], prevPostPos[3], 360f/2f/(float)Math.PI*thetaTemp);
		g.fillRect(prevPostPos[2], prevPostPos[3]-KICKRANGE/2, -.5f-mag(new float[]{prevPostPos[2]-prevPostPos[0], prevPostPos[3]-prevPostPos[1]}), KICKRANGE);
		g.drawImage(hemicircle.getFlippedCopy(true, false), prevPostPos[2], prevPostPos[3]-KICKRANGE/2, getColor5()); 
		g.rotate(prevPostPos[2], prevPostPos[3], -360f/2f/(float)Math.PI*thetaTemp);
		g.rotate(prevPostPos[0], prevPostPos[1], 360f/2f/(float)Math.PI*thetaTemp);
		g.drawImage(hemicircle, prevPostPos[0]-KICKRANGE/2, prevPostPos[1]-KICKRANGE/2, getColor5());
		g.rotate(prevPostPos[0], prevPostPos[1], -360f/2f/(float)Math.PI*thetaTemp);
		g.setLineWidth(5f);
	}
	
	@Override
	public void update(int delta) {

		if (cExist) {
			if(powerCoolDown == 0)//No controller listening when cooling down from a dash
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
			
			if (actionButton2.getPollData() == 1.0 && !button2Pressed){
				button2Pressed = true;
			}else if(actionButton2.getPollData() == 0 && button2Pressed){
				button2Pressed = false;
			}
		
		}
		
		if(power>0){
			velMag = 0;
		}else if(powerCoolDown>0){
			velMag = .5f*VELMAG*powerCoolDown/POWERCOOLDOWN;
		}else{
			velMag = VELMAG;
		}
		
		updatePos(delta);

		stun -= (float)delta;
		if(stun<=0){
			stun = 0;
		}
		
		lastKickAlpha -= (float)(delta)/1200f;
		if(lastKickAlpha<0){
			lastKickAlpha = 0f;
		}
		
		kickingCoolDown -= (float)delta;
		if(kickingCoolDown<0)
			kickingCoolDown = 0;
		
		powerCoolDown -= (float)delta;
		if(powerCoolDown<0)
			powerCoolDown = 0;
		
		if(power>0){
			power -= (float)delta/12f;
			if(power<=0){
				powerKeyReleased();
				
				if(mag(vel)>0){
					mySoundSystem.quickPlay( true, "pow2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
					lastKickAlpha = 1f;
					
					parallelComponent(new float[] {ball.getX()-pos[0], ball.getY()-pos[1]}, vel, ballParallel);
					ballOrth[0] = ball.getX()-pos[0]-ballParallel[0];
					ballOrth[1] = ball.getY()-pos[1]-ballParallel[1];
					
					if(!ball.scored() && mag(ballOrth)<TRAILRANGE/2f && ball.getX()>=xyLimit[0]&&ball.getX()<=xyLimit[1]&&ball.getY()>=xyLimit[2]&&ball.getY()<=xyLimit[3]){
						if(sameDir(vel,ballParallel)){//Push it aside
							ball.setVel(new float[]{ball.getVelX(), ball.getVelY()}, ball.getVelMag()/2f);
							ball.setReadyForGust(true);
							gustCountDown = GUSTCOUNTDOWN;
							//ball.setVel(new float[]{2f*ballOrth[0]-tempf*ballParallel[0],2f*ballOrth[1]-tempf*ballParallel[1]}, 1.5f*POWERKICK);
							//ball.setAcc(new float[]{-ballParallel[0],-ballParallel[1]}, 2f*(1f-tempf));
						}else if(mag(ballOrth)<KICKRANGE/2){//If it's behind you, backkick relative to distance
							ball.setVel(new float[]{ballParallel[0], ballParallel[1]}, 3f*POWERKICK);
						}
						ball.setLastKicker(playerNum);
						ball.setCanBeKicked(playerNum, false);
						kickingCoolDown = KICKCOOLDOWN;
					}
					
					prevPostPos[0] = pos[0];
					prevPostPos[1] = pos[1];
					
					//Dash to wall
					while(pos[0]-KICKRANGE/2>=xyLimit[0]&&pos[0]+KICKRANGE/2<=xyLimit[1]&&pos[1]-KICKRANGE/2>=xyLimit[2]&&pos[1]+KICKRANGE/2<=xyLimit[3]){
						pos[0]+=vel[0];
						pos[1]+=vel[1];
					}
					
					//Pull back from wall
					pos[0]-=vel[0];
					pos[1]-=vel[1];
					
					prevPostPos[2] = pos[0];
					prevPostPos[3] = pos[1];
					
					dashDist = mag(new float[]{prevPostPos[2]-prevPostPos[0], prevPostPos[3]-prevPostPos[1]});
					
					prevPostPos[0] = pos[0]-2400f*vel[0];
					prevPostPos[1] = pos[1]-2400f*vel[1];
					
					vel[0] = -vel[0];
					vel[1] = -vel[1];
					
					powerCoolDown = POWERCOOLDOWN;

				}
			}
		}
		
		//Delayed gusting of ball
		if(gustCountDown>0){
			gustCountDown -= (float)delta;
			if(gustCountDown <= 0){
				if(!ball.scored() && ball.gustReady() && dashDist > 0){
					ball.clearLocked();
					ball.setVel(new float[]{ballParallel[0],  ballParallel[1]}, .1f);
					ball.speedUp(POWERKICK+VELMAG*2f, 0, .02f);
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
		if(powerCoolDown >= POWERCOOLDOWN-400){//For dash chaining, untested
			power = 50;
			powerCoolDown = 0;
		}else{
			power = MAXPOWER;
		}
		velMag = 0;
		mySoundSystem.quickPlay( true, "whoosh2r.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
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
	
	//vx is vel, dir is ballParallel
	public boolean sameDir(float[] vx, float[] dir){
		if(mag(vx)==0 || mag(dir) == 0)//Unsure about this, should never be called though
			return false;
		if(vx[0]==0 && vx[1]!=0)
			return vx[1]/Math.abs(vx[1]) == dir[1]/Math.abs(dir[1]);
		if(vx[1]==0 && vx[0]!=0)
			return vx[0]/Math.abs(vx[0]) == dir[0]/Math.abs(dir[0]);
		return vx[0]/Math.abs(vx[0]) == dir[0]/Math.abs(dir[0]) && vx[1]/Math.abs(vx[1])==dir[1]/Math.abs(dir[1]);
	}
	
}
