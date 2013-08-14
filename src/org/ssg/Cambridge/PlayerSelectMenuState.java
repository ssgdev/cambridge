package org.ssg.Cambridge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.java.games.input.Controller;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;

public class PlayerSelectMenuState extends BasicGameState implements KeyListener{
	private GlobalData data;
	private Ini ini;
	private int stateID;
	SoundSystem mySoundSystem;
	
	private boolean shouldRender;

	private AngelCodeFont font, font_white, font_small;
	
	private Cambridge cambridge;
	private AppGameContainer appGc;
	
	CambridgeController c1, c2, c3, c4;
	boolean c1Exist, c2Exist, c3Exist, c4Exist;
	
	//Constructor
	public PlayerSelectMenuState(int i, boolean renderon) {
		stateID = i;
		shouldRender = renderon;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		
		data = ((Cambridge) sbg).getData();
		mySoundSystem = data.mySoundSystem();
		
		c1 = data.getC()[0];
		c2 = data.getC()[1];
		c3 = data.getC()[2];
		c4 = data.getC()[3];
		c1Exist = (c1 != null);
		c2Exist = (c2 != null);
		c3Exist = (c3 != null);
		c4Exist = (c4 != null);
		
		font = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0.png"));
		font_white = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0_white.png"));
		font_small = new AngelCodeFont(data.RESDIR + "8bitoperator_small.fnt", new Image(data.RESDIR + "8bitoperator_small_0.png"));
		
		cambridge = (Cambridge) sbg;
		appGc = (AppGameContainer) gc;
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		if (!shouldRender)
			return;
		
		g.setAntiAlias(true);
		
		g.setBackground(Color.black);
//		g.fillRect(0, 0, gc.getScreenWidth(), gc.getScreenHeight());
		
		g.setColor(Color.white);
		g.setFont(font_white);
		g.drawString("Options", data.screenWidth()/6, data.screenHeight()*0.1f);
		
		g.setFont(font_small);
		
		
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		
	}
	
	@Override
	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
		
	}
	
	public void setShouldRender(boolean shouldRender) {
		this.shouldRender = shouldRender;
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return stateID;
	}

}
