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

public class PlayerDash extends Player{

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
	Image slice_tri;
	
	float[] dashVel;
	float dashDist;
	float DASHDURATION = 500;//The time window in which the trail can "catch" the ball
	float gustCountDown;
	float gustCoolDown;//Used for drawing the ghost kicker
	float GUSTCOUNTDOWN = 800;
	float[] gustVel;
	float[] gustCurve;
	
	//Used to draw the ghost kicker
	float[] lastBallPos;
	float[] lastGustVel;
	
	boolean shortDash;
	float shortDashCoolDown;
	float SHORTDASHCOOLDOWN = 500;
	
	Ball ball;
	
	public PlayerDash(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc, Image slc_t, Ball b, Image hc) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, slc);

		MAXPOWER = 100;
		POWERCOOLDOWN = 500;
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
		slice_tri = slc_t;
		
		dashVel = new float[2];
		powerCoolDown = 0;
		
		dashDist = 0;
		gustCountDown = 0;
		gustCoolDown = 0;
		gustVel = new float[2];
		gustCurve = new float[2];
		lastBallPos = new float[2];
		lastGustVel = new float[2];
		
		shortDash = false;
		shortDashCoolDown = 0;
		
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
		g.fillRect(prevPostPos[2], prevPostPos[3]-KICKRANGE/2, -.5f-3200f, KICKRANGE);
		g.drawImage(hemicircle.getFlippedCopy(true, false), prevPostPos[2], prevPostPos[3]-KICKRANGE/2, getColor5()); 
		g.rotate(prevPostPos[2], prevPostPos[3], -360f/2f/(float)Math.PI*thetaTemp);
