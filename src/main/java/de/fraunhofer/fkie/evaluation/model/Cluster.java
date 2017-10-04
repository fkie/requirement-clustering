package de.fraunhofer.fkie.evaluation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fkie.aidpfm.model.Requirement;
public class Cluster implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public List<Requirement> requirements;
	String test = "test";
//	private transient ViewARV01 viewDelegate;
	public Cluster(List<Requirement> requirements) {
		super();
		this.requirements = requirements;
//		try {
//			viewDelegate = Main.model.createARV01();
//		} catch (ModelException e) {
//			// TODO Auto-generated catch block
//			throw new RuntimeException(e);
//		}
////		for(Requirement requirement : requirements){
//			viewDelegate.addRequirement(requirement.delegate);
//		}
	}
	public Cluster() {
		super();
		this.requirements = new ArrayList<>();
	}
	
	public List<Requirement> getRequirements(){
		return requirements;
	}
	
	public String getText(){
		return test;
	}
}
