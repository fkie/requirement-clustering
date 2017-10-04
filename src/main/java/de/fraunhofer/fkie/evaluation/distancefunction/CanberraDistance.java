package de.fraunhofer.fkie.evaluation.distancefunction;

import java.util.Map;

import mikera.vectorz.Vector;

public class CanberraDistance implements DistanceFunction {

	@Override
	public String getName() {
		return "CanberraDistance";
	}

	@Override
	public double distance(final Map<String, Double> v1Content, final Map<String, Double> v2Content) {
		double dist = 0;
		for (String key : v1Content.keySet()) {
			double v1Value = v1Content.get(key), v2Value = v2Content.get(key);
			if (v1Value + v2Value != 0) {
				double dimensionDistance = Math.abs(v1Value - v2Value);
				dimensionDistance = dimensionDistance / (v1Value + v2Value);
				dist += dimensionDistance;
			}
		}
		return dist;
	}

	@Override
	public double distanceZ(final Vector v1, final Vector v2) {
		double dist = 0;
		for (int i = 0; i < v1.length(); i++) {
			double v1Value = v1.get(i), v2Value = v2.get(i);
			if (v1Value + v2Value != 0) {
				double dimensionDistance = Math.abs(v1Value - v2Value);
				dimensionDistance = dimensionDistance / (v1Value + v2Value);
				dist += dimensionDistance;
			}
		}
		return Math.sqrt(dist);
	}

}
