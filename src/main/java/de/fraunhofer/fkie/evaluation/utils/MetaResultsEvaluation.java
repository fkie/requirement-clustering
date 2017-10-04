package de.fraunhofer.fkie.evaluation.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.fkie.aidpfm.model.Node;
import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.fraunhofer.fkie.aidpfm.utilities.Datasets;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB.Query;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.ClusterDataGenerator;
import de.fraunhofer.fkie.evaluation.ClusterExecutor;
import de.fraunhofer.fkie.evaluation.clusterfunction.KMeans;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceFunction;
import de.fraunhofer.fkie.evaluation.distancefunction.EuclideanDistance;
import de.fraunhofer.fkie.evaluation.model.Cluster;
import de.fraunhofer.fkie.evaluation.model.EvaluationConfig;
import de.fraunhofer.fkie.evaluation.model.MetaResult;
import de.fraunhofer.fkie.evaluation.model.Result;

/**
 * This is a tool class. Methods print out .csv files you can evaluate. Simply
 * put the method name in the main method
 *
 * @author daniel.toews
 *
 */
public class MetaResultsEvaluation {
	// public static File FOLDER = new File(Config.DEBUGFOLDER);
	public static Datasets DATASET = Datasets.GAMMA;
	public static Logger LOG = LoggerFactory.getLogger(MetaResultsEvaluation.class);

	public static void main(final String[] args) throws FileNotFoundException, ClassNotFoundException, IOException {

		// hard coded methods, depending on your specific wishes
		// EvaluationConfig settingsA = getConfigA();
		// EvaluationConfig settingsB = getConfigB();
		// // for names
		// KMeans kmeans = new KMeans(23, 20);
		// DistanceFunction distance = new CosineDistance();
		// settingsA.setClusterer(kmeans.getName());
		// settingsA.setDistance(distance.getName());
		// settingsB.setClusterer(kmeans.getName());
		// settingsB.setDistance(distance.getName());
		addF1Weighted();
		// outputResultsParallel();

	}

	// hard coded. write your specific configurations here and in the method
	// below
	private static EvaluationConfig getConfigA() {
		EvaluationConfig settings = new EvaluationConfig();
		settings.setTfidf(true);
		settings.setLemmatized(true);
		settings.setStopWords(false);
		settings.setUpperCase(false);
		settings.setRuppInterpretation(false);
		settings.setOnthology(false);
		settings.setSource(false);
		settings.setSynonyms(false);

		return settings;
	}

	private static EvaluationConfig getConfigB() {
		EvaluationConfig settings = new EvaluationConfig();
		settings.setTfidf(false);
		settings.setLemmatized(false);
		settings.setStopWords(false);
		settings.setUpperCase(false);
		settings.setRuppInterpretation(false);
		settings.setOnthology(false);
		settings.setSource(false);
		settings.setSynonyms(false);

		return settings;
	}

