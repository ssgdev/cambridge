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
import paulscode.sound.SoundSystemConfig;

public class MenuMainState extends BasicGameState implements KeyListener{
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
	private boolean[] selectFlag, backFlag, leftFlag, rightFlag, upFlag, downFlag;
	private int[] inputDelay;
	private final int inputDelayConst = 200;
	
	Image bg_img;
	
	private boolean shouldRender;
	
	private float cursorY, cursorYTarget;
	
	final static String RESDIR = "res/";
	private AngelCodeFont font, font_white;
	
	//Constructor
	public MenuMainState(int i, boolean renderon) {
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
		
		selectFlag = new boolean[4];
		backFlag = new boolean[4];
		leftFlag = new boolean[4];
		rightFlag = new boolean[4];
		upFlag = new boolean[4];
		downFlag = new boolean[4];
		inputDelay = new int[4];
		resetButtons();
		
		try {
			font = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0.png"));
			font_white = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0_white.png"));
			//font_small = new AngelCodeFont(RESDIR + "8bitoperator_small.fnt", new Image(RESDIR + "8bitoperator_small_0.png"));
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			System.out.println("Fonts not loaded properly. Uh oh. Spaghettio.");
			e.printStackTrace();
		}
		
		bg_img = new Image(RESDIR + "mainbg_1080p.png");
		
		selected = 0;
		
		menuOptions = new String[] {"Start Game", "Options", "Exit"};
	}
	
