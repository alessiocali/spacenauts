package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.screens.GameScreen;

/**
 * <p>
 * A rather convoluted, broad-to-narrow phase collision system. It puts every collidable objects inside a hash grid, then checks against collisions
 * for objects in the same cell. The broad phase mechanism is code borrowed from Beginning Android Games, Second Edition.
 * </p>
 * 
 * <p>
 * In short, the world is divided into square cells each of a fixed size. An array of lists is then instantiated, one for each cell. These lists
 * contain a reference to all entities that have part of their bodies within the respective cell. At each step, these lists are updated accordingly
 * (broad phase), then entities within the same cell are checked for collision (narrow phase).
 * </p>
 * 
 * <p>
 * To avoid time aliasing this should be wrapped inside a {@link PhysicsSystem}.
 * </p>
 * 
 * <h1>The collision window</h1>
 * <p>
 * To improve performance, the algorithm has been modified a bit. First, the hash grid only covers enough space to fit the camera's viewport, 
 * plus a tolerance amount; this space is called the collision window, which is a rectangle centered on the current camera position. 
 * The total hash grid dimension is given by:
 * </p>
 * 
 * <div>
 * <code>collisionWidth = TARGET_CAMERA_WIDTH * ( 1 + TOLERANCE_RATIO )</code><br>
 * <code>collisionHeight = TARGET_CAMERA_HEIGHT * ( 1 + TOLERANCE_RATIO )</code>
 * </div>
 * 
 * <p>
 * For the algorithm to work the entities must have positive coordinates not higher than (collisionWidth, collisionHeight).
 * Because of that, we must translate all entities to a new space whose origin is the lower left corner of the collision window.
 * If the camera has world coordinates (xC, yC), the new origin will have world coordinates (xC - collisionWidth / 2, yC - collisionHeight / 2).
 * Given this offset we can find the entities' coordinates inside the collision space by calculating pos' = pos - offset.
 * All entities outside the collision window will automatically be ignored, ensuring the computation is limited to nearby entities.
 * </p>
 *  
 * @author Alessio Cali'
 * @see <b>Mario Zechner</b> and <b>Robert Green</b>, <a href="http://www.apress.com/9781430246770"><i>Beginning Android Games, Second Edition</i></a> (Apress, 2012), 391-398.
 *
 */
public class CollisionSystem extends EntitySystem {
	
	private static final float REGULAR_CELL_SIZE = 10f;
	private static final float BOSS_CELL_SIZE = 50f;
	
	private static final float TOLERANCE_RATIO = 0f;

	private float cellSize = REGULAR_CELL_SIZE;
	private int cellsNumber;
	private int cellsPerCol;
	private int cellsPerRow;
	
	//private float levelWidth, levelHeight;
	private float collisionWidth, collisionHeight;
	private Vector2 offset;
	private boolean dirty = false;

	private Array<Entity>[] collidableCells;
	private Array<Entity> potentialColliders;
	private Array<Entity> collidersList;
	private int[] cellIdsBuffer = new int[4];

	/**
	 * Inits the spatial grid based on the TOLERANCE_RATIO and the {@link #cellSize}.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public CollisionSystem(){
		collisionWidth = Globals.TARGET_CAMERA_WIDTH * (1 + TOLERANCE_RATIO);
		collisionHeight = Globals.TARGET_CAMERA_HEIGHT * (1 + TOLERANCE_RATIO);
		cellsPerRow = (int)Math.ceil(collisionWidth / cellSize);
		cellsPerCol = (int)Math.ceil(collisionHeight / cellSize);
		cellsNumber = cellsPerRow * cellsPerCol;
		offset = new Vector2();

		collidableCells = new Array[cellsNumber];
		collidersList = new Array<Entity>();
		potentialColliders = new Array<Entity>(10);

		for (int i = 0 ; i < cellsNumber ; i++)
			collidableCells[i] = new Array<Entity>(10);
	}

	/**
	 * First, the cells and the colliders list are updated. Then each entity within collidersList is tested against its 
	 * potential colliders. Collisions only happen point-to-body ( {@link Rectangle#contains(float, float)} ) or
	 * body-to-body ( {@link Rectangle#overlaps(Rectangle)} ). When collision happens {@link #performCollision(Entity, Entity)}
	 * is invoked.
	 * 
	 * @see com.badlogic.ashley.core.EntitySystem#update(float)
	 */
	@Override
	public void update(float delta){
		if (GameScreen.getEngine().getBoss() != null && cellSize != BOSS_CELL_SIZE) 
			resizeCells(BOSS_CELL_SIZE);
		
		if (dirty) resizeCellsInternal();
		
		refreshCells();

		for (Entity entity : collidersList){
			Body entityBody = Mappers.bm.get(entity);

			potentialColliders = getPotentialColliders(entity, null);

			// WITH BOUNDING RECTANGLES
			for (Entity collider : potentialColliders){
				Body colliderBody = Mappers.bm.get(collider);
				
				if (entityBody == null) {
					//Point-Body
					Vector2 entityPos = Mappers.pm.get(entity).value;
					
					if (colliderBody.polygon.getBoundingRectangle().contains(entityPos.x, entityPos.y))
						performCollision(entity, collider);
					
				} else if (colliderBody == null) {
					//Body-Point
					Vector2 colliderPos = Mappers.pm.get(collider).value;
					
					if (entityBody.polygon.getBoundingRectangle().contains(colliderPos.x, colliderPos.y))
						performCollision(entity, collider);
					
				} else if (entityBody.polygon.getBoundingRectangle().overlaps(colliderBody.polygon.getBoundingRectangle())) {
					performCollision(entity, collider);
				}
				
				/*
				 * INTERSECTOR VARIANT
				 * 
				if (entityBody == null) {
					//Point-Body
					Vector2 entityPos = Mappers.pm.get(entity).value;
					
					if (colliderBody.polygon.contains(entityPos.x, entityPos.y))
						performCollision(entity, collider);
					
				} else if (colliderBody == null) {
					//Body-Point
					Vector2 colliderPos = Mappers.pm.get(collider).value;
					
					if (entityBody.polygon.contains(colliderPos.x, colliderPos.y))
						performCollision(entity, collider);
					
				} else if (Intersector.overlapConvexPolygons(entityBody.polygon, colliderBody.polygon))
					//Body-Body
					performCollision(entity, collider);
				}
				*/	
			}
		}
	}
	
