package de.fraunhofer.fkie.evaluation.utils;

import java.util.List;

import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceFunction;
import de.fraunhofer.fkie.evaluation.model.Cluster;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;

public class Metrics {
	/**
	 *
	 * @param computerClusters
	 * @param humanClusters
	 * @return average of F1 between the ccs and hcs
	 */
	public static double[] calculateF1Avg(final List<Cluster> computerClusters, final List<Cluster> humanClusters,
			final List<Requirement> testData) {
		double f1 = 0;
		double globalRecall = 0;
		double globalPrecision = 0;
		for (int i = 0; i < computerClusters.size(); i++) {
			Cluster cc = computerClusters.get(i);
			Cluster hc = humanClusters.get(i);
			double[] localArray;
			if (cc.getRequirements().size() == 0 || hc.getRequirements().size() == 0) {
				localArray = new double[3];
			} else {
				localArray = calculateF1(cc, hc);
			}
			// for end result out of all cluster centers
			globalPrecision += localArray[0];
			globalRecall += localArray[1];
			f1 += localArray[2];
		}
		double f1Array[] = new double[3]; // precision, recall, f1
		f1Array[0] = globalPrecision / computerClusters.size();
		f1Array[1] = globalRecall / computerClusters.size();
		f1Array[2] = f1 / computerClusters.size();
		return f1Array;

	}

	/**
	 *
	 * @param computerClusters
	 * @param humanClusters
	 * @return average of F1 between the ccs and hcs
	 */
	public static double[] calculateF1WeightedAvg(final List<Cluster> computerClusters,
			final List<Cluster> humanClusters, final List<Requirement> testData) {
		double f1 = 0;
		double globalRecall = 0;
		double globalPrecision = 0;
		for (int i = 0; i < computerClusters.size(); i++) {
			Cluster cc = computerClusters.get(i);
			Cluster hc = humanClusters.get(i);
			double[] localArray;
			if (cc.getRequirements().size() == 0 || hc.getRequirements().size() == 0) {
				localArray = new double[3];
			} else {
				localArray = calculateF1(cc, hc);
			}
			// for end result out of all cluster centers

			double weight = (double) hc.getRequirements().size() / testData.size();
			globalPrecision += localArray[0] * weight;
			globalRecall += localArray[1] * weight;
			f1 += localArray[2] * weight;
		}
		double f1Array[] = new double[3]; // precision, recall, f1
		f1Array[0] = globalPrecision;
		f1Array[1] = globalRecall;
		f1Array[2] = f1;
		return f1Array;

	}

	/**
	 *
	 * @param cc
	 * @param hc
	 * @return the F1 between cc and hc
	 */
	public static double[] calculateF1(final Cluster cc, final Cluster hc) {
		double sizeOfIntersection = 0;
		for (Requirement r1 : cc.getRequirements()) {
			if (hc.getRequirements().contains(r1)) {
				sizeOfIntersection++;
			}
		}
		double recall = sizeOfIntersection / hc.getRequirements().size();
		double precision = sizeOfIntersection / cc.getRequirements().size();
		double f1;
		if (recall == 0 || precision == 0) {
			f1 = 0;
		} else {
			f1 = (2 * recall * precision) / (recall + precision);
		}
		double f1Array[] = new double[3]; // precision, recall, f1
		f1Array[0] = precision;
		f1Array[1] = recall;
		f1Array[2] = f1;
		return f1Array;

	}

	/**
	 *
	 * @param computerCluster
	 * @param humanCluster
	 * @param testData
	 * @return the naive metric value
	 */
	public static double calculateNaive(final List<Cluster> computerCluster, final List<Cluster> humanCluster,
			final List<Requirement> testData) {
		int negative = 0;
		int positive = 0;
		for (Requirement requirement : testData) {
			int computerClusterIndex = getIndex(computerCluster, requirement);
			int humanClusterIndex = getIndex(humanCluster, requirement);
			if (computerClusterIndex == humanClusterIndex) {
				positive++;
			} else {
				negative++;
			}
		}
		return (double) positive / (negative + positive);
	}

	/**
	 *
	 * @param computerClusters
	 * @param humanClusters
	 * @param testData
	 * @return the average purity of all cc
	 */
	public static double calculatePurityAvg(final List<Cluster> computerClusters, final List<Cluster> humanClusters,
			final List<Requirement> testData) {
		double purity = 0;
		for (int i = 0; i < computerClusters.size(); i++) {
			Cluster cc = computerClusters.get(i);
			purity += calculatePurity(cc, humanClusters, testData);
		}
		return purity;
	}

