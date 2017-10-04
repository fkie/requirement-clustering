package de.fraunhofer.fkie.aidpfm.germanet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.utilities.NLProcessor;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.tuebingen.uni.sfs.germanet.api.ConRel;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.Synset;

public class GermaNetUniqueMapping extends GermaNetFunction {

	@Override
	public String replaceWithGermaNetFunction(final String text, final List<Requirement> requirements)
			throws Exception {
		StringBuilder returnText = new StringBuilder();
		Map<String, String> germaNetMap = this.germaNetContextMapping(requirements);
		for (String word : text.split(" ")) {
			if (germaNetMap.containsKey(word)) {
				word = germaNetMap.get(word);
			}
			returnText.append(word + " ");

		}
		return returnText.toString();
	}

	/**
	 * finds the best hypernyms of a word by their context (the rest of the
	 * requirement stock). This also includes determing the best meaning of this
	 * word in this context
	 *
	 * @param container
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> germaNetContextMapping(final List<Requirement> requirements) throws Exception {
		Map<String, String> germaNetMapping = new HashMap<>();
		List<Synset> synsets;
		NLProcessor nlProcessor = new NLProcessor();

		// IF THE DAY COMES, WHEN WE NEED DIRECTORY STREAMS, I AM PREPARED!!!
		// ClassLoader classLoader = getClass().getClassLoader();
		// String folder = "GN_V100_XML";
		// URI uri = classLoader.getResource(folder).toURI();
		// if(uri.getScheme().contains("jar")){
		// /** jar case */
		// try{
		// URL jar =
		// getClass().getProtectionDomain().getCodeSource().getLocation();
		// //jar.toString() begins with file:
		// Path jarFile = Paths.get(jar.toString().substring(6));
		// FileSystem fs = FileSystems.newFileSystem(jarFile, null);
		// DirectoryStream<Path> directoryStream =
		// Files.newDirectoryStream(fs.getPath(folder));
		// for(Path p: directoryStream){
		// InputStream is = getClass().getResourceAsStream(p.toString()) ;
		// System.out.println(p.getFileName());
		// }
		// }catch(IOException e) {
		// throw new Exception(e.getMessage());
		// }
		// }
		// else{
		// /** IDE case */
		// Path path = Paths.get(uri);
		// try {
		// DirectoryStream<Path> directoryStream =
		// Files.newDirectoryStream(path);
		// for(Path p : directoryStream){
		// InputStream is = new FileInputStream(p.toFile());
		// System.out.println(p.getFileName());
		// }
		// } catch (IOException _e) {
		// throw new Exception(_e.getMessage());
		// }
		// }

		GermaNet gnet = GermaNetSingleton.getInstance();
		// List because we need to count the instances of each word for the
		// context
		List<String> allWordsInRequirements = Util.splitToWords(requirements);
		StringBuilder toLemmatize = new StringBuilder();
		for (String word : allWordsInRequirements) {
			toLemmatize.append(" " + word);
		}
		List<String> lemmatized = nlProcessor.lemmatize(toLemmatize.toString());
		// now get the mapping for the words
		for (String word : lemmatized) {
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
					germaNetMapping.put(word, parent.getAllOrthForms().get(0));
				}
				// go through the children of parent to identify context
			}
		}

		return germaNetMapping;
	}

	@Override
	public String toString() {
		return "UniqueMapping";
	}

}
