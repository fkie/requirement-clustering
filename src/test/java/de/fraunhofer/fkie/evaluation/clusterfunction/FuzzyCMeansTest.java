package de.fraunhofer.fkie.evaluation.clusterfunction;

import java.io.FileInputStream;
import java.io.IOException;
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
import de.fraunhofer.fkie.evaluation.Evaluation;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceFunction;
import de.fraunhofer.fkie.evaluation.distancefunction.EuclideanDistance;
import de.fraunhofer.fkie.evaluation.model.EvaluationConfig;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;
import de.fraunhofer.fkie.evaluation.model.Result;

public class FuzzyCMeansTest {
//	@Test
//	public void testFuzzy() throws Exception {
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
//		FuzzyCMeans fuzzy = new FuzzyCMeans(23, 20, 2);
//		DistanceFunction distance = new EuclideanDistance();
//		settings.setClusterer(fuzzy.getName());
//		settings.setDistance(distance.getName());
//		List<RequirementVector> vectors = generator.generateVecotrs(container, settings);
//		ClusterExecutor executor = new ClusterExecutor();
////		executor.group(vectors, distance, fuzzy, container, humanReference, 0);
//		Evaluation.evaluate(5, vectors, container, null, humanReference, settings, false, null, fuzzy, distance, 0);
//	}
}
