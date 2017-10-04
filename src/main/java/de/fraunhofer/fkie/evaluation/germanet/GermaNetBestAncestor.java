package de.fraunhofer.fkie.aidpfm.germanet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.utilities.NLProcessor;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.tuebingen.uni.sfs.germanet.api.ConRel;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.Synset;

public class GermaNetBestAncestor extends GermaNetFunction {

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
		Set<String> reduced = new HashSet<>(lemmatized);
		GermaNet gnet = GermaNetSingleton.getInstance();
		Map<String, String> mapping = new HashMap<String, String>();
		for (String word : reduced) {
			if (!mapping.containsKey(word)) {
				mapping.putAll(this.germaNetContextMappingRecursive(word, new ArrayList<String>(), gnet, lemmatized));
			}
		}
		StringBuilder returnText = new StringBuilder();
		for (String word : text.split(" ")) {
			if (mapping.containsKey(word)) {
				word = mapping.get(word);
			}
			returnText.append(word + " ");

		}
		return returnText.toString();
	}

	public Map<String, String> germaNetContextMappingRecursive(final String word, final List<String> partOfMapping,
			final GermaNet gnet, final List<String> lemmatized) throws Exception {
		Map<String, String> germaNetMapping = new HashMap<>();
		List<Synset> synsets;
		partOfMapping.add(word);

		// now get the mapping for the words
		synsets = gnet.getSynsets(word);
		if (synsets.size() != 0) {
			// which one appears more often in text
			Synset match = this.getBestSynset(synsets, lemmatized);
			// the parent
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
						germaNetMapping = this.germaNetContextMappingRecursive(form, partOfMapping, gnet, lemmatized);
						break;
					}
				}
				if (!found) {
					// all map to the current parent
					String mapToThis = parent.getAllOrthForms().get(0);
					if (!mapToThis.equals("GNROOT")) {
						for (String map : partOfMapping) {
							germaNetMapping.put(map, mapToThis);
						}
					}
				}
			}
		}

		return germaNetMapping;
	}

	@Override
	public String toString() {
		return "OneAncestor";
	}
}
