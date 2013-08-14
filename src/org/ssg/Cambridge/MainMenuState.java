package org.ssg.Cambridge;

import java.util.ArrayList;
import net.java.games.input.Controller;

import org.newdawn.slick.AngelCodeFont;
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

public class MainMenuState extends BasicGameState implements KeyListener{
	private GlobalData data;
	private String[] menuOptions;
	private int stateID;
	SoundSystem mySoundSystem;
	
	private int selected;
	private boolean focus;
	private final int menuHeight = 80;
	
	CambridgeController c1, c2, c3, c4;
	CambridgeController[] controllers;
	boolean c1Exist, c2Exist, c3Exist, c4Exist;
	private final float deadzone = 0.28f;
	private boolean down, up, enter;
	private int inputDelay;
	private final int inputDelayConst = 10;
	
	private boolean shouldRender;
	
	final static String RESDIR = "res/";
	private AngelCodeFont font, font_white, font_small;
	
	//Constructor
	public MainMenuState(int i, boolean renderon) {
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
		controllers = data.getC();
		
		up = false;
		down = false;
		enter = false;
		inputDelay = 0;
		
		font = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0.png"));
		font_white = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0_white.png"));
		font_small = new AngelCodeFont(RESDIR + "8bitoperator_small.fnt", new Image(RESDIR + "8bitoperator_small_0.png"));
		
		selected = 0;
		
		menuOptions = new String[] {"Start Game", "Options", "Exit"};
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		if (!shouldRender)
			return;
		
		g.setAntiAlias(true);
		
		g.setBackground(Color.black);
		
		g.setColor(Color.white);
		g.setFont(font_white);
		g.drawString("Main Menu", data.screenWidth()/6, data.screenHeight()*0.1f);
		
		g.drawString(menuOptions[0], data.screenWidth()/6, data.screenHeight()*0.5f);
		g.drawString(menuOptions[1], data.screenWidth()/6, data.screenHeight()*0.5f+menuHeight);
		g.drawString(menuOptions[2], data.screenWidth()/6, data.screenHeight()*0.5f+menuHeight*2);
		
		g.drawRect(data.screenWidth()/6 - 10, data.screenHeight()*0.5f+selected*menuHeight + 7, font.getWidth(menuOptions[selected]) + 20, 70);
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		Input input = gc.getInput();
		
		up = false;
		down = false;
		enter = false;
		if (inputDelay <= 0) {
			for (CambridgeController c: controllers) {
				if (c.exists() && c.poll()) {
					if (c.getLeftStickY() > deadzone) {
						down = true;
					} else if (c.getLeftStickY() < -deadzone) {
						up = true;
					}
					enter = c.getAction();
				}
			}
			if (up || down || enter)
				inputDelay = inputDelayConst;
		} else {
			inputDelay--;
		}
		
		if (input.isKeyPressed(Input.KEY_W) || input.isKeyPressed(Input.KEY_UP) || up) {
			selected = --selected % 3;
			if (selected == -1)
				selected = 2;
		} else if (input.isKeyPressed(Input.KEY_S) || input.isKeyPressed(Input.KEY_DOWN) || down) {
			selected = ++selected % 3;
		} else if(input.isKeyPressed(Input.KEY_ESCAPE)){
			gc.exit();
			mySoundSystem.cleanup();
		} else if (input.isKeyPressed(Input.KEY_ENTER) || enter) {
			switch (selected) {
				case 0:
					((GameplayState)sbg.getState(data.GAMEPLAYSTATE)).setShouldRender(true);
					sbg.enterState(data.GAMEPLAYSTATE);
					break;
				case 1:
					((OptionsMenuState)sbg.getState(data.OPTIONSMENUSTATE)).setShouldRender(true);
					sbg.enterState(data.OPTIONSMENUSTATE);
					break;
				case 2:
					gc.exit();
					mySoundSystem.cleanup();
				default:
					break;
			}
		}
		
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
