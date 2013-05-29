/**
TODO:
Expanding into walls gets you stuck, where as it should push you away from the wall – DONE
expanding into players is bad too
expanding into the ball should kick it away from you?
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
	float MAXSIZE = 400;
	float MINSIZE = 20;
	float DEFAULTNORMALKICK;
	float DEFAULTPOWERKICK;
	boolean puffup;
	boolean puffdown;
	
	public PlayerPuffer(int n, float[]  consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Player op) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, op);
		
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

		lastKickAlpha -= (float)(delta)/2400f;
		if(lastKickAlpha<0){
			lastKickAlpha = 0f;
		}
		
		temp = (int)(pos[0]+vel[0]*velMag*(float)delta);
		if(temp-KICKRANGE/2>xyLimit[0] && temp+KICKRANGE/2<xyLimit[1] && dist(temp,pos[1],otherPlayer.getX(),otherPlayer.getY())>=(KICKRANGE+otherPlayer.getKickRange())/2)
			pos[0]=temp;

		temp = (int)(pos[1]+vel[1]*velMag*(float)delta);
		if(temp-KICKRANGE/2>xyLimit[2] && temp+KICKRANGE/2<xyLimit[3] && dist(pos[0],temp,otherPlayer.getX(),otherPlayer.getY())>=(KICKRANGE+otherPlayer.getKickRange())/2)
			pos[1]=temp;

		kickingCoolDown -= (float)delta;
		if(kickingCoolDown<0)
			kickingCoolDown = 0;

		theta+= (1f-(powerCoolDown/POWERCOOLDOWN))*(float)delta;
		
		if(puffup && !puffdown){
			if(PLAYERSIZE+(float)(delta) <= MAXSIZE)
				PLAYERSIZE+=(float)(delta);
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
	}
	
	@Override
	public void activatePower(){
		puffup = true;
	}
	
	@Override
	public void powerKeyReleased(){
		puffup = false;
	}
	
	public void activateAntiPower(){
		puffdown = true;
	}
	
	public void antiPowerKeyReleased(){
		puffdown = false;
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