	/**
	 *
	 * @param cc
	 * @param humanClusters
	 * @param testData
	 * @return the purity for cc
	 */
	public static double calculatePurity(final Cluster cc, final List<Cluster> humanClusters,
			final List<Requirement> testData) {
		double purity = 0;
		double relativeSize = (double) cc.getRequirements().size() / testData.size();
		double maxPrecision = 0;
		for (int j = 0; j < humanClusters.size(); j++) {
			Cluster hc = humanClusters.get(j);
			int sizeOfIntersection = 0;
			for (Requirement r1 : cc.getRequirements()) {
				if (hc.getRequirements().contains(r1)) {
					sizeOfIntersection++;
				}
			}
			double precision = (double) sizeOfIntersection / cc.getRequirements().size();
			if (precision > maxPrecision) {
				maxPrecision = precision;
			}
		}
		purity = relativeSize * maxPrecision;
		return purity;
	}

	/**
	 *
	 * @param computerClusters
	 * @param humanClusters
	 * @param testData
	 * @return the average jaccard between all cc and hc
	 */
	public static double calculateJaccardAvg(final List<Cluster> computerClusters, final List<Cluster> humanClusters,
			final List<Requirement> testData) {
		double jaccard = 0;

		for (int i = 0; i < computerClusters.size(); i++) {
			Cluster cc = computerClusters.get(i);
			Cluster hc = humanClusters.get(i);
			jaccard += calculateJaccard(cc, hc, computerClusters, humanClusters, testData);
		}

		return jaccard / computerClusters.size();
	}

	public static double calculateJaccardWeightedAvg(final List<Cluster> computerClusters,
			final List<Cluster> humanClusters, final List<Requirement> testData) {
		double jaccard = 0;

		for (int i = 0; i < computerClusters.size(); i++) {
			Cluster cc = computerClusters.get(i);
			Cluster hc = humanClusters.get(i);
			double weight = (double) hc.getRequirements().size() / testData.size();
			jaccard += calculateJaccard(cc, hc, computerClusters, humanClusters, testData) * weight;
		}

		return jaccard;
	}

	/**
	 *
	 * @param cc
	 * @param hc
	 * @param computerClusters
	 * @param humanClusters
	 * @param testData
	 * @return the jaccard between cc and hc
	 */
	public static double calculateJaccard(final Cluster cc, final Cluster hc, final List<Cluster> computerClusters,
			final List<Cluster> humanClusters, final List<Requirement> testData) {

		double sizeOfIntersection = 0;
		double sizeOfMerge = 0;
		for (Requirement r1 : cc.getRequirements()) {
			if (hc.getRequirements().contains(r1)) {
				sizeOfIntersection++;
			}
		}
		for (Requirement r1 : testData) {
			int computerClusterIndex = getIndex(computerClusters, r1);
			int humanClusterIndex = getIndex(humanClusters, r1);
			if (computerClusterIndex != -1 && (computerClusters.get(computerClusterIndex) == cc
					|| humanClusters.get(humanClusterIndex) == hc)) {
				sizeOfMerge++;
			}
		}
		return (sizeOfIntersection / sizeOfMerge);
	}

	/**
	 *
	 * @param computerClusters
	 * @param humanClusters
	 * @param testData
	 * @return the average rand between all cc and hc
	 */
	public static double calculateRandAvg(final List<Cluster> computerClusters, final List<Cluster> humanClusters,
			final List<Requirement> testData) {
		double rand = 0;
		for (int i = 0; i < computerClusters.size(); i++) {
			Cluster cc = computerClusters.get(i);
			Cluster hc = humanClusters.get(i);

			rand += calculateRand(cc, hc, computerClusters, humanClusters, testData);
		}

		return rand / computerClusters.size();

	}

	/**
	 *
	 * @param cc
	 * @param hc
	 * @param computerClusters
	 * @param humanClusters
	 * @param testData
	 * @return the jaccard between cc and hc
	 */
	public static double calculateRand(final Cluster cc, final Cluster hc, final List<Cluster> computerClusters,
			final List<Cluster> humanClusters, final List<Requirement> testData) {
		double sizeOfIntersection = 0;
		double sizeOfOutside = 0;
		for (Requirement r1 : cc.getRequirements()) {
			if (hc.getRequirements().contains(r1)) {
				sizeOfIntersection++;
			}
		}
		for (Requirement r1 : testData) {
			int computerClusterIndex = getIndex(computerClusters, r1);
			int humanClusterIndex = getIndex(humanClusters, r1);
			if (computerClusterIndex == -1 || (computerClusters.get(computerClusterIndex) != cc
					&& humanClusters.get(humanClusterIndex) != hc)) {
				sizeOfOutside++;
			}
		}
		return (sizeOfIntersection + sizeOfOutside) / testData.size();
	}

