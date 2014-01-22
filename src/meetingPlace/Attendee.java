package meetingPlace;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Attendee {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private boolean moved;
	private Attendee chosen;
	private boolean hasBeenChosen;
	private final int name;
	
	public Attendee(ContinuousSpace<Object> space, Grid<Object> grid, int name) {
		this.space = space;
		this.grid = grid;
		this.name = name;
		hasBeenChosen = false;
	}
	
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		GridCellNgh<Attendee> nghCreator = new GridCellNgh<Attendee>(
				grid, myLocation(), Attendee.class, 1, 1);
		List<GridCell<Attendee>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		//moveTowards(directionTowardsChosenOne(gridCells));
		assert chosen != null;
		assert space != null;
		NdPoint nx = space.getLocation(chosen);
		moveTowards(nx);
		//infect();
	}
	
	public void moveTowards(NdPoint nx) {
		//if(!pt.equals(myLocation())) {
			NdPoint myPoint  = space.getLocation(this);
			NdPoint otherPoint = nx;
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this, 1, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());
			moved = true;
		//}
	}
	
//	public void infect() {
//		List<Object> humans = new ArrayList<Object>();
//		for(Object obj : grid.getObjectsAt(myLocation().getX(), myLocation().getY())) {
//			if(obj instanceof Human) {
//				humans.add(obj);
//			}
//		}
//		
//		if(humans.size() > 0) {
//			int index = RandomHelper.nextIntFromTo(0, humans.size()-1);
//			Object obj = humans.get(index);
//			NdPoint spacePt = space.getLocation(obj);
//			Context<Object> context = ContextUtils.getContext(obj);
//			context.remove(obj);
//			Zombie zombie = new Zombie(space, grid);
//			context.add(zombie);
//			space.moveTo(zombie, spacePt.getX(), spacePt.getY());
//			grid.moveTo(zombie, myLocation().getX(), myLocation().getY());
//			
//			Network<Object> net = (Network<Object>)context.getProjection("meetingNetwork");
//			net.addEdge(this, zombie);
//		}
//	}
	
	private GridPoint myLocation() {
		return grid.getLocation(this);
	}
	
	private GridPoint directionTowardsChosenOne(List<GridCell<Attendee>> gridCells) {
		GridPoint pt = null;
		int maxCount = -1;
		for(GridCell<Attendee> cell : gridCells) {
			if(cell.size() > maxCount) {
				pt = cell.getPoint();
				maxCount = cell.size();
			}
		}
		return pt;
	}
	
//	private GridPoint pointWithMostHumans(List<GridCell<Human>> gridCells) {
//		GridPoint pt = null;
//		int maxCount = -1;
//		for(GridCell<Human> cell : gridCells) {
//			if(cell.size() > maxCount) {
//				pt = cell.getPoint();
//				maxCount = cell.size();
//			}
//		}
//		return pt;
//	}


	public Attendee getChosen() {
		return chosen;
	}


	public void setChosen(Attendee chosen) {
		this.chosen = chosen;
	}


	public int getName() {
		return name;
	}


	public boolean hasBeenChosen() {
		return hasBeenChosen;
	}


	public void setHasBeenChosen(boolean hasBeenChosen) {
		this.hasBeenChosen = hasBeenChosen;
	}
}
