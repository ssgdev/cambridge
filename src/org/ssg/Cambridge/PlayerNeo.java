package org.ssg.Cambridge;

import net.java.games.input.Controller;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerNeo extends Player {

	public PlayerNeo(int n, float[] consts, int[] f, int[] c, CambridgeController c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, slc);
		
		POWERKICK = 1.1f;
		
	}

	@Override
	public void update(float delta){

		if (c.exists()) {
			pollController(delta);
			
			if (c.getAction()){
				activatePower();
			}
			
		}else{
			
			pollKeys(delta);
			
			if(buttonPressed){
				activatePower();
				buttonPressed = false;
			}
		}

		updatePos(delta);

		updateCounters(delta);

		theta+= (1f-(powerCoolDown/POWERCOOLDOWN))*omega*delta;
		if(theta>360) theta-=360;
		
		if(power>0){
			power-=delta;
			if(power<=0){
				power = 0;
				velMag = VELMAG;
				mySoundSystem.quickPlay( true, "NeoSlowOut.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				if(mySoundSystem.playing(slowName))
					mySoundSystem.pause(slowName);
				for(Player player: players){
					player.setSlowMo(false);
				}
			}
		}

		if(powerCoolDown>-500f){
			powerCoolDown -= delta;
			if(powerCoolDown<=0 && !playedPowerDing){
				mySoundSystem.quickPlay( true, slowMo?"NeoRechargedSlow.ogg":"NeoRecharged.ogg", false, 0,0,0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				playedPowerDing = true;
			}
			if(powerCoolDown <=-500f){
				powerCoolDown = -500f;
			}
		}
	}

	@Override
	public void activatePower(){
		if(powerCoolDown<=0){
			power = MAXPOWER;
			powerCoolDown = MAXPOWER+POWERCOOLDOWN;
			velMag = POWERVELMAG;
			playedPowerDing = false;
			mySoundSystem.quickPlay( true, "NeoSlowIn.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			mySoundSystem.play(slowName);
			for(Player p: players){
				p.setSlowMo(true);
			}
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
		return kickingCoolDown == 0;
	}
	
	//Is this kick a flash kick
	@Override
	public boolean flashKick(){
		return isPower();
	}
	
}
