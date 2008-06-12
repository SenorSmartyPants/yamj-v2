/*
 * Copyright 2001-2007 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 * $Id: StringUtils.java 3877 2007-08-03 19:48:10Z gbevin $
 */
package com.moviejukebox;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.moviejukebox.model.Library;
import com.moviejukebox.model.MediaLibraryPath;
import com.moviejukebox.model.Movie;
import com.moviejukebox.plugin.MovieThumbnailPlugin;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public abstract class MovieJukeboxTools {

	private static Logger logger = Logger.getLogger("moviejukebox");

	private static final Map<Character, String> AGGRESSIVE_HTML_ENCODE_MAP = new HashMap<Character, String>();
	private static final Map<Character, String> DEFENSIVE_HTML_ENCODE_MAP = new HashMap<Character, String>();
	private static final Map<String, Character> HTML_DECODE_MAP = new HashMap<String, Character>();
	private static final HtmlEncoderFallbackHandler HTML_ENCODER_FALLBACK = new HtmlEncoderFallbackHandler();

	 {
		// Html encoding mapping according to the HTML 4.0 spec
		// http://www.w3.org/TR/REC-html40/sgml/entities.html

		// Special characters for HTML
		AGGRESSIVE_HTML_ENCODE_MAP.put('\u0026', "&amp;");
		AGGRESSIVE_HTML_ENCODE_MAP.put('\u003C', "&lt;");
		AGGRESSIVE_HTML_ENCODE_MAP.put('\u003E', "&gt;");
		AGGRESSIVE_HTML_ENCODE_MAP.put('\u0022', "&quot;");

		DEFENSIVE_HTML_ENCODE_MAP.put('\u0152', "&OElig;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0153', "&oelig;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0160', "&Scaron;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0161', "&scaron;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0178', "&Yuml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u02C6', "&circ;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u02DC', "&tilde;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2002', "&ensp;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2003', "&emsp;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2009', "&thinsp;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u200C', "&zwnj;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u200D', "&zwj;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u200E', "&lrm;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u200F', "&rlm;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2013', "&ndash;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2014', "&mdash;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2018', "&lsquo;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2019', "&rsquo;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u201A', "&sbquo;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u201C', "&ldquo;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u201D', "&rdquo;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u201E', "&bdquo;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2020', "&dagger;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2021', "&Dagger;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2030', "&permil;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2039', "&lsaquo;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u203A', "&rsaquo;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u20AC', "&euro;");

		// Character entity references for ISO 8859-1 characters
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00A0', "&nbsp;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00A1', "&iexcl;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00A2', "&cent;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00A3', "&pound;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00A4', "&curren;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00A5', "&yen;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00A6', "&brvbar;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00A7', "&sect;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00A8', "&uml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00A9', "&copy;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00AA', "&ordf;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00AB', "&laquo;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00AC', "&not;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00AD', "&shy;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00AE', "&reg;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00AF', "&macr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00B0', "&deg;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00B1', "&plusmn;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00B2', "&sup2;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00B3', "&sup3;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00B4', "&acute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00B5', "&micro;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00B6', "&para;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00B7', "&middot;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00B8', "&cedil;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00B9', "&sup1;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00BA', "&ordm;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00BB', "&raquo;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00BC', "&frac14;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00BD', "&frac12;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00BE', "&frac34;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00BF', "&iquest;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00C0', "&Agrave;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00C1', "&Aacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00C2', "&Acirc;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00C3', "&Atilde;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00C4', "&Auml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00C5', "&Aring;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00C6', "&AElig;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00C7', "&Ccedil;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00C8', "&Egrave;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00C9', "&Eacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00CA', "&Ecirc;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00CB', "&Euml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00CC', "&Igrave;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00CD', "&Iacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00CE', "&Icirc;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00CF', "&Iuml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00D0', "&ETH;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00D1', "&Ntilde;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00D2', "&Ograve;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00D3', "&Oacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00D4', "&Ocirc;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00D5', "&Otilde;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00D6', "&Ouml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00D7', "&times;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00D8', "&Oslash;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00D9', "&Ugrave;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00DA', "&Uacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00DB', "&Ucirc;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00DC', "&Uuml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00DD', "&Yacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00DE', "&THORN;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00DF', "&szlig;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00E0', "&agrave;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00E1', "&aacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00E2', "&acirc;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00E3', "&atilde;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00E4', "&auml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00E5', "&aring;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00E6', "&aelig;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00E7', "&ccedil;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00E8', "&egrave;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00E9', "&eacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00EA', "&ecirc;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00EB', "&euml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00EC', "&igrave;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00ED', "&iacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00EE', "&icirc;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00EF', "&iuml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00F0', "&eth;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00F1', "&ntilde;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00F2', "&ograve;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00F3', "&oacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00F4', "&ocirc;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00F5', "&otilde;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00F6', "&ouml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00F7', "&divide;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00F8', "&oslash;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00F9', "&ugrave;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00FA', "&uacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00FB', "&ucirc;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00FC', "&uuml;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00FD', "&yacute;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00FE', "&thorn;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u00FF', "&yuml;");

		// Mathematical, Greek and Symbolic characters for HTML
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0192', "&fnof;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0391', "&Alpha;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0392', "&Beta;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0393', "&Gamma;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0394', "&Delta;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0395', "&Epsilon;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0396', "&Zeta;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0397', "&Eta;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0398', "&Theta;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u0399', "&Iota;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u039A', "&Kappa;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u039B', "&Lambda;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u039C', "&Mu;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u039D', "&Nu;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u039E', "&Xi;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u039F', "&Omicron;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03A0', "&Pi;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03A1', "&Rho;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03A3', "&Sigma;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03A4', "&Tau;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03A5', "&Upsilon;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03A6', "&Phi;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03A7', "&Chi;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03A8', "&Psi;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03A9', "&Omega;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03B1', "&alpha;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03B2', "&beta;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03B3', "&gamma;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03B4', "&delta;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03B5', "&epsilon;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03B6', "&zeta;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03B7', "&eta;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03B8', "&theta;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03B9', "&iota;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03BA', "&kappa;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03BB', "&lambda;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03BC', "&mu;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03BD', "&nu;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03BE', "&xi;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03BF', "&omicron;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03C0', "&pi;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03C1', "&rho;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03C2', "&sigmaf;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03C3', "&sigma;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03C4', "&tau;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03C5', "&upsilon;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03C6', "&phi;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03C7', "&chi;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03C8', "&psi;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03C9', "&omega;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03D1', "&thetasym;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03D2', "&upsih;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u03D6', "&piv;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2022', "&bull;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2026', "&hellip;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2032', "&prime;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2033', "&Prime;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u203E', "&oline;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2044', "&frasl;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2118', "&weierp;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2111', "&image;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u211C', "&real;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2122', "&trade;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2135', "&alefsym;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2190', "&larr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2191', "&uarr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2192', "&rarr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2193', "&darr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2194', "&harr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u21B5', "&crarr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u21D0', "&lArr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u21D1', "&uArr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u21D2', "&rArr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u21D3', "&dArr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u21D4', "&hArr;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2200', "&forall;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2202', "&part;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2203', "&exist;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2205', "&empty;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2207', "&nabla;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2208', "&isin;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2209', "&notin;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u220B', "&ni;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u220F', "&prod;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2211', "&sum;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2212', "&minus;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2217', "&lowast;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u221A', "&radic;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u221D', "&prop;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u221E', "&infin;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2220', "&ang;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2227', "&and;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2228', "&or;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2229', "&cap;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u222A', "&cup;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u222B', "&int;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2234', "&there4;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u223C', "&sim;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2245', "&cong;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2248', "&asymp;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2260', "&ne;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2261', "&equiv;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2264', "&le;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2265', "&ge;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2282', "&sub;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2283', "&sup;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2284', "&nsub;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2286', "&sube;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2287', "&supe;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2295', "&oplus;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2297', "&otimes;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u22A5', "&perp;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u22C5', "&sdot;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2308', "&lceil;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2309', "&rceil;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u230A', "&lfloor;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u230B', "&rfloor;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2329', "&lang;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u232A', "&rang;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u25CA', "&loz;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2660', "&spades;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2663', "&clubs;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2665', "&hearts;");
		DEFENSIVE_HTML_ENCODE_MAP.put('\u2666', "&diams;");

		Set<Map.Entry<Character, String>> aggresive_entries = AGGRESSIVE_HTML_ENCODE_MAP.entrySet();
		for (Map.Entry<Character, String> entry : aggresive_entries) {
			HTML_DECODE_MAP.put(entry.getValue(), entry.getKey());
		}

		Set<Map.Entry<Character, String>> defensive_entries = DEFENSIVE_HTML_ENCODE_MAP.entrySet();
		for (Map.Entry<Character, String> entry : defensive_entries) {
			HTML_DECODE_MAP.put(entry.getValue(), entry.getKey());
		}
	}

	public static String decodeHtml(String source) {
		if (null == source || 0 == source.length()) {
			return source;
		}

		int current_index = 0;
		int delimiter_start_index = 0;
		int delimiter_end_index = 0;

		StringBuilder result = null;

		while (current_index <= source.length()) {
			delimiter_start_index = source.indexOf('&', current_index);
			if (delimiter_start_index != -1) {
				delimiter_end_index = source.indexOf(';', delimiter_start_index + 1);
				if (delimiter_end_index != -1) {
					// ensure that the string builder is setup correctly
					if (null == result) {
						result = new StringBuilder();
					}

					// add the text that leads up to this match
					if (delimiter_start_index > current_index) {
						result.append(source.substring(current_index, delimiter_start_index));
					}

					// add the decoded entity
					String entity = source.substring(delimiter_start_index, delimiter_end_index + 1);

					current_index = delimiter_end_index + 1;

					// try to decoded numeric entities
					if (entity.charAt(1) == '#') {
						int start = 2;
						int radix = 10;
						// check if the number is hexadecimal
						if (entity.charAt(2) == 'X' || entity.charAt(2) == 'x') {
							start++;
							radix = 16;
						}
						try {
							Character c = new Character((char) Integer.parseInt(entity.substring(start, entity.length() - 1), radix));
							result.append(c);
						}
						// when the number of the entity can't be parsed, add
						// the entity as-is
						catch (NumberFormatException e) {
							result.append(entity);
						}
					} else {
						// try to decode the entity as a literal
						Character decoded = HTML_DECODE_MAP.get(entity);
						if (decoded != null) {
							result.append(decoded);
						}
						// if there was no match, add the entity as-is
						else {
							result.append(entity);
						}
					}
				} else {
					break;
				}
			} else {
				break;
			}
		}

		if (null == result) {
			return source;
		} else if (current_index < source.length()) {
			result.append(source.substring(current_index));
		}

		return result.toString();
	}

	public static void copyDir(String srcDir, String dstDir) {
		try {
			File src = new File(srcDir);
			if (!src.exists()) {
				logger.severe("The specified " + srcDir + " file or directory does not exist!");
				return;
			}

			File dst = new File(dstDir);
			dst.mkdirs();
			
			if (!dst.exists()) {
				logger.severe("The specified " + dstDir + " output directory does not exist!");
				return;
			}

			if (src.isFile())
				copy(new FileInputStream(src), new FileOutputStream(dstDir));
			else {
				File[] contentList = src.listFiles();
				if (contentList!=null) {
					List<File> files = Arrays.asList(contentList);
					Collections.sort(files);
					
					for (File file : files) {
						if (!file.getName().equals(".svn")) {
							if (file.isDirectory()) {
								copyDir(file.getAbsolutePath(), dstDir + File.separator + file.getName());
							} else {
								copyFile(file, dst);
							}
						}
					}
				}
			}		
		} catch (IOException e) {
			logger.severe("Failed copying file " + srcDir + " to " + dstDir);
			e.printStackTrace();
		}
	}
	
	public static void copyFile(File src, File dst) {
		try {
			if (!src.exists()) {
				logger.severe("The specified " + src + " file does not exist!");
				return;
			}
			
			if (dst.isDirectory()) {
				dst.mkdirs();
				copy(new FileInputStream(src), new FileOutputStream(dst + File.separator + src.getName()));
			} else {
				copy(new FileInputStream(src), new FileOutputStream(dst));
			}
			
		} catch (IOException e) {
			logger.severe("Failed copying file " + src + " to " + dst);
			e.printStackTrace();
		}
	}
	
	final static int BUFF_SIZE = 100000;
	final static byte[] buffer = new byte[BUFF_SIZE];
	public static void copy(InputStream is, OutputStream os) throws IOException {
		try {
			while (true) {
				synchronized (buffer) {
					int amountRead = is.read(buffer);
					if (amountRead == -1)
						break;
					os.write(buffer, 0, amountRead);
				}
			}
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// ignore
			}
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public static void createThumbnail(MovieThumbnailPlugin thumbnailManager, String rootPath, String skinHome, Movie movie, boolean forceThumbnailOverwrite) {
		try {
			String src = rootPath + File.separator + movie.getPosterFilename();
			String dst = rootPath + File.separator + movie.getThumbnailFilename();

			if (!(new File(dst).exists()) || forceThumbnailOverwrite) {
				BufferedImage bi = loadBufferedImage(src);
				if (bi == null) {
					copyFile(new File(skinHome + File.separator + "resources" + File.separator + "dummy.jpg"),
							new File(rootPath + File.separator + movie.getPosterFilename()));
					bi = loadBufferedImage(src);
				}
				
				bi = thumbnailManager.generate(movie, bi);
				
				saveImageToDisk(bi, dst);
			}
		} catch (Exception e) {
			logger.severe("Failed creating thumbnail for " + movie.getTitle());
			e.printStackTrace();
		}
	}

	public static void saveImageToDisk(BufferedImage bi, String str) {
		if (str.endsWith("jpg") | str.endsWith("jpeg")) {
			saveImageAsJpeg(bi,str);
		} else if (str.endsWith("png")) {
			saveImageAsPng(bi,str);
		} else {
			saveImageAsJpeg(bi,str);
		}
	}
	
	public static void saveImageAsJpeg(BufferedImage bi, String str) {
		if (bi == null || str == null)
			return;

		// save image as Jpeg
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(str);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
			param.setQuality(0.95f, false);

	        BufferedImage bufImage = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
	        bufImage.createGraphics().drawImage(bi, 0, 0, null, null);
			
			encoder.encode(bufImage);

		} catch (Exception e) {
			logger.severe("Failed Saving thumbnail file: " + str);
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	public static void saveImageAsPng(BufferedImage bi, String str) {
		if (bi == null || str == null)
			return;

		// save image as PNG
		try {
			ImageIO.write(bi, "png", new File(str));
		} catch (Exception e) {
			logger.severe("Failed Saving thumbnail file: " + str);
			e.printStackTrace();
		} 
	}

	/**
	 * Download the movie poster for the specified movie into the specified
	 * file.
	 * 
	 * @throws IOException
	 */
	public static void downloadPoster(File posterFile, Movie mediaFile) throws IOException {
		InputStream in = null;
		OutputStream out = null;

		URL url = new URL(mediaFile.getPosterURL());
		URLConnection cnx = url.openConnection();

		// Let's pretend we're Firefox...
		cnx.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-GB; rv:1.8.1.5) Gecko/20070719 Iceweasel/2.0.0.5 (Debian-2.0.0.5-0etch1)");

		try {
			in = cnx.getInputStream();
			out = new FileOutputStream(posterFile);

			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead);
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}

			if (out != null) {
				out.close();
			}
		}
	}
	

	public static BufferedImage loadBufferedImage(String filename) {
		// Create BufferedImage
		BufferedImage bi = null;
		FileInputStream fis = null;
		try {
			// load file from disk using Sun's JPEGIMageDecoder
			fis = new FileInputStream(filename);
			JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(fis);
			bi = decoder.decodeAsBufferedImage();
			fis.close();
		} catch (Exception e) {
			logger.severe("Failed Loading poster file: " + filename);
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				}
			}
		}
		return bi;
	}
	
	private static boolean needsHtmlEncoding(String source, boolean defensive)
	{
		if (null == source)
		{
			return false;
		}
		
		boolean encode = false;
		char ch;
		for (int i = 0; i < source.length(); i++)
		{
			ch = source.charAt(i);
			
			if ((defensive || (ch != '\u0022' && ch != '\u0026' && ch != '\u003C' && ch != '\u003E')) &&
				ch < '\u00A0')
			{
				continue;
			}
			
			encode = true;
			break;
		}
		
		return encode;
	}
	
	/**
	 * Transforms a provided <code>String</code> object into a new string,
	 * containing only valid Html characters.
	 *
	 * @param source The string that has to be transformed into a valid Html
	 * string.
	 * @return The encoded <code>String</code> object.
	 * @see #encodeClassname(String)
	 * @see #encodeUrl(String)
	 * @see #encodeUrlValue(String)
	 * @see #encodeXml(String)
	 * @see #encodeSql(String)
	 * @see #encodeString(String)
	 * @see #encodeLatex(String)
	 * @see #encodeRegexp(String)
	 * @since 1.0
	 */
	public static String encodeHtml(String source)
	{
		if (needsHtmlEncoding(source, false))
		{
			return encode(source, HTML_ENCODER_FALLBACK, AGGRESSIVE_HTML_ENCODE_MAP, DEFENSIVE_HTML_ENCODE_MAP);
		}
		return source;
	}

	/**
	 * Transforms a provided <code>String</code> object into a new string,
	 * using the mapping that are provided through the supplied encoding
	 * table.
	 *
	 * @param source The string that has to be transformed into a valid
	 * string, using the mappings that are provided through the supplied
	 * encoding table.
	 * @param encodingTables A <code>Map</code> object containing the mappings
	 * to transform characters into valid entities. The keys of this map
	 * should be <code>Character</code> objects and the values
	 * <code>String</code> objects.
	 * @return The encoded <code>String</code> object.
	 * @since 1.0
	 */
	private static String encode(String source, EncoderFallbackHandler fallbackHandler, Map<Character, String>... encodingTables)
	{
		if (null == source)
		{
			return null;
		}

		if (null == encodingTables ||
			0 == encodingTables.length)
		{
			return source;
		}

		StringBuilder	encoded_string = null;
		char[]			string_to_encode_array = source.toCharArray();
		int				last_match = -1;

		for (int i = 0; i < string_to_encode_array.length; i++)
		{
			char char_to_encode = string_to_encode_array[i];
			for (Map<Character, String> encoding_table : encodingTables)
			{
				if (encoding_table.containsKey(char_to_encode))
				{
					encoded_string = prepareEncodedString(source, encoded_string, i, last_match, string_to_encode_array);
					
					encoded_string.append(encoding_table.get(char_to_encode));
					last_match = i;
				}
			}
			
			if (fallbackHandler != null &&
				last_match < i &&
				fallbackHandler.hasFallback(char_to_encode))
			{
				encoded_string = prepareEncodedString(source, encoded_string, i, last_match, string_to_encode_array);

				fallbackHandler.appendFallback(encoded_string, char_to_encode);
				last_match = i;
			}
		}

		if (null == encoded_string)
		{
			return source;
		}
		else
		{
			int difference = string_to_encode_array.length-(last_match+1);
			if (difference > 0)
			{
				encoded_string.append(string_to_encode_array, last_match+1, difference);
			}
			return encoded_string.toString();
		}
	}

	private static StringBuilder prepareEncodedString(String source, StringBuilder encodedString, int i, int lastMatch, char[] stringToEncodeArray)
	{
		if (null == encodedString)
		{
			encodedString = new StringBuilder(source.length());
		}
		
		int difference = i - (lastMatch + 1);
		if (difference > 0)
		{
			encodedString.append(stringToEncodeArray, lastMatch + 1, difference);
		}
		
		return encodedString;
	}
	
	private static interface EncoderFallbackHandler
	{
		abstract boolean hasFallback(char character);
		abstract void appendFallback(StringBuilder encodedBuffer, char character);
	}
	
	private static class HtmlEncoderFallbackHandler implements EncoderFallbackHandler
	{
		private final  String PREFIX = "&#";
		private final  String SUFFIX = ";";
		
		public boolean hasFallback(char character)
		{
			if (character < '\u00A0')
			{
				return false;
			}
			
			return true;
		}
		
		public void appendFallback(StringBuilder encodedBuffer, char character)
		{
			encodedBuffer.append(PREFIX);
			encodedBuffer.append((int)character);
			encodedBuffer.append(SUFFIX);
		}
	}
}
