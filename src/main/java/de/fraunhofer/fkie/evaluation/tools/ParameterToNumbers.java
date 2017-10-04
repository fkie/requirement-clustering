package de.fraunhofer.fkie.evaluation.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.fraunhofer.fkie.aidpfm.utilities.Datasets;
import de.fraunhofer.fkie.aidpfm.utilities.Util;

public class ParameterToNumbers {
	public static void main(final String[] args) throws IOException {
		List<String> lines = new ArrayList<>();
		BufferedReader reader = Util.readDataFile(Config.RESULT + Datasets.ALPHA.name + "Verification.csv");
		String line = reader.readLine();
		// header up to f1
		String header = "";
		String[] lineArray = line.split(";");
		for (int i = 0; i < 11; i++) {
			header += lineArray[i] + ";";
		}
		header += "completeSettings;withoutClusterDistance;ClusterDistance";
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		// go through all parameters
		List<Set<String>> sets = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Set<String> thisParameterSettings = new HashSet<>();
			for (String settingLine : lines) {
				if (settingLine.equals("")) {
					continue;
				}
				String setting = settingLine.split(";")[i];
				if (i == 0) {
					setting = setting.replaceAll("[^A-Za-z]", "");
				}
				thisParameterSettings.add(setting);
			}
			sets.add(thisParameterSettings);
		}

		for (Datasets data : Datasets.values()) {
			List<String> dataLines = new ArrayList<>();
			BufferedReader dataReader = Util.readDataFile(Config.RESULT + data.name + "Verification.csv");
			String dataLine = dataReader.readLine();
			while ((dataLine = dataReader.readLine()) != null) {
				dataLines.add(dataLine);
			}
			// translate line to number
			List<Integer[]> lineNumbers = new ArrayList<>();
			for (String settingsLine : dataLines) {
				if (settingsLine.equals("")) {
					continue;
				}
				String[] settings = settingsLine.split(";");
				Integer[] toNumber = new Integer[10];
				for (int i = 0; i < 10; i++) {
					List<String> settingsList = new ArrayList<String>(sets.get(i));
					if (i == 0) {
						settings[i] = settings[i].replaceAll("[^A-Za-z]", "");
					}
					toNumber[i] = settingsList.indexOf(settings[i]) + 1;
				}
				lineNumbers.add(toNumber);
			}

			StringBuilder text = new StringBuilder();
			line = reader.readLine();
			text.append(header + "\n");
			for (int i = 0; i < lineNumbers.size(); i++) {
				StringBuilder printLine = new StringBuilder();
				String lineInfo = dataLines.get(i);
				Integer[] toNumber = lineNumbers.get(i);
				if (lineInfo.equals("")) {
					System.out.println("");
				}
				StringBuilder upToF1 = new StringBuilder();
				for (int j = 0; j < 11; j++) {
					upToF1.append(lineInfo.split(";")[j] + ";");
				}
				printLine.append(upToF1);
				StringBuilder completeSettings = new StringBuilder();
				for (int j = 0; j < toNumber.length; j++) {
					completeSettings.append(toNumber[j]);
				}
				printLine.append(completeSettings + ";");

				StringBuilder withoutClusterDistanceSettings = new StringBuilder();
				for (int j = 2; j < 10; j++) {
					withoutClusterDistanceSettings.append(toNumber[j]);
				}
				printLine.append(withoutClusterDistanceSettings + ";");

				StringBuilder clusterDistanceSettings = new StringBuilder();
				for (int j = 0; j < 2; j++) {
					clusterDistanceSettings.append(toNumber[j]);
				}
				printLine.append(clusterDistanceSettings + ";");
				text.append(printLine + "\n");
			}
			Util.writeUtf8File(Config.RESULT + "/" + data.name + "WithNumber.csv", text.toString());
		}
	}
}
