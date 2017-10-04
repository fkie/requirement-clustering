package de.fraunhofer.fkie.evaluation.distancefunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;
import mikera.vectorz.Vector;

public class DistanceCalculatorRupp extends DistanceHandler {
	List<Field> ruppFields;
	List<Double> weights;
	Map<Field, Double> valueNormalizers;
	Map<Field, Double> fieldNormalizers;

	public DistanceCalculatorRupp(final List<RequirementVector> vectors) {
		if (vectors != null) {
			this.ruppFields = Util.getRuppFields();
			this.weights = new ArrayList<>();
			this.weights.add(0.9);
			this.weights.add(0.0);
			this.weights.add(0.2);
			this.weights.add(1.0);
			this.weights.add(0.8);
			this.weights.add(0.7);
			this.weights.add(0.1);
			this.valueNormalizers = new HashMap<>();
			this.fieldNormalizers = new HashMap<>();
			this.setNormalizers(vectors);
		}
	}

	private void setNormalizers(final List<RequirementVector> vectors) {
		for (Field field : this.ruppFields) {
			double maxValue = 1.0;
			double maxWords = 1;
			double avg = 0;
			for (RequirementVector vector : vectors) {
				// for (Double value : vector.getContentMap(field).values()) {
				// if (value != 0) {
				// words++;
				// }
				// if (value > maxValue) {
				// maxValue = value;
				// }
				// avg += value;
				// }
				double words = vector.getFieldVectorRepresentation(field).nonZeroCount();
				double max = vector.getFieldVectorRepresentation(field).maxElement();
				if (maxWords < words) {
					maxWords = words;
				}
				if (maxValue < max) {
					maxValue = max;
				}
			}
			if (avg == 0) {
				avg = 1;
			}
			this.fieldNormalizers.put(field, maxWords);
			this.valueNormalizers.put(field, maxValue);
		}
	}

	// @Override
	// public double distance(final RequirementVector v1, final
	// RequirementVector v2) {
	// Map<Field, Double> distances = new HashMap<>();
	// for (Field field : this.ruppFields) {
	// Map<String, Double> v1Normalized =
	// this.normalize(v1.getContentMap(field),
	// this.valueNormalizers.get(field));
	// Map<String, Double> v2Normalized =
	// this.normalize(v2.getContentMap(field),
	// this.valueNormalizers.get(field));
	// double distance = this.distanceFunction.distance(v1Normalized,
	// v2Normalized);
	// distance = distance / (double) this.fieldNormalizers.get(field);
	// // distance = -Math.pow(distance, 2);
	// // double exp = distance/(2*average.get(field));
	// // double score = Math.pow(Math.E, exp);
	// distances.put(field, distance);
	// }
	// double distance = 0;
	// for (int i = 0; i < this.ruppFields.size(); i++) {
	// distance += this.weights.get(i) * distances.get(this.ruppFields.get(i));
	// }
	// // distance = 1-score;
	// return distance;
	// }

	@Override
	public double distanceZ(final RequirementVector v1, final RequirementVector v2) {
		Map<Field, Double> distances = new HashMap<>();
		for (Field field : this.ruppFields) {
			Vector v1Normalized = Vector.create(v1.getFieldVectorRepresentation(field));
			v1Normalized.divide(this.valueNormalizers.get(field));
			Vector v2Normalized = Vector.create(v2.getFieldVectorRepresentation(field));
			v2Normalized.divide(this.valueNormalizers.get(field));
			double distance = this.distanceFunction.distanceZ(v1Normalized, v2Normalized);
			distance = distance / (double) this.fieldNormalizers.get(field);
			distances.put(field, distance);
		}

		double distance = 0;
		for (int i = 0; i < this.ruppFields.size(); i++) {
			distance += this.weights.get(i) * distances.get(this.ruppFields.get(i));
		}
		return distance;
	}

	protected Map<String, Double> normalize(final Map<String, Double> contentMap, final double normalizer) {
		Map<String, Double> normalizedMap = new HashMap<>();

		for (String key : contentMap.keySet()) {
			normalizedMap.put(key, contentMap.get(key) / normalizer);
		}
		return normalizedMap;
	}

	@Override
	public String toString() {
		return "MyNormalized";
	}

}
