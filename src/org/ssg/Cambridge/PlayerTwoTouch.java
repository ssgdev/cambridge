package org.ssg.Cambridge;

import net.java.games.input.Controller;
import org.newdawn.slick.Color;
import paulscode.sound.SoundSystem;

public class PlayerTwoTouch extends Player{
	//Power kicks are super weak
	//Normal kicks slightly stronger
	//First kick after coming out of power is a little stronger
	//TODO: Should forfeit the stronger kick if ball leaves radius
	//TODO: Fix sounds playing
	
	float DEFAULTKICK;
	float EXTRAKICK;
	int EXTRAKICKTIME = 50;
	int extraKickTimer;
	
	boolean buttonPressed;
	
	public PlayerTwoTouch(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Player op) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, op);
		
		DEFAULTKICK = NORMALKICK*1.2f;
		EXTRAKICK = DEFAULTKICK*2f;
		KICKRANGE *= 1.5f;
		
		NORMALKICK = DEFAULTKICK;

		POWERKICK = VELMAG/4f;
		POWERCOOLDOWN = 100;
		extraKickTimer = EXTRAKICKTIME;
		
		buttonPressed = false;
	}
	
	@Override
	public void update(int delta){
		
		//Controller untested on this character
		if (cExist) {
			vel[0] = lStickX.getPollData();
			vel[1] = lStickY.getPollData();
			if (Math.abs(vel[0]) < 0.2)
				vel[0] = 0f;
			if (Math.abs(vel[1]) < 0.2)
				vel[1] = 0f;
			
			curve[0] = rStickX.getPollData();
			curve[1] = rStickY.getPollData();
			if(Math.abs(curve[0]) < 0.2)
				curve[0] = 0;
			if(Math.abs(curve[1]) < 0.2)
				curve[1] = 0;

			if (actionButton.getPollData() == 1.0)
				if(powerCoolDown <= 0){
					activatePower();
					buttonPressed = true;
				}else{
					if(buttonPressed){
						powerKeyReleased();
						buttonPressed = false;
					}
				}
		}
		
		lastKickAlpha -= (float)(delta)/2400f;
		if(lastKickAlpha<0){
			lastKickAlpha = 0f;
		}
		
		if(extraKickTimer > 0)
			extraKickTimer -= delta;
		if(extraKickTimer < 0){
			extraKickTimer = 0;
			NORMALKICK = DEFAULTKICK;
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

		theta+= (1f-(power*.8f))*(float)delta;
		
	}

	@Override
	public void activatePower(){
		power = 1f;
	}
	
	@Override
	public void powerKeyReleased(){
		power = 0;
		kickingCoolDown = 0;
		NORMALKICK = EXTRAKICK;
		extraKickTimer = EXTRAKICKTIME;
	}
	
	@Override
	public void setPower(){
		if(NORMALKICK != DEFAULTKICK){
			NORMALKICK = DEFAULTKICK;
			lastKickAlpha = 1.0f;
		}
	}
	
	//Return if the kick should flash and make the power kick sound
	@Override
	public boolean flashKick(){
		return NORMALKICK == EXTRAKICK;
	}
	
	@Override
	public void setLastKick(float bx, float by, float px, float py, float lka){//ball pos, player pos, was it a power kick
		lastKickBallPos[0] = bx;
		lastKickBallPos[1] = by;
		lastKickPos[0] = px;
		lastKickPos[1] = py;
		
	}
	
	@Override
	public Color getColor3(){//return color of powercircle
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), ((150/MAXPOWER)));
	}

}
