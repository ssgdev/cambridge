package org.ssg.Cambridge;

import net.java.games.input.Controller;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import paulscode.sound.SoundSystem;

public class PlayerGun extends Player{

	boolean buttonPressed;
	
	float[] aimPos;
	float CURSORSIZE = 40;
	float aimVelMag = .5f;
	PlayerDummy cursor;
	
	Ball ball;
	float[] ballParallel, ballOrth;
	
	public PlayerGun(int n, float[] consts, int[] f, int[] c, Controller c1,
			boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss,
			String sn, Image slc, Ball b) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, slc);

		KICKRANGE = PLAYERSIZE*2f;
		NORMALKICK = 0.1f;
		
		buttonPressed = false;
		
		aimPos = new float[]{pos[0], pos[1]+KICKRANGE/2f+CURSORSIZE/2f};
		
		ball = b;
		ballParallel = new float[2];
		ballOrth = new float[2];
	}
	
	public void setDummy(PlayerDummy dum){
		cursor = dum;
		cursor.setPos(aimPos);
	}

	//Repurposed for the aiming
	@Override
	public void drawSlice(Graphics g){
		if(cExist){
//			tempf = 360f/2f/(float)Math.PI*(float)Math.atan2(curve[1], curve[0]);
//			g.setColor(getColor(1f));
//			g.rotate(pos[0], pos[1], tempf);
//			g.fillRect(pos[0]+KICKRANGE/2f, pos[1]-1f, 3200f, 2f);
//			g.rotate(pos[0], pos[1], -tempf);
			tempf = 180f/(float)Math.PI*(float)Math.atan2(aimPos[0]-pos[0], aimPos[1]-pos[1]);
			g.setLineWidth(2f);
			g.setColor(getColor(1f));
			g.rotate(aimPos[0], aimPos[1], -tempf);
			g.drawOval(aimPos[0]-CURSORSIZE/2.5f, aimPos[1]-CURSORSIZE/2.5f, CURSORSIZE*.8f, CURSORSIZE*.8f);
			g.drawLine(aimPos[0]-CURSORSIZE/1.5f, aimPos[1], aimPos[0]+CURSORSIZE/1.5f, aimPos[1]);
			g.drawLine(aimPos[0], aimPos[1]-CURSORSIZE/1.5f, aimPos[0], aimPos[1]+CURSORSIZE/1.5f);
			g.rotate(aimPos[0], aimPos[1], tempf);
			g.setLineWidth(5f);
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

		}

		updatePos(delta);

		updateCounters(delta);
		
		updateAimPos(delta);
		
		if(power>0){//Fire!
			tempArr[0] = aimPos[0] - pos[0];
			tempArr[1] = aimPos[1] - pos[1];
			if(mag(tempArr)>0){
				unit(tempArr);
				parallelComponent(new float[] {ball.getX()-pos[0], ball.getY()-pos[1]}, tempArr , ballParallel);
				ballOrth[0] = ball.getX()-pos[0]-ballParallel[0];
				ballOrth[1] = ball.getY()-pos[1]-ballParallel[1];
				
				if(mag(ballOrth)<14f){
					ball.setVel(tempArr, 2f*(1f-mag(ballParallel)/field[0]));
					ball.setLastKicker(playerNum);
					ball.cancelAcc();
					ball.clearLocked();
				}
			}
			power = 0;
		}
		
		//theta+= omega*(float)delta;
		//if(theta>360) theta-=360;
	}
	
	public void updateAimPos(int delta){
		aimPos[0] = aimPos[0]+curve[0]*aimVelMag*(float)delta+vel[0]*velMag*(float)delta;
		aimPos[1] = aimPos[1]+curve[1]*aimVelMag*(float)delta+vel[1]*velMag*(float)delta;		
		
		if(aimPos[0]<xyLimit[0]+CURSORSIZE/2){
			aimPos[0]=xyLimit[0]+CURSORSIZE/2;
		}
		if(aimPos[0]>xyLimit[1]-CURSORSIZE/2){
			aimPos[0]=xyLimit[1]-CURSORSIZE/2;
		}
		if(aimPos[1]<xyLimit[2]+CURSORSIZE/2){
			aimPos[1]=xyLimit[2]+CURSORSIZE/2;
		}
		if(aimPos[1]>xyLimit[3]-CURSORSIZE/2){
			aimPos[1]=xyLimit[3]-CURSORSIZE/2;
		}
	}

	@Override
	public void activatePower() {
		power = 1;
	}

	@Override
	public void powerKeyReleased() {
//		power = 0;
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
	
	//Parallel component of u on v, written to w
	public void parallelComponent(float[] u, float[] v, float[] w){
		tempf = (u[0]*v[0]+u[1]*v[1])/mag(v)/mag(v);
		w[0] = v[0]*tempf;
		w[1] = v[1]*tempf;
	}
	

}