	public static void addF1Weighted() throws ClassNotFoundException, FileNotFoundException, IOException {
		for (Datasets data : Datasets.values()) {
			// first global things for the dataset
			Query query = Query.ALL;
			List<Requirement> requirements = R7DB.getRequirements(query, data.db);
			Node<String> testData = getTestData();
			ClusterDataGenerator generator = new ClusterDataGenerator();
			List<Requirement> testRequirements = generator.toRequirementList(testData, requirements);
			// Human reference
			Result humanReference = generator.humanReferenceAlpha(testData, requirements);

			File file = new File(data.outputVerificationData);
			List<Integer> streamIterations = new ArrayList<>();
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				streamIterations.add(i);
			}
			// now look at all evaluations
			Map<Integer, String> parallelizationResults = new ConcurrentHashMap<>();
			streamIterations.parallelStream().forEach(x -> {
				File fileEntry = files[x];
				String s = fileEntry.getName();
				MetaResult meta;
				if (s.contains("ser") && !s.contains("weightAdded")) {
					try (FileInputStream fis = new FileInputStream(fileEntry);
							ObjectInputStream ois = new ObjectInputStream(fis)) {
						Object o = ois.readObject();
						ois.close();
						if (o instanceof MetaResult) {
							meta = (MetaResult) o;
							// all of the 100 results of this evaluation
							double[] f1 = new double[meta.getResults().size()];
							double[] precision = new double[meta.getResults().size()];
							double[] recall = new double[meta.getResults().size()];
							for (int i = 0; i < meta.getResults().size(); i++) {
								Result r = meta.getResults().get(i);
								List<Cluster> resultWithObjectReferences = getObjectReferences(r.getClusters(),
										requirements);
								Result referenceResult = new Result(resultWithObjectReferences, 1);
								// get the Mapping needed
								int[] mapping = ClusterExecutor.getF1MaxMapping(referenceResult, humanReference);
								List<Cluster> mappedClustering = ClusterExecutor
										.getClusterMapping(resultWithObjectReferences, mapping);
								double[] weightedF1 = Metrics.calculateF1WeightedAvg(mappedClustering,
										humanReference.getClusters(), requirements);
								f1[i] = weightedF1[2];
								precision[i] = weightedF1[0];
								recall[i] = weightedF1[1];

							}
							meta.setF1Weighted(f1);
							meta.setPrecisionWeighted(precision);
							meta.setRecallWeighted(recall);
							LOG.info("Loading File {} from up to {} files: {}", parallelizationResults.size(),
									file.listFiles().length, fileEntry.getName());
							LOG.info("F1 normal: {}, F1 weighted: {}", meta.getF1Avg(), meta.getF1WeightedAvg());
							// make smaller for faster analysis
							List<Result> results = meta.getResults();
							meta.setResults(null);
							try (FileOutputStream fos = new FileOutputStream(
									data.outputVerificationData + " small/" + s);
									ObjectOutputStream oos = new ObjectOutputStream(fos)) {
								oos.writeObject(meta);
							}

							meta.setResults(results);
							try (FileOutputStream fos = new FileOutputStream(data.outputVerificationData + "/"
									+ s.split("\\.")[0] + "weightAdded." + s.split("\\.")[1]);
									ObjectOutputStream oos = new ObjectOutputStream(fos)) {
								oos.writeObject(meta);
							}
							Path source = Paths.get(fileEntry.getAbsolutePath());
							Files.delete(source);
						}

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

				}
			});
		}

	}

	/**
	 * compare the content of two Meta results. Which requirements are sorted
	 * correct by both and which are sorted correct by one not the other?
	 *
	 * @param settingsA
	 *            describes metaresult 1 (has to be already produced!)
	 * @param settingsB
	 *            describes metaresult 2 (has to be already produced!
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 */

