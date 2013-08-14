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
	float fakeCounter;//counts the lifetime of the fakes
	float fakeMoveCounter;//counts the intervals between direction changes of the fake player
	float FAKECOUNTER;
	
	//For the decoy
	float[] fakePos;
	//curve[] acts as fakeVel[]
	float fakeAlpha;
	
	PlayerDummy dummy;
	
	float camoAlpha;
	float camoAlphaTarget;
	
	public PlayerTricky(int n, float[] consts, int[] f, int[] c, CambridgeController c1, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc, Ball b) {
		super(n, consts, f, c, c1, p, xyL, se, ss, sn, slc);
		
		MAXPOWER = 1;
		
		ball = b;
		fakeballAlpha = 0f;
		fakeKickPos = new float[2];
		fakeCounter = 0;
		fakeMoveCounter = 0;
		FAKECOUNTER = 5000f;
		
		fakePos = new float[2];
		fakeAlpha = 0f;
		
		camoAlpha = 1f;
		camoAlphaTarget = 1f;
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
		if(power==0 && fakeAlpha == 0)
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
	public void drawPowerCircle(Graphics g){
		//Draw power circle
		if(isPower() && fakeAlpha <= 0){
			g.setColor(getColor3());
			g.drawOval(pos[0]-KICKRANGE/2f-power/2f, pos[1]-KICKRANGE/2f-power/2f, KICKRANGE+power, KICKRANGE+power);
			g.setColor(Color.white);
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
	
	//These colors are overridden to fade away if active camo is on
	@Override
	public Color getColor(){
		return getColor6(1f);
	}

	@Override
	public Color getColor(float f){
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), f*camoAlpha);
	}

	@Override
	public Color getColor3(){//return color of powercircle
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), ((power/MAXPOWER))*camoAlpha);
	}

	@Override
	public Color getColor4(){
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), (powerCoolDown+500f)/500f*camoAlpha);
	}

	@Override
	public Color getColor5(){//powerkick bar
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), lastKickAlpha*camoAlpha);
	}
	
	//Returns the actual "color" scaled by the float, instead of a weird alternate color
	public Color getColor6(float f){
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(256f*f*camoAlpha));
	}
	
	@Override
	public void update(float delta) {
		if (c.exists()) {
			pollController(delta);
			
			if(c.getAction() && !buttonPressed){
				buttonPressed = true;
				activatePower();
			}else if(!c.getAction() && buttonPressed){
				buttonPressed = false;
				powerKeyReleased();
			}
			
			//This doesn't do anything right now
			if(c.getAction2() && !button2Pressed){
				button2Pressed = true;
				camoAlphaTarget = 0f;
			}else if(!c.getAction2() && button2Pressed){
				button2Pressed = false;
				camoAlphaTarget = 1f;
			}
		}else{
			
			pollKeys(delta);
			
			if(buttonPressed){
				activatePower();
				buttonPressed = false;
			}
			if(buttonReleased){
				powerKeyReleased();
				buttonReleased = false;
			}
			
			if(button2Pressed){
				camoAlphaTarget = 0f;
			}
			if(button2Released){
				button2Pressed = false;
				button2Released = false;
				camoAlphaTarget = 1f;
			}
			
			//Fake player AI code
			if(fakeAlpha <= 0){//Haven't kicked it yet
				tempf = (float)(Math.random()*Math.PI*2.0);
				curve[0] = (float)Math.cos(tempf);
				curve[1] = (float)Math.sin(tempf);
			}else{
				if(fakeMoveCounter>0){
					fakeMoveCounter -= delta;
				}else{
					tempArr[0] = fakeball.getX()+fakeball.getVelX()*5f-fakePos[0];
					tempArr[1] = fakeball.getY()+fakeball.getVelY()*5f-fakePos[1];
					tempf = (float)(Math.round(Math.atan2(tempArr[1], tempArr[0])/Math.PI*4.0)*Math.PI/4.0);//Round to 8dirs
					curve[0] = (float)Math.cos(tempf);
					curve[1] = (float)Math.sin(tempf);
					
					unit(curve);
					
					fakeMoveCounter = 400;
					if(dist(fakeball.getX(), fakeball.getY(), fakePos[0], fakePos[1]) < KICKRANGE)
						fakeMoveCounter = 700;
				}
			}
			
		}

		updatePos(delta);

		updateCounters(delta);

		camoAlpha = approachTarget(camoAlpha, camoAlphaTarget, delta/600f);
		
		if(fakeAlpha>0){
			updateFakePos(delta);
			
			fakeball.update(delta);
			
			fakeCounter -= delta;
			if(fakeCounter < 0)
				fakeCounter = 0;
			
			if(fakeCounter == 0){
				fakeAlpha -= delta/2400f;
				fakeballAlpha -= delta/2400f;
			}
		}else{
			fakePos[0] = approachTarget(fakePos[0], pos[0], delta);
			fakePos[1] = approachTarget(fakePos[1], pos[1], delta);
		}
		
		theta+= omega*delta;
		if(theta>360) theta-=360;
	}
	
	public void updateFakePos(float delta){
		fakePos[0] = (fakePos[0]+curve[0]*velMag*delta);
		fakePos[1] = (fakePos[1]+curve[1]*velMag*delta);

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
	
		//player on player collision handling and also the fake ball on player collision code goes here for convenience
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
		return kickingCoolDown == 0;
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
