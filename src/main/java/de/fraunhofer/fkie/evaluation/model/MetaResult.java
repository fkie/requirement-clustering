package de.fraunhofer.fkie.evaluation.model;

import java.io.Serializable;
import java.util.List;

import de.fraunhofer.fkie.aidpfm.germanet.GermaNetFunction;
import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceHandler;

public class MetaResult implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private double[] f1 = null;
	private double[] f1Max = null;
	private double[] naive = null;
	private double[] naiveMax = null;
	private double[] precision = null;
	private double precisionMax = -1;
	private double[] recall = null;
	private double[] recallMax = null;
	private double[] rand = null;
	private double randMax = -1;
	private double[] jaccard = null;
	private double jaccardMax = -1;
	private double[] purity = null;
	private double purityMax = -1;
	private double[] cohesion = null;
	private double cohesionMax = -1;
	private double[] seperation = null;
	private double seperationMax = -1;
	private double[] silhouette = null;
	private double silhouetteMax = -1;
	private double accordance = -1;
	private double[] runtime = null;
	private double[] f1Weighted = null;
	private double[] precisionWeighted = null;
	private double[] recallWeighted = null;
	private double[] jaccardWeighted = null;

	private int parameter = 0;
	private String[] fields = null;
	private List<Result> results = null;
	private String clusterer = "";
	private String distance = "";
	private boolean tfidf = false;
	private boolean stopWords = false;
	private boolean lemmatized = false;
	private boolean onthology = false;
	private boolean synonyms = false;
	private boolean ruppInterpretation = true;
	private boolean upperCase = false;
	private boolean source = false;
	private GermaNetFunction germaNetFunction;
	private DistanceHandler distanceHandler;

	public List<Result> getResults() {
		return this.results;
	}

	public void setResults(final List<Result> results) {
		this.results = results;
	}

	public double[] getF1Max() {
		return this.f1Max;
	}

	public void setF1Max(final double[] f1Max) {
		this.f1Max = f1Max;
	}

	public double[] getNaiveMax() {
		return this.naiveMax;
	}

	public void setNaiveMax(final double[] naiveMax) {
		this.naiveMax = naiveMax;
	}

	public double getPrecisionMax() {
		return this.precisionMax;
	}

	public void setPrecisionMax(final double precisionMax) {
		this.precisionMax = precisionMax;
	}

	public double[] getRecallMax() {
		return this.recallMax;
	}

	public void setRecallMax(final double[] recallMax) {
		this.recallMax = recallMax;
	}

	public double getRandMax() {
		return this.randMax;
	}

	public void setRandMax(final double randMax) {
		this.randMax = randMax;
	}

	public double getJaccardMax() {
		return this.jaccardMax;
	}

	public void setJaccardMax(final double jaccardMax) {
		this.jaccardMax = jaccardMax;
	}

	public double getPurityMax() {
		return this.purityMax;
	}

	public void setPurityMax(final double purityMax) {
		this.purityMax = purityMax;
	}

	public double[] getF1Weighted() {
		return this.f1Weighted;
	}

	public void setF1Weighted(final double[] f1Weighted) {
		this.f1Weighted = f1Weighted;
	}

	public double[] getPrecisionWeighted() {
		return this.precisionWeighted;
	}

	public void setPrecisionWeighted(final double[] precisionWeighted) {
		this.precisionWeighted = precisionWeighted;
	}

	public double[] getRecallWeighted() {
		return this.recallWeighted;
	}

	public void setRecallWeighted(final double[] recallWeighted) {
		this.recallWeighted = recallWeighted;
	}

	public double[] getJaccardWeighted() {
		return this.jaccardWeighted;
	}

	public void setJaccardWeighted(final double[] jaccardWeighted) {
		this.jaccardWeighted = jaccardWeighted;
	}

	public double getCohesionMax() {
		return this.cohesionMax;
	}

	public void setCohesionMax(final double cohesionMax) {
		this.cohesionMax = cohesionMax;
	}

	public double getSeperationMax() {
		return this.seperationMax;
	}

	public void setSeperationMax(final double seperationMax) {
		this.seperationMax = seperationMax;
	}

	public double getSilhouetteMax() {
		return this.silhouetteMax;
	}

	public void setSilhouetteMax(final double silhouetteMax) {
		this.silhouetteMax = silhouetteMax;
	}

	public double[] getCohesion() {
		return this.cohesion;
	}

	public double getCohesionAvg() {
		return this.average(this.cohesion);
	}

	public double getCohesionStd() {
		return this.deviation(this.cohesion);
	}

	public void setCohesion(final double[] cohesion) {
		this.cohesion = cohesion;
	}

	public double[] getSeperation() {
		return this.seperation;
	}

	public double getSeperationAvg() {
		return this.average(this.seperation);
	}

	public double getSeperationStd() {
		return this.deviation(this.seperation);
	}

	public void setSeperation(final double[] seperation) {
		this.seperation = seperation;
	}

	public double[] getSilhouette() {
		return this.silhouette;
	}

	public double getSilhouetteAvg() {
		return this.average(this.silhouette);
	}

	public double getSilhouetteStd() {
		return this.deviation(this.silhouette);
	}

	public void setSilhouette(final double[] silhouette) {
		this.silhouette = silhouette;
	}

	public double[] getRand() {
		return this.rand;
	}

	public double getRandAvg() {
		return this.average(this.rand);
	}

	public double getRandStd() {
		return this.deviation(this.rand);
	}

	public void setRand(final double[] rand) {
		this.rand = rand;
	}

	public double[] getJaccard() {
		return this.jaccard;
	}

	public double getJaccardAvg() {
		return this.average(this.jaccard);
	}

	public double getJaccardStd() {
		return this.deviation(this.jaccard);
	}

	public void setJaccard(final double[] jaccard) {
		this.jaccard = jaccard;
	}

	public double[] getPurity() {
		return this.purity;
	}

	public double getJaccardWeightedAvg() {
		try {
			return this.average(this.jaccard);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getJaccardWeightedStd() {
		try {
			return this.deviation(this.jaccard);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getPurityAvg() {
		return this.average(this.purity);
	}

	public double getPurityStd() {
		return this.deviation(this.purity);
	}

	public void setPurity(final double[] purity) {
		this.purity = purity;
	}

	public boolean isUpperCase() {
		return this.upperCase;
	}

	public void setUpperCase(final boolean upperCase) {
		this.upperCase = upperCase;
	}

	public boolean isTfidf() {
		return this.tfidf;
	}

	public void setTfidf(final boolean tfidf) {
		this.tfidf = tfidf;
	}

	public boolean isStopWords() {
		return this.stopWords;
	}

	public void setStopWords(final boolean stopWords) {
		this.stopWords = stopWords;
	}

	public boolean isLemmatized() {
		return this.lemmatized;
	}

	public void setLemmatized(final boolean lemmatized) {
		this.lemmatized = lemmatized;
	}

	public boolean isOnthology() {
		return this.onthology;
	}

	public void setOnthology(final boolean onthology) {
		this.onthology = onthology;
	}

	public boolean isSynonyms() {
		return this.synonyms;
	}

	public void setSynonyms(final boolean synonms) {
		this.synonyms = synonms;
	}

	public boolean isRuppInterpretation() {
		return this.ruppInterpretation;
	}

	public void setRuppInterpretation(final boolean ruppInterpretation) {
		this.ruppInterpretation = ruppInterpretation;
	}

	public String getFields() {
		String text = "";
		for (String field : this.fields) {
			text += field + ",";
		}
		text = text.substring(0, text.length() - 1);
		return text;
	}

	public void setFields(final List<Field> fields) {
		String fieldStringArray[] = new String[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			fieldStringArray[i] = fields.get(i).fieldname;
		}
		this.fields = fieldStringArray;
	}

	public double[] getF1() {
		return this.f1;
	}

	public void setF1(final double[] f1) {
		this.f1 = f1;
	}

	public double[] getNaive() {
		return this.naive;
	}

	public void setNaive(final double[] naive) {
		this.naive = naive;
	}

	public double getF1Avg() {
		return this.average(this.f1);
	}

	public double getF1Var() {
		return this.variance(this.f1);
	}

	public double getF1Std() {
		return this.deviation(this.f1);
	}

	public double getF1WeightedAvg() {
		try {
			return this.average(this.f1Weighted);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getF1WeightedVar() {
		try {
			return this.variance(this.f1Weighted);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getF1WeightedStd() {
		try {
			return this.deviation(this.f1Weighted);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getNaiveAvg() {
		return this.average(this.naive);
	}

	public double getNaiveVar() {
		return this.variance(this.naive);
	}

	public double getNaiveStd() {
		return this.deviation(this.naive);
	}

	public double getPrecisionWeightedAvg() {
		try {
			return this.average(this.precisionWeighted);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getPrecisionWeightedVar() {
		try {
			return this.variance(this.precisionWeighted);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getPrecisionWeightedStd() {
		try {
			return this.deviation(this.precisionWeighted);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getRecallWeightedAvg() {
		try {
			return this.average(this.recallWeighted);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getRecallWeightedVar() {
		try {
			return this.variance(this.recallWeighted);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getRecallWeightedStd() {
		try {
			return this.deviation(this.recallWeighted);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getPrecisionAvg() {
		return this.average(this.precision);
	}

	public double getPrecisionVar() {
		return this.variance(this.precision);
	}

	public double getPrecisionStd() {
		return this.deviation(this.precision);
	}

	public double getRecallAvg() {
		return this.average(this.recall);
	}

	public double getRecallVar() {
		return this.variance(this.recall);
	}

	public double getRecallStd() {
		return this.deviation(this.recall);
	}

	public int getParameter() {
		return this.parameter;
	}

	public void setParameter(final int parameter) {
		this.parameter = parameter;
	}

	public double getAccordance() {
		return this.accordance;
	}

	public void setAccordance(final double accordance) {
		this.accordance = accordance;
	}

	public String getClusterer() {
		return this.clusterer;
	}

	public void setClusterer(final String clusterer) {
		this.clusterer = clusterer;
	}

	public String getDistance() {
		return this.distance;
	}

	public void setDistance(final String distance) {
		this.distance = distance;
	}

	public double[] getRuntime() {
		return this.runtime;
	}

	public void setRuntime(final double[] runtime) {
		this.runtime = runtime;
	}

	public double getRuntimeAvg() {
		return this.average(this.runtime);
	}

	public double[] getPrecision() {
		return this.precision;
	}

	public void setPrecision(final double[] precision) {
		this.precision = precision;
	}

	public double[] getRecall() {
		return this.recall;
	}

	public void setRecall(final double[] recall) {
		this.recall = recall;
	}

	private double average(final double[] array) {
		double average = 0;
		for (double x : array) {
			average += x;
		}
		return average / array.length;
	}

	private double variance(final double[] array) {
		double average = this.average(array);
		double variance = 0;
		for (double x : array) {
			variance += Math.pow((x - average), 2);
		}

		return variance / array.length;
	}

	private double deviation(final double[] array) {
		double variance = this.variance(array);
		return Math.sqrt(variance);
	}

	public boolean isSource() {
		return this.source;
	}

	public void setSource(final boolean source) {
		this.source = source;
	}

	public GermaNetFunction getGermaNetFunction() {
		return this.germaNetFunction;
	}

	public void setGermaNetFunction(final GermaNetFunction germaNetFunction) {
		this.germaNetFunction = germaNetFunction;
	}

	public DistanceHandler getDistanceHandler() {
		return this.distanceHandler;
	}

	public void setDistanceHandler(final DistanceHandler distanceHandler) {
		this.distanceHandler = distanceHandler;
	}

	public String info() {
		String info = this.clusterer + ";" + this.distance + ";" + this.fields.length + ";" + this.tfidf + ";"
				+ this.stopWords + ";" + this.ruppInterpretation + ";" + this.lemmatized + ";" + this.source + ";"
				+ this.synonyms + ";" + this.germaNetFunction + ";" + Util.round(this.getF1WeightedAvg()) + ";"
				+ Util.round(this.getF1WeightedStd()) + ";" + Util.round(this.getCohesionAvg()) + ";"
				+ Util.round(this.getCohesionStd()) + ";" + Util.round(this.getSeperationAvg()) + ";"
				+ Util.round(this.getSeperationStd()) + ";" + Util.round(this.getSilhouetteAvg()) + ";"
				+ Util.round(this.getSilhouetteStd()) + ";" + this.getRuntimeAvg();
		return info;
	}

}
