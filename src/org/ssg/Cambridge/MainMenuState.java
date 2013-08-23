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
	
	CambridgeController[] controllers;
	
	private final float deadzone = 0.28f;
	private boolean down, up, left, right, enter, back;
	private int inputDelay;
	private final int inputDelayConst = 200;
	
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
		
		controllers = data.getC();
		
		up = false;
		down = false;
		left = false;
		right = false;
		enter = false;
		back = false;
		inputDelay = 0;
		
		try {
			font = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0.png"));
			font_white = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0_white.png"));
			font_small = new AngelCodeFont(RESDIR + "8bitoperator_small.fnt", new Image(RESDIR + "8bitoperator_small_0.png"));
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			System.out.println("Fonts not loaded properly. Uh oh. Spaghettio.");
			e.printStackTrace();
		}
		
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
		g.drawString("MAIN MENU", data.screenWidth()/6, data.screenHeight()*0.1f);
		
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
		left = false;
		right = false;
		back = false;
		enter = false;
		if (inputDelay <= 0) {
			for (CambridgeController c: controllers) {
				if (c.exists() && c.poll()) {
					if (c.getLeftStickY() > deadzone) {
						down = true;
					} else if (c.getLeftStickY() < -deadzone) {
						up = true;
					}
					if (c.getLeftStickX() > deadzone) {
						right = true;
					} else if (c.getLeftStickX() < -deadzone) {
						left = true;
					}
					back = c.getMenuBack();
					enter = c.getMenuSelect();
				}
			}
			if (up || down || left || right || enter || back)
				inputDelay = inputDelayConst;
		} else {
			inputDelay-=delta;
		}
		
		if (input.isKeyPressed(Input.KEY_W) || input.isKeyPressed(Input.KEY_UP) || up) {
			selected = --selected % 3;
			if (selected == -1)
				selected = 2;
		} else if (input.isKeyPressed(Input.KEY_S) || input.isKeyPressed(Input.KEY_DOWN) || down) {
			selected = ++selected % 3;
		} else if(input.isKeyPressed(Input.KEY_ESCAPE) || back){
			gc.exit();
			mySoundSystem.cleanup();
		} else if (input.isKeyPressed(Input.KEY_ENTER) || enter) {
			switch (selected) {
				case 0:
					((GameplayState)sbg.getState(data.GAMEPLAYSTATE)).setShouldRender(true);
					setShouldRender(false);
					sbg.enterState(data.GAMEPLAYSTATE);
					break;
				case 1:
					((OptionsMenuState)sbg.getState(data.OPTIONSMENUSTATE)).setShouldRender(true);
					setShouldRender(false);
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
