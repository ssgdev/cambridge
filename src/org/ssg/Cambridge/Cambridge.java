package org.ssg.Cambridge;

import java.io.File;
import java.io.IOException;

import net.java.games.input.*;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.openal.SoundStore;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class Cambridge extends StateBasedGame {

	final static String RESDIR = "res/";
	public static final int GAMEPLAYSTATE = 10;
	SoundSystem mySoundSystem;
	Controller c1, c2; //gamepad controllers
	
	public Cambridge() throws SlickException{
		super("Kick Kickerung");
		
        try {
        	SoundSystemConfig.addLibrary( LibraryLWJGLOpenAL.class );
			SoundSystemConfig.setCodec( "wav", CodecWav.class );
			SoundSystemConfig.setCodec( "ogg", CodecJOrbis.class );
			SoundSystemConfig.setSoundFilesPackage( "org/ssg/Cambridge/Sounds/" );
			SoundSystemConfig.setStreamQueueFormatsMatch( true );
		} catch (SoundSystemException e) {
			e.printStackTrace();
		} 
        
        mySoundSystem = new SoundSystem();
        
        c1 = null;
        c2 = null;
        
        for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
        	if (c.getType() == Controller.Type.GAMEPAD) {
        		if (c1 == null) {
        			c1 = c;
        			System.out.println("First Controller Found: " + c1.getName());
        		} else if (c2 == null) {
        			c2 = c;
        			System.out.println("Second Controller Found: " + c2.getName());
        		} //end if-else block
        	} //end if statement
        } //end for each loop
        
		this.addState(new GameplayState(GAMEPLAYSTATE, true, 0));
		((GameplayState) this.getState(GAMEPLAYSTATE)).setSoundSystem(mySoundSystem);
		this.enterState(GAMEPLAYSTATE);
	}
	
	public void initStatesList(GameContainer gc) throws SlickException {
		
		SoundStore.get().setMaxSources(16);
		initSounds();
		
       AppGameContainer appContainer = (AppGameContainer) gc;
		
        if (!appContainer.isFullscreen()) {
            String[] icons = {"res/icon16.png", "res/icon32.png"};
            appContainer.setIcons(icons);
        }
		
        ((GameplayState) this.getState(GAMEPLAYSTATE)).getControllers(c1, c2);
		this.getState(GAMEPLAYSTATE).init(gc, this);
	}
	
	public void initSounds() throws SlickException{
		mySoundSystem.loadSound("bump.wav");
		mySoundSystem.loadSound("bumpslow.wav");
		mySoundSystem.loadSound("bump2.wav");
		mySoundSystem.loadSound("bump2slow.wav");
		mySoundSystem.loadSound("ding.wav");
		mySoundSystem.loadSound("pow2.wav");
		mySoundSystem.loadSound("pneng.wav");
		mySoundSystem.loadSound("rumble.wav");
		mySoundSystem.loadSound("bwuw.wav");
		mySoundSystem.loadSound("ping.wav");
		mySoundSystem.loadSound("pingslow.wav");
		mySoundSystem.loadSound("thud.wav");
		mySoundSystem.loadSound("whoosh2.wav");
		mySoundSystem.loadSound("whoosh2r.wav");
		mySoundSystem.loadSound("TwoTouchActivate.wav");
		mySoundSystem.newStreamingSource(false, "slow1", "rumble.wav", true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0f);
		mySoundSystem.newStreamingSource(false, "slow2", "rumble.wav", true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0f);
	}
	
	public static void main(String[] args) throws SlickException{

		int screenWidth = 1000;
		int screenHeight = 600;
		int fullscreen = 0;
		
		try {
			Ini ini = new Ini(new File(RESDIR + "config.cfg"));
			Ini.Section section = ini.get("CONF");
			screenWidth = section.get("SCREENWIDTH", int.class);
			screenHeight = section.get("SCREENHEIGHT", int.class);
			fullscreen = section.get("FULLSCREEN", int.class);
		}catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
			
		AppGameContainer app = new AppGameContainer(new Cambridge());
		app.setDisplayMode(screenWidth, screenHeight, false);
		app.setVSync(true);
		app.setTargetFrameRate(60);
		app.setAlwaysRender(true);
		app.setShowFPS(true);
		app.setTitle("Kick Kickerung");
//		app.setSmoothDeltas(true);
//		if (app.supportsMultiSample())
//			app.setMultiSample(2);
		app.setFullscreen((fullscreen==1));
		app.setMaximumLogicUpdateInterval(24);
		app.setMinimumLogicUpdateInterval(24);
		app.start();
	}
	
}
