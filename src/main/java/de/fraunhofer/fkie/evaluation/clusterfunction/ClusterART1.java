package de.fraunhofer.fkie.evaluation.clusterfunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.fraunhofer.fkie.evaluation.distancefunction.DistanceHandler;
import de.fraunhofer.fkie.evaluation.model.ARTNeuron;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;
import mikera.vectorz.Vector;

public class ClusterART1 implements ClusterFunction {
	double vigilance;
	double l; // still not quite sure about this parameter
	int k;

	public ClusterART1(final double vigilance, final double l, final int k) {
		this.vigilance = vigilance;
		this.l = l;
		this.k = k;
	}

	@Override
	public String getName() {
		return "ClusterART";
	}

	public double getVigilance() {
		return this.vigilance;
	}

	public double getL() {
		return this.l;
	}

	public int getK() {
		return this.k;
	}

	@Override
	public List<List<RequirementVector>> cluster(final List<RequirementVector> dataSet,
			final DistanceHandler distanceHandler, final List<Integer> givenCenters) {
		// if k = -1 he is allowed to decide himself
		if (this.k == -1) {
			this.k = dataSet.size();
		}

		// init cluster:

		// list with neurons (= clusters)
		List<ARTNeuron> neurons = new ArrayList<>();
		neurons.add(this.createNewNeuron(dataSet.get(0)));
		// list of clusters
		List<List<RequirementVector>> clusters = new LinkedList<>();
		List<RequirementVector> cluster = new LinkedList<>();
		clusters.add(cluster);
		List<RequirementVector> forShuffle = new LinkedList<>();
		for (RequirementVector v1 : dataSet) {
			forShuffle.add(v1);
		}
		Collections.shuffle(forShuffle);
		for (int i = 0; i < forShuffle.size(); i++) {
			RequirementVector toCluster = forShuffle.get(i);
			boolean searching = true;
			List<Integer> alreadyTried = new LinkedList<>();
			List<Double> vectorMatches = new LinkedList<>(); // to force into
																// one of 23
			while (searching) {
				double maxActivation = -1;
				int maxActivationIndex = -1;
				for (int j = 0; j < neurons.size(); j++) {
					// OPTIMIZE HERE BY SAVING THE ACTIVATION
					if (!alreadyTried.contains(j)) {
						double activation = this.calculateActivation(toCluster, neurons.get(j).getWeights());
						if (activation > maxActivation) {
							maxActivation = activation;
							maxActivationIndex = j;
						}
					}
				}
				if (maxActivationIndex == -1) {
					neurons.add(this.createNewNeuron(toCluster));
					clusters.add(new LinkedList<RequirementVector>());
				} else if (this.checkVigilance(toCluster, neurons.get(maxActivationIndex).getRepresentative(),
						vectorMatches)) {
					// add to Cluster and update representative
					clusters.get(maxActivationIndex).add(toCluster);
					this.updateWeightsAndRep(toCluster, neurons.get(maxActivationIndex));
					searching = false;
				} else {
					alreadyTried.add(maxActivationIndex);
					if (alreadyTried.size() == neurons.size()) {
						// we tried all, set new neuron
						if (neurons.size() < this.k) {
							neurons.add(this.createNewNeuron(toCluster));
							clusters.add(new LinkedList<RequirementVector>());
						}
						// to force into k
						else {
							double bestMatch = 0;
							int index = 0;
							for (int a = 0; a < vectorMatches.size(); a++) {
								if (vectorMatches.get(a) > bestMatch) {
									bestMatch = vectorMatches.get(a);
									index = a;
								}
							}
							clusters.get(index).add(toCluster);
							searching = false;
						}
					}
				}
			}
		}
		// List<List<RequirementVector>> sortedClusters = sortClusters(clusters,
		// neurons);
		return clusters;
	}