	public static void compareClusterContent(final EvaluationConfig settingsA, final EvaluationConfig settingsB)
			throws FileNotFoundException, ClassNotFoundException, IOException {
		// initialize testReq and GoldenStandard
		Query query = Query.ALL;
		List<Requirement> requirements = R7DB.getRequirements(query, Config.ALPHA_DB);
		Node<String> testData = getTestData();
		ClusterDataGenerator generator = new ClusterDataGenerator();
		List<Requirement> testRequirements = generator.toRequirementList(testData, requirements);
		// Human reference
		Result humanReference = generator.humanReferenceAlpha(testData, requirements);
		// get Metaresults
		MetaResult MetaResultA = loadMeta(settingsA);
		MetaResult MetaResultB = loadMeta(settingsB);
		int index = Util.getArrayMaxIndex(MetaResultA.getF1());
		Result resultA = MetaResultA.getResults().get(index);
		Result resultB = MetaResultB.getResults().get(index);

		// reconstruct lost references when saved and loaded
		List<Cluster> clusteringAWithObjectReferences = getObjectReferences(resultA.getClusters(), testRequirements);
		List<Cluster> clusteringBWithObjectReferences = getObjectReferences(resultB.getClusters(), testRequirements);
		Result referenceResult = new Result(clusteringAWithObjectReferences, 1);
		// get the Mapping needed
		int[] mapping = ClusterExecutor.getF1MaxMapping(referenceResult, humanReference);
		// make the correct clustered lists
		List<Cluster> mappedClusteringA = ClusterExecutor.getClusterMapping(clusteringAWithObjectReferences, mapping);
		List<Cluster> mappedClusteringB = ClusterExecutor.getClusterMapping(clusteringBWithObjectReferences, mapping);
		List<Requirement> correctClusteredA = getCorrectClusteredReq(mappedClusteringA, humanReference.getClusters());
		List<Requirement> correctClusteredB = getCorrectClusteredReq(mappedClusteringB, humanReference.getClusters());

		List<Requirement> bothCorrect = new ArrayList<>();
		for (Requirement requirement : correctClusteredA) {
			if (correctClusteredB.contains(requirement)) {
				bothCorrect.add(requirement);
			}
		}
		// mitigate concurrent modification
		for (Requirement requirement : bothCorrect) {
			correctClusteredA.remove(requirement);
			correctClusteredB.remove(requirement);
		}

		// create output
		String csvContent = "";
		String head = "correct by A (" + settingsA.subtract(settingsB) + ");Correct by Both;correct by B ("
				+ settingsB.subtract(settingsA) + ")\n";
		csvContent += head;
		int i = 0;
		while (i < bothCorrect.size() || i < correctClusteredA.size() || i < correctClusteredB.size()) {
			String line = "";
			if (i < correctClusteredA.size()) {
				line += correctClusteredA.get(i).get(Field.TITEL);
			}
			if (i < bothCorrect.size()) {
				line += ";" + bothCorrect.get(i).get(Field.TITEL);
			}
			if (i < correctClusteredB.size()) {
				line += ";" + correctClusteredB.get(i).get(Field.TITEL);
			}
			csvContent += line + "\n";
			i++;
		}
		Util.writeUtf8File(Config.AIDPFM_VSNFD + "/clusteringComparisonLemmatized.csv", csvContent);
	}

