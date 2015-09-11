package pkg6.entities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class General_Infos {
	public static String IP_OF_OTHER_SIDE = "192.168.43.1";
	public static int PORT_FOR_CALL_RECEIVECALL = 4444;
	public static int PORT_FOR_IMAGES = 55555;
	public static int PORT_FOR_AUDIO = 50001;
	
	public static String[] sortAscending(String[] input) {
		List<String> sortedList = Arrays.asList(input);
		Collections.sort(sortedList);

		String[] output = (String[]) sortedList.toArray();
		return output;
	}
}
