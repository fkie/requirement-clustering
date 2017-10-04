package de.fraunhofer.fkie.evaluation.model;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fkie.aidpfm.germanet.GermaNetFunction;
import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceHandler;

/**
 * 
 * @author daniel.toews This class allows to visualize in a simple manner the
 *         settings of the current evaluation It will be used as a static
 *         variable in main
 */

public class EvaluationConfig {
	private String clusterer;
	private String distance;
	private boolean tfidf;
	private boolean stopWords;
	private boolean lemmatized;
	private boolean onthology;
	private boolean synonyms;
	private boolean ruppInterpretation;
	private boolean upperCase;
	private boolean source;
	private int parameter;
	private List<Field> fields;
	private GermaNetFunction germaNetFunction;
	private DistanceHandler distanceHandler;

	public EvaluationConfig() {
		super();
		this.tfidf = true;
		this.stopWords = true;
		this.lemmatized = true;
		this.onthology = false;
		this.synonyms = true;
		this.ruppInterpretation = true;
		this.upperCase = true;
		this.source = true;
		this.clusterer = "";
		this.distance = "";
		this.parameter = 0;
		this.fields = new ArrayList<>();
		fields.add(Field.OBJECTUNDERGÄNZUNG_4);
		this.germaNetFunction = null;
	}

	public void allTrue() {
		this.tfidf = true;
		this.stopWords = true;
		this.lemmatized = true;
		this.onthology = true;
		this.synonyms = true;
		this.ruppInterpretation = true;
		this.upperCase = true;
		this.source = true;
	}

	public void allFalse() {
		this.tfidf = false;
		this.stopWords = false;
		this.lemmatized = false;
		this.onthology = false;
		this.synonyms = false;
		this.ruppInterpretation = false;
		this.upperCase = false;
		this.source = false;
	}

	public int getParameter() {
		return parameter;
	}

	public void setParameter(int parameter) {
		this.parameter = parameter;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	public String getClusterer() {
		return clusterer;
	}

	public void setClusterer(String clusterer) {
		this.clusterer = clusterer;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public boolean isTfidf() {
		return tfidf;
	}

	public void setTfidf(boolean tfidf) {
		this.tfidf = tfidf;
	}

	public boolean isStopWords() {
		return stopWords;
	}

	public void setStopWords(boolean stopWords) {
		this.stopWords = stopWords;
	}

	public boolean isLemmatized() {
		return lemmatized;
	}

	public void setLemmatized(boolean lemmatized) {
		this.lemmatized = lemmatized;
	}

	public boolean isOnthology() {
		return onthology;
	}

	public void setOnthology(boolean onthology) {
		this.onthology = onthology;
	}

	public boolean isSynonyms() {
		return synonyms;
	}

	public void setSynonyms(boolean synonms) {
		this.synonyms = synonms;
	}

	public boolean isRuppInterpretation() {
		return ruppInterpretation;
	}

	public void setRuppInterpretation(boolean ruppInterpretation) {
		this.ruppInterpretation = ruppInterpretation;
	}

	public boolean isUpperCase() {
		return upperCase;
	}

	public void setUpperCase(boolean upperCase) {
		this.upperCase = upperCase;
	}

	public boolean isSource() {
		return source;
	}

	public void setSource(boolean source) {
		this.source = source;
	}

	public GermaNetFunction getGermaNetFunction() {
		return germaNetFunction;
	}

	public void setGermaNetFunction(GermaNetFunction germaNetFunction) {
		this.germaNetFunction = germaNetFunction;
	}

	public DistanceHandler getDistanceHandler() {
		return distanceHandler;
	}

	public void setDistanceHandler(DistanceHandler distanceHandler) {
		this.distanceHandler = distanceHandler;
	}

	public String toString() {
		return this.clusterer + this.fields.size() + this.distance + this.tfidf + this.stopWords
				+ this.ruppInterpretation + this.lemmatized + this.onthology + this.source
				+ this.synonyms + this.germaNetFunction;
	}

	/**
	 * finds the difference between two evaluation configs and returns it
	 * 
	 * @param settings
	 * @return
	 */
	public String subtract(EvaluationConfig settings) {
		String subtraction = "";
		if(!this.clusterer.equals(settings.getClusterer())){
			subtraction += "Clusterer: " + this.clusterer + ", ";
		}
		if(!this.distance.equals(settings.getDistance())){
			subtraction += "Distance: " + this.distance + ", ";
		}

		if(this.fields.size() != settings.getFields().size()){
			subtraction += "Number of Fields : " + this.fields.size() + ", ";
		}

		if(this.fields.size() != settings.getFields().size()){
			subtraction += "Number of Fields : " + this.fields.size() + ", ";
		}

		if(this.parameter != settings.getParameter()){
			subtraction += "Minimum number of Occurrences : " + this.parameter + ", ";
		}

		if(this.tfidf != settings.isTfidf()){
			subtraction += "Tfidif : " + this.tfidf + ", ";
		}

		if(this.stopWords != settings.isStopWords()){
			subtraction += "StopWords : " + this.stopWords + ", ";
		}

		if(this.ruppInterpretation != settings.isRuppInterpretation()){
			subtraction += "Interpreted : " + this.ruppInterpretation + ", ";
		}

		if(this.upperCase != settings.isUpperCase()){
			subtraction += "UpperCase : " + this.upperCase + ", ";
		}

		if(this.lemmatized != settings.isLemmatized()){
			subtraction += "Lemmatized : " + this.lemmatized + ", ";
		}

		if(this.onthology != settings.isOnthology()){
			subtraction += "Onthology : " + this.onthology + ", ";
		}

		if(this.source != settings.isSource()){
			subtraction += "Source : " + this.source + ", ";
		}
		if(this.germaNetFunction == null && settings.getGermaNetFunction() != null){
			subtraction += "GermaNetFunction : null";
		}
		else if(this.germaNetFunction != null && settings.getGermaNetFunction() == null){
			subtraction += "GermaNetFunction : " + this.germaNetFunction + ", ";
		}
		else if(this.germaNetFunction != null && settings.getGermaNetFunction() != null
				&& this.germaNetFunction.getClass().equals(settings.getGermaNetFunction().getClass())){
			subtraction += "GermaNetFunction : " + this.germaNetFunction + ", ";
		}
		subtraction = subtraction.substring(0, subtraction.length() - 2);
		return subtraction;
	}


}
