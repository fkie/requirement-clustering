package de.fraunhofer.fkie.evaluation.distancefunction;

import java.util.Map;

import mikera.vectorz.Vector;

public class CosineDistance implements DistanceFunction {

	@Override
	public double distance(final Map<String, Double> v1Content, final Map<String, Double> v2Content) {
		double z = 0, n1 = 0, n2 = 0;
		for (String key : v1Content.keySet()) {
			z += v1Content.get(key) * v2Content.get(key);
			n1 += v1Content.get(key) * v1Content.get(key);
			n2 += v2Content.get(key) * v2Content.get(key);
		}
		double result = z;
		if (n1 == n2) {
			result = z / n1;
		} else {
			result = z / (Math.sqrt(n1) * Math.sqrt(n2));
		}
		if (!(result > 0.0)) {
			result = 0.0;
		}

		return (1.0 - result);
	}

	@Override
	public String getName() {
		return "CosineDistance";
	}

	@Override
	public double distanceZ(final Vector v1, final Vector v2) {
		double z = 0, n1 = 0, n2 = 0;
		for (int i = 0; i < v1.length(); i++) {
			z += v1.get(i) * v2.get(i);
			n1 += v1.get(i) * v1.get(i);
			n2 += v2.get(i) * v2.get(i);
		}
		double result = z;
		if (n1 == n2) {
			result = z / n1;
		} else {
			result = z / (Math.sqrt(n1) * Math.sqrt(n2));
		}
		if (!(result > 0.0)) {
			result = 0.0;
		}

		return (1.0 - result);
	}

}
