package de.fraunhofer.fkie.evaluation.model;

import java.util.HashMap;
import java.util.Map;

public class ARTNeuron {
	
	private RequirementVector representative;
	private Map<String,Double> weights;
	
	public ARTNeuron(RequirementVector input){
		representative = input;
		weights = new HashMap<>();
		for(String key : input.getContentKeys()){
			double size = input.sizeOfContent();
			weights.put(key, 1.0/(1.0+size));
		}
	}
	public RequirementVector getRepresentative() {
		return representative;
	}
	public void setRepresentative(RequirementVector representative) {
		this.representative = representative;
	}
	public Map<String, Double> getWeights() {
		return weights;
	}
	public void setWeights(Map<String, Double> weights) {
		this.weights = weights;
	}
}
