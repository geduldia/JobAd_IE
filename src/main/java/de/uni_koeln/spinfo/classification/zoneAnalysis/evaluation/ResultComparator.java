package de.uni_koeln.spinfo.classification.zoneAnalysis.evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ExperimentResult;

/**
 * @author geduldia
 * an object to compare and rank experiment-results
 *
 */
public class ResultComparator {

	Set<ExperimentResult> allResults;

	
	public ResultComparator() {
		this.allResults = new HashSet<ExperimentResult>();
	}

	Comparator<Double> comparator = new Comparator<Double>() {
		public int compare(Double o1, Double o2) {
			// compare
			if (o1 < o2) {
				return 1;
			}
			if (o2 < o1) {
				return -1;
			} else
				return 0;
		}
	};

	/**
	 * @param results
	 */
	public void addResults(List<ExperimentResult> results) {
		allResults.addAll(results);
	}
	
	/**
	 * @param objectFile
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void addResults(File objectFile) throws ClassNotFoundException, IOException{
		readObjects(objectFile);
	}

	/**
	 * @param ev 
	 * @param classID ( 0 = overall value)
	 * @return ranking 
	 * @throws IOException
	 */
	public Map<Double, List<ExperimentResult>> rankAll(EvaluationValue ev,
			int classID, String folderName) throws IOException {
		Map<Double, List<ExperimentResult>> ranking = new TreeMap<Double, List<ExperimentResult>>(comparator);
		for (ExperimentResult result : allResults) {
			double value = 0.0;
			if (classID == 0) {
				value = result.getEvaluationValue(ev);
			} else {
				value = result.getCategoryEvaluations().get(classID - 1)
						.getEvaluationValue(ev);
			}
			List<ExperimentResult> list = ranking.get(value);
			if (list == null) {
				list = new ArrayList<ExperimentResult>();
			}
			list.add(result);
			ranking.put(value, list);

		}
		writeRankingFile(folderName +"/" + ev + "_" + classID + "_" + allResults.hashCode()
				+ ".csv", ranking);

		return ranking;
	}


