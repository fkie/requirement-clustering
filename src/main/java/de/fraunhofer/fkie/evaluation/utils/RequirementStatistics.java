package de.fraunhofer.fkie.evaluation.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.fraunhofer.fkie.aidpfm.utilities.NLProcessor;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB.Query;
import de.fraunhofer.fkie.aidpfm.utilities.Util;

public class RequirementStatistics {

	public static void main(String[] args) throws Exception {
		// List<Field> fields= new ArrayList<>();
		// fields.add(Field.AKTIVITÄT_3);
		// fields.add(Field.OBJECTUNDERGÄNZUNG_4);
		// fields.add(Field.PROZESSWORT_5);
		// interpretedWordOccurencesSpecificFields(fields);
		sameMapping();
	}

	public static void lemmatizedWordCount() throws Exception {
		List<Requirement> list = R7DB.getRequirements(Query.ALL, Config.ALPHA_DB);
		Map<String, Integer> wordsAndCounts = new HashMap<>();

		for(Requirement req : list){
			String text = "";
			// collect to one text (saves time, since less server calls)
			for(Field field : Field.values()){
				if(field == Field.ID || field == Field.TEXT || field == Field.TITEL || field == Field.OBJ_ID){
					continue;
				}
				text += req.get(field) + " ";
			}
			NLProcessor processor = new NLProcessor();
			List<String> words = processor.lemmatize(text);
			// fill count
			for(String word : words){
				if(wordsAndCounts.containsKey(word)){
					int value = wordsAndCounts.get(word);
					wordsAndCounts.put(word, value + 1);
				}
				else{
					wordsAndCounts.put(word, 1);
				}
			}
		}
		// write csv
		String csvContent = "";
		String header = "Word,Count \n";
		csvContent += header;
		for(String key : wordsAndCounts.keySet()){
			String row = key + "," + wordsAndCounts.get(key) + "\n";
			csvContent += row;
		}

		Util.writeUtf8File("WordCounts.csv", csvContent);
	}

	public static void interpretedWordCount() throws Exception {
		List<Requirement> list = R7DB.getRequirements(Query.ALL, Config.ALPHA_DB);
		Map<String, Integer> wordsAndCounts = new HashMap<>();

		for(Requirement req : list){
			String text = "";
			// collect to one text (saves time, since less server calls)
			for(Field field : Field.values()){
				if(field == Field.ID || field == Field.TEXT || field == Field.TITEL || field == Field.OBJ_ID){
					continue;
				}
				// get single field
				String fieldContent = req.get(field);
				List<String> content = Arrays.asList(fieldContent.split(" "));
				// and fill it
				for(String word : content){
					word = Util.cleanWord(word);
					String wordWithField = word + "_" + field;
					if(wordsAndCounts.containsKey(wordWithField)){
						int value = wordsAndCounts.get(wordWithField);
						wordsAndCounts.put(wordWithField, value + 1);
					}
					else{
						wordsAndCounts.put(wordWithField, 1);
					}
				}
			}
		}
		// write csv
		String csvContent = "";
		String header = "Word,Count \n";
		csvContent += header;
		for(String key : wordsAndCounts.keySet()){
			String row = key + "," + wordsAndCounts.get(key) + "\n";
			csvContent += row;
		}

		Util.writeUtf8File("InterpretedWordCounts.csv", csvContent);
	}

	public static void interpretedWordOccurencesSpecificFields(List<Field> fields) throws Exception {
		List<Requirement> list = R7DB.getRequirements(Query.ALL, Config.ALPHA_DB);
		// TODO delete here when you fix it in R7DB
		list.remove(0);
		Map<String, Integer> wordsAndCounts = new HashMap<>();
		String csvContent = "";
		String header = "Word,Count,Occurence \n";
		csvContent += header;

		for(Requirement req : list){
			String text = "";
			for(Field field : Field.values()){
				if(field == Field.ID || field == Field.TEXT || field == Field.TITEL || field == Field.OBJ_ID){
					continue;
				}
				text += req.get(field) + " ";
			}
			for(Field field : fields){
				// get single field
				String fieldContent = req.get(field);
				List<String> content = Arrays.asList(fieldContent.split(" "));
				// and fill it
				for(String word : content){
					word = Util.cleanWord(word);
					if(!word.equals("")){
						// String wordWithField = word + "_" + field;
						String row = word + ",1," + text + "\n";
						csvContent += row;
					}
				}
			}
		}

		Util.writeUtf8File("InterpretedWordCounts.csv", csvContent);
	}

	public static void lemmatizedWordOccurences() throws Exception {
		List<Requirement> list = R7DB.getRequirements(Query.ALL, Config.ALPHA_DB);
		String csvContent = "";
		String header = "Word,Count \n";
		csvContent += header;
		for(Requirement req : list){
			String text = "";
			// collect to one text (saves time, since less server calls)
			for(Field field : Field.values()){
				if(field == Field.ID || field == Field.TEXT || field == Field.TITEL || field == Field.OBJ_ID){
					continue;
				}
				text += req.get(field) + " ";
			}
			NLProcessor processor = new NLProcessor();
			List<String> words = processor.lemmatize(text);
			// fill csv
			for(String word : words){
				String row = word + ",1," + text + "\n";
				csvContent += row;
			}
		}
		// write csv
		Util.writeUtf8File("WordOccurences.csv", csvContent);
	}

	public static void sourceTextImpact() throws Exception {
		List<Requirement> list = R7DB.getRequirements(Query.ALL, Config.ALPHA_DB);
		// TODO: change if R7DB is changed
		list.remove(0);
		Map<String, Integer> sourceLengthMap = new HashMap<>();
		Map<String, List<Requirement>> sourceRequirementsMap = new HashMap<>();

		for(Requirement requirement : list){
			String source = requirement.get(Field.QUELLE_8);
			// do some cleaning
			source = source.replaceAll("\"", "");
			source = source.replaceAll("\\n", " ");
			if(sourceLengthMap.containsKey(source)){
				sourceRequirementsMap.get(source).add(requirement);
			}
			else{
				sourceLengthMap.put(source, source.split(" ").length);
				List<Requirement> sourceList = new ArrayList<>();
				sourceList.add(requirement);
				sourceRequirementsMap.put(source, sourceList);
			}
		}

		String csvContent = "";
		String head = "Source, Source Length, Number of Contained Requirements, Average Requirement Length, Impact";
		csvContent += head + "\n";

		for(String key : sourceLengthMap.keySet()){
			String sourceInformation = "";
			sourceInformation += key.split(" ")[0] + ",";
			int sourceLength = sourceLengthMap.get(key);
			sourceInformation += sourceLength + ",";
			sourceInformation += sourceRequirementsMap.get(key).size() + ",";
			double avgReqLength = 0;
			double impact = 0;
			for(Requirement requirement : sourceRequirementsMap.get(key)){
				double reqLength = requirement.get(Field.TEXT).split(" ").length;

				avgReqLength += reqLength;
				double wholeLength = reqLength + sourceLength;
				impact += (sourceLength / wholeLength);
			}
			avgReqLength = avgReqLength / sourceRequirementsMap.get(key).size();
			impact = impact / sourceRequirementsMap.get(key).size();
			sourceInformation += avgReqLength + ",";
			sourceInformation += impact + ",";
			// csvContent += key;
			csvContent += sourceInformation + "\n";
		}

		Util.writeUtf8File(Config.AIDPFM_VSNFD + "/sourceInformation.csv", csvContent);
	}

	public static class Word {
		String word;
		Set<Field> fields;
		int overallOcc;
		int field1, field2, field3, field4, field5, field6, field7;

		public Word(String word) {
			this.word = word;
			field1 = 0;
			field2 = 0;
			field3 = 0;
			field4 = 0;
			field5 = 0;
			field6 = 0;
			field7 = 0;
			overallOcc = 0;
			fields = new HashSet<>();
		}

		public void increaseField(Field field) throws Exception {
			switch(field){
				case SUBJEKT_1:
					field1++;
					break;
				case VERBINDLICHKEIT_2:
					field2++;
					break;
				case AKTIVITÄT_3:
					field3++;
					break;
				case OBJECTUNDERGÄNZUNG_4:
					field4++;
					break;
				case PROZESSWORT_5:
					field5++;
					break;
				case QUALITÄT_6:
					field6++;
					break;
				case BEDINGUNG_7:
					field7++;
					break;
				default:
					throw new Exception("Not a valid DistanceFunction");
			}

		}
	}

