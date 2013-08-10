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
		mySoundSystem.loadSound("KickBump.ogg");
		mySoundSystem.loadSound("KickBumpSlow.ogg");
		mySoundSystem.loadSound("BallBounce.ogg");
		mySoundSystem.loadSound("BallBounceSlow.ogg");
		mySoundSystem.loadSound("GoalScored.ogg");
		mySoundSystem.loadSound("GoalOwnScored.ogg");
		mySoundSystem.loadSound("BallLaunch.ogg");
		mySoundSystem.loadSound("PowerKick.ogg");
		mySoundSystem.loadSound("Rumble.ogg");
		mySoundSystem.loadSound("PowerRecharged.ogg");
		mySoundSystem.loadSound("PowerRechargedSlow.ogg");
		mySoundSystem.loadSound("MenuThud.ogg");
		mySoundSystem.loadSound("NeoSlowOut.ogg");
		mySoundSystem.loadSound("NeoSlowIn.ogg");
		mySoundSystem.loadSound("TwoTouchActivate.ogg");
		mySoundSystem.loadSound("TwoTouchLockOn.ogg");
		mySoundSystem.loadSound("TwinsLtoR.ogg");
		mySoundSystem.loadSound("TwinsRtoL.ogg");
		mySoundSystem.loadSound("PufferPuffUp.ogg");
		mySoundSystem.loadSound("PufferPuffDown.ogg");
		mySoundSystem.loadSound("EnforcerBump.ogg");
		mySoundSystem.loadSound("EnforcerStep.ogg");
		mySoundSystem.loadSound("EnforcerActivate.ogg");
		mySoundSystem.loadSound("EnforcerTurn.ogg");
		mySoundSystem.loadSound("EnforcerWallBounce.ogg");
		mySoundSystem.loadSound("ChargeCharging.ogg");
		mySoundSystem.loadSound("ChargeDash.ogg");
		mySoundSystem.loadSound("ChargeGust.ogg");
		mySoundSystem.loadSound("ChargeWindingDown.ogg");
		mySoundSystem.loadSound("ChargeShortDash.ogg");
		mySoundSystem.loadSound("BackLock.ogg");
		mySoundSystem.loadSound("BackActivate.ogg");
		mySoundSystem.loadSound("NeutronPush.ogg");
		mySoundSystem.loadSound("NeutronPull.ogg");
		mySoundSystem.loadSound("NeutronCatch.ogg");
		mySoundSystem.loadSound("NeutronSwing.ogg");
		//TODO: Have streams created after character selection?
		mySoundSystem.newStreamingSource(false, "slow1", "Rumble.ogg", true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0f);
		mySoundSystem.newStreamingSource(false, "slow2", "Rumble.ogg", true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0f);
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
