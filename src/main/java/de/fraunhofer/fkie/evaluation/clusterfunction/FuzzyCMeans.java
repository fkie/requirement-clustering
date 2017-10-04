package de.fraunhofer.fkie.evaluation.clusterfunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.Main;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceHandler;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;
import mikera.vectorz.Vector;

public class FuzzyCMeans implements ClusterFunction {

	public static Logger LOG = LoggerFactory.getLogger(FuzzyCMeans.class);
	private int k;
	private int iterations;
	private int m;
	private double eps;
	private List<Integer> randomNumbers = null;
	// only for debugging purpose
	private int numberSeed;

	public FuzzyCMeans(final int k, final int iterations, final int m) {
		this.k = k;
		this.iterations = iterations;
		this.m = m;
		this.eps = 0.1;
	}

	public FuzzyCMeans(final int k, final int iterations, final int m, final double eps) {
		this.k = k;
		this.iterations = iterations;
		this.m = m;
		this.eps = eps;
	}

	@Override
	public String getName() {
		return "FuzzyCMeans" + (this.k) + (this.iterations);
	}

	public int getK() {
		return this.k;
	}

	public void setK(final int k) {
		this.k = k;
	}

	public int getIterations() {
		return this.iterations;
	}

	public int getM() {
		return this.m;
	}

	public void setM(final int m) {
		this.m = m;
	}

	// read out your specific random number seed
	public void setRandomFile(final int x, final boolean verification) throws IOException {
		if (!verification) {
			this.randomNumbers = Util.getRandomNumbersFromFile(x, Main.DATASET.name);
		} else {
			this.randomNumbers = Util.getRandomNumbersFromFile(x, Main.DATASET.name + "Verification");
		}
		this.numberSeed = x;
	}

