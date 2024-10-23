package ch.vilki.secured;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CsvReader {

	public static final Logger logger = LoggerFactory.getLogger(CsvReader.class);

	public static String ENCODING = "iso-8859-1";
	public static String SEPARATOr = ";";

	private ArrayList<HashMap<String, ArrayList<String>>> _parsedEntries = null;
	private List<String> _columnNames = new ArrayList<>();

	public ArrayList<HashMap<String, ArrayList<String>>> getParsedEntries() {
		return _parsedEntries;
	}

	public void readCSVFile(String fileName, String encoding) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e1) {
			logger.error("Could not find the file", e1);
			return;
		}
		String code = null;
		if (encoding != null) code = encoding;
		else code = ENCODING;

		String sCurrentLine;

		_parsedEntries = new ArrayList<HashMap<String, ArrayList<String>>>();
		HashMap<Integer, String> csvMapping = new HashMap<Integer, String>();
		int globalLineSize = -1;
		logger.info("Resolve CSV Mapping now");
		try {
			sCurrentLine = br.readLine();
			String line = new String(sCurrentLine.getBytes(), Charset.forName(code));
			globalLineSize = line.length() - line.replace(";", "").length();
			String[] keys = line.split(";");
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i].toLowerCase();
				csvMapping.put(i, key);
			}

		} catch (IOException e1) {
			logger.error("Exception occured during resolving of csv mapping->" + fileName, e1);
		}

		try {
			while ((sCurrentLine = br.readLine()) != null) {
				String line = new String(sCurrentLine.getBytes(), Charset.forName(code));
				String lineReady = null;
				int currentLineSize = line.length() - line.replace(";", "").length();
				int endLineCheck = 0;
				boolean done = false;
				if (currentLineSize != globalLineSize) {
					logger.info("Found entry which goes over multiple lines->" + line);
					StringBuilder builder = new StringBuilder();
					endLineCheck = line.length() - line.replace("\"", "").length();
					builder.append(line.replace("\"", ""));
					int counted = currentLineSize;

					while (!done) {
						sCurrentLine = br.readLine();

						line = new String(sCurrentLine.getBytes(), Charset.forName(code));
						counted += sCurrentLine.length() - sCurrentLine.replace(";", "").length();
						endLineCheck += line.length() - line.replace("\"", "").length();
						builder.append(line.replace("\"", ""));
						builder.append("\n");
						if (counted == globalLineSize && endLineCheck % 2 == 0) done = true;
					}
					lineReady = builder.toString();
				} else {
					lineReady = line;
				}

				String[] values = lineReady.split(";");
				HashMap<String, ArrayList<String>> parsedLine = new HashMap<String, ArrayList<String>>();
				for (int i = 0; i < values.length; i++) {
					String value = values[i];
					ArrayList<String> multiValues = new ArrayList<String>();
					if (value == null || value.equalsIgnoreCase("")) {
						parsedLine.put(csvMapping.get(i), null);
					} else {
						if (value.contains("##")) {
							String[] split = value.split("##");
							for (String s1 : split) {
								multiValues.add(s1);
							}
						} else multiValues.add(value);
					}
					parsedLine.put(csvMapping.get(i), multiValues);
				}
				_parsedEntries.add(parsedLine);
			}
		} catch (IOException e) {
			logger.error("Error occured during parsing of csv file->" + e);

		}
	}

	public static String getSingleValue(String attributeName, HashMap<String, ArrayList<String>> attributes) {
		ArrayList<String> values = attributes.get(attributeName);
		if (values == null) return null;
		if (values.size() != 1) {
			logger.error("Is not single value->" + attributeName);
			return null;
		}
		return values.get(0);
	}

	public String toString() {
		if (getParsedEntries() == null || getParsedEntries().size() == 0) {
			return "NO ENTRIES FOUND \n";
		}
		StringBuilder toString = new StringBuilder();
		for (HashMap<String, ArrayList<String>> line : getParsedEntries()) {
			toString.append("--------------------------------------------------------------- \n");
			for (String key : line.keySet()) {
				toString.append(key);
				toString.append("  ");
				toString.append(line.get(key));

				toString.append("\n");

			}
		}
		return toString.toString();

	}
}
	