package org.ssg.Cambridge;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class Confetti {
	
	GlobalData data;
	
	Color color;
	float size = 20;
	float[] pos;
	float[] vel;

	float theta;
	float omega;
	
	float dragCoefficient = 1;
	float grav = .002f;
	
	public Confetti(float x, float y, float vx, float vy, Color c, float rotDir, GlobalData dat){
		pos = new float[2];
		vel = new float[2];
		
		color = c;
		pos[0] = x;
		pos[1] = y;
		vel[0] = vx*.01f;
		vel[1] = vy*.01f;
		theta = 0;
		omega = rotDir*5f;
		
		data = dat;
	}
	
	public void render(Graphics g){
		g.rotate(pos[0], pos[1], theta);
		g.setColor(color);
		g.fillRect(pos[0]-size, pos[1]-size, size, size);
		//System.out.println(pos[0]+" "+ pos[1]+", " +vel[1]);
		g.rotate(pos[0], pos[1], -theta);
	}
	
	public void update(float delta){
		if(pos[1]<data.screenHeight()+50){
			pos[0]+=delta*vel[0];
			pos[1]+=delta*vel[1];
			vel[1]+=delta*(grav);
			theta += omega*delta*(float)Math.sqrt((vel[1]*vel[1]+vel[0]*vel[0]));
		}
	}
}
