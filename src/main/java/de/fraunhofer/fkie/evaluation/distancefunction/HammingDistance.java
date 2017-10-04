package de.fraunhofer.fkie.evaluation.distancefunction;

import java.util.Map;

import mikera.vectorz.Vector;

public class HammingDistance implements DistanceFunction {

	@Override
	public String getName() {
		return "HammingDistance";
	}

	@Override
	public double distance(final Map<String, Double> v1Content, final Map<String, Double> v2Content) {
		double dist = 0;
		for (String key : v1Content.keySet()) {
			double x = v1Content.get(key);
			double y = v2Content.get(key);
			if (x != y) {
				dist++;
			}
		}
		return dist;
	}

	@Override
	public double distanceZ(final Vector v1, final Vector v2) {
		double dist = 0;
		for (int i = 0; i < v1.length(); i++) {
			if (v1.get(i) != v2.get(i)) {
				dist++;
			}
		}
		return dist;
	}

}
