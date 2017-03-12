package net.mfjassociates.xencenter.util;

public class ReflectionHelper {
	
	public static Object getField(Object o, String fieldName) {
		Object field=null;
		try {
			field = o.getClass().getDeclaredField(fieldName).get(o);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return field;
	}

	public static void main(String[] args) {
	}

}
