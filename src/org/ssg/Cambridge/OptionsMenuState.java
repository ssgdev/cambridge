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

public class OptionsMenuState extends BasicGameState implements KeyListener{
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
	public OptionsMenuState(int i, boolean renderon) {
		stateID = i;
		shouldRender = renderon;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		
		data = ((Cambridge) sbg).getData();
		mySoundSystem = data.mySoundSystem();
		display = data.displayConfig();
		sound = data.soundConfig();
		gameplay = data.gameplayConfig();
		
		controllers = data.getC();
		
		up = false;
		down = false;
		left = false;
		right = false;
		enter = false;
		back = false;
		inputDelay = 0;
		
		tempWidth = data.screenWidth();
		tempHeight = data.screenHeight();
		
		font = data.font();
		font_white = data.whiteFont();
		font_small = data.smallFont();
		
		menuOptions = new String[] {
			"Screen Width",
			"Screen Height",
			"Fullscreen",
			"Sound - Master",
			"Sound - Effects",
			"Sound - Ambience",
			"Display Player ID"
		};
		
		selected = 0;
		
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
		
		for (int i = 0; i < menuOptions.length; i++) {
			g.drawString(menuOptions[i], data.screenWidth()/6, data.screenHeight()*0.4f+menuHeight*i);
			switch(i) {
				case 0:
					g.drawString(tempWidth+"", data.screenWidth()/5*3, data.screenHeight()*0.4f+menuHeight*i);
					break;
				case 1:
					g.drawString(tempHeight+"", data.screenWidth()/5*3, data.screenHeight()*0.4f+menuHeight*i);
					break;
				case 2:
					g.drawString((data.fullscreen() ? "On" : "off"), data.screenWidth()/5*3, data.screenHeight()*0.4f+menuHeight*i);
					break;
				case 3:
				case 4:
				case 5:
					int volume = 0;
					if (i == 3) {
						volume = data.masterSound();
					} else if (i == 4) {
						volume = data.effectSound();
					} else if (i == 5) {
						volume = data.ambientSound();
					}
					for (int j = 0; j < 10; j++) {
						if (j < volume) {
							g.fillRect(data.screenWidth()/5*3 + menuHeight/2*j, data.screenHeight()*0.4f+menuHeight*i, menuHeight/3, menuHeight/5*4);
						} else {
							g.drawRect(data.screenWidth()/5*3 + menuHeight/2*j, data.screenHeight()*0.4f+menuHeight*i, menuHeight/3, menuHeight/5*4);
						}
					}
					break;
				case 6:
					g.drawString((data.playerIdDisplay() ? "On" : "off"), data.screenWidth()/5*3, data.screenHeight()*0.4f+menuHeight*i);
					break;
				default:
					break;
			}
		}
		
		if (focus) {
			g.drawRect(data.screenWidth()/6 - 12, data.screenHeight()*0.4f+selected*menuHeight-2, font_small.getWidth(menuOptions[selected]) + 24, menuHeight+4);
		}
		g.drawRect(data.screenWidth()/6 - 10, data.screenHeight()*0.4f+selected*menuHeight, font_small.getWidth(menuOptions[selected]) + 20, menuHeight);
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
			inputDelay--;
		}
		
