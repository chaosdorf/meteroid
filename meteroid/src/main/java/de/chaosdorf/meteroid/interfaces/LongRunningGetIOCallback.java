package de.chaosdorf.meteroid.interfaces;

import de.chaosdorf.meteroid.enums.LongRunningIOTask;

public interface LongRunningGetIOCallback
{
	public void displayErrorMessage(final String message);

	public void processIOResult(final LongRunningIOTask task, final String json);
}
