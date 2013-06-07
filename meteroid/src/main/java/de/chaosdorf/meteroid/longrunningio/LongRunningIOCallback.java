package de.chaosdorf.meteroid.longrunningio;

public interface LongRunningIOCallback
{
	public void displayErrorMessage(final LongRunningIOTask task, final String message);

	public void processIOResult(final LongRunningIOTask task, final String json);
}
