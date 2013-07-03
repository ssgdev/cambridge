package org.ssg.Cambridge;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import paulscode.sound.SoundSystem;

public class PlayerTricky extends Player{

	Ball ball;
	BallFake fakeball;
	float fakeballAlpha;
	float[] fakeKickPos;
	float fakeCounter;
	float FAKECOUNTER;
	
	//For the decoy
	float[] fakePos;
	//curve[] acts as fakeVel[]
	float fakeAlpha;
	
	PlayerDummy dummy;
	
	boolean buttonPressed;
	Component actionButton2;
	boolean button2Pressed;
	
	public PlayerTricky(int n, float[] consts, int[] f, int[] c, Controller c1,	boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc, Ball b) {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, slc);

		ball = b;
		fakeballAlpha = 0f;
		fakeKickPos = new float[2];
		fakeCounter = 0;
		FAKECOUNTER = 5000f;
		
		fakePos = new float[2];
		fakeAlpha = 0f;
		
		buttonPressed = false;
		button2Pressed = false;
		if(cExist){
			actionButton2 = this.c.getComponent(Component.Identifier.Button._4);
		}
	}
	
	public void setFakeBall(BallFake b){
		fakeball = b;
	}

	public void setDummy(PlayerDummy dum){
		dummy = dum;
		dummy.setPos(fakePos);//This should be pass by reference so this is enough to lock the dummy with the fakePos forever
	}
	
	//Don't draw the slice if you're in power, since that gives away which is the real one
	@Override
	public void drawSlice(Graphics g){
		if(power==0)
			super.drawSlice(g);
	}
	
	@Override
	public void drawKickTrail(Graphics g){
		g.setColor(getColor5());
		g.setLineWidth(KICKRANGE*1.5f);
//		tempTrailArr = p.getTrailArr();//{bx, by, px, py}
		float dx = lastKickPos[0]-lastKickBallPos[0];
		float dy = lastKickPos[1]-lastKickBallPos[1];
		tempf = (float)Math.atan2((double)dy, (double)dx);
		g.rotate(lastKickBallPos[0], lastKickBallPos[1], 360f/2f/(float)Math.PI*tempf);
		//g.drawLine(getTrailArr()[0]-2*FIELDWIDTH,getTrailArr()[1], getTrailArr()[0]+2*FIELDWIDTH, getTrailArr()[1]);
		//g.drawLine(getTrailArr()[0]-2*1600,getTrailArr()[1], getTrailArr()[0]+2*1600, getTrailArr()[1]);
		g.fillRect(lastKickBallPos[0]-2*1600, lastKickBallPos[1]-lastKickWidth/2-5, 1600*4, lastKickWidth+10);
		g.rotate(lastKickBallPos[0], lastKickBallPos[1], -360f/2f/(float)Math.PI*tempf);
		
		dx = fakeKickPos[0]-lastKickBallPos[0];
		dy = fakeKickPos[1]-lastKickBallPos[1];
		tempf = (float)Math.atan2((double)dy, (double)dx);
		g.rotate(lastKickBallPos[0], lastKickBallPos[1], 360f/2f/(float)Math.PI*tempf);
		g.fillRect(lastKickBallPos[0]-2*1600, lastKickBallPos[1]-lastKickWidth/2-5, 1600*4, lastKickWidth+10);
		g.rotate(lastKickBallPos[0], lastKickBallPos[1], -360f/2f/(float)Math.PI*tempf);
		g.setLineWidth(5f);
	}
	
	@Override
	public void drawPlayer(Graphics g){
		g.setColor(getColor());
		g.rotate(pos[0], pos[1], theta);
		g.drawRect(pos[0]-PLAYERSIZE/2, pos[1]-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
		g.rotate(pos[0], pos[1], -theta);
		//g.drawOval(getX()-getKickRange()/2f+BALLSIZE/2f, getY()-getKickRange()/2f+BALLSIZE/2f, getKickRange()-BALLSIZE, getKickRange()-BALLSIZE);//Draw kicking circle;
		
		if(fakeAlpha>0){
			//Draw fake ball
			g.setColor(Color.red.scaleCopy(fakeballAlpha));
			g.rotate(fakeball.getX(), fakeball.getY(), fakeball.getTheta());
			g.fillRect(fakeball.getX()-10f, fakeball.getY()-10f, 20f, 20f);
			g.rotate(fakeball.getX(), fakeball.getY(), -fakeball.getTheta());
			
			//Draw fake player
			g.setColor(getColor6(fakeAlpha));
			g.rotate(fakePos[0], fakePos[1], theta);
			g.drawRect(fakePos[0]-PLAYERSIZE/2, fakePos[1]-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
			g.rotate(fakePos[0], fakePos[1], -theta);	
			g.setColor(getColor(.5f*fakeAlpha).darker());
			g.drawOval(fakePos[0]-KICKRANGE/2, fakePos[1]-KICKRANGE/2, KICKRANGE, KICKRANGE);
		}
		
	}
	
	@Override
	public void drawNameTag(Graphics g, Image triangle, AngelCodeFont font_small){
		g.drawImage(triangle, getX()-triangle.getWidth()/2, getY()-getKickRange()/2-25, getColor());
		g.setColor(getColor());
		g.setFont(font_small);
		g.drawString("P"+(playerNum+1), pos[0]-font_small.getWidth("P"+(playerNum+1))/2, pos[1]-font_small.getHeight("P")-KICKRANGE/2-30);
		
		//Fake nametag for the fake
		g.drawImage(triangle, fakePos[0]-triangle.getWidth()/2, fakePos[1]-getKickRange()/2-25, getColor6(fakeAlpha));
		g.setColor(getColor6(fakeAlpha));
		g.setFont(font_small);
		g.drawString("P"+(playerNum+1), fakePos[0]-font_small.getWidth("P"+(playerNum+1))/2, fakePos[1]-font_small.getHeight("P")-KICKRANGE/2-30);
		
		
	}
	
	//Returns the actual "color" scaled by the float, instead of a weird alternate color
	public Color getColor6(float f){
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(256f*f));
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

		if(fakeAlpha>0){
			updateFakePos(delta);
			
			fakeball.update(delta);
			
			fakeCounter -= (float)delta;
			if(fakeCounter < 0)
				fakeCounter = 0;
			
			if(fakeCounter == 0){
				fakeAlpha -= (float)delta/2400f;
				fakeballAlpha -= (float)delta/2400f;
			}
		}else{
			fakePos[0] = pos[0];
			fakePos[1] = pos[1];
		}
		
		theta+= omega*(float)delta;
		if(theta>360) theta-=360;
	}
	
	public void updateFakePos(int delta){
		fakePos[0] = (fakePos[0]+curve[0]*velMag*(float)delta);
		fakePos[1] = (fakePos[1]+curve[1]*velMag*(float)delta);

		if(fakePos[0]<xyLimit[0]+KICKRANGE/2){
			fakePos[0]=xyLimit[0]+KICKRANGE/2;
			bool = true;//Indicating a wall collision occured
		}
		if(fakePos[0]>xyLimit[1]-KICKRANGE/2){
			fakePos[0]=xyLimit[1]-KICKRANGE/2;
			bool = true;
		}
		if(fakePos[1]<xyLimit[2]+KICKRANGE/2){
			fakePos[1]=xyLimit[2]+KICKRANGE/2;
			bool = true;
		}
		if(fakePos[1]>xyLimit[3]-KICKRANGE/2){
			fakePos[1]=xyLimit[3]-KICKRANGE/2;
			bool = true;
		}
	
		//player on player collision handling and also the fake ball collision code goes here for convenience
		for(Player otherPlayer: players){
			if(otherPlayer != this && otherPlayer != dummy){
				tempf = dist(fakePos[0], fakePos[1], otherPlayer.getX(), otherPlayer.getY());
				if(fakeAlpha > 0.2f && tempf < (KICKRANGE + otherPlayer.getKickRange())/2){
					fakeAlpha = 0.2f;
				}
				tempf = dist(fakeball.getX(), fakeball.getY(), otherPlayer.getX(), otherPlayer.getY());
				if(fakeballAlpha > 0.5f && tempf < otherPlayer.getKickRange()/2){
					fakeballAlpha = 0.5f;
				}
			}
		}
	}

	@Override
	public void activatePower() {
		power = 1;
	}

	@Override
	public void powerKeyReleased() {
		power = 0;
	}

	@Override
	public boolean isKicking() {
		return true;
	}

	@Override
	public boolean flashKick() {
		return power > 0;
	}

	@Override
	public void setPower() {
		fakeball.setPos(ball.getX(), ball.getY());
		//Take the ball a step back, to prevent going through the player
		tempArr[0] = (fakeball.getX()-pos[0]);
		tempArr[1] = (fakeball.getY()-pos[1]);
		
		unit(tempArr);
		tempf = 0;//Used to store the amount of player velocity added to the kick
		if(sameDir(vel[0], tempArr[0])){
			tempArr[0] += curve[0];
			tempf += curve[0]*curve[0];
		}
		if(sameDir(vel[1], tempArr[1])){
			tempArr[1] += curve[1];
			tempf += curve[1]*curve[1];
		}
		fakeball.setVel(new float[]{tempArr[0], tempArr[1]}, POWERKICK*0.5f+(float)Math.sqrt(tempf)*0.5f);
		fakeAlpha = 1f;
		fakeballAlpha = 1f;
		fakeCounter = FAKECOUNTER;
		
		fakeKickPos[0] = ball.getPrevX()+tempArr[0];
		fakeKickPos[1] = ball.getPrevY()+tempArr[1];
		
		fakePos[0] = pos[0];
		fakePos[1] = pos[1];
	}

	@Override
	public float[] getCurve(){
		return new float[]{0f,0f};
	}
	
	public boolean sameDir(float vx, float dir){
		if(vx == 0)
			return false;
		return vx/Math.abs(vx) == dir/Math.abs(dir);
	}
}
