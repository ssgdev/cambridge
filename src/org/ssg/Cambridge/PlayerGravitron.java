/**
TODO:
gravity well
slowmo version of activation sound
 */

package org.ssg.Cambridge;

import net.java.games.input.Controller;
import org.newdawn.slick.Color;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class PlayerGravitron extends Player{
	//Power kicks are super weak
	//Normal kicks slightly stronger
	//First kick after coming out of power is a little stronger
	Ball ball;
	
	float DEFAULTKICK;
	float DEFAULTKICKCOOLDOWN;
	float EXTRAKICK;
	int EXTRAKICKTIME = 100;
	int extraKickTimer;
	
	boolean buttonPressed;//The power button, that is
	
	public PlayerGravitron(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Ball b) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn);
		
		MAXPOWER = 15;
		DEFAULTKICK = NORMALKICK*1.2f;
		DEFAULTKICKCOOLDOWN = KICKCOOLDOWN*2;
		EXTRAKICK = DEFAULTKICK*2f;
		KICKRANGE *= 1.5f;
		
		NORMALKICK = DEFAULTKICK;
		VELMAG *= 1.2f;
		velMag = VELMAG;
		
		//POWERKICK = VELMAG/8f;
		POWERKICK /= 8f;
		POWERCOOLDOWN = 100;
		extraKickTimer = EXTRAKICKTIME;
		
		buttonPressed = false;
		
		ball = b;
	}
	
	@Override
	public void update(int delta){
		
		if (cExist) {
			
			pollController(delta);
			
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
		
		lastKickAlpha -= (float)(delta)/2400f;
		if(lastKickAlpha<0){
			lastKickAlpha = 0f;
		}

		kickingCoolDown -= (float)delta;
		if(kickingCoolDown<0)
			kickingCoolDown = 0;

		theta+= (1f-(power*.8f))*omega*(float)delta;
		if(theta>360) theta-=360;
		
		if(extraKickTimer > 0)
			extraKickTimer -= delta;
		if(extraKickTimer < 0){
			extraKickTimer = 0;
			NORMALKICK = DEFAULTKICK;
		}
		
		if(power>0){
			if(dist(ball.getX(), ball.getY(), pos[0], pos[1]) > KICKRANGE/2)
				ball.setTwoTouchAcc(new float[]{pos[0]-ball.getX(),  pos[1]-ball.getY()}, .5f / (dist(pos[0],pos[1],ball.getX(),ball.getY() + 1f)));
			if(dist(ball.getX(), ball.getY(), pos[0], pos[1]) <= KICKRANGE/2)
				ball.setTwoTouchAcc(new float[]{-ball.getVelX(), -ball.getVelY()},.05f);
		}
		
	}

	@Override
	public void activatePower(){
		power = 15f;
		KICKCOOLDOWN = 5000;
		mySoundSystem.quickPlay( true, "TwoTouchActivate.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		velMag = VELMAG/2f;
	}
	
	@Override
	public void powerKeyReleased(){
		power = 0;
		kickingCoolDown = 0;
		NORMALKICK = EXTRAKICK;
		extraKickTimer = EXTRAKICKTIME;
		KICKCOOLDOWN = DEFAULTKICKCOOLDOWN;
		velMag = VELMAG;
		ball.setTwoTouchAcc(new float[]{0, 0}, 0);
	}
	
	@Override
	public void setPower(){
		if(NORMALKICK != DEFAULTKICK){
			NORMALKICK = DEFAULTKICK;
			lastKickAlpha = 1.0f;
		}
	}

	//I just kicked (power or regular kick) the ball now what
	@Override
	public void setKicking(Ball b){
		if(power == 0){
			b.setCanBeKicked(playerNum, false);
		}else{
			//b.setAcc(new float[]{-b.getVelX(), -b.getVelY()}, 40f*(float)Math.pow((mag(b.getX()-pos[0],b.getY()-pos[1])/KICKRANGE/2+.6f),2));
		}
		kickingCoolDown = KICKCOOLDOWN;
	}

	//Most other players get to kick instantly, but this makes TwoTouch have a dribble effect
	@Override
	public boolean isKicking(){
		if(power==0)
			return true;
		if(kickingCoolDown<=0)
			return true;
		return false;
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
		//TwoTouch doesn't have the last kick alpha
	}
	
	@Override
	public Color getColor3(){//return color of powercircle
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), ((150/MAXPOWER)));
	}
	
	public float mag(float a, float b){
		return (float)Math.sqrt(a*a+b*b);
	}
}