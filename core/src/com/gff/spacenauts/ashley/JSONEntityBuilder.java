package com.gff.spacenauts.ashley;

import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Logger;
import com.gff.spacenauts.Logger.LogLevel;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.listeners.DeathListener;
import com.gff.spacenauts.listeners.HitListener;
import com.gff.spacenauts.listeners.ShotListener;
import com.gff.spacenauts.listeners.death.ActivatePowerUp;
import com.gff.spacenauts.listeners.death.DropPowerUp;
import com.gff.spacenauts.listeners.death.EmitSound;
import com.gff.spacenauts.listeners.death.ReleaseAnimation;
import com.gff.spacenauts.listeners.hit.DamageAndDie;
import com.gff.spacenauts.listeners.hit.Die;
import com.gff.spacenauts.listeners.hit.PushAway;

public class JSONEntityBuilder {
	
	private static final String TAG = "JsonEntityBuilder";
	
	//Temporary storage
	private Array<HitListener> hitListeners;
	private Array<DeathListener> deathListeners;
	private Array<ShotListener> shotListeners;
	private Array<Family> families;
	
	//Json
	private JsonValue root;
	private JsonValue subcomponents;
	private JsonValue components;
	private JsonValue prototypes;
	private JsonValue entities;
	
	//Caches
	private ObjectMap<String, Object> subcomCache;
	private ObjectMap<String, Sprite> spriteCache;
	private ObjectMap<String, Animation> animationCache;
	private ObjectMap<String, Sound> soundCache;
	
	//Other stuff
	private PooledEngine engine;
	private AssetManager assets;
	private TextureAtlas spriteAtlas;
	
	public JSONEntityBuilder(PooledEngine engine, AssetManager assets) {
		this.engine = engine;
		this.assets = assets;
		this.spriteAtlas = assets.get(AssetsPaths.ATLAS_TEXTURES, TextureAtlas.class);
		root = new JsonReader().parse(Gdx.files.internal(AssetsPaths.DATA_ENTITIES_JSON));
		subcomponents = root.getChild("subcomponents");
		components = root.getChild("components");
		prototypes = root.getChild("prototypes");
		entities = root.getChild("entities");
		
		hitListeners = new Array<HitListener>();
		families = new Array<Family>();
		
		subcomCache = new ObjectMap<String, Object>();
		spriteCache = new ObjectMap<String, Sprite>();
		animationCache = new ObjectMap<String, Animation>();
		soundCache = new ObjectMap<String, Sound>();
	}
	
	private Sprite getSprite(String spriteName) {
		if (spriteCache.containsKey(spriteName)) return spriteCache.get(spriteName);
		//else
		Sprite sprite = spriteAtlas.createSprite(spriteName);
		spriteCache.put(spriteName, sprite);
		return sprite;
	}
	
	private Animation getAnimation(int width, int height, float delta, String animationName) {
		if (animationCache.containsKey(animationName)) return animationCache.get(animationName);
		//else
		Sprite s = getSprite(animationName);
		Animation a = new Animation(delta, s.split(width, height)[0]);
		animationCache.put(animationName, a);
		return a;
	}
	
	private Sound getSound(String soundPath) {
		if (soundCache.containsKey(soundPath)) return soundCache.get(soundPath);
		//else
		Sound sound = assets.get(soundPath, Sound.class);
		soundCache.put(soundPath, sound);
		return sound;
	}
	
	private float getAngleRadians(String value) {
		String degOrRad = value.substring(value.length() - 1);
		
		if (degOrRad.equals("d")) {
			return Float.valueOf(value.substring(0, value.length() - 2)) / 180 * MathUtils.PI ;
		} else if (degOrRad.equals("r")) {
			return Float.valueOf(value.substring(0, value.length() - 2));
		} else {
			return Float.valueOf(value) / 180 * MathUtils.PI;	//Assuming degree value without ending character
		}
	}
	
	private Array<Family> getFilters(JsonValue filters) {
		families.clear();
		String filterValue;
		
		for (JsonValue filter : filters) {
			filterValue = filter.asString();
			if (filterValue == null) continue;
			else {
				try {
					families.add((Family)ClassReflection.getField(Families.class, filterValue).get(null));
				} catch (ReflectionException rfe) {
					Logger.log(LogLevel.ERROR, TAG, "No family named: " + filterValue);
					continue;
				}
			}
		}
		
		return families;
	}

	public Object buildSubComponent (String id) {
		
		if (subcomCache.containsKey(id)) return subcomCache.get(id);
		//else
		JsonValue subcJson = null;
		
		for (JsonValue child : subcomponents) {
			
			if (child.getString("id").equals(id)) {
				subcJson = child;
				break;
			}
		}
		
		if (subcJson == null) {
			Logger.log(LogLevel.WARNING, TAG, "No subcomponent with id: " + id);
			return null;
		}
		
		Object retVal = null;
		String type = subcJson.getString("type", "");
		JsonValue content = subcJson.getChild("content");
		
		if (content == null) {
			Logger.log(LogLevel.ERROR, TAG, "No content for this subcomponent.");
			return null;
		}
		
		if (type.equals("gdata")) {
			retVal = parseGdata(content);
		} else if (type.equals("hlisteners")) {
			retVal = parseHitListeners(content.getChild("hlisteners"));
		} else if (type.equals("dlisteners")) {
			retVal = parseDeathListeners(content.getChild("dlisteners"));
		}
		
		if (retVal != null) {
			subcomCache.put(id, retVal);
			return retVal;
		} else {
			Logger.log(LogLevel.WARNING, TAG, "Unknown subcomponent type: " + type);
			return null;
		}
	}
	
