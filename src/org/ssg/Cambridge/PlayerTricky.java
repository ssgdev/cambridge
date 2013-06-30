package org.ssg.Cambridge;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

import paulscode.sound.SoundSystem;

public class PlayerTricky extends Player{

	Ball ball;

	//For the decoy
	float[] fakePos;
	//curve[] acts as fakeVel[]
	float fakeAlpha;
	
	boolean buttonPressed;
	Component actionButton2;
	boolean button2Pressed;
	
	public PlayerTricky(int n, float[] consts, int[] f, int[] c, Controller c1,	boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc, Ball b) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, slc);

		ball = b;
		
		fakePos = new float[2];
		fakeAlpha = 0f;
		
		buttonPressed = false;
		button2Pressed = false;
		if(cExist){
			actionButton2 = this.c.getComponent(Component.Identifier.Button._4);
		}
	}

	@Override
	public void update(int delta) {
		if (cExist) {
			pollController(delta);
			
			if(actionButton.getPollData() == 1.0 && !buttonPressed){
				buttonPressed = true;
				activatePower();
			}else if(actionButton.getPollData() == 0.0 && buttonPressed){
				buttonPressed = false;
				powerKeyReleased();
			}
			
			if(actionButton2.getPollData() == 1.0 && !button2Pressed){
				button2Pressed = true;
			}else if(actionButton2.getPollData() == 0 && button2Pressed){
				button2Pressed = false;
			}
		}

		updatePos(delta);

		updateCounters(delta);

		theta+= omega*(float)delta;
		if(theta>360) theta-=360;
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

}
