package ru.idealplm.vp.oceanos.comparator;

import java.util.Comparator;

public class StrSort implements Comparator<String> {

	@Override
	public int compare(String arg0, String arg1) {
		int result=0;
		
		int len, arg0len, arg1len;

		arg0len=arg0.length();
		arg1len=arg1.length();
		
		if (arg0len==arg1len) len=arg0len;
		if (arg0len>arg1len) len=arg1len; else len=arg0len;
		
		int i;
		boolean isEq=true;
		for (i = 0; (i < len)&&(isEq); i++) 
			if (arg0.charAt(i)!=arg1.charAt(i)) isEq=false;

		if (i==arg0.length()&&isEq) return -1;
		if (i==arg1.length()&&isEq) return 1;

		i--;
		boolean isArg0Digit=Character.isDigit(arg0.charAt(i));
		boolean isArg1Digit=Character.isDigit(arg1.charAt(i));
		
		if (isArg0Digit&&isArg1Digit&&i>0){
			while (((Character.isDigit(arg0.charAt(i)))||(Character.isDigit(arg1.charAt(i))))&&i>0) i--;
			i++;
		
			if (Integer.valueOf(getCompString(arg0, i))<Integer.valueOf(getCompString(arg1, i))) result=-1;
			else result=1;
		} else{
			if (arg0.charAt(i)<arg1.charAt(i)) result=-1;
			else result=1;
		}
	
		return result;	
	}
	
	private String getCompString(String arg0, int i){
		boolean isNumericArg0=Character.isDigit(arg0.charAt(i));
		boolean controlState=isNumericArg0;
		String cStringArg0="";
		
		for (int j = i; (isNumericArg0==controlState)&&(j < arg0.length()); j++) {
			if ((Character.isDigit(arg0.charAt(j))&&isNumericArg0)||(!Character.isDigit(arg0.charAt(j))&&!isNumericArg0))
				cStringArg0=cStringArg0+arg0.charAt(j);
			else controlState=!controlState;
		}
		
		return cStringArg0;
	}
}