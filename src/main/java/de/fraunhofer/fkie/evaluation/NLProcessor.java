package de.fraunhofer.fkie.aidpfm.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.tools.NLPStatistics.ShotgunEvaluation;
import de.fraunhofer.fkie.nlytics.NlpService;
import de.tuebingen.uni.sfs.germanet.api.ConRel;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.Synset;

public class NLProcessor {

	public List<String> lemmatize(String toFilter) throws Exception {
		String result = NlpService.getTokenize(toFilter);
		if(result.contains("all")){
			String[] resultContent = result.split("\"all\":");
			JSONArray data = new JSONArray(resultContent[1]);
			List<String> lemmas = new ArrayList<>();
			for(int i = 0; i < data.length(); i++){
				String value = ((JSONObject) data.get(i)).get("lemma").toString();
				// get rid of none words
				String toAdd = Util.cleanWord(value);
				if(!toAdd.equals("")){
					lemmas.add(toAdd);
				}
			}
			return lemmas;
		}

		return new ArrayList<String>();
	}

	public List<String> filterStopWords(String toFilter) {
		String result = NlpService.deleteStopwords(toFilter);
		if(result.contains("all")){
			String[] resultContent = result.split("\"all\":");
			JSONArray data = new JSONArray(resultContent[1]);
			List<String> filtered = new ArrayList<>();
			for(int i = 0; i < data.length(); i++){
				String value = ((JSONObject) data.get(i)).get("text").toString();
				filtered.add(value);
			}
			return filtered;
		}

		return new ArrayList<String>();
	}

	/**
	 * simple naive mapping. Everything will be mapped to its last occurence
	 * (pretty much random)
	 * 
	 * @return
	 * @throws IOException
	 */
	public Map<String, String> thesaurusMap() throws IOException {
		Map<String, String> thesaurusMapping = new HashMap<>();
		String resourceName = "openthesaurus.txt";
		// ClassLoader loader = Thread.currentThread().getContextClassLoader();
		BufferedReader br;
		try(InputStreamReader resourceStream = new InputStreamReader(
				this.getClass().getResourceAsStream("/" + resourceName))){
			br = new BufferedReader(resourceStream);

			String line = br.readLine();
			while(line != null){
				String[] words = line.split(";");
				// cut out the bracket explanations
				for(int i = 0; i < words.length; i++){
					if(words[i].contains("(")){
						words[i] = words[i].replaceAll("\\(.*?\\)", "");
						words[i] = words[i].trim();
					}
					thesaurusMapping.put(words[i], words[0]);
				}

				line = br.readLine();
			}
		}
		return thesaurusMapping;
	}

	/**
	 * more complex mapping. Tries to find the best mapping by finding out which
	 * mapping contains the most words also appearing in the requirement stock
	 * 
	 * @param container
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> thesaurusContextMap(List<Requirement> requirements) throws Exception {
		String resourceName = "openthesaurus.txt";
		Map<String, String> thesaurusMapping = new HashMap<>();
		// start with mapping all words in thesaurus to all their possibilities
		Map<String, List<String>> wordsToPossibilites = new HashMap<>();
		// and fill the lines of the possibilites for later look up
		Map<String, List<String>> firstToLine = new HashMap<>();
		BufferedReader br;
		try(InputStreamReader resourceStream = new InputStreamReader(
				this.getClass().getResourceAsStream("/" + resourceName))){
			br = new BufferedReader(resourceStream);

			String line = br.readLine();
			while(line != null){
				String[] words = line.split(";");
				// cut out the bracket explanations
				List<String> listForWord0 = new ArrayList<>();
				// ensure that words that appear in position 0 more often have
				// each their own line
				words[0] = words[0].replaceAll("\\(.*?\\)", "");
				words[0] = words[0].trim();
				String iteratedWord = words[0];
				int number = 0;
				while(firstToLine.containsKey(iteratedWord)){
					number++;
					iteratedWord = words[0] + number;
				}
				firstToLine.put(iteratedWord, listForWord0);
				for(int i = 0; i < words.length; i++){
					if(words[i].contains("(")){
						words[i] = words[i].replaceAll("\\(.*?\\)", "");
						words[i] = words[i].trim();
					}
					if(wordsToPossibilites.containsKey(words[i])){
						// for word[i] add word[0] as a possible synonym
						List<String> possibilities = wordsToPossibilites.get(words[i]);
						possibilities.add(iteratedWord);
					}
					else{
						List<String> possibilities = new ArrayList<>();
						possibilities.add(iteratedWord);
						wordsToPossibilites.put(words[i], possibilities);
					}
					listForWord0.add(words[i]);
				}

				line = br.readLine();
			}
		}
		// finished reading and saving the open thesaurus db
		// now lets get the context
		Set<String> allWordsInRequirements = Util.splitToWordsSet(requirements);
		StringBuilder toLemmatize = new StringBuilder();
		for(String word : allWordsInRequirements){
			toLemmatize.append(" " + word);
		}
		List<String> lemmatized = lemmatize(toLemmatize.toString());
		// now get the mapping for the words
		for(String word : lemmatized){
			if(wordsToPossibilites.containsKey(word)){
				List<String> possibilities = wordsToPossibilites.get(word);
				int maxContext = 0;
				String currentMapping = "";
				// go through all possibilities
				for(String possibility : possibilities){
					// look through the line of this possibiliy
					List<String> line = firstToLine.get(possibility);
					int context = 0;
					for(String lineWord : line){
						// check if this is contained in our requirement set
						if(lemmatized.contains(lineWord)){
							context++;
						}
					}

					if(context > maxContext && possibility.split(" ").length < 2){
						currentMapping = possibility;
						maxContext = context;
					}
				}
				if(!word.equals(currentMapping) && !currentMapping.equals("")){
					thesaurusMapping.put(word, currentMapping);
				}
			}
		}

		return thesaurusMapping;
	}	

	public String replaceWithSynonyms(String text, List<Requirement> requirements) throws Exception {
		StringBuilder returnText = new StringBuilder();
		Map<String, String> thesaurusMap = thesaurusContextMap(requirements);
		for(String word : text.split(" ")){
			if(thesaurusMap.containsKey(word)){
				word = thesaurusMap.get(word);
			}
			returnText.append(word + " ");

		}
		return returnText.toString();
	}

}
