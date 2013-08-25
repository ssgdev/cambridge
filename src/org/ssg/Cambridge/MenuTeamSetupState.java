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

public class MenuTeamSetupState extends BasicGameState implements KeyListener {
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

	private Cambridge cambridge;
	private AppGameContainer appGc;

	//Constructor
	public MenuTeamSetupState(int i, boolean renderon) {
		stateID = i;
		shouldRender = renderon;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {

		data = ((Cambridge) sbg).getData();
		mySoundSystem = data.mySoundSystem();

		anchors = data.playerAnchors();

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

		g.setFont(font_white);

		g.drawString(
				"Team Select",
				data.screenWidth()/2 - font_white.getWidth("Team Select")/2,
				data.screenHeight() * 1/12
				);
		
		for (int i = 0; i < anchors.length; i++) {
			if (anchors[i].initiated()) {
				int x = data.screenWidth() * (2+anchors[i].getTeam())/4;
				g.drawString(anchors[i].playerNum()+"", x - font_white.getWidth(anchors[i].playerNum()+""), data.screenHeight() * 1/3 + i*50);
				if (anchors[i].teamSelected()) {
					g.drawRect(x - 5, data.screenHeight() * 1/3 + i*50 - 5, font_white.getWidth(anchors[i].playerNum()+"")+10, font_white.getLineHeight()+10);
				}
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
		int[] teamCount = new int[] {0, 0};
		for (int i = 0; i < anchors.length; i++) {
			if (anchors[i].teamSelected())
				readyNum++;
			if (anchors[i].initiated())
				existsNum++;
			if (anchors[i].getTeam() == 1 && anchors[i].teamSelected())
				teamCount[1]++;
			else if (anchors[i].getTeam() == -1 && anchors[i].teamSelected()) {
				teamCount[0]++;
			}
		}
		
		// Read input from activated anchors
		if (existsNum > 0) {
			for (int i = 0; i < anchors.length; i++) {
				if (anchors[i].initiated()) {
					if (!anchors[i].teamSelected()) {
						if (anchors[i].left(gc, delta)) {
							anchors[i].changeTeam(-1);
						} else if (anchors[i].right(gc, delta)) {
							anchors[i].changeTeam(1);
						} else if (anchors[i].back(gc, delta)) {
							((MenuGamemodeSetupState)sbg.getState(data.MENUGAMEMODESETUPSTATE)).setShouldRender(true);
							setShouldRender(false);
							sbg.enterState(data.MENUGAMEMODESETUPSTATE);
						} else if (anchors[i].select(gc, delta)) {
							if (anchors[i].getTeam() != 0) {
								if (anchors[i].getTeam() == 1 && teamCount[1] != existsNum-1
										|| anchors[i].getTeam() == -1 && teamCount[0] != existsNum-1) {
									anchors[i].setTeam(true);
								}
							}
						}
					} else {
						if (anchors[i].back(gc, delta)) {
							anchors[i].setTeam(false);
						}
					}
				}
			}
		}

		// Move onto next gamestate if all initiated players are ready
		if (readyNum == existsNum && existsNum > 1) {
			((GameplayState)sbg.getState(data.GAMEPLAYSTATE)).setShouldRender(true);
			setShouldRender(false);
			sbg.enterState(data.GAMEPLAYSTATE);
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
