package net.filebot.format;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Settings.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import groovy.lang.Closure;
import groovy.util.XmlSlurper;
import net.filebot.ApplicationFolder;
import net.filebot.mac.MacAppUtilities;
import net.filebot.util.FileUtilities;

/**
 * Global functions available in the {@link ExpressionFormat}
 */
public class ExpressionFormatFunctions {

	/**
	 * General helpers and utilities
	 */
	public static Object call(Object object) {
		if (object instanceof Closure<?>) {
			try {
				return ((Closure<?>) object).call();
			} catch (Exception e) {
				return null;
			}
		}
		if (object instanceof CharSequence && object.toString().isEmpty()) {
			return null;
		}
		return object;
	}

	public static Object any(Object c1, Object c2, Object... cN) {
		return stream(c1, c2, cN).findFirst().orElse(null);
	}

	public static List<Object> allOf(Object c1, Object c2, Object... cN) {
		return stream(c1, c2, cN).collect(toList());
	}

	public static String concat(Object c1, Object c2, Object... cN) {
		return stream(c1, c2, cN).map(Objects::toString).collect(joining());
	}

	protected static Stream<Object> stream(Object c1, Object c2, Object... cN) {
		return Stream.concat(Stream.of(c1, c2), Stream.of(cN)).map(ExpressionFormatFunctions::call).filter(Objects::nonNull);
	}

	public static String quote(Object c1, Object... cN) {
		String q = String.valueOf('"');
		String r = "\\" + q;
		return stream(c1, null, cN).map(Objects::toString).map(s -> q + s.replace(q, r) + q).collect(joining(" "));
	}

	public static Map<String, String> csv(Object path) throws IOException {
		Pattern[] delimiter = { TAB, SEMICOLON };
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (String line : readLines(path)) {
			for (Pattern d : delimiter) {
				String[] field = d.split(line, 2);
				if (field.length >= 2) {
					map.put(field[0].trim(), field[1].trim());
					break;
				}
			}
		}
		return map;
	}

	public static List<String> readLines(Object path) throws IOException {
		return FileUtilities.readLines(getUserFile(path));
	}

	public static Object readXml(Object path) throws Exception {
		return new XmlSlurper().parse(getUserFile(path));
	}

	public static File getUserFile(Object path) {
		File f = new File(path.toString());

		if (!f.isAbsolute()) {
			f = ApplicationFolder.UserHome.resolve(f.getPath());
		}

		if (isMacSandbox()) {
			MacAppUtilities.askUnlockFolders(null, singleton(f));
		}

		return f;
	}

	private ExpressionFormatFunctions() {
		throw new UnsupportedOperationException();
	}

}
