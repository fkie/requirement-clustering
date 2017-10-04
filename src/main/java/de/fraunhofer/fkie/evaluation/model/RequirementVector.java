package de.fraunhofer.fkie.evaluation.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import mikera.vectorz.Vector;

public class RequirementVector {

	// protected Map<String, Double> content;

	// protected Map<Field, Map<String, Double>> fieldsContent;
	int totalWords = 0;
	private TreeSet<String> alphabet;
	private static Map<String, Integer> wordOrder;
	private static Map<Field, Map<String, Integer>> wordOrderField;
	private String id;
	private String name;
	private boolean locked;
	protected Vector vectorRepresentation;
	protected Map<Field, Vector> fieldVectorRepresentation;

	public RequirementVector(final TreeSet<String> alphabet, final List<String> doc, final String id,
			final String name) {
		// this.content = new HashMap<String, Double>();
		// this.fieldsContent = new HashMap<>();
		this.id = id;
		this.name = name;
		// this.initialiseFieldContent(this.fieldsContent);
		if (wordOrder == null) {
			wordOrder = new HashMap<>();
			wordOrderField = new HashMap<>();
			this.initialiseFieldContent();
			this.initialiseContent(alphabet);
		}
		this.fieldVectorRepresentation = new HashMap<>();
		this.alphabet = alphabet;
		this.set(doc);
		// this.initVectors();
	}

	public RequirementVector(final String id) {
		this.id = id;
		double[] vector = new double[wordOrder.size()];
		this.vectorRepresentation = Vector.create(vector);
		this.fieldVectorRepresentation = new HashMap<>();
		for (Field field : Util.getRuppFields()) {
			double[] fieldVector = new double[wordOrderField.get(field).size()];
			this.fieldVectorRepresentation.put(field, Vector.create(fieldVector));
		}
	}

	public RequirementVector(final Vector v, final Map<Field, Vector> vMap, final String id) {
		// this.content = new HashMap<>();
		// this.fieldsContent = new HashMap<>();
		this.id = id;
		this.vectorRepresentation = v;
		this.fieldVectorRepresentation = vMap;
	}

	public static Map<String, Integer> getWordOrder() {
		return wordOrder;
	}

	public static void setWordOrder(final Map<String, Integer> wordOrder) {
		RequirementVector.wordOrder = wordOrder;
	}

	public static Map<Field, Map<String, Integer>> getWordOrderField() {
		return wordOrderField;
	}

	public static void setWordOrderField(final Map<Field, Map<String, Integer>> wordOrderField) {
		RequirementVector.wordOrderField = wordOrderField;
	}

	public String getID() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public double getContent(final String key) {
		int index = wordOrder.get(key);
		return this.vectorRepresentation.get(index);
	}

	public double getFieldContent(final Field field, final String key) {
		int index = wordOrderField.get(field).get(key);
		return this.fieldVectorRepresentation.get(field).get(index);
	}

	public Set<String> getContentKeys() {
		return new HashSet<String>(wordOrder.keySet());
	}

	// public Double getContent(final String key, final Field field) {
	// return this.fieldsContent.get(field).get(key);
	// }

	// public Set<String> getContentKeys(final Field field) {
	// return this.fieldsContent.get(field).keySet();
	// }
	//
	// public Map<String, Double> getContentMap() {
	// return Collections.unmodifiableMap(this.content);
	// }

	// public Map<String, Double> getContentMap(final Field field) {
	// return Collections.unmodifiableMap(this.fieldsContent.get(field));
	// }

	public void putContent(final String key, final double value) {
		if (this.locked) {
			throw new IllegalAccessError();
		}
		int index = wordOrder.get(key);
		this.vectorRepresentation.set(index, value);
	}

	public void putContent(final String key, final double value, final Field field) {
		if (this.locked) {
			throw new IllegalAccessError();
		}
		int index = wordOrderField.get(field).get(key);
		this.fieldVectorRepresentation.get(field).set(index, value);
	}

	public Vector getVectorRepresentation() {
		return this.vectorRepresentation;
	}

	public Vector getFieldVectorRepresentation(final Field field) {
		return this.fieldVectorRepresentation.get(field);
	}

	public boolean isLocked() {
		return this.locked;
	}

	public void lock() {
		this.locked = true;
	}

	private void initialiseFieldContent() {
		for (Field field : Util.getRuppFields()) {
			Map<String, Integer> fieldOrder = new HashMap<>();
			wordOrderField.put(field, fieldOrder);
		}
	}

	/**
	 * sets the vector values in the correct order
	 *
	 * @param doc
	 */
	public void set(final List<String> doc) {
		// the vector for all words
		double[] vector = new double[wordOrder.size()];
		// the vectors for each field
		Map<Field, double[]> fieldVectors = new HashMap<>();
		for (Field field : Util.getRuppFields()) {
			int size = wordOrderField.get(field).size();
			fieldVectors.put(field, new double[size]);
		}
		for (int i = 0; i < doc.size(); i++) {
			String word = doc.get(i);
			if (this.alphabet.contains(word)) {
				if (word.split("_").length > 1) {
					// 1 contains the name and 2 the number
					Field field = Field.valueOf(word.split("_")[1] + "_" + word.split("_")[2]);
					String key = word.split("_")[0].toLowerCase() + "_" + field;
					// increases the vector of field at position from the
					// wordOrder(field) by 1
					int fieldPosition = wordOrderField.get(field).get(key);
					fieldVectors.get(field)[fieldPosition] += 1;
					vector[wordOrder.get(key)] += 1;
				} else {
					vector[wordOrder.get(word)] += 1;
				}
				this.totalWords++;
			}
		}

		this.vectorRepresentation = Vector.create(vector);
		for (Field field : Util.getRuppFields()) {
			this.fieldVectorRepresentation.put(field, Vector.create(fieldVectors.get(field)));
		}
	}

