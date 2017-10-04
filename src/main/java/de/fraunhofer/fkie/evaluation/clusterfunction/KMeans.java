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
import de.fraunhofer.fkie.evaluation.distancefunction.EuclideanDistance;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;
import mikera.vectorz.Vector;

public class KMeans implements ClusterFunction {

	public static Logger LOG = LoggerFactory.getLogger(KMeans.class);
	private int k;
	private int iterations;
	private double eps;
	private List<Integer> randomNumbers = null;
	// only for debugging purpose
	private int numberSeed;

	public KMeans(final int k, final int iterations) {
		this.k = k;
		this.iterations = iterations;
		this.eps = 0.1;
	}

	public KMeans(final int k, final int iterations, final double eps) {
		this.k = k;
		this.iterations = iterations;
		this.eps = eps;
	}

	@Override
	public String getName() {
		return "KMeans" + (this.k) + (this.iterations);
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
		List<List<RequirementVector>> clusters = new ArrayList<List<RequirementVector>>();
		for (int i = 0; i < this.k; i++) {
			List<RequirementVector> c_i = new ArrayList<RequirementVector>();
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

		// start kMeans
		for (int i = 0; i < this.iterations; i++) {
			for (RequirementVector v1 : vectors) {
				// check against each center
				double minDist = Double.MAX_VALUE;
				int cluster = -1;
				for (int j = 0; j < oldCentroids.size(); j++) {
					double dist = distanceHandler.distanceZ(v1, oldCentroids.get(j));
					if (dist <= minDist) {
						minDist = dist;
						cluster = j;
					}
				}

				clusters.get(cluster).add(v1);
			}

			// assigned all points to clusters, now check if good clustering
			List<RequirementVector> newCentroids = new ArrayList<RequirementVector>();
			// go through each cluster
			for (int j = 0; j < clusters.size(); j++) {
				// create new centroid for this cluster
				List<RequirementVector> currentCluster = clusters.get(j);
				if (currentCluster.size() > 0) {
					// init with first vector
					Vector newCenter = Vector.create(currentCluster.get(0).getVectorRepresentation());
					// now manipulate with rest of cluster
					for (int k = 1; k < currentCluster.size(); k++) {
						newCenter.add(currentCluster.get(k).getVectorRepresentation());
					}
					newCenter.divide(currentCluster.size());
					// newCenter.fillFieldsMap();
					// new center finished
					Map<Field, Vector> fieldVector = new HashMap<>();
					for (Field field : Util.getRuppFields()) {
						Vector newFieldCenter = Vector
								.create(currentCluster.get(0).getFieldVectorRepresentation(field));
						for (int k = 1; k < currentCluster.size(); k++) {
							newFieldCenter.add(currentCluster.get(k).getFieldVectorRepresentation(field));
						}
						newFieldCenter.divide(currentCluster.size());
						fieldVector.put(field, newFieldCenter);
					}
					newCentroids.add(new RequirementVector(newCenter, fieldVector, "-1"));
				} else {
					newCentroids.add(oldCentroids.get(j));
				}
			}
			// determine difference of clusters
			double centerDist = 0;
			for (int j = 0; j < oldCentroids.size(); j++) {
				centerDist += distanceHandler.distanceZ(oldCentroids.get(j), newCentroids.get(j));
			}

			if (centerDist < this.eps) {
				break;
			}

			else {
				oldCentroids = newCentroids;
				// cleanup for next iteration
				if (i != this.iterations - 1) {
					for (List<RequirementVector> c : clusters) {
						c.clear();
					}
				}
			}
		}

		return clusters;
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
			final DistanceHandler handler) {
		// contains all clusters: cluster is a list of Requirement Vectors ->
		// clusters has k elements
		List<List<RequirementVector>> clusters = new ArrayList<List<RequirementVector>>();
		for (int i = 0; i < this.k; i++) {
			List<RequirementVector> c_i = new ArrayList<RequirementVector>();
			clusters.add(c_i);
		}
		// end of cluster init
		// start of center init
		List<RequirementVector> clusterCenters = new ArrayList<RequirementVector>(this.k);
		// init****************************************************************
		// this takes the random numbers from a given file defined by the
		// constructor
		List<Integer> noDoubleCenters = new ArrayList<Integer>(this.k);
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

		// start kMeans
		for (int i = 0; i < this.iterations; i++) {
			for (RequirementVector v1 : vectors) {
				// check against each center
				double minDist = Double.MAX_VALUE;
				int cluster = -1;
				for (int j = 0; j < oldCentroids.size(); j++) {
					double dist = handler.distanceZ(v1, oldCentroids.get(j));
					if (dist <= minDist) {
						minDist = dist;
						cluster = j;
					}
				}

				clusters.get(cluster).add(v1);
			}

			// assigned all points to clusters, now check if good clustering
			List<RequirementVector> newCentroids = new ArrayList<RequirementVector>();
			// go through each cluster
			for (int j = 0; j < clusters.size(); j++) {
				// create new centroid for this cluster
				List<RequirementVector> currentCluster = clusters.get(j);
				if (currentCluster.size() > 0) {
					// init with first vector
					Vector newCenter = Vector.create(currentCluster.get(0).getVectorRepresentation());
					// now manipulate with rest of cluster
					for (int k = 1; k < currentCluster.size(); k++) {
						newCenter.add(currentCluster.get(k).getVectorRepresentation());
					}
					newCenter.divide(currentCluster.size());
					// newCenter.fillFieldsMap();
					// new center finished
					Map<Field, Vector> fieldVector = new HashMap<>();
					for (Field field : Util.getRuppFields()) {
						Vector newFieldCenter = Vector
								.create(currentCluster.get(0).getFieldVectorRepresentation(field));
						for (int k = 1; k < currentCluster.size(); k++) {
							newFieldCenter.add(currentCluster.get(k).getFieldVectorRepresentation(field));
						}
						newFieldCenter.divide(currentCluster.size());
						fieldVector.put(field, newFieldCenter);
					}
					newCentroids.add(new RequirementVector(newCenter, fieldVector, "-1"));
				} else {
					newCentroids.add(oldCentroids.get(j));
				}
			}
			// determine difference of clusters
			double centerDist = 0;
			for (int j = 0; j < oldCentroids.size(); j++) {
				centerDist += handler.distanceZ(oldCentroids.get(j), newCentroids.get(j));
			}

			if (centerDist < this.eps) {
				break;
			}

			else {
				oldCentroids = newCentroids;
				// cleanup for next iteration
				if (i != this.iterations - 1) {
					for (List<RequirementVector> c : clusters) {
						c.clear();
					}
				}
			}
		}

		return clusters;
	}

	public List<List<RequirementVector>> evaluationClusterVectorz(final List<RequirementVector> vectors,
			final DistanceHandler handler) {
		// contains all clusters: cluster is a list of Requirement Vectors ->
		// clusters has k elements
		EuclideanDistance euclidean = new EuclideanDistance();
		List<List<RequirementVector>> clusters = new ArrayList<List<RequirementVector>>();
		for (int i = 0; i < this.k; i++) {
			List<RequirementVector> c_i = new ArrayList<RequirementVector>();
			clusters.add(c_i);
		}
		// end of cluster init
		// start of center init
		List<RequirementVector> clusterCenters = new ArrayList<RequirementVector>(this.k);
		// init****************************************************************
		// this takes the random numbers from a given file defined by the
		// constructor
		List<Integer> noDoubleCenters = new ArrayList<Integer>(this.k);
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

		// start kMeans
		for (int i = 0; i < this.iterations; i++) {
			for (RequirementVector v1 : vectors) {
				// check against each center
				double minDist = Double.MAX_VALUE;
				int cluster = -1;
				for (int j = 0; j < oldCentroids.size(); j++) {
					double dist = handler.distanceZ(v1, oldCentroids.get(j));
					if (dist <= minDist) {
						minDist = dist;
						cluster = j;
					}
				}

				clusters.get(cluster).add(v1);
			}

			// assigned all points to clusters, now check if good clustering
			List<RequirementVector> newCentroids = new ArrayList<RequirementVector>();
			// go through each cluster
			for (int j = 0; j < clusters.size(); j++) {
				// create new centroid for this cluster
				List<RequirementVector> currentCluster = clusters.get(j);
				if (currentCluster.size() > 0) {
					// init with first vector
					Vector newCenter = Vector.create(currentCluster.get(0).getVectorRepresentation());
					// now manipulate with rest of cluster
					for (int k = 1; k < currentCluster.size(); k++) {
						newCenter.add(currentCluster.get(k).getVectorRepresentation());
					}
					newCenter.divide(currentCluster.size());
					// newCenter.fillFieldsMap();
					// new center finished
					Map<Field, Vector> fieldVector = new HashMap<>();
					for (Field field : Util.getRuppFields()) {
						Vector newFieldCenter = Vector
								.create(currentCluster.get(0).getFieldVectorRepresentation(field));
						for (int k = 1; k < currentCluster.size(); k++) {
							newFieldCenter.add(currentCluster.get(k).getFieldVectorRepresentation(field));
						}
						newFieldCenter.divide(currentCluster.size());
						fieldVector.put(field, newFieldCenter);
					}
					newCentroids.add(new RequirementVector(newCenter, fieldVector, "-1"));
				} else {
					newCentroids.add(oldCentroids.get(j));
				}
			}
			// determine difference of clusters
			double centerDist = 0;
			for (int j = 0; j < oldCentroids.size(); j++) {
				centerDist += handler.distanceZ(oldCentroids.get(j), newCentroids.get(j));
			}

			if (centerDist < this.eps) {
				break;
			}

			else {
				oldCentroids = newCentroids;
				// cleanup for next iteration
				if (i != this.iterations - 1) {
					for (List<RequirementVector> c : clusters) {
						c.clear();
					}
				}
			}
		}

		return clusters;
	}

}