	private static double cohesionOfbj(final RequirementVector v1, final List<RequirementVector> ownCluster,
			final DistanceFunction distanceFunction) {
		double cohesion = 0;
		for (RequirementVector v2 : ownCluster) {
			double dist = distanceFunction.distanceZ(v1.getVectorRepresentation(), v2.getVectorRepresentation());
			cohesion += dist;
		}
		if (ownCluster.size() > 0) {
			return cohesion / ownCluster.size();
		} else {
			return cohesion;
		}
	}

	private static double distToSecond(final RequirementVector v1, final List<RequirementVector> ownCluster,
			final List<List<RequirementVector>> clusters, final DistanceFunction distanceFunction) {
		double minDist = Double.MAX_VALUE;
		for (List<RequirementVector> cluster : clusters) {
			if (cluster != ownCluster) {
				double dist = 0;
				for (RequirementVector v2 : cluster) {
					dist += distanceFunction.distanceZ(v1.getVectorRepresentation(), v2.getVectorRepresentation());
				}
				if (cluster.size() > 0) {
					dist = dist / cluster.size();
				}
				// if cluster is empty, then now dist would be 0. But empty
				// clusters should be treated as outliers
				// so it will be set to double.max. This means it will not be
				// chosen as a close cluster
				else {
					dist = Double.MAX_VALUE;
				}
				if (dist < minDist) {
					minDist = dist;
				}
			}
		}
		return minDist;
	}

	// good if close to 1, bad if close to -1 (good if closer to own than to
	// other cluster)
	private static double silhouetteOfObj(final RequirementVector v1, final List<RequirementVector> ownCluster,
			final List<List<RequirementVector>> clusters, final DistanceFunction distanceFunction) {
		double a = cohesionOfbj(v1, ownCluster, distanceFunction);
		double b = distToSecond(v1, ownCluster, clusters, distanceFunction);
		if (a == 0) {
			return 0;
		} else {
			double s = b - a;
			if (a > b) {
				return s / a;
			} else {
				return s / b;
			}
		}
	}

	/**
	 * returns silhouette of the clustering. Good are values betwwen 1.0 and
	 * 0.7, ok between 0.7 and 0.5, everything below 0.25 is bad
	 *
	 * @param clusters
	 * @param distanceFunction
	 * @return
	 */
	public static double silhouetteOfClustering(final List<List<RequirementVector>> clusters,
			final DistanceFunction distanceFunction) {
		double silAll = 0;
		for (List<RequirementVector> cluster : clusters) {
			double silCluster = 0;
			for (RequirementVector v1 : cluster) {
				silCluster += silhouetteOfObj(v1, cluster, clusters, distanceFunction);
			}
			if (cluster.size() > 0) {
				silCluster = silCluster / cluster.size();
			}
			silAll += silCluster;
		}
		silAll = silAll / clusters.size();
		return silAll;
	}

	private static double cohesionOfCluster(final List<RequirementVector> cluster,
			final DistanceFunction distanceFunction) {
		double cohesion = 0;
		for (RequirementVector v1 : cluster) {
			cohesion += cohesionOfbj(v1, cluster, distanceFunction);
		}
		if (cluster.size() > 0) {
			cohesion = cohesion / cluster.size();
		}
		return cohesion;
	}

	/**
	 * Calculates the distance between a point to points from his own Cluster.
	 * The results are then averaged per Cluster and then per clustering
	 *
	 * @param clusters
	 * @param distanceFunction
	 * @return
	 */
	public static double cohesionOfClustering(final List<List<RequirementVector>> clusters,
			final DistanceFunction distanceFunction) {
		double cohesion = 0;
		for (List<RequirementVector> cluster : clusters) {
			cohesion += cohesionOfCluster(cluster, distanceFunction);
		}
		cohesion = cohesion / clusters.size();
		return cohesion;
	}

	private static double seperationOfObj(final RequirementVector v1, final List<RequirementVector> ownCluster,
			final List<List<RequirementVector>> clusters, final DistanceFunction distanceFunction) {
		double seperation = 0;
		for (List<RequirementVector> cluster : clusters) {
			if (cluster != ownCluster) {
				double dist = 0;
				for (RequirementVector v2 : cluster) {
					dist += distanceFunction.distanceZ(v1.getVectorRepresentation(), v2.getVectorRepresentation());
				}

				if (cluster.size() > 0) {
					dist = dist / cluster.size();
				}
				seperation += dist;
			}
		}
		seperation = seperation / clusters.size();
		return seperation;
	}

