package de.fraunhofer.fkie.evaluation;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;
import org.sqlite.SQLiteConfig;

import de.fraunhofer.fkie.aidpfm.model.Node;
import de.fraunhofer.fkie.aidpfm.model.Requirement;
import de.fraunhofer.fkie.aidpfm.model.Requirement.Field;
import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB;
import de.fraunhofer.fkie.aidpfm.utilities.R7DB.Query;
import de.fraunhofer.fkie.evaluation.ClusterDataGenerator;
import de.fraunhofer.fkie.evaluation.model.Cluster;
import de.fraunhofer.fkie.evaluation.model.Result;

public class ClusterDataGeneratorTest {

//	@Test
//	public void testUtilityVectorGeneration() throws FileNotFoundException, IOException, ClassNotFoundException,
//			EncryptedDocumentException, InvalidFormatException {
//		Query query = Query.ALL;
//		List<Requirement> requirements = R7DB.getRequirements(query);
//		Node<String> testData;
//		try(FileInputStream fis = new FileInputStream(Config.TESTDATA);
//				ObjectInputStream ois = new ObjectInputStream(fis)){
//			testData = (Node<String>) ois.readObject();
//		}
//		testData.getChildren().remove(2); // get "zurückgestellt" out
//		ClusterDataGenerator generator = new ClusterDataGenerator();
//		List<Requirement> testRequirements = generator.toRequirementList(testData, requirements);
//		List<Requirement> utilityRequirements = generator.utilityRequirements(testRequirements);
//		Result humanReference = generator.inputIntoResult0504(testData, requirements);
//		generator.filterHumanReference(humanReference, utilityRequirements);
//		int y = 0;
//		for(Cluster cluster : humanReference.getClusters()){
//			y += cluster.getRequirements().size();
//		}
//		// int x = utilityRequirements.size();
//		// assertTrue("Some Requirements are missing. There were " + x
//		// + " requirements found in the utility table that are also in the test
//		// data. In the test data are after filtering " + y
//		// + " requirements", x == y);
//	}

//	@Test
//	public void testOpenThesaurus() {
//		try{
//			LinkedList<Requirement> requirements = new LinkedList<Requirement>();
//			Class.forName("org.sqlite.JDBC");
//
//			Connection connection = null;
//			try{
//				SQLiteConfig config = new SQLiteConfig();
//				config.setReadOnly(true);
//				connection = DriverManager.getConnection("jdbc:sqlite:" + "C:/Users/daniel.toews/Documents/openthesaurus_dump.tar/openthesaurus_dump.sql",
//						config.toProperties());
//				Statement statement = connection.createStatement();
//
//				ResultSet rs = statement.executeQuery("SELECT * FROM term, synset, term term2 WHERE synset.is_visible = 1 AND synset.id"+
//   "= term.synset_id AND term.synset_id AND term2.synset_id = synset.id AND term2.word = 'Bank'");
//				while(rs.next()){
//					Requirement requirement = new Requirement();
//					for(Field field : Field.values()){
//						requirement.set(field, rs.getString(field.fieldname));
//					}
//					requirements.add(requirement);
//				}
//			}
//			catch(SQLException e){
//				System.err.println(e.getMessage());
//			}
//			finally{
//				try{
//					if(connection != null)
//						connection.close();
//				}
//				catch(SQLException e){
//					System.err.println(e);
//				}
//			}
//		}
//		catch(Exception e){
//			throw new RuntimeException("Could not retrieve Requirements:", e);
//		}
//	}

}
