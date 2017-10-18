package ru.idealplm.vp.oceanos.core;

import java.util.HashMap;

import ru.idealplm.vp.oceanos.data.ReportLine;

public class Cache
{
	private static HashMap<String, ReportLine> cachedLines;
	
	public Cache()
	{
		cachedLines = new HashMap<String, ReportLine>();
	}
	
	public static void cacheLine(ReportLine line)
	{
		cachedLines.put(line.uid, line);
	}
	
	public static ReportLine get(String uid)
	{
		return cachedLines.get(uid);
	}
	
	public static boolean contains(String uid)
	{
		return cachedLines.containsKey(uid);
	}
}