	/**
	 * sets the order of the words
	 *
	 * @param alphabet
	 */
	private void initialiseContent(final TreeSet<String> alphabet) {
		// saves current position for wordOrder
		int i = 0;
		// saves field positions
		Map<Field, Integer> fieldPosition = new HashMap<>();
		for (Field field : Util.getRuppFields()) {
			fieldPosition.put(field, 0);
		}
		for (String word : alphabet) {
			if (word.split("_").length > 1) {
				// 1 contains the name and 2 the number
				Field field = Field.valueOf(word.split("_")[1] + "_" + word.split("_")[2]);
				// only the first part to lower case
				String key = word.split("_")[0].toLowerCase() + "_" + field;
				if (!wordOrderField.get(field).containsKey(key)) {
					wordOrderField.get(field).put(key, fieldPosition.get(field));
					fieldPosition.replace(field, fieldPosition.get(field) + 1);
				}
				if (!wordOrder.containsKey(key)) {
					wordOrder.put(key, i);
					i++;
				}

			} else {
				if (!wordOrder.containsKey(word)) {
					wordOrder.put(word, i);
					i++;
				}
			}

		}
	}

	// public void initVectors() {
	// if (this.wordOrder == null) {
	// this.wordOrder = new HashMap<>();
	// int i = 0;
	// for (String key : this.content.keySet()) {
	// this.wordOrder.put(key, i);
	// i++;
	// }
	// this.wordOrderField = new HashMap<>();
	// for (Field field : Util.getRuppFields()) {
	// Map<String, Double> fieldMap = this.fieldsContent.get(field);
	// Map<String, Integer> fieldList = new HashMap<>();
	// i = 0;
	// for (String key : fieldMap.keySet()) {
	// fieldList.put(key, i);
	// i++;
	// }
	// this.wordOrderField.put(field, fieldList);
	// }
	// }
	// this.fieldVectorRepresentation = new HashMap<>();
	// double[] valueArray = new double[this.content.size()];
	// int i = 0;
	// for (String key : this.content.keySet()) {
	// valueArray[i] = this.content.get(key);
	// i++;
	// }
	//
	// this.vectorRepresentation = Vector.create(valueArray);
	//
	// for (Field field : Util.getRuppFields()) {
	// Map<String, Double> fieldMap = this.fieldsContent.get(field);
	// if (fieldMap.keySet().size() > 0) {
	// double[] fieldArray = new double[fieldMap.size()];
	// i = 0;
	// for (String key : fieldMap.keySet()) {
	// fieldArray[i] = fieldMap.get(key);
	// i++;
	// }
	// Vector vector = Vector.create(fieldArray);
	// this.fieldVectorRepresentation.put(field, vector);
	// } else {
	// this.fieldVectorRepresentation.put(field, Vector.of(0));
	// }
	// }
	// this.content = null;
	// this.fieldsContent = null;
	// }

	public double tf(final String word) {
		return new Double(this.vectorRepresentation.get(wordOrder.get(word))) / this.totalWords;
	}

	@Override
	public String toString() {
		return this.vectorRepresentation.toString();
	}

	// public void concatVector(final RequirementVector v) {
	// this.content.putAll(v.content);
	// }

	// public void add(final RequirementVector v) {
	// for (Map.Entry<String, Double> entry : this.content.entrySet()) {
	// double value = entry.getValue();
	// value += v.content.get(entry.getKey());
	// entry.setValue(value);
	// }
	// }

	// public void normalize(final int normalizer) {
	// for (Map.Entry<String, Double> entry : this.content.entrySet()) {
	// double value = entry.getValue();
	// value = value / normalizer;
	// entry.setValue(value);
	// }
	// }
	//
	// public void normalize(final double normalizer) {
	// for (Map.Entry<String, Double> entry : this.content.entrySet()) {
	// double value = entry.getValue();
	// value = value / normalizer;
	// entry.setValue(value);
	// }
	// }

	public double normOfMap() {
		double count = 0;
		for (int i = 0; i < this.vectorRepresentation.length(); i++) {
			if (this.vectorRepresentation.get(i) > 0) {
				count++;
			}
		}
		return count;
	}

	public double sizeOfContent() {
		return wordOrder.size();
	}

	public void fillFieldsMap() {
		for (String word : wordOrder.keySet()) {
			if (word.split("_").length > 1) {
				// 1 contains the name and 2 the number
				Field field = Field.valueOf(word.split("_")[1] + "_" + word.split("_")[2]);
				// get the field vector and set at position you get from the
				// field worder to the value you find and vector for this word
				this.fieldVectorRepresentation.get(field).set(wordOrderField.get(field).get(word),
						this.vectorRepresentation.get(wordOrder.get(word)));
			}
		}
	}

}