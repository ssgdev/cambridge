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
import paulscode.sound.SoundSystemConfig;

public class PlayerTwin extends Player{

	PlayerTwin twin;
	int twinNum;
	Image hemicircle;
	Image slice_twin;
	
	Ball ball;
	
	float DEFAULTKICKRANGE;
	float DESIREDKICKRANGE;
	float DEFAULTPLAYERSIZE;
	float DESIREDPLAYERSIZE;
	float DEFAULTKICK;
	
	float theta2;
	float DESIREDORBITRADIUS;
	float orbitRadius;

	float syncFactor;
	
	Component moveStickX, moveStickY;
	Component otherStickX, otherStickY;
	
	int nukes;//nucleii
	
	boolean actionButton;
	
	public PlayerTwin(int n, float[] consts, int[] f, int[] c, CambridgeController c1, boolean c1Exist, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc, Image slc_t, int tn, Image hc, Ball b) throws SlickException {
		super(n, consts, f, c, c1, c1Exist, p, xyL, se, ss, sn, slc);
		
		ball = b;
		
		DEFAULTKICKRANGE = KICKRANGE;
		DESIREDKICKRANGE = KICKRANGE;
		DEFAULTPLAYERSIZE = PLAYERSIZE*.8f;
		DESIREDPLAYERSIZE = PLAYERSIZE*.8f;
		DEFAULTKICK = NORMALKICK;
		orbitRadius = 0;
		DESIREDORBITRADIUS = 20;
		
		theta2 = 0;
		
		twinNum = tn;
		nukes = 1;
		if(twinNum== 0 && c1.exists()){
			moveStickX = c1.leftStickX();
			moveStickY = c1.leftStickY();
			otherStickX = c1.rightStickX();
			otherStickY = c1.rightStickY();
		}else if(twinNum==1 && c1.exists()){
			moveStickX = c1.rightStickX();
			moveStickY = c1.rightStickY();
			otherStickX = c1.leftStickX();
			otherStickY = c1.leftStickY();
			omega *= -1;
		}
		
		hemicircle = hc;
		slice_twin = slc_t;
		
		syncFactor = 1f;

	}

	public void setTwin(PlayerTwin p){
		twin = p;
	}
	
	@Override
	public void update(float delta) {
		if(c.exists()){
			
			if(nukes>0){
				vel[0] = moveStickX.getPollData();
				vel[1] = moveStickY.getPollData();
				if (Math.abs(vel[0]) < 0.1)
					vel[0] = 0f;
				if (Math.abs(vel[1]) < 0.1)
					vel[1] = 0f;
			}else{
				vel[0]=0;
				vel[1]=0;
			}
			
			if(twin.numNukes()==0){
				curve[0] = otherStickX.getPollData();
				curve[1] = otherStickY.getPollData();
				if(Math.abs(curve[0]) < 0.1)
					curve[0] = 0;
				if(Math.abs(curve[1]) < 0.1)
					curve[1] = 0;
			}
			
			actionButton = (twinNum == 0) ? c.getAction() : c.getAction2();
			
			if (actionButton){
				if(!buttonPressed){
					activatePower();
					buttonPressed = true;
				}
			}else if(buttonPressed){
					powerKeyReleased();
					buttonPressed = false;
			}
		}else{
			
			vel[0] = 0;
			vel[1] = 0;
			
			if(twinNum == 0 && buttonPressed || !(buttonPressed || button2Pressed)){
				pollKeys(delta);
			}
			if(buttonReleased){
				buttonPressed = false;
				buttonReleased = false;
			}
			
			if(twinNum ==  1 && button2Pressed  || !(buttonPressed || button2Pressed)){
				pollKeys(delta);
			}
			if(button2Released){
				button2Pressed = false;
				button2Released = false;
			}			
			
		}
		
		updatePos(delta);

		updateCounters(delta);
		
		velMag = VELMAG * (.7f+.3f*(float)nukes);
//		NORMALKICK = DEFAULTKICK * (.5f+.5f*(float)nukes);
		
		DESIREDKICKRANGE = DEFAULTKICKRANGE * (.8f + .2f*(float)(nukes*nukes));
		
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

		theta+= (1f-(powerCoolDown/POWERCOOLDOWN))*omega*delta;
		if(theta>360) theta-=360;
		
		theta2 += -1*omega*.002f*delta;
		if(theta2>360) theta2-=360;
		
		syncFactor = dot(vel, twin.getVel());
		
	}
	
	@Override
	public void drawKickCircle(Graphics g){
		//Draw kicking circle
		g.setColor(getColor(.5f).darker());
		g.drawOval(pos[0]-KICKRANGE/2, pos[1]-KICKRANGE/2, KICKRANGE, KICKRANGE);
		
		//Kicking circle flash when kick happens
		g.setColor(getColor2().brighter());
		g.drawOval(pos[0]-KICKRANGE/2f, pos[1]-KICKRANGE/2f, KICKRANGE, KICKRANGE);
	}
	
