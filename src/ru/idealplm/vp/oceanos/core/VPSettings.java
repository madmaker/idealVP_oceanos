package ru.idealplm.vp.oceanos.core;

public class VPSettings
{
	public static boolean isOKPressed;
	public static boolean isCancelled;
	public static boolean doShowAdditionalForm;
	
	public static String[] nonbreakableWords = new String[]{};
	
	static
	{
		reset();
	}
	
	public static void reset()
	{
		isOKPressed = false;
		isCancelled = false;
		doShowAdditionalForm = false;
		if(nonbreakableWords!=null && nonbreakableWords.length > 0)
			nonbreakableWords = new String[]{};
	}
}
