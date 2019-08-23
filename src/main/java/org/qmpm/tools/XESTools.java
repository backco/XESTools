
/*
 * 	XESTools - A helper class for the OpenXES implementation of the eXtensible Event Stream standard.
 *
 * 	Author: Christoffer Olling Back	<www.christofferback.com>
 *
 * 	Copyright (C) 2018 University of Copenhagen
 *
 *	This file is part of LogTrie.
 *
 *	LogTrie is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	LogTrie is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with LogTrie.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.qmpm.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.deckfour.xes.out.XesXmlSerializer;

public class XESTools {

	private static final String	CONCEPTNAME	= "concept:name";
	private final static String	ERROR		= "[ERROR (" + XESTools.class.getName() + ")]: ";
	private static final String	TIMESTAMP	= "time:timestamp";

	public static boolean canParse(String path) throws IOException {

		final File file = read(path);
		final XesXmlParser xesXmlParser = new XesXmlParser();
		final XesXmlGZIPParser xesXmlGZIPParser = new XesXmlGZIPParser();

		if (xesXmlParser.canParse(file)) {

			return true;

		} else if (xesXmlGZIPParser.canParse(file)) {

			return true;

		} else {

			return false;
		}

	}

	public static Set<String> getAllActivities(XLog l) {

		final Set<String> result = new HashSet<>();

		for (final XTrace t : l) {
			for (final XEvent e : t) {
				result.add(e.getAttributes().get(CONCEPTNAME).toString());
			}
		}

		return result;
	}

	public static boolean isSorted(XLog l) {

		for (final XTrace t : l) {
			if (!isSorted(t)) {
				return false;
			}
		}

		for (int i = 0; i < l.size() - 1; i++) {
			if (0 < LocalDateTime.parse(l.get(i).get(0).getAttributes().get(TIMESTAMP).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).compareTo(LocalDateTime.parse(l.get(i + 1).get(0).getAttributes().get(TIMESTAMP).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME))) {
				return false;
			}
		}

		return true;
	}

	public static boolean isSorted(XTrace t) {

		for (int i = 0; i < t.size() - 1; i++) {
			if (0 < LocalDateTime.parse(t.get(i).getAttributes().get(TIMESTAMP).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).compareTo(LocalDateTime.parse(t.get(i + 1).getAttributes().get(TIMESTAMP).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME))) {
				return false;
			}
		}

		return true;
	}

	private static List<XLog> load(String path, PrintStream ps) throws IOException {

		final PrintStream psOrig = System.out;
		System.setOut(ps);
		System.setErr(ps);

		final File file = read(path);
		List<XLog> xLogs = null;
		final XesXmlParser xesXmlParser = new XesXmlParser();
		final XesXmlGZIPParser xesXmlGZIPParser = new XesXmlGZIPParser();

		if (xesXmlParser.canParse(file)) {

			xLogs = parse(file, xesXmlParser);

		} else if (xesXmlGZIPParser.canParse(file)) {

			xLogs = parse(file, xesXmlGZIPParser);

		} else {
			throw new IOException(ERROR + " file format can't be parsed");
		}

		System.setOut(psOrig);
		System.setErr(psOrig);

		return xLogs;
	}

	public static XLog loadXES(String logPath) throws IOException {

		return loadXES(logPath, true, System.out);
	}

	public static XLog loadXES(String logPath, boolean sortByTimeStamp) throws IOException {

		return loadXES(logPath, sortByTimeStamp, System.out);
	}

	public static XLog loadXES(String logPath, boolean sortByTimeStamp, PrintStream ps) throws IOException {

		final File f = new File(logPath);

		if (f.isFile()) {

			if (logPath.toLowerCase().endsWith("xes")) {

				if (canParse(logPath)) {

					final XLog log = load(logPath, ps).get(0);

					if (sortByTimeStamp) {
						sortByTimeStamp(log);
					}

					return log;

				} else {
					throw new IOException(ERROR + " cannot parse file: " + logPath);
				}

			} else {
				throw new IOException(ERROR + " wrong extension: " + logPath);
			}

		} else {
			throw new IOException(ERROR + " not a file: " + logPath);
		}
	}

	private static List<XLog> parse(File file, XesXmlGZIPParser parser) throws IOException {

		List<XLog> xLogs = null;

		try {
			xLogs = parser.parse(file);
		} catch (final Exception e) {
			throw new IOException(ERROR + " problem parsing file (GZIP): " + file.getAbsolutePath());
		}

		return xLogs;
	}

	private static List<XLog> parse(File file, XesXmlParser parser) throws IOException {

		List<XLog> xLogs = null;

		try {
			xLogs = parser.parse(file);
		} catch (final Exception e) {
			throw new IOException(ERROR + " problem parsing file: " + file.getAbsolutePath());
		}

		return xLogs;
	}

	private static File read(String path) throws IOException {

		File file = null;

		try {
			file = new File(path);
		} catch (final NullPointerException e) {
			throw new IOException(ERROR + " problem loading file: " + path);
		}

		return file;
	}

	public static void saveFile(XLog log, File f) throws IOException {

		f.getParentFile().mkdirs();
		f.createNewFile();

		final XesXmlSerializer xesSerial = new XesXmlSerializer();
		xesSerial.serialize(log, new FileOutputStream(f));
	}

	public static void saveFile(XLog log, String path) throws IOException {

		final File f = new File(path);

		f.getParentFile().mkdirs();
		f.createNewFile();

		final XesXmlSerializer xesSerial = new XesXmlSerializer();
		xesSerial.serialize(log, new FileOutputStream(f));
	}

	public static void sortByTimeStamp(XLog l) {

		for (final XTrace t : l) {
			sortByTimeStamp(t);
		}

		l.sort((x, y) -> LocalDateTime.parse(x.get(0).getAttributes().get(TIMESTAMP).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).compareTo(LocalDateTime.parse(y.get(0).getAttributes().get(TIMESTAMP).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
	}

	public static void sortByTimeStamp(XTrace t) {

		t.sort((x, y) -> LocalDateTime.parse(x.getAttributes().get(TIMESTAMP).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).compareTo(LocalDateTime.parse(y.getAttributes().get(TIMESTAMP).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
	}

	public static XTrace toXtrace(List<Object> lblTrace, XFactory xFactory) {

		final List<String> strTrace = new ArrayList<>();

		for (final Object e : lblTrace) {
			strTrace.add(e.toString());
		}

		return toXTrace(strTrace, xFactory);
	}

	public static XTrace toXTrace(List<String> strTrace, XFactory xFactory) {

		XTrace xTrace;

		final XAttributeMap map = new XAttributeMapImpl();
		if (strTrace.isEmpty()) {
			xTrace = new XTraceImpl(map);
		} else {
			xTrace = new XTraceImpl(map);
		}

		for (final String s : strTrace) {

			final XAttributeLiteral label = new XAttributeLiteralImpl(CONCEPTNAME, s);
			final Instant now = Instant.now();
			final XAttributeTimestamp timestamp = new XAttributeTimestampImpl(TIMESTAMP, Date.from(now));
			final XAttributeMap xMap = new XAttributeMapImpl();
			xMap.put(CONCEPTNAME, label);
			xMap.put(TIMESTAMP, timestamp);
			final XEvent xEvent = new XEventImpl(xMap);

			xTrace.add(xEvent);
		}

		return xTrace;
	}

	// TODO: Improve XEvent parsing
	public static String xEventName(XEvent event) throws IOException {

		final XAttributeMap xaMap = event.getAttributes();

		if (xaMap.containsKey(CONCEPTNAME)) {
			final XAttribute xa = xaMap.get(CONCEPTNAME);
			return xa.toString();
		} else {
			throw new IOException(ERROR + " cannot find '" + CONCEPTNAME + "' entry for XEvent");
		}
	}

	public static String xTraceID(XTrace trace) throws IOException {

		final XAttributeMap xaMap = trace.getAttributes();

		if (xaMap.containsKey(CONCEPTNAME)) {
			final XAttribute xa = xaMap.get(CONCEPTNAME);
			return xa.toString();
		} else {
			throw new IOException(ERROR + " cannot find '" + CONCEPTNAME + "' entry for XTrace");
		}
	}

	public static String xTraceTimeStamp(XTrace trace) throws IOException {

		final XAttributeMap xaMap = trace.get(0).getAttributes();

		if (xaMap.containsKey(TIMESTAMP)) {
			final XAttribute xa = xaMap.get(TIMESTAMP);
			return xa.toString();
		} else {
			throw new IOException(ERROR + " cannot find '" + TIMESTAMP + "' entry for XEvent");
		}
	}

	public static String xTraceToString(final XTrace trace) throws IOException {

		String traceAsString = "$";

		for (final XEvent event : trace) {
			if (event != null) {
				traceAsString += xEventName(event);
			}
		}

		return traceAsString;
	}
}