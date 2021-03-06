package edu.cmu.ml.proppr.prove.wam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Holds a modified Warran abstract machine program, consisting of:

    1)instructions = a list of tuples (opcode,arg1,...)

    2) labels = a defaultdict such that labels["p/n"] is a list of
     addresses (ie, indices in instructions) where the instructions
     for the clauses of p/n start.

    3) instLabels = a dict such that instLabels[i] is the label
    given to instruction i, if there is such a label.
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 *
 */
public class WamBaseProgram extends WamProgram {
	private List<Instruction> instructions;
	private Map<Integer,String> instLabels;
	private Map<String,List<Integer>> labels;
	private int saveLength;
	public WamBaseProgram() {
		instructions = new ArrayList<Instruction>();
		instLabels = new HashMap<Integer,String>();
		labels = new HashMap<String,List<Integer>>();
	}
	/* (non-Javadoc)
	 * @see edu.cmu.ml.proppr.prove.wam.IWamProgram#append(edu.cmu.ml.proppr.prove.wam.Instruction)
	 */
	@Override
	public void append(Instruction inst) {
		instructions.add(inst);
	}

	/* (non-Javadoc)
	 * @see edu.cmu.ml.proppr.prove.wam.IWamProgram#setInstruction(int, edu.cmu.ml.proppr.prove.wam.Instruction)
	 */
	@Override
	public void setInstruction(int placeToPatch, Instruction instruction) {
		this.instructions.set(placeToPatch, instruction);
	}
	/* (non-Javadoc)
	 * @see edu.cmu.ml.proppr.prove.wam.IWamProgram#size()
	 */
	@Override
	public int size() { return instructions.size(); }
	/* (non-Javadoc)
	 * @see edu.cmu.ml.proppr.prove.wam.IWamProgram#getInstruction(int)
	 */
	@Override
	public Instruction getInstruction(int addr) {
		return instructions.get(addr);
	}
	/* (non-Javadoc)
	 * @see edu.cmu.ml.proppr.prove.wam.IWamProgram#insertLabel(java.lang.String)
	 */
	@Override
	public void insertLabel(String label) {
		int i = instructions.size();
		instLabels.put(i, label);
		if (!labels.containsKey(label)) labels.put(label, new ArrayList<Integer>());
		labels.get(label).add(i);
	}
	/* (non-Javadoc)
	 * @see edu.cmu.ml.proppr.prove.wam.IWamProgram#hasLabel(java.lang.String)
	 */
	@Override
	public boolean hasLabel(String jumpTo) {
		return labels.containsKey(jumpTo);
	}	
	/* (non-Javadoc)
	 * @see edu.cmu.ml.proppr.prove.wam.IWamProgram#getAddresses(java.lang.String)
	 */
	@Override
	public List<Integer> getAddresses(String jumpTo) {
		return labels.get(jumpTo);
	}
	/* (non-Javadoc)
	 * @see edu.cmu.ml.proppr.prove.wam.IWamProgram#save()
	 */
	@Override
	public void save() {
		this.saveLength = this.instructions.size();
	}
	/* (non-Javadoc)
	 * @see edu.cmu.ml.proppr.prove.wam.IWamProgram#revert()
	 */
	@Override
	public void revert() {
		for (int i=this.instructions.size()-1; i>=this.saveLength; i--) {
			this.instructions.remove(i);
		}
	}
	
	public static WamProgram load(File file) throws IOException {
		LineNumberReader reader = new LineNumberReader(new FileReader(file));
		return load(reader);
	}
	public static WamProgram load(LineNumberReader reader) throws IOException {
		WamProgram program = new WamBaseProgram();
		for (String line; (line = reader.readLine()) != null;){
			String[] parts = line.split("\t",3);
			if (parts[1] != "") program.insertLabel(parts[1]);
			program.append(Instruction.parseInstruction(parts[2]));
		}
		program.save();
		return program;
	}

}