		if (input.isKeyPressed(Input.KEY_W) || input.isKeyPressed(Input.KEY_UP) || up) {
			if (!focus) {
				selected = --selected % menuOptions.length;
				if (selected == -1) {
					selected = menuOptions.length-1;
				}
			}
		} else if (input.isKeyPressed(Input.KEY_S) || input.isKeyPressed(Input.KEY_DOWN) || down) {
			if (!focus) {
				selected = ++selected % menuOptions.length;
			}
		} else if (input.isKeyPressed(Input.KEY_A) || input.isKeyPressed(Input.KEY_LEFT) || left) {
			if (focus) {
				switch(selected) {
					case 0:
						tempWidth -= 50;
						break;
					case 1:
						tempHeight -= 50;
						break;
					case 2:
						data.setFullscreen(!data.fullscreen());
						break;
					case 3:
						data.setMasterSound(data.masterSound() - 1 < 0 ? 0 : data.masterSound() - 1);
						mySoundSystem.setMasterVolume(data.masterSound() / 10f);
						break;
					case 4:
						data.setEffectSound(data.effectSound() - 1 < 0 ? 0 : data.effectSound() - 1);
						break;
					case 5:
						data.setAmbientSound(data.ambientSound() - 1 < 0 ? 0 : data.ambientSound() - 1);
						mySoundSystem.setVolume("BGM", data.ambientSound()/10f);
						break;
					case 6:
						data.setPlayerIdDisplay(!data.playerIdDisplay());
						break;
					default:
						break;
				}
			}
		} else if (input.isKeyPressed(Input.KEY_D) || input.isKeyPressed(Input.KEY_RIGHT) || right) {
			if (focus) {
				switch(selected) {
					case 0:
						tempWidth += 50;
						break;
					case 1:
						tempHeight += 50;
						break;
					case 2:
						data.setFullscreen(!data.fullscreen());
						break;
					case 3:
						data.setMasterSound(data.masterSound() + 1 > 10 ? 10 : data.masterSound() + 1);
						mySoundSystem.setMasterVolume(data.masterSound() / 10f);
						break;
					case 4:
						data.setEffectSound(data.effectSound() + 1 > 10 ? 10 : data.effectSound() + 1);
						break;
					case 5:
						data.setAmbientSound(data.ambientSound() + 1 > 10 ? 10 : data.ambientSound() + 1);
						mySoundSystem.setVolume("BGM", data.ambientSound()/10f);
						break;
					case 6:
						data.setPlayerIdDisplay(!data.playerIdDisplay());
						break;
					default:
						break;
				}
			}
		} else if(input.isKeyPressed(Input.KEY_ESCAPE) || back) {
			if (focus) {
				focus = false;
				switch(selected) {
					case 0:
						data.setScreenWidth(display.get("SCREENWIDTH", int.class));
						tempWidth = data.screenWidth();
						break;
					case 1:
						data.setScreenHeight(display.get("SCREENHEIGHT", int.class));
						tempHeight = data.screenHeight();
						break;
					case 2:
						data.setFullscreen(display.get("FULLSCREEN", boolean.class));
						break;
					case 3:
						data.setMasterSound(sound.get("MASTER", int.class));
						mySoundSystem.setMasterVolume(data.masterSound() / 10f);
						break;
					case 4:
						data.setEffectSound(sound.get("EFFECTS", int.class));
						break;
					case 5:
						data.setAmbientSound(sound.get("AMBIENT", int.class));
						break;
					case 6:
						data.setPlayerIdDisplay(gameplay.get("PLAYERID", boolean.class));
						break;
					default:
						break;
				}
			} else {
				((MainMenuState)sbg.getState(data.MAINMENUSTATE)).setShouldRender(true);
				setShouldRender(false);
				sbg.enterState(data.MAINMENUSTATE);
			}
		} else if (input.isKeyPressed(Input.KEY_ENTER) || enter) {
			if (focus) {
				focus = false;
				switch(selected) {
					case 0:
						data.setScreenWidth(tempWidth);
						display.put("SCREENWIDTH", data.screenWidth());
						appGc.setDisplayMode(data.screenWidth(), data.screenHeight(), data.fullscreen());
						break;
					case 1:
						data.setScreenHeight(tempHeight);
						display.put("SCREENHEIGHT", data.screenHeight());
						appGc.setDisplayMode(data.screenWidth(), data.screenHeight(), data.fullscreen());
						break;
					case 2:
						display.put("FULLSCREEN", data.fullscreen());
						appGc.setFullscreen(data.fullscreen());
						break;
					case 3:
						sound.put("MASTER", data.masterSound());
						break;
					case 4:
						sound.put("EFFECTS", data.effectSound());
						break;
					case 5:
						sound.put("AMBIENT", data.ambientSound());
						break;
					case 6:
						gameplay.put("PLAYERID", data.playerIdDisplay());
						break;
					default:
						break;
				}
				try {
					data.userConfig().store();
				} catch (IOException e) {
					System.out.println("Failed to save config!");
					e.printStackTrace();
				}
			} else {
				focus = true;
				tempHeight = data.screenHeight();
				tempWidth = data.screenWidth();
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
