package de.fraunhofer.fkie.evaluation.model;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.junit.Test;

public class RequirementVectorTest {

	@Test
	public void testContentKeys() {
		TreeSet<String> alphabet = new TreeSet<String>();
		alphabet.add("hallo");
		alphabet.add("guten");
		alphabet.add("morgen");
		alphabet.add("das");
		alphabet.add("ist");
		alphabet.add("ein");
		alphabet.add("test");
		List<String> doc = new ArrayList<>();
		doc.add("Hallo");
		doc.add("guten");
		doc.add("Moen");
		doc.add("as");
		doc.add("ist");
		doc.add("ein");
		doc.add("Test");
		RequirementVector.setWordOrder(null);
		RequirementVector.setWordOrderField(null);
		RequirementVector requirementVector = new RequirementVector(alphabet, doc, "sada", "sada");
		for (String check : doc) {
			if (requirementVector.getContentKeys().contains(check)) {
				assertTrue(check + " Should not be contained", alphabet.contains(check));
			} else {
				assertTrue(check + " Should be contained", !alphabet.contains(check));
			}
		}
	}

	@Test
	public void testContentValues() {
		TreeSet<String> alphabet = new TreeSet<String>();
		alphabet.add("hallo");
		alphabet.add("guten");
		alphabet.add("morgen");
		alphabet.add("das");
		alphabet.add("ist");
		alphabet.add("ein");
		alphabet.add("test");
		List<String> doc = new ArrayList<>();
		doc.add("hallo");
		doc.add("hallo");
		doc.add("hallo");
		doc.add("guten");
		doc.add("guten");
		doc.add("ist");
		doc.add("Test");
		RequirementVector.setWordOrder(null);
		RequirementVector.setWordOrderField(null);
		RequirementVector requirementVector = new RequirementVector(alphabet, doc, "sada", "sada");
		assertTrue(requirementVector.getContent("hallo") == 3 && requirementVector.getContent("guten") == 2
				&& requirementVector.getContent("ist") == 1);
	}

	// @Test
	// public void testCoLemmatizer() throws Exception {
	// String test = "Hallo, das ist ein neuer Test mit neuen Daten. Mal sehen
	// ob Flugzeug und Flugzeuge als ein Wort gepackt werden";
	// TreeSet<String> alphabet = new TreeSet<>();
	// List<String> content = NlpInterface.lemmatize(test);
	// List<String> doc = new ArrayList<>();
	// for(String word : content){
	// alphabet.add(Util.cleanWord(word));
	// doc.add(Util.cleanWord(word));
	// }
	//
	// RequirementVector requirementVector = new
	// RequirementVector(alphabet,doc,"sada","sada");
	//
	// assertTrue("das" + " Should not be contained",
	// requirementVector.getContentKeys().contains("das") == false);
	// assertTrue("," + " Should not be contained",
	// requirementVector.getContentKeys().contains(",") == false);
	// assertTrue("." + " Should not be contained",
	// requirementVector.getContentKeys().contains(".") == false);
	// assertTrue("sein" + " Should be contained",
	// requirementVector.getContent("sein") == 1);
	// assertTrue("flugzeug" + " Should be contained twice",
	// requirementVector.getContent("flugzeug") == 2);
	// }

}
