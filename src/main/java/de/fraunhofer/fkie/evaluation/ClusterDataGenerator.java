package de.fraunhofer.fkie.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.fkie.aidpfm.model.Node;
import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.fraunhofer.fkie.aidpfm.utilities.ModelContainer;
import de.fraunhofer.fkie.aidpfm.utilities.NLProcessor;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.model.Cluster;
import de.fraunhofer.fkie.evaluation.model.EvaluationConfig;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;
import de.fraunhofer.fkie.evaluation.model.Result;
import weka.core.Instances;

public class ClusterDataGenerator {
	public Logger LOG = LoggerFactory.getLogger(ClusterDataGenerator.class);

	public void vectorsIntoArff(final List<RequirementVector> list, final String name) {
		String arff = "@RELATION RequirementData\n\n";

		// get attributes
		RequirementVector v = list.get(0);
		for (String key : v.getContentKeys()) {
			arff += "@ATTRIBUTE \"" + key + "\" NUMERIC\n";
		}
		arff += "\n @DATA\n";

		// add vectors
		for (RequirementVector vector : list) {
			String line = "";
			for (String key : vector.getContentKeys()) {
				line += vector.getContent(key) + ",";
			}
			line = line.substring(0, line.length() - 1);
			arff += line + "\n";
		}

		Util.writeUtf8File(Config.EVALUATIONFRAMEWORK + "/temp/" + name + ".arff", arff);
	}

	/**
	 * generates the RequirementVectors from start to end by handing in a
	 * settings object and a ModelContainer
	 *
	 * @param container
	 * @param settings
	 * @return
	 * @throws Exception
	 */
	public List<RequirementVector> generateVectors(final ModelContainer container, final EvaluationConfig settings)
			throws Exception {
		List<RequirementVector> vectors;
		RequirementVector.setWordOrder(null);
		RequirementVector.setWordOrderField(null);
		vectors = this.makeFieldVectors(container.getRequirementsUnmodifiable(), settings);

		List<RequirementVector> tfidfVectors;
		if (settings.isTfidf()) {
			tfidfVectors = this.weightWithTfIdf(vectors, settings);
		} else {
			tfidfVectors = vectors;
		}
		return tfidfVectors;
	}

	/**
	 *
	 * @param requirements
	 *            - those will be transformed
	 * @param fields
	 *            - which fields to include
	 * @param stopWords
	 *            - filter stopwords?
	 * @param upperCase
	 *            - include only upperCase?
	 * @param interpreted
	 *            - add rupp interpretation?
	 * @return list of Requirement vectors that have the words from Requirements
	 *         as dimension
	 * @throws Exception
	 * @throws Throwable
	 */
	public List<RequirementVector> makeFieldVectors(final List<Requirement> requirements,
			final EvaluationConfig settings) throws Exception {
		List<RequirementVector> vectors = new ArrayList<RequirementVector>();
		TreeSet<String> alphabet = new TreeSet<String>();
		NLProcessor processor = new NLProcessor();
		// generate global text
		StringBuilder allRequirements = new StringBuilder();
		if (settings.isRuppInterpretation()) {
			for (Requirement requirement : requirements) {
				for (Field field : settings.getFields()) {
					String fieldContent = requirement.get(field);
					String cleanedContent = Util.clean(fieldContent).trim();
					if (field == Field.SUBJEKT_1 || field == Field.AKTIVITÄT_3) {
						String hashValue = "" + cleanedContent.hashCode();
						allRequirements.append(hashValue + " FIELD ");
					} else {
						allRequirements.append(cleanedContent + " FIELD ");
					}
				}
				if (settings.isSource()) {
					String fieldContent = requirement.get(Field.QUELLE_8);
					String cleanedContent = Util.clean(fieldContent);
					allRequirements.append(cleanedContent + " FIELD ");
				}
				allRequirements.append("REQUIREMENT ");
			}
		} else {
			for (Requirement requirement : requirements) {
				for (Field field : settings.getFields()) {
					String fieldContent = requirement.get(field);
					String cleanedContent = Util.clean(fieldContent);
					// add up to a string containing all words
					allRequirements.append(cleanedContent);
				}
				if (settings.isSource()) {
					String fieldContent = requirement.get(Field.QUELLE_8);
					String cleanedContent = Util.clean(fieldContent);
					allRequirements.append(cleanedContent);
				}
				// seperate requirements by this notation
				allRequirements.append("REQUIREMENT ");
			}
		}

		String text = allRequirements.toString();
		// fill up empty requirements, so that we know all have been processed
		// by NLP (otherwise split.length != size)
		return this.applyModifiers(requirements, settings, vectors, alphabet, processor, text);

	}

