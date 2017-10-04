package de.fraunhofer.fkie.evaluation.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.fraunhofer.fkie.aidpfm.utilities.Datasets;
import de.fraunhofer.fkie.aidpfm.utilities.Util;

public class ParameterPositions {
	public static void main(final String[] args) throws IOException {
		Map<Datasets, List<String>> lines = new HashMap<>();
		List<String> alphaLines = new ArrayList<>();
		BufferedReader alphaReader = Util.readDataFile(Config.RESULT + Datasets.ALPHA.name + "WithNumber.csv");
		String line = alphaReader.readLine();
		String originalHeader = line;
		// header up to f1
		while ((line = alphaReader.readLine()) != null) {
			if (line.equals("")) {
				continue;
			}
			alphaLines.add(line);
		}
		lines.put(Datasets.ALPHA, alphaLines);

		List<String> betaLines = new ArrayList<>();
		BufferedReader betaReader = Util.readDataFile(Config.RESULT + Datasets.BETA.name + "WithNumber.csv");
		line = betaReader.readLine();
		// header up to f1
		while ((line = betaReader.readLine()) != null) {
			if (line.equals("")) {
				continue;
			}
			betaLines.add(line);
		}
		lines.put(Datasets.BETA, betaLines);

		List<String> gammaLines = new ArrayList<>();
		BufferedReader gammaReader = Util.readDataFile(Config.RESULT + Datasets.GAMMA.name + "WithNumber.csv");
		line = gammaReader.readLine();
		// header up to f1
		while ((line = gammaReader.readLine()) != null) {
			if (line.equals("")) {
				continue;
			}
			gammaLines.add(line);
		}
		lines.put(Datasets.GAMMA, gammaLines);

		List<String> deltaLines = new ArrayList<>();
		BufferedReader deltaReader = Util.readDataFile(Config.RESULT + Datasets.DELTA.name + "WithNumber.csv");
		line = deltaReader.readLine();
		// header up to f1
		while ((line = deltaReader.readLine()) != null) {
			if (line.equals("")) {
				continue;
			}
			deltaLines.add(line);
		}
		lines.put(Datasets.DELTA, deltaLines);

		List<String> zetaLines = new ArrayList<>();
		BufferedReader zetaReader = Util.readDataFile(Config.RESULT + Datasets.ZETA.name + "WithNumber.csv");
		line = zetaReader.readLine();
		// header up to f1
		while ((line = zetaReader.readLine()) != null) {
			if (line.equals("")) {
				continue;
			}
			zetaLines.add(line);
		}
		lines.put(Datasets.ZETA, zetaLines);

		Set<String> checkedNumbers = new HashSet<>();
		Map<Datasets, List<Map.Entry<Double, List<String>>>> f1ToNumberMaps = new HashMap<>();
		for (Datasets data : Datasets.values()) {
			f1ToNumberMaps.put(data, getF1ToNumbers(lines.get(data), 11));
		}

		Map<Datasets, Map<String, Double>> completeNumberToRank = new HashMap<>();
		for (Datasets data : Datasets.values()) {
			completeNumberToRank.put(data, new HashMap<String, Double>());
		}
		for (int i = 0; i < alphaLines.size(); i++) {
			String number = alphaLines.get(i).split(";")[11];
			if (checkedNumbers.contains(number)) {
				continue;
			}
			for (Datasets data : Datasets.values()) {
				completeNumberToRank.get(data).put(number, getAverage(number, f1ToNumberMaps.get(data)));
			}
		}
		Map<Datasets, List<Map.Entry<Double, List<String>>>> f1ToWithoutNumberMaps = new HashMap<>();
		for (Datasets data : Datasets.values()) {
			f1ToWithoutNumberMaps.put(data, getF1ToNumbers(lines.get(data), 12));
		}
		Map<Datasets, Map<String, Double>> withoutNumberToRank = new HashMap<>();
		for (Datasets data : Datasets.values()) {
			withoutNumberToRank.put(data, new HashMap<String, Double>());
		}
		for (int i = 0; i < alphaLines.size(); i++) {
			String number = alphaLines.get(i).split(";")[12];
			if (checkedNumbers.contains(number)) {
				continue;
			}
			for (Datasets data : Datasets.values()) {
				withoutNumberToRank.get(data).put(number, getAverage(number, f1ToWithoutNumberMaps.get(data)));
			}
		}

		Map<Datasets, List<Map.Entry<Double, List<String>>>> f1ToWithNumberMaps = new HashMap<>();
		for (Datasets data : Datasets.values()) {
			f1ToWithNumberMaps.put(data, getF1ToNumbers(lines.get(data), 13));
		}

		Map<Datasets, Map<String, Double>> withNumberToRank = new HashMap<>();
		for (Datasets data : Datasets.values()) {
			withNumberToRank.put(data, new HashMap<String, Double>());
		}
		for (int i = 0; i < alphaLines.size(); i++) {
			String number = alphaLines.get(i).split(";")[13];
			if (checkedNumbers.contains(number)) {
				continue;
			}
			for (Datasets data : Datasets.values()) {
				withNumberToRank.get(data).put(number, getAverage(number, f1ToWithNumberMaps.get(data)));
			}
		}

		// to Text now
		String header = "";
		String[] headerArray = originalHeader.split(";");
		for (int i = 0; i < 12; i++) {
			header += headerArray[i] + ";";
		}
		header += "averagePositionInData;averagePositionInAllData;";
		header += headerArray[12] + ";";
		header += "averagePositionInData;averagePositionInAllData;";
		header += headerArray[13] + ";";
		header += "averagePositionInData;averagePositionInAllData\n";

		for (Datasets data : Datasets.values()) {
			StringBuilder text = new StringBuilder();
			text.append(header);
			for (int i = 0; i < lines.get(data).size(); i++) {
				StringBuilder currentLine = new StringBuilder();
				String[] lineArray = lines.get(data).get(i).split(";");
				for (int j = 0; j < 12; j++) {
					currentLine.append(lineArray[j] + ";");
				}
				currentLine.append(completeNumberToRank.get(data).get(lineArray[11])).append(";")
						.append(average(completeNumberToRank, lineArray[11])).append(";");
				currentLine.append(lineArray[12]).append(";");
				currentLine.append(withoutNumberToRank.get(data).get(lineArray[12])).append(";")
						.append(average(withoutNumberToRank, lineArray[12])).append(";");
				currentLine.append(lineArray[13]).append(";");
				currentLine.append(withNumberToRank.get(data).get(lineArray[13])).append(";")
						.append(average(withNumberToRank, lineArray[13])).append(";");
				currentLine.append("\n");
				text.append(currentLine);
			}
			Util.writeUtf8File(Config.RESULT + "/" + data.name + "WithPosition1.csv", text.toString());
		}
	}

