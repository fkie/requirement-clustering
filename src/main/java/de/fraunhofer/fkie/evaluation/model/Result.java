package de.fraunhofer.fkie.evaluation.model;

import java.io.Serializable;
import java.util.List;

public class Result implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Cluster> clusters;

	private double naiveAccuracy = -1;
	private double f1 = -1;
	private double precision = -1;
	private double recall = -1;
	private double purity = -1;
	private double jaccard = -1;
	private double rand = -1;
	private double f1Weighted = -1;
	private double precisionWeighted = -1;
	private double recallWeighted = -1;
	private double jaccardWeighted = -1;
	private long runtime;
	private boolean accordance;
	private int[] f1Mapping;
	private int[] naiveMapping;
	private int[] purityMapping;
	private int[] jaccardMapping;
	private int[] randMapping;
	private int[] f1MappingWeighted;
	private int[] jaccardMappingWeighted;
	private double cohesion = -1;
	private double seperation = -1;
	private double silhouette = -1;

	public Result() {
	}

	public double getCohesion() {
		return this.cohesion;
	}

	public void setCohesion(final double cohesion) {
		this.cohesion = cohesion;
	}

	public double getSeperation() {
		return this.seperation;
	}

	public void setSeperation(final double seperation) {
		this.seperation = seperation;
	}

	public double getSilhouette() {
		return this.silhouette;
	}

	public void setSilhouette(final double silhouette) {
		this.silhouette = silhouette;
	}

	public int[] getF1Mapping() {
		return this.f1Mapping;
	}

	public void setF1Mapping(final int[] f1Mapping) {
		this.f1Mapping = f1Mapping;
	}

	public int[] getNaiveMapping() {
		return this.naiveMapping;
	}

	public void setNaiveMapping(final int[] naiveMapping) {
		this.naiveMapping = naiveMapping;
	}

	public int[] getPurityMapping() {
		return this.purityMapping;
	}

	public void setPurityMapping(final int[] purityMapping) {
		this.purityMapping = purityMapping;
	}

	public int[] getJaccardMapping() {
		return this.jaccardMapping;
	}

	public void setJaccardMapping(final int[] jaccardMapping) {
		this.jaccardMapping = jaccardMapping;
	}

	public int[] getRandMapping() {
		return this.randMapping;
	}

	public void setRandMapping(final int[] randMapping) {
		this.randMapping = randMapping;
	}

	public double getPurity() {
		return this.purity;
	}

	public void setPurity(final double purity) {
		this.purity = purity;
	}

	public double getJaccard() {
		return this.jaccard;
	}

	public void setJaccard(final double jaccard) {
		this.jaccard = jaccard;
	}

	public double getRand() {
		return this.rand;
	}

	public void setRand(final double rand) {
		this.rand = rand;
	}

	public double getF1Weighted() {
		return this.f1Weighted;
	}

	public void setF1Weighted(final double f1Weighted) {
		this.f1Weighted = f1Weighted;
	}

	public double getPrecisionWeighted() {
		return this.precisionWeighted;
	}

	public void setPrecisionWeighted(final double precisionWeighted) {
		this.precisionWeighted = precisionWeighted;
	}

	public double getRecallWeighted() {
		return this.recallWeighted;
	}

	public void setRecallWeighted(final double recallWeighted) {
		this.recallWeighted = recallWeighted;
	}

	public double getJaccardWeighted() {
		return this.jaccardWeighted;
	}

	public void setJaccardWeighted(final double jaccardWeighted) {
		this.jaccardWeighted = jaccardWeighted;
	}

	public int[] getF1MappingWeighted() {
		return this.f1MappingWeighted;
	}

	public void setF1MappingWeighted(final int[] f1MappingWeighted) {
		this.f1MappingWeighted = f1MappingWeighted;
	}

	public int[] getJaccardMappingWeighted() {
		return this.jaccardMappingWeighted;
	}

	public void setJaccardMappingWeighted(final int[] jaccardMappingWeighted) {
		this.jaccardMappingWeighted = jaccardMappingWeighted;
	}

	public Result(final List<Cluster> clusters, final long time) {
		this.clusters = clusters;
		this.runtime = time;

	}

	public List<Cluster> getClusters() {
		return this.clusters;
	}

	public void setClusters(final List<Cluster> clusters) {
		this.clusters = clusters;
	}

	public long getRuntime() {
		return this.runtime;
	}

	@Override
	public String toString() {
		return null;
	}

	public double getNaiveAccuracy() {
		return this.naiveAccuracy;
	}

	public void setNaiveAccuracy(final double relativeAccuracy) {
		this.naiveAccuracy = relativeAccuracy;
	}

	public double getF1() {
		return this.f1;
	}

	public void setF1(final double f1) {
		this.f1 = f1;
	}

	public void setAccordance(final boolean b) {
		this.accordance = b;
	}

	public boolean getAccordance() {
		return this.accordance;
	}

	public double getPrecision() {
		return this.precision;
	}

	public void setPrecision(final double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return this.recall;
	}

	public void setRecall(final double recall) {
		this.recall = recall;
	}

}