	/**
	 * Adds collider to the entity's colliders list. HitListeners
	 * will later be called from a {@link HitSystem}.
	 * 
	 * @param entity
	 * @param collider
	 */
	private void performCollision(Entity entity, Entity collider) {
		Hittable entityHittable = Mappers.hm.get(entity);
		
		if (!entityHittable.colliders.contains(collider, true))
			entityHittable.colliders.add(collider);
	}

	private void clearCells(Array<Entity>[] cells){
		for (int i = 0 ; i < cellsNumber ; i++)
			cells[i].clear();
	}

	/**
	 * Inserts an entity in its cells, given a cell array. Remember that each cell is represented by an {@link Array} list containing
	 * all entities inside it. 
	 * 
	 * @param entity the entity to insert
	 * @param cells the cells array
	 */
	private void insertEntityInCells(Entity entity, Array<Entity>[] cells){
		int[] cellIds = getCellIds(entity);

		if (cellIds == null)
			return;

		int i = 0;
		int cellId = -1;

		while (i < 3 && (cellId = cellIds[i++]) != -1)
			cells[cellId].add(entity);
	}

	/**
	 * Return the IDs of the cells an entity is contained in within the hash grid. 
	 * This assumes no object is large enough to be included in more than 4 cells.
	 * 
	 * @param entity
	 * @return The cells' IDs.
	 */
	private int[] getCellIds(Entity entity){
		Body body = Mappers.bm.get(entity);

		//Point entity, checks against position.
		if (body == null) {
			Vector2 pos = Mappers.pm.get(entity).value;
			
			//Positions are offset by the origin of the collision area. 
			//Only viewable entities will be processed, plus a small margin.
			
			int x = (int)Math.floor((pos.x - offset.x)  / cellSize);
			int y = (int)Math.floor((pos.y - offset.y) / cellSize);

			if(x >= 0 && x < cellsPerRow && y >= 0 && y < cellsPerCol)
				cellIdsBuffer[0] = x + y * cellsPerRow;
			else
				cellIdsBuffer[0] = -1;

			cellIdsBuffer[1] = -1;
			cellIdsBuffer[2] = -1;
			cellIdsBuffer[3] = -1;
		} 
		
		else {	
			//Body entity, checks against the bounding rectangle.
			Rectangle bodyBounds = body.polygon.getBoundingRectangle();
			
			int x1 = (int)Math.floor((bodyBounds.getX() - offset.x) / cellSize);
			int y1 = (int)Math.floor((bodyBounds.getY() - offset.y) / cellSize);
			int x2 = (int)Math.floor((bodyBounds.getX() - offset.x + bodyBounds.width) / cellSize);
			int y2 = (int)Math.floor((bodyBounds.getY() - offset.y + bodyBounds.height) / cellSize);
			
			if(x1 == x2 && y1 == y2) {
				if(x1 >= 0 && x1 < cellsPerRow && y1 >= 0 && y1 < cellsPerCol)
					cellIdsBuffer[0] = x1 + y1 * cellsPerRow;
				else
					cellIdsBuffer[0] = -1;

				cellIdsBuffer[1] = -1;
				cellIdsBuffer[2] = -1;
				cellIdsBuffer[3] = -1;

			} else if (x1 == x2) {
				int i = 0;

				if(x1 >= 0 && x1 < cellsPerRow) {
					if(y1 >= 0 && y1 < cellsPerCol)
						cellIdsBuffer[i++] = x1 + y1 * cellsPerRow;
					if(y2 >= 0 && y2 < cellsPerCol)
						cellIdsBuffer[i++] = x1 + y2 * cellsPerRow;
				}

				while(i <= 3) cellIdsBuffer[i++] = -1;

			} else if(y1 == y2) {
				int i = 0;

				if(y1 >= 0 && y1 < cellsPerCol) {
					if(x1 >= 0 && x1 < cellsPerRow)
						cellIdsBuffer[i++] = x1 + y1 * cellsPerRow;
					if(x2 >= 0 && x2 < cellsPerRow)
						cellIdsBuffer[i++] = x2 + y1 * cellsPerRow;
				}

				while(i <= 3) cellIdsBuffer[i++] = -1;

			} else {
				int i = 0;
				int y1CellsPerRow = y1 * cellsPerRow;
				int y2CellsPerRow = y2 * cellsPerRow;

				if(x1 >= 0 && x1 < cellsPerRow && y1 >= 0 && y1 < cellsPerCol)
					cellIdsBuffer[i++] = x1 + y1CellsPerRow;
				if(x2 >= 0 && x2 < cellsPerRow && y1 >= 0 && y1 < cellsPerCol)
					cellIdsBuffer[i++] = x2 + y1CellsPerRow;
				if(x2 >= 0 && x2 < cellsPerRow && y2 >= 0 && y2 < cellsPerCol)
					cellIdsBuffer[i++] = x2 + y2CellsPerRow;
				if(x1 >= 0 && x1 < cellsPerRow && y2 >= 0 && y2 < cellsPerCol)
					cellIdsBuffer[i++] = x1 + y2CellsPerRow;

				while(i <= 3) cellIdsBuffer[i++] = -1;
			}
		}

		return cellIdsBuffer;
	}

