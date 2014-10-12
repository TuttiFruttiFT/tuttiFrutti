package tuttifrutti.utils;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.joda.time.DateTime.now;
import static org.joda.time.Seconds.secondsBetween;
import static scala.concurrent.duration.Duration.Zero;
import static scala.concurrent.duration.Duration.create;

import org.joda.time.DateTime;

import scala.concurrent.duration.FiniteDuration;

public final class FiniteDurationUtils {

	public static final FiniteDuration ONE_MINUTE = create(1, MINUTES);
	
	public static final FiniteDuration FIVE_MINUTES = create(5, MINUTES);

	public static final FiniteDuration TEN_MINUTES = create(10, MINUTES);
	
	public static final FiniteDuration THIRTY_MINUTES = create(30, MINUTES);

	public static final FiniteDuration ONE_HOUR = create(1, HOURS);
	
	public static final FiniteDuration ONE_DAY = create(1, DAYS);
	
	private FiniteDurationUtils() {
		super();
	}
	
	public static FiniteDuration nextExecutionInSeconds(int hour, int minute) {
		return create(secondsBetween(now(), nextExecution(hour, minute)).getSeconds(), SECONDS);
	}
	
	public static FiniteDuration closestTimeWithZeroMinutesZeroSecondsAwayFrom(DateTime dateTime) {
		FiniteDuration initialDelay = Zero();
		
		boolean dateTimeIsNotZeroMinutes = dateTime.getMinuteOfHour() != 0;
		boolean dateTimeIsNotZeroSeconds = dateTime.getSecondOfMinute() != 0;

		if (dateTimeIsNotZeroMinutes || dateTimeIsNotZeroSeconds) {
			initialDelay = create(nextTimeWithZeroMinutesAndZeroSecondsAwayFrom(dateTime), SECONDS);
		}
		return initialDelay;
	}

	private static int nextTimeWithZeroMinutesAndZeroSecondsAwayFrom(DateTime dateTime) {
		return secondsBetween(dateTime, dateTime.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0)).getSeconds();
	}

	private static DateTime nextExecution(int hour, int minute) {
		DateTime next = new DateTime().withHourOfDay(hour).withMinuteOfHour(minute).withSecondOfMinute(0).withMillisOfSecond(0);

		return (next.isBeforeNow()) ? next.plusHours(24) : next;
	}
}
