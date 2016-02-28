package com.gff.spacenauts.ashley;

import com.badlogic.ashley.core.Family;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.AngularVelocity;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.Boss;
import com.gff.spacenauts.ashley.components.CoopPlayer;
import com.gff.spacenauts.ashley.components.DialogTrigger;
import com.gff.spacenauts.ashley.components.Enemy;
import com.gff.spacenauts.ashley.components.FSMAI;
import com.gff.spacenauts.ashley.components.Friendly;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Immunity;
import com.gff.spacenauts.ashley.components.Obstacle;
import com.gff.spacenauts.ashley.components.Player;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Removable;
import com.gff.spacenauts.ashley.components.Render;
import com.gff.spacenauts.ashley.components.Steering;
import com.gff.spacenauts.ashley.components.Timers;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.ashley.components.WorldCamera;


/**
 * Container for all required families.
 * 
 * @author Alessio Cali'
 *
 */
@SuppressWarnings("unchecked")
public final class Families {
	
	public static final Family FRIENDLY_FAMILY = Family.all(Friendly.class).get();
	public static final Family ENEMY_FAMILY = Family.all(Enemy.class).get();
	public static final Family OBSTACLE_FAMILY = Family.all(Obstacle.class).get();
	public static final Family PLAYER_FAMILY = Family.all(Player.class).get();
	public static final Family COOP_FAMILY = Family.all(CoopPlayer.class).get();
	public static final Family CAMERA_FAMILY = Family.all(WorldCamera.class).get();
	public static final Family STEERABLE_FAMILY = Family.all(Position.class, Velocity.class, Angle.class, AngularVelocity.class).get();
	public static final Family AI_FAMILY = Family.all(FSMAI.class).get();
	public static final Family COLLIDERS_FAMILY = Family.all(Hittable.class).one(Body.class, Position.class).get();
	public static final Family DIALOG_FAMILY = Family.all(DialogTrigger.class).get();
	public static final Family IMMUNE_FAMILY = Family.all(Immunity.class).get();
	public static final Family MOVEMENT_FAMILY = Family.all(Position.class, Velocity.class, Angle.class, AngularVelocity.class).get();
	public static final Family REMOVABLE_FAMILY = Family.all(Position.class, Removable.class).get();
	public static final Family RENDERING_FAMILY = Family.one(Body.class, Render.class).get();
	public static final Family SPRITE_FAMILY = Family.all(Render.class, Position.class, Angle.class).get();
	public static final Family BODY_FAMILY = Family.all(Body.class).get();
	public static final Family SHOOTER_FAMILY = Family.all(Gun.class, Position.class, Angle.class).one(Friendly.class, Enemy.class).get();
	public static final Family STEERING_FAMILY = Family.all(Steering.class, Velocity.class, Angle.class, AngularVelocity.class).get();
	public static final Family BOSS_FAMILY = Family.all(Boss.class).get();
	public static final Family TIMER_FAMILY = Family.all(Timers.class).get();
	
}
