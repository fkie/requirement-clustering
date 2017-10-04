package de.fraunhofer.fkie.evaluation;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Calendar;

import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.fraunhofer.fkie.aidpfm.utilities.Util;
import de.fraunhofer.fkie.evaluation.model.EvaluationConfig;

public class Pacemaker {

	/**
	 * checks whether the current machine is alive or dead. If it died before,
	 * don´t reanimate. We need to know the cause of death
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	public static boolean checkHeartbeat() {
		String file = getHeartbeatFile();
		// may contain / to seperate name and ip adress
		Main.HEARTBEATFILE = file;
		File testHeartbeat = new File(Config.HEARTBEAT);
		for(final File fileEntry : testHeartbeat.listFiles()){
			String s = fileEntry.getName();
			if(s.contains(file)){
				String status = getStatus(s);
				if(status.equals("DEAD")){
					// needed for later checks
					Main.HEARTBEATFILE = fileEntry.getAbsolutePath();
					// NO PULSE! IT IS DEAD TIMM!
					return false;
				}
				else{
					return true;
				}
			}
		}
		return true;
	}

	public static String getStatus(String heartbeat) {
		return heartbeat.substring(heartbeat.indexOf("[") + 1, heartbeat.indexOf("]"));
	}

	public static void createHeartbeat(String status) throws IOException {
		// get System and Name
		String file = getHeartbeatFile();
		// make absoulute path
		Main.HEARTBEATFILE = Config.HEARTBEAT + "/" + status+ " "+ file + ".txt";
		Main.HEARTBEATCONTENT = Util.getVersion() + "\n";
		// write heartbeat with current status evaluating (if some file of this
		// system already exists, rename it before)
		String existingHeartbeatAbsolutePath = Pacemaker.existingHeartbeat();
		if(existingHeartbeatAbsolutePath != null){
			Path source = Paths.get(existingHeartbeatAbsolutePath);
			Files.move(source, source.resolveSibling(Main.HEARTBEATFILE));
		}
		Util.writeUtf8File(Main.HEARTBEATFILE, Main.HEARTBEATCONTENT);
	}

	public static void goToSleep() throws IOException {
		// may contain / to seperate name and ip adress
		// write heartbeat with current status evaluating
		String existingHeartbeatAbsolutePath = Pacemaker.existingHeartbeat();
		if(existingHeartbeatAbsolutePath != null){
			Path source = Paths.get(existingHeartbeatAbsolutePath);
			Files.move(source, source.resolveSibling(Main.HEARTBEATFILE));
		}
	}

	public static boolean inEvaluationRange(LocalTime candidate, Calendar today) {
		// hardcoded start and end
		int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
		// WEEKEND!!!
		if(dayOfWeek == 1 || dayOfWeek == 7){
			return true;
		}
		boolean beforeSix = candidate.getHour() < 6;
		boolean afterEight = candidate.getHour() >= 20;
		return beforeSix || afterEight;
	}

	public static void updateHeartbeat(EvaluationConfig settings) throws IOException {
		Main.HEARTBEATCONTENT += LocalTime.now() + ": " +settings.toString() + " with version: " + Util.getVersion() + "\n";
		Util.writeUtf8File(Main.HEARTBEATFILE, Main.HEARTBEATCONTENT);
	}

	/**
	 * check if there is already a heartbeat and if so, return it so we can
	 * rename it
	 * 
	 * @return
	 */
	public static String existingHeartbeat() {
		String file = getHeartbeatFile();
		File testHeartbeat = new File(Config.HEARTBEAT);
		for(final File fileEntry : testHeartbeat.listFiles()){
			String s = fileEntry.getName();
			if(s.contains(file)){
				return fileEntry.getAbsolutePath();
			}
		}
		return null;
	}

	public static String getHeartbeatFile() {
		String userName = System.getenv("MINION_USERNAME");
		String localMachine = System.getenv("MINION_SYSTEMNAME");
		String file = localMachine.toString().replace("/", "") + " "+ userName;
		return file;
	}

	public static void changeStatus(String newStatus) throws IOException {
		Path source = Paths.get(Main.HEARTBEATFILE);
		Main.HEARTBEATFILE = Main.HEARTBEATFILE.replace(getStatus(Main.HEARTBEATFILE), newStatus);
		Files.move(source, source.resolveSibling(Main.HEARTBEATFILE));
	}

	public static boolean vacations() {
		File testHeartbeat = new File(Config.HEARTBEAT);
		//my computer must be able to work for bugfixes
		if(System.getenv("MINION_USERNAME").equals("Gru")){
			return false;
		}
		for(final File fileEntry : testHeartbeat.listFiles()){
			String s = fileEntry.getName();
			// The S is very important, as the resulting status will be
			// VACATION. So the S is needed to prevent that the status messages
			// will keep the minions in vacation
			if(s.contains("VACATIONS")){
				return true;
			}
		}
		return false;
	}
}