	public void resetButtons() {
		for(int i=0; i<4; i++){
			selectFlag[i] = false;
			backFlag[i] = false;
			leftFlag[i] = false;
			rightFlag[i] = false;
			upFlag[i] = false;
			downFlag[i] = false;
			inputDelay[i] = inputDelayConst;
		}
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		if (!shouldRender)
			return;
		
		g.drawImage(bg_img.getScaledCopy(data.screenWidth(), data.screenHeight()), 0, 0);
		
		g.setAntiAlias(true);
		
		g.setLineWidth(2f);
		
		g.setBackground(Color.black);
		
		//Vertical bar
		g.setColor(Color.black);
		g.fillRect(data.screenWidth()/6-20, 0, font.getWidth(menuOptions[0])+40, data.screenHeight());
		g.setColor(Color.white);
		g.drawRect(data.screenWidth()/6-20, 0, font.getWidth(menuOptions[0])+40, data.screenHeight());
		
		//horizontal bar
		g.setColor(Color.black);
		g.fillRect(-10, cursorY, data.screenWidth()+20, 70);
		g.setColor(Color.white);
		g.drawRect(-10, cursorY, data.screenWidth()+20, 70);
		
		g.setFont(font_white);
		g.setColor(Color.white);
		
		g.drawString(menuOptions[0], data.screenWidth()/6, data.screenHeight()*0.5f);
		g.drawString(menuOptions[1], data.screenWidth()/6, data.screenHeight()*0.5f+menuHeight);
		g.drawString(menuOptions[2], data.screenWidth()/6, data.screenHeight()*0.5f+menuHeight*2);
		
		//draw cursor
//		g.drawRect(data.screenWidth()/6 - 10, data.screenHeight()*0.5f+selected*menuHeight + 7, font_white.getWidth(menuOptions[selected]) + 20, 70);

	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		Input input = gc.getInput();
		
		cursorYTarget = data.screenHeight()*0.5f+selected*menuHeight + 7;
//		cursorY = approachTarget(cursorY, cursorYTarget, delta/2f);
		cursorY = cursorYTarget;
		
		up = false;
		down = false;
		left = false;
		right = false;
		back = false;
		enter = false;
		for (int i=0; i<controllers.length; i++) {
			if (controllers[i].exists() && controllers[i].poll()) {
				
				// Analog stick checking
				if (inputDelay[i] <= 0) {
					if (controllers[i].getLeftStickY() > deadzone) {
						down = true;
					} else if (controllers[i].getLeftStickY() < -deadzone) {
						up = true;
					}
					if (controllers[i].getLeftStickX() > deadzone) {
						right = true;
					} else if (controllers[i].getLeftStickX() < -deadzone) {
						left = true;
					}
					if (up || down || left || right) {
						inputDelay[i] = inputDelayConst;
					}
				} else {
					inputDelay[i]-=delta;
				}
				
				// D-Pad input checking
				if (downFlag[i]) {
					if (controllers[i].getDPad() != data.DPAD_DOWN) {
						downFlag[i] = false;
					}
				} else {
					if (controllers[i].getDPad() == data.DPAD_DOWN) {
						downFlag[i] = true;
						down = true;
					}
				}
				if (upFlag[i]) {
					if (controllers[i].getDPad() != data.DPAD_UP) {
						upFlag[i] = false;
					}
				} else {
					if (controllers[i].getDPad() == data.DPAD_UP) {
						upFlag[i] = true;
						up = true;
					}
				}
				if (leftFlag[i]) {
					if (controllers[i].getDPad() != data.DPAD_LEFT) {
						leftFlag[i] = false;
					}
				} else {
					if (controllers[i].getDPad() == data.DPAD_LEFT) {
						leftFlag[i] = true;
						left = true;
					}
				}
				if (rightFlag[i]) {
					if (controllers[i].getDPad() != data.DPAD_RIGHT) {
						rightFlag[i] = false;
					}
				} else {
					if (controllers[i].getDPad() == data.DPAD_RIGHT) {
						rightFlag[i] = true;
						right = true;
					}
				}
				
				// A and B button checking
				if (backFlag[i]) {
					if (!controllers[i].getMenuBack()) {
						backFlag[i] = false;
					}
				} else {
					if (controllers[i].getMenuBack()) {
						backFlag[i] = true;
						back = true;
					}
				}
				if (selectFlag[i]) {
					if (!controllers[i].getMenuSelect()) {
						selectFlag[i] = false;
					}
				} else {
					if (controllers[i].getMenuSelect()) {
						selectFlag[i] = true;
						enter = true;
					}
				}
			}
		}
		
		if (input.isKeyPressed(Input.KEY_W) || input.isKeyPressed(Input.KEY_UP) || up) {
			selected = --selected % 3;
			if (selected == -1)
				selected = 2;
			mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		} else if (input.isKeyPressed(Input.KEY_S) || input.isKeyPressed(Input.KEY_DOWN) || down) {
			selected = ++selected % 3;
			mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		} else if(input.isKeyPressed(Input.KEY_ESCAPE) || back){
			mySoundSystem.quickPlay( true, "MenuBack.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			gc.exit();
			mySoundSystem.cleanup();
		} else if (input.isKeyPressed(Input.KEY_ENTER) || enter) {
			mySoundSystem.quickPlay( true, "MenuThud.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			switch (selected) {
				case 0:
					((MenuPlayerSetupState)sbg.getState(data.MENUPLAYERSETUPSTATE)).setShouldRender(true);
					setShouldRender(false);
					sbg.enterState(data.MENUPLAYERSETUPSTATE);
					break;
				case 1:
					((MenuOptionsState)sbg.getState(data.MENUOPTIONSSTATE)).setShouldRender(true);
					setShouldRender(false);
					sbg.enterState(data.MENUOPTIONSSTATE);
					break;
				case 2:
					gc.exit();
					mySoundSystem.cleanup();
				default:
					break;
			}
		}
		
		input.clearKeyPressedRecord();
		
	}
	
	@Override
	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
		mySoundSystem.backgroundMusic("BGM", "BGMHotline.ogg" , true);
		mySoundSystem.setVolume("BGM", data.ambientSound()/10f);
		resetButtons();
		selected = 0;
		cursorYTarget = data.screenHeight()*0.5f+selected*menuHeight + 7;
		cursorY = cursorYTarget;
	}

	@Override
	public void leave(GameContainer gc, StateBasedGame sbg) throws SlickException {
		
	}
	
	public float approachTarget(float val, float target, float inc){

		if(val<target){
			val+=inc;
			if(val>target)
				val=target;
		}
		if(val>target){
			val-=inc;
			if(val<target)
				val=target;
		}

		return val;
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
