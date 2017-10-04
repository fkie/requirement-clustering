package de.fraunhofer.fkie.evaluation.model;

import java.io.Serializable;
public class ClusterEntry implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String name;
	private int clusterID;
	private double probability;
	
	public ClusterEntry(String id, String name, int clusterID, double probability) {
		super();
		this.id = id;
		this.name = name;
		this.clusterID = clusterID;
		this.probability = probability;
	}
	
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getClusterID() {
		return clusterID;
	}
	public double getProbability() {
		return probability;
	}
	
	
}
