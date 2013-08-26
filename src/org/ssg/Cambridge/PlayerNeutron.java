package org.ssg.Cambridge;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerNeutron extends Player {

	Ball ball;

	float targetVelMag;
	
	float GRAVRANGE;
	float gravRange;
	int gravDir;//1 for increase, -1 for decrease, 0 for stand
	
	float prevDot;//Used to tell when the ball crosses 90 degrees with the player
	boolean orbiting;
	float orbitAngle;
	float orbitDir;
	float orbitOmega;
	float orbitRadius;
	float orbitCounter;//Used for playing the whoosh sound effect per orbit
	float OMEGALOW;
	float ORBITVEL = .7f;
	
	boolean pushCoolDown;
	boolean pullCoolDown;
	boolean lockCoolDown;
	
	public PlayerNeutron(int n, int tN, float[] consts, int[] f, int[] c, CambridgeController c1, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc, Ball b) {
		super(n, tN, consts, f, c, c1, p, xyL, se, ss, sn, slc);

		NORMALKICK = 1f;
		KICKRANGE *= .7f;
		
		ball = b;

		targetVelMag = VELMAG;
		
		GRAVRANGE = 250f;
		gravRange = 0;
		gravDir = 0;
		prevDot = 0;
		orbiting = false;
		orbitAngle = 0f;
		orbitOmega = 0f;
		orbitDir = 0f;
		orbitRadius = 0f;
		orbitCounter = 0f;
		pushCoolDown = false;
		pullCoolDown = false;
		
		lockCoolDown = false;
	}

	@Override
	public void drawPlayer(Graphics g){
		g.rotate(pos[0], pos[1], theta);
		g.setColor(getColor());
		g.setLineWidth(2);
		g.drawOval(pos[0]-PLAYERSIZE/2, pos[1]-PLAYERSIZE/2,  PLAYERSIZE,  PLAYERSIZE);
		g.setLineWidth(1f);
		for(int i=0; i<6; i++){
			g.drawLine(pos[0]+(float)Math.cos(i*Math.PI/3)*PLAYERSIZE/2, pos[1]+(float)Math.sin(i*Math.PI/3)*PLAYERSIZE/2,
					pos[0]+(float)Math.cos(i*Math.PI/3+Math.PI*.6f)*PLAYERSIZE/2, pos[1]+(float)Math.sin(i*Math.PI/3+Math.PI*.6f)*PLAYERSIZE/2);
		}
		g.rotate(pos[0], pos[1], -theta);
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
		}else if(gravDir == -1){
			g.setColor(getColor(orbiting? 1f: .4f));
			g.drawOval(pos[0]-gravRange/2f, pos[1]-gravRange/2f, gravRange, gravRange);
		}
//		g.setColor(getColor());
//		g.drawOval(pos[0]-GRAVRANGE/2f, pos[1]-GRAVRANGE/2f, GRAVRANGE, GRAVRANGE);

	}
	
	@Override
	public void update(float delta) {
		if (c.exists()) {
			pollController(delta);
			
			if (c.getAction() && !buttonPressed){
				buttonPressed = true;
				activatePower();
			}else if(!c.getAction() && buttonPressed){
				buttonPressed = false;
				powerKeyReleased();
			}
			
			if (c.getAction2() && !button2Pressed){
				button2Pressed = true;
				activatePower2();
			}else if(!c.getAction2() && button2Pressed){
				button2Pressed = false;
				powerKey2Released();
			}
		}else{
			
			pollKeys(delta);
			
			if(buttonPressed){
				activatePower();
				buttonPressed = false;
			}

			if(button2Pressed){
				activatePower2();
				button2Pressed = false;
			}
			if(button2Released){
				powerKey2Released();
				button2Released = false;
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
					ball.slowDown(-VELMAG-NORMALKICK, .01f, 0);
				}else{
					ball.setVel(new float[]{ball.getX()-pos[0], ball.getY()-pos[1]}, ball.getVelMag());
					ball.speedUp(VELMAG+NORMALKICK, .01f, 0);
				}
			}else{
//				ball.setVel(new float[]{tempArr[0],  tempArr[1]}, ball.getVelMag());
				ball.speedUp((1.5f-gravRange/GRAVRANGE)*NORMALKICK+ball.getVelMag(), .01f, 0);
			}
			ball.setLastKicker(teamNum);
			ball.clearLocked();
			pushCoolDown = true;
			kickingCoolDown = KICKCOOLDOWN;
		}else if(!pullCoolDown && tempf <= gravRange/2f && gravDir == -1 && !ball.scored()){//Grav Pull
			tempArr[0] = ball.getX()-pos[0];
			tempArr[1] = ball.getY()-pos[1];
			prevDot = dot(ball.getVel(), tempArr);
			//System.out.println(prevDot);
			ball.setLocked(playerNum, true);
			//ball.setLastKicker(teamNum);
			parallelComponent(ball.getVel(), new float[]{ball.getX()-pos[0], ball.getY()-pos[1]}, tempArr);
			if(sameDir(tempArr[0], pos[0]-ball.getX()) && sameDir(tempArr[1], pos[1]-ball.getY()) && ball.getVelMag()/tempf < ORBITVEL){
				ball.speedUp(ORBITVEL, .01f, 0);
				ball.setCurve(new float[]{-tempArr[0], -tempArr[1]}, .05f);
			}else{
				ball.setCurve(new float[]{-tempArr[0], -tempArr[1]}, 1f);
			}
			pullCoolDown = true;
		}
		
		if(ball.locked(playerNum)){
			if(!orbiting){
				tempArr[0] = ball.getX()-pos[0];
				tempArr[1] = ball.getY()-pos[1];
				//System.out.println(Math.abs(dot(ball.getVel(), tempArr)));
				if(!sameDir(prevDot, dot(ball.getVel(), tempArr))){//When it crosses 90 degrees, seize it into circular orbit
					orbitAngle = (float)Math.atan2(tempArr[1], tempArr[0]);
					orbitDir = (tempArr[0]*ball.getVelY()-tempArr[1]*ball.getVelX());//z component of the cross product, for orbit cw or ccw
					if(orbitDir!=0)
						orbitDir /= Math.abs(orbitDir);
					orbitOmega = ball.getVelMag()/mag(tempArr)*orbitDir;
					orbitRadius = mag(tempArr);
					orbiting = true;
					orbitCounter = (float)Math.PI;
					ball.setVel(ball.getVel(), 0);
					ball.cancelAcc();
					ball.setLastKicker(teamNum);
					mySoundSystem.quickPlay( true, slowMo?"NeutronCatchSlow.ogg":"NeutronCatch.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}
			}else{
				orbitCounter+=delta*orbitOmega;
				if(orbitCounter>(float)Math.PI*2f || orbitCounter < 0){
					orbitCounter -= Math.PI*2f*orbitDir;
					mySoundSystem.quickPlay( true, slowMo?"NeutronSwingSlow.ogg":"NeutronSwing.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}
				orbitAngle+=delta*orbitOmega;
				if(Math.abs(orbitOmega*orbitRadius)<ORBITVEL)
					orbitOmega+=orbitDir*delta*.000005f;
				if(orbitRadius > KICKRANGE/2f+15)
					orbitRadius -= delta/80f;
				ball.setPos(pos[0]+(float)Math.cos(orbitAngle)*orbitRadius, pos[1]+(float)Math.sin(orbitAngle)*orbitRadius);
				if(ball.getX()<=0 || ball.getX()>=field[0] || ball.getY()<=0 || ball.getY()>=field[1]){//Bounce off wall
					if(ball.getX()<0)
						ball.setPos(0, ball.getY());
					if(ball.getX()>field[0])
						ball.setPos(field[0], ball.getY());
					if(ball.getY()<0)
						ball.setPos(ball.getX(), 0);
					if(ball.getY()>field[1])
						ball.setPos(ball.getX(), field[1]);
					if(!((ball.getX()<=0 || ball.getX()>=field[0]) && ball.betweenGoals(ball.getX(),  ball.getY(),  ball.getVel()))){//If it's not a goal
						mySoundSystem.quickPlay( true, slowMo?"BallBounceSlow.ogg":"BallBounce.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
						tempf = orbitAngle+(float)Math.PI/2f*orbitDir;
						ball.setVel(new float[]{-(float)Math.cos(tempf), -(float)Math.sin(tempf)}, orbitOmega*orbitRadius);	
					}
					ball.setLocked(playerNum, false);
					orbiting = false;
				}
//				System.out.println(ball.getX()+" "+ball.getY());
			}
		}
		
		//For if the ball gets knocked out of your hands
		if(dist(pos[0],pos[1],ball.getX(),ball.getY()) > GRAVRANGE/2f){
			if(ball.locked(playerNum))
				ball.setCurve(zeroes, 0);
			ball.setLocked(playerNum, false);
			orbiting = false;
		}
		
		omega = approachTarget(omega, gravDir*.4f, delta/1000f);
		
		theta+= omega*delta;
		if(theta>360) theta-=360;
	}

	@Override
	public void activatePower() {
		gravDir = 1;
		gravRange = 0;
		targetVelMag = VELMAG/3f;
		pushCoolDown = false;
		mySoundSystem.quickPlay( true, slowMo?"NeutronPushSlow.ogg":"NeutronPush.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
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
		mySoundSystem.quickPlay( true, slowMo?"NeutronPullSlow.ogg":"NeutronPull.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
	}
	
	public void powerKey2Released(){
		gravDir = 0;
		gravRange = 0;
		targetVelMag = VELMAG;
		if(ball.locked(playerNum)){
			if(orbiting && (ball.getVelX()!=0 || ball.getVelY()!=0)){
				tempf = orbitAngle+(float)Math.PI/2f*orbitDir;
				ball.setVel(new float[]{(float)Math.cos(tempf), (float)Math.sin(tempf)}, Math.abs(orbitOmega)*orbitRadius);
				ball.speedUp((1f-orbitRadius/GRAVRANGE)*2f*VELMAG+NORMALKICK, 0.05f, 0);
			}
			ball.setCurve(zeroes, 0);
			ball.setLocked(playerNum, false);
			orbiting = false;
		}
	}
	
	@Override
	public boolean isKicking() {
		return !buttonPressed && !orbiting && kickingCoolDown == 0;
	}

	@Override
	public boolean flashKick() {
		return false;
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
