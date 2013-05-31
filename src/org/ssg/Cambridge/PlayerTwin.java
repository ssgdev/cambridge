//Is the main twin

package org.ssg.Cambridge;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import paulscode.sound.SoundSystem;

public class PlayerTwin extends Player{

	PlayerTwin twin;
	int twinNum;
	Image hemicircle;
	
	boolean buttonPressed;
	
	float DEFAULTKICKRANGE;
	float DESIREDKICKRANGE;
	float DEFAULTPLAYERSIZE;
	float DESIREDPLAYERSIZE;
	float DEFAULTKICK;
	
	float theta2;
	float DESIREDORBITRADIUS;
	float orbitRadius;
	
	Component actionButton2;
	Component moveStickX, moveStickY;
	Component otherStickX, otherStickY;
	
	int nukes;//nucleii
	
	public PlayerTwin(int n, float[] consts, int[] f, int[] c, Controller c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, int tn, Image hc) throws SlickException {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn);
		
		DEFAULTKICKRANGE = KICKRANGE;
		DESIREDKICKRANGE = KICKRANGE;
		DEFAULTPLAYERSIZE = PLAYERSIZE;
		DESIREDPLAYERSIZE = PLAYERSIZE;
		DEFAULTKICK = NORMALKICK;		
		orbitRadius = 0; 
		DESIREDORBITRADIUS = 20;
		
		theta2 = 0;
		
		twinNum = tn;
		if(twinNum== 0){
			nukes = 1;
			moveStickX = lStickX;
			moveStickY = lStickY;
			otherStickX = rStickX;
			otherStickY = rStickY;
			//actionButton = this.c.getComponent(Component.Identifier.Button._5);
		}else if(twinNum==1){
			nukes = 1;
			actionButton = this.c.getComponent(Component.Identifier.Button._4);
			moveStickX = rStickX;
			moveStickY = rStickY;
			otherStickX = lStickX;
			otherStickY = lStickY;
			omega *= -1;
		}
		
		hemicircle = hc;
		
		buttonPressed = false;
	}

	public void setTwin(PlayerTwin p){
		twin = p;
	}
	
	@Override
	public void update(int delta) {
		if(cExist){
			
			if(nukes>0){
				vel[0] = moveStickX.getPollData();
				vel[1] = moveStickY.getPollData();
				if (Math.abs(vel[0]) < 0.2)
					vel[0] = 0f;
				if (Math.abs(vel[1]) < 0.2)
					vel[1] = 0f;
			}else{
				vel[0]=0;
				vel[1]=0;
			}
			
			if(twin.numNukes()==0){
				curve[0] = otherStickX.getPollData();
				curve[1] = otherStickY.getPollData();
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

		velMag = VELMAG * (.5f+.5f*(float)nukes);
		NORMALKICK = DEFAULTKICK * (.5f+.5f*(float)nukes);
		
		DESIREDKICKRANGE = DEFAULTKICKRANGE * (1f + .2f*(float)(nukes*nukes));
		
		if(KICKRANGE < DESIREDKICKRANGE){
			KICKRANGE+=2;
			
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
		}
		if(KICKRANGE > DESIREDKICKRANGE)
			KICKRANGE-=2;
		

		
		if(nukes==0){
			DESIREDPLAYERSIZE = 0;
		}else{
			DESIREDPLAYERSIZE = DEFAULTPLAYERSIZE;
		}
		
		if(PLAYERSIZE < DESIREDPLAYERSIZE )
			PLAYERSIZE++;
		if(PLAYERSIZE > DESIREDPLAYERSIZE )
			PLAYERSIZE--;
		
		if(nukes<=1){
			DESIREDORBITRADIUS = 0;
		}else{
			DESIREDORBITRADIUS = 20;
		}
		
		if(orbitRadius < DESIREDORBITRADIUS)
			orbitRadius++;
		if(orbitRadius > DESIREDORBITRADIUS)
			orbitRadius--;
		
		lastKickAlpha -= (float)(delta)/2400f;
		if(lastKickAlpha<0){
			lastKickAlpha = 0f;
		}
		
		kickingCoolDown -= (float)delta;
		if(kickingCoolDown<0)
			kickingCoolDown = 0;

		theta+= (1f-(powerCoolDown/POWERCOOLDOWN))*omega*(float)delta;
		if(theta>360) theta-=360;
		
		theta2 += -1*omega*.002f*(float)delta;
		if(theta2>360) theta2-=360;
		
	}
	
	@Override
	public void drawPlayer(Graphics g){
		g.drawImage(hemicircle.getScaledCopy(KICKRANGE/hemicircle.getHeight()), pos[0]-KICKRANGE/2+twinNum*KICKRANGE/2, pos[1]-KICKRANGE/2, getColor(.2f));
		
		g.setColor(getColor());
		
		g.rotate(getX()+(float)Math.cos(theta2)*orbitRadius, getY()+(float)Math.sin(theta2)*orbitRadius, getTheta());
		g.drawRect(getX()+(float)Math.cos(theta2)*orbitRadius-PLAYERSIZE/2, getY()+(float)Math.sin(theta2)*orbitRadius-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
		g.rotate(getX()+(float)Math.cos(theta2)*orbitRadius, getY()+(float)Math.sin(theta2)*orbitRadius, -getTheta());
		
		g.rotate(getX()-(float)Math.cos(theta2)*orbitRadius, getY()-(float)Math.sin(theta2)*orbitRadius, getTheta());
		g.drawRect(getX()-(float)Math.cos(theta2)*orbitRadius-PLAYERSIZE/2, getY()-(float)Math.sin(theta2)*orbitRadius-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
		g.rotate(getX()-(float)Math.cos(theta2)*orbitRadius, getY()-(float)Math.sin(theta2)*orbitRadius, -getTheta());
		
	}
	
	@Override
	public void drawNameTag(Graphics g, Image triangle, AngelCodeFont font_small){
		g.drawImage(triangle, getX()-triangle.getWidth()/2, getY()-getKickRange()/2-25, getColor());
		g.setColor(getColor());
		g.setFont(font_small);
		g.drawString(("P"+(getPlayerNum()+1)), getX()-font_small.getWidth("P"+(getPlayerNum()+1))/2, getY()-font_small.getHeight("P")-getKickRange()/2-30);
	}
	

	@Override
	public void activatePower() {
		if(twin.numNukes()<2){
			twin.addNukes(+1);
			nukes--;
		}
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
		if(twin.numNukes()==0)
			return curve;
		return vel;
	}
	
	public int numNukes(){
		return nukes;
	}
	
	public void addNukes(int n){
		nukes += n;
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
