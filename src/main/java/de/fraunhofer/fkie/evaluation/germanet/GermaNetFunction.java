package de.fraunhofer.fkie.aidpfm.germanet;

import java.io.Serializable;
import java.util.List;

import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.tuebingen.uni.sfs.germanet.api.ConRel;
import de.tuebingen.uni.sfs.germanet.api.Synset;

public abstract class GermaNetFunction implements Serializable{

	public abstract String replaceWithGermaNetFunction(String text, List<Requirement> requirements) throws Exception;
	
	protected Synset getBestParent(List<String> lemmatized, List<Synset> possibilities) {
		int maxContext = 0;
		Synset bestParent = null;
		for(Synset parent : possibilities){
			int context = 0;
			// get all his children
			List<Synset> children = parent.getRelatedSynsets(ConRel.has_hyponym);
			// and now go through all children synsets
			for(Synset child : children){
				// and through all their orth forms
				for(String form : child.getAllOrthForms()){
					if(lemmatized.contains(form)){
						context++;
					}
				}
			}
			if(context > maxContext && parent.getAllOrthForms().get(0).split(" ").length < 2){
				maxContext = context;
				bestParent = parent;
			}
		}
		return bestParent;
	}

	protected Synset getBestSynset(List<Synset> synsets, List<String> lemmatized) {
		Synset best = null;
		int maxContext = 0;

		for(Synset synset : synsets){
			int context = 0;
			for(String form : synset.getAllOrthForms()){
				// now count this form in the text
				for(String word : lemmatized){
					if(word.equals(form)){
						context++;
					}
				}
			}
			if(context > maxContext){
				maxContext = context;
				best = synset;
			}
		}

		return best;
	}
}
