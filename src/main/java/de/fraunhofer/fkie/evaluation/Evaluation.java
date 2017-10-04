package de.fraunhofer.fkie.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.fkie.aidpfm.germanet.GermaNetBestAncestor;
import de.fraunhofer.fkie.aidpfm.germanet.GermaNetFunction;
import de.fraunhofer.fkie.aidpfm.germanet.GermaNetShotgun;
import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.ModelContainer;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.clusterfunction.ClusterART1;
import de.fraunhofer.fkie.evaluation.clusterfunction.ClusterFunction;
import de.fraunhofer.fkie.evaluation.clusterfunction.FuzzyCMeans;
import de.fraunhofer.fkie.evaluation.clusterfunction.KMeans;
import de.fraunhofer.fkie.evaluation.clusterfunction.NeuralGas;
import de.fraunhofer.fkie.evaluation.distancefunction.CanberraDistance;
import de.fraunhofer.fkie.evaluation.distancefunction.ChebyshevDistance;
import de.fraunhofer.fkie.evaluation.distancefunction.CosineDistance;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceCalculatorNormal;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceCalculatorRupp;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceFunction;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceHandler;
import de.fraunhofer.fkie.evaluation.distancefunction.EuclideanDistance;
import de.fraunhofer.fkie.evaluation.distancefunction.HammingDistance;
import de.fraunhofer.fkie.evaluation.distancefunction.ManhattanDistance;
import de.fraunhofer.fkie.evaluation.model.EvaluationConfig;
import de.fraunhofer.fkie.evaluation.model.EvaluationContainer;
import de.fraunhofer.fkie.evaluation.model.MetaResult;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;
import de.fraunhofer.fkie.evaluation.model.Result;
import de.fraunhofer.fkie.evaluation.utils.UtilityFunctions;
import weka.clusterers.EM;
import weka.clusterers.HierarchicalClusterer;
import weka.core.Instances;
import weka.core.SelectedTag;

public class Evaluation {
	public static Logger LOG = LoggerFactory.getLogger(Evaluation.class);

