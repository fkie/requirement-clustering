package de.fraunhofer.fkie.evaluation.model;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import de.fraunhofer.fkie.aidpfm.model.Node;
import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.Datasets;
import de.fraunhofer.fkie.aidpfm.utilities.ModelContainer;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB.Query;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.ClusterDataGenerator;
import de.fraunhofer.fkie.evaluation.Main;

public class RequirementVectorIT {
	List<Requirement> requirements;
	ModelContainer container;
	EvaluationConfig settings;
	Result humanReference;
	ClusterDataGenerator generator;

	@Before
	public void setupDependencies() throws Exception {
		Main.DATASET = Datasets.ALPHA;
		this.requirements = R7DB.getRequirements(Query.ALL, Datasets.ALPHA.db);
		Node<String> testData;
		try (FileInputStream fis = new FileInputStream(Datasets.ALPHA.testData);
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			testData = (Node<String>) ois.readObject();
		}
		this.generator = new ClusterDataGenerator();
		// side effects on testdata!
		this.container = Main.getRequirementsFromDataset(this.requirements, testData, this.generator, false);
		this.settings = new EvaluationConfig();
		this.settings.allFalse();
		this.settings.setFields(Util.getRuppFields());
	}

	@Test
	public void testVectorWithReq() throws Exception {
		List<RequirementVector> vectors = this.generator.generateVectors(this.container, this.settings);
		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			int index = random.nextInt(vectors.size());
			RequirementVector vector = vectors.get(index);
			Requirement requirement = Util.getRequirement(vector.getID(), this.requirements);
			String[] words;
			String text = "";
			for (Field field : Util.getRuppFields()) {
				// not good to test
				String fieldContent = requirement.get(field);
				String cleanedContent = Util.clean(fieldContent).trim();
				text += " " + cleanedContent;
			}
			words = text.trim().split(" ");
			for (String word : words) {
				int countTotal = Util.determineWordCount(word, text.split(" "));
				// not good to test
				if (word.equals("")) {
					continue;
				}
				assertTrue(word + " has error in total " + countTotal + ", " + (int) vector.getContent(word),
						countTotal == vector.getContent(word));

			}
		}
	}

	@Test
	public void testVectorWithReqFields() throws Exception {
		this.settings.setRuppInterpretation(true);
		List<RequirementVector> vectors = this.generator.generateVectors(this.container, this.settings);
		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			int index = random.nextInt(vectors.size());
			RequirementVector vector = vectors.get(index);
			Requirement requirement = Util.getRequirement(vector.getID(), this.requirements);
			for (Field field : Util.getRuppFields()) {
				// not good to test
				if (field == Field.SUBJEKT_1 || field == Field.AKTIVITÄT_3) {
					continue;
				}
				String fieldContent = requirement.get(field);
				String cleanedContent = Util.clean(fieldContent).trim();
				String text = cleanedContent;
				if (field == Field.SUBJEKT_1 || field == Field.AKTIVITÄT_3) {
					text = "" + cleanedContent.hashCode();
				}
				String[] words = text.split(" ");
				for (String word : words) {
					int countField = Util.determineWordCount(word, words);
					// not good to test
					if (word.equals("")) {
						continue;
					}
					word = word.toLowerCase() + "_" + field;
					assertTrue(word + " has error " + countField + ", " + (int) vector.getFieldContent(field, word),
							countField == vector.getFieldContent(field, word));
					// assertTrue(word + " has error in total " + countTotal +
					// ", " + (int) vector.getContent(word),
					// countTotal == vector.getContent(word));

				}

			}
		}

	}

	// @Test
	// public void testCoLemmatizer() throws Exception {
	// String test = "Hallo, das ist ein neuer Test mit neuen Daten. Mal sehen
	// ob Flugzeug und Flugzeuge als ein Wort gepackt werden";
	// TreeSet<String> alphabet = new TreeSet<>();
	// NLProcessor processor = new NLProcessor();
	// List<String> content = processor.lemmatize(test);
	// List<String> doc = new ArrayList<>();
	// for (String word : content) {
	// alphabet.add(Util.cleanWord(word));
	// doc.add(Util.cleanWord(word));
	// }
	//
	// RequirementVector requirementVector = new RequirementVector(alphabet,
	// doc, "sada", "sada");
	//
	// assertTrue("das" + " Should not be contained",
	// requirementVector.getContentKeys().contains("das") == false);
	// assertTrue("," + " Should not be contained",
	// requirementVector.getContentKeys().contains(",") == false);
	// assertTrue("." + " Should not be contained",
	// requirementVector.getContentKeys().contains(".") == false);
	// assertTrue("sein" + " Should be contained",
	// requirementVector.getContent("sein") == 1);
	// assertTrue("flugzeug" + " Should be contained twice",
	// requirementVector.getContent("flugzeug") == 2);
	// }

}