	private List<RequirementVector> makeShortTextVectors(final List<Requirement> requirements,
			final EvaluationConfig settings) throws Exception {
		List<RequirementVector> vectors = new ArrayList<RequirementVector>();
		TreeSet<String> alphabet = new TreeSet<String>();
		NLProcessor processor = new NLProcessor();
		// generate global text
		StringBuilder allRequirements = new StringBuilder();
		for (Requirement requirement : requirements) {
			String fieldContent = requirement.get(Field.TEXT);
			String cleanedContent = Util.clean(fieldContent);
			// add up to a string containing all words
			allRequirements.append(cleanedContent);

			// seperate requirements by this notation
			allRequirements.append("REQUIREMENT ");
		}

		String text = allRequirements.toString();
		return this.applyModifiers(requirements, settings, vectors, alphabet, processor, text);
	}

	public List<RequirementVector> applyModifiers(final List<Requirement> requirements, final EvaluationConfig settings,
			final List<RequirementVector> vectors, final TreeSet<String> alphabet, final NLProcessor processor,
			String text) throws Exception {
		boolean changed = true;
		String oldText = text;
		while (changed) {
			text = text.replaceAll("REQUIREMENT REQUIREMENT", "REQUIREMENT x REQUIREMENT");
			if (oldText.equals(text)) {
				changed = false;
			} else {
				oldText = text;
			}
		}
		if (settings.isLemmatized()) {
			// workaround loop for nlp
			boolean succesfulAccess = false;
			while (!succesfulAccess) {
				// get the original text back, for new call
				text = oldText;
				// some cleaning
				String toLemmatize = text.replaceAll("  ", " ");
				List<String> content;
				try {
					content = processor.lemmatize(toLemmatize);
				} catch (Throwable e) {
					throw e;
				}
				// piece back to one text
				text = "";
				for (String word : content) {
					text += word + " ";
				}
				text = text.trim();
				int size = text.split("REQUIREMENT").length;
				if (size != requirements.size()) {
					this.LOG.info("Synchronus NLP access during lemmatization, trying again");
					Random random = new Random();
					Thread.sleep(random.nextInt(10) * 1000);
				} else {
					succesfulAccess = true;
				}
			}
		}
		String afterLemmatized = text;
		if (settings.isStopWords()) {
			// workaround loop for nlp
			boolean succesfulAccess = false;
			while (!succesfulAccess) {
				// get the original text for new call
				text = afterLemmatized;
				// some cleaning
				String toFilter = text.replaceAll("  ", " ");
				List<String> content;
				try {
					content = processor.filterStopWords(toFilter);
				} catch (Throwable e) {
					throw e;
				}
				// piece back to one text
				text = "";
				for (String word : content) {
					text += word + " ";
				}
				text = text.trim();
				int size = text.split("REQUIREMENT").length;
				if (size != requirements.size()) {
					this.LOG.info("Synchronus NLP access during stopwords filtering, trying again");

					Random random = new Random();
					Thread.sleep(random.nextInt(10) * 1000);
				} else {
					succesfulAccess = true;
				}
			}
		}
		if (settings.isSynonyms()) {
			text = processor.replaceWithSynonyms(text, requirements);
		}
		if (settings.getGermaNetFunction() != null) {
			text = settings.getGermaNetFunction().replaceWithGermaNetFunction(text, requirements);
		}
		// **********************************************************************

		if (settings.isRuppInterpretation()) {
			alphabet.addAll(this.getAllFromFieldsWithAppendix(text, settings.getFields()));
		} else {
			alphabet.addAll(this.getAllFromFields(text));
		}

		this.LOG.info("Found {} total words, with parameters " + settings.toString(), alphabet.size());
		this.LOG.info("Producing word {} vectors...", requirements.size());
		String[] requirementsArray = text.split("REQUIREMENT");
		for (int i = 0; i < requirements.size(); i++) {
			// get one requirement
			String requirement = requirementsArray[i].trim();
			List<String> words = new ArrayList<String>();
			if (settings.isRuppInterpretation()) {
				// fill empty fields to ensure correct order
				changed = true;
				String oldRequirement = requirement;
				while (changed) {
					// do this to fill empty fields with something, so that we
					// get no nullpointer exception
					requirement = requirement.replaceAll("FIELD  FIELD", "FIELD x FIELD ");
					if (oldRequirement.equals(requirement)) {
						changed = false;
					} else {
						oldRequirement = requirement;
					}
				}
				// get one field
				List<String> fieldsText = Arrays.asList(requirement.split("FIELD"));
				// if requirement is empty
				if (fieldsText.size() == 0 || (fieldsText.size() == 1 && fieldsText.get(0).equals(" "))) {
					words = new ArrayList<>();
				} else {
					for (int j = 0; j < settings.getFields().size(); j++) {
						// get one word
						if (!fieldsText.get(j).equals("x")) {
							List<String> part = Arrays.asList(fieldsText.get(j).split(" "));
							for (String word : part) {
								word = word + "_" + settings.getFields().get(j);
								words.add(word);
							}
						}
					}
				}

			} else {
				List<String> part = Arrays.asList(requirement.split(" "));
				for (String word : part) {
					words.add(word);
				}

			}
			if (requirement.equals("x")) {
				words = new ArrayList<>();
			}
			vectors.add(new RequirementVector(alphabet, words, requirements.get(i).getObjId(),
					requirements.get(i).getTitel()));

		}
		this.LOG.info("Done calculating vectors");
		return vectors;
	}

