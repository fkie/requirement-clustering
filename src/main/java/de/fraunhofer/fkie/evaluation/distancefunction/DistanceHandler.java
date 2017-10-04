package de.fraunhofer.fkie.evaluation.distancefunction;

import java.io.Serializable;

import de.fraunhofer.fkie.evaluation.model.RequirementVector;

public abstract class DistanceHandler implements Serializable {
	@SuppressWarnings("unused")
	protected DistanceFunction distanceFunction;

	// public abstract double distance(RequirementVector v1, RequirementVector
	// v2);

	public abstract double distanceZ(RequirementVector v1, RequirementVector v2);

	public void setDistanceFunction(final DistanceFunction distanceFunction) {
		this.distanceFunction = distanceFunction;
	}

	@Override
	public abstract String toString();
}