	/**
	 * Returns all potential colliders for an entity, by checking all the cell lists whose IDs match the result of {@link #getCellIds(Entity)}. 
	 * 
	 * @param entity
	 * @param filter the Family all colliders must match. Null for any.
	 * @return
	 */
	public Array<Entity> getPotentialColliders(Entity entity, Family filter){
		potentialColliders.clear();
		int[] cellIds = getCellIds(entity);

		if (cellIds == null)
			return null;

		boolean hasBody = Mappers.bm.get(entity) != null;
		int i = 0;
		int cellId = -1;

		while(i <= 3 && (cellId = cellIds[i++]) != -1){
			int len = collidableCells[cellId].size;
			
			for (int j = 0 ; j < len ; j++){
				Entity collider = collidableCells[cellId].get(j);
				
				//If there's a filter, skip unmatching entities
				if (filter != null) 
					if (!filter.matches(collider)) continue;
				
				boolean hasColliderBody = Mappers.bm.get(collider) != null;
				
				//Collision happens only if one of two entities has a Body.
				if (!potentialColliders.contains(collider, true) && collider != entity && (hasColliderBody || hasBody))	
					potentialColliders.add(collider);
			}
		}

		return potentialColliders;		
	}

	/**
	 * Clears both the list of colliders and the lists representing the hash grid's cells, then updates both.
	 * 
	 */
	private void refreshCells(){
		clearCells(collidableCells);
		collidersList.clear();
		offset.set(GameScreen.getEngine().getCameraPosition());
		offset.add(-collisionWidth / 2, -collisionHeight / 2);

		for (Entity collidable : GameScreen.getEngine().getEntitiesFor(Families.COLLIDERS_FAMILY)){
			insertEntityInCells(collidable, collidableCells);
			collidersList.add(collidable);
		}
	}
	
	public void resizeCells(float cellSize) {
		this.cellSize = cellSize;
		dirty = true;
	}
	
	@SuppressWarnings("unchecked")
	private void resizeCellsInternal() {
		cellsPerRow = (int)Math.ceil(collisionWidth / cellSize);
		cellsPerCol = (int)Math.ceil(collisionHeight / cellSize);
		cellsNumber = cellsPerRow * cellsPerCol;

		collidableCells = new Array[cellsNumber];
		collidersList = new Array<Entity>();
		potentialColliders = new Array<Entity>(10);

		for (int i = 0 ; i < cellsNumber ; i++)
			collidableCells[i] = new Array<Entity>(10);
		
		dirty = false;
	}
}
