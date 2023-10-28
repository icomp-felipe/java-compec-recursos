package compec.ufam.recursos;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapTester {

	public static void main(String[] args) {
		
		Map<Integer, String> map = new LinkedHashMap<>();
		
		map.put(1, "a");
		map.put(2, "b");
		map.put(1, "c");
		
		for (String s: map.values())
			System.out.println(s);
		
	}

}
