package OtherFunctions.ReSdiWithWindowsedMsa;

import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndelPlinkFromMsaAAA {

    private String msaFolder;
    private String accessionListFile;
    private String refName;
    private ArrayList<String> chrs = new  ArrayList<String>();
    private String genomeFolder;

    public synchronized void setMsaFolder(String msaFolder) {
        this.msaFolder = msaFolder;
    }
    public synchronized void setAccessionListFile(String accessionListFile) {
        this.accessionListFile = accessionListFile;
    }
    public synchronized void setRefName(String refName) {
        this.refName = refName;
    }
    public synchronized void setChrs(ArrayList<String> chrs) {
        this.chrs = chrs;
    }
    public synchronized void setGenomeFolder(String genomeFolder) {
        this.genomeFolder = genomeFolder;
    }
    public IndelPlinkFromMsaAAA(){

    }

	public IndelPlinkFromMsaAAA(String[] argv ){
		StringBuffer helpMessage=new StringBuffer("INDEL synchronization pipeline\nE-mail:song@mpipz.mpg.de\nArguments:\n");
        helpMessage.append("  -i   input folder where could find MSA result\n");
        helpMessage.append("  -l   list of accession names. (the reference accession should be in included)\n");
        helpMessage.append("  -r   name of reference accession/line\n");
        helpMessage.append("  -c   list of chromosome names\n");
        helpMessage.append("  -g   the folder where the genome sequences and sdi files are located\n");
        
		Options options = new Options();
        options.addOption("n",true,"numberOfAccessions");
        options.addOption("i",true,"msaFolder");
        options.addOption("l",true,"accessionListFile");
        options.addOption("r",true,"refName");
        options.addOption("c",true,"chrlist");
        options.addOption("o",true,"outPutPath");
        options.addOption("g",true,"genomeFolder");
        
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd=null;
        try {
            cmd = parser.parse(options, argv);
        } catch (ParseException e) {
            System.out.println("Please, check the parameters.");
            e.printStackTrace();
            System.exit(1);
        }
        if(cmd.hasOption("i")){
        	msaFolder = cmd.getOptionValue("i");
        }else{
        	System.err.println("-i is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("l")){
        	accessionListFile = cmd.getOptionValue("l");
        }else{
        	System.err.println("-l is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("r")){
        	refName = cmd.getOptionValue("r");
        }else{
        	System.err.println("-r is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        String chrlist="";
        if(cmd.hasOption("c")){
        	chrlist = cmd.getOptionValue("c");
        }else{
        	System.err.println("-c is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("g")){
        	genomeFolder = cmd.getOptionValue("g");
        }else{
        	System.err.println("-g is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }


        File file2 = new File(chrlist);
        BufferedReader reader2 = null;
        try {

            Pattern p = Pattern.compile("\\w");

            reader2 = new BufferedReader(new FileReader(file2));
            String tempString = null;
            while ((tempString = reader2.readLine()) != null) {
                Matcher m = p.matcher(tempString);
                if( m.find() ){
                    chrs.add(tempString);
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e1) {
                    e1.getStackTrace();
                }
            }
        }
        doit();
	}
	
	
	public void doit(){
        // prepare accession List begin
		ArrayList<String> names = new  ArrayList<String>();
		File file = new File(accessionListFile);
		BufferedReader reader = null;
        try {
        	reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
            	names.add(tempString);
            }
            names.add(refName);
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                	e1.getStackTrace();
                }
            }
        }
        // prepare accession List end
        try {
            // tfam begin
            PrintWriter outTfam = new PrintWriter(new FileOutputStream("msa_indel.tfam"), true);
            for(String name : names){
                outTfam.println(name+"\t"+name+"\t0\t0\t1\t1");
            }
            outTfam.close();
            // tfam end

            PrintWriter outTped = new PrintWriter(new FileOutputStream("msa_indel.tped"), true); // empty it
            outTped.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        for(String ss1 : chrs){
        	if( (new File(msaFolder + File.separator + ss1)).isDirectory() ){
        	    // get msa file list begin
        		HashMap<String, ArrayList<MsaFile>> msaFileLocationsHashmap = new HashMap<String, ArrayList<MsaFile>>();
				msaFileLocationsHashmap.put(ss1, new ArrayList<MsaFile>());
				File f = new File(msaFolder + File.separator + ss1 + File.separator);
				String s[]=f.list();
				Pattern p = Pattern.compile("^(\\d+)_(\\d+)\\.mafft$");
				for(String ss : s){
					Matcher m=p.matcher(ss);
					if(m.find()){
                        MsaFile msaFile = new MsaFile(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), msaFolder + File.separator + ss1 + File.separator + ss);
						msaFileLocationsHashmap.get(ss1).add(msaFile);
					}
				}
                // get msa file list end

                new IndelPlinkFromMsaAction(names, msaFileLocationsHashmap, refName, genomeFolder);
			}
		}
	}
}

