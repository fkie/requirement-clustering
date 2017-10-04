package de.fraunhofer.fkie.evaluation.clusterfunction;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

import org.junit.Test;

import de.fraunhofer.fkie.aidpfm.model.Node;
import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.fraunhofer.fkie.aidpfm.utilities.ModelContainer;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB.Query;
import de.fraunhofer.fkie.evaluation.ClusterDataGenerator;
import de.fraunhofer.fkie.evaluation.ClusterExecutor;
import de.fraunhofer.fkie.evaluation.distancefunction.CanberraDistance;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceFunction;
import de.fraunhofer.fkie.evaluation.model.EvaluationConfig;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;
import de.fraunhofer.fkie.evaluation.model.Result;

public class EvaluationTest {
//	@Test
//	public void testEvaluation() throws Exception {
//		EvaluationConfig settings = new EvaluationConfig();
//		settings.allFalse();
//		Query query = Query.ALL;
//		List<Requirement> requirements = R7DB.getRequirements(query);
//		Node<String> testData;
//		try(FileInputStream fis = new FileInputStream(Config.TESTDATA);
//				ObjectInputStream ois = new ObjectInputStream(fis)){
//			testData = (Node<String>) ois.readObject();
//		}
//		testData.getChildren().remove(2); // get "zurückgestellt" out
//		ClusterDataGenerator generator = new ClusterDataGenerator();
//		List<Requirement> testRequirements = generator.toRequirementList(testData, requirements);
//		ModelContainer container = new ModelContainer(testRequirements);
//		Result humanReference = generator.inputIntoResult0504(testData, requirements);
//		KMeans kmeans = new KMeans(23, 20);
//		DistanceFunction distance = new CanberraDistance();
//		settings.setClusterer(kmeans.getName());
//		settings.setDistance(distance.getName());
//		List<RequirementVector> vectors = generator.generateVecotrs(container, settings);
//		ClusterExecutor executor = new ClusterExecutor();
//		executor.group(vectors, distance, kmeans, container, humanReference, 0);
//		Evaluation.evaluate(5, vectors, container, null, humanReference, settings, false, null, fuzzy, distance, 0);
//	}
}