	public static void compareClusterContentAverage(final EvaluationConfig settingsA, final EvaluationConfig settingsB)
			throws FileNotFoundException, ClassNotFoundException, IOException {
		// initialize testReq and GoldenStandard
		Query query = Query.ALL;
		List<Requirement> requirements = R7DB.getRequirements(query, Config.ALPHA_DB);
		Node<String> testData = getTestData();
		ClusterDataGenerator generator = new ClusterDataGenerator();
		List<Requirement> testRequirements = generator.toRequirementList(testData, requirements);
		// Human reference
		Result humanReference = generator.humanReferenceAlpha(testData, requirements);
		// get Metaresults
		MetaResult MetaResultA = loadMeta(settingsA);
		MetaResult MetaResultB = loadMeta(settingsB);
		List<Integer> correctA = new ArrayList<>();
		List<Integer> correctB = new ArrayList<>();
		List<Integer> correctBoth = new ArrayList<>();
		for (int i = 0; i < MetaResultA.getResults().size(); i++) {
			Result resultA = MetaResultA.getResults().get(i);
			Result resultB = MetaResultB.getResults().get(i);

			// reconstruct lost references when saved and loaded
			List<Cluster> clusteringAWithObjectReferences = getObjectReferences(resultA.getClusters(),
					testRequirements);
			List<Cluster> clusteringBWithObjectReferences = getObjectReferences(resultB.getClusters(),
					testRequirements);
			Result referenceResult = new Result(clusteringAWithObjectReferences, 1);
			// get the Mapping needed
			int[] mapping = ClusterExecutor.getF1MaxMapping(referenceResult, humanReference);
			// make the correct clustered lists
			List<Cluster> mappedClusteringA = ClusterExecutor.getClusterMapping(clusteringAWithObjectReferences,
					mapping);
			List<Cluster> mappedClusteringB = ClusterExecutor.getClusterMapping(clusteringBWithObjectReferences,
					mapping);
			List<Requirement> correctClusteredA = getCorrectClusteredReq(mappedClusteringA,
					humanReference.getClusters());
			List<Requirement> correctClusteredB = getCorrectClusteredReq(mappedClusteringB,
					humanReference.getClusters());

			List<Requirement> bothCorrect = new ArrayList<>();
			for (Requirement requirement : correctClusteredA) {
				if (correctClusteredB.contains(requirement)) {
					bothCorrect.add(requirement);
				}
			}
			// mitigate concurrent modification
			for (Requirement requirement : bothCorrect) {
				correctClusteredA.remove(requirement);
				correctClusteredB.remove(requirement);
			}
			correctA.add(correctClusteredA.size());
			correctB.add(correctClusteredB.size());
			correctBoth.add(bothCorrect.size());
		}
		double averageA = Util.averageList(correctA);
		double averageB = Util.averageList(correctB);
		List<Integer> differenceList = new ArrayList<>();
		List<Integer> winningDifferenceA = new ArrayList<>();
		List<Integer> winningDifferenceB = new ArrayList<>();
		int winA = 0, winB = 0;
		for (int i = 0; i < correctA.size(); i++) {
			int difference = correctA.get(i) - correctB.get(i);
			differenceList.add(difference);
			if (difference > 0) {
				winA++;
				winningDifferenceA.add(difference);
			}
			if (difference < 0) {
				winB++;
				winningDifferenceB.add(Math.abs(difference));
			}
		}
		double averageDifference = Util.averageList(differenceList);
		double averageWinA = Util.averageList(winningDifferenceA);
		double averageWinB = Util.averageList(winningDifferenceB);
		// create output
		String csvContent = "";
		csvContent += "Average only correct by A;" + averageA + ";Average only correct by B;" + averageB + "\n";
		csvContent += "Average difference;" + averageDifference + "\n";
		csvContent += "Number of wins by A;" + winA + ";Number of wins by B;" + winB + "\n";
		csvContent += "Average difference when A wins;" + averageWinA + ";Average difference when B wins;" + averageWinB
				+ "\n";
		Util.writeUtf8File(Config.AIDPFM_VSNFD + "/clusteringComparisonTFIDFLemmatized.csv", csvContent);
	}

	@SuppressWarnings("unchecked")
	public static Node<String> getTestData() throws IOException, ClassNotFoundException, FileNotFoundException {
		Node<String> testData;
		try (FileInputStream fis = new FileInputStream(Config.TESTDATA_ALPHA);
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			testData = (Node<String>) ois.readObject();
		}
		// get "zurückgestellt" out
		testData.getChildren().remove(2);
		return testData;
	}

