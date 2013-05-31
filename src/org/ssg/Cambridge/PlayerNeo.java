package org.ssg.Cambridge;

import net.java.games.input.Controller;

import org.newdawn.slick.Color;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerNeo extends Player {

	public PlayerNeo(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn);
	}

	@Override
	public void update(int delta){

		if (cExist) {
			pollController(delta);
			
			if (actionButton.getPollData() == 1.0){
				if(powerCoolDown <= 0){
					activatePower();
				}
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

		theta+= (1f-(powerCoolDown/POWERCOOLDOWN))*omega*(float)delta;

		if(power>0){
			power-=(float)delta;
			if(power<=0){
				power = 0;
				velMag = VELMAG;
				mySoundSystem.quickPlay( true, "whoosh2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				if(mySoundSystem.playing(slowName))
					mySoundSystem.pause(slowName);
				for(Player player: players){
					player.setSlowMo(false);
				}
			}
		}

		if(powerCoolDown>-500f){
			powerCoolDown -=(float)delta;
			if(powerCoolDown<=0 && !playedPowerDing){
				if(slowMo){
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
		for(Player p: players){
			p.setSlowMo(true);
		}
	}
	
	@Override
	public void powerKeyReleased() {
		//Does nothing
	}
	
	@Override
	public void setPower(){
		power = 0;//
		velMag = VELMAG;
		if(mySoundSystem.playing(slowName))
			mySoundSystem.pause(slowName);
		for(Player player: players){
			player.setSlowMo(false);
		}
	}

	@Override
	public boolean isSlowMoPower(){
		return isPower() && true;
	}
	
	@Override
	public boolean isKicking(){
		return true;
	}
	
	//Is this kick a flash kick
	@Override
	public boolean flashKick(){
		return isPower();
	}
	
}
