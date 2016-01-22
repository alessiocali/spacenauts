package com.gff.spacenauts.ashley;

import com.badlogic.ashley.core.ComponentMapper;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.AngularVelocity;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.Boss;
import com.gff.spacenauts.ashley.components.Bullet;
import com.gff.spacenauts.ashley.components.CollisionDamage;
import com.gff.spacenauts.ashley.components.Death;
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
import com.gff.spacenauts.ashley.components.Render;
import com.gff.spacenauts.ashley.components.Steering;
import com.gff.spacenauts.ashley.components.Timers;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.ashley.components.WorldCamera;

/**
 * Container for all required {@link ComponentMapper}s
 * 
 * @author Alessio Cali'
 *
 */
public final class Mappers {
	
	public static final ComponentMapper<Steering> stm = ComponentMapper.getFor(Steering.class);
	public static final ComponentMapper<Position> pm = ComponentMapper.getFor(Position.class);
	public static final ComponentMapper<Velocity> vm = ComponentMapper.getFor(Velocity.class);
	public static final ComponentMapper<Angle> am = ComponentMapper.getFor(Angle.class);
	public static final ComponentMapper<AngularVelocity> avm = ComponentMapper.getFor(AngularVelocity.class);
	public static final ComponentMapper<Body> bm = ComponentMapper.getFor(Body.class);
	public static final ComponentMapper<WorldCamera> wcm = ComponentMapper.getFor(WorldCamera.class);
	public static final ComponentMapper<Hittable> hm = ComponentMapper.getFor(Hittable.class);
	public static final ComponentMapper<CollisionDamage> cdm = ComponentMapper.getFor(CollisionDamage.class);
	public static final ComponentMapper<Bullet> bum = ComponentMapper.getFor(Bullet.class);
	public static final ComponentMapper<Obstacle> om = ComponentMapper.getFor(Obstacle.class);
	public static final ComponentMapper<Friendly> fm = ComponentMapper.getFor(Friendly.class);
	public static final ComponentMapper<Enemy> em = ComponentMapper.getFor(Enemy.class);
	public static final ComponentMapper<Gun> gm = ComponentMapper.getFor(Gun.class);
	public static final ComponentMapper<Render> rm = ComponentMapper.getFor(Render.class);
	public static final ComponentMapper<FSMAI> aim = ComponentMapper.getFor(FSMAI.class);
	public static final ComponentMapper<DialogTrigger> dm = ComponentMapper.getFor(DialogTrigger.class);
	public static final ComponentMapper<Death> dem = ComponentMapper.getFor(Death.class);
	public static final ComponentMapper<Immunity> im = ComponentMapper.getFor(Immunity.class);
	public static final ComponentMapper<Player> plm = ComponentMapper.getFor(Player.class);
	public static final ComponentMapper<Boss> bom = ComponentMapper.getFor(Boss.class);
	public static final ComponentMapper<Timers> tm = ComponentMapper.getFor(Timers.class);
	
}