//		g.rotate(prevPostPos[0], prevPostPos[1], 360f/2f/(float)Math.PI*thetaTemp);
//		g.drawImage(hemicircle, prevPostPos[0]-KICKRANGE/2, prevPostPos[1]-KICKRANGE/2, getColor5());
//		g.rotate(prevPostPos[0], prevPostPos[1], -360f/2f/(float)Math.PI*thetaTemp);
		g.setLineWidth(5f);
		
		//Ghostly Gust Kicker
		g.setColor(getColor6());
		if(gustCountDown>0 && ball.gustReady()){
			//tempArr[0] = prevPostPos[2]-prevPostPos[0];
			//tempArr[1] = prevPostPos[3]-prevPostPos[1];
			//unit(tempArr);
			//g.drawOval(ball.getX()-1.5f*tempArr[0]*gustCountDown-KICKRANGE/2f, ball.getY()-1.5f*tempArr[1]*gustCountDown-KICKRANGE/2f, KICKRANGE, KICKRANGE);
			g.drawOval(ball.getX()-gustCountDown/2f, ball.getY()-gustCountDown/2f, gustCountDown, gustCountDown);
			tempf = 360f/2f/(float)Math.PI*(float)Math.atan2(gustVel[1], gustVel[0]);
			g.rotate(ball.getX(), ball.getY(), tempf);
			g.drawImage(slice_tri, ball.getX()-slice_tri.getWidth()/2f, ball.getY()-slice_tri.getHeight()/2f, getColor6());
			g.rotate(ball.getX(), ball.getY(), -tempf);
			lastBallPos[0] = ball.getX();//In case of use for if(gustCoolDown>0) drawing below
			lastBallPos[1] = ball.getY();
			lastGustVel[0] = gustVel[0];
			lastGustVel[1] = gustVel[1];
		}else if(gustCoolDown>0){
			//Maybe draw something after the gust kick
		}
	}
	
	public Color getColor6(){//gusting fade
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), 1f-gustCountDown/GUSTCOUNTDOWN);
	}
	
	@Override
	public void update(int delta) {

		if (cExist) {
			if(shortDashCoolDown==0)
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
			
			if (actionButton2.getPollData() == 1.0 && !button2Pressed && shortDashCoolDown == 0){
				button2Pressed = true;
				shortDash();
			}else if(actionButton2.getPollData() == 0 && button2Pressed){
				button2Pressed = false;
				shortDashReleased();
			}
		
		}
		
		if(power>0){
			velMag = 0;
		}else if(shortDash){
			velMag = 0;
		}else if(shortDashCoolDown>0){
			velMag = VELMAG*shortDashCoolDown/SHORTDASHCOOLDOWN;
		}else{
			velMag = VELMAG;
		}
		
		updatePos(delta);

		updateCounters(delta);
		
		powerCoolDown -= (float)delta;
		if(powerCoolDown<0)
			powerCoolDown = 0;
		
		gustCoolDown -= (float)delta;
		if(gustCoolDown < 0)
			gustCoolDown = 0;
		
		if(shortDashCoolDown>0){
			shortDashCoolDown -= (float)delta;
			prevPostPos[2] = pos[0];
			prevPostPos[3] = pos[1];
			if(shortDashCoolDown<0)
				shortDashCoolDown = 0;
		}
		
		
		if(power>0){
			power -= (float)delta/12f;
			if(power<=0){
				powerKeyReleased();
				
				if(mag(vel)>0){
					mySoundSystem.quickPlay( true, "ChargeDash.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
					lastKickAlpha = 1f;
					
					parallelComponent(new float[] {ball.getX()-pos[0], ball.getY()-pos[1]}, vel, ballParallel);
					ballOrth[0] = ball.getX()-pos[0]-ballParallel[0];
					ballOrth[1] = ball.getY()-pos[1]-ballParallel[1];
					
					if(!ball.scored() && mag(ballOrth)<TRAILRANGE/2f && ball.getX()>=xyLimit[0]&&ball.getX()<=xyLimit[1]&&ball.getY()>=xyLimit[2]&&ball.getY()<=xyLimit[3]){
						if(sameDir(vel,ballParallel)){//Set up the gust
							//ball.setVel(new float[]{ball.getVelX(), ball.getVelY()}, ball.getVelMag()/2f);//Maybe slow it down?
							ball.setReadyForGust(true);
							gustCountDown = GUSTCOUNTDOWN;
						}else if(mag(ballOrth)<KICKRANGE/2f + 10f){//If it's behind you, backkick relative to distance
							ball.setVel(new float[]{ballParallel[0], ballParallel[1]}, 3f*POWERKICK);
						}
						ball.setLastKicker(playerNum);
						ball.setCanBeKicked(playerNum, false);
						kickingCoolDown = KICKCOOLDOWN;
					}else if(!ball.scored() && ball.getX()>=xyLimit[0]&&ball.getX()<=xyLimit[1]&&ball.getY()>=xyLimit[2]&&ball.getY()<=xyLimit[3]){//The second clause, for "catching" ball in the trail
						//Find the orthogonal component of the ball's velocity relative to the trail
						tempArr = normal(new float[]{ball.getVelX()*ball.getVelMag(), ball.getVelY()*ball.getVelMag()}, vel);//Note the magnitude of tempArr matters this time
						//if the ball is heading towards the trail
						if(sameDir(tempArr, new float[]{-ballOrth[0], -ballOrth[1]})){
							//calculate how much how much time until the ball will reach the trail
							tempf = mag(ballOrth)/(mag(tempArr));
							//If that is within the accepted limit, set up the gust with the added delay
							if(tempf<DASHDURATION){
								ball.setReadyForGust(true);
								gustCountDown = GUSTCOUNTDOWN + tempf;
								ball.setLastKicker(playerNum);
								ball.setCanBeKicked(playerNum, false);
								kickingCoolDown = KICKCOOLDOWN;
							}
						}
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
					
					gustVel[0] = prevPostPos[2] - prevPostPos[0];
					gustVel[1] = prevPostPos[3] - prevPostPos[1];
					
					dashDist = mag(new float[]{prevPostPos[2]-prevPostPos[0], prevPostPos[3]-prevPostPos[1]});
					
					prevPostPos[0] = pos[0]-3000f*vel[0];
					prevPostPos[1] = pos[1]-3000f*vel[1];
					
					vel[0] = -vel[0];//Bouncing off the wall
					vel[1] = -vel[1];
					setStunned(MAXSTUN, new float[]{vel[0],vel[1]}, velMag);
					
					//powerCoolDown = POWERCOOLDOWN;
				}else{
					mySoundSystem.quickPlay( true, "ChargeWindingDown.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}
			}
		}
		
		//Delayed gusting of ball
		if(gustCountDown>0){
			//Shortdash direction can change the gust direction.
			//The second part of the condition is so shortdashing can't slow down the gust countdown before the ball has touched the trail
			if(shortDash && gustCountDown<GUSTCOUNTDOWN){
				gustCountDown -= (float)delta/2f;
				gustVel[0] = vel[0];
				gustVel[1] = vel[1];
			}else{
				gustCountDown -= (float)delta;
			}
			if(gustCountDown <= 0){
				if(!ball.scored() && ball.gustReady()){
					ball.clearLocked();
					ball.setVel(new float[]{gustVel[0], gustVel[1]}, .1f);
					ball.speedUp(POWERKICK+VELMAG*2f, 0, .02f);
					kickingCoolDown = KICKCOOLDOWN;
					mySoundSystem.quickPlay( true, "ChargeGust.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
					lastKickAlpha = 1f;//or maybe kickingCoolDown = KICKCOOLDOWN;
					
					gustCoolDown = GUSTCOUNTDOWN;
				}
			}	
		}
		
		//Angle code
		if(power>0 || shortDash){
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
		mySoundSystem.quickPlay( true, "ChargeCharging.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
//		velMag = 0;//Unneeded, as velMag is set in update anyway
	}

	@Override
	public void powerKeyReleased() {
		power = 0;
//		velMag = VELMAG;
	}
	
	public void shortDash(){
//		velMag = 0;
		shortDash = true;
	}
	
	public void shortDashReleased(){//More of a teleport
		if(mag(vel)>0){
			mySoundSystem.quickPlay( true, "ChargeShortDash.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			
			parallelComponent(new float[]{ball.getX()-pos[0], ball.getY()-pos[1]}, vel, ballParallel);
			ballOrth[0] = ball.getX()-pos[0]-ballParallel[0];
			ballOrth[1] = ball.getY()-pos[1]-ballParallel[1];
			
			prevPostPos[0] = pos[0];
			prevPostPos[1] = pos[1];
			
			//for(int i=0;i<100 && pos[0]-KICKRANGE/2>=xyLimit[0]&&pos[0]+KICKRANGE/2<=xyLimit[1]&&pos[1]-KICKRANGE/2>=xyLimit[2]&&pos[1]+KICKRANGE/2<=xyLimit[3];i++){
			for(int i=0; i<150 ; i++){
				pos[0]+=vel[0];
				pos[1]+=vel[1];
			}
			
			pos[0]-=vel[0];
			pos[1]-=vel[1];
	
			prevPostPos[2] = pos[0];
			prevPostPos[3] = pos[1];
			
			dashDist = mag(new float[]{prevPostPos[2]-prevPostPos[0], prevPostPos[3]-prevPostPos[1]});
			
			prevPostPos[0] = pos[0]-3000f*vel[0];
			prevPostPos[1] = pos[1]-3000f*vel[1];
			
			gustVel[0] = prevPostPos[2] - prevPostPos[0];
			gustVel[1] = prevPostPos[3] - prevPostPos[1];
			
			lastKickAlpha = 1f;
			
			shortDashCoolDown = SHORTDASHCOOLDOWN;
			
			if(mag(ballOrth)<KICKRANGE/2f && mag(ballParallel)<dashDist+KICKRANGE/2f && sameDir(vel,ballParallel)){
				unit(ballParallel);
				ball.setPos(pos[0]+ballParallel[0]*KICKRANGE/2f+4f, pos[1]+ballParallel[1]*KICKRANGE/2f+4f);//Teleport ball to front
				ball.setVel(new float[]{ballParallel[0], ballParallel[1]}, POWERKICK+VELMAG);
				ball.setLastKicker(playerNum);
				ball.setCanBeKicked(playerNum, false);
				ball.cancelAcc();
				ball.clearLocked();
				kickingCoolDown = KICKCOOLDOWN;
				if(slowMo){
					mySoundSystem.quickPlay( true, "KickBumpSlow.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}else{
					mySoundSystem.quickPlay( true, "KickBump.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}
			}
		}
		shortDash = false;
//		velMag = VELMAG;
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
	
	public float[] normalNeg(float[] v, float[] w){//orthogonal proj v on w, negative
		tempf = dot(v,w)/mag(w);
		return new float[]{-v[0]+tempf*w[0], -v[1]+tempf*w[1]};
	}
	
	public float[] normal(float[] v, float[] w){//orthogonal proj v on w
		tempf = dot(v,w)/mag(w);
		return new float[]{v[0]-tempf*w[0], v[1]-tempf*w[1]};
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