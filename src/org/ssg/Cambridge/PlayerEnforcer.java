package org.ssg.Cambridge;

import net.java.games.input.Controller;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerEnforcer extends Player{

	boolean buttonPressed;
	float MINVELMAG;//The velocity you're set to when you start charging
	float MAXVELMAG;
	float targetVelmag;
	float[] launchVel;
	boolean coolingDown;
	float[] curve;
	float powerAlpha;//Used for drawing powercircle
	
	float stepCoolDown;//used for playing the walking sound
	float STEPCOOLDOWN;
	
	public PlayerEnforcer(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn);
		
		MINVELMAG = .2f*VELMAG;
		MAXVELMAG = VELMAG * 2.5f;
		targetVelmag = VELMAG;
		launchVel = new float[2];
		coolingDown = false;
		curve = new float[2];
		powerAlpha = 0;
		stepCoolDown = 0;
		STEPCOOLDOWN = MAXPOWER;
	}
	
	@Override
	public void drawPlayer(Graphics g){
		g.setColor(getColor());
		g.rotate(pos[0], pos[1], theta);
		g.drawRect(pos[0]-PLAYERSIZE/2, pos[1]-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
		if(power>0 || coolingDown){//Speed glow
			g.setColor(getColor4());
			g.fillRect(pos[0]-PLAYERSIZE/2, pos[1]-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
		}
		g.rotate(pos[0], pos[1], -getTheta());
		//g.drawOval(getX()-getKickRange()/2f+BALLSIZE/2f, getY()-getKickRange()/2f+BALLSIZE/2f, getKickRange()-BALLSIZE, getKickRange()-BALLSIZE);//Draw kicking circle;
	}

	@Override
	public void drawPowerCircle(Graphics g){
		//Draw power circle
		if(isPower()){
			g.setColor(getColor3());
			g.drawOval(pos[0]-KICKRANGE/2f-power/2f, pos[1]-KICKRANGE/2f-power/2f, KICKRANGE, KICKRANGE);
			g.setColor(Color.white);
		}
	}
	
	@Override
	public Color getColor3(){//return color of powercircle
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), powerAlpha);
	}

	
	@Override
	public Color getColor4(){
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), (velMag-MINVELMAG)/MAXVELMAG);
	}
	
	@Override
	public void update(int delta) {
		if (cExist) {
			
			pollController(delta);				
			
			if (actionButton.getPollData() == 1.0){
					activatePower();
					if(!buttonPressed){
						mySoundSystem.quickPlay( true, "EnforcerActivate.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
						powerAlpha = 1f;
					}
					buttonPressed = true;
			}else if(actionButton.getPollData() == 0 && buttonPressed){
					powerKeyReleased();
					buttonPressed = false;
			}
		
		}
		
		if(power>0){
			if(mag(launchVel)==0 && mag(vel)!=0){
				launchVel[0] = vel[0];
				launchVel[1] = vel[1];
				unit(launchVel);
			}else if(mag(launchVel)>0){
				curve = normal(vel, launchVel);
				unit(curve);
				velMag = approachTarget(velMag,targetVelmag, (float)delta/1600f);
				vel[0] = launchVel[0]+curve[0]*.1f;
				vel[1] = launchVel[1]+curve[1]*.1f;
				unit(vel);
				launchVel[0] = vel[0];
				launchVel[1] = vel[1];
			}
		}else if(coolingDown){
			velMag = approachTarget(velMag,targetVelmag, (float)delta/1200f);
			vel[0] = launchVel[0];
			vel[1] = launchVel[1];
			if(velMag == targetVelmag){
				coolingDown = false;
				targetVelmag = VELMAG;
			}
		}else{
			velMag = approachTarget(velMag,targetVelmag, (float)delta/1200f);	
		}
			
		updatePos(delta);
		
		if(power>0 || coolingDown){//TODO: Make synced with actual speed
			stepCoolDown -= (float)delta;
			if(stepCoolDown<=0){
				stepCoolDown = STEPCOOLDOWN;
				if(power > 0){
					STEPCOOLDOWN -= 4f*(float)delta;//Might not make sense to have a delta here
				}else if(coolingDown){
					STEPCOOLDOWN += 8f*(float)delta;
				}
				if(STEPCOOLDOWN < 180)
					STEPCOOLDOWN = 180;
				if(STEPCOOLDOWN > MAXPOWER)
					STEPCOOLDOWN = MAXPOWER;
				mySoundSystem.quickPlay( true, "EnforcerStep.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			}
		}
		
		lastKickAlpha -= (float)(delta)/1200f;
		if(lastKickAlpha<0)
			lastKickAlpha = 0f;
		
		powerAlpha -= (float)delta/600f;
		if(powerAlpha<0f)
			powerAlpha = 0f;
		
		kickingCoolDown -= (float)delta;
		if(kickingCoolDown<0)
			kickingCoolDown = 0;
		
		theta += omega * (float)delta * 2f * velMag/MAXVELMAG;
		if(theta>360f)
			theta-=360f;
	}
	
	//Can kick other players
	@Override
	public void updatePos(int delta){
		pos[0] = (pos[0]+vel[0]*velMag*(float)delta);
		tempf = 0;//Used here as flag for bouncing
		if(pos[0]<xyLimit[0]+KICKRANGE/2){
			pos[0]=xyLimit[0]+KICKRANGE/2;
			tempf = 1f;
		}
		if(pos[0]>xyLimit[1]-KICKRANGE/2){
			pos[0]=xyLimit[1]-KICKRANGE/2;
			tempf = 1f;
		}
		pos[1]= (pos[1]+vel[1]*velMag*(float)delta);
		if(pos[1]<xyLimit[2]+KICKRANGE/2){
			pos[1]=xyLimit[2]+KICKRANGE/2;
			tempf = 1f;
		}
		if(pos[1]>xyLimit[3]-KICKRANGE/2){
			pos[1]=xyLimit[3]-KICKRANGE/2;
			tempf = 1f;
		}
		
		if(tempf==1f){
			if(power>0){
				launchVel[0]*=-1f;
				launchVel[1]*=-1f;
				velMag/=2f;
				power = 0;
				targetVelmag = 0;
				coolingDown = true;
				if(velMag>.1f)
					mySoundSystem.quickPlay( true, "EnforcerWallBounce.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				STEPCOOLDOWN = MAXPOWER;
			}
		}
		
		//player on player collision handling
		for(Player otherPlayer: players){
			if(otherPlayer != this){//If he's not you, collide with him
				tempf = dist(pos[0], pos[1], otherPlayer.getX(), otherPlayer.getY());
				if(tempf < (KICKRANGE + otherPlayer.getKickRange())/2){
					if(power > 0 && !otherPlayer.stunned()){
						otherPlayer.setStunned(MAXSTUN, new float[]{otherPlayer.getX()-pos[0]+vel[0]*vel[0],otherPlayer.getY()-pos[1]+vel[1]*vel[1]}, NORMALKICK+VELMAG);
						kickingCoolDown = KICKCOOLDOWN;
						mySoundSystem.quickPlay( true, "EnforcerBump.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
					}else{
						tempArr[0] = otherPlayer.getX()-pos[0];
						tempArr[1] = otherPlayer.getY()-pos[1];
						unit(tempArr);
						tempArr[0]*= (KICKRANGE + otherPlayer.getKickRange())/2 - tempf;
						tempArr[1]*= (KICKRANGE + otherPlayer.getKickRange())/2 - tempf;
						
						//////X AXIS
						//Get pushed, weighted based on size
						//tempf = KICKRANGE / ( KICKRANGE+ otherPlayer.getKickRange());
						//shiftX(-tempArr[0]*(1-tempf));
						otherPlayer.shiftX(tempArr[0]);
						
						//Then if anyone got pushed into a wall, pop out
						if(pos[0]-KICKRANGE/2 <= xyLimit[0]){
							tempf = xyLimit[0] - pos[0] + KICKRANGE/2;
						}else if(otherPlayer.getX()-otherPlayer.getKickRange()/2 <= xyLimit[0]){
							tempf = xyLimit[0] - otherPlayer.getX()+otherPlayer.getKickRange()/2;
						}else if(pos[0]+KICKRANGE/2 >= xyLimit[1]){
							tempf = xyLimit[1] - pos[0] - KICKRANGE/2;
						}else if(otherPlayer.getX() + otherPlayer.getKickRange()/2 >= xyLimit[1]){
							tempf = xyLimit[1] - otherPlayer.getX() - otherPlayer.getKickRange()/2;
						}else{
							tempf = 0;
						}
						shiftX(tempf);
						otherPlayer.shiftX(tempf);
						
						//////Y AXIS
						//tempf = KICKRANGE / ( KICKRANGE+ otherPlayer.getKickRange());
						//shiftY(-tempArr[1]*(1-tempf));
						otherPlayer.shiftY(tempArr[1]);
						
						//Then if anyone got pushed into a wall, pop out
						if(pos[1]-KICKRANGE/2 <= xyLimit[2]){
							tempf = xyLimit[2] - pos[1] + KICKRANGE/2;
						}else if(otherPlayer.getY()-otherPlayer.getKickRange()/2 <= xyLimit[2]){
							tempf = xyLimit[2] - otherPlayer.getY()+otherPlayer.getKickRange()/2;
						}else if(pos[1]+KICKRANGE/2 >= xyLimit[3]){
							tempf = xyLimit[3] - pos[1] - KICKRANGE/2;
						}else if(otherPlayer.getY() + otherPlayer.getKickRange()/2 >= xyLimit[3]){
							tempf = xyLimit[3] - otherPlayer.getY() - otherPlayer.getKickRange()/2;
						}else{
							tempf = 0;
						}
						shiftY(tempf);
						otherPlayer.shiftY(tempf);
					}
				}
			}
		}
	}
	
	
	@Override
	public void activatePower() {
		if(power==0 && !coolingDown){
			power = 1;
			velMag = MINVELMAG;
			targetVelmag = MAXVELMAG;
			launchVel[0] = 0;
			launchVel[1] = 0;
			//stepCoolDown = MAXPOWER;
			STEPCOOLDOWN = MAXPOWER;
		}
	}

	@Override
	public void powerKeyReleased() {
		if(power>0){
			power = 0;
			targetVelmag = 0;
			coolingDown = true;
		}
	}

	@Override
	public boolean isKicking() {
		return true;
	}

	@Override
	public boolean flashKick() {
		return power>0;
	}

	@Override
	public void setPower() {
		
	}

	public float[] normal(float[] v, float[] w){//orthogonal proj v on w
		tempf = dot(v,w)/mag(w);//Repurposing this as a temp calculation holder
		return new float[]{v[0]-tempf*w[0], v[1]-tempf*w[1]};
	}
	
}