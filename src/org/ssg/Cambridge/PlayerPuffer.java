/**
TODO:
Expanding into walls gets you stuck, where as it should push you away from the wall – DONE
expanding into players is bad too – DONE
expanding into the ball should kick it away from you? – DONE
expanding and deflating sounds
*/
package org.ssg.Cambridge;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.newdawn.slick.Color;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerPuffer extends Player{
	
	Component actionButton2;
	float MAXSIZE = 300;
	float MINSIZE = 20;
	float DEFAULTNORMALKICK;
	float DEFAULTPOWERKICK;
	boolean puffup;
	boolean puffdown;
	
	public PlayerPuffer(int n, float[]  consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn);
		
		DEFAULTNORMALKICK = NORMALKICK;
		DEFAULTPOWERKICK = POWERKICK;		
		
		KICKRANGE = PLAYERSIZE*1.42f;
		velMag = VELMAG * (100f/PLAYERSIZE > 2f ? 2f : 100f/PLAYERSIZE);
		NORMALKICK = DEFAULTNORMALKICK * (150f/PLAYERSIZE > 2f ? 2f : 150f/PLAYERSIZE);		
		
		puffup = false;
		puffdown = false;
		
		if (cExist) {
			actionButton2 = this.c.getComponent(Component.Identifier.Button._4); 
		}
	}
	
	@Override
	public void update(int delta){
		if (cExist) {
			pollController(delta);
			
			if (actionButton.getPollData() == 1.0){
				activatePower();//inflate
			}else{
				powerKeyReleased();
			}
			
			if (actionButton2.getPollData() == 1.0){
				activateAntiPower();//deflate
			}else{
				antiPowerKeyReleased();
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

		theta+= (1f-(PLAYERSIZE/(MAXSIZE+100)))*omega*(float)delta;
		if(theta>360) theta-=360;
		
		if(puffup && !puffdown){
			if(PLAYERSIZE+(float)(delta) > MAXSIZE){
				PLAYERSIZE = MAXSIZE;
				powerKeyReleased();
			}else{
				PLAYERSIZE+=(float)(delta);
			}
			KICKRANGE = PLAYERSIZE*1.42f;
			
			if(pos[0]-KICKRANGE/2 < xyLimit[0]){
				pos[0]+= xyLimit[0] - pos[0] + KICKRANGE/2;
			}else if(pos[0]+KICKRANGE/2 > xyLimit[1]){
				pos[0]+= xyLimit[1] - pos[0] - KICKRANGE/2;
			}
			
			if(pos[1]-KICKRANGE/2 < xyLimit[2]){
				pos[1]+= xyLimit[2] - pos[1] + KICKRANGE/2;
			}else if(pos[1]+KICKRANGE/2 > xyLimit[3]){
				pos[1]+= xyLimit[3] - pos[1] - KICKRANGE/2;
			}
			
		}else if(!puffup && puffdown){
			if(PLAYERSIZE-(float)(delta/2)>=MINSIZE)
				PLAYERSIZE-=(float)(delta/2);
			KICKRANGE = PLAYERSIZE*1.42f;
		}
		
		velMag = VELMAG * (100f/PLAYERSIZE > 2f ? 2f : 100f/PLAYERSIZE);
		NORMALKICK = DEFAULTNORMALKICK * (150f/PLAYERSIZE > 2f ? 2f : 150f/PLAYERSIZE);
		POWERKICK = DEFAULTPOWERKICK * (1.8f - PLAYERSIZE/MAXSIZE);		
	}
	
	@Override
	public void activatePower(){
		if(PLAYERSIZE < MAXSIZE && !puffup){
			puffup = true;
			power = 1;
		}
	}
	
	@Override
	public void powerKeyReleased(){
		puffup = false;
		power = 0;
		kickingCoolDown = 0;
	}
	
	public void activateAntiPower(){
		if(PLAYERSIZE > MINSIZE && !puffdown)
			puffdown = true;
	}
	
	public void antiPowerKeyReleased(){
		puffdown = false;
	}
	
	//Once you get that powerkick you can't kick no more until you stop inflating
	@Override
	public void setPower(){
		kickingCoolDown = 1000;//This is basically a flag value
		power = 0;
	}
	
	//Only care about kickingCoolDown if we're puffing up, otherwise instant kicks like normal
	@Override
	public boolean isKicking(){
		if(puffup)
			return kickingCoolDown <= 0;
		return true;//which for now is just true;
	}
	
	@Override
	public boolean flashKick(){
		return puffup;
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
			}else if(input == controls[4]){
				activatePower();//Inflate
			}else if(input == controls[5]){
				activateAntiPower();//Deflate
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
			}else if(input == controls[4]){
				powerKeyReleased();
			}else if(input == controls[5]){
				antiPowerKeyReleased();
			}
		}
	}
	
}
