package org.ssg.Cambridge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

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
import paulscode.sound.SoundSystemConfig;

public class MenuGamemodeSetupState extends BasicGameState implements KeyListener {
	private GlobalData data;
	private String[] menuOptions;
	private Ini ini;
	private int stateID;
	SoundSystem mySoundSystem;
	Ini.Section display, sound, gameplay;

	CambridgeController[] controllers;
	CambridgePlayerAnchor[] anchors;

	private boolean down, up, left, right, back, enter;
	private final float deadzone = 0.28f;
	private int inputDelay;
	private final int inputDelayConst = 10;

	private boolean shouldRender;

	final static String RESDIR = "res/";
	private AngelCodeFont font, font_white, font_small;
	//Image mapSoccer, mapHockey, mapTennis, mapSquash, mapFoursquare;
	Image[] maps;
	
	
	private Cambridge cambridge;
	private AppGameContainer appGc;

	private int selected;
	
	private float tempX;//Used for drawing the field
	private float tempY;
	private float tempf;
	
	private float cursorY, cursorYTarget;
	
	//Constructor
	public MenuGamemodeSetupState(int i, boolean renderon) {
		stateID = i;
		shouldRender = renderon;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {

		data = ((Cambridge) sbg).getData();
		mySoundSystem = data.mySoundSystem();

		anchors = data.playerAnchors();

		up = false;
		down = false;
		left = false;
		right = false;
		enter = false;
		back = false;
		inputDelay = 0;

		font = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0.png"));
		font_white = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0_white.png"));
		font_small = new AngelCodeFont(data.RESDIR + "8bitoperator_small.fnt", new Image(data.RESDIR + "8bitoperator_small_0.png"));

		maps = new Image[data.gameConfig().get("CONF").get("NUMGAMES", int.class)];
		maps[0] = new Image(data.RESDIR+"map_soccer.png");
		maps[1] = new Image(data.RESDIR+"map_hockey.png");
		maps[2] = new Image(data.RESDIR+"map_tennis.png");
		maps[3] = new Image(data.RESDIR+"map_squash.png");
		maps[4] = new Image(data.RESDIR+"map_foursquare.png");
		
		((MenuTeamSetupState)(sbg.getState(data.MENUTEAMSETUPSTATE))).setMaps(maps);
		
		cambridge = (Cambridge) sbg;
		appGc = (AppGameContainer) gc;

		menuOptions = new String[] {
				"Time Limit",
				"Score Limit",
				"Camera",
				"Start Game"
		};
		
		reset();
	}
	
	public void reset() {
		selected = 0;
		cursorY = data.screenHeight()/3f-font_small.getLineHeight()/2f-5;
		cursorYTarget = cursorY;
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		if (!shouldRender)
			return;

		g.setAntiAlias(true);
		
		g.setLineWidth(2f);

		g.setBackground(Color.black);

		g.setColor(Color.white);

		g.setFont(font_white);
		
		//Draw cursor
		g.drawRect(-10, cursorY, data.screenWidth()+20, font_small.getLineHeight() + 10f);
		
		drawField(g);
		
		g.drawString(
				data.gameConfig().get(data.gameType()+"").get("NAME", String.class),
				data.screenWidth()/2 - font_white.getWidth(data.gameConfig().get(data.gameType()+"").get("NAME", String.class))/2,
				data.screenHeight() * 1/3 - font_white.getLineHeight()/2
				);

		g.setFont(font_small);

		for (int i = 0; i < menuOptions.length; i++) {
			g.drawString(
					menuOptions[i],
					data.screenWidth() * 1/4,
					data.screenHeight() * (7.5f+i)/12 + (data.screenHeight() * 1/12 - font_small.getLineHeight()) / 2
					);
			switch(i) {
			case 0:
				g.drawString(
						data.timeLimit() > 0 ? (data.timeLimit()/60 + ":" + String.format("%02d",data.timeLimit()%60)) : "Unlimited",
						data.screenWidth()/2,
						data.screenHeight() * (7.5f+i)/12 + (data.screenHeight() * 1/12 - font_small.getLineHeight()) / 2);
				break;
			case 1:
				g.drawString(
						data.scoreLimit() > 0 ? data.scoreLimit()+"" : "Unlimited",
						data.screenWidth()/2,
						data.screenHeight() * (7.5f+i)/12 + (data.screenHeight() * 1/12 - font_small.getLineHeight()) / 2);
				break;
			case 2:
				g.drawString((data.actionCam() ? "Dynamic" : "Fixed"), data.screenWidth()/2, data.screenHeight() * (7.5f+i)/12 + (data.screenHeight() * 1/12 - font_small.getLineHeight()) / 2);
				break;
			default:
				break;
			}
		}
	}

	public void drawField(Graphics g){
		tempX = maps[data.gameType()].getWidth();
		tempY = maps[data.gameType()].getHeight();
		
		//Set scaling factor
		tempf = Math.min(data.screenWidth()/tempX, data.screenHeight()/tempY);
		
		tempX *= tempf/2f;
		tempY *= tempf/2f;
		
		g.drawImage(maps[data.gameType()].getScaledCopy((int)tempX, (int)tempY), data.screenWidth()/2-tempX/2, data.screenHeight()/3f-tempY/2);
	}
	
	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		Input input = gc.getInput();

		if (selected > 0) {
//			g.drawRect(
//					-10,
//					data.screenHeight() * (6.5f+selected)/12 + (data.screenHeight() * 1/12 - font_small.getLineHeight()) / 2 - 5,
//					data.screenWidth() + 20,
//					font_small.getLineHeight() + 10
//					);
			cursorYTarget = data.screenHeight() * (6.5f+selected)/12 + (data.screenHeight() * 1/12 - font_small.getLineHeight()) / 2 - 5;
		}else{//Game mode
			cursorYTarget = data.screenHeight()/3f-font_small.getLineHeight()/2f-5;
		}
		
		//cursorY = approachTarget(cursorY, cursorYTarget, (float)delta/2f);
		cursorY = cursorYTarget;
		
		if (input.isKeyPressed(Input.KEY_ESCAPE)) {
			((MenuMainState)sbg.getState(data.MENUMAINSTATE)).setShouldRender(true);
			setShouldRender(false);
			sbg.enterState(data.MENUMAINSTATE);
		}

		up = false;
		down = false;
		left = false;
		right = false;
		back = false;
		enter = false;
		if (inputDelay <= 0) {
			for (CambridgePlayerAnchor a : anchors) {
				if (a.initiated()) {
					if (up || down || left || right || enter || back) {
						inputDelay = inputDelayConst;
					} else {
						if (a.down(gc, delta)) {
							down = true;
						} else if (a.up(gc, delta)) {
							up = true;
						} else if (a.left(gc, delta)) {
							left = true;
						} else if (a.right(gc, delta)) {
							right = true;
						} else if (a.select(gc, delta)) {
							enter = true;
						} else if (a.back(gc, delta)) {
							back = true;
						}
					}
				}
			}
		} else {
			inputDelay--;
		}

		if (up) {
			if (selected > 0) {
				selected--;
				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			}
		} else if (down) {
			if (selected < 4) {
				selected++;
				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			}
		} else if (left) {
			switch(selected) {
			case 0:
				data.setGameType((data.gameType() > 0) ? data.gameType()-1 : data.GAMEMODES-1);
				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				break;
			case 1:
				data.setTimeLimit(-1);
				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				break;
			case 2:
				data.setScoreLimit(-1);
				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				break;
			case 3:
				data.setActionCam(!data.actionCam());
				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				break;
			case 4:
				break;
			default:
				break;
			}
		} else if (right) {
			switch(selected) {
			case 0:
				data.setGameType((data.gameType() < data.GAMEMODES-1) ? data.gameType()+1 : 0);
				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				break;
			case 1:
				data.setTimeLimit(1);
				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				break;
			case 2:
				data.setScoreLimit(1);
				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				break;
			case 3:
				data.setActionCam(!data.actionCam());
				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				break;
			case 4:
				break;
			default:
				break;
			}
		} else if(back) {
//			for (CambridgePlayerAnchor a: anchors) {
//				a.setCharacter(false);
//			}
			mySoundSystem.quickPlay( true, "MenuBack.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			((MenuPlayerSetupState)sbg.getState(data.MENUPLAYERSETUPSTATE)).setShouldRender(true);
			setShouldRender(false);
			sbg.enterState(data.MENUPLAYERSETUPSTATE);
		} else if (enter) {
			mySoundSystem.quickPlay( true, "MenuThud.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			if (selected != 4) {
				selected = 4;
			} else {
				if (data.gameType() == data.GAMEMODES-1) {//Foursquare
					((GameplayState)sbg.getState(data.GAMEPLAYSTATE)).setShouldRender(true);
					setShouldRender(false);
					sbg.enterState(data.GAMEPLAYSTATE);
				} else {
					((MenuTeamSetupState)sbg.getState(data.MENUTEAMSETUPSTATE)).setStart(data.gameType(), data.screenWidth()/2-tempX/2, data.screenHeight()/3f-tempY/2, tempX, tempY);
					((MenuTeamSetupState)sbg.getState(data.MENUTEAMSETUPSTATE)).setShouldRender(true);
					setShouldRender(false);
					sbg.enterState(data.MENUTEAMSETUPSTATE);
				}
			}
		}

		input.clearKeyPressedRecord();
	}

	@Override
	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
		reset();
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
