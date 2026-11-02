package com.xc3fff0e.xmanager;

/**
 * Compares dotted version names numerically, segment by segment, so that
 * "5.10" is newer than "5.9" and "5.9.1" is newer than "5.9".
 */
public final class VersionUtil {

	private VersionUtil() {
	}

	/**
	 * @return negative if {@code a} is older than {@code b}, zero if equal, positive if newer.
	 *         Missing segments count as 0 ("5.9" == "5.9.0"). A leading "v" and any
	 *         non-numeric suffix of a segment ("1-beta") are ignored.
	 * @throws NumberFormatException if either version contains no digits at all
	 */
	public static int compare(String a, String b) {
		int[] x = parse(a);
		int[] y = parse(b);
		int n = Math.max(x.length, y.length);
		for (int i = 0; i < n; i++) {
			int p = i < x.length ? x[i] : 0;
			int q = i < y.length ? y[i] : 0;
			if (p != q) {
				return p < q ? -1 : 1;
			}
		}
		return 0;
	}

	static int[] parse(String version) {
		if (version == null) {
			throw new NumberFormatException("null version");
		}
		String v = version.trim();
		if (v.startsWith("v") || v.startsWith("V")) {
			v = v.substring(1);
		}
		String[] parts = v.split("\\.");
		int[] out = new int[parts.length];
		boolean anyDigit = false;
		for (int i = 0; i < parts.length; i++) {
			String p = parts[i].trim();
			int end = 0;
			while (end < p.length() && Character.isDigit(p.charAt(end))) {
				end++;
			}
			if (end > 0) {
				anyDigit = true;
				out[i] = Integer.parseInt(p.substring(0, Math.min(end, 9)));
			}
		}
		if (!anyDigit) {
			throw new NumberFormatException("not a version: \"" + version + "\"");
		}
		return out;
	}
}
