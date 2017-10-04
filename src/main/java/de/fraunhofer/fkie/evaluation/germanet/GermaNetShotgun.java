package de.fraunhofer.fkie.aidpfm.germanet;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.tools.NLPStatistics.ShotgunEvaluation;
import de.tuebingen.uni.sfs.germanet.api.ConRel;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.Synset;

public class GermaNetShotgun extends GermaNetFunction {

	@Override
	public String replaceWithGermaNetFunction(final String text, final List<Requirement> requirements)
			throws Exception {
		String[] wordArray = text.split(" ");
		GermaNet gnet = GermaNetSingleton.getInstance();
		for (int i = 0; i < wordArray.length; i++) {
			List<String> shotgunCone = this.germaNetGetAllParentsRecursive(wordArray[i], gnet, new ArrayList<String>());
			String replace = "";
			for (String word : shotgunCone) {
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

	public List<String> germaNetGetAllParentsRecursive(final String word, final GermaNet gnet,
			final List<String> alreadyKnown) throws Exception {
		List<String> words = new ArrayList<>();
		alreadyKnown.add(word);
		List<Synset> synsets;
		synsets = gnet.getSynsets(word);
		if (synsets.size() > 0) {
			for (Synset synset : synsets) {
				List<Synset> parents = synset.getRelatedSynsets(ConRel.has_hypernym);
				if (parents.size() == 0) {
				}
				for (Synset parent : parents) {
					String parentWord = parent.getAllOrthForms().get(0);
					if (!alreadyKnown.contains(parentWord)) {
						words.add(parentWord);
						words.addAll(this.germaNetGetAllParentsRecursive(parentWord, gnet, alreadyKnown));
					}
				}

			}
		} else {
			words.add(word);
		}

		return words;
	}

	public List<String> germaNetGetAllParentsRecursive(final String word, final GermaNet gnet,
			final List<String> alreadyKnown, final ShotgunEvaluation shotgunEvaluation, final int depth)
			throws Exception {
		List<String> words = new ArrayList<>();
		alreadyKnown.add(word);
		List<Synset> synsets;
		synsets = gnet.getSynsets(word);
		if (synsets.size() > shotgunEvaluation.maxSynsets) {
			shotgunEvaluation.maxSynsets = synsets.size();
		}
		shotgunEvaluation.averageSynsets.add(synsets.size());
		if (synsets.size() > 0) {
			for (Synset synset : synsets) {
				List<Synset> parents = synset.getRelatedSynsets(ConRel.has_hypernym);
				if (parents.size() > shotgunEvaluation.maxParents) {
					shotgunEvaluation.maxParents = parents.size();
				}
				shotgunEvaluation.averageParents.add(parents.size());
				if (parents.size() == 0) {
					if (depth > shotgunEvaluation.maxDepth) {
						shotgunEvaluation.maxDepth = depth;
					}
					shotgunEvaluation.averageDepth.add(depth);
				}
				for (Synset parent : parents) {
					String parentWord = parent.getAllOrthForms().get(0);
					if (!alreadyKnown.contains(parentWord)) {
						words.add(parentWord);
						words.addAll(this.germaNetGetAllParentsRecursive(parentWord, gnet, alreadyKnown,
								shotgunEvaluation, depth + 1));
					}
				}

			}
		}

		return words;
	}

	@Override
	public String toString() {
		return "Shotgun";
	}
}