	//Repurposed to draw synchronization rating. More sync means harder kicks
	@Override
	public void drawSlice(Graphics g){
		if(nukes == 2)
			super.drawSlice(g);
		
		tempf = 360f/2f/(float)Math.PI*(float)Math.atan2(vel[1], vel[0]);
		g.rotate(pos[0], pos[1], tempf);
		g.drawImage(slice_twin, pos[0]-slice_twin.getWidth()/2f, pos[1]-slice_twin.getHeight()/2f, getColor(syncFactor*.75f));
		g.rotate(pos[0], pos[1], -tempf);
	}
	
	@Override
	public void drawPlayer(Graphics g){
		g.drawImage(hemicircle.getScaledCopy(KICKRANGE/hemicircle.getHeight()), pos[0]-KICKRANGE/2+twinNum*KICKRANGE/2, pos[1]-KICKRANGE/2, getColor(.2f));

		if(PLAYERSIZE>0){
			g.rotate(pos[0]+(float)Math.cos(theta2)*orbitRadius, pos[1]+(float)Math.sin(theta2)*orbitRadius, getTheta());
			g.setColor(Color.black);
			g.fillRect(pos[0]+(float)Math.cos(theta2)*orbitRadius-PLAYERSIZE/2, pos[1]+(float)Math.sin(theta2)*orbitRadius-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
			g.setColor(getColor());
			g.drawRect(pos[0]+(float)Math.cos(theta2)*orbitRadius-PLAYERSIZE/2, pos[1]+(float)Math.sin(theta2)*orbitRadius-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
			g.rotate(pos[0]+(float)Math.cos(theta2)*orbitRadius, pos[1]+(float)Math.sin(theta2)*orbitRadius, -getTheta());
			
			g.rotate(pos[0]-(float)Math.cos(theta2)*orbitRadius, pos[1]-(float)Math.sin(theta2)*orbitRadius, getTheta());
			g.setColor(Color.black);
			g.fillRect(pos[0]-(float)Math.cos(theta2)*orbitRadius-PLAYERSIZE/2, pos[1]-(float)Math.sin(theta2)*orbitRadius-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
			g.setColor(getColor());
			g.drawRect(pos[0]-(float)Math.cos(theta2)*orbitRadius-PLAYERSIZE/2, pos[1]-(float)Math.sin(theta2)*orbitRadius-PLAYERSIZE/2, PLAYERSIZE, PLAYERSIZE);
			g.rotate(pos[0]-(float)Math.cos(theta2)*orbitRadius, pos[1]-(float)Math.sin(theta2)*orbitRadius, -getTheta());
		}
	}
	
	@Override
	public void drawNameTag(Graphics g, Image triangle, AngelCodeFont font_small){
		if(DESIREDPLAYERSIZE > 0){
			g.drawImage(triangle, pos[0]-triangle.getWidth()/2, pos[1]-KICKRANGE/2-25, getColor());
			g.setColor(getColor());
			g.setFont(font_small);
			g.drawString(("P"+(playerNum+1)), pos[0]-font_small.getWidth("P"+(playerNum+1))/2, pos[1]-font_small.getHeight("P")-KICKRANGE/2-30);
		}
	}
	

	@Override
	public void activatePower() {
		if(twin.numNukes()<2){
			twin.addNukes(+1);
			nukes--;
			if(twinNum == 0){
				mySoundSystem.quickPlay( true, slowMo?"TwinsLtoRSlow.ogg":"TwinsLtoR.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			}else{
				mySoundSystem.quickPlay( true, slowMo?"TwinsRtoLSlow.ogg":"TwinsRtoL.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			}
		}
	}

	@Override
	public void powerKeyReleased() {
		//Does nothing
	}
	
	@Override
	public boolean isKicking() {
		return kickingCoolDown == 0;
	}

	//Is called after flashkick
	@Override
	public void setKicking(Ball b){
//		b.setCanBeKicked(playerNum, false);
		kickingCoolDown = KICKCOOLDOWN;
	}
	
	@Override
	public float kickStrength(){
		if(mag(vel)==0){
			return .5f;
		}else if(flashKick()){
			return POWERKICK*(1f+syncFactor);
		}else{
			return NORMALKICK*(1f+syncFactor);
		}
	}
	
	@Override
	public boolean flashKick() {
		if(syncFactor>=.9f){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public void setPower() {
		
	}
	
	@Override
	public float[] getCurve() {
		if(nukes == 2)
			return curve;
		return new float[]{0,0};
	}
	
	public int numNukes(){
		return nukes;
	}
	
	public void addNukes(int n){
		nukes += n;
	}
	
	public int getTwinNum(){
		return twinNum;
	}
}
