package de.fraunhofer.fkie.aidpfm.germanet;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.utilities.NLProcessor;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.tuebingen.uni.sfs.germanet.api.ConRel;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.Synset;

public class GermaNetBestParentPath extends GermaNetFunction {
	@Override
	public String replaceWithGermaNetFunction(final String text, final List<Requirement> requirements)
			throws Exception {
		List<String> allWordsInRequirements = Util.splitToWords(requirements);
		StringBuilder toLemmatize = new StringBuilder();
		for (String word : allWordsInRequirements) {
			toLemmatize.append(" " + word);
		}
		NLProcessor nlProcessor = new NLProcessor();
		List<String> lemmatized = nlProcessor.lemmatize(toLemmatize.toString());
		GermaNet gnet = GermaNetSingleton.getInstance();
		String[] wordArray = text.split(" ");
		for (int i = 0; i < wordArray.length; i++) {
			List<String> replaces = this.germaNetContextMappingRecursive(wordArray[i], new ArrayList<String>(), gnet,
					lemmatized);
			String replace = "";
			for (String word : replaces) {
				replace += word + " ";
			}
			wordArray[i] = replace;
		}
		StringBuilder toReturn = new StringBuilder();
		for (String block : wordArray) {
			toReturn.append(block);
		}
		return toReturn.toString();
	}

	public List<String> germaNetContextMappingRecursive(final String word, final List<String> partOfMapping,
			final GermaNet gnet, final List<String> lemmatized) throws Exception {
		List<String> path = new ArrayList<>();
		List<Synset> synsets;
		partOfMapping.add(word);
		path.add(word);

		// now get the mapping for the words
		synsets = gnet.getSynsets(word);
		if (synsets.size() != 0) {
			// which one appears more often in text
			Synset match = this.getBestSynset(synsets, lemmatized);
			// the parent
			if (match != null) {

				List<Synset> possibilities = match.getRelatedSynsets(ConRel.has_hypernym);
				Synset parent;
				if (possibilities.size() > 1) {
					parent = this.getBestParent(lemmatized, possibilities);
				} else {
					parent = possibilities.get(0);
				}
				// put the first form of parent in there
				if (parent != null) {
					// check whether one of the orth forms is contained in text
					boolean found = false;
					for (String form : parent.getAllOrthForms()) {
						// found a circle, break
						if (partOfMapping.contains(form) || form.equals("GNROOT")) {
							break;
						}
						// found as part of our text, so go further for mapping
						if (lemmatized.contains(form)) {
							found = true;
							path.addAll(this.germaNetContextMappingRecursive(form, partOfMapping, gnet, lemmatized));
							break;
						} else {
							path.add(form);
						}
					}
				}
			}
		}
		return path;
	}

	@Override
	public String toString() {
		return "OnePath";
	}
}
