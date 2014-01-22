package meetingPlace;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.annotate.AgentAnnot;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
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

@AgentAnnot(displayName = "Attendee")
public class Attendee {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private boolean gridOnly;

	private Attendee chosen;
	private boolean chosenBySomebody;
	private final int name;

	public Attendee(ContinuousSpace<Object> space, Grid<Object> grid, int name) {
		this.space = space;
		this.grid = grid;
		this.name = name;
		chosenBySomebody = false;
		if (space == null)
			gridOnly = true;
	}

	public Attendee(Grid<Object> grid, int name) {
		this(null, grid, name);
	}
	
	@ScheduledMethod(start = 1, interval = 1000, priority = 5)
	public void step() {
		GridCellNgh<Attendee> nghCreator = new GridCellNgh<Attendee>(grid,
				myLocation(), Attendee.class, 1, 1);
		List<GridCell<Attendee>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());

		assert chosen != null;
		assert space != null;
		if (gridOnly) {
			GridPoint nx = grid.getLocation(chosen);
			moveTowards(nx);
			drawEdges();
		} else {
			NdPoint nx = space.getLocation(chosen);
			moveTowards(nx);
			drawEdges();
		}
	}

	private void drawEdges() {
		Context<Object> context = ContextUtils.getContext(this);
		Network<Object> net = (Network<Object>) context
				.getProjection("meetingNetwork");
		net.addEdge(this, chosen);
	}

	public void moveTowards(NdPoint nx) {
		NdPoint myPoint = space.getLocation(this);
		if (!enoughDistanceToReach(myPoint, nx)) {
			NdPoint otherPoint = nx;
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint,
					otherPoint);
			space.moveByVector(this, 1, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			EnvironmentEquilibrium.setActivity(true);
		}
	}

	public void moveTowards(GridPoint nx) {
		GridPoint myPoint = grid.getLocation(this);
		if (!enoughDistanceToReach(myPoint, nx)) {
			GridPoint otherPoint = nx;
			double angle = calcAngleFor2DMovement(grid, myPoint, otherPoint);
			grid.moveByVector(this, 1, angle, 0);
			myPoint = grid.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			EnvironmentEquilibrium.setActivity(true);
		}
	}

	private double calcAngleFor2DMovement(Grid<? extends Object> space,
			GridPoint point1, GridPoint point2) {
		double[] displacement = getDisplacement(point1, point2);
		return SpatialMath.angleFromDisplacement(displacement[0],
				displacement[1]);
	}
	
	private double[] getDisplacement(GridPoint point1, GridPoint point2){
		return new double[]{point2.getX() - point1.getX(), point2.getY() - point1.getY()};
	}

	private Boolean enoughDistanceToReach(NdPoint myPoint, NdPoint nx) {
		boolean result = true;
		assert myPoint != null;
		assert nx != null;
		double distance = space.getDistance(myPoint, nx);
		if (distance > 5)
			result = false;
		return result;
	}

	private Boolean enoughDistanceToReach(GridPoint myPoint, GridPoint nx) {
		boolean result = true;
		double distance = grid.getDistance(myPoint, nx);
		if (distance > 5)
			result = false;
		return result;
	}

	private GridPoint myLocation() {
		return grid.getLocation(this);
	}

	// private GridPoint directionTowardsChosenOne(
	// List<GridCell<Attendee>> gridCells) {
	// GridPoint pt = null;
	// int maxCount = -1;
	// for (GridCell<Attendee> cell : gridCells) {
	// if (cell.size() > maxCount) {
	// pt = cell.getPoint();
	// maxCount = cell.size();
	// }
	// }
	// return pt;
	// }

	public Attendee getChosen() {
		return chosen;
	}

	public void setChosen(Attendee chosen) {
		this.chosen = chosen;
	}

	public int getName() {
		return name;
	}

	public boolean isChosenBySomebody() {
		return chosenBySomebody;
	}

	public void setChosenBySomebody(boolean chosenBySomebody) {
		this.chosenBySomebody = chosenBySomebody;
	}

}