	public static void wordsInWhichFields() throws Exception {
		List<Requirement> list = R7DB.getRequirements(Query.ALL, Config.ALPHA_DB);
		// TODO: change if R7DB is changed
		list.remove(0);
		String csvContent = "";
		String head = "Word, Occurences, Number of Fields," + Field.SUBJEKT_1 + ","
				+ Field.VERBINDLICHKEIT_2 + "," + Field.AKTIVITÄT_3 + ","
				+ Field.OBJECTUNDERGÄNZUNG_4 + "," + Field.PROZESSWORT_5 + "," + Field.QUALITÄT_6 + ","
				+ Field.BEDINGUNG_7;
		csvContent += head + "\n";

		List<Field> fields = new ArrayList<>();
		fields.add(Field.SUBJEKT_1);
		fields.add(Field.VERBINDLICHKEIT_2);
		fields.add(Field.AKTIVITÄT_3);
		fields.add(Field.OBJECTUNDERGÄNZUNG_4);
		fields.add(Field.PROZESSWORT_5);
		fields.add(Field.QUALITÄT_6);
		fields.add(Field.BEDINGUNG_7);

		Map<String, Word> wordsMap = new HashMap<>();
		// for fast check if word already exists
		List<String> wordsCheck = new ArrayList<>();
		for(Requirement requirement : list){
			for(Field field : fields){
				String fieldWords = requirement.get(field);
				// do some cleaning
				String[] wordsArray = fieldWords.split(" ");
				for(String word : wordsArray){
					word = Util.cleanWord(word);
					if(word.equals("")){
						continue;
					}
					if(wordsCheck.contains(word)){
						Word thisWord = wordsMap.get(word);
						thisWord.overallOcc++;
						thisWord.fields.add(field);
						thisWord.increaseField(field);
					}
					else{
						Word thisWord = new Word(word);
						thisWord.overallOcc++;
						thisWord.fields.add(field);
						thisWord.increaseField(field);
						wordsCheck.add(word);
						wordsMap.put(word, thisWord);
					}
				}
			}
		}

		for(String key : wordsMap.keySet()){
			Word word = wordsMap.get(key);
			String wordInformation = word.word + "," + word.overallOcc + "," + word.fields.size() + "," + word.field1
					+ "," + word.field2 + "," + word.field3 + "," + word.field4 + "," + word.field5 + "," + word.field6
					+ "," + word.field7;
			csvContent += wordInformation + "\n";
		}
		Util.writeUtf8File(Config.AIDPFM_VSNFD + "/wordInformation.csv", csvContent);
	}

	public static void wordMeanings() throws Exception {
		List<Requirement> list = R7DB.getRequirements(Query.ALL, Config.ALPHA_DB);
		// TODO: change if R7DB is changed
		list.remove(0);
		String csvContent = "";

		// because i am curious
		// String head = "Word, Number of Meanings \n";
		// csvContent += head;
		// Map<String,Integer> wordsMeanings = Util.thesaurusMapDouble();
		// int i = 0;
		// for(String key : wordsMeanings.keySet()){
		// if(wordsMeanings.get(key) < 2){
		// continue;
		// }
		// String line = key + "," + wordsMeanings.get(key) + "\n";
		// csvContent += line;
		// System.out.println(i);
		// i++;
		// }
		// Util.writeUtf8File(Config.AIDPFM_VSNFD+"/allDoubleWordMeanings.csv",
		// csvContent);
		// *******************************************************************************

		String head = "Word, Number of Meanings \n";
		csvContent += head;
		Map<String, Integer> wordsMeanings = Util.thesaurusMapDouble();
		List<Field> fields = new ArrayList<>();
		fields.add(Field.SUBJEKT_1);
		fields.add(Field.VERBINDLICHKEIT_2);
		fields.add(Field.AKTIVITÄT_3);
		fields.add(Field.OBJECTUNDERGÄNZUNG_4);
		fields.add(Field.PROZESSWORT_5);
		fields.add(Field.QUALITÄT_6);
		fields.add(Field.BEDINGUNG_7);
		StringBuilder allRequirements = new StringBuilder();

		for(Requirement requirement : list){
			for(Field field : fields){
				if(field == Field.ID || field == Field.TEXT || field == Field.TITEL || field == Field.OBJ_ID){
					throw new RuntimeException(
							"Entered the not allowable fields. I notify you and crash everything, because Daniel wanted me to");
				}
				String fieldContent = requirement.get(field);
				String cleanedContent = clean(fieldContent);
				// add up to a string containing all words
				allRequirements.append(cleanedContent);
			}
			// seperate requirements by this notation
			allRequirements.append("__REQUIREMENT__ ");
		}

		String text = allRequirements.toString();
		// some cleaning
		String toLemmatize = text.replaceAll("  ", " ");
		NLProcessor processor = new NLProcessor();
		List<String> content = processor.lemmatize(toLemmatize);

		// this loop is needed, so that each word only appears once in the csv
		Map<String, Integer> requirementsWithMeaning = new HashMap<>();
		for(String word : content){
			if(wordsMeanings.containsKey(word)){
				requirementsWithMeaning.put(word, wordsMeanings.get(word));
			}
			else{
				requirementsWithMeaning.put(word, 0);
			}
		}

		for(String key : requirementsWithMeaning.keySet()){
			String line = key + "," + requirementsWithMeaning.get(key) + "\n";
			csvContent += line;
		}

		Util.writeUtf8File(Config.AIDPFM_VSNFD + "/requirementWordsNumberOfMeanings.csv", csvContent);
	}

