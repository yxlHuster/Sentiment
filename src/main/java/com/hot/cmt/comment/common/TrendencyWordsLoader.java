package com.hot.cmt.comment.common;
import java.io.BufferedReader;   
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;   
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import com.hot.cmt.comment.common.Constants;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author yongleixiao
 *
 */
public class TrendencyWordsLoader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrendencyWordsLoader.class);

	/* 语气词库  */
	private Set<String> toneWords;
	
	/* 正例情感词库 */
	private Map<String, Double> posSenWordMap;
	/* 经过同义词扩展的正例情感词库 */
	private Map<String, Double> posSemWordMap;
	/* 正例评价词库 */
	private Map<String, Double> posCommentWordMap;
	
	/* 负例情感词库 */
	private Map<String, Double> negSenWordMap; 
	/* 经过同义词扩展的负例情感词库 */
	private Map<String, Double> negSemWordMap;
	/* 负例评价词库 */
	private Map<String, Double> negCommentWordMap;
	
	/* 加载词的pmi分数 */
	private Map<String, Double> posPMIScores;
	private Map<String, Double> negPMIScores;
	
	/* 停用词表 */
	private Set<String> stopWords;
      
    public TrendencyWordsLoader() {  
        loadWords();  
    }
    
    private Map<String, Double> loadWordsWithWeight(String path) {
    	Map<String, Double> results = new HashMap<String, Double>();
    	String line = null;  
        String word = null;
        try {
        	InputStreamReader is = new InputStreamReader(new FileInputStream(path), "UTF-8");            
            BufferedReader br = new BufferedReader(is);
            while((line = br.readLine()) != null){  
                  word = line.trim();               
                  results.put(word, 0.0D);  
            }
            br.close();
            is.close();
        } catch(Exception e) {
        	
        }
        LOGGER.info("word_path = {} size = {}", path, results.size());
        return results;    
    }
    
    
    
    private Map<String, Double> loadPMIScores(String path) {
    	Map<String, Double> results = new HashMap<String, Double>();
    	String line = null;  
        String word = null;
        double weight = 0.0D;
        try {
        	InputStreamReader is = new InputStreamReader(new FileInputStream(path), "UTF-8");             
            BufferedReader br = new BufferedReader(is);
            while((line = br.readLine()) != null){  
                  String[] array = StringUtils.split(line.trim(), "="); 
                  if (array.length != 2) {
                	  continue;
                  }
                  word = array[0];
                  weight = Double.parseDouble(array[1]);
                  results.put(word, weight);  
            }
            br.close();
            is.close();
        } catch(Exception e) {
        	
        }
        LOGGER.info("word_pmi_score_path = {} size = {}", path, results.size());
        return results;    
    }
    
    
    private Set<String> loadWords(String path) {
    	Set<String> results = new HashSet<String>();
    	String line = null;
    	String word = null;
    	try {
    		InputStreamReader is = new InputStreamReader(new FileInputStream(path),"UTF-8");             
            BufferedReader br = new BufferedReader(is);
            while((line = br.readLine()) != null){  
                  word = line.trim();               
                  results.add(word);  
            }
            br.close();
            is.close();
    	} catch(Exception e) {
    		
    	}
    	LOGGER.info("word_path = {} size = {}", path, results.size());
    	return results;
    }
      
    private void loadWords() { 
    	toneWords = loadWords(Constants.TONE_WORDS);
    	
    	posSenWordMap = loadWordsWithWeight(Constants.POS_SEN_DICT);
    	posSemWordMap = loadWordsWithWeight(Constants.POS_SEM_DICT);
    	posCommentWordMap = loadWordsWithWeight(Constants.POS_CON_DICT);
    	
    	negSenWordMap = loadWordsWithWeight(Constants.NEG_SEN_DICT);
    	negSemWordMap = loadWordsWithWeight(Constants.NEG_SEM_DICT);
    	negCommentWordMap = loadWordsWithWeight(Constants.NEG_CON_DICT);
    
        stopWords = loadWords(Constants.STOP_WORDS);
        
        posPMIScores = loadPMIScores(Constants.POS_PMI_SCORE);
        negPMIScores = loadPMIScores(Constants.NEG_PMI_SCORE);
        
    } 
    
    public Set<String> getToneWords() {
    	return toneWords;
    }
    
    public Map<String, Double> getPosSenWordsMap() {
    	return posSenWordMap;
    }
    
    public Map<String, Double> getPosSemWordsMap() {
    	return posSemWordMap;
    }
    
    public Map<String, Double> getPosCommentWordsMap() {
    	return posCommentWordMap;
    }
    
    public Map<String, Double> getNegSenWordsMap() {
    	return negSenWordMap;
    }
    
    public Map<String, Double> getNegSemWordsMap() {
    	return negSemWordMap;
    }
    
    public Map<String, Double> getNegCommentWordsMap() {
    	return negCommentWordMap;
    }
    
    public Set<String> getStopWords() {
    	return stopWords;
    }
    
    public boolean isToneWords(String word) {
    	return toneWords.contains(word);
    }
    
    public boolean isPosSenWord(String word) {
    	return posSenWordMap.containsKey(word);
    }
    
    public boolean isPosSemWord(String word) {
    	return posSemWordMap.containsKey(word);
    }
    
    public boolean isPosCommentWord(String word) {
    	return posCommentWordMap.containsKey(word);
    }
    
    public boolean isNegSenWord(String word) {
    	return negSenWordMap.containsKey(word);
    }
    
    public boolean isNegSemWord(String word) {
    	return negSemWordMap.containsKey(word);
    }
    
    public boolean isNegCommentWord(String word) {
    	return negCommentWordMap.containsKey(word);
    }
    
    public boolean isStopWord(String word) {
    	return stopWords.contains(word);
    }
      
    
    public Map<String, Double> getPosPMIScores() {
    	return posPMIScores;
    }
    
    public Map<String, Double> getNegPMIScores() {
    	return negPMIScores;
    }
    
    
    public double getPosPMIScoreOfWord(String word) {
    	if (posPMIScores.containsKey(word)) {
    		return posPMIScores.get(word);
    	}
    	return 0.0D;
    }
    
    public double getNegPMIScoreOfWord(String word) {
    	if (negPMIScores.containsKey(word)) {
    		return negPMIScores.get(word);
    	}
    	return 0.0D;
    }
    
}
