package meetingPlace;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridDimensions;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

/**
 * A simulation of an ice-breaking game where everybody tries to reach back of
 * his "secret colleague". Shows grid with final pattern of agents.
 * 
 * @author Oskar Jarczyk
 * @version 1.0a
 * @since 1.0
 */
public class MeetingsBuilder implements ContextBuilder<Object> {

	private List<Attendee> attendeesList = new ArrayList<Attendee>();
	private boolean gridOnly;
	private int attendeeCount;

	private Schedule schedule = new Schedule();

	private Grid<Object> grid;
	private ContinuousSpace<Object> space;

	public MeetingsBuilder() {
		super();
		List<ISchedulableAction> actions = schedule.schedule(this);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Context build(Context<Object> context) {
		context.setId("pairingSimulation");
		RandomHelper.init();

		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>(
				"meetingNetwork", context, true);
		netBuilder.buildNetwork();
		Parameters params = loadParameters();
		if (gridOnly) {
			System.out.println("This parameter is not implemented yet!");
			System.out
					.println("Seems like Repast works better with both continuus space"
							+ " and grids. I dont have more time to investigate this more. Os.J.");
			throw new UnsupportedOperationException();
			// TODO: uncomment when ready..
			// grid = initiateGrid(context);
		} else {
			Object[] euclidean = initiateContinuusSpace(context);
			grid = (Grid<Object>) euclidean[0];
			space = (ContinuousSpace<Object>) euclidean[1];
		}

		randomizeChosenAgents(context, params);

		for (Object obj : context) {
			if (gridOnly) {
				GridPoint pt = grid.getLocation(obj);
				grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
			} else {
				NdPoint pt = space.getLocation(obj);
				grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
			}
		}
		
		ISchedule schedule = RunEnvironment.getInstance()
				.getCurrentSchedule();
		ScheduleParameters scheduleParams = ScheduleParameters.createRepeating(
				1, 1000, ScheduleParameters.FIRST_PRIORITY - 1);
		schedule.schedule(scheduleParams, this, "prepareForTick");
		
		ScheduleParameters scheduleParamsDm = ScheduleParameters.createRepeating(
				1, 1000, 1);
		schedule.schedule(scheduleParamsDm, this, "detectMove");

		return context;
	}

	private Parameters loadParameters() {
		Parameters params = RunEnvironment.getInstance().getParameters();
		attendeeCount = (Integer) params.getValue("attendee_count");
		gridOnly = (Boolean) params.getValue("grid_only");
		return params;
	}

	private Grid<Object> initiateGrid(Context<Object> context) {
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);

		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.StrictBorders(), 50, 50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, 50, 50));

		for (int i = 0; i < attendeeCount; i++) {
			Attendee att = new Attendee(grid, i);
			context.add(att);
			attendeesList.add(att);
		}
		return grid;
	}

	private Object[] initiateContinuusSpace(Context<Object> context) {
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);

		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.StrictBorders(), 50, 50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, 50, 50));

		for (int i = 0; i < attendeeCount; i++) {
			Attendee att = new Attendee(space, grid, i);
			context.add(att);
			attendeesList.add(att);
			if (gridOnly)
				add(grid, att);
		}
		return new Object[] { grid, space };
	}

	private void randomizeChosenAgents(Context<Object> context,
			Parameters params) {
		if ((Boolean) params.getValue("secret_choice")) {
			for (Object at : context.getObjects(Attendee.class)) {
				int i;
				do {
					i = RandomHelper.nextIntFromTo(0, attendeesList.size() - 1);
				} while (i == ((Attendee) at).getName());
				Attendee atx = attendeesList.get(i);
				((Attendee) at).setChosen(atx);
				atx.setChosenBySomebody(true);
			}
		} else {
			for (Object at : context.getObjects(Attendee.class)) {
				int i = RandomHelper.nextIntFromTo(0, attendeesList.size() - 1);
				while (attendeesList.get(i).isChosenBySomebody()) {
					i = RandomHelper.nextIntFromTo(0, attendeesList.size() - 1);
				}
				Attendee atx = attendeesList.get(i);
				((Attendee) at).setChosen(atx);
				atx.setChosenBySomebody(true);
				attendeesList.remove(i);
			}
		}
	}

	private void add(Grid<Object> space, Object obj) {
		GridDimensions dims = space.getDimensions();
		int[] location = new int[dims.size()];
		findLocation(location, dims);
		while (!space.moveTo(obj, location)) {
			findLocation(location, dims);
		}
	}

	private void findLocation(int[] location, GridDimensions dims) {
		for (int i = 0; i < location.length; i++) {
			location[i] = RandomHelper.nextIntFromTo(0,
					dims.getDimension(i) - 1);
		}
	}

	/**
	 * When no agent moves because he is close enough to his secret lover simply
	 * stop the simulation, we dont need any more ticks.
	 */
	protected void noActivityDetected() {
		RunEnvironment.getInstance().endRun();
	}

	@ScheduledMethod(start = 1, interval = 100, priority = ScheduleParameters.FIRST_PRIORITY)
	public void prepareForTick() {
		EnvironmentEquilibrium.setActivity(false);
	}

	@ScheduledMethod(start = 1, interval = 1000, priority = 1)
	public void detectMove() {
		if (!EnvironmentEquilibrium.getActivity())
			noActivityDetected();
	}
}
