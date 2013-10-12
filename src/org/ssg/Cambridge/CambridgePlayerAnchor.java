package org.ssg.Cambridge;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;

public class CambridgePlayerAnchor {
	private static final int CHARACTERS = 9;
	private final float deadzone = 0.28f;
	private int inputDelay;
	private final int inputDelayConst = 1000;
	private int character;
	private boolean characterSelected;
	private int playerNum;
	private int playerTeam;
	private boolean teamSelected;
	private int keyboard;
	private boolean ready;
	private CambridgeController controller;
	
	private boolean selectFlag, backFlag, leftFlag, rightFlag, upFlag, downFlag;
	
	public CambridgePlayerAnchor() {
		character = -1;
		playerNum = -1;
		playerTeam = -1;
		keyboard = -1;
		controller = new CambridgeController();
		commonFields();
	}
	
	public CambridgePlayerAnchor(int n) {
		character = -1;
		playerNum = n;
		playerTeam = -1;
		keyboard = -1;
		controller = new CambridgeController();
		commonFields();
	}
	
	public CambridgePlayerAnchor(int c, int num, int team, int k, CambridgeController contr) {
		character = c;
		playerNum = num;
		playerTeam = team;
		keyboard = k;
		controller = contr;
		commonFields();
	}
	
	private void commonFields() {
		ready = false;
		characterSelected = false;
		teamSelected = false;
		inputDelay = inputDelayConst;
		resetButtonInput();
	}
	
	public void resetButtonInput() {
//		selectFlag = true;
//		backFlag = true;
		leftFlag = true;
		rightFlag = true;
		upFlag = true;
		downFlag = true;
	}
	
	public void changeCharacter(int n) {
		character += n;
		if (character < 0) {
			character = CHARACTERS + character;
		} else if (character >= CHARACTERS) {
			character %= CHARACTERS;
		}
	}
	
	public int getCharacter() {
		return character;
	}
	
	public int getTeam() {
		return playerTeam;
	}
	
	public void changeTeam(int n) {
		if(n == 1 || n == -1){
			if (n < 0) {
				if (playerTeam == -1) {
					playerTeam = 0;
				} else if (playerTeam == 1) {
					playerTeam = -1;
				}
			} else {
				if (playerTeam == -1) {
					playerTeam = 1;
				} else if (playerTeam == 0) {
					playerTeam = -1;
				}
			}
		}else if(n==2 || n==-2){
			if (n < 0) {
				if (playerTeam == -1) {
					playerTeam = 2;
				} else if (playerTeam == 3) {
					playerTeam = -1;
				}
			} else {
				if (playerTeam == -1) {
					playerTeam = 3;
				} else if (playerTeam == 2) {
					playerTeam = -1;
				}
			}
		}
	}
	
	public void setTeam(int n){
		playerTeam = n;
	}
	
	public boolean initiated() {
		return (controller.exists() || keyboard != -1);
	}
	
	public int playerNum() {
		return playerNum;
	}
	
	public void ready() {
		ready = true;
	}
	
