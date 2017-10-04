package de.fraunhofer.fkie.evaluation;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;

import org.junit.Test;

public class PacemakerIT {
//	@Test
//	public void testHeartbeatCreation() throws IOException{
//		String userName = "TESTMINION"; 
//		String localMachine = "TESTMINIONSSYSTEM";
//		Pacemaker.createHeartbeat("[TEST]");
//		File testHeartbeat = new File(Config.HEARTBEAT);
//		boolean createdCorrect = false;
//		boolean rightContent = false;
//		for(final File fileEntry : testHeartbeat.listFiles()){
//			String s = fileEntry.getName();
//			if(s.contains(localMachine.toString().replace("/", "") + userName)){
//				createdCorrect = true;
//				BufferedReader br = new BufferedReader(new FileReader(fileEntry));
//				String line = br.readLine();
//				String content = "";
//				while(line != null){
//					content += line +"\n";
//					line = br.readLine();
//				}
//				br.close();
//				String version = UtilityFunctions.getVersion();
//				if(content.contains(version)){
//					rightContent = true;
//				}
//				try{
//					Files.delete(Paths.get(fileEntry.getAbsolutePath()));
//				}
//				catch(IOException e){
//					e.printStackTrace();
//				}
//			}
//		}
//		assertTrue(createdCorrect);
//		assertTrue(rightContent);
//	}
	
//	@Test(expected = RuntimeException.class)
//	@Test
//	public void testHeartbeatCheck() {
//		Pacemaker.checkHeartbeat();
//	}
	
	@Test
	public void testSleepTime(){
		boolean sleepTime = Pacemaker.inEvaluationRange(LocalTime.now(), Calendar.getInstance());
//		assertTrue(sleepTime);
	}
}
