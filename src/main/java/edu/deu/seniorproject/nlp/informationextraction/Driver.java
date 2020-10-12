package edu.deu.seniorproject.nlp.informationextraction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Driver {

	private static final String DONE = "\tDone";
	
	public void start() throws InterruptedException {		
		// Preprocessing part. Uncomment if data needs prepration
		// SET FILENAME IN THE PYTHON SCRIPT FILE
		/*
		System.out.println("Pre processing data...");
		try {
			preprocess();
		} catch (ProcessException | IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println(DONE);
		*/
		System.out.println("Html Parsing...");
		try {
			htmlParse();
		} catch (ProcessException | IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println(DONE);
		InformationExtractor ie = new InformationExtractor(3);
		System.out.println("Pattern Matching...");
		ie.extractFromFile("parse.txt");
		ie.saveToFile();
		System.out.println(DONE);
		System.out.println("Clustering...");
		try {
			clustering();
			System.out.println(DONE);
		} catch (ProcessException | IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Deleting Temp files...");
			try {
				ie.deleteTempFiles();
				System.out.println(DONE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void htmlParse() throws IOException, InterruptedException, ProcessException {
		// Number of rows to be read
		// n = -1 for all file
		int n = 15000;
		// Source file name
		String fileName = "dataset.xlsx";
		Process p = Runtime.getRuntime().exec(String.format("html-parse/KariyerDotNet.exe %d %s", n, fileName));
		while(p.isAlive()) {
			// Sleep while waiting for the process to exit, instead of lazy looping
			Thread.sleep(250);
		}
		BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		if(errReader.ready()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Html-Parse has run into some errors. Detailed error message: \n");
			String line = "";
			while((line = errReader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			errReader.close();
			throw new ProcessException(sb.toString());
		}
	}
	
	private void clustering() throws InterruptedException, IOException, ProcessException {
		Process p = Runtime.getRuntime().exec("python cluster/doc-cluster.py");
		BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		while(p.isAlive()) {
			// Sleep while waiting for the process to exit, instead of lazy looping
			Thread.sleep(250);
		}
		if(errReader.ready()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Clustering has run into some errors. Detailed error message: \n");
			String line = "";
			while((line = errReader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			errReader.close();
			throw new ProcessException(sb.toString());
		}
	}
	
	@SuppressWarnings("unused")
	private void preprocess() throws IOException, InterruptedException, ProcessException {
		String command = "python preprocess/preprocess.py";
		Process p = Runtime.getRuntime().exec(command);
		BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		while(p.isAlive()) {
			// Sleep while waiting for the process to exit, instead of lazy looping
			Thread.sleep(250);
		}
		if(errReader.ready()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Preprocessing has run into some errors. Detailed error message: \n");
			String line = "";
			while((line = errReader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			errReader.close();
			throw new ProcessException(sb.toString());
		}
	}

}
