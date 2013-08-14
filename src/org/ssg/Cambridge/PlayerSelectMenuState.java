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
	private String[] menuOptions;
	private Ini ini;
	private int stateID;
	SoundSystem mySoundSystem;
	Ini.Section display, sound, gameplay;
	
	private int selected;
	private boolean focus;
	private final int menuHeight = 30;
	private int tempHeight, tempWidth;

	CambridgeController[] controllers;
	
	private final float deadzone = 0.28f;
	private boolean down, up, left, right, back, enter;
	private int inputDelay;
	private final int inputDelayConst = 10;
	
	private boolean shouldRender;
	
	final static String RESDIR = "res/";
	private AngelCodeFont font, font_white, font_small;
	
	private Cambridge cambridge;
	private AppGameContainer appGc;
	
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
		
		controllers = data.getC();
		
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
