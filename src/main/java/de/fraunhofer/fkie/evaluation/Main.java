package de.fraunhofer.fkie.evaluation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.fkie.aidpfm.model.Node;
import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.fraunhofer.fkie.aidpfm.utilities.Datasets;
import de.fraunhofer.fkie.aidpfm.utilities.ModelContainer;
import de.fraunhofer.fkie.aidpfm.utilities.NLProcessor;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB.Query;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.model.EvaluationConfig;
import de.fraunhofer.fkie.evaluation.model.Result;
import de.fraunhofer.fkie.itf.eaWorkbench.eaModelingCore.exception.ModelException;

public class Main {

	public static Datasets DATASET;
	public static Logger LOG = LoggerFactory.getLogger(Main.class);
	public static String HEARTBEATFILE;
	public static String HEARTBEATCONTENT;
	public static boolean DELETIONNEEDED = false;
	public static boolean debugging = true;
	public static final EvaluationConfig currentSettings = new EvaluationConfig();

	public static void main(final String[] args) throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					if (Pacemaker.getStatus(HEARTBEATFILE).equals("EVALUATION")) {
						Pacemaker.changeStatus("TERMINATED");
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (DELETIONNEEDED) {
					HEARTBEATCONTENT += "iteration not finished \n";
					Util.writeUtf8File(HEARTBEATFILE, HEARTBEATCONTENT);
					LOG.info("Deleting " + currentSettings.toString());
					try {
						Files.delete(Paths.get(DATASET + "/" + currentSettings.toString() + ".ser"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		// throws exception if it can not be accesed => we do not need to check
		// variable
		LOG.info("Starting evaluation with version: {}", Util.getVersion());
		LOG.info("Initialize Nlytics");
		NLProcessor processor = new NLProcessor();
		processor.lemmatize("Hallo, bitte lemmatisiere diesen Text");
		try {
			Util.terraAccess();
			Util.testConfiguration();
		} catch (Throwable e) {
			Pacemaker.createHeartbeat("[DYING]");
			HEARTBEATCONTENT += "\n\n\n ERROR THAT CAUSED DEATH: \n";
			HEARTBEATCONTENT += Util.stackTraceString(e);
			// changes Heartbeatfile!!!
			Pacemaker.changeStatus("DEAD");
			Util.writeUtf8File(HEARTBEATFILE, HEARTBEATCONTENT);
			throw e;
		}
		if (Pacemaker.vacations()) {
			Pacemaker.createHeartbeat("[VACATION]");
		} else if (Pacemaker.checkHeartbeat()) {
			// if in range then doesnt matter if he is a hibernating minion,
			// otherwise look at environment variable
			Calendar c = Calendar.getInstance();
			if (Pacemaker.inEvaluationRange(LocalTime.now(), c) || (System.getenv("MINION_PLZ_SLEEP") == null
					|| !System.getenv("MINION_PLZ_SLEEP").equals("true"))) {
				Pacemaker.createHeartbeat("[EVALUATION]");
				try {
					// this loop is for sequential evaluation through the data
					// sets
					for (Datasets dataset : Datasets.values()) {
						DATASET = Datasets.ZETA;
						// true if yo uwant to evaluate verification data
						startEvaluation(true);
					}

					// DATASET = Datasets.ALPHA;
					// startEvaluation(true);

				} catch (Throwable e) {
					HEARTBEATCONTENT += "\n\n\n ERROR THAT CAUSED DEATH: \n";
					HEARTBEATCONTENT += Util.stackTraceString(e);
					// changes Heartbeatfile!!!
					Pacemaker.changeStatus("DEAD");
					Util.writeUtf8File(HEARTBEATFILE, HEARTBEATCONTENT);
					throw e;
				}
			} else {
				Pacemaker.createHeartbeat("[SLEEPING]");
			}

		}

	}

	@SuppressWarnings("unchecked")
	public static void startEvaluation(final boolean verification)
			throws IOException, ClassNotFoundException, FileNotFoundException, Exception {
		// Requirements auslesen
		Query query = Query.ALL;
		LOG.info("Working on Dataset {}", DATASET.name);
		HEARTBEATCONTENT += DATASET.name + "\n";

		ModelContainer container;
		Result humanReference;
		String output;
		if (DATASET.requirements) {
			if (!verification) {
				List<Requirement> requirements = R7DB.getRequirements(query, DATASET.db);
				Node<String> testData;
				try (FileInputStream fis = new FileInputStream(DATASET.testData);
						ObjectInputStream ois = new ObjectInputStream(fis)) {
					testData = (Node<String>) ois.readObject();
				}
				ClusterDataGenerator generator = new ClusterDataGenerator();
				// side effects on testdata!
				container = getRequirementsFromDataset(requirements, testData, generator, verification);
				humanReference = generateHumanReference(testData, container, generator);
				output = DATASET.outputTestData;
			}

			else {
				List<Requirement> requirements = R7DB.getRequirements(query, DATASET.db);
				Node<String> testData;
				try (FileInputStream fis = new FileInputStream(DATASET.allData);
						ObjectInputStream ois = new ObjectInputStream(fis)) {
					testData = (Node<String>) ois.readObject();
				}
				ClusterDataGenerator generator = new ClusterDataGenerator();
				// side effects on testdata!
				container = getRequirementsFromDataset(requirements, testData, generator, verification);
				humanReference = generateHumanReference(testData, container, generator);
				output = DATASET.outputVerificationData;
			}
		}

		else {
			ClusterDataGenerator generator = new ClusterDataGenerator();
			List<Requirement> requirements = generator.shortTextLoader();
			// side effects on testdata!
			container = new ModelContainer(requirements);
			humanReference = generator.generateHumanReferenceShortTexts(container);
			output = DATASET.outputTestData;
		}

		if (debugging) {
			output = Config.DEBUGFOLDER;
		}
		Evaluation.bigEvaluation(humanReference, container, currentSettings, output);
	}

	public static Result generateHumanReference(final Node<String> testData, final ModelContainer container,
			final ClusterDataGenerator generator) {
		Result result;
		if (DATASET == Datasets.BETA) {
			result = generator.humanReferenceBeta(testData, container.getRequirementsUnmodifiable());
		} else if (DATASET == Datasets.ZETA) {
			result = generator.humanReferenceBeta(testData.getChildren().get(0),
					container.getRequirementsUnmodifiable());
		} else if (DATASET == Datasets.DELTA) {
			result = generator.humanReferenceDelta(testData, container.getRequirementsUnmodifiable());
		} else {
			result = generator.humanReferenceAlpha(testData, container.getRequirementsUnmodifiable());
		}
		return result;
	}

	public static ModelContainer getRequirementsFromDataset(final List<Requirement> requirements,
			final Node<String> testData, final ClusterDataGenerator generator, final boolean verification)
			throws ModelException {
		ModelContainer container;
		if (DATASET == Datasets.BETA) {
			List<Requirement> testRequirements = generator.toRequirementList(testData, requirements);
			container = new ModelContainer(testRequirements);
			// humanReference = generator.humanReferenceBeta(testData,
			// requirements);
		} else if (DATASET == Datasets.ZETA) {
			testData.getChildren().remove(0);
			List<Requirement> testRequirements = generator.toRequirementList(testData.getChildren().get(0),
					requirements);
			container = new ModelContainer(testRequirements);
		} else if (DATASET == Datasets.DELTA) {
			testData.getChildren().remove(2);
			testData.getChildren().remove(2);
			testData.getChildren().remove(2);
			testData.getChildren().remove(2);
			testData.getChildren().remove(2);
			testData.getChildren().get(0).getChildren().remove(0);
			List<Requirement> testRequirements = generator.toRequirementList(testData, requirements);
			container = new ModelContainer(testRequirements);
			// humanReference = generator.humanReferenceDelta(newRoot,
			// requirements);
		} else {
			// zurückgestellt out
			testData.getChildren().remove(2);
			List<Requirement> testRequirements = generator.toRequirementList(testData, requirements);
			container = new ModelContainer(testRequirements);
			// humanReference = generator.humanReferenceAlpha(testData,
			// requirements);
		}
		return container;
	}
}
