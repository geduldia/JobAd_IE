package de.uni_koeln.spinfo.classification.zoneAnalysis.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ZoneClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.helpers.SingleToMultiClassConverter;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

/**
 * 
 * @author geduldia
 *
 */

public class DumpDataBaseTrainingDataGenerator {
	
	SingleToMultiClassConverter stmc;
	List<ClassifyUnit> data = null;
	File databaseFile;
	Map<ClassifyUnit, Integer> correctedData = new HashMap<ClassifyUnit, Integer>();
	TrainingDataGenerator tdg;
	
	public DumpDataBaseTrainingDataGenerator(SingleToMultiClassConverter stmc, File databaseFile,  File newTrainingDataFile) throws IOException{
		this.stmc = stmc;	
		this.databaseFile = databaseFile;
		if(!newTrainingDataFile.exists()){
			newTrainingDataFile.createNewFile();
		}
		this.tdg = new TrainingDataGenerator(newTrainingDataFile);
		//readClassifiedParagraphsFromFile();
	}
	
	public void readInBIBBClassifiedParagraphsFromFile() throws IOException{
		data = new ArrayList<ClassifyUnit>();
		JASCClassifyUnit.setNumberOfCategories(stmc.getNumberOfCategories(), stmc.getNumberOfClasses(), stmc.getTranslations());
		
		Workbook w;
		try {
			WorkbookSettings ws = new WorkbookSettings();
			ws.setEncoding("UTF-8");
			w = Workbook.getWorkbook(databaseFile, ws);
			Sheet sheet = w.getSheet(0);
			JASCClassifyUnit cu;
			boolean[] classes;
			for (int i = 1; i < sheet.getRows(); i++) {
				classes = new boolean[4];
				String content = sheet.getCell(5,i).getContents();
				UUID id = UUID.fromString(sheet.getCell(2,i).getContents());
				for(int column = 7; column <= 10; column++){
					boolean b;
					if(sheet.getCell(column,i).getContents().equals("0")){
						b = false;
					}
					else{
						b = true;
					}
					classes[column-7] = b;
				}
				int jahrgang = Integer.parseInt(sheet.getCell(3,i).getContents());
				int zeilennr = Integer.parseInt(sheet.getCell(4,i).getContents());
				cu = new JASCClassifyUnit(content, jahrgang, zeilennr, id);
				cu.setClassIDs(classes);
				cu.setActualClassID(stmc.getSingleClass(classes));
				data.add(cu);
			}
		}
		catch(BiffException e){
			e.printStackTrace();
		}
	}
	
	public void annotate() throws IOException{
		tdg.annotate(data);
	}
	
	public List<ClassifyUnit> getData(){
		return data;
	}

}
