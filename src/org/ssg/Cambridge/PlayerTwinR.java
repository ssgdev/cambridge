package org.ssg.Cambridge;

import net.java.games.input.Controller;

import org.newdawn.slick.Color;

import paulscode.sound.SoundSystem;

public class PlayerTwinR extends Player{

	PlayerTwinL twinL;
	boolean active;
	
	public PlayerTwinR(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn);

		active = false;
	}

	public void setTwin(PlayerTwinL p){
		twinL = p;
	}
	
	@Override
	public void update(int delta) {
		if(cExist && active){
			vel[0] = rStickX.getPollData();
			vel[1] = rStickY.getPollData();
			if (Math.abs(vel[0]) < 0.2)
				vel[0] = 0f;
			if (Math.abs(vel[1]) < 0.2)
				vel[1] = 0f;
		}
		
		updatePos(delta);
	}

	@Override
	public void activatePower() {

	}

	@Override
	public void powerKeyReleased() {

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
	
	//Activate the twin
	public void activate(){
		active = true;
	}
	
	public void setActive(boolean b){
		active = b;
	}
	
	public boolean active(){
		return active;
	}
	
	@Override
	public float[] getCurve() {
		return vel;
	}
	
	@Override
	public void keyPressed(int input, char arg1) {

	}

	@Override
	public void keyReleased(int input, char arg1) {

	}
	
}
