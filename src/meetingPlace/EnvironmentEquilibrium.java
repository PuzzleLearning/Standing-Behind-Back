package meetingPlace;

public class EnvironmentEquilibrium {

	private static boolean activity = false;

	public static synchronized boolean getActivity() {
		return activity;
	}

	public static synchronized void setActivity(boolean defineActivity) {
		activity = defineActivity;
	}

}