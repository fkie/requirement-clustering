package de.fraunhofer.fkie.evaluation.clusterfunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.Main;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceHandler;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;
import mikera.vectorz.Vector;

public class NeuralGas implements ClusterFunction {
	private int k;
	private double eps_i = 0.3, eps_f = 0.05;
	private double lambda_i = 30, lambda_f = 0.01;
	private List<Integer> randomNumbers = null;
	private int numberSeed;
	Random random = new Random(100);

	public NeuralGas(final int k) {
		this.k = k;
	}

	@Override
	public String getName() {
		return "Neural Gas";
	}

	public int getK() {
		return this.k;
	}

	public void setRandom(final Random random) {
		this.random = random;
	}

	public void setK(final int k) {
		this.k = k;
	}

	public void setRandomFile(final int x, final boolean verification) throws IOException {
		if (!verification) {
			this.randomNumbers = Util.getRandomNumbersFromFile(x, Main.DATASET.name);
		} else {
			this.randomNumbers = Util.getRandomNumbersFromFile(x, Main.DATASET.name + "Verification");
		}
		this.numberSeed = x;
	}

	// used by clustering interface
	@Override
	public List<List<RequirementVector>> cluster(final List<RequirementVector> vectors,
			final DistanceHandler distanceHandler, final List<Integer> givenCenters) {
		// contains all clusters: cluster is a list of Requirement Vectors ->
		// clusters has k elements
		List<List<RequirementVector>> clusters = new LinkedList<List<RequirementVector>>();
		for (int i = 0; i < this.k; i++) {
			List<RequirementVector> c_i = new LinkedList<RequirementVector>();
			clusters.add(c_i);
		}
		// end of cluster init

		// start of center init
		Map<RequirementVector, Double> clusterCenters = new LinkedHashMap<>();

		// normal init************************************************
		// start by going through the list given by the user
		List<Integer> noDoubleCenters = new ArrayList<Integer>();
		if (givenCenters != null) {
			for (int i = 0; i < givenCenters.size() && i < this.k; i++) {
				String id = "" + givenCenters.get(i);
				boolean found = false;
				for (int j = 0; j < vectors.size(); j++) {
					if (vectors.get(j).getID().equals(id)) {
						found = true;
						clusterCenters.put(vectors.get(j), 0.0);
						noDoubleCenters.add(j);
						break;
					}
				}
				if (!found) {
					throw new RuntimeException("Not a valid object id. Can not use it as cluster center");
				}
			}
		}
		// now fill rest with random centers
		for (int i = 0; i < this.k; i++) {
			int centerIndex = this.random.nextInt(vectors.size());
			while (noDoubleCenters.contains(centerIndex)) {
				centerIndex = this.random.nextInt(vectors.size());
			}
			noDoubleCenters.add(centerIndex);
			clusterCenters.put(vectors.get(centerIndex), 0.0);
		}
		// ***********************************************************
		// end of center init
		Map<RequirementVector, Double> oldCentroids = clusterCenters;
		for (int i = 0; i < vectors.size(); i++) {
			// calc eps and lamb
			double epsilon = this.eps_i * Math.pow((this.eps_f / this.eps_i), i / vectors.size());
			double lambda = this.lambda_i * Math.pow((this.lambda_f / this.lambda_i), i / vectors.size());
			RequirementVector v1 = vectors.get(i);
			// determine distance to each representative
			for (Entry<RequirementVector, Double> entry : oldCentroids.entrySet()) {
				double dist = distanceHandler.distanceZ(v1, entry.getKey());
				entry.setValue(dist);
			}
			// sort the list
			List<Map.Entry<RequirementVector, Double>> entries = new ArrayList<>(oldCentroids.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<RequirementVector, Double>>() {
				@Override
				public int compare(final Map.Entry<RequirementVector, Double> v1,
						final Map.Entry<RequirementVector, Double> v2) {
					return v1.getValue().compareTo(v2.getValue());
				}
			});
			// finished sort

			// now adapt the representatives
			LinkedHashMap<RequirementVector, Double> newCentroids = new LinkedHashMap<>();
			for (int j = 0; j < entries.size(); j++) {
				// go trhough each entry
				RequirementVector v2 = entries.get(j).getKey();
				Vector newCenter = Vector.create(v2.getVectorRepresentation());
				for (int l = 0; l < newCenter.length(); l++) {
					double newValue = v2.getVectorRepresentation().get(l);
					newValue += epsilon * Math.pow(Math.E, (-j / lambda))
							* (v1.getVectorRepresentation().get(l) - v2.getVectorRepresentation().get(l));
					newCenter.set(l, newValue);

				}
				Map<Field, Vector> fieldVector = new HashMap<>();

				for (Field field : Util.getRuppFields()) {
					Vector newFieldCenter = Vector.create(v2.getFieldVectorRepresentation(field));

					for (int l = 0; l < newFieldCenter.length(); l++) {
						double newValue = newFieldCenter.get(l);
						newValue += epsilon * Math.pow(Math.E, (-j / lambda))
								* (v1.getVectorRepresentation().get(l) - newFieldCenter.get(l));
						newFieldCenter.set(l, newValue);

					}
					fieldVector.put(field, newFieldCenter);
				}
				newCentroids.put(new RequirementVector(newCenter, fieldVector, "-1"), 0.0);
			}
			oldCentroids = newCentroids;
		}

		// final assignment
		for (RequirementVector v1 : vectors) {
			double minDist = Double.MAX_VALUE;
			int clusterIndex = -1;
			int i = 0;
			for (Entry<RequirementVector, Double> entry : oldCentroids.entrySet()) {
				double dist = distanceHandler.distanceZ(v1, entry.getKey());
				if (dist < minDist) {
					minDist = dist;
					clusterIndex = i;
				}
				i++;
			}
			if (clusterIndex == -1) {
				Random ran = new Random();
				clusterIndex = ran.nextInt(this.k);
			}
			clusters.get(clusterIndex).add(v1);
		}

		return clusters;
	}

	// this method is used by the evaluation framework
	@Override
	public List<List<RequirementVector>> evaluationCluster(final List<RequirementVector> vectors,
			final DistanceHandler distanceHandler) {
		// contains all clusters: cluster is a list of Requirement Vectors ->
		// clusters has k elements
		List<List<RequirementVector>> clusters = new LinkedList<List<RequirementVector>>();
		for (int i = 0; i < this.k; i++) {
			List<RequirementVector> c_i = new LinkedList<RequirementVector>();
			clusters.add(c_i);
		}
		// end of cluster init

		// start of center init
		Map<RequirementVector, Double> clusterCenters = new LinkedHashMap<>();

		// normal init************************************************
		// each one has their own random number seed
		List<Integer> noDoubleCenters = new ArrayList<Integer>();

		Iterator<Integer> random = this.randomNumbers.iterator();
		for (int i = 0; i < this.k; i++) {
			int centerIndex = random.next();
			while (noDoubleCenters.contains(centerIndex)) {
				centerIndex = random.next();
			}
			noDoubleCenters.add(centerIndex);
			clusterCenters.put(vectors.get(centerIndex), 0.0);
		}
		// ***********************************************************
		// end of center init
		Map<RequirementVector, Double> oldCentroids = clusterCenters;
		for (int i = 0; i < vectors.size(); i++) {
			// calc eps and lamb
			double epsilon = this.eps_i * Math.pow((this.eps_f / this.eps_i), i / vectors.size());
			double lambda = this.lambda_i * Math.pow((this.lambda_f / this.lambda_i), i / vectors.size());
			RequirementVector v1 = vectors.get(i);
			// determine distance to each representative
			for (Entry<RequirementVector, Double> entry : oldCentroids.entrySet()) {
				double dist = distanceHandler.distanceZ(v1, entry.getKey());
				entry.setValue(dist);
			}
			// sort the list
			List<Map.Entry<RequirementVector, Double>> entries = new ArrayList<>(oldCentroids.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<RequirementVector, Double>>() {
				@Override
				public int compare(final Map.Entry<RequirementVector, Double> v1,
						final Map.Entry<RequirementVector, Double> v2) {
					return v1.getValue().compareTo(v2.getValue());
				}
			});
			// finished sort

			// now adapt the representatives
			LinkedHashMap<RequirementVector, Double> newCentroids = new LinkedHashMap<>();
			for (int j = 0; j < entries.size(); j++) {
				// go trhough each entry
				RequirementVector v2 = entries.get(j).getKey();
				Vector newCenter = Vector.create(v2.getVectorRepresentation());
				for (int l = 0; l < newCenter.length(); l++) {
					double newValue = v2.getVectorRepresentation().get(l);
					newValue += epsilon * Math.pow(Math.E, (-j / lambda))
							* (v1.getVectorRepresentation().get(l) - v2.getVectorRepresentation().get(l));
					newCenter.set(l, newValue);

				}
				Map<Field, Vector> fieldVector = new HashMap<>();

				for (Field field : Util.getRuppFields()) {
					Vector newFieldCenter = Vector.create(v2.getFieldVectorRepresentation(field));

					for (int l = 0; l < newFieldCenter.length(); l++) {
						double newValue = newFieldCenter.get(l);
						newValue += epsilon * Math.pow(Math.E, (-j / lambda))
								* (v1.getVectorRepresentation().get(l) - newFieldCenter.get(l));
						newFieldCenter.set(l, newValue);

					}
					fieldVector.put(field, newFieldCenter);
				}
				newCentroids.put(new RequirementVector(newCenter, fieldVector, "-1"), 0.0);
			}
			oldCentroids = newCentroids;
		}

		// final assignment
		for (RequirementVector v1 : vectors) {
			double minDist = Double.MAX_VALUE;
			int clusterIndex = -1;
			int i = 0;
			for (Entry<RequirementVector, Double> entry : oldCentroids.entrySet()) {
				double dist = distanceHandler.distanceZ(v1, entry.getKey());
				if (dist < minDist) {
					minDist = dist;
					clusterIndex = i;
				}
				i++;
			}
			if (clusterIndex == -1) {
				Random ran = new Random();
				clusterIndex = ran.nextInt(this.k);
			}
			clusters.get(clusterIndex).add(v1);
		}

		return clusters;
	}

	public List<List<RequirementVector>> evaluationClusterZ(final List<RequirementVector> vectors,
			final DistanceHandler distanceHandler) {
		// contains all clusters: cluster is a list of Requirement Vectors ->
		// clusters has k elements
		List<List<RequirementVector>> clusters = new LinkedList<List<RequirementVector>>();
		for (int i = 0; i < this.k; i++) {
			List<RequirementVector> c_i = new LinkedList<RequirementVector>();
			clusters.add(c_i);
		}
		// end of cluster init

		// start of center init
		Map<RequirementVector, Double> clusterCenters = new LinkedHashMap<>();

		// normal init************************************************
		// each one has their own random number seed
		List<Integer> noDoubleCenters = new ArrayList<Integer>();

		Iterator<Integer> random = this.randomNumbers.iterator();
		for (int i = 0; i < this.k; i++) {
			int centerIndex = random.next();
			while (noDoubleCenters.contains(centerIndex)) {
				centerIndex = random.next();
			}
			noDoubleCenters.add(centerIndex);
			clusterCenters.put(vectors.get(centerIndex), 0.0);
		}
		// ***********************************************************
		// end of center init
		Map<RequirementVector, Double> oldCentroids = clusterCenters;
		for (int i = 0; i < vectors.size(); i++) {
			// calc eps and lamb
			double epsilon = this.eps_i * Math.pow((this.eps_f / this.eps_i), i / vectors.size());
			double lambda = this.lambda_i * Math.pow((this.lambda_f / this.lambda_i), i / vectors.size());
			RequirementVector v1 = vectors.get(i);
			// determine distance to each representative
			for (Entry<RequirementVector, Double> entry : oldCentroids.entrySet()) {
				double dist = distanceHandler.distanceZ(v1, entry.getKey());
				entry.setValue(dist);
			}
			// sort the list
			List<Map.Entry<RequirementVector, Double>> entries = new ArrayList<>(oldCentroids.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<RequirementVector, Double>>() {
				@Override
				public int compare(final Map.Entry<RequirementVector, Double> v1,
						final Map.Entry<RequirementVector, Double> v2) {
					return v1.getValue().compareTo(v2.getValue());
				}
			});
			// finished sort

			// now adapt the representatives
			LinkedHashMap<RequirementVector, Double> newCentroids = new LinkedHashMap<>();
			for (int j = 0; j < entries.size(); j++) {
				// go trhough each entry
				RequirementVector v2 = entries.get(j).getKey();
				Vector newCenter = Vector.create(v2.getVectorRepresentation());
				for (int l = 0; l < newCenter.length(); l++) {
					double newValue = v2.getVectorRepresentation().get(l);
					newValue += epsilon * Math.pow(Math.E, (-j / lambda))
							* (v1.getVectorRepresentation().get(l) - v2.getVectorRepresentation().get(l));
					newCenter.set(l, newValue);

				}
				Map<Field, Vector> fieldVector = new HashMap<>();

				for (Field field : Util.getRuppFields()) {
					Vector newFieldCenter = Vector.create(v2.getFieldVectorRepresentation(field));

					for (int l = 0; l < newFieldCenter.length(); l++) {
						double newValue = newFieldCenter.get(l);
						newValue += epsilon * Math.pow(Math.E, (-j / lambda))
								* (v1.getVectorRepresentation().get(l) - newFieldCenter.get(l));
						newFieldCenter.set(l, newValue);

					}
					fieldVector.put(field, newFieldCenter);
				}
				newCentroids.put(new RequirementVector(newCenter, fieldVector, "-1"), 0.0);
			}
			oldCentroids = newCentroids;
		}

		// final assignment
		for (RequirementVector v1 : vectors) {
			double minDist = Double.MAX_VALUE;
			int clusterIndex = -1;
			int i = 0;
			for (Entry<RequirementVector, Double> entry : oldCentroids.entrySet()) {
				double dist = distanceHandler.distanceZ(v1, entry.getKey());
				if (dist < minDist) {
					minDist = dist;
					clusterIndex = i;
				}
				i++;
			}
			if (clusterIndex == -1) {
				Random ran = new Random();
				clusterIndex = ran.nextInt(this.k);
			}
			clusters.get(clusterIndex).add(v1);
		}

		return clusters;
	}
	// private List<List<RequirementVector>>
	// sortClusters(List<List<RequirementVector>> clusters,
	// List<RequirementVector> centers) {
	// List<List<RequirementVector>> toReturn = new ArrayList<>();
	// DistanceFunction distanceFunction = new EuclideanDistance();
	//
	// for(int i = 0; i < clusters.size(); i++){
	// List<RequirementVector> cluster = clusters.get(i);
	// RequirementVector center = centers.get(i);
	// Map<RequirementVector, Double> pointDistance = new LinkedHashMap<>();
	// for(RequirementVector v1 : cluster){
	// double dist = distanceFunction.distance(v1, center);
	// pointDistance.put(v1, dist);
	// }
	// // sort the list
	// List<Map.Entry<RequirementVector, Double>> entries = new
	// ArrayList<>(pointDistance.entrySet());
	// Collections.sort(entries, new Comparator<Map.Entry<RequirementVector,
	// Double>>() {
	// public int compare(Map.Entry<RequirementVector, Double> v1,
	// Map.Entry<RequirementVector, Double> v2) {
	// return v1.getValue().compareTo(v2.getValue());
	// }
	// });
	// List<RequirementVector> sortedCluster = new ArrayList<>();
	// for(Map.Entry<RequirementVector, Double> vector : entries){
	// sortedCluster.add(vector.getKey());
	// }
	// toReturn.add(sortedCluster);
	// }
	// return toReturn;
	// }

}