	private TreeSet<String> getAllFromFieldsWithAppendix(final String text, final List<Field> fields) throws Exception {
		// Collect all words
		TreeSet<String> words = new TreeSet<String>();
		// split between requirements
		String[] requirementsArray = text.split("REQUIREMENT");

		for (String requirement : requirementsArray) {
			// ignore empty requirements
			if (!requirement.equals(" ")) {
				// first fill a letter into empty fields (to ensure the order is
				// correct)
				// replace all sometomes does not work (if the Fields are
				// "overlapping")
				boolean changed = true;
				String oldRequirement = requirement;
				while (changed) {
					requirement = requirement.replaceAll("FIELDFIELD", "FIELDxFIELD");
					if (oldRequirement.equals(requirement)) {
						changed = false;
					} else {
						oldRequirement = requirement;
					}
				}
				List<String> fieldsList = Arrays.asList(requirement.split("FIELD"));
				// if requirement is empty
				if (fieldsList.size() == 0 || (fieldsList.size() == 1 && fieldsList.get(0).equals(" "))) {
					continue;
				}
				for (int i = 0; i < fields.size(); i++) {
					// get the single words of the requirement
					if (!fieldsList.get(i).equals("x")) {
						List<String> part = Arrays.asList(fieldsList.get(i).split(" "));

						for (String word : part) {
							words.add(word + "_" + fields.get(i));
						}
					}

				}
			}
		}

		return words;
	}

	private TreeSet<String> getAllFromFields(final String text) throws Exception {
		// Collect all words
		TreeSet<String> words = new TreeSet<String>();
		String[] requirementsArray = text.split("REQUIREMENT");

		for (String requirement : requirementsArray) {
			// get the single words of the requirement
			List<String> part = Arrays.asList(requirement.split(" "));
			for (String word : part) {
				words.add(word);
			}
		}
		return words;

	}

	/**
	 *
	 * @param root
	 * @param input
	 * @return creates a List of the requirements contained in root && input
	 */
	public List<Requirement> toRequirementList(final Node<String> root, final List<Requirement> input) {
		ArrayList<Requirement> list = new ArrayList<Requirement>();
		for (Node<String> child : root.getChildren()) {
			if (!child.isFolder()) {
				Requirement add = null;
				for (Requirement req : input) {
					if (req.getObjId().equals(child.getData())) {
						add = req;
						break;
					}
				}
				list.add(add);

			}
			list.addAll(this.toRequirementList(child, input));
		}
		return list;
	}

	public List<Requirement> toList(final List<Cluster> clusters, final List<Requirement> input) {
		ArrayList<Requirement> list = new ArrayList<Requirement>();
		for (int i = 0; i < clusters.size(); i++) {
			Cluster cluster = clusters.get(i);
			for (Requirement requirement : cluster.getRequirements()) {
				list.add(requirement);
			}
		}
		return list;
	}