	/**
	 * This method evaluates all possible parameters for the clustering and
	 * fills the objects containing the results and metrics
	 *
	 * @param input
	 *            - map for the template output
	 * @param metas
	 *            - currently empty list that will be filled in the method
	 * @param humanReference
	 *            - results provided by human experts
	 * @param testRequirements
	 *            - the test data
	 * @throws Exception
	 */
	public static void bigEvaluation(final Result humanReference, final ModelContainer container,
			final EvaluationConfig settings, final String output) throws Exception {

		// my kmeans
		KMeans kMeans;
		NeuralGas ng;
		ClusterART1 art;
		FuzzyCMeans fuzzy;
		if (!output.contains("Verification")) {
			kMeans = new KMeans(Main.DATASET.kTest, 20);
			ng = new NeuralGas(Main.DATASET.kTest);
			art = new ClusterART1(0.3, 1.5, Main.DATASET.kTest);
			fuzzy = new FuzzyCMeans(Main.DATASET.kTest, 20, 2);
		} else {
			kMeans = new KMeans(Main.DATASET.kVerification, 20);
			ng = new NeuralGas(Main.DATASET.kVerification);
			art = new ClusterART1(0.3, 1.5, Main.DATASET.kVerification);
			fuzzy = new FuzzyCMeans(Main.DATASET.kVerification, 20, 2);
		}
		// needed for kmeans clustering
		List<DistanceFunction> distances = new ArrayList<DistanceFunction>();
		distances.add(new EuclideanDistance());
		distances.add(new CosineDistance());
		distances.add(new CanberraDistance());
		distances.add(new ChebyshevDistance());
		distances.add(new ManhattanDistance());
		distances.add(new HammingDistance());
		List<weka.core.NormalizableDistance> wekaDistances = new ArrayList<>();
		wekaDistances.add(new weka.core.EuclideanDistance());
		wekaDistances.add(new weka.core.ManhattanDistance());
		wekaDistances.add(new weka.core.ChebyshevDistance());
		List<List<Field>> fieldCombs = new ArrayList<>();
		List<Field> combOne = new ArrayList<>();
		combOne.add(Field.OBJECTUNDERGÄNZUNG_4);
		fieldCombs.add(combOne);
		List<Field> combTwo = new ArrayList<>();
		combTwo.add(Field.SUBJEKT_1);
		combTwo.add(Field.OBJECTUNDERGÄNZUNG_4);
		combTwo.add(Field.PROZESSWORT_5);
		combTwo.add(Field.BEDINGUNG_7);
		fieldCombs.add(combTwo);
		List<Field> combThree = new ArrayList<>();
		combThree.add(Field.SUBJEKT_1);
		combThree.add(Field.VERBINDLICHKEIT_2);
		combThree.add(Field.AKTIVITÄT_3);
		combThree.add(Field.OBJECTUNDERGÄNZUNG_4);
		combThree.add(Field.PROZESSWORT_5);
		combThree.add(Field.QUALITÄT_6);
		combThree.add(Field.BEDINGUNG_7);
		fieldCombs.add(combThree);
		List<GermaNetFunction> germaNet = new ArrayList<>();
		germaNet.add(null);
		// germaNet.add(new GermaNetUniqueMapping());
		germaNet.add(new GermaNetShotgun());
		germaNet.add(new GermaNetBestAncestor());
		// germaNet.add(new GermaNetBestParentPath());
		int overAllCalculation = 0;
		boolean requirement = Main.DATASET.requirements;
		for (List<Field> fields : fieldCombs) {
			settings.setFields(fields);
			if (!requirement) {
				settings.setFields(combOne);
			}
			for (int sourceIndex = 0; sourceIndex < 2; sourceIndex++) {
				settings.setSource(!settings.isSource());
				if (!requirement) {
					settings.setSource(false);
				}
				for (int lemmatizeIndex = 0; lemmatizeIndex < 2; lemmatizeIndex++) {
					settings.setLemmatized(!settings.isLemmatized());
					for (GermaNetFunction function : germaNet) {
						settings.setGermaNetFunction(function);
						// only use synonyms with lemmatized
						if (settings.getGermaNetFunction() != null && !settings.isLemmatized()) {
							settings.setGermaNetFunction(null);
						}
						for (int synonymIndex = 0; synonymIndex < 2; synonymIndex++) {
							settings.setSynonyms(!settings.isSynonyms());
							// only use synonyms with lemmatized
							if (settings.isSynonyms() && !settings.isLemmatized()) {
								settings.setSynonyms(false);
							}
							for (int stopWordsIndex = 0; stopWordsIndex < 2; stopWordsIndex++) {
								settings.setStopWords(!settings.isStopWords());
								for (int interpretedIndex = 0; interpretedIndex < 2; interpretedIndex++) {
									if (!requirement) {
										settings.setRuppInterpretation(false);
									}
									settings.setRuppInterpretation(!settings.isRuppInterpretation());
									// settings.setRuppInterpretation(!settings.isRuppInterpretation());
									for (int tfidfIndex = 0; tfidfIndex < 2; tfidfIndex++) {
										settings.setTfidf(!settings.isTfidf());
										EvaluationContainer evaluationContainer = null;
										List<weka.clusterers.Clusterer> wekaClusters = new ArrayList<>();
										EM em = new EM();
										em.setNumClusters(Main.DATASET.kTest);
										HierarchicalClusterer hc = new HierarchicalClusterer();
										wekaClusters.add(em);
										wekaClusters.add(hc);
										int clusterIndex = 0;
										for (weka.clusterers.Clusterer clusterer : wekaClusters) {
											if (clusterIndex == 0) {

												settings.setClusterer("EM");
												settings.setDistance("Not needed");
												if (!checkExistence(settings, output)) {
													if (evaluationContainer == null) {
														evaluationContainer = init(container, settings, humanReference);
													}
													evaluate(1, evaluationContainer, true, clusterer, null, null,
															overAllCalculation, output);
													overAllCalculation++;
												}

											} else {
												int distanceIndex = 0;
												for (weka.core.NormalizableDistance distance : wekaDistances) {
													HierarchicalClusterer hcNewInstance = new HierarchicalClusterer();
													hcNewInstance.setNumClusters(Main.DATASET.kTest);
													// get old
													SelectedTag tag = hcNewInstance.getLinkType();
													// select to mean
													// Link
													SelectedTag newTag = new SelectedTag(3, tag.getTags());
													// set to mean
													hcNewInstance.setLinkType(newTag);
													// for output of
													// table
													hcNewInstance.setDistanceFunction(distance);
													String distanceName;
													if (distanceIndex == 0) {
														distanceName = "EuclideanDistance";
													} else if (distanceIndex == 1) {
														distanceName = "ManhattanDistance";
													} else {
														distanceName = "ChebyshevDistance";
													}
													settings.setClusterer("HC");
													settings.setDistance(distanceName);
													if (!checkExistence(settings, output)) {
														if (evaluationContainer == null) {
															evaluationContainer = init(container, settings,
																	humanReference);
														}

														evaluate(1, evaluationContainer, true, hcNewInstance, null,
																null, overAllCalculation, output);
														overAllCalculation++;

													}
													distanceIndex++;
												}
											}
											clusterIndex++;
										}

										settings.setClusterer("ClusterART");
										settings.setDistance("Not needed");
										if (!checkExistence(settings, output)) {
											if (evaluationContainer == null) {
												evaluationContainer = init(container, settings, humanReference);
											}
											evaluate(100, evaluationContainer, false, null, art,
													new EuclideanDistance(), overAllCalculation, output);
										}

										for (DistanceFunction distance : distances) {
											settings.setDistance(distance.getName());

											settings.setClusterer(ng.getName());
											if (!checkExistence(settings, output)) {
												if (evaluationContainer == null) {
													evaluationContainer = init(container, settings, humanReference);
												}
												evaluate(100, evaluationContainer, false, null, ng, distance,
														overAllCalculation, output);
												overAllCalculation++;
											}

											settings.setClusterer(kMeans.getName());
											if (!checkExistence(settings, output)) {
												if (evaluationContainer == null) {
													evaluationContainer = init(container, settings, humanReference);
												}
												evaluate(100, evaluationContainer, false, null, kMeans, distance,
														overAllCalculation, output);
												overAllCalculation++;
											}

											settings.setClusterer(fuzzy.getName());
											if (!checkExistence(settings, output)) {
												if (evaluationContainer == null) {
													evaluationContainer = init(container, settings, humanReference);
												}
												evaluate(100, evaluationContainer, false, null, fuzzy, distance,
														overAllCalculation, output);
												overAllCalculation++;
											}

										}
									}

								}
							}
						}
					}
				}
			}
		}

	}