	/**
	 * @throws IOException
	 */
	public void writeObjects(File file) throws IOException {

		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fos);
		for (ExperimentResult result : allResults) {
			out.writeObject(result);

		}
		out.flush();
		out.close();
	}

	/**
	 * @return resultList
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Set<ExperimentResult> readObjects(File file) throws IOException,
			ClassNotFoundException {
		//allResults.clear();
		if(file.isDirectory()){
			File[] listFiles = file.listFiles();
			for (File file2 : listFiles) {
				FileInputStream fis = new FileInputStream(file2);
				ObjectInputStream in = new ObjectInputStream(fis);
				while (true) {
					try {
						Object o = in.readObject();
						ExperimentResult read = (ExperimentResult) o;
						allResults.add(read);
					} catch (Exception e) {
						break;
					}
				}
				in.close();
			}
		}
		else{
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fis);
			while (true) {
				try {
					Object o = in.readObject();
					ExperimentResult read = (ExperimentResult) o;
					allResults.add(read);
				} catch (Exception e) {
					break;
				}
			}
			in.close();
		}
		return allResults;
	}

	/**
	 * @param fileName
	 * @param ranking
	 * @throws IOException
	 */
	private void writeRankingFile(String fileName,
		Map<Double, List<ExperimentResult>> ranking) throws IOException {
		makePrettyExperimentConf(ranking.get(ranking.keySet().iterator().next()).get(0).getExperimentConfiguration());
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream os = new FileOutputStream(file);
		PrintWriter out = new PrintWriter(os);
		DecimalFormat f = new DecimalFormat("#0.0000");
		out.print("valu"+"\t");
		int numberOfClasses = ranking.values().iterator().next().iterator().next().getNumberOfClasses();
		for(int classID = 0; classID <= numberOfClasses; classID++){
			out.print("pre"+classID+"\t");
			out.print("rec"+classID+"\t");
			out.print("fme"+classID+"\t");
			out.print("acc"+classID+"\t");
		}
	

		out.print("Classifier\t");
		out.print("Distance\t");
		out.print("Quantifier\t");
		out.print("NGrams\t");
		out.print("MI\t");
		out.print("NoStopwords\t");
		out.print("Normalized\t");
		out.print("Stemmed\t");
		out.print("SuffixeTrees");
		
		out.println();
		for (Double d : ranking.keySet()) {
			List<ExperimentResult> list = ranking.get(d);
			
			for (ExperimentResult er : list) {
				out.print(f.format(d)+ "\t");
				out.print(f.format(er.getPrecision()) + "\t");
				out.print(f.format(er.getRecall())+ "\t");
				out.print(f.format(er.getF1Measure()) + "\t");
				out.print(f.format(er.getAccuracy())+ "\t");
				for (int classID = 1; classID <= numberOfClasses; classID++) {
					out.print(f.format(er.getCategoryEvaluations().get(classID - 1).getPrecision())+ "\t");
					out.print(f.format(er.getCategoryEvaluations().get(classID - 1).getRecall()) + "\t");
					out.print(f.format(er.getCategoryEvaluations().get(classID - 1).getF1Score()) + "\t");
					out.print(f.format(er.getCategoryEvaluations().get(classID - 1).getAccuracy()) +"\t");

				}		
				out.print(makePrettyExperimentConf(er.getExperimentConfiguration()));
				out.print(er.getID() + "\t");
				out.println();
			}
			
		}
		out.flush();
		out.close();
	}

	/**
	 * @return resultList
	 */
	public Set<ExperimentResult> getAllResults() {
		return allResults;
	}

	/**
	 * @param result
	 */
	public void addResult(ExperimentResult result) {
		allResults.add(result);
	}
	
	private String makePrettyExperimentConf(String experimentConfiguration){
		String classifier = null;
		String distance = null;
		String quantifier = null;
		boolean stopwords = false;
		boolean normalize = false;
		boolean suffixTree = false;
		boolean stem = false;
		int mi = 0;
		String nGram = null;
		String k = null;
		StringBuffer buff = new StringBuffer();
		String[] snippets = experimentConfiguration.split("&");
		for (String string : snippets) {
			String[] smallSnippets = string.split("_");
			for (String smallS : smallSnippets) {
				if(smallS.equals("filterStopwords")){
					stopwords = true;				
				}
				if(smallS.equals("norm")){
					normalize = true;
				}
				if(smallS.equals("stem")){
					stem = true;
				}
				if(smallS.equals("suffixTrees")){
					suffixTree = true;
				}
				if(smallS.startsWith("mi:")){
					mi = Integer.parseInt(smallS.substring(3));
					
				}
				if(smallS.endsWith("gramms")){
					nGram = smallS;
					
				}
				if(smallS.contains("Classifier")){
					classifier = smallS.substring(0,smallS.length()-10);
				}
				if(smallS.contains("Quantifier")){
					quantifier= smallS.substring(0, smallS.length()-17);
				}
				if(smallS.equals("MANHATTAN")||smallS.equals("COSINUS")||smallS.equals("EUKLID")){
					distance = smallS;
				}				
				if(smallS.startsWith("k=")){
					k = smallS;
				}
			}
			
		}
		if(k==null){
			buff.append(classifier+"\t");
		}
		else{
			buff.append(classifier+"("+k+")\t");
		}
		buff.append(distance+"\t");
		buff.append(quantifier+"\t");
		buff.append(nGram+"\t");
		buff.append(mi+"\t");
		buff.append(stopwords+"\t");
		buff.append(normalize+"\t");
		buff.append(stem+"\t");
		buff.append(suffixTree+"\t");
		
		//System.out.println(buff);
		return buff.toString();
	}
}
