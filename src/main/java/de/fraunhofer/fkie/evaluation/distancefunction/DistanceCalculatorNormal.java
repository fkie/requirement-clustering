package de.fraunhofer.fkie.evaluation.distancefunction;

import de.fraunhofer.fkie.evaluation.model.RequirementVector;

public class DistanceCalculatorNormal extends DistanceHandler {

	// @Override
	// public double distance(final RequirementVector v1, final
	// RequirementVector v2) {
	// return this.distanceFunction.distance(v1.getContentMap(),
	// v2.getContentMap());
	// }

	@Override
	public double distanceZ(final RequirementVector v1, final RequirementVector v2) {
		return this.distanceFunction.distanceZ(v1.getVectorRepresentation(), v2.getVectorRepresentation());
	}

	@Override
	public String toString() {
		return "WithoutRupp";
	}

}
