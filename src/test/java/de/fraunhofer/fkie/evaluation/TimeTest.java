package de.fraunhofer.fkie.evaluation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.fraunhofer.fkie.aidpfm.model.Node;
import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.fraunhofer.fkie.aidpfm.utilities.Datasets;
import de.fraunhofer.fkie.aidpfm.utilities.ModelContainer;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB.Query;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.clusterfunction.FuzzyCMeans;
import de.fraunhofer.fkie.evaluation.clusterfunction.KMeans;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceCalculatorNormal;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceCalculatorRupp;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceHandler;
import de.fraunhofer.fkie.evaluation.distancefunction.EuclideanDistance;
import de.fraunhofer.fkie.evaluation.model.Cluster;
import de.fraunhofer.fkie.evaluation.model.EvaluationConfig;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;
import de.fraunhofer.fkie.evaluation.model.Result;
import mikera.vectorz.Vector;
import weka.core.Debug.Random;

public class TimeTest {
	ModelContainer container;
	EvaluationConfig settings;
	List<RequirementVector> vectors;
	Result humanReference;

	@Before
	public void setupDependencies() throws Exception {
		Main.DATASET = Datasets.ALPHA;
		List<Requirement> requirements = R7DB.getRequirements(Query.ALL, Datasets.ALPHA.db);
		Node<String> testData;
		try (FileInputStream fis = new FileInputStream(Datasets.ALPHA.testData);
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			testData = (Node<String>) ois.readObject();
		}
		ClusterDataGenerator generator = new ClusterDataGenerator();
		// side effects on testdata!
		this.container = Main.getRequirementsFromDataset(requirements, testData, generator, false);
		this.humanReference = Main.generateHumanReference(testData, this.container, generator);
		this.settings = new EvaluationConfig();
		this.settings.allFalse();
		this.settings.setRuppInterpretation(true);
		this.settings.setLemmatized(true);
		this.settings.setStopWords(true);
		this.settings.setTfidf(true);
		// this.settings.setGermaNetFunction(new GermaNetShotgun());
		this.vectors = generator.generateVectors(this.container, this.settings);
	}