	@Override
	public List<List<RequirementVector>> evaluationCluster(final List<RequirementVector> dataSet,
			final DistanceHandler distanceHandler) {
		// if k = -1 he is allowed to decide himself
		if (this.k == -1) {
			this.k = dataSet.size();
		}

		// init cluster:

		// list with neurons (= clusters)
		List<ARTNeuron> neurons = new ArrayList<>();
		neurons.add(this.createNewNeuron(dataSet.get(0)));
		// list of clusters
		List<List<RequirementVector>> clusters = new ArrayList<>();
		List<RequirementVector> cluster = new ArrayList<>();
		clusters.add(cluster);
		// List<RequirementVector> forShuffle = new LinkedList<>();
		// for(RequirementVector v1 : dataSet){
		// forShuffle.add(v1);
		// }
		// Collections.shuffle(forShuffle);
		for (int i = 0; i < dataSet.size(); i++) {
			RequirementVector toCluster = dataSet.get(i);
			boolean searching = true;
			List<Integer> alreadyTried = new ArrayList<>();
			List<Double> vectorMatches = new ArrayList<>(); // to force into
															// one of 23
			while (searching) {
				double maxActivation = -1;
				int maxActivationIndex = -1;
				for (int j = 0; j < neurons.size(); j++) {
					// OPTIMIZE HERE BY SAVING THE ACTIVATION
					if (!alreadyTried.contains(j)) {
						double activation = this.calculateActivation(toCluster, neurons.get(j).getWeights());
						if (activation > maxActivation) {
							maxActivation = activation;
							maxActivationIndex = j;
						}
					}
				}
				if (maxActivationIndex == -1) {
					neurons.add(this.createNewNeuron(toCluster));
					clusters.add(new LinkedList<RequirementVector>());
				} else if (this.checkVigilance(toCluster, neurons.get(maxActivationIndex).getRepresentative(),
						vectorMatches)) {
					// add to Cluster and update representative
					clusters.get(maxActivationIndex).add(toCluster);
					this.updateWeightsAndRep(toCluster, neurons.get(maxActivationIndex));
					searching = false;
				} else {
					alreadyTried.add(maxActivationIndex);
					if (alreadyTried.size() == neurons.size()) {
						// we tried all, set new neuron
						if (neurons.size() < this.k) {
							neurons.add(this.createNewNeuron(toCluster));
							clusters.add(new LinkedList<RequirementVector>());
						}
						// to force into k
						else {
							double bestMatch = 0;
							int index = 0;
							for (int a = 0; a < vectorMatches.size(); a++) {
								if (vectorMatches.get(a) > bestMatch) {
									bestMatch = vectorMatches.get(a);
									index = a;
								}
							}
							clusters.get(index).add(toCluster);
							searching = false;
						}
					}
				}
			}
		}
		// List<List<RequirementVector>> sortedClusters = sortClusters(clusters,
		// neurons);
		return clusters;
	}

	// private List<List<RequirementVector>>
	// sortClusters(List<List<RequirementVector>> clusters,
	// List<ARTNeuron> neurons) {
	// List<List<RequirementVector>> toReturn = new ArrayList<>();
	// DistanceFunction distanceFunction = new EuclideanDistance();
	//
	// for(int i = 0; i < clusters.size(); i++){
	// List<RequirementVector> cluster = clusters.get(i);
	// RequirementVector representative = neurons.get(i).getRepresentative();
	// Map<RequirementVector, Double> pointDistance = new LinkedHashMap<>();
	// for (RequirementVector v1 : cluster) {
	// double dist = distanceFunction.distance(v1, representative);
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

	private void updateWeightsAndRep(final RequirementVector toCluster, final ARTNeuron artNeuron) {
		// update representative
		RequirementVector representative = artNeuron.getRepresentative();
		Map<String, Double> weights = artNeuron.getWeights();
		for (String key : toCluster.getContentKeys()) {
			if (toCluster.getContent(key) > 0 && representative.getContent(key) > 0) {
				representative.putContent(key, 1.0);
			} else {
				representative.putContent(key, 0.0);
			}
		}

		// update weights
		// now update the weigth of this key
		double normOfRep = representative.normOfMap();
		for (String key : toCluster.getContentKeys()) {
			double weight = this.l * representative.getContent(key);
			double divide = this.l - 1 + normOfRep;
			double newValue = weight / divide;
			weights.put(key, newValue);
		}
	}

	private boolean checkVigilance(final RequirementVector toCluster, final RequirementVector representative,
			final List<Double> matches) {
		Map<String, Double> and = new HashMap<String, Double>();
		for (String key : toCluster.getContentKeys()) {
			if (toCluster.getContent(key) > 0 && representative.getContent(key) > 0) {
				and.put(key, 1.0);
			}
		}

		double normOfAnd = this.normOfMap(and);
		double normOfToCluster = toCluster.normOfMap();
		double leftSide = normOfAnd / normOfToCluster;
		matches.add(leftSide);
		return leftSide >= this.vigilance;
	}

	public ARTNeuron createNewNeuron(final RequirementVector counter) {
		double[] vector = new double[counter.getContentKeys().size()];
		for (int i = 0; i < counter.getContentKeys().size(); i++) {
			vector[i] = 1;
		}

		RequirementVector firstRepresentative = new RequirementVector(Vector.create(vector), null, "-1");
		ARTNeuron firstNeuron = new ARTNeuron(firstRepresentative);
		return firstNeuron;
	}

	private double calculateActivation(final RequirementVector toCluster, final Map<String, Double> weights) {
		double activation = 0;
		for (String key : toCluster.getContentKeys()) {
			if (toCluster.getContent(key) > 0) {
				activation += weights.get(key);
			}
		}
		return activation;
	}

	private double normOfMap(final Map<String, Double> map) {
		double count = 0;
		for (String key : map.keySet()) {
			if (map.get(key) > 0) {
				count++;
			}
		}
		return count;
	}

}