	/**
	 * This method compares the correct clustered items of the best results for
	 * metric F1 and Metric Jaccard
	 *
	 * @param settingsA
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void compareDifferentMetrics(final EvaluationConfig settingsA)
			throws FileNotFoundException, ClassNotFoundException, IOException {
		// initialize testReq and GoldenStandard
		Query query = Query.ALL;
		List<Requirement> requirements = R7DB.getRequirements(query, Config.ALPHA_DB);
		Node<String> testData = getTestData();
		ClusterDataGenerator generator = new ClusterDataGenerator();
		List<Requirement> testRequirements = generator.toRequirementList(testData, requirements);
		// Human reference
		Result humanReference = generator.humanReferenceAlpha(testData, requirements);
		// get Metaresults
		MetaResult MetaResultA = loadMeta(settingsA);
		int indexF1 = Util.getArrayMaxIndex(MetaResultA.getF1());
		int indexJaccard = Util.getArrayMaxIndex(MetaResultA.getJaccard());
		Result resultA = MetaResultA.getResults().get(indexF1);
		Result resultB = MetaResultA.getResults().get(indexJaccard);

		// reconstruct lost references when saved and loaded
		List<Cluster> clusteringAWithObjectReferences = getObjectReferences(resultA.getClusters(), testRequirements);
		List<Cluster> clusteringBWithObjectReferences = getObjectReferences(resultB.getClusters(), testRequirements);
		Result referenceResultF1 = new Result(clusteringAWithObjectReferences, 1);
		Result referenceResultJaccard = new Result(clusteringBWithObjectReferences, 1);
		// get the Mapping needed
		int[] mappingF1 = ClusterExecutor.getF1MaxMapping(referenceResultF1, humanReference);
		int[] mappingJaccard = ClusterExecutor.getJaccardMaxMapping(referenceResultJaccard, humanReference,
				testRequirements);
		// make the correct clustered lists
		List<Cluster> mappedClusteringA = ClusterExecutor.getClusterMapping(clusteringAWithObjectReferences, mappingF1);
		List<Cluster> mappedClusteringB = ClusterExecutor.getClusterMapping(clusteringBWithObjectReferences,
				mappingJaccard);
		List<Requirement> correctClusteredA = getCorrectClusteredReq(mappedClusteringA, humanReference.getClusters());
		List<Requirement> correctClusteredB = getCorrectClusteredReq(mappedClusteringB, humanReference.getClusters());

		List<Requirement> bothCorrect = new ArrayList<>();
		for (Requirement requirement : correctClusteredA) {
			if (correctClusteredB.contains(requirement)) {
				bothCorrect.add(requirement);
			}
		}
		// mitigate concurrent modification
		for (Requirement requirement : bothCorrect) {
			correctClusteredA.remove(requirement);
			correctClusteredB.remove(requirement);
		}
		// create output
		String csvContent = "";
		String head = "correct by F1;Correct by Both;correct by Jaccard\n";
		csvContent += head;
		int i = 0;
		while (i < bothCorrect.size() || i < correctClusteredA.size() || i < correctClusteredB.size()) {
			String line = "";
			if (i < correctClusteredA.size()) {
				line += correctClusteredA.get(i).get(Field.TITEL);
			}
			if (i < bothCorrect.size()) {
				line += ";" + bothCorrect.get(i).get(Field.TITEL);
			}
			if (i < correctClusteredB.size()) {
				line += ";" + correctClusteredB.get(i).get(Field.TITEL);
			}
			csvContent += line + "\n";
			i++;
		}
		Util.writeUtf8File(Config.AIDPFM_VSNFD + "/clusteringMetricComparison.csv", csvContent);
	}

	/**
	 * prints all 100 F1 Accuarcy results of A and B next to each other
	 *
	 * @param settingsA
	 * @param settingsB
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void compareSingleAccuarcyResults(final EvaluationConfig settingsA, final EvaluationConfig settingsB)
			throws FileNotFoundException, ClassNotFoundException, IOException {
		MetaResult MetaResultA = loadMeta(settingsA);
		MetaResult MetaResultB = loadMeta(settingsB);

		String csvContent = "";
		String head = "F1 A (" + settingsA.subtract(settingsB) + "); F1 B (" + settingsB.subtract(settingsA)
				+ ");Difference;;Naive A (" + settingsA.subtract(settingsB) + "); Naive B ("
				+ settingsB.subtract(settingsA) + ");Difference";
		csvContent += head + "\n";

		for (int i = 0; i < MetaResultA.getF1().length; i++) {
			String line = "";
			// F1 content
			line += MetaResultA.getF1()[i] + ";" + MetaResultB.getF1()[i] + ";"
					+ (MetaResultA.getF1()[i] - MetaResultB.getF1()[i]) + ";;";
			// naive content
			line += MetaResultA.getNaive()[i] + ";" + MetaResultB.getNaive()[i] + ";"
					+ (MetaResultA.getNaive()[i] - MetaResultB.getNaive()[i]);
			csvContent += line + "\n";
		}
		Util.writeUtf8File(Config.AIDPFM_VSNFD + "/AccuarcyComparisonLemmatized.csv", csvContent);
	}

	public static void compareResultsByEps() throws ClassNotFoundException, FileNotFoundException, IOException {
		EvaluationConfig settingsA = getConfigA();
		// initialize testReq and GoldenStandard
		Query query = Query.ALL;
		List<Requirement> requirements = R7DB.getRequirements(query, Config.ALPHA_DB);
		Node<String> testData = getTestData();
		ClusterDataGenerator generator = new ClusterDataGenerator();
		List<Requirement> testRequirements = generator.toRequirementList(testData, requirements);
		// Human reference
		Result humanReference = generator.humanReferenceAlpha(testData, requirements);
		List<List<Requirement>> correctClustered = new ArrayList<>();
		List<Double> epsValues = new ArrayList<Double>();
		for (int i = 0; i < 20; i++) {
			double eps;
			if (i == 0) {
				eps = 0;
			} else if (i < 11) {
				eps = (double) 10 / i;
			} else {
				eps = 10 / (Math.pow(10, i));
			}
			epsValues.add(eps);
			KMeans kMeans = new KMeans(23, 20, eps);
			DistanceFunction distance = new EuclideanDistance();
			settingsA.setClusterer(kMeans.getName());
			settingsA.setDistance(distance.getName());
			// get Metaresults
			MetaResult MetaResultA = loadMeta(settingsA);
			Result resultA = MetaResultA.getResults().get(0);
			// Objectreference
			List<Cluster> clusteringAWithObjectReferences = getObjectReferences(resultA.getClusters(),
					testRequirements);
			Result referenceResultF1 = new Result(clusteringAWithObjectReferences, 1);
			// mapping
			int[] mappingF1 = ClusterExecutor.getF1MaxMapping(referenceResultF1, humanReference);
			List<Cluster> mappedClusteringA = ClusterExecutor.getClusterMapping(clusteringAWithObjectReferences,
					mappingF1);
			List<Requirement> correctClusteredA = getCorrectClusteredReq(mappedClusteringA,
					humanReference.getClusters());
			correctClustered.add(correctClusteredA);
		}

		String csvContent = "";
		String head = "";
		for (double eps : epsValues) {
			head += "Epsilon: " + eps + ";";
		}
		csvContent += head + "\n";
		boolean moreLines = true;
		int lineIndex = 0;
		while (moreLines) {
			moreLines = false;
			String line = "";
			for (List<Requirement> cluster : correctClustered) {
				if (cluster.size() > lineIndex) {
					moreLines = true;
					line += cluster.get(lineIndex).getTitel() + ";";
				} else {
					line += ";";
				}
			}
			lineIndex++;
			csvContent += line + "\n";
			lineIndex++;
		}
		Util.writeUtf8File(Config.AIDPFM_VSNFD + "/epsComparison.csv", csvContent);
	}

	/**
	 * Goes through all collected iterations and prints out which seed of the
	 * 100 had the best F1
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	// TODO get in after Weekend
	// public static void bestF1Iteration() throws FileNotFoundException,
	// IOException, ClassNotFoundException {
	// List<MetaResult> metas = new ArrayList<>();
	// int i = 0;
	// for(final File fileEntry : DATASET.listFiles()){
	// String s = fileEntry.getName();
	// if(s.contains("needed") || s.contains("HC")){
	// LOG.info("skipped result {}", i);
	// i++;
	// continue;
	// }
	// LOG.info(s);
	// MetaResult meta;
	// try(FileInputStream fis = new FileInputStream(DATASET + "/" + s);
	// ObjectInputStream ois = new ObjectInputStream(fis)){
	// meta = (MetaResult) ois.readObject();
	// }
	// metas.add(meta);
	// LOG.info("Loaded result {}", i);
	// i++;
	// }
	// // iteration, parametersSetToTrue
	// Map<Integer, IterationSettings> bestIterations = new HashMap<>();
	// for(MetaResult result : metas){
	// int best = Util.getArrayMaxIndex(result.getF1());
	// if(best == 99 && result.getF1()[0] == result.getF1()[99]){
	// best = -1;
	// }
	// if(bestIterations.containsKey(best)){
	// bestIterations.get(best).update(result);
	// }
	// else{
	// IterationSettings settings = new IterationSettings();
	// settings.update(result);
	// bestIterations.put(best, settings);
	// }
	// }
	//
	// String csvContent = "";
	// String head =
	// "Iteration;Amount;field1;field4;field7;neuralGas;kMeans;tfidf;stopWords;lemmatized;interpreted;upperCase;source";
	// csvContent += head + "\n";
	// for(Integer key : bestIterations.keySet()){
	// String line = "";
	// line += key + ";" + bestIterations.get(key).toString();
	// csvContent += line + "\n";
	// }
	// Util.writeUtf8File(Config.AIDPFM_VSNFD + "/bestIterations.csv",
	// csvContent);
	// }

	/**
	 * Helper class for bestIteration
	 *
	 * @author daniel.toews
	 *
	 */
	public static class IterationSettings {
		int amount;
		int field1;
		int field4;
		int field7;
		int kMeans;
		int neuralGas;
		int tfidf;
		int stopWords;
		int lemmatized;
		int interpreted;
		int upperCase;
		int source;

