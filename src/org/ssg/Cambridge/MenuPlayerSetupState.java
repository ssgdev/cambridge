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

public class MenuPlayerSetupState extends BasicGameState implements KeyListener {
	private GlobalData data;
	private Ini ini;
	private int stateID;
	SoundSystem mySoundSystem;
	Ini.Section display, sound, gameplay;

	private boolean keyboardOneTaken, keyboardTwoTaken;

	CambridgeController[] controllers;
	CambridgePlayerAnchor[] anchors;

	private final float deadzone = 0.28f;
	private int inputDelay;
	private final int inputDelayConst = 10;

	private boolean shouldRender;

	final static String RESDIR = "res/";
	private AngelCodeFont font, font_white, font_small;

	private Cambridge cambridge;
	private AppGameContainer appGc;

	//Constructor
	public MenuPlayerSetupState(int i, boolean renderon) {
		stateID = i;
		shouldRender = renderon;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {

		data = ((Cambridge) sbg).getData();
		mySoundSystem = data.mySoundSystem();

		// Hopefully refresh the controller list
		data.initializeControllers();
		data.getControllers();
		controllers = data.getC();
		anchors = data.playerAnchors();

		keyboardOneTaken = false;
		keyboardTwoTaken = false;

		inputDelay = 0;

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
		
		g.setLineWidth(2f);

		g.setBackground(Color.black);

		g.setColor(Color.white);

		g.setFont(font_small);
		
		//Drawing Character Selection Area
		g.drawRect(data.screenWidth() * 5/16, data.screenHeight() * 1/4, data.screenWidth() * 3/8, data.screenHeight() * 1/2);
		g.drawLine(data.screenWidth() * 7/16, data.screenHeight() * 1/4, data.screenWidth() * 7/16, data.screenHeight() * 3/4);
		g.drawLine(data.screenWidth() * 9/16, data.screenHeight() * 1/4, data.screenWidth() * 9/16, data.screenHeight() * 3/4);
		g.drawLine(data.screenWidth() * 5/16, data.screenHeight() * 5/12, data.screenWidth() * 11/16, data.screenHeight() * 5/12);
		g.drawLine(data.screenWidth() * 5/16, data.screenHeight() * 7/12, data.screenWidth() * 11/16, data.screenHeight() * 7/12);

		//Drawing Player Boxes
		g.drawRect(data.screenWidth() * 1/16, data.screenHeight() * 1/12, data.screenWidth() * 1/4, data.screenHeight() * 1/3);
		g.drawRect(data.screenWidth() * 11/16, data.screenHeight() * 1/12, data.screenWidth() * 1/4, data.screenHeight() * 1/3);
		g.drawRect(data.screenWidth() * 1/16, data.screenHeight() * 7/12, data.screenWidth() * 1/4, data.screenHeight() * 1/3);
		g.drawRect(data.screenWidth() * 11/16, data.screenHeight() * 7/12, data.screenWidth() * 1/4, data.screenHeight() * 1/3);

		g.setFont(font_white);
		g.drawString("P1", data.screenWidth() * 3/8 - font_white.getWidth("P1")/2, data.screenHeight()*1/12);
		g.drawString("P2", data.screenWidth() * 5/8 - font_white.getWidth("P2")/2, data.screenHeight()*1/12);
		g.drawString("P3", data.screenWidth() * 3/8 - font_white.getWidth("P3")/2, data.screenHeight()*11/12 - font_white.getLineHeight());
		g.drawString("P4", data.screenWidth() * 5/8 - font_white.getWidth("P4")/2, data.screenHeight()*11/12 - font_white.getLineHeight());

		if (anchors[0].initiated()) {
			g.fillRect(data.screenWidth() * 1/16, data.screenHeight() * 1/12, data.screenWidth() * 1/4, data.screenHeight() * 1/9);
			if (anchors[0].characterSelected()) {
				g.fillRect(data.screenWidth() * 1/16, data.screenHeight() * 1/12, data.screenWidth() * 1/4, data.screenHeight() * 2/9);
			}
			if (anchors[0].isReady()) {
				g.fillRect(data.screenWidth() * 1/16, data.screenHeight() * 1/12, data.screenWidth() * 1/4, data.screenHeight() * 1/3);
			}
		}

		if (anchors[1].initiated()) {
			g.fillRect(data.screenWidth() * 11/16, data.screenHeight() * 1/12, data.screenWidth() * 1/4, data.screenHeight() * 1/9);
			if (anchors[1].characterSelected()) {
				g.fillRect(data.screenWidth() * 11/16, data.screenHeight() * 1/12, data.screenWidth() * 1/4, data.screenHeight() * 2/9);
			}
			if (anchors[1].isReady()) {
				g.fillRect(data.screenWidth() * 11/16, data.screenHeight() * 1/12, data.screenWidth() * 1/4, data.screenHeight() * 1/3);
			}
		}

		if (anchors[2].initiated()) {
			g.fillRect(data.screenWidth() * 1/16, data.screenHeight() * 7/12, data.screenWidth() * 1/4, data.screenHeight() * 1/9);
			if (anchors[2].characterSelected()) {
				g.fillRect(data.screenWidth() * 1/16, data.screenHeight() * 7/12, data.screenWidth() * 1/4, data.screenHeight() * 2/9);
			}
			if (anchors[2].isReady()) {
				g.fillRect(data.screenWidth() * 1/16, data.screenHeight() * 7/12, data.screenWidth() * 1/4, data.screenHeight() * 1/3);
			}
		}

		if (anchors[3].initiated()) {
			g.fillRect(data.screenWidth() * 11/16, data.screenHeight() * 7/12, data.screenWidth() * 1/4, data.screenHeight() * 1/9);
			if (anchors[3].characterSelected()) {
				g.fillRect(data.screenWidth() * 11/16, data.screenHeight() * 7/12, data.screenWidth() * 1/4, data.screenHeight() * 2/9);
			}
			if (anchors[3].isReady()) {
				g.fillRect(data.screenWidth() * 11/16, data.screenHeight() * 7/12, data.screenWidth() * 1/4, data.screenHeight() * 1/3);
			}
		}
		
		for (CambridgePlayerAnchor a : anchors) {
			if (a.initiated()) {
				g.drawRect(
						data.screenWidth() * 5/16 + (a.getCharacter() % 3) * data.screenWidth() * 1/8 + 5,
						data.screenHeight() * 1/4 + (a.getCharacter() / 3) * data.screenHeight() * 1/6 + 5,
						data.screenWidth() * 1/8 - 10,
						data.screenHeight() * 1/6 - 10
						);
			}
		}
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		Input input = gc.getInput();

		if (input.isKeyPressed(Input.KEY_ESCAPE)) {
			((MenuMainState)sbg.getState(data.MENUMAINSTATE)).setShouldRender(true);
			setShouldRender(false);
			sbg.enterState(data.MENUMAINSTATE);
		}

		int readyNum = 0;
		int existsNum = 0;
		for (int i = 0; i < anchors.length; i++) {
			if (anchors[i].characterSelected())
				readyNum++;
			if (anchors[i].initiated())
				existsNum++;
		}

		// There are still slots to be filled
		if (existsNum < anchors.length) {
			if (!keyboardOneTaken && input.isKeyPressed(Input.KEY_PERIOD)) {
				anchors[existsNum] = new CambridgePlayerAnchor(0, existsNum, 0, 0, new CambridgeController());
				keyboardOneTaken = true;
			} else if (!keyboardTwoTaken && input.isKeyPressed(Input.KEY_2)) {
				anchors[existsNum] = new CambridgePlayerAnchor(0, existsNum, 0, 1, new CambridgeController());
				keyboardTwoTaken = true;
			} else {
				for (CambridgeController c : controllers) {
					boolean used = false;
					if (inputDelay <= 0) {
						if (c.exists() && c.poll() && c.getMenuSelect()) {
							for (CambridgePlayerAnchor a : anchors) {
								if (a.controller() == c) {
									System.out.println("Controller is already taken! :)");
									used = true;
								}
							}
							if (!used) {
								anchors[existsNum] = new CambridgePlayerAnchor(0, existsNum, 0, -1, c);
								inputDelay = inputDelayConst;
							}
						}
					} else {
						inputDelay--;
					}
				}
			}
		}

		// Read input from activated anchors
		if (existsNum > 0) {
			for (int i = 0; i < anchors.length; i++) {
				if (anchors[i].initiated()) {
					if (!anchors[i].characterSelected()) {
						if (anchors[i].down(gc, delta)) {
							anchors[i].changeCharacter(3);
						}
						if (anchors[i].up(gc, delta)) {
							anchors[i].changeCharacter(-3);
						}
						if (anchors[i].left(gc, delta)) {
							anchors[i].changeCharacter(-1);
						} 
						if (anchors[i].right(gc, delta)) {
							anchors[i].changeCharacter(1);
						} 
						if (anchors[i].back(gc, delta)) {
							switch(anchors[i].getKeyboard()) {
							case 0:
								keyboardOneTaken = false;
								break;
							case 1:
								keyboardTwoTaken = false;
								break;
							default:
								break;
							}
							anchors[i] = new CambridgePlayerAnchor();
						} 
						if (anchors[i].select(gc, delta)) {
							if (anchors[i].getCharacter() == 8) {
								anchors[i].changeCharacter((int)(Math.random() * 7) + 1);
							}
							anchors[i].setCharacter(true);
						}
						//					} else if (!a.teamSelected()) {
						//						if (a.left(gc)) {
						//							a.changeTeam(-1);
						//						} else if (a.right(gc)) {
						//							a.changeTeam(1);
						//						} else if (a.back(gc)) {
						//							a.setCharacter(false);
						//						} else if (a.select(gc)) {
						//							a.setTeam(true);
						//						}
						//					} else if (!anchors[i].isReady()) {
						//						System.out.println(anchors[i].getCharacter());
						//						if (anchors[i].back(gc)) {
						//							anchors[i].setCharacter(false);
						//						} else if (anchors[i].select(gc)) {
						//							anchors[i].ready();
						//						}
					} else {
						if (anchors[i].back(gc, delta)) {
							anchors[i].setCharacter(false);
						}
					}
				}
			}
		}

		// Move onto next gamestate if all initiated players are ready
		if (readyNum == existsNum && existsNum > 1) {
			((MenuGamemodeSetupState)sbg.getState(data.MENUGAMEMODESETUPSTATE)).setShouldRender(true);
			setShouldRender(false);
			sbg.enterState(data.MENUGAMEMODESETUPSTATE);
		}

		input.clearKeyPressedRecord();
	}

	@Override
	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {

	}

	@Override
	public void leave(GameContainer gc, StateBasedGame sbg) throws SlickException {

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
