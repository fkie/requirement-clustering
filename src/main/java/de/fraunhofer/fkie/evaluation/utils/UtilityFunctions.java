package de.fraunhofer.fkie.evaluation.utils;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.model.EvaluationConfig;
import de.fraunhofer.fkie.evaluation.model.MetaResult;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;

public class UtilityFunctions {
	public static void lock(List<RequirementVector> vectors) {
		for(RequirementVector v : vectors){
			v.lock();
		}
		
	}

	public static EvaluationConfig extractSettings(MetaResult meta) {
		EvaluationConfig settings = new EvaluationConfig();
		settings.setClusterer(meta.getClusterer());
		settings.setParameter(meta.getParameter());
		List<Field> fields = new ArrayList<>();
		if(meta.getFields().split(",").length == 1){
			fields.add(Field.OBJECTUNDERGÄNZUNG_4);
		}
		else if(meta.getFields().split(",").length == 4){
			fields.add(Field.SUBJEKT_1);
			fields.add(Field.OBJECTUNDERGÄNZUNG_4);
			fields.add(Field.PROZESSWORT_5);
			fields.add(Field.BEDINGUNG_7);
		}
		else{
			fields.addAll(Util.getRuppFields());
		}
		settings.setFields(fields);
		settings.setDistance(meta.getDistance());
		settings.setTfidf(meta.isTfidf());
		settings.setStopWords(meta.isStopWords());
		settings.setRuppInterpretation(meta.isRuppInterpretation());
		settings.setLemmatized(meta.isLemmatized());
		settings.setOnthology(meta.isOnthology());
		settings.setSource(meta.isSource());
		settings.setSynonyms(meta.isSynonyms());
		settings.setGermaNetFunction(meta.getGermaNetFunction());
		
		return settings;
	}
}