	@Ignore
	public void testTimeVectorzDistance() {
		Random random = new Random();
		double maxDist = 0;
		EuclideanDistance euclidean = new EuclideanDistance();
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			int x = random.nextInt(this.vectors.size());
			int y = random.nextInt(this.vectors.size());
			RequirementVector v1 = this.vectors.get(x);
			RequirementVector v2 = this.vectors.get(y);
			double dist = euclidean.distanceZ(v1.getVectorRepresentation(), v2.getVectorRepresentation());
		}
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);
	}

	@Ignore
	public void testTimeVectorzNormalize() {

		long startTime = System.currentTimeMillis();
		Random random = new Random();
		Vector initVector = this.vectors.get(0).getVectorRepresentation();
		for (int i = 0; i < 10000; i++) {
			int x = random.nextInt(this.vectors.size());
			Vector v1 = this.vectors.get(x).getVectorRepresentation();
			initVector.add(v1);
		}
		for (int i = 0; i < 10; i++) {
			initVector.divide(random.nextInt(10));
		}
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);
	}

	@Test
	public void testTimeKMeansHashMap() throws Exception {
		Main.DATASET = Datasets.ALPHA;
		KMeans ng = new KMeans(Datasets.ALPHA.kTest, 20);

		DistanceHandler handler = new DistanceCalculatorRupp(this.vectors);

		// print vectors
		String vectorsString = "";
		for (RequirementVector vector : this.vectors) {
			String vectorString = "";
			for (double x : vector.getVectorRepresentation()) {
				vectorString += x + ";";
			}
			vectorsString += vectorString + "\n";
		}
		Util.writeUtf8File(Config.RESULT + "/vectorsResult.csv", vectorsString);

		handler.setDistanceFunction(new EuclideanDistance());
		long times = 0;
		for (int i = 0; i < 5; i++) {
			ng.setRandomFile(i, false);
			long start = System.currentTimeMillis();
			List<List<RequirementVector>> clusters = ng.evaluationCluster(this.vectors, handler);
			long end = System.currentTimeMillis();
			System.out.println("Time for iteration " + i + " : " + (end - start));
			times += end - start;

			ClusterExecutor group = new ClusterExecutor();
			List<Cluster> cluster = new ArrayList<Cluster>();
			for (List<RequirementVector> list : clusters) {
				cluster.add(new Cluster(group.translate(list, this.container.getRequirementsUnmodifiable())));
			}
			Result result = new Result(cluster, (end - start));
			List<int[]> mappings2 = group.getGreedyExternalMappings(result, this.humanReference,
					this.container.getRequirementsUnmodifiable());

			// also sets precision, recall, f1 and naive
			result.setAccordance(group.compareExternalMetrics(result, this.humanReference,
					this.container.getRequirementsUnmodifiable(), mappings2));
			result.setAccordance(group.compareInternalMetrics(clusters, result, new EuclideanDistance()));
			System.out.println("F1 of iteration " + i + " is: " + result.getF1());
		}
		System.out.println("Average: " + (times / 5));

		// test if both after another also work
		ClusterDataGenerator generator = new ClusterDataGenerator();
		this.settings.allFalse();
		this.settings.setRuppInterpretation(true);
		this.settings.setTfidf(true);
		this.vectors = generator.generateVectors(this.container, this.settings);
		Main.DATASET = Datasets.ALPHA;
		KMeans kMeans = new KMeans(Datasets.ALPHA.kTest, 20);
		DistanceHandler handler2 = new DistanceCalculatorNormal();
		handler2.setDistanceFunction(new EuclideanDistance());
		long times2 = 0;
		for (int i = 0; i < 5; i++) {
			kMeans.setRandomFile(i, false);
			long start = System.currentTimeMillis();
			List<List<RequirementVector>> clusters = kMeans.evaluationCluster(this.vectors, handler2);
			long end = System.currentTimeMillis();
			System.out.println("Time for iteration " + i + " : " + (end - start));
			times += end - start;

			ClusterExecutor group = new ClusterExecutor();
			List<Cluster> cluster = new ArrayList<Cluster>();
			for (List<RequirementVector> list : clusters) {
				cluster.add(new Cluster(group.translate(list, this.container.getRequirementsUnmodifiable())));
			}
			Result result = new Result(cluster, (end - start));
			List<int[]> mappings2 = group.getGreedyExternalMappings(result, this.humanReference,
					this.container.getRequirementsUnmodifiable());

			// also sets precision, recall, f1 and naive
			result.setAccordance(group.compareExternalMetrics(result, this.humanReference,
					this.container.getRequirementsUnmodifiable(), mappings2));
			result.setAccordance(group.compareInternalMetrics(clusters, result, new EuclideanDistance()));
			System.out.println("F1 of iteration " + i + " is: " + result.getF1());
		}
		System.out.println("Average: " + (times2 / 5));
	}

	@Ignore
	public void testTimeKMeansVectorz() throws IOException {

		FuzzyCMeans kMeans = new FuzzyCMeans(Datasets.ALPHA.kVerification, 20, 2);
		DistanceHandler handler = new DistanceCalculatorRupp(this.vectors);
		handler.setDistanceFunction(new EuclideanDistance());
		long times = 0;
		for (int i = 0; i < 5; i++) {
			// kMeans.setRandomFile(i, false);
			long start = System.currentTimeMillis();
			List<List<RequirementVector>> clusters = kMeans.evaluationCluster(this.vectors, handler);
			long end = System.currentTimeMillis();
			System.out.println("Time for iteration " + i + " : " + (end - start));
			times += end - start;

			ClusterExecutor group = new ClusterExecutor();
			List<Cluster> cluster = new ArrayList<Cluster>();
			for (List<RequirementVector> list : clusters) {
				cluster.add(new Cluster(group.translate(list, this.container.getRequirementsUnmodifiable())));
			}
			Result result = new Result(cluster, (end - start));
			List<int[]> mappings2 = group.getGreedyExternalMappings(result, this.humanReference,
					this.container.getRequirementsUnmodifiable());

			// also sets precision, recall, f1 and naive
			long startExt = System.currentTimeMillis();
			result.setAccordance(group.compareExternalMetrics(result, this.humanReference,
					this.container.getRequirementsUnmodifiable(), mappings2));
			long endExt = System.currentTimeMillis();
			result.setAccordance(group.compareInternalMetrics(clusters, result, new EuclideanDistance()));
			long endInt = System.currentTimeMillis();
			System.out.println("F1 of iteration " + i + " is: " + result.getF1());
			System.out.println("Cohesion of iteration " + i + " is: " + result.getCohesion());
			System.out.println("Seperation of iteration " + i + " is: " + result.getSeperation());
			System.out.println("Silhouette of iteration " + i + " is: " + result.getSilhouette());
			System.out.println("Time ext: " + (endExt - startExt) + " Time int:" + (endInt - endExt));
		}
		System.out.println("Average: " + (times / 5));
	}
}