		public IterationSettings() {
			super();
			this.amount = 0;
			this.field1 = 0;
			this.field4 = 0;
			this.field7 = 0;
			this.kMeans = 0;
			this.neuralGas = 0;
			this.tfidf = 0;
			this.stopWords = 0;
			this.lemmatized = 0;
			this.interpreted = 0;
			this.upperCase = 0;
			this.source = 0;
		}

		public void update(final MetaResult result) {
			this.amount++;
			if (result.getFields().split(",").length == 1) {
				this.field1++;
			}
			if (result.getFields().split(",").length == 4) {
				this.field4++;
			}
			if (result.getFields().split(",").length == 7) {
				this.field7++;
			}
			if (result.getClusterer().contains("eural")) {
				this.neuralGas++;
			} else {
				this.kMeans++;
			}
			if (result.isTfidf()) {
				this.tfidf++;
			}
			if (result.isStopWords()) {
				this.stopWords++;
			}
			if (result.isLemmatized()) {
				this.lemmatized++;
			}
			if (result.isRuppInterpretation()) {
				this.interpreted++;
			}
			if (result.isUpperCase()) {
				this.upperCase++;
			}
			if (result.isSource()) {
				this.source++;
			}

		}

		@Override
		public String toString() {
			return this.amount + ";" + this.field1 + ";" + this.field4 + ";" + this.field7 + ";" + this.neuralGas + ";"
					+ this.kMeans + ";" + this.tfidf + ";" + this.stopWords + ";" + this.lemmatized + ";"
					+ this.interpreted + ";" + this.upperCase + ";" + this.source;
		}

	}

