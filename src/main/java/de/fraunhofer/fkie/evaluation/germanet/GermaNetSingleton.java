package de.fraunhofer.fkie.aidpfm.germanet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import de.fraunhofer.fkie.aidpfm.utilities.Config;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;

public class GermaNetSingleton {
	private static volatile GermaNet germaNet = null;
	public static volatile GermaNetSingleton instance = null;

	private GermaNetSingleton() throws FileNotFoundException, XMLStreamException, IOException {
		File gnetDir = new File(Config.GERMANET);
		germaNet = new GermaNet(gnetDir);
	}

	public static GermaNet getInstance() throws FileNotFoundException, XMLStreamException, IOException {
		if (instance == null) {
			synchronized (GermaNet.class) {
				if (instance == null) {
					instance = new GermaNetSingleton();
				}
			}
		}
		return instance.getGermaNet();
	}

	public GermaNet getGermaNet() {
		return germaNet;
	}
}