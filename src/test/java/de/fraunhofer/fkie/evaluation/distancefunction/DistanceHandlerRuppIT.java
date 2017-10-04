package de.fraunhofer.fkie.evaluation.distancefunction;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.utilities.Datasets;
import de.fraunhofer.fkie.aidpfm.utilities.ModelContainer;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB.Query;
import de.fraunhofer.fkie.evaluation.ClusterDataGenerator;
import de.fraunhofer.fkie.evaluation.Main;
import de.fraunhofer.fkie.evaluation.model.EvaluationConfig;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;
import weka.core.Debug.Random;

public class DistanceHandlerRuppIT {
	ModelContainer container;
	EvaluationConfig settings;
	List<RequirementVector> vectors;

	@Before
	public void setupDependencies() throws Exception {
		Main.DATASET = Datasets.ALPHA;
		List<Requirement> requirements = R7DB.getRequirements(Query.ALL, Datasets.ALPHA.db);
		this.container = new ModelContainer(requirements);
		this.settings = new EvaluationConfig();
		this.settings.allFalse();
		this.settings.setRuppInterpretation(true);
		ClusterDataGenerator generator = new ClusterDataGenerator();
		this.vectors = generator.generateVectors(this.container, this.settings);
	}

	@Test
	public void testDistancesWithRupp() {
		DistanceCalculatorRupp calculator = new DistanceCalculatorRupp(this.vectors);
		DistanceFunction distance = new EuclideanDistance();
		calculator.setDistanceFunction(distance);
		RequirementVector v1 = this.vectors.get(0);
		Set<Double> possibleDistancesEuclidean = new HashSet<>();
		for (RequirementVector vector : this.vectors) {
			possibleDistancesEuclidean
					.add(distance.distanceZ(v1.getVectorRepresentation(), vector.getVectorRepresentation()));
		}
		Set<Double> possibleDistancesNormalized = new HashSet<>();
		for (RequirementVector vector : this.vectors) {
			possibleDistancesNormalized.add(calculator.distanceZ(v1, vector));
		}
		System.out.println("Normal amount of different distances: " + possibleDistancesEuclidean.size());
		System.out.println("Amount of different distances with Normalization: " + possibleDistancesNormalized.size());
		// assertTrue(possibleDistancesEuclidean.size() ==
		// possibleDistancesNormalized.size());
	}

	@Test
	public void testEuclidean() {
		DistanceCalculatorRupp calculator = new DistanceCalculatorRupp(this.vectors);
		calculator.setDistanceFunction(new EuclideanDistance());
		Random random = new Random();
		double maxDist = 0;
		for (int i = 0; i < 1000; i++) {
			int x = random.nextInt(this.vectors.size());
			int y = random.nextInt(this.vectors.size());
			RequirementVector v1 = this.vectors.get(x);
			RequirementVector v2 = this.vectors.get(y);
			double dist = calculator.distanceZ(v1, v2);
			if (dist > maxDist) {
				maxDist = dist;
			}
			assertTrue(dist <= 1 && dist >= 0);
		}
	}

	@Test
	public void testNormalizer() {
		DistanceCalculatorRupp calculator = new DistanceCalculatorRupp(this.vectors);
		calculator.setDistanceFunction(new EuclideanDistance());
		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			int x = random.nextInt(this.vectors.size());
			RequirementVector v1 = this.vectors.get(x);
			// Map<String, Double> map =
			// calculator.normalize(v1.getContentMap());
			// for(double value : map.values()){
			// assertTrue(value <= 1 && value >= 0);
			// }
		}
	}
}