	private static Object average(final Map<Datasets, Map<String, Double>> numberToRank, final String string) {
		double average = 0;
		for (Datasets data : Datasets.values()) {
			average += numberToRank.get(data).get(string);
		}
		return average / Datasets.values().length;
	}

	private static double getAverage(final String number, final List<Map.Entry<Double, List<String>>> f1ToNumbers) {
		double average = 0;
		double divider = 0;
		for (int i = 0; i < f1ToNumbers.size(); i++) {
			List<String> values = f1ToNumbers.get(i).getValue();
			for (int j = 0; j < values.size(); j++) {
				if (values.get(j).equals(number)) {
					// i is current rank
					average += i;
					divider++;
				}
			}
		}
		average = average / divider;
		return average;
	}

	private static List<Map.Entry<Double, List<String>>> getF1ToNumbers(final List<String> lines, final int x) {
		Map<Double, List<String>> f1ToNumbers = new HashMap<>();
		for (int i = 0; i < lines.size(); i++) {
			Double key = Double.parseDouble(lines.get(i).split(";")[10]);
			String value = lines.get(i).split(";")[x];
			if (f1ToNumbers.containsKey(key)) {
				f1ToNumbers.get(key).add(value);
			} else {
				List<String> values = new ArrayList<>();
				values.add(value);
				f1ToNumbers.put(key, values);
			}

		}
		List<Map.Entry<Double, List<String>>> entries = new ArrayList<>(f1ToNumbers.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<Double, List<String>>>() {
			@Override
			public int compare(final Map.Entry<Double, List<String>> v1, final Map.Entry<Double, List<String>> v2) {
				return v1.getKey().compareTo(v2.getKey());
			}
		});
		Collections.reverse(entries);

		return entries;
	}
}