	public void unready() {
		ready = false;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public void setCharacter(boolean b) {
		characterSelected = b;
	}
	
	public void setTeam(boolean b) {
		teamSelected = b;
	}
	
	public boolean characterSelected() {
		return characterSelected;
	}
	
	public boolean teamSelected() {
		return teamSelected;
	}
	
	public int getKeyboard() {
		return keyboard;
	}
	
	public CambridgeController controller() {
		return controller;
	}
	
	public boolean left(GameContainer gc, int delta) {
		if (controller.exists() && controller.poll()) {
			if (leftFlag) {
				if (controller.getDPad() != 1.0) {
					leftFlag = false;
				}
			} else {
				if (controller.getDPad() == 1.0) {
					leftFlag = true;
					return true;
				}
			}
			if (inputDelay <= 0) {
				if (controller.getLeftStickX() < -deadzone) {
					inputDelay = inputDelayConst;
					return true;
				} else {
					return false;
				}
			} else {
				inputDelay -= delta;
				return false;
			}
		} else {
			Input input = gc.getInput();
			if (keyboard == 0) {
				return input.isKeyPressed(Input.KEY_LEFT);
			} else {
				return input.isKeyPressed(Input.KEY_F);
			}
		}
	}
	
	public boolean right(GameContainer gc, int delta) {
		if (controller.exists() && controller.poll()) {
			if (rightFlag) {
				if (controller.getDPad() != 0.5f) {
					rightFlag = false;
				}
			} else {
				if (controller.getDPad() == 0.5f) {
					rightFlag = true;
					return true;
				}
			}
			if (inputDelay <= 0) {
				if (controller.getLeftStickX() > deadzone) {
					inputDelay = inputDelayConst;
					return true;
				} else {
					return false;
				}
			} else {
				inputDelay -= delta;
				return false;
			}
		} else {
			Input input = gc.getInput();
			if (keyboard == 0) {
				return input.isKeyPressed(Input.KEY_RIGHT);
			} else {
				return input.isKeyPressed(Input.KEY_H);
			}
		}
	}
	
	public boolean up(GameContainer gc, int delta) {
		if (controller.exists() && controller.poll()) {
			if (upFlag) {
				if (controller.getDPad() != 0.25f) {
					upFlag = false;
				}
			} else {
				if (controller.getDPad() == 0.25f) {
					upFlag = true;
					return true;
				}
			}
			if (inputDelay <= 0) {
				if (controller.getLeftStickY() < -deadzone) {
					inputDelay = inputDelayConst;
					return true;
				} else {
					return false;
				}
			} else {
				inputDelay -= delta;
				return false;
			}
		} else {
			Input input = gc.getInput();
			if (keyboard == 0) {
				return input.isKeyPressed(Input.KEY_UP);
			} else {
				return input.isKeyPressed(Input.KEY_T);
			}
		}
	}
	
	public boolean down(GameContainer gc, int delta) {
		if (controller.exists() && controller.poll()) {
			if (downFlag) {
				if (controller.getDPad() != 0.75f) {
					downFlag = false;
				}
			} else {
				if (controller.getDPad() == 0.75f) {
					downFlag = true;
					return true;
				}
			}
			if (inputDelay <= 0) {
				if (controller.getLeftStickY() > deadzone) {
					inputDelay = inputDelayConst;
					return true;
				} else {
					return false;
				}
			} else {
				inputDelay -= delta;
				return false;
			}
		} else {
			Input input = gc.getInput();
			if (keyboard == 0) {
				return input.isKeyPressed(Input.KEY_DOWN);
			} else {
				return input.isKeyPressed(Input.KEY_G);
			}
		}
	}
	
	public boolean select(GameContainer gc, int delta) {
		if (controller.exists() && controller.poll()) {
//			if (selectFlag) {
//				if (!controller.getMenuSelect()) {
//					selectFlag = false;
//				}
//				return false;
//			} else {
//				if (controller.getMenuSelect()) {
//					selectFlag = true;
//					return true;
//				} else {
//					return false;
//				}
//			}
			return controller.getMenuSelect();
		} else {
			Input input = gc.getInput();
			if (keyboard == 0) {
				return input.isKeyPressed(Input.KEY_PERIOD) || input.isKeyPressed(Input.KEY_ENTER);
			} else {
				return input.isKeyPressed(Input.KEY_2) || input.isKeyPressed(Input.KEY_ENTER);
			}
		}
	}
	
	public boolean back(GameContainer gc, int delta) {
		if (controller.exists() && controller.poll()) {
//			if (backFlag) {
//				if (!controller.getMenuBack()) {
//					backFlag = false;
//				}
//				return false;
//			} else {
//				if (controller.getMenuBack()) {
//					backFlag = true;
//					return true;
//				} else {
//					return false;
//				}
//			}
			return controller.getMenuBack();
		} else {
			Input input = gc.getInput();
			if (keyboard == 0) {
				return input.isKeyPressed(Input.KEY_COMMA) || input.isKeyPressed(Input.KEY_ESCAPE);
			} else {
				return input.isKeyPressed(Input.KEY_1) || input.isKeyPressed(Input.KEY_ESCAPE);
			}
		}
	}
	
	public boolean start(GameContainer gc, int delta){
		if(controller.exists() && controller.poll()) {
			return controller.getStart();
		}else{
			Input input = gc.getInput();
			return input.isKeyPressed(Input.KEY_ESCAPE);
		}
	}

}