	private static List<Cluster> getObjectReferences(final List<Cluster> clusters,
			final List<Requirement> testRequirements) {
		List<Cluster> withReferences = new ArrayList<>();
		for (Cluster cluster : clusters) {
			List<Requirement> requirements = new ArrayList<>();
			for (Requirement requirementCluster : cluster.getRequirements()) {
				for (Requirement requirementData : testRequirements) {
					if (requirementCluster.get(Field.TEXT).equals(requirementData.get(Field.TEXT))) {
						requirements.add(requirementData);
						break;
					}
				}
			}
			Cluster referenceCluster = new Cluster(requirements);
			withReferences.add(referenceCluster);
		}
		return withReferences;
	}

	private static List<Requirement> getCorrectClusteredReq(final List<Cluster> cc, final List<Cluster> hc) {
		List<Requirement> correctClustered = new ArrayList<>();
		for (int i = 0; i < cc.size(); i++) {
			for (Requirement crequirement : cc.get(i).getRequirements()) {
				for (Requirement hrequirement : hc.get(i).getRequirements()) {
					if (crequirement.get(Field.TEXT).equals(hrequirement.get(Field.TEXT))) {
						correctClustered.add(hrequirement);
					}
				}
			}
		}
		return correctClustered;
	}

	private static MetaResult loadMeta(final EvaluationConfig settings1)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		MetaResult meta;
		String path = DATASET + "/" + settings1.toString() + ".ser";
		try (FileInputStream fis = new FileInputStream(DATASET + "/" + settings1.toString() + ".ser");
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			meta = (MetaResult) ois.readObject();
		}
		return meta;
	}

	/**
	 * write a new table of the contents in FOLDER
	 *
	 * @throws IOException
	 * @throws TemplateException
	 * @throws ClassNotFoundException
	 */
	// public static void outputResults() throws IOException, TemplateException,
	// ClassNotFoundException {
	// // load all metas
	// Configuration cfg = configureTemplate();
	// Map<String, Object> input = new HashMap<String, Object>();
	// List<MetaResult> metas = new ArrayList<>();
	// File file = new File(DATASET.outputTestData + " small");
	// int i = 0;
	// for (final File fileEntry : file.listFiles()) {
	// String s = fileEntry.getName();
	// MetaResult meta;
	// if (s.contains("ser")) {
	// try (FileInputStream fis = new FileInputStream(fileEntry);
	// ObjectInputStream ois = new ObjectInputStream(fis)) {
	// Object o = ois.readObject();
	// if (o instanceof MetaResult) {
	// meta = (MetaResult) o;
	// LOG.info("Loading File {} from up to {} files: {}", i,
	// file.listFiles().length,
	// fileEntry.getName());
	// metas.add(meta);
	// i++;
	// }
	// }
	//
	// }
	// // if(Double.isNaN(meta.getPrecisionAvg())){
	// // meta.setPrecision(new double[meta.getPrecision().length]);
	// // }
	//
	// }
	// System.out.println(metas.get(0).getFields());
	// input.put("Metas", metas);
	// createMetaOutputCSV(metas, cfg, input);
	// // createMetaOutputWithUtilities(metas,cfg,input);
	// createMetaOutputMD(metas, cfg, input);
	// LOG.info("Finished");
	// }

	// private static void createClusterMD(final Configuration cfg, final
	// Map<String, Object> input)
	// throws TemplateNotFoundException, MalformedTemplateNameException,
	// freemarker.core.ParseException,
	// IOException, TemplateException {
	// Template template = cfg.getTemplate("template.ftl");
	// Writer fileWriter = new FileWriter(new File("clusterMD.md"));
	// try {
	// template.process(input, fileWriter);
	// } finally {
	// fileWriter.close();
	// }
	// Writer consoleWriter = new OutputStreamWriter(System.out);
	// template.process(input, consoleWriter);
	// }

	// private static Configuration configureTemplate() throws IOException {
	// try {
	// Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
	//
	// // // Where do we load the templates from:
	// URI templatesFolder = Main.class.getResource("/templates").toURI();
	// cfg.setClassForTemplateLoading(Main.class, "/templates");
	//
	// // Some other recommended settings:
	// cfg.setIncompatibleImprovements(new Version(2, 3, 20));
	// cfg.setDefaultEncoding("UTF-8");
	// cfg.setLocale(Locale.US);
	// cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	// LOG.info("Template will be loaded from {}", templatesFolder.toString());
	// // cfg.setDirectoryForTemplateLoading(new File(
	// // templatesFolder));
	//
	// return cfg;
	// } catch (Exception e) {
	// throw new RuntimeException("Cannot open template path", e);
	// }
	// }

}