	private static double seperationOfCluster(final List<RequirementVector> cluster,
			final DistanceFunction distanceFunction, final List<List<RequirementVector>> clusters) {
		double seperation = 0;
		for (RequirementVector v1 : cluster) {
			seperation += seperationOfObj(v1, cluster, clusters, distanceFunction);
		}
		if (cluster.size() > 0) {
			seperation = seperation / cluster.size();
		}
		return seperation;
	}

	/**
	 * Calculates the distance between a point to points from other clusters.
	 * The results are then averaged per Cluster and then per clustering
	 *
	 * @param clusters
	 * @param distanceFunction
	 * @return
	 */
	public static double seperationOfClustering(final List<List<RequirementVector>> clusters,
			final DistanceFunction distanceFunction) {
		double seperation = 0;
		for (List<RequirementVector> cluster : clusters) {
			seperation += seperationOfCluster(cluster, distanceFunction, clusters);
		}
		seperation = seperation / clusters.size();
		return seperation;
	}

	private static int getIndex(final List<Cluster> computerCluster, final Requirement requirement) {
		for (int i = 0; i < computerCluster.size(); i++) {
			if (computerCluster.get(i).getRequirements().size() > 0
					&& computerCluster.get(i).getRequirements().contains(requirement)) {
				return i;
			}
		}
		return -1;
	}

	public static double[] internalMeasurements(final List<List<RequirementVector>> clusters,
			final DistanceFunction distanceFunction) {
		double returnArray[] = new double[3];
		double silAll = 0;
		double seperation = 0;
		double cohesion = 0;
		for (List<RequirementVector> cluster : clusters) {
			double clusterSeperation = 0;
			double clusterCohesion = 0;
			double silCluster = 0;
			for (RequirementVector v1 : cluster) {
				double[] values = valuesOfCluster(v1, cluster, clusters, distanceFunction);
				clusterCohesion += values[0];
				clusterSeperation += values[1];
				silCluster += values[2];

			}
			if (cluster.size() > 0) {
				clusterCohesion = clusterCohesion / cluster.size();
				clusterSeperation = clusterSeperation / cluster.size();
				silCluster = silCluster / cluster.size();
			}
			silAll += silCluster;
			cohesion += clusterCohesion;
			seperation += clusterSeperation;
		}
		returnArray[0] = cohesion / clusters.size();
		returnArray[1] = seperation / clusters.size();
		returnArray[2] = silAll / clusters.size();
		return returnArray;
	}

	private static double[] valuesOfCluster(final RequirementVector v1, final List<RequirementVector> ownCluster,
			final List<List<RequirementVector>> clusters, final DistanceFunction distanceFunction) {
		double returnArray[] = new double[3];
		double a = cohesionOfbj(v1, ownCluster, distanceFunction);
		returnArray[0] = a;
		double[] values = distToSecondAndSeperation(v1, ownCluster, clusters, distanceFunction);
		double b = values[0];
		returnArray[1] = values[1];
		if (a == 0) {
			returnArray[2] = 0;
			;
		} else {
			double s = b - a;
			if (a > b) {
				returnArray[2] = s / a;
			} else {
				returnArray[2] = s / b;
			}
		}
		return returnArray;
	}

	private static double[] distToSecondAndSeperation(final RequirementVector v1,
			final List<RequirementVector> ownCluster, final List<List<RequirementVector>> clusters,
			final DistanceFunction distanceFunction) {
		double returnArray[] = new double[2];
		double minDist = Double.MAX_VALUE;
		double seperationOfObj = 0;
		for (List<RequirementVector> cluster : clusters) {
			if (cluster != ownCluster) {
				double dist = 0;
				for (RequirementVector v2 : cluster) {
					dist += distanceFunction.distanceZ(v1.getVectorRepresentation(), v2.getVectorRepresentation());
				}
				if (cluster.size() > 0) {
					dist = dist / cluster.size();
					seperationOfObj += dist;
				}
				// if cluster is empty, then now dist would be 0. But empty
				// clusters should be treated as outliers
				// so it will be set to double.max. This means it will not be
				// chosen as a close cluster
				else {
					seperationOfObj += dist;
					dist = Double.MAX_VALUE;
				}

				if (dist < minDist) {
					minDist = dist;
				}
			}
		}
		seperationOfObj = seperationOfObj / clusters.size();
		returnArray[0] = minDist;
		returnArray[1] = seperationOfObj;
		return returnArray;
	}

}
