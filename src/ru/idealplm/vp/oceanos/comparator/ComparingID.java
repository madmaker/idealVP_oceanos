package ru.idealplm.vp.oceanos.comparator;

import java.util.ArrayList;
import java.util.Comparator;


public class ComparingID implements Comparator<String> {

	@Override
	public int compare(String arg0, String arg1) {

		String[] arg0Arr = makeIdStrArray(arg0);
		String[] arg1Arr = makeIdStrArray(arg1);
		
		if (arg0Arr == null)
			return -1;
		else if (arg1Arr == null)
			return 1;
		
		int minLength = arg0Arr.length;

		if (arg0Arr.length > arg1Arr.length)
			minLength = arg1Arr.length;

		for (int i = 0; i < minLength; i++) {

			if (Character.isLetter(arg0Arr[i].charAt(0))
					&& Character.isDigit(arg1Arr[i].charAt(0))) {
				return 1;
			}

			if (Character.isDigit(arg0Arr[i].charAt(0))
					&& Character.isLetter(arg1Arr[i].charAt(0))) {
				return -1;
			}

			if (Character.isDigit(arg0Arr[i].charAt(0))) {
				if (Long.parseLong(arg0Arr[i]) < Long.parseLong((arg1Arr[i]))) {
					return -1;
				}
				if (Long.parseLong(arg0Arr[i]) > Long.parseLong(arg1Arr[i])) {
					return 1;
				}

				if ((Long.parseLong(arg0Arr[i]) == Long.parseLong(arg1Arr[i]))
						&& ((i + 1) == arg0Arr.length)
						&& (arg0Arr.length < arg1Arr.length)) {
					return -1;
				}
						
				if ((Long.parseLong(arg0Arr[i]) == Long.parseLong(arg1Arr[i]))
						&& ((i + 1) == arg0Arr.length)
						&& ((i + 1) == arg1Arr.length)) 
				return 0;
			}
			if (Character.isLetter(arg0Arr[i].charAt(0))) {
				if (arg0Arr[i].compareTo(arg1Arr[i]) < 0) {
					return -1;
				}
				if (arg0Arr[i].compareTo(arg1Arr[i]) > 0) {
					return 1;
				}
				if ((arg0Arr[i].compareTo(arg1Arr[i]) == 0)
						&& ((i + 1) == arg0Arr.length)
						&& ((i + 1) == arg1Arr.length)) {
					return 0;
				}
			}
		}
		return 0;
	}
	
	static String[] makeIdStrArray(String inStr) {

		if (inStr.trim().length() > 0) {
			char[] charIdArray = inStr.toCharArray();
			ArrayList<String> tempCollector = new ArrayList<String>();
			String tempStr = inStr.substring(0, 1);
			for (int i = 1; i < charIdArray.length; i++) {
				if (charIdArray[i] == ' ' || charIdArray[i] == '-'
						|| charIdArray[i] == '/' || charIdArray[i] == '.') {
					if (!tempStr.equals("")) {
						tempCollector.add(tempStr.trim());
					}
					tempStr = "";
					continue;
				}

				if ((Character.isLetter(charIdArray[i]) && Character
						.isDigit(charIdArray[i - 1]))
						|| (Character.isLetter(charIdArray[i - 1]) && Character
								.isDigit(charIdArray[i]))) {
					if (!tempStr.equals("")) {
						tempCollector.add(tempStr.trim());
					}
					tempStr = "" + charIdArray[i];
					continue;
				}
				tempStr += charIdArray[i];
			}
			tempCollector.add(tempStr.trim());
			return tempCollector.toArray(new String[tempCollector.size()]);
		}
		else {
			String out[];
			return out = null;
		}
	}		
}
