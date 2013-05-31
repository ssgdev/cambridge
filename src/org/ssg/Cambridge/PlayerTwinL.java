//Is the main twin

package org.ssg.Cambridge;

import net.java.games.input.Controller;

import org.newdawn.slick.Color;

import paulscode.sound.SoundSystem;

public class PlayerTwinL extends Player{

	PlayerTwinR twinR;
	boolean buttonPressed;
	
	public PlayerTwinL(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn);

		buttonPressed = false;
	}

	public void setTwin(PlayerTwinR p){
		twinR = p;
	}
	
	@Override
	public void update(int delta) {
		if(cExist){
			vel[0] = lStickX.getPollData();
			vel[1] = lStickY.getPollData();
			if (Math.abs(vel[0]) < 0.2)
				vel[0] = 0f;
			if (Math.abs(vel[1]) < 0.2)
				vel[1] = 0f;
			
			if(!twinR.active()){
				curve[0] = rStickX.getPollData();
				curve[1] = rStickY.getPollData();
				if(Math.abs(curve[0]) < 0.2)
					curve[0] = 0;
				if(Math.abs(curve[1]) < 0.2)
					curve[1] = 0;
			}
			
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
	}

	@Override
	public void activatePower() {
		twinR.setActive(!twinR.active());
	}

	@Override
	public void powerKeyReleased() {
		//Does nothing
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
	
	@Override
	public float[] getCurve() {
		if(twinR.active)
			return vel;
		return curve;
	}
	
	@Override
	public void keyPressed(int input, char arg1) {
		if (!cExist) {
			if(input == controls[0]){
				up = true;
				vel[1] = -1;
			}else if(input == controls[1]){
				down = true;
				vel[1] = 1;
			}else if(input == controls[2]){
				left = true;
				vel[0] = -1;
			}else if(input == controls[3]){
				right = true;
				vel[0] = 1;
			}
		}
	}

	@Override
	public void keyReleased(int input, char arg1) {
		if (!cExist) {
			if(input == controls[0]){
				up = false;
				if(down){
					vel[1] = 1;
				}else{
					vel[1] = 0;
				}
			}else if(input == controls[1]){
				down = false;
				if(up){
					vel[1] = -1;
				}else{
					vel[1] = 0;
				}
			}else if(input == controls[2]){
				left = false;
				if(right){
					vel[0] = 1;
				}else{
					vel[0] = 0;
				}
			}else if(input == controls[3]){
				right = false;
				if(left){
					vel[0] = -1;
				}else{
					vel[0] = 0;
				}
			}
		}
	}
	
}