	private GunData parseGdata(JsonValue json) {
		GunData data = Pools.get(GunData.class).obtain();
		
		if (json == null) return data;
		
		JsonValue poffset;
		String sound;
		
		data.bulletDamage = json.getFloat("damage", data.bulletDamage);
		data.bulletHitListeners.addAll(parseHitListeners(json.getChild("hlisteners")));
		data.bulletDeathListeners.addAll(parseDeathListeners(json.getChild("dlisteners")));
		data.gunShotListeners.addAll(parseShotListeners(json.getChild("shotlisteners")));
		data.bulletImage = getSprite(json.getString("sprite", null));
		data.speed = json.getFloat("speed", data.speed);
		if ((poffset = json.getChild("poffset")) != null) {
			data.pOffset.x = poffset.getFloat("x", data.pOffset.x);
			data.pOffset.y = poffset.getFloat("y", data.pOffset.y);
		}
		data.aOffset = getAngleRadians(json.getString("aoffset", "0d"));
		if ((sound = json.getString("sound")) != null) {
			try {
				data.shotSound = (String) ClassReflection.getField(AssetsPaths.class, sound).get(null);			
			} catch (ReflectionException rfe) {
				Logger.log(LogLevel.ERROR, TAG, "Reflection error with sonund: " + sound);
				data.shotSound = "";
				rfe.printStackTrace();
			}
		}
		data.scale = json.getFloat("scale", data.scale);
		
		return data;
	}
	
	private Array<HitListener> parseHitListeners(JsonValue json) {
		hitListeners.clear();
		
		if (json == null) return hitListeners;
		
		String parent = json.getString("parent");
		String type;
		JsonValue params;
		
		if (parent != null) buildSubComponent(parent);	
		//No need to do more. If scan for parent is successful, all HitListeners 
		//of the parent will be added to the hitListeners Array.
		//Beware of loops!
		
		for (JsonValue listener : json) {
			type = listener.getString("type", "");
			params = listener.getChild("params");
			
			if (type.equals("die")) {
				if (params == null) continue;
				JsonValue filters = params.getChild("filters");
				if (filters == null) continue;
				else hitListeners.add(new Die(getFilters(filters).items));
			} else if (type.equals("push")) {
				if (params == null) continue;
				JsonValue filters = params.getChild("filters");
				if (filters == null) continue;
				else hitListeners.add(new PushAway(getFilters(filters).items));
			} else if (type.equals("dnd")) {
				if (params == null) continue;
				JsonValue filters = params.getChild("filters");
				if (filters == null) continue;
				else hitListeners.add(new DamageAndDie(getFilters(filters).items));
			} else {
				Logger.log(LogLevel.ERROR, TAG, "Unknown HitListener: " + type);
				continue;
			}
		}
		return hitListeners;
	}
	
	private Array<DeathListener> parseDeathListeners(JsonValue json) {
		deathListeners.clear();
		
		if (json == null) return deathListeners;
		
		String parent = json.getString("parent");
		String type;
		JsonValue params;
		
		if (parent != null) buildSubComponent(parent);
		
		for (JsonValue listener : json) {
			type = listener.getString("type", "");
			params = listener.getChild("params");
			
			if (type.equals("apu")) {
				if (params == null) continue;
				JsonValue powerup = params.getChild("powerup");
				if (powerup == null) continue;
				else deathListeners.add(new ActivatePowerUp(powerup.asString()));
			} else if (type.equals("dpu")) {
				if (params == null) continue;
				JsonValue powerup = params.getChild("powerup");
				if (powerup == null) continue;
				else deathListeners.add(new DropPowerUp(powerup.asString()));
			} else if (type.equals("death_sound")) {
				if (params == null) continue;
				JsonValue sound = params.getChild("sound");
				if (sound == null) continue;
				else {
					try {
						String soundPath = (String) ClassReflection.getField(AssetsPaths.class, sound.asString()).get(null);
						deathListeners.add(new EmitSound(getSound(soundPath)));
					} catch (ReflectionException rfe) {
						Logger.log(LogLevel.WARNING, TAG, "No sound path matching: " + sound.asString());
					}
				}
			} else if (type.equals("death_animation")) {
				if (params == null) continue;
				Animation anim = getAnimation(params.getChild("width").asInt(), params.getChild("height").asInt(),
											  params.getChild("delta").asFloat(), params.getChild("animation").asString());
				deathListeners.add(new ReleaseAnimation(anim, engine));						
			} else {
				Logger.log(LogLevel.ERROR, TAG, "Unknown death listener: " + type);
				continue;
			}
		}
		return deathListeners;
	}
	
	private Array<ShotListener> parseShotListeners(JsonValue json) {
		//TODO stuff!
		return null;
	}
}