	public static void sameMapping() throws Exception {
		List<Requirement> list = R7DB.getRequirements(Query.ALL, Config.ALPHA_DB);
		// TODO: change if R7DB is changed
		list.remove(0);
		String csvContent = "";

		String head = "Word, Mapped to it \n";
		csvContent += head;
		NLProcessor processor = new NLProcessor();
		Map<String, String> thesaurusMap = processor.thesaurusMap();
		List<Field> fields = new ArrayList<>();
		fields.add(Field.SUBJEKT_1);
		fields.add(Field.VERBINDLICHKEIT_2);
		fields.add(Field.AKTIVITÄT_3);
		fields.add(Field.OBJECTUNDERGÄNZUNG_4);
		fields.add(Field.PROZESSWORT_5);
		fields.add(Field.QUALITÄT_6);
		fields.add(Field.BEDINGUNG_7);
		StringBuilder allRequirements = new StringBuilder();

		for(Requirement requirement : list){
			for(Field field : fields){
				if(field == Field.ID || field == Field.TEXT || field == Field.TITEL || field == Field.OBJ_ID){
					throw new RuntimeException(
							"Entered the not allowable fields. I notify you and crash everything, because Daniel wanted me to");
				}
				String fieldContent = requirement.get(field);
				String cleanedContent = clean(fieldContent);
				// add up to a string containing all words
				allRequirements.append(cleanedContent);
			}
			// seperate requirements by this notation
			allRequirements.append("__REQUIREMENT__ ");
		}

		String text = allRequirements.toString();
		// some cleaning
		String toLemmatize = text.replaceAll("  ", " ");
		List<String> content = processor.lemmatize(toLemmatize);

		// fill map with the word and the words mapped to it
		Map<String, Set<String>> wordsAndMappings = new HashMap<>();
		for(String word : content){
			if(thesaurusMap.containsKey(word)){
				String mapping = thesaurusMap.get(word);
				if(wordsAndMappings.containsKey(mapping)){
					wordsAndMappings.get(mapping).add(word);
				}
				else{
					Set<String> set = new HashSet<>();
					set.add(word);
					wordsAndMappings.put(mapping, set);
				}
			}
		}
		// output
		for(String key : wordsAndMappings.keySet()){
			if(wordsAndMappings.get(key).size() > 1){
				String line = "";
				line += key + ",";
				Set<String> mappedTo = wordsAndMappings.get(key);
				for(String mapped : mappedTo){
					line += mapped + ";";
				}
				line += "\n";
				csvContent += line;
			}
		}
		Util.writeUtf8File(Config.AIDPFM_VSNFD + "/WordsAndMappings.csv", csvContent);
	}

	private static String clean(String fieldContent) {
		String[] words = fieldContent.split(" ");
		for(int i = 0; i < words.length; i++){
			String cleanedWord = Util.cleanWord(words[i]);
			words[i] = cleanedWord;
		}
		String text = "";
		for(String word : words){
			text += word + " ";
		}
		text.trim();
		return text;
	}
}
