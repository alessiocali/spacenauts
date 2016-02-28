package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.gff.spacenauts.Logger;
import com.gff.spacenauts.Logger.LogLevel;
import com.gff.spacenauts.Spacenauts;
import com.gff.spacenauts.ai.PowerUpAI;
import com.gff.spacenauts.ai.PowerUpAI.PowerUpState;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.Death;
import com.gff.spacenauts.ashley.components.Immunity;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.net.NetworkAdapter;
import com.gff.spacenauts.net.NetworkAdapter.AdapterState;
import com.gff.spacenauts.screens.GameScreen;

/**
 * Updates and reads messages coming from a {@link NetworkAdapter}, then
 * parses them to evaluate the state of the Coop player. In case of lost
 * connection this system is removed and the NetworkAdapter reset.
 * 
 * @author Alessio
 *
 */
public class MultiplayerSystem extends EntitySystem {

	private static final int MAX_HANDLED_MSG = 50;
	private NetworkAdapter na;
	private Entity friendPlayer;
	private boolean friendDied = false;
	private boolean friendDc = false;	//Friend disconnected

	private float avgX, avgY, avgA;
	private int n;

	public MultiplayerSystem() {
		if (Spacenauts.getNetworkAdapter() == null) 
			throw new GdxRuntimeException(new IllegalStateException("Can't start multiplayer system without a network adapter"));

		na = Spacenauts.getNetworkAdapter();
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		friendPlayer = GameScreen.getBuilder().buildCoopPlayer(0, 0);
		engine.addEntity(friendPlayer);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntity(friendPlayer);
	}

	@Override
	public void update(float delta) {
		na.updateState(delta);

		if (na.getState() != AdapterState.GAME) {
			//Network adapter is not in GAME state, it must have lost connection.
			Logger.log(LogLevel.ERROR, this.toString(), "Lost connection with coop player.");

			if (na.getState() == AdapterState.FAILURE) 
				Logger.log(LogLevel.ERROR, this.toString(),"Reason was: " + na.getFailureReason());

			disconnect();
		} 

		else {
			avgX = avgY = avgA = 0;
			n = 0; 
			int i;

			//Read and parse all messages
			for (i = 0 ; i < MAX_HANDLED_MSG ; i++) {
				String msg = na.receive();

				if (msg == null) break;

				else parse(msg);

				if (friendDied) {
					Death d = Mappers.dem.get(friendPlayer);

					if (d != null) 
						d.listeners.onDeath(friendPlayer);

					disconnect();
					return;
				} else if (friendDc) {
					Logger.log(LogLevel.UPDATE, this.toString(), "Coop player closed connection");			
					disconnect();
					return;
				}
			}

			//Sets friend position to an average of all data received.
			//It is meant to smooth movement in case of lag.
			if (n != 0) {
				avgX /= n;
				avgY /= n;
				avgA /= n;

				Position pos = Mappers.pm.get(friendPlayer);
				Angle ang = Mappers.am.get(friendPlayer);
				Body body = Mappers.bm.get(friendPlayer);

				pos.value.set(avgX, avgY);
				ang.value = avgA;
				body.polygon.setPosition(pos.value.x, pos.value.y);
				body.polygon.setRotation(ang.getAngleDegrees());
			}

			if (i == MAX_HANDLED_MSG)
				Logger.log(LogLevel.WARNING, this.toString(), "Handled max number of messages, queue might be crowded");
		}
	}

	private void parse(String msg) {
		String[] cmds = msg.split("\\s");

		if (cmds.length == 0) {
			Logger.log(LogLevel.ERROR, "Network parser", "Error parsing message");
			return;
		}

		String cmd0 = cmds[0];

		if (cmd0.equals("PLAYER_POS")) {

			if (cmds.length < 4) {
				Logger.log(LogLevel.ERROR, "Network parser", "No position data");
				return;
			}

			try {
				avgX += Float.parseFloat(cmds[1]);
				avgY += Float.parseFloat(cmds[2]);
				avgA+= Float.parseFloat(cmds[3]);
				n++;				
			} catch (NumberFormatException e) {
				Logger.log(LogLevel.ERROR, "Network parser", "Invalid position data");
				return;
			}
		} 

		else if (cmd0.equals("PLAYER_SHOT"))
			for (GunData data : Mappers.gm.get(friendPlayer).guns) data.triggered = true;

		else if (cmd0.equals("PLAYER_HIT"))
			friendPlayer.add(GameScreen.getEngine().createComponent(Immunity.class));

		else if (cmd0.equals("PLAYER_DEAD"))
			friendDied = true;

		else if (cmd0.equals("PLAYER_PWUP")) {
			if (cmds.length < 2) {
				Logger.log(LogLevel.ERROR, "Network parser", "No powerup data");
				return;
			}

			PowerUpState powerUp = PowerUpAI.PowerUpState.getById(cmds[1]);

			if (powerUp == null) {
				Logger.log(LogLevel.ERROR, "Network parser", "Invalid powerup data");
				return;
			}

			Mappers.aim.get(friendPlayer).fsm.changeState(powerUp);
		} 

		else if (cmd0.equals("CLOSE"))
			friendDc = true;

		else 
			return;
	}

	private void disconnect() {
		GameScreen.getEngine().removeSystem(this);
		GameScreen.getEngine().removeEntity(friendPlayer);
		na.reset();
	}
}