	/**
	 *
	 * @param testData
	 * @param requirements
	 * @return creates humanResult from the given Node
	 */
	public Result inputIntoResult(final Node<String> testData, final List<Requirement> requirements) {
		List<Cluster> clusters = new ArrayList<Cluster>();
		for (Node<String> child : testData.getChildren()) {
			List<Requirement> preCluster = new ArrayList<Requirement>();
			if (!child.isFolder()) {
				Requirement req = Util.getRequirement(child.getData(), requirements);
				preCluster.add(req);
			}
			preCluster.addAll(this.getChildrenAsRequirments(child, requirements));
			Cluster cluster = new Cluster(preCluster);
			clusters.add(cluster);
		}
		Result r = new Result(clusters, (long) 0);

		return r;
	}

	/**
	 *
	 * @param testData
	 * @param requirements
	 * @return creates humanResult from the given Node
	 */
	public Result humanReferenceAlpha(final Node<String> testData, final List<Requirement> requirements) {
		List<Cluster> clusters = new ArrayList<Cluster>();
		// ebene Betrieb und Funktionalität
		for (Node<String> child : testData.getChildren()) {
			// Ebene darunter
			for (Node<String> child2 : child.getChildren()) {
				// die Ebene die ich haben will
				for (Node<String> child3 : child2.getChildren()) {
					List<Requirement> preCluster = new ArrayList<Requirement>();
					if (!child3.isFolder()) {
						Requirement req = Util.getRequirement(child3.getData(), requirements);
						preCluster.add(req);
					}
					preCluster.addAll(this.getChildrenAsRequirments(child3, requirements));
					Cluster cluster = new Cluster(preCluster);
					clusters.add(cluster);
				}
			}
		}
		List<Integer> remove = new ArrayList<>();
		for (int i = 0; i < clusters.size(); i++) {
			Cluster cluster = clusters.get(i);
			if (cluster.getRequirements().size() == 0) {
				remove.add(i);
			}
		}
		for (int x : remove) {
			clusters.remove(x);
		}
		Result r = new Result(clusters, (long) 0);

		return r;
	}

	/**
	 *
	 * @param testData
	 * @param requirements
	 * @return creates humanResult from the given Node
	 */
	public Result humanReferenceDelta(final Node<String> testData, final List<Requirement> requirements) {
		List<Cluster> clusters = new ArrayList<Cluster>();
		// ebene Betrieb und Funktionalität
		for (Node<String> child : testData.getChildren()) {
			// Ebene darunter
			for (Node<String> child2 : child.getChildren()) {
				// die Ebene die ich haben will
				for (Node<String> child3 : child2.getChildren()) {
					List<Requirement> preCluster = new ArrayList<Requirement>();
					preCluster.addAll(this.getChildrenAsRequirments(child3, requirements));
					Cluster cluster = new Cluster(preCluster);
					clusters.add(cluster);
				}
			}
		}
		List<Integer> remove = new ArrayList<>();
		for (int i = 0; i < clusters.size(); i++) {
			Cluster cluster = clusters.get(i);
			if (cluster.getRequirements().size() == 0) {
				remove.add(i);
			}
		}
		for (int x : remove) {
			clusters.remove(x);
		}
		Result r = new Result(clusters, (long) 0);

		return r;
	}

	/**
	 *
	 * @param testData
	 * @param requirements
	 * @return creates humanResult from the given Node
	 */
	public Result humanReferenceBeta(final Node<String> testData, final List<Requirement> requirements) {
		List<Cluster> clusters = new ArrayList<Cluster>();
		// 10 clusters in testdata
		for (Node<String> child : testData.getChildren()) {
			List<Requirement> preCluster = new ArrayList<Requirement>();
			preCluster.addAll(this.getChildrenAsRequirments(child, requirements));
			Cluster cluster = new Cluster(preCluster);
			clusters.add(cluster);

		}
		List<Integer> remove = new ArrayList<>();
		for (int i = 0; i < clusters.size(); i++) {
			Cluster cluster = clusters.get(i);
			if (cluster.getRequirements().size() == 0) {
				clusters.remove(i);
				i--;
			}
		}
		// for(int x : remove){
		// clusters.remove(x);
		// }
		Result r = new Result(clusters, (long) 0);

		return r;
	}

