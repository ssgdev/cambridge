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
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class MenuTeamSetupState extends BasicGameState implements KeyListener {
	private GlobalData data;
	private String[] menuOptions;
	private Ini ini;
	private int stateID;
	SoundSystem mySoundSystem;
	Ini.Section display, sound, gameplay;//Seems unused

	CambridgeController[] controllers;
	CambridgePlayerAnchor[] anchors;

	private boolean down, up, left, right, back, enter;
	private final float deadzone = 0.28f;
//	private int inputDelay;
//	private final int inputDelayConst = 200;

	boolean readiedUp;
	
	private boolean shouldRender;

	final static String RESDIR = "res/";
	private AngelCodeFont font, font_white, font_small;
	Image[] maps;
	
	int gameMode;
	float mapX, mapY, targetMapY, originalMapY, mapWidth, mapHeight;
	boolean exiting;
	//float targetMapWidth, targetMapHeight;
	
	//For drawing the players
	Polygon[] polys;
	float[] pAlphas;
	float[] pAlphaTargets;
	Color[] pColorTargets;
	float[][] pCoords;
	float[][] pCoordTargets;
	float[] pThetas;
	
	private Cambridge cambridge;
	private AppGameContainer appGc;
	
	float charSize;
	float tempf;

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

//		inputDelay = 0;

		font = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0.png"));
		font_white = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0_white.png"));
		font_small = new AngelCodeFont(data.RESDIR + "8bitoperator_small.fnt", new Image(data.RESDIR + "8bitoperator_small_0.png"));

		cambridge = (Cambridge) sbg;
		appGc = (AppGameContainer) gc;
		
		polys = new Polygon[8];
		initPlayerPolys();
		
		pAlphas = new float[4];
		pAlphaTargets = new float[4];
		pCoords = new float[4][2];
		pCoordTargets = new float[4][2];
		pColorTargets = new Color[4];
		pThetas = new float[4];
		
		reset();
	}

	public void setMaps(Image[] m){
		maps = m;
	}
	
	public void initPlayerPolys(){
		//Back
		tempf = mapHeight/50f*.8f;
		polys[0] = new Polygon(new float[]{
				tempf*2, tempf*2,
				tempf*2, -tempf*2,
				tempf, -tempf*2,
				tempf, -tempf*3,
				-tempf, -tempf*3,
				-tempf, -tempf*2,
				-tempf*2, -tempf*2,
				-tempf*2, tempf*2,
				-tempf, tempf*2,
				-tempf, tempf*3,
				tempf, tempf*3,
				tempf, tempf*2});
		//Dash
		tempf = mapHeight/10f*.9f*.8f;
		polys[1] = new Polygon(new float[]{-tempf*2/3,0,-tempf/3, -tempf/2, tempf*2/3, 0, -tempf/3, tempf/2});
		//Enforcer
		polys[2] = new Polygon();//not used
		//Neo
		tempf = mapHeight/10f/2f*.6f;
		polys[3] = new Polygon(new float[]{
				tempf, tempf,
				-tempf, -tempf,
				tempf, -tempf,
				-tempf, tempf,
				tempf, tempf,
				tempf, -tempf,
				-tempf, -tempf,
				-tempf, tempf});
		//Neutron
		polys[4] = new Polygon();//not used
		//Tricky
		tempf = mapHeight/10f/2f*.6f;
		polys[5] = 	new Polygon(new float[]{
				tempf, tempf,
				tempf, -tempf,
				-tempf, -tempf,
				-tempf, tempf});
		//Twin
		tempf = mapHeight/10f/7f;
		polys[6] = new Polygon(new float[]{
				tempf, tempf,
				tempf, -tempf,
				-tempf, -tempf,
				-tempf, tempf});
		//TwoTouch
		tempf = mapHeight/10f*.7f;
		polys[7] = new Polygon(new float[]{0,0,-tempf/3, -tempf/2, tempf*2/3, 0, -tempf/3, tempf/2});
	}
	
	public void setPlayerGraphics(){
		if(gameMode<=2){
			for (int i = 0; i < anchors.length; i++) {
				if (anchors[i].initiated()) {
					if(anchors[i].getTeam()==-1){
						pCoords[i][0] = data.screenWidth()/2;
						pCoords[i][1] = (data.screenHeight()/2f-mapHeight/2f)+mapHeight/5f + mapHeight/10f*2*i;
						pCoordTargets[i][0] = data.screenWidth()/2;
						pCoordTargets[i][1] = pCoords[i][1];
						pColorTargets[i] = Color.gray;
					}else if(anchors[i].getTeam()==0){
						pCoords[i][0] = data.screenWidth()/2 - (int)mapWidth/5;
						pCoords[i][1] = (data.screenHeight()/2f-mapHeight/2f)+mapHeight/5f + mapHeight/10f*2*i;
						pCoordTargets[i][0] = data.screenWidth()/2 - (int)mapWidth/5;
						pCoordTargets[i][1] = pCoords[i][1];
						pColorTargets[i] = Color.cyan;
					}else if(anchors[i].getTeam()==1){
						pCoords[i][0] = data.screenWidth()/2 + (int)mapWidth/5;
						pCoords[i][1] = (data.screenHeight()/2f-mapHeight/2f)+mapHeight/5f + mapHeight/10f*2*i;
						pCoordTargets[i][0] = data.screenWidth()/2 + (int)mapWidth/5;
						pCoordTargets[i][1] = pCoords[i][1];
						pColorTargets[i] = Color.orange;
					}
				}
			}
		}else{
			pCoordTargets[0][0] = data.screenWidth()/2 - mapHeight/12f;
			pCoordTargets[0][1] = data.screenHeight()/2 - mapHeight/13f;
			pCoordTargets[1][0] = data.screenWidth()/2 + mapHeight/12f;
			pCoordTargets[1][1] = data.screenHeight()/2 - mapHeight/13f;
			pCoordTargets[2][0] = data.screenWidth()/2 - mapHeight/12f;
			pCoordTargets[2][1] = data.screenHeight()/2 +mapHeight/13f;
			pCoordTargets[3][0] = data.screenWidth()/2 +mapHeight/12f;
			pCoordTargets[3][1] = data.screenHeight()/2 +mapHeight/13f;
			
			for(int i=0;i<4;i++)
				for(int j=0;j<2;j++)
					pCoords[i][j] = pCoordTargets[i][j];
		}
	}
	
	public void reset() {
		for (int i=0;i<anchors.length; i++) {
			if (anchors[i].initiated()) {
				anchors[i].setTeam(false);
				anchors[i].setTeam(-1);
				pAlphas[i] = 0f;
				pAlphaTargets[i] = 0f;
				pThetas[i] = 0;
			}
		}
		setPlayerGraphics();
		readiedUp = false;
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
				"Select Teams",
				data.screenWidth()/2 - font_white.getWidth("Select Teams")/2,
				data.screenHeight() * 1/12
				);
		
		g.drawImage(maps[gameMode].getScaledCopy((int)mapWidth, (int)mapHeight), mapX, mapY, Color.gray);
		
		//Draw the player icons
		for(int i=0; i<anchors.length; i++){
			if(anchors[i].initiated()){
				//Circle sizes (diameters)
				if(anchors[i].getCharacter()==4){//Neutron
					charSize = mapHeight/8f;
				}else if(anchors[i].getCharacter() == 6){//Twin
					charSize = mapHeight/6f;
				}else{
					charSize = mapHeight/7f;
				}
				
				g.setColor(new Color((int)((float)pColorTargets[i].getRed()*pAlphas[i]+255f*(1f-pAlphas[i])),
						(int)((float)pColorTargets[i].getGreen()*pAlphas[i]+255f*(1f-pAlphas[i])),
						(int)((float)pColorTargets[i].getBlue()*pAlphas[i]+255f*(1f-pAlphas[i]))));
				g.setLineWidth(2);
				drawPlayer(g, pCoords[i][0], pCoords[i][1], anchors[i].getCharacter(), i);
				g.setFont(font_small);
				if(gameMode<3){
					if(anchors[i].getTeam()==-1){
						//g.drawString("P"+(anchors[i].playerNum()+1), pCoords[i][0]-font_small.getWidth("P0")/2 ,pCoords[i][1]-charSize/2-font_small.getHeight("0")/2-8);
					}else if(anchors[i].getTeam()==0){
						g.drawString("P"+(anchors[i].playerNum()+1), pCoords[i][0]-font_small.getWidth("P0") - charSize/2 -10, pCoords[i][1]-font_small.getHeight("0")/2-5);
					}else{//==1
						g.drawString("P"+(anchors[i].playerNum()+1), pCoords[i][0] + charSize/2 +10, pCoords[i][1]-font_small.getHeight("0")/2-5);
					}
				}else{
					if(anchors[i].getTeam() != -1 && pCoords[i][0]==pCoordTargets[i][0] && pCoords[i][1]==pCoordTargets[i][1]){
						if(i==0 ||i == 2){
							g.drawString("P"+(anchors[i].playerNum()+1), pCoords[i][0]-font_small.getWidth("P0") - charSize/2 -10, pCoords[i][1]-font_small.getHeight("0")/2-5);
						}else if(i==1 || i == 3){
							g.drawString("P"+(anchors[i].playerNum()+1), pCoords[i][0] + charSize/2 +10, pCoords[i][1]-font_small.getHeight("0")/2-5);
						}
					}
				}
				g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), (int)(pAlphas[i]*255f)));
				g.setLineWidth(4);
				g.drawOval(pCoords[i][0]-charSize/2, pCoords[i][1]-charSize/2, charSize, charSize);
			}
		}
		
		if(readiedUp){
			g.setColor(Color.white);
			g.setFont(font_white);
			g.drawString("START GAME", data.screenWidth()/2-font.getWidth("START GAME")/2f, data.screenHeight()-font.getHeight("0")-30);
			g.setLineWidth(2);
			g.drawRect(-10, data.screenHeight()-font.getHeight("0")-28, data.screenWidth()+20, font.getLineHeight());
		}

	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		Input input = gc.getInput();

		if(!exiting)
			targetMapY = data.screenHeight()/2f-mapHeight/2f;
		
		mapY = approachTarget(mapY, targetMapY, delta);
		
		mapX = data.screenWidth()/2f-mapWidth/2f;
		
		//Set the position, color, and alpha of the player icons
		for (int i = 0; i < anchors.length; i++) {
			if (anchors[i].initiated()) {
				if(anchors[i].teamSelected()){
					pAlphaTargets[i] = 1f;
					pThetas[i]+=(float)delta/240f;
					if(pThetas[i]>=(float)Math.PI*2f)
						pThetas[i] -= (float)Math.PI*2f;
				}else{
					pAlphaTargets[i] = 0f;
				}
				
				if (anchors[i].getTeam() == -1) {
					//pCoordTargets[i][0] = data.screenWidth()/2;
					pColorTargets[i] = Color.gray;
				} else if (anchors[i].getTeam() == 0) {
					//pCoordTargets[i][0] = data.screenWidth()/2 - (int)mapWidth/5;
					pColorTargets[i] = Color.cyan;
				} else if(anchors[i].getTeam()==1){
					//pCoordTargets[i][0] = data.screenWidth()/2 + (int)mapWidth/5;
					pColorTargets[i] = Color.orange;
				}else if(anchors[i].getTeam()==2){
					//pCoordTargets[i][1] = data.screenHeight()/2 - (int)mapHeight/5;
					pColorTargets[i] = Color.magenta;
				}else if(anchors[i].getTeam()==3){
					//pCoordTargets[i][1] = data.screenHeight()/2 + (int)mapHeight/5;
					pColorTargets[i] = Color.green;
				}
//				g.drawString(anchors[i].playerNum()+"", x - font_white.getWidth(anchors[i].playerNum()+""), data.screenHeight() * 1/3 + i*50);
				
				pCoords[i][0] = approachTarget(pCoords[i][0], pCoordTargets[i][0], delta/2f);
				pCoords[i][1] = approachTarget(pCoords[i][1], pCoordTargets[i][1], delta/2f);
				pAlphas[i] = approachTarget(pAlphas[i], pAlphaTargets[i], delta/240f);			
			}
		}
		
		if (input.isKeyPressed(Input.KEY_ESCAPE)) {
			((MenuMainState)sbg.getState(data.MENUMAINSTATE)).setShouldRender(true);
			setShouldRender(false);
			sbg.enterState(data.MENUMAINSTATE);
		}

		int readyNum = 0;
		int existsNum = 0;
		int[] teamCount = new int[] {0, 0,0,0};
		for (int i = 0; i < anchors.length; i++) {
			if (anchors[i].teamSelected())
				readyNum++;
			if (anchors[i].initiated())
				existsNum++;
			for(int j=0;j<4;j++)
				if(anchors[i].getTeam() == j && anchors[i].teamSelected())
					teamCount[j]++;
			
//			if (anchors[i].getTeam() == 1 && anchors[i].teamSelected())
//				teamCount[1]++;
//			else if (anchors[i].getTeam() == 0 && anchors[i].teamSelected()) {
//				teamCount[0]++;
//			}
		}
		
		// Read input from activated anchors
		if (existsNum > 0) {
			for (int i = 0; i < anchors.length; i++) {
				if (anchors[i].initiated()) {
					if (!anchors[i].teamSelected()) {
						if (anchors[i].left(gc, delta)) {
							mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
							if(anchors[i].getTeam()==-1 || anchors[i].getTeam() == 1)
								 pCoordTargets[i][0] -= gameMode < 3 ? (int)mapWidth/5 : (int)mapHeight/2.2;
							anchors[i].changeTeam(-1);
						} else if (anchors[i].right(gc, delta)) {
							mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
							if(anchors[i].getTeam()==-1 || anchors[i].getTeam() == 0)
								 pCoordTargets[i][0] += gameMode < 3 ? (int)mapWidth/5 : (int)mapHeight/2.2;
							anchors[i].changeTeam(1);
						} else if(anchors[i].up(gc, delta)){
							if(gameMode>2){//CHANGE TO MATCH ACTUAL GAME MODE COUNT
								mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
								if(anchors[i].getTeam()==-1 || anchors[i].getTeam() == 3)
									 pCoordTargets[i][1] -= gameMode < 3 ? (int)mapWidth/5 : (int)mapHeight/3.1;
								anchors[i].changeTeam(-2);
							}
						} else if(anchors[i].down(gc, delta)){
							if(gameMode>2){//
								mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
								if(anchors[i].getTeam()==-1 || anchors[i].getTeam() == 2)
									 pCoordTargets[i][1] += gameMode < 3 ? (int)mapWidth/5 : (int)mapHeight/3.1;
								anchors[i].changeTeam(2);
							}
						}else if (anchors[i].back(gc, delta)) {
							mySoundSystem.quickPlay( true, "MenuBack.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
							targetMapY = originalMapY;
							exiting = true;
						} else if (anchors[i].select(gc, delta)) {
							if (anchors[i].getTeam() != -1) {
								if (teamCount[anchors[i].getTeam()] != existsNum-1) {
									anchors[i].setTeam(true);
									mySoundSystem.quickPlay( true, "MenuThud.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
								}else{
									mySoundSystem.quickPlay( true, "MenuFail.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
								}
							}else{
								mySoundSystem.quickPlay( true, "MenuFail.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
							}
							exiting = false;
						}
					} else {
						if (anchors[i].back(gc, delta)) {
							anchors[i].setTeam(false);
							mySoundSystem.quickPlay( true, "MenuBack.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
						}else if(anchors[i].select(gc, delta)){
							if (readyNum == existsNum && existsNum > 1) {
								mySoundSystem.quickPlay( true, "MenuThud.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
								((GameplayState)sbg.getState(data.GAMEPLAYSTATE)).setShouldRender(true);
								((GameplayState)sbg.getState(data.GAMEPLAYSTATE)).initFields(gc);
								setShouldRender(false);
								sbg.enterState(data.GAMEPLAYSTATE);
							}
						}
					}
				}
			}
		}
		
		if(exiting && mapY == originalMapY){
				setShouldRender(false);
			((MenuGameSetupState)sbg.getState(data.MENUGAMESETUPSTATE)).setShouldRender(true);
			sbg.enterState(data.MENUGAMESETUPSTATE);
		}
		
		//used for drawing the "Start Game" prompt
		if(readyNum == existsNum && existsNum > 1){
			readiedUp = true;
		}else{
			readiedUp = false;
		}
		
//		// Move onto next gamestate if all initiated players are ready
//		if (readyNum == existsNum && existsNum > 1) {
//			((GameplayState)sbg.getState(data.GAMEPLAYSTATE)).setShouldRender(true);
//			setShouldRender(false);
//			sbg.enterState(data.GAMEPLAYSTATE);
//		}

		input.clearKeyPressedRecord();
	}

	@Override
	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
		initPlayerPolys();
		reset();
	}

	@Override
	public void leave(GameContainer gc, StateBasedGame sbg) throws SlickException {

	}

	public void setShouldRender(boolean shouldRender) {
		this.shouldRender = shouldRender;
	}

	public void setStart(int imdex, float x, float y, float w, float h){
		gameMode = imdex;
		mapX = x;
		mapY = y;
		targetMapY = y;
		originalMapY = y;
		mapWidth = w;
		mapHeight = h;
		exiting = false;
	}
	
	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return stateID;
	}
	
	public void drawPlayer(Graphics g, float x, float y, int charNum, int anchorNum){
		switch(charNum){
		case 0://Back
			g.translate(x, y);
			g.draw(polys[0].transform(Transform.createRotateTransform((float)Math.PI/2f+pThetas[anchorNum])));
			g.translate(-x, -y);
			break;
		case 1://Dash
			g.translate(x,y);
			g.draw(polys[1].transform(Transform.createRotateTransform(pThetas[anchorNum])));
			g.translate(-x,-y);
			break;
		case 2://Enforcer
			tempf = mapHeight/10f/2*.7f;
			g.setLineWidth(8);
			g.rotate(x, y, pThetas[anchorNum]*360f/2f/(float)Math.PI);
			g.drawLine(x-tempf, y, x-tempf/2-3, y);
			g.drawLine(x+tempf/2+3, y, x+tempf, y);
			g.drawLine(x, y-tempf, x, y-tempf/2-3);
			g.drawLine(x, y+tempf/2+3, x, y+tempf);
			g.setLineWidth(2);
			g.drawRect(x-tempf/2, y-tempf/2, tempf, tempf);
			g.rotate(x, y, -pThetas[anchorNum]*360f/2f/(float)Math.PI);
			break;
		case 3://Neo
			g.translate(x,y);
			g.draw(polys[3].transform(Transform.createRotateTransform(pThetas[anchorNum])));
			g.translate(-x,-y);
			break;
		case 4://Neutron
			tempf = mapHeight/10f*.8f;
			g.rotate(x, y, pThetas[anchorNum]*360f/2f/(float)Math.PI);
			g.drawOval(x-tempf/2, y-tempf/2,  tempf,  tempf);
			g.setLineWidth(1f);
			for(int i=0; i<6; i++){
				g.drawLine(x+(float)Math.cos(i*Math.PI/3)*tempf/2, y+(float)Math.sin(i*Math.PI/3)*tempf/2,
						x+(float)Math.cos(i*Math.PI/3+Math.PI*.6f)*tempf/2, y+(float)Math.sin(i*Math.PI/3+Math.PI*.6f)*tempf/2);
			}
			g.rotate(x, y, -pThetas[anchorNum]*360f/2f/(float)Math.PI);
			break;
		case 5://Tricky
			g.setLineWidth(2);
			g.translate(x, y);
			g.draw(polys[5].transform(Transform.createRotateTransform(pThetas[anchorNum])));
			g.translate(-x, -y);
			break;
		case 6://Twin
			tempf = mapHeight/50f;
			g.rotate(x, y, pThetas[anchorNum]*90f/(float)Math.PI);
			g.translate(x-tempf,y-tempf);
			g.draw(polys[6].transform(Transform.createRotateTransform(pThetas[anchorNum])));
			g.translate(-x+tempf,-y+tempf);
			g.translate(x+tempf, y+tempf);
			g.draw(polys[6].transform(Transform.createRotateTransform(pThetas[anchorNum])));
			g.translate(-x-tempf, -y-tempf);
			g.rotate(x, y, -pThetas[anchorNum]*90f/(float)Math.PI);
			break;
		case 7://TwoTouch
			g.translate(x,y);
			g.draw(polys[7].transform(Transform.createRotateTransform(pThetas[anchorNum])));
			g.translate(-x,-y);
			break;
		case 8://Unused
			g.setFont(font_white);
			g.drawString("?", x-font.getWidth("?")/2, y-font.getHeight("?")/2-12f);
//			g.drawRect(x,y,font.getWidth("?"), font.getHeight("?"));
			break;
		default:
			break;
		}
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

}
