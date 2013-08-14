package org.ssg.Cambridge;

import java.io.File;
import java.io.IOException;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import paulscode.sound.SoundSystem;

public class GlobalData {
	final static String RESDIR = "res/";
	public final String userconfigdir = "user_config.cfg";
	public final String gameconfigdir = "config.cfg";
	public final int GAMEPLAYSTATE = 10;
	public final int MAINMENUSTATE = 11;
	public final int OPTIONSMENUSTATE = 12;
	public final int PLAYERSELECTMEUNSTATE = 13;
	private int screenHeight;
	private int screenWidth;
	private boolean fullscreen;
	private int masterSound;
	private int effectSound;
	private int ambientSound;
	private boolean playerIdDisplay;
	private SoundSystem mySoundSystem;
	private Ini userConfig, gameConfig;
	private Ini.Section displayConfig, soundConfig, gameplayConfig;
	
	private ControllerEnvironment controllerEnv;
	private CambridgeController[] controllers;
	private ControllerListener cListener;

	private boolean loaded;
	
	public GlobalData() {
		userConfig = null;
		gameConfig = null;
		displayConfig = null;
		soundConfig = null;
		gameplayConfig = null;
		loaded = false;
		this.screenHeight = 800;
		this.screenWidth = 600;
		this.fullscreen = false;
		this.masterSound = 10;
		this.effectSound = 10;
		this.ambientSound = 10;
		this.playerIdDisplay = true;
		this.controllerEnv = ControllerEnvironment.getDefaultEnvironment();
		controllers = new CambridgeController[4];
		cListener = null;
		initializeControllers();
		loadConfig();
		getControllers();
	}
	
	public void loadConfig() {
		try {
			userConfig = new Ini(new File(RESDIR + userconfigdir));
			gameConfig = new Ini(new File(RESDIR + gameconfigdir));
			displayConfig = userConfig.get("DISPLAY");
			setScreenWidth(displayConfig.get("SCREENWIDTH", int.class));
			setScreenHeight(displayConfig.get("SCREENHEIGHT", int.class));
			setFullscreen(displayConfig.get("FULLSCREEN", boolean.class));
			
			soundConfig = userConfig.get("AUDIO");
			setMasterSound(soundConfig.get("MASTER", int.class));
			setEffectSound(soundConfig.get("EFFECTS", int.class));
			setAmbientSound(soundConfig.get("AMBIENT", int.class));
			
			gameplayConfig = userConfig.get("GAMEPLAY");
			setPlayerIdDisplay((gameplayConfig.get("PLAYERID", boolean.class)));
			
			loaded = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Config Load Failed!");
			e.printStackTrace();
		}
	}
	
	// DOES NOT WORK RIGHT NOW. Looking into JInput and source code, but things aren't promising.
	private class CambridgeControllerListener implements ControllerListener {
		@Override
		public void controllerRemoved(ControllerEvent ev) {
			removeControllers();
			System.out.println("Controller Removed!" + ev);
		}
		
		@Override
		public void controllerAdded(ControllerEvent ev) {
			getControllers();
			System.out.println("Controller Added!" + ev);
		}
	}
	
	// Method to initialize controllers and attach controller added and removed listeners.
	public void initializeControllers() {
		cListener = new CambridgeControllerListener();
		
		controllerEnv.addControllerListener(cListener);
		
		for (int i = 0; i < controllers.length; i++) {
			controllers[i] = new CambridgeController();
		}
	}
	
	public void getControllers() {
		for (Controller c : controllerEnv.getControllers()) {
//			System.out.println(c.getName() + " " + c.getType());
			if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
				if (!controllers[0].exists() || controllers[0].poll() == false) {
					controllers[0] = new CambridgeController(c);
//					controllers.addControllerListener(c1.);
					System.out.println("First Controller Found: " + controllers[0].getName());
				} else if (!controllers[1].exists() || controllers[1].poll() == false) {
					controllers[1] = new CambridgeController(c);
					System.out.println("Second Controller Found: " + controllers[1].getName());
				} else if (!controllers[2].exists() || controllers[2].poll() == false) {
					controllers[2] = new CambridgeController(c);
					System.out.println("Third Controller Found: " + controllers[2].getName());
				} else if (!controllers[3].exists() || controllers[3].poll() == false) {
					controllers[3] = new CambridgeController(c);
					System.out.println("Fourth Controller Found: " + controllers[3].getName());
				} //end if-else block
			} //end if statement
		} //end for each loop
	}
	
	public void removeControllers() {
		for (CambridgeController cTemp: controllers) {
			if (cTemp != null && cTemp.poll() == false) {
				cTemp = new CambridgeController();
			}
		}
	}

	public CambridgeController[] getC() {
		return controllers;
	}

//	public void setC(Controller[] controllers) {
//		this.controllers = controllers;
//	}

	public void setControllerEnv(ControllerEnvironment controllerEnv) {
		this.controllerEnv = controllerEnv;
	}
	
	public ControllerEnvironment getControllerEnv() {
		return this.controllerEnv;
	}

	public int screenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public int screenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public boolean fullscreen() {
		return fullscreen;
	}

	public void setFullscreen(boolean fullscreen) {
		this.fullscreen = fullscreen;
	}

	public int masterSound() {
		return masterSound;
	}

	public void setMasterSound(int masterSound) {
		this.masterSound = masterSound;
	}

	public int effectSound() {
		return effectSound;
	}

	public void setEffectSound(int effectSound) {
		this.effectSound = effectSound;
	}

	public int ambientSound() {
		return ambientSound;
	}

	public void setAmbientSound(int ambientSound) {
		this.ambientSound = ambientSound;
	}

	public SoundSystem mySoundSystem() {
		return mySoundSystem;
	}

	public void setMySoundSystem(SoundSystem mySoundSystem) {
		this.mySoundSystem = mySoundSystem;
	}

	public boolean playerIdDisplay() {
		return playerIdDisplay;
	}

	public void setPlayerIdDisplay(boolean playerIdDisplay) {
		this.playerIdDisplay = playerIdDisplay;
	}
	
	public Ini.Section displayConfig() {
		return displayConfig;
	}
	
	public void setDisplayConfig(Ini.Section displayConfig) {
		this.displayConfig = displayConfig;
	}
	
	public Ini.Section soundConfig() {
		return soundConfig;
	}
	
	public void setSoundConfig(Ini.Section soundConfig) {
		this.soundConfig = soundConfig;
	}
	
	public Ini.Section gameplayConfig() {
		return gameplayConfig;
	}
	
	public void setGameplayConfig(Ini.Section gameplayConfig) {
		this.gameplayConfig = gameplayConfig;
	}
	public Ini userConfig() {
		return userConfig;
	}
	public void setUserConfig(Ini userConfig) {
		this.userConfig = userConfig;
	}
	public Ini gameConfig() {
		return gameConfig;
	}
	public void setGameConfig(Ini gameConfig) {
		this.gameConfig = gameConfig;
	}
}