	// this is used by the clustering interface
	@Override
	public List<List<RequirementVector>> cluster(final List<RequirementVector> vectors,
			final DistanceHandler distanceHandler, final List<Integer> givenCenters) {
		// contains all clusters: cluster is a list of Requirement Vectors ->
		// clusters has k elements
		List<Map<RequirementVector, Double>> clusters = new ArrayList<>();
		for (int i = 0; i < this.k; i++) {
			Map<RequirementVector, Double> c_i = new HashMap<>();
			clusters.add(c_i);
		}
		// end of cluster init

		// start of center init
		List<RequirementVector> clusterCenters = new ArrayList<RequirementVector>();
		// init****************************************************************
		// start with reading out the list given by the user
		List<Integer> noDoubleCenters = new ArrayList<Integer>();
		if (givenCenters != null) {
			for (int i = 0; i < givenCenters.size() && i < this.k; i++) {
				String id = "" + givenCenters.get(i);
				boolean found = false;
				for (int j = 0; j < vectors.size(); j++) {
					if (vectors.get(j).getID().equals(id)) {
						found = true;
						clusterCenters.add(vectors.get(j));
						noDoubleCenters.add(j);
						break;
					}
				}
				if (!found) {
					throw new RuntimeException("Not a valid object id. Can not use it as cluster center");
				}
			}
		}
		// finished reading list, now rest random
		for (int i = clusterCenters.size(); i < this.k; i++) {
			Random randomGenerator = new Random();
			int centerIndex = randomGenerator.nextInt(vectors.size());
			while (noDoubleCenters.contains(centerIndex)) {
				centerIndex = randomGenerator.nextInt(vectors.size());
			}
			noDoubleCenters.add(centerIndex);
			clusterCenters.add(vectors.get(centerIndex));
		}

		// *****************************************************************************
		// end of center init
		List<RequirementVector> oldCentroids = clusterCenters;

		for (int i = 0; i < this.iterations; i++) {
			List<Map<RequirementVector, Double>> currentIterationClusters = new ArrayList<>();
			for (int j = 0; j < this.k; j++) {
				Map<RequirementVector, Double> c_j = new HashMap<>();
				currentIterationClusters.add(c_j);
			}
			for (RequirementVector v1 : vectors) {
				// check against each center
				List<Double> distances = new ArrayList<>();
				for (int j = 0; j < oldCentroids.size(); j++) {
					double distToCenter = distanceHandler.distanceZ(v1, oldCentroids.get(j));
					distances.add(distToCenter);
				}
				for (int j = 0; j < oldCentroids.size(); j++) {
					double weightToJ = 1;
					double tempWeight = 0;
					double distToJ = distances.get(j);
					for (int l = 0; l < oldCentroids.size(); l++) {
						double distToCenter = distances.get(l);
						if (distToCenter != 0) {
							tempWeight += Math.pow((distToJ / distToCenter), (2 / (this.m - 1)));
						}
					}
					if (tempWeight != 0) {
						weightToJ = weightToJ / tempWeight;
					}
					currentIterationClusters.get(j).put(v1, weightToJ);
				}
			}
			// assigned all points to clusters, now check if good clustering
			List<RequirementVector> newCentroids = new ArrayList<RequirementVector>();
			// go through each cluster
			for (int j = 0; j < currentIterationClusters.size(); j++) {
				// create new centroid for this cluster
				Map<RequirementVector, Double> currentCluster = currentIterationClusters.get(j);
				double totalClusterWeight = 0;
				Vector newCenter = null;
				for (RequirementVector v1 : currentCluster.keySet()) {
					// get new center
					double weight = currentCluster.get(v1);
					totalClusterWeight += weight;
					if (newCenter == null) {
						newCenter = v1.getVectorRepresentation().multiplyCopy(weight);
					} else {
						Vector toAdd = v1.getVectorRepresentation().multiplyCopy(weight);
						newCenter.add(toAdd);
					}
				}

				// get new field maps
				newCenter.divide(totalClusterWeight);
				Map<Field, Vector> fieldVector = new HashMap<>();
				for (Field field : Util.getRuppFields()) {
					double totalClusterWeightField = 0;
					Vector newFieldCenter = null;
					for (RequirementVector v1 : currentCluster.keySet()) {
						double weight = currentCluster.get(v1);
						totalClusterWeightField += weight;
						if (newFieldCenter == null) {
							newFieldCenter = v1.getFieldVectorRepresentation(field).multiplyCopy(weight);
						} else {
							Vector toAdd = v1.getFieldVectorRepresentation(field).multiplyCopy(weight);
							newFieldCenter.add(toAdd);
						}
					}
					newFieldCenter.divide(totalClusterWeightField);
					fieldVector.put(field, newFieldCenter);
				}
				// new center finished
				newCentroids.add(new RequirementVector(newCenter, fieldVector, "-1"));
			}
			clusters = currentIterationClusters;
			double centerDist = 0;
			for (int j = 0; j < oldCentroids.size(); j++) {
				centerDist += distanceHandler.distanceZ(oldCentroids.get(j), newCentroids.get(j));
			}
			if (centerDist < this.eps) {
				break;
			} else {
				oldCentroids = newCentroids;
			}

		}

		// final Assignment
		List<List<RequirementVector>> returnClusters = new ArrayList<List<RequirementVector>>();
		for (int i = 0; i < this.k; i++) {
			List<RequirementVector> c_i = new ArrayList<RequirementVector>();
			returnClusters.add(c_i);
		}

		for (RequirementVector v1 : vectors) {

			// check against each center
			double maxWeight = Double.MIN_VALUE;
			int cluster = -1;
			for (int i = 0; i < clusters.size(); i++) {

				double weight = clusters.get(i).get(v1);

				if (weight >= maxWeight) {
					maxWeight = weight;
					cluster = i;
				}
			}
			returnClusters.get(cluster).add(v1);
		}

		return returnClusters;
	}

	// this method tries to sort the elements in the cluster in order of their
	// distance to the center
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