	private static EvaluationContainer init(final ModelContainer container, final EvaluationConfig settings,
			final Result humanReference) throws Exception {
		List<RequirementVector> vectors = initVectors(container, settings);
		Instances data = initInstances(vectors);
		DistanceHandler distanceHandler;
		if (settings.isRuppInterpretation()) {
			distanceHandler = new DistanceCalculatorRupp(vectors);

		} else {
			distanceHandler = new DistanceCalculatorNormal();
		}
		EvaluationContainer evaluationContainer = new EvaluationContainer(settings, vectors, data, distanceHandler,
				humanReference, container);
		return evaluationContainer;
	}

	public static void research(final Result humanReference, final ModelContainer container,
			final EvaluationConfig settings, final String output) throws Exception {

		// my kmeans
		KMeans kMeans = new KMeans(23, 20);
		NeuralGas ng = new NeuralGas(23);
		ClusterART1 art = new ClusterART1(0.3, 1.5, 23);
		FuzzyCMeans fuzzy = new FuzzyCMeans(23, 20, 2);
		// needed for kmeans clustering
		List<DistanceFunction> distances = new ArrayList<DistanceFunction>();
		distances.add(new EuclideanDistance());
		distances.add(new CosineDistance());
		distances.add(new CanberraDistance());
		distances.add(new ChebyshevDistance());
		distances.add(new ManhattanDistance());
		distances.add(new HammingDistance());
		List<weka.core.NormalizableDistance> wekaDistances = new ArrayList<>();
		wekaDistances.add(new weka.core.EuclideanDistance());
		wekaDistances.add(new weka.core.ManhattanDistance());
		wekaDistances.add(new weka.core.ChebyshevDistance());
		List<List<Field>> fieldCombs = new ArrayList<>();
		List<Field> combOne = new ArrayList<>();
		combOne.add(Field.OBJECTUNDERGÄNZUNG_4);
		fieldCombs.add(combOne);
		List<Field> combTwo = new ArrayList<>();
		combTwo.add(Field.SUBJEKT_1);
		combTwo.add(Field.OBJECTUNDERGÄNZUNG_4);
		combTwo.add(Field.PROZESSWORT_5);
		combTwo.add(Field.BEDINGUNG_7);
		fieldCombs.add(combTwo);
		List<Field> combThree = new ArrayList<>();
		combThree.add(Field.SUBJEKT_1);
		combThree.add(Field.VERBINDLICHKEIT_2);
		combThree.add(Field.AKTIVITÄT_3);
		combThree.add(Field.OBJECTUNDERGÄNZUNG_4);
		combThree.add(Field.PROZESSWORT_5);
		combThree.add(Field.QUALITÄT_6);
		combThree.add(Field.BEDINGUNG_7);
		fieldCombs.add(combThree);
		List<GermaNetFunction> germaNet = new ArrayList<>();
		germaNet.add(null);
		// germaNet.add(new GermaNetUniqueMapping());
		germaNet.add(new GermaNetShotgun());
		germaNet.add(new GermaNetBestAncestor());
		// germaNet.add(new GermaNetBestParentPath());
		int overAllCalculation = 0;
		for (List<Field> fields : fieldCombs) {
			settings.setFields(fields);
			settings.setFields(combThree);
			for (int sourceIndex = 0; sourceIndex < 2; sourceIndex++) {
				settings.setSource(!settings.isSource());
				for (int lemmatizeIndex = 0; lemmatizeIndex < 2; lemmatizeIndex++) {
					settings.setLemmatized(!settings.isLemmatized());
					for (GermaNetFunction function : germaNet) {
						settings.setGermaNetFunction(function);
						// only use synonyms with lemmatized
						if (function != null && !settings.isLemmatized()) {
							settings.setGermaNetFunction(null);
						}
						for (int synonymIndex = 0; synonymIndex < 2; synonymIndex++) {
							settings.setSynonyms(!settings.isSynonyms());
							// only use synonyms with lemmatized
							if (settings.isSynonyms() && !settings.isLemmatized()) {
								settings.setSynonyms(false);
							}
							for (int stopWordsIndex = 0; stopWordsIndex < 2; stopWordsIndex++) {
								settings.setStopWords(!settings.isStopWords());
								for (int interpretedIndex = 0; interpretedIndex < 2; interpretedIndex++) {
									settings.setRuppInterpretation(!settings.isRuppInterpretation());
									settings.setRuppInterpretation(true);
									for (int tfidfIndex = 0; tfidfIndex < 2; tfidfIndex++) {
										settings.setTfidf(!settings.isTfidf());
										settings.setTfidf(true);
										EvaluationContainer evaluationContainer = null;
										List<weka.clusterers.Clusterer> wekaClusters = new ArrayList<>();
										EM em = new EM();
										em.setNumClusters(23);
										HierarchicalClusterer hc = new HierarchicalClusterer();
										wekaClusters.add(em);
										wekaClusters.add(hc);
										// for(weka.clusterers.Clusterer
										// clusterer : wekaClusters){
										// if(clusterIndex == 0){
										// try{
										// settings.setClusterer("EM");
										// settings.setDistance("Not
										// needed");
										// if(!checkExistence(settings)){
										// if(vectors == null){
										// vectors =
										// initVectors(container,
										// settings);
										// data =
										// initInstances(vectors);
										// }
										// evaluate(1, vectors,
										// container,
										// data, humanReference,
										// settings, true, clusterer,
										// null,
										// null,
										// overAllCalculation);
										// overAllCalculation++;
										// }
										// }
										// catch(Exception e){
										//
										// }
										// }
										// else{
										// int distanceIndex = 0;
										// for(weka.core.NormalizableDistance
										// distance : wekaDistances){
										// HierarchicalClusterer
										// hcNewInstance = new
										// HierarchicalClusterer();
										// hcNewInstance.setNumClusters(23);
										// // get old
										// SelectedTag tag =
										// hcNewInstance.getLinkType();
										// // select to mean
										// // Link
										// SelectedTag newTag = new
										// SelectedTag(3,
										// tag.getTags());
										// // set to mean
										// hcNewInstance.setLinkType(newTag);
										// // for output of
										// // table
										// hcNewInstance.setDistanceFunction(distance);
										// String distanceName;
										// if(distanceIndex == 0){
										// distanceName =
										// "EuclideanDistance";
										// }
										// else if(distanceIndex == 1){
										// distanceName =
										// "ManhattanDistance";
										// }
										// else{
										// distanceName =
										// "ChebychevDistance";
										// }
										// settings.setClusterer("HC");
										// settings.setDistance(distanceName);
										// if(!checkExistence(settings)){
										// if(vectors == null){
										// vectors =
										// initVectors(container,
										// settings);
										// data =
										// initInstances(vectors);
										// }
										// try{
										// evaluate(1, vectors,
										// container, data,
										// humanReference, settings,
										// true,
										// hcNewInstance, null, null,
										// overAllCalculation);
										// overAllCalculation++;
										// }
										// catch(Exception e){
										//
										// }
										// }
										// distanceIndex++;
										// }
										// }
										// clusterIndex++;
										// }
										// try{
										// settings.setClusterer("ClusterART");
										// settings.setDistance("Not
										// needed");
										// if(!checkExistence(settings)){
										// if(vectors == null){
										// vectors =
										// initVectors(container,
										// settings);
										// data =
										// initInstances(vectors);
										// }
										// evaluate(100, vectors,
										// container, data,
										// humanReference, settings,
										// false, null,
										// art, new EuclideanDistance(),
										// overAllCalculation);
										// }
										// }
										// catch(Exception e){
										// LOG.info(e.toString());
										// }
										for (DistanceFunction distance : distances) {
											settings.setDistance(distance.getName());
											// try{
											// settings.setClusterer(ng.getName());
											// if(!checkExistence(settings)){
											// if(vectors == null){
											// vectors =
											// initVectors(container,
											// settings);
											// data =
											// initInstances(vectors);
											// }
											// evaluate(100, vectors,
											// container, data,
											// humanReference, settings,
											// false,
											// null,
											// ng, distance,
											// overAllCalculation);
											// overAllCalculation++;
											// }
											// }
											// catch(Exception e){
											// LOG.info(e.toString());
											// }

											try {
												kMeans = new KMeans(23, 20);
												settings.setClusterer(kMeans.getName());
												settings.setFields(Util.getRuppFields());
												settings.setDistance(new CosineDistance().getName());
												settings.setLemmatized(true);
												settings.setStopWords(true);
												settings.setRuppInterpretation(true);
												settings.setTfidf(true);
												if (!checkExistence(settings, output)) {
													if (evaluationContainer == null) {
														evaluationContainer = init(container, settings, humanReference);
													}
													evaluate(1, evaluationContainer, false, null, kMeans,
															new CosineDistance(), overAllCalculation, output);
													overAllCalculation++;
												}
											} catch (Exception e) {
												LOG.info(e.toString());
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

	}

	// executor of bigEvaluation. Does one evaluation with certain settings
	public static void evaluate(final int iterations, final EvaluationContainer evaluationContainer,
			final boolean wekaCluster, final weka.clusterers.Clusterer wekaClusterer,
			final ClusterFunction ownClusterer, final DistanceFunction distance, final int overAllCalculation,
			final String output) throws Exception {

		// First things first! what time is it? Do we need to sleep?
		if (Main.HEARTBEATFILE != null) {
			if (!Pacemaker.inEvaluationRange(LocalTime.now(), Calendar.getInstance())
					&& System.getenv("MINION_PLZ_SLEEP") != null && !System.getenv("MINION_PLZ_SLEEP").equals("true")) {
				Pacemaker.changeStatus("SLEEPING");
				System.exit(0);
			}
			if (Pacemaker.vacations()) {
				Pacemaker.changeStatus("VACATION");
				System.exit(0);
			}
		}

		EvaluationConfig settings = evaluationContainer.getSettings();
		List<RequirementVector> vectors = evaluationContainer.getVectors();
		ModelContainer container = evaluationContainer.getContainer();
		Result humanReference = evaluationContainer.getHumanReference();
		DistanceHandler distanceHandler = evaluationContainer.getDistanceHandler();
		distanceHandler.setDistanceFunction(distance);
		// if something happens here, we have to kill the file
		boolean next = false;
		if (!checkExistence(settings, output)) {
			try (FileOutputStream fos = new FileOutputStream(output + "/" + settings.toString() + ".ser");
					ObjectOutputStream oos = new ObjectOutputStream(fos)) {
				oos.writeObject(true);
				fos.close();
			} catch (Exception e) {
				LOG.info("Someone was faster, next iteration");
				next = true;
			}
		}
		// next means, that somebody wrote the file while we wanted to, so we
		// skip this file and carry on
		if (!next) {
			Main.DELETIONNEEDED = true;
			if (Main.HEARTBEATFILE != null) {
				Pacemaker.updateHeartbeat(settings);
			}
			double[] f1 = new double[iterations];
			double[] precision = new double[iterations];
			double[] recall = new double[iterations];
			double[] f1Weighted = new double[iterations];
			double[] precisionWeighted = new double[iterations];
			double[] recallWeighted = new double[iterations];
			double[] naive = new double[iterations];
			double[] rand = new double[iterations];
			double[] purity = new double[iterations];
			double[] jaccard = new double[iterations];
			double[] jaccardWeighted = new double[iterations];
			double[] runtime = new double[iterations];
			double[] cohesion = new double[iterations];
			double[] seperation = new double[iterations];
			double[] silhouette = new double[iterations];
			List<Result> results = new ArrayList<>();
			double accordance = 0;
			List<Integer> streamIterations = new ArrayList<>();
			for (int i = 0; i < iterations; i++) {
				streamIterations.add(i);
			}
			boolean verification = output.contains("Verification");
			Map<Integer, Result> parallelizationResults = new ConcurrentHashMap<>();

			streamIterations.parallelStream().forEach(x -> {
				LOG.info("Start iteration {} of clusterer {}. This is evaluation {} with distance {}", x,
						settings.getClusterer(), overAllCalculation, settings.getDistance());
				LOG.info(
						"Parameters are set to: rarityIndex {}, tfidf {}, stopwords {}, upperCase {}, interpretation {}, lemmatized {}, number of Fields {}",
						settings.getParameter(), settings.isTfidf(), settings.isStopWords(), settings.isUpperCase(),
						settings.isRuppInterpretation(), settings.isLemmatized(), settings.getFields().size());
				ClusterExecutor g = new ClusterExecutor();
				if (wekaCluster) {
					try {
						Result r = g.group(vectors, wekaClusterer, container, evaluationContainer.getData(),
								humanReference);
						parallelizationResults.put(x, r);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				else {
					try {
						Result r = g.group(Collections.unmodifiableList(vectors), distanceHandler, ownClusterer,
								container, humanReference, x, verification);
						parallelizationResults.put(x, r);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			// contains the metric value at 0 and the internal measures on 1-3
			double[] f1Max = new double[4], naiveMax = new double[4], recallMax = new double[4];
			for (int i = 0; i < iterations; i++) {
				Result r = parallelizationResults.get(i);

				results.add(r);
				f1[i] = r.getF1();
				precision[i] = r.getPrecision();
				recall[i] = r.getRecall();
				f1Weighted[i] = r.getF1Weighted();
				precisionWeighted[i] = r.getPrecisionWeighted();
				recallWeighted[i] = r.getRecallWeighted();
				naive[i] = r.getNaiveAccuracy();
				rand[i] = r.getRand();
				purity[i] = r.getPurity();
				jaccard[i] = r.getJaccard();
				jaccardWeighted[i] = r.getJaccardWeighted();
				if (f1Max[0] < r.getF1()) {
					f1Max[0] = r.getF1();
					f1Max[1] = r.getCohesion();
					f1Max[2] = r.getSeperation();
					f1Max[3] = r.getSilhouette();
				}
				if (naiveMax[0] < r.getNaiveAccuracy()) {
					naiveMax[0] = r.getNaiveAccuracy();
					naiveMax[1] = r.getCohesion();
					naiveMax[2] = r.getSeperation();
					naiveMax[3] = r.getSilhouette();
				}
				if (recallMax[0] < r.getRecall()) {
					recallMax[0] = r.getRecall();
					recallMax[1] = r.getCohesion();
					recallMax[2] = r.getSeperation();
					recallMax[3] = r.getSilhouette();
				}
				runtime[i] = (double) r.getRuntime();
				cohesion[i] = r.getCohesion();
				seperation[i] = r.getSeperation();
				silhouette[i] = r.getSilhouette();
				if (r.getAccordance()) {
					accordance++;
				}
			}
			MetaResult meta = new MetaResult();
			meta.setParameter(settings.getParameter());
			meta.setFields(settings.getFields());
			meta.setTfidf(settings.isTfidf());
			meta.setStopWords(settings.isStopWords());
			meta.setRuppInterpretation(settings.isRuppInterpretation());
			meta.setUpperCase(settings.isUpperCase());
			meta.setLemmatized(settings.isLemmatized());
			meta.setOnthology(settings.isOnthology());
			meta.setSource(settings.isSource());
			meta.setSynonyms(settings.isSynonyms());
			meta.setF1(f1);
			meta.setNaive(naive);
			meta.setPurity(purity);
			meta.setRand(rand);
			meta.setJaccard(jaccard);
			meta.setRuntime(runtime);
			meta.setPrecision(precision);
			meta.setRecall(recall);
			meta.setF1Weighted(f1Weighted);
			meta.setPrecisionWeighted(precisionWeighted);
			meta.setRecallWeighted(recallWeighted);
			meta.setJaccardWeighted(jaccardWeighted);
			meta.setF1Max(f1Max);
			meta.setNaiveMax(naiveMax);
			// meta.setPrecisionMax(precisionMax);
			meta.setRecallMax(recallMax);
			// meta.setJaccardMax(jaccardMax);
			meta.setRand(rand);
			// meta.setPurityMax(purityMax);
			meta.setCohesion(cohesion);
			meta.setSeperation(seperation);
			meta.setSilhouette(silhouette);
			meta.setClusterer(settings.getClusterer());
			meta.setDistance(settings.getDistance());
			meta.setAccordance(accordance / iterations);
			meta.setGermaNetFunction(settings.getGermaNetFunction());
			meta.setDistanceHandler(settings.getDistanceHandler());

			try (FileOutputStream fos = new FileOutputStream(output + " small/" + settings.toString() + ".ser");
					ObjectOutputStream oos = new ObjectOutputStream(fos)) {
				oos.writeObject(meta);
			}

			meta.setResults(results);

			try (FileOutputStream fos = new FileOutputStream(output + "/" + settings.toString() + ".ser");
					ObjectOutputStream oos = new ObjectOutputStream(fos)) {
				oos.writeObject(meta);
			}
			// everything went smooth, file was written, dont delete it
			Main.DELETIONNEEDED = false;
		}

	}

	public static boolean checkExistence(final EvaluationConfig settings, final String output)
			throws FileNotFoundException, IOException {
		// if (Main.debugging) {
		// return false;
		// }
		boolean check = new File(output + "/" + settings.toString() + ".ser").exists();
		if (check) {
			LOG.info("skipped iteration, already exists");
			return true;
		} else {
			return false;
		}
	}

	public static List<RequirementVector> initVectors(final ModelContainer container, final EvaluationConfig settings)
			throws Exception {
		ClusterDataGenerator generator = new ClusterDataGenerator();
		List<RequirementVector> vectors = generator.generateVectors(container, settings);
		// lock all vectors and their
		// content
		UtilityFunctions.lock(vectors);
		return vectors;
	}

	public static Instances initInstances(final List<RequirementVector> vectors) throws IOException {
		ClusterDataGenerator generator = new ClusterDataGenerator();
		Instances data = ClusterDataGenerator.getInstances(generator, vectors);

		return data;
	}
}
