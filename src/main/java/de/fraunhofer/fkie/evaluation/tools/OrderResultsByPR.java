package de.fraunhofer.fkie.evaluation.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.fraunhofer.fkie.aidpfm.utilities.Datasets;
import de.fraunhofer.fkie.aidpfm.utilities.Util;

public class OrderResultsByPR {

	public static void main(final String[] args) throws IOException {
		for (Datasets data : Datasets.values()) {
			BufferedReader reader = Util.readDataFile(Config.RESULT + data.name + "Verification.csv");
			String line = reader.readLine();
			List<Double> recallValues = new ArrayList<>();
			List<Double> precisionValues = new ArrayList<>();
			List<Double> f1Values = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) {
					break;
				}
				// Hard coded to the current order!!
				String precisionString = line.split(";")[15];
				String recallString = line.split(";")[13];
				String f1String = line.split(";")[11];
				recallValues.add(Double.parseDouble(recallString));
				precisionValues.add(Double.parseDouble(precisionString));
				f1Values.add(Double.parseDouble(f1String));
			}
			List<Double> orderPR = new ArrayList<>();
			List<Double> f1Order = new ArrayList<>();
			List<Double> recallOrder = new ArrayList<>();
			List<Double> precisionOrder = new ArrayList<>();
			for (int i = 0; i < recallValues.size(); i++) {
				int prCount = 0;
				int f1Count = 0;
				int recallCount = 0;
				int precisionCount = 0;
				for (int j = 0; j < recallValues.size(); j++) {
					if (recallValues.get(i) > recallValues.get(j)) {
						recallCount++;
					}
					if (precisionValues.get(i) > precisionValues.get(j)) {
						precisionCount++;
					}
					if (f1Values.get(i) > f1Values.get(j)) {
						f1Count++;
					}
					if (recallValues.get(i) > recallValues.get(j) && precisionValues.get(i) > precisionValues.get(j)) {
						prCount++;
					} else if (recallValues.get(i) > recallValues.get(j)
							&& precisionValues.get(i) == precisionValues.get(j)) {
						prCount++;
					}

					else if (recallValues.get(i) == recallValues.get(j)
							&& precisionValues.get(i) > precisionValues.get(j)) {
						prCount++;
					}
				}
				orderPR.add((double) prCount / recallValues.size());
				f1Order.add((double) f1Count / recallValues.size());
				recallOrder.add((double) recallCount / recallValues.size());
				precisionOrder.add((double) precisionCount / recallValues.size());
			}

			reader = Util.readDataFile(Config.RESULT + data.name + "Verification.csv");
			StringBuilder text = new StringBuilder();
			line = reader.readLine();
			String header = line.replace("\n", "");
			header += ";F1Order;RecallOrder;PrecisionOrder;PROrder\n";
			text.append(header);
			int i = 0;
			while ((line = reader.readLine()) != null && i < recallValues.size()) {
				String lineInfo = line.replace("\n", "");
				lineInfo += ";" + Util.round(f1Order.get(i)) + ";" + Util.round(recallOrder.get(i)) + ";"
						+ Util.round(precisionOrder.get(i)) + ";" + Util.round(orderPR.get(i)) + "\n";
				text.append(lineInfo);
				i++;
			}
			Util.writeUtf8File(Config.RESULT + "/" + data.name + "WithOrder.csv", text.toString());
		}
	}
}