	// this is used by the evaluation jar
	@Override
	public List<List<RequirementVector>> evaluationCluster(final List<RequirementVector> vectors,
			final DistanceHandler distanceHandler) {
		// contains all clusters: cluster is a list of Requirement Vectors ->
		// clusters has k elements

		List<Map<RequirementVector, Double>> clusters = new ArrayList<>();
		for (int i = 0; i < this.k; i++) {
			Map<RequirementVector, Double> c_i = new HashMap<>();
			clusters.add(c_i);
		}
		// end of cluster init
		// start of center init
		List<RequirementVector> clusterCenters = new ArrayList<RequirementVector>();
		// init****************************************************************
		// this takes the random numbers from a given file defined by the
		// constructor
		List<Integer> noDoubleCenters = new ArrayList<Integer>();
		Iterator<Integer> random = this.randomNumbers.iterator();
		for (int i = 0; i < this.k; i++) {
			int centerIndex = random.next();
			while (noDoubleCenters.contains(centerIndex)) {
				centerIndex = random.next();
			}
			noDoubleCenters.add(centerIndex);
			clusterCenters.add(vectors.get(centerIndex));
		}

		// *****************************************************************************
		// end of center init
		List<RequirementVector> oldCentroids = clusterCenters;

		// start fuzzy c means
		for (int i = 0; i < this.iterations; i++) {
			List<Map<RequirementVector, Double>> currentIterationClusters = new ArrayList<>();
			for (int j = 0; j < this.k; j++) {
				Map<RequirementVector, Double> c_j = new HashMap<>();
				currentIterationClusters.add(c_j);
			}
			for (RequirementVector v1 : vectors) {
				// check against each center
				List<Double> distances = new ArrayList<>();
				for (int j = 0; j < oldCentroids.size(); j++) {
					double distToCenter = distanceHandler.distanceZ(v1, oldCentroids.get(j));
					distances.add(distToCenter);
				}
				for (int j = 0; j < oldCentroids.size(); j++) {
					double weightToJ = 1;
					double tempWeight = 0;
					double distToJ = distances.get(j);
					for (int l = 0; l < oldCentroids.size(); l++) {
						double distToCenter = distances.get(l);
						if (distToCenter != 0) {
							tempWeight += Math.pow((distToJ / distToCenter), (2 / (this.m - 1)));
						}
					}
					if (tempWeight != 0) {
						weightToJ = weightToJ / tempWeight;
					}
					currentIterationClusters.get(j).put(v1, weightToJ);
				}
			}
			// assigned all points to clusters, now check if good clustering
			List<RequirementVector> newCentroids = new ArrayList<RequirementVector>();
			// go through each cluster
			for (int j = 0; j < currentIterationClusters.size(); j++) {
				// create new centroid for this cluster
				Map<RequirementVector, Double> currentCluster = currentIterationClusters.get(j);
				double totalClusterWeight = 0;
				Vector newCenter = null;
				for (RequirementVector v1 : currentCluster.keySet()) {
					// get new center
					double weight = currentCluster.get(v1);
					totalClusterWeight += weight;
					if (newCenter == null) {
						newCenter = v1.getVectorRepresentation().multiplyCopy(weight);
					} else {
						Vector toAdd = v1.getVectorRepresentation().multiplyCopy(weight);
						newCenter.add(toAdd);
					}
				}

				// get new field maps
				newCenter.divide(totalClusterWeight);
				Map<Field, Vector> fieldVector = new HashMap<>();
				for (Field field : Util.getRuppFields()) {
					double totalClusterWeightField = 0;
					Vector newFieldCenter = null;
					for (RequirementVector v1 : currentCluster.keySet()) {
						double weight = currentCluster.get(v1);
						totalClusterWeightField += weight;
						if (newFieldCenter == null) {
							newFieldCenter = v1.getFieldVectorRepresentation(field).multiplyCopy(weight);
						} else {
							Vector toAdd = v1.getFieldVectorRepresentation(field).multiplyCopy(weight);
							newFieldCenter.add(toAdd);
						}
					}
					newFieldCenter.divide(totalClusterWeightField);
					fieldVector.put(field, newFieldCenter);
				}
				// new center finished
				newCentroids.add(new RequirementVector(newCenter, fieldVector, "-1"));
			}
			clusters = currentIterationClusters;
			double centerDist = 0;
			for (int j = 0; j < oldCentroids.size(); j++) {
				centerDist += distanceHandler.distanceZ(oldCentroids.get(j), newCentroids.get(j));
			}
			if (centerDist < this.eps) {
				break;
			} else {
				oldCentroids = newCentroids;
			}

		}

		// final Assignment
		List<List<RequirementVector>> returnClusters = new ArrayList<List<RequirementVector>>();
		for (int i = 0; i < this.k; i++) {
			List<RequirementVector> c_i = new ArrayList<RequirementVector>();
			returnClusters.add(c_i);
		}

		for (RequirementVector v1 : vectors) {

			// check against each center
			double maxWeight = Double.MIN_VALUE;
			int cluster = -1;
			for (int i = 0; i < clusters.size(); i++) {

				double weight = clusters.get(i).get(v1);

				if (weight >= maxWeight) {
					maxWeight = weight;
					cluster = i;
				}
			}
			returnClusters.get(cluster).add(v1);
		}

		return returnClusters;
	}

