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

	float w;//The minimum horizontal segment length
	float h;//The min vertical segment length
	Polygon[] polys;//Polygons for drawing player icons
	float[][] corners;//coordinates of top left coordinates
	
	//Fields for drawing info in boxes
	//String[] names = {"No. 82", "Kitamura Yahei", "Ramdel Garth", "Lucas Taniels", "Anomaly XIII", "Aldobrandi", "Liem & Riem", "Franco Fortes", "Random"};
	//String[] nickname = {"Spirit of Team", "Lightspeed Unbreakable", "Stampede", "Mercury LP", "Anomalous", "Deceptive Man", "Brothers", "Alacritous", "Unchosen"};
	//String[] statNames = {"OFF", "DEF", "SPC", "BAL"};
//	int[][] stats = {{96,94,68,85,80,96,80,86,0},//Offense
//					 {74,52,99,85,80,60,80,70,0},//Defense
//					 {50,78,99,85,78,90,90,64,0},//Space Control
//					 {99,99,52,70,88,70,70,99,0}};//Ball Control
	//Polygon[] statPolys;//For drawing the graph of stats for each player
	//String[] power1 = {"R1 (Hold) : Grab and Go", "R1 (Hold) : Lightning God Flash", "R1 (Hold) : Miura Charge", "R1 : Hummingbird Waltz", "R1 : Gravitation Hill", "R1 (Hold, Aim) : Blue Shins Shuffle", "Passive : Fraternal Bond", "R1 (Hold, Aim) : Magnus Destiny", ""};
	//String[] power2 = {"", "R1 (Tap) : Lesser Deity Flash", "R1 (During Charge) : Toreador Pivot", "", "L1 (Hold) : Gravity Well", "L1 (Hold) : Glib Define", "R1/L1 : Team Up", "", ""};
	
	final static String RESDIR = "res/";
	private AngelCodeFont font, font_white, font_small;
	Image controls_key_arrows, controls_key_no2ndpow, controls_key_tfgh, controls_pad_base, controls_pad_lb, controls_pad_rstick;
	Image[] portraits;
	
	private Cambridge cambridge;
	private AppGameContainer appGc;
	
	float tempf;

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

		controls_key_arrows = new Image(data.RESDIR+"controls_key_arrows.png");
		controls_key_no2ndpow = new Image(data.RESDIR+"controls_key_no2ndpow.png");
		controls_key_tfgh = new Image(data.RESDIR+ "controls_key_tfgh.png");
		controls_pad_base = new Image(data.RESDIR+"controls_pad_base.png");
		controls_pad_lb = new Image(data.RESDIR+"controls_pad_lb.png");
		controls_pad_rstick = new Image(data.RESDIR+"controls_pad_rstick.png");
		
		portraits = new Image[9];
		portraits[0] = new Image(data.RESDIR+"p_back.png");
		portraits[1] = new Image(data.RESDIR+"p_dash.png");
		portraits[2] = new Image(data.RESDIR+"p_enforcer.png");
		portraits[3] = new Image(data.RESDIR+"p_neo.png");
		portraits[4] = new Image(data.RESDIR+"p_neutron.png");
		portraits[5] = new Image(data.RESDIR+"p_tricky.png");
		portraits[6] = new Image(data.RESDIR+"p_twins.png");
		portraits[7] = new Image(data.RESDIR+"p_twotouch.png");
		portraits[8] = new Image(data.RESDIR+"p_random.png");
		
		cambridge = (Cambridge) sbg;
		appGc = (AppGameContainer) gc;
		
		setWindow();
		
		initPlayerPolys();
	}

	public void initPlayerPolys(){
		polys = new Polygon[8];
		
		//Back
		tempf = h/5f*.8f;
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
		tempf = h*.9f*.8f;
		polys[1] = new Polygon(new float[]{-tempf*2/3,0,-tempf/3, -tempf/2, tempf*2/3, 0, -tempf/3, tempf/2});
		//Enforcer
		polys[2] = new Polygon();//not used
		//Neo
		tempf = h/2f*.6f;
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
		tempf = h/2f*.6f;
		polys[5] = 	new Polygon(new float[]{
				tempf, tempf,
				tempf, -tempf,
				-tempf, -tempf,
				-tempf, tempf});
		//Twin
		tempf = h/7f;
		polys[6] = new Polygon(new float[]{
				tempf, tempf,
				tempf, -tempf,
				-tempf, -tempf,
				-tempf, tempf});
		//TwoTouch
		tempf = h*.7f;
		polys[7] = new Polygon(new float[]{0,0,-tempf/3, -tempf/2, tempf*2/3, 0, -tempf/3, tempf/2});
	}
	
	public void setWindow() {
		w = (float)data.screenWidth()/11f;
		h = (float)data.screenHeight()/9f;
		
		corners = new float[4][];
		corners[0] = new float[] {w, h};
		corners[1] = new float[] {6*w, h};
		corners[2] = new float[] {w, 5*h};
		corners[3] = new float[] {6*w, 5*h};
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
		g.setColor(new Color(120,120,120));
		hLine(g, 4, 4, 3);
		hLine(g, 4, 5, 3);
		vLine(g, 5, 3, 3);
		vLine(g, 6, 3, 3);
		g.setLineWidth(5);
		g.setColor(Color.white);
		g.drawRect(4*w, 3*h, 3*w, 3*h);
		
		//Drawing Player Boxes
		//P1

		hLine(g, 1, 1, 4);
		vLine(g, 5, 1, 2);
		hLine(g, 1, 4, 3);
		vLine(g, 1, 1, 3);
		
		//P2
		hLine(g, 6, 1, 4);
		vLine(g, 6, 1, 2);
		hLine(g, 7, 4, 3);
		vLine(g, 10, 1, 3);
		
		//P3
		hLine(g, 7, 5, 3);
		vLine(g, 10, 5, 3);
		hLine(g, 6, 8, 4);
		vLine(g, 6, 6, 2);
		
		//P4
		hLine(g, 1, 5, 3);
		vLine(g, 5, 6, 2);
		hLine(g, 1, 8, 4);
		vLine(g, 1, 5, 3);
		
		g.setLineWidth(2);
//		//Shade the tooth of the player boxes
//		g.setColor(Color.gray);
//		//p1
//		g.fillRect(4*w, 1*h, w, 2*h);
//		//p2
//		g.fillRect(6*w, 1*h, w, 2*h);
//		//p3
//		g.fillRect(6*w, 6*h, w, 2*h);
//		//p4
//		g.fillRect(4*w, 6*h, w, 2*h);
		
//		g.setFont(font_white);
//		g.drawString("P1", data.screenWidth() * 3/8 - font_white.getWidth("P1")/2, data.screenHeight()*1/12);
//		g.drawString("P2", data.screenWidth() * 5/8 - font_white.getWidth("P2")/2, data.screenHeight()*1/12);
//		g.drawString("P3", data.screenWidth() * 3/8 - font_white.getWidth("P3")/2, data.screenHeight()*11/12 - font_white.getLineHeight());
//		g.drawString("P4", data.screenWidth() * 5/8 - font_white.getWidth("P4")/2, data.screenHeight()*11/12 - font_white.getLineHeight());

		//Draw player icons
		drawPlayer(g, 4.5f*w, 3.5f*h, 0);
		drawPlayer(g, 5.5f*w, 3.5f*h, 1);
		drawPlayer(g, 6.5f*w, 3.5f*h, 2);
		drawPlayer(g, 4.5f*w, 4.5f*h, 3);
		drawPlayer(g, 5.5f*w, 4.5f*h, 4);
		drawPlayer(g, 6.5f*w, 4.5f*h, 5);
		drawPlayer(g, 4.5f*w, 5.5f*h, 6);
		drawPlayer(g, 5.5f*w, 5.5f*h, 7);
		drawPlayer(g, 6.5f*w, 5.5f*h, 8);
		
		//Draw player boxes
		for(int i=0;i<anchors.length; i++){
			if (anchors[i].initiated()) {
				//Draw "P1" etc
				g.setFont(font_white);
				g.setColor(Color.white);
				g.drawString("P"+(anchors[i].playerNum()+1), corners[i][0]+(anchors[i].playerNum()%2==0? 3.5f*w:.5f*w)-font.getWidth("P2")/2f, corners[i][1]+(anchors[i].playerNum()>1?h:0)+h/2-font.getHeight("P")/2-10);
				//Draw controller icon
				if(anchors[i].getKeyboard()==-1){
					g.drawImage(controls_pad_base.getScaledCopy((int)w, (int)h), corners[i][0]+(anchors[i].playerNum()%2==0? 3f*w:0f*w), corners[i][1]+h+(anchors[i].playerNum()>1?h:0)-5);
					if(anchors[i].getCharacter() >=5 && anchors[i].getCharacter() <= 7 )//fill in the right stick
						g.drawImage(controls_pad_rstick.getScaledCopy((int)w+1, (int)h+1), corners[i][0]+(anchors[i].playerNum()%2==0? 3f*w:0f*w), corners[i][1]+h+(anchors[i].playerNum()>1?h:0)-5);
					if(anchors[i].getCharacter() >=4 && anchors[i].getCharacter() <= 6)
						g.drawImage(controls_pad_lb.getScaledCopy((int)w, (int)h), corners[i][0]+(anchors[i].playerNum()%2==0? 3f*w:0f*w), corners[i][1]+h+(anchors[i].playerNum()>1?h:0)-5);
				}else if(anchors[i].getKeyboard()==1){
					g.drawImage(controls_key_tfgh.getScaledCopy((int)w, (int)h), corners[i][0]+(anchors[i].playerNum()%2==0? 3f*w:0f*w), corners[i][1]+h+(anchors[i].playerNum()>1?h:0)-5);
					if(!(anchors[i].getCharacter() >=4 && anchors[i].getCharacter() <= 6))
						g.drawImage(controls_key_no2ndpow.getScaledCopy((int)w, (int)h), corners[i][0]+(anchors[i].playerNum()%2==0? 3f*w:0f*w), corners[i][1]+h+(anchors[i].playerNum()>1?h:0)-5);
				}else if(anchors[i].getKeyboard()==0){
					g.drawImage(controls_key_arrows.getScaledCopy((int)w, (int)h), corners[i][0]+(anchors[i].playerNum()%2==0? 3f*w:0f*w), corners[i][1]+h+(anchors[i].playerNum()>1?h:0)-5);
					if(!(anchors[i].getCharacter() >=4 && anchors[i].getCharacter() <= 6))
						g.drawImage(controls_key_no2ndpow.getScaledCopy((int)w, (int)h), corners[i][0]+(anchors[i].playerNum()%2==0? 3f*w:0f*w), corners[i][1]+h+(anchors[i].playerNum()>1?h:0)-5);
				}
				g.drawImage(portraits[anchors[i].getCharacter()].getScaledCopy((int)(3f*w), (int)(3f*h)),corners[i][0]+(anchors[i].playerNum()%2==0?0:w), corners[i][1]);
				if (anchors[i].characterSelected()) {
					g.setFont(font);
					g.setColor(Color.white);
					g.fillRect(corners[i][0], corners[i][1]+h, 4*w, h);
					g.drawString("READY", corners[i][0]+2*w-font.getWidth("READY")/2, corners[i][1]+h+(h-font.getHeight("0"))/2f-8);
				}
			}else{
				g.setFont(font_white);
				g.setColor(Color.white);
				g.drawString("Player "+(i+1), corners[i][0]+2*w-font.getWidth("Player 1")/2, corners[i][1]+h+(h-font.getHeight("0"))/2f-8);
			}
		}

		//Draw the cursors
		for (CambridgePlayerAnchor a : anchors) {
			if (a.initiated()) {
				g.setLineWidth(4);
				g.drawRect(
						4*w + (a.getCharacter() % 3)*w+2,
						3*h + (a.getCharacter() / 3)*h+2,
						w-4,
						h-4
						);
				g.fillRect(
						4*w + (a.getCharacter() % 3)*w + ((a.playerNum()+1)%2==0? w*.75f:0),
						3*h + (a.getCharacter() / 3)*h + (a.playerNum()>1 ? h*.75f:0),
						w*.25f,
						h*.25f
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
		for (int i = 0; i < anchors.length; i++) {
			if (!anchors[i].initiated()) {
				if (!keyboardOneTaken && input.isKeyPressed(Input.KEY_PERIOD)) {
					anchors[i] = new CambridgePlayerAnchor(0, i, -1, 0, new CambridgeController());
					keyboardOneTaken = true;
				} else if (!keyboardTwoTaken && input.isKeyPressed(Input.KEY_2)) {
					anchors[i] = new CambridgePlayerAnchor(0, i, -1, 1, new CambridgeController());
					keyboardTwoTaken = true;
				} else {
					for (CambridgeController c : controllers) {
						boolean used = false;
						if (c.exists() && c.poll()) {
							for (CambridgePlayerAnchor a : anchors) {
								if (a.controller() == c) {
									used = true;
								}
							}
							if (!used) {
								if (c.getMenuSelect()) {
									anchors[i] = new CambridgePlayerAnchor(0, i, -1, -1, c);
								}
								if (c.getMenuBack()) {
									setShouldRender(false);
									((MenuMainState)sbg.getState(data.MENUMAINSTATE)).setShouldRender(true);
									sbg.enterState(data.MENUMAINSTATE);
								}
							}
						}
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
		for (CambridgePlayerAnchor a : anchors) {
			if (a.initiated() && a.characterSelected()) {
				a.setCharacter(false);
			}
		}
		setWindow();
		initPlayerPolys();
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
	
	public void hLine(Graphics g, float x, float y, float l){
		g.drawLine(x*w, y*h, (x+l)*w, y*h);		
	}
	
	public void vLine(Graphics g, float x, float y, float l){
		g.drawLine(x*w, y*h, x*w, (y+l)*h);
	}
	
	public void drawPlayer(Graphics g, float x, float y, int player){
		switch(player){
		case 0://Back
			g.translate(x, y);
			g.draw(polys[0].transform(Transform.createRotateTransform((float)Math.PI/2f)));
			g.translate(-x, -y);
			break;
		case 1://Dash
			g.translate(x,y);
			g.draw(polys[1]);
			g.translate(-x,-y);
			break;
		case 2://Enforcer
			tempf = h/2*.7f;
			g.setLineWidth(15);
			g.drawLine(x-tempf, y, x+tempf, y);
			g.drawLine(x, y-tempf, x, y+tempf);
			g.setColor(Color.black);
			g.fillRect(x-tempf/2, y-tempf/2, tempf, tempf);
			g.setColor(Color.white);
			g.setLineWidth(2);
			g.drawRect(x-tempf/2, y-tempf/2, tempf, tempf);
			break;
		case 3://Neo
			g.translate(x,y);
			g.draw(polys[3]);
			g.translate(-x,-y);
			break;
		case 4://Neutron
			tempf = h*.8f;
			g.drawOval(x-tempf/2, y-tempf/2,  tempf,  tempf);
			g.setLineWidth(1f);
			for(int i=0; i<6; i++){
				g.drawLine(x+(float)Math.cos(i*Math.PI/3)*tempf/2, y+(float)Math.sin(i*Math.PI/3)*tempf/2,
						x+(float)Math.cos(i*Math.PI/3+Math.PI*.6f)*tempf/2, y+(float)Math.sin(i*Math.PI/3+Math.PI*.6f)*tempf/2);
			}
			break;
		case 5://Tricky
			tempf = 5;
			g.setLineWidth(3);
			g.setColor(Color.gray);
			g.translate(x+tempf, y+tempf);
			g.draw(polys[5]);
			g.translate(-x-tempf, -y-tempf);
			g.setColor(Color.white);
			g.translate(x-tempf,y-tempf);
			g.draw(polys[5]);
			g.translate(-x+tempf,-y+tempf);
			g.setLineWidth(2);
			break;
		case 6://Twin
			tempf = 15;
			g.translate(x-tempf,y-tempf);
			g.draw(polys[6]);
			g.translate(-x+tempf,-y+tempf);
			g.translate(x+tempf, y+tempf);
			g.draw(polys[6]);
			g.translate(-x-tempf, -y-tempf);
			break;
		case 7://TwoTouch
			g.translate(x,y);
			g.draw(polys[7]);
			g.translate(-x,-y);
			break;
		case 8://Random
			g.setFont(font_white);
			g.drawString("?", x-font.getWidth("?")/2, y-font.getHeight("?")/2-12f);
//			g.drawRect(x,y,font.getWidth("?"), font.getHeight("?"));
			break;
		default:
			break;
		}
	}

}
