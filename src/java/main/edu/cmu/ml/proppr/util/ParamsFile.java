package edu.cmu.ml.proppr.util;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TObjectDoubleMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ParamsFile extends ParsedFile {
	private static final Logger log = Logger.getLogger(ParamsFile.class);
	public static final String HEADER_PREFIX="#! ";
	
	private Properties properties;
	
	public ParamsFile(File file) {
		super(file);
	}

	public ParamsFile(String filename) {
		super(filename);
	}
	
	public ParamsFile(StringReader stringReader) {
		super(stringReader);
	}
	
	private Properties getProperties() {
		if (this.properties == null) 
			this.properties = new Properties();
		return this.properties;
	}
	
	@Override
	protected void processComment(String line) {
		if (!line.startsWith(HEADER_PREFIX)) return;
		try {
			getProperties().load(new StringReader(line.substring(3)));
		} catch (IOException e) {
			throw new IllegalArgumentException("Bad params file syntax at line "+this.getAbsoluteLineNumber()+": "+line);
		}
	}
	
	public String getProperty(String name) {
		return getProperties().getProperty(name);
	}
	public Properties getHeader() {
		if (!this.isClosed()) { 
			throw new IllegalStateException("Bad programmer: read the parsedFile to completion and close it before requesting the header!"); 
		}
		return getProperties(); 
	}



	private static void saveParameter(Writer writer, String paramName, Double paramValue) throws IOException {
		writer.write(String.format("%s\t%.6g\n", paramName,paramValue));
	}
	private static void saveHeader(Writer writer, ModuleConfiguration config) throws IOException {
		writer.write(HEADER_PREFIX);
		writer.write(ModuleConfiguration.SQUASHFUNCTION_MODULE_OPTION);
		writer.write("=");
		writer.write(config.squashingFunction.toString());
		writer.write("\n");
		
		if (config.programFiles != null) {
			writer.write(HEADER_PREFIX);
			writer.write("programFiles=");
			writer.write(Dictionary.buildString(config.programFiles, new StringBuilder(), ":", true).toString());
			writer.write("\n");
		}
		
		if (config.prover != null) {
			writer.write(HEADER_PREFIX);
			writer.write("prover=");
			writer.write(config.prover.toString());
			writer.write("\n");
		}
		
	}
	public static void save(Map<String,Double> params, File paramsFile, ModuleConfiguration config) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(paramsFile));
			// write header
			if (config != null) saveHeader(writer,config);
			// write params
			for (Map.Entry<String,Double>e : params.entrySet()) {
				saveParameter(writer,String.valueOf(e.getKey()),e.getValue());
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void save(TIntDoubleMap params, File paramsFile, ModuleConfiguration config) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(paramsFile));
			// write header
			if (config != null) saveHeader(writer,config);
			// write params
			for (TIntDoubleIterator e = params.iterator(); e.hasNext(); ) {
				e.advance();
				saveParameter(writer,String.valueOf(e.key()),e.value());
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void save(TObjectDoubleMap<String> params,
			File paramsFile, ModuleConfiguration config) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(paramsFile));
			// write header
			if (config != null) saveHeader(writer,config);
			// write params
			for (TObjectDoubleIterator<String> e = params.iterator(); e.hasNext(); ) {
				e.advance();
				saveParameter(writer,e.key(),e.value());
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/** Check supported configuraton settings to see that the specified configuration
	 * matches the settings stored in this params file.
	 * @param c
	 */
	public void check(ModuleConfiguration c) {
		if (!this.getProperty(ModuleConfiguration.SQUASHFUNCTION_MODULE_OPTION).equals(c.squashingFunction.toString())) 
			failCheck(ModuleConfiguration.SQUASHFUNCTION_MODULE_OPTION,this.getProperty(ModuleConfiguration.SQUASHFUNCTION_MODULE_OPTION), c.squashingFunction.toString(), c.force);
		
		if (this.getProperty("programFiles") != null) {
			if (c.programFiles == null) {
				failCheck("programFiles",this.getProperty("programFiles"),"null",c.force);
			} else {
				int i=0;
				for (String p : this.getProperty("programFiles").split(":")) {
					if (!p.equals(c.programFiles[i])) failCheck("programFiles:"+i,p,c.programFiles[i], c.force);
					i++;
				}
			}
		}
		
		if (this.getProperty("prover") != null && !this.getProperty("prover").equals(c.prover.toString()))
			failCheck("prover",this.getProperty("prover"),c.prover.toString(), c.force);
	}
	private void failCheck(String setting, String paramsFile, String configuration, boolean force) {
		String line = "Command line configuration does not match params file! Setting '"+setting+"' expected '"+paramsFile+"' but was '"+configuration+"'";
		if (force) {
			log.error(line);
			return;
		}
		throw new MisconfigurationException(line);
	}

}