	private List<Requirement> getChildrenAsRequirments(final Node<String> parent,
			final List<Requirement> requirements) {
		List<Requirement> preCluster = new ArrayList<Requirement>();
		if (!parent.isFolder()) {
			Requirement req = Util.getRequirement(parent.getData(), requirements);
			preCluster.add(req);
		}

		for (Node<String> child : parent.getChildren()) {
			preCluster.addAll(this.getChildrenAsRequirments(child, requirements));
		}

		return preCluster;
	}

	/**
	 *
	 * @param vectors
	 * @return vectors weighted with tfidf
	 */
	public List<RequirementVector> weightWithTfIdf(final List<RequirementVector> vectors,
			final EvaluationConfig settings) {
		List<RequirementVector> tfidfVectors = new ArrayList<RequirementVector>();
		// determine idf for each word
		RequirementVector idfVector = new RequirementVector("-1");
		for (String key : vectors.get(0).getContentKeys()) {
			double idf = 0;
			for (RequirementVector v : vectors) {
				if (v.getContent(key) > 0) {
					idf++;
				}
			}
			if (idf != 0) {
				idf = (double) vectors.size() / idf;
				idf = Math.log(idf);
			}

			idfVector.putContent(key, idf);
		}
		// determine tf and tfidf for each word in a vector and create that
		// vector
		for (RequirementVector v : vectors) {
			RequirementVector tfidfofV = new RequirementVector(v.getID());
			for (String key : v.getContentKeys()) {
				double tf = v.getContent(key);
				double idf = idfVector.getContent(key);
				double tfidf = tf * idf;
				tfidfofV.putContent(key, tfidf);
				if (settings.isRuppInterpretation()) {
					Field field = Field.valueOf(key.split("_")[1] + "_" + key.split("_")[2]);
					tfidfofV.putContent(key, tfidf, field);
				}

			}
			tfidfVectors.add(tfidfofV);
		}
		return tfidfVectors;
	}

	public void filterHumanReference(final Result humanReference, final List<Requirement> utilityRequirements) {
		for (Cluster cluster : humanReference.getClusters()) {
			Iterator<Requirement> iterator = cluster.getRequirements().iterator();
			while (iterator.hasNext()) {
				Requirement requirement = iterator.next();
				if (!utilityRequirements.contains(requirement)) {
					iterator.remove();
				}
			}
		}

	}

	public static Instances getInstances(final ClusterDataGenerator generator,
			final List<RequirementVector> tfidfVectors) throws IOException {
		Random random = new Random();
		int arffName = random.nextInt();
		generator.vectorsIntoArff(tfidfVectors, "vectors" + arffName);
		BufferedReader reader = Util.readDataFile(Config.EVALUATIONFRAMEWORK + "/temp/vectors" + arffName + ".arff");
		Instances data = new Instances(reader);
		reader.close();
		Files.delete(Paths.get(Config.EVALUATIONFRAMEWORK + "/temp/vectors" + arffName + ".arff"));
		return data;
	}

	public List<Requirement> shortTextLoader() throws IOException {
		BufferedReader reader = Util.readDataFile(Main.DATASET.db);
		List<Requirement> requirements = new ArrayList<>();
		String line = "";
		while ((line = reader.readLine()) != null) {
			String id = line.split(" ")[0];
			String text = line.substring(line.indexOf(' ') + 1);
			Requirement requirement = new Requirement();
			for (Field field : Field.values()) {
				requirement.set(field, " ");
			}
			requirement.set(Field.OBJ_ID, id);
			requirement.set(Field.TEXT, text);
			requirements.add(requirement);
		}
		return requirements;
	}

	public Result generateHumanReferenceShortTexts(final ModelContainer container) throws IOException {
		List<Cluster> clusters = new ArrayList<Cluster>();
		List<Requirement> data = container.getRequirementsUnmodifiable();
		BufferedReader reader = Util.readDataFile(Main.DATASET.verificationData);
		String line = "";
		while ((line = reader.readLine()) != null) {
			String[] elements = line.split(" ");
			List<Requirement> requirements = new ArrayList<>();
			for (int i = 0; i < elements.length; i++) {
				Requirement requirement = Util.getRequirement(elements[i], data);
				requirements.add(requirement);
			}
			Cluster cluster = new Cluster(requirements);
			clusters.add(cluster);
		}
		Result r = new Result(clusters, (long) 0);

		return r;
	}

}
