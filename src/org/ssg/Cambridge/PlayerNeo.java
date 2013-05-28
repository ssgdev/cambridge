package org.ssg.Cambridge;

import net.java.games.input.Controller;

import org.newdawn.slick.Color;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerNeo extends Player {

	public PlayerNeo(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Player op) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, op);
	}

	@Override
	public void update(int delta){

		if (cExist) {
			vel[0] = lStickX.getPollData();
			vel[1] = lStickY.getPollData();
			if (Math.abs(vel[0]) < 0.2)
				vel[0] = 0f;
			if (Math.abs(vel[1]) < 0.2)
				vel[1] = 0f;
			
			//TODO: Curve
			curve[0] = rStickX.getPollData();
			curve[1] = rStickY.getPollData();
			if(Math.abs(curve[0]) < 0.2)
				curve[0] = 0;
			if(Math.abs(curve[1]) < 0.2)
				curve[1] = 0;
			
			if (actionButton.getPollData() == 1.0)
				if(powerCoolDown <= 0){
					activatePower();
				}else{
					//play whiff animation
				}
		}

		lastKickAlpha -= (float)(delta)/2400f;
		if(lastKickAlpha<0){
			lastKickAlpha = 0f;
		}
		temp = (int)(pos[0]+vel[0]*velMag*(float)delta);
		if(temp>xyLimit[0] && temp<xyLimit[1] && dist(temp,pos[1],otherPlayer.getX(),otherPlayer.getY())>=28)
			pos[0]=temp;

		temp = (int)(pos[1]+vel[1]*velMag*(float)delta);
		if(temp>xyLimit[2] && temp<xyLimit[3] && dist(pos[0],temp,otherPlayer.getX(),otherPlayer.getY())>=28)
			pos[1]=temp;

		kickingCoolDown -= (float)delta;
		if(kickingCoolDown<0)
			kickingCoolDown = 0;

		theta+= (1f-(powerCoolDown/POWERCOOLDOWN))*(float)delta;

		if(power>0){
			power-=(float)delta;
			if(power<=0){
				power = 0;
				velMag = VELMAG;
				mySoundSystem.quickPlay( true, "whoosh2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				if(mySoundSystem.playing(slowName));
				mySoundSystem.pause(slowName);
			}
		}

		if(powerCoolDown>-500f){
			powerCoolDown -=(float)delta;
			if(powerCoolDown<=0 && !playedPowerDing){
				if(otherPlayer.isPower()){
					mySoundSystem.quickPlay( true, "pingslow.wav", false, 0,0,0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}else{
					mySoundSystem.quickPlay( true, "ping.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}
				playedPowerDing = true;
			}
			if(powerCoolDown <=-500f){
				powerCoolDown = -500f;
			}
		}
	}

	@Override
	public void activatePower(){
		power = MAXPOWER;
		powerCoolDown = MAXPOWER+POWERCOOLDOWN;
		velMag = POWERVELMAG;
		playedPowerDing = false;
		mySoundSystem.quickPlay( true, "whoosh2r.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		mySoundSystem.play(slowName);
	}
	
	@Override
	public boolean isSlowMoPower(){
		return isPower() && true;
	}
	
}