	public List<List<RequirementVector>> evaluationClusterZ(final List<RequirementVector> vectors,
			final DistanceHandler distanceHandler) {
		// contains all clusters: cluster is a list of Requirement Vectors ->
		// clusters has k elements

		List<Map<RequirementVector, Double>> clusters = new ArrayList<>();
		for (int i = 0; i < this.k; i++) {
			Map<RequirementVector, Double> c_i = new HashMap<>();
			clusters.add(c_i);
		}
		// end of cluster init
		// start of center init
		List<RequirementVector> clusterCenters = new ArrayList<RequirementVector>();
		// init****************************************************************
		// this takes the random numbers from a given file defined by the
		// constructor
		List<Integer> noDoubleCenters = new ArrayList<Integer>();
		Iterator<Integer> random = this.randomNumbers.iterator();
		for (int i = 0; i < this.k; i++) {
			int centerIndex = random.next();
			while (noDoubleCenters.contains(centerIndex)) {
				centerIndex = random.next();
			}
			noDoubleCenters.add(centerIndex);
			clusterCenters.add(vectors.get(centerIndex));
		}

		// *****************************************************************************
		// end of center init
		List<RequirementVector> oldCentroids = clusterCenters;

		// start fuzzy c means
		for (int i = 0; i < this.iterations; i++) {
			List<Map<RequirementVector, Double>> currentIterationClusters = new ArrayList<>();
			for (int j = 0; j < this.k; j++) {
				Map<RequirementVector, Double> c_j = new HashMap<>();
				currentIterationClusters.add(c_j);
			}
			for (RequirementVector v1 : vectors) {
				// check against each center
				List<Double> distances = new ArrayList<>();
				for (int j = 0; j < oldCentroids.size(); j++) {
					double distToCenter = distanceHandler.distanceZ(v1, oldCentroids.get(j));
					distances.add(distToCenter);
				}
				for (int j = 0; j < oldCentroids.size(); j++) {
					double weightToJ = 1;
					double tempWeight = 0;
					double distToJ = distances.get(j);
					for (int l = 0; l < oldCentroids.size(); l++) {
						double distToCenter = distances.get(l);
						if (distToCenter != 0) {
							tempWeight += Math.pow((distToJ / distToCenter), (2 / (this.m - 1)));
						}
					}
					if (tempWeight != 0) {
						weightToJ = weightToJ / tempWeight;
					}
					currentIterationClusters.get(j).put(v1, weightToJ);
				}
			}
			// assigned all points to clusters, now check if good clustering
			List<RequirementVector> newCentroids = new ArrayList<RequirementVector>();
			// go through each cluster
			for (int j = 0; j < currentIterationClusters.size(); j++) {
				// create new centroid for this cluster
				Map<RequirementVector, Double> currentCluster = currentIterationClusters.get(j);
				double totalClusterWeight = 0;
				Vector newCenter = null;
				for (RequirementVector v1 : currentCluster.keySet()) {
					// get new center
					double weight = currentCluster.get(v1);
					totalClusterWeight += weight;
					if (newCenter == null) {
						newCenter = v1.getVectorRepresentation().multiplyCopy(weight);
					} else {
						Vector toAdd = v1.getVectorRepresentation().multiplyCopy(weight);
						newCenter.add(toAdd);
					}
				}

				// get new field maps
				newCenter.divide(totalClusterWeight);
				Map<Field, Vector> fieldVector = new HashMap<>();
				for (Field field : Util.getRuppFields()) {
					double totalClusterWeightField = 0;
					Vector newFieldCenter = null;
					for (RequirementVector v1 : currentCluster.keySet()) {
						double weight = currentCluster.get(v1);
						totalClusterWeightField += weight;
						if (newFieldCenter == null) {
							newFieldCenter = v1.getFieldVectorRepresentation(field).multiplyCopy(weight);
						} else {
							Vector toAdd = v1.getFieldVectorRepresentation(field).multiplyCopy(weight);
							newFieldCenter.add(toAdd);
						}
					}
					newFieldCenter.divide(totalClusterWeightField);
					fieldVector.put(field, newFieldCenter);
				}
				// new center finished
				newCentroids.add(new RequirementVector(newCenter, fieldVector, "-1"));
			}
			clusters = currentIterationClusters;
			double centerDist = 0;
			for (int j = 0; j < oldCentroids.size(); j++) {
				centerDist += distanceHandler.distanceZ(oldCentroids.get(j), newCentroids.get(j));
			}
			if (centerDist < this.eps) {
				break;
			} else {
				oldCentroids = newCentroids;
			}

		}

		// final Assignment
		List<List<RequirementVector>> returnClusters = new ArrayList<List<RequirementVector>>();
		for (int i = 0; i < this.k; i++) {
			List<RequirementVector> c_i = new ArrayList<RequirementVector>();
			returnClusters.add(c_i);
		}

		for (RequirementVector v1 : vectors) {

			// check against each center
			double maxWeight = Double.MIN_VALUE;
			int cluster = -1;
			for (int i = 0; i < clusters.size(); i++) {

				double weight = clusters.get(i).get(v1);

				if (weight >= maxWeight) {
					maxWeight = weight;
					cluster = i;
				}
			}
			returnClusters.get(cluster).add(v1);
		}

		return returnClusters;
	}
}
