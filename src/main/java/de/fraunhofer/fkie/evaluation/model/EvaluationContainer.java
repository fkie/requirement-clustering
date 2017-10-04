package de.fraunhofer.fkie.evaluation.model;

import java.util.List;

import de.fraunhofer.fkie.aidpfm.utilities.ModelContainer;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceHandler;
import weka.core.Instances;

public class EvaluationContainer {

	private EvaluationConfig settings;
	private List<RequirementVector> vectors;
	private Instances data;
	private DistanceHandler distanceHandler;
	private Result humanReference;
	private ModelContainer container;

	public EvaluationContainer(EvaluationConfig settings, List<RequirementVector> vectors, Instances data,
			DistanceHandler distanceHandler, Result humanReference, ModelContainer container) {
		super();
		this.settings = settings;
		this.vectors = vectors;
		this.data = data;
		this.distanceHandler = distanceHandler;
		this.humanReference = humanReference;
		this.container = container;
	}

	public EvaluationConfig getSettings() {
		return settings;
	}

	public List<RequirementVector> getVectors() {
		return vectors;
	}

	public Instances getData() {
		return data;
	}

	public DistanceHandler getDistanceHandler() {
		return distanceHandler;
	}

	public Result getHumanReference() {
		return humanReference;
	}

	public ModelContainer getContainer() {
		return container;
	}

}
