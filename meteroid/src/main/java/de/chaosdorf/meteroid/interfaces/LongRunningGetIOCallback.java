package de.chaosdorf.meteroid.interfaces;

public interface LongRunningGetIOCallback
{
	public void displayErrorMessage(String message);
	public void processIOResult(String json);
}
