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
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class MeetingsBuilder implements ContextBuilder<Object> {

	private List<Attendee> attendeesList = new ArrayList<Attendee>();

	@Override
	public Context build(Context<Object> context) {
		context.setId("pairingSimulation");

		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>(
				"meetingNetwork", context, true);
		netBuilder.buildNetwork();

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);

		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), 50,
				50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, 50, 50));

		Parameters params = RunEnvironment.getInstance().getParameters();
		int attendeeCount = (Integer) params.getValue("attendee_count");

		for (int i = 0; i < attendeeCount; i++) {
			Attendee att = new Attendee(space, grid, i);
			context.add(att);
			attendeesList.add(att);
		}

		for (Object at : context.getObjects(Attendee.class)) {
			int i = RandomHelper.nextIntFromTo(0, attendeesList.size() - 1);
			while (attendeesList.get(i).hasBeenChosen()) {
				i = RandomHelper.nextIntFromTo(0, attendeesList.size() - 1);
			}
			Attendee atx = attendeesList.get(i);
			((Attendee)at).setChosen(atx);
			atx.setHasBeenChosen(true);
			attendeesList.remove(i);
		}

		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}

		return context;
	}
}
