package tw.com.util;

import java.util.HashMap;
import java.util.Map;

public class Util {
	// public static Class<?> getFieldType(String type) {
	public static String getFieldType(String type) {

		Map<String, String> map = new HashMap<>();
		map.put("CHAR", "String");
		map.put("VARCHAR", "String");
		map.put("LONGVARCHAR", "String");
		map.put("NUMERIC", "BigDecimal");
		map.put("DECIMAL", "BigDecimal");
		map.put("BIT", "boolean");
		map.put("TINYINT", "byte");
		map.put("SMALLINT", "short");
		map.put("INTEGER", "int");
		map.put("BIGINT", "long");
		map.put("REAL", "float");
		map.put("FLOAT", "double");
		map.put("DOUBLE", "double");
		map.put("BINARY", "byte[]");
		map.put("VARBINARY", "byte[]");
		map.put("LONGVARBINARY", "byte[]");
		map.put("DATE", "Date");
		map.put("TIME", "Time");
		map.put("TIMESTAMP", "Timestamp");

		// Map<String, Object> map = new HashMap<>();
		// map.put("CHAR", String.class);
		// map.put("VARCHAR", String.class);
		// map.put("LONGVARCHAR", String.class);
		// map.put("NUMERIC", java.math.BigDecimal.class);
		// map.put("DECIMAL", java.math.BigDecimal.class);
		// map.put("BIT", boolean.class);
		// map.put("TINYINT", byte.class);
		// map.put("SMALLINT", short.class);
		// map.put("INTEGER", int.class);
		// map.put("BIGINT", long.class);
		// map.put("REAL", float.class);
		// map.put("FLOAT", double.class);
		// map.put("DOUBLE", double.class);
		// map.put("BINARY", byte[].class);
		// map.put("VARBINARY", byte[].class);
		// map.put("LONGVARBINARY", byte[].class);
		// map.put("DATE", java.sql.Date.class);
		// map.put("TIME", java.sql.Time.class);
		// map.put("TIMESTAMP", java.sql.Timestamp.class);

		return map.get(type.toUpperCase());

	}
}
