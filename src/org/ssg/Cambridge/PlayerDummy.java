//Used to force camera movement without actually drawing anything

package org.ssg.Cambridge;

import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Polygon;

import paulscode.sound.SoundSystem;

public class PlayerDummy extends Player{

	public PlayerDummy(int n, int tN, float[] consts, int[] f, int[] c, CambridgeController c1,
			float[] p, int[] xyL, Color se, SoundSystem ss,
			String sn, Image slc) {
		super(n, tN, consts, f, c, c1, p, xyL, se, ss, sn, slc);
		
		poly = new Polygon(new float[]{0,0});
		
		KICKRANGE = 0;
		PLAYERSIZE = 0;
	}

	public void setPos(float[] f){
		pos = f;
	}
	
	public void setVel(float[] v){
		vel = v;
	}
	
	@Override
	public void render(Graphics g, float BALLSIZE, Image triangle, AngelCodeFont font_small, boolean drawId){
		
	}
	
	@Override
	public void update(float delta) {
		
	}

	@Override
	public void activatePower() {

	}

	@Override
	public void powerKeyReleased() {

	}

	@Override
	public boolean isKicking() {
		return false;
	}

	@Override
	public boolean flashKick() {
		return false;
	}

	@Override
	public void setPower() {
		
	}
	
	@Override
	public void keyPressed(int a, char b){
		
	}
	
	@Override
	public void keyReleased(int a, char b){
		
	}

}
