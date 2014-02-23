package com.hot.cmt.comment.preprocess;

import java.io.BufferedReader;   
import java.io.FileReader; 
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.hot.cmt.comment.common.TrendencyWordsLoader;
import com.hot.cmt.comment.tokenize.MMTokenizer;

/**
 * 计算词语和情感词之间的PMI信息
 * @author yongleixiao
 *
 */
public class PMIGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(PMIGenerator.class);
	
	/* 计算PMI */
	/* 评论及其分词结果 */
	private Map<Long, List<String>> comments2Words = null;
	/* 词语及其出现的文章ID列表 */
	private Map<String, List<Long>> words2Comments = null;
	/* 总的词数 */
	private int totalWords = 0;
	
	/* 停用词 */
	Set<String> stopWords = null;
	
	/* 加载词典 */
	private TrendencyWordsLoader wordsLoader = null;
	/* 分词  */
	private MMTokenizer tokenizer = null;
	
	public PMIGenerator() {	
		comments2Words = new HashMap<Long, List<String>>();
		words2Comments = new HashMap<String, List<Long>>();
		stopWords = new HashSet<String>();
		wordsLoader = new TrendencyWordsLoader();
		tokenizer = new MMTokenizer();
	}
	
	/**
	 * 从语料中初始化矩阵
	 * 矩阵的行为评论，矩阵的列为词到文档的索引
	 * 这里并没有对词进行Id化，简单起见，只是过滤了停用词等
	 * @param path
	 */
	public void initFromCorpus(String path) {
		stopWords = wordsLoader.getStopWords();
		long start = System.currentTimeMillis();
		String line = null;  
        String comment = null;
        try {
        	FileReader fr = new FileReader(path);             
            BufferedReader br = new BufferedReader(fr);
            while((line = br.readLine()) != null){  
                  comment = line.trim();     
                  if (StringUtils.isBlank(comment)) {
                	  continue;
                  }
                  if (comment.length() < 10) continue;
                  if (comment.length() > 200) {
                	  LOGGER.info("too long! comment = {}", comment);
                	  continue;
                  }
                  List<String> words = tokenizer.getSplitedWords(comment);
                  words = cleanWords(words);
                  if (words.size() <= 1) {
                	  continue;
                  }
                  totalWords += words.size();
                  LOGGER.info("totalWords = {}", totalWords);
                  compute(words);
            }
            br.close();
            fr.close();
        } catch(Exception e) {
        	
        }
        long end = System.currentTimeMillis();
        LOGGER.info("init corpus done! cost = {}", (end-start)/1000);
	}
	
	private List<String> cleanWords(List<String> words) {
		List<String> cleaned = new ArrayList<String>();
		for (String word : words) {
			if (stopWords.contains(word)) {
				continue;
			}
			if (StringUtils.isBlank(word)) {
				continue;
			}
			cleaned.add(word);
		}
		return cleaned;
	}
	
	/**
	 * 将分词结果存入矩阵
	 * @param words
	 */
	private void compute(List<String> words) {
		long cId = comments2Words.size() + 1;
		comments2Words.put(cId, words);
		for (String word: words) {
			if (words2Comments.containsKey(word)) {
				List<Long> commentIds = words2Comments.get(word);
				commentIds.add(cId);
			} else {
				List<Long> commentIds = new ArrayList<Long>();
				commentIds.add(cId);
				words2Comments.put(word, commentIds);
			}
		}
	}
	
	
	public double getPMIScore(String word1, String word2) {
		double score = 0.0D;
		List<Long> word1CommentIds = words2Comments.get(word1);
		List<Long> word2CommentIds = words2Comments.get(word2);
		if (word1CommentIds == null || word2CommentIds == null) {
			return score;
		}
		if (word1CommentIds.size() == 0 || word2CommentIds.size() == 0) {
			return score;
		}
		int word1Freq = word1CommentIds.size();
		int word2Freq = word2CommentIds.size();
		double word1Prob = div(word1Freq, totalWords, 8);
		double word2Prob = div(word2Freq, totalWords, 8);
		Map<Long, Integer> word1Map = list2Map(word1CommentIds);
		Map<Long, Integer> word2Map = list2Map(word2CommentIds);
		int uniFreq = 0;
		for (Map.Entry<Long, Integer> entry : word1Map.entrySet()) {
			long id = entry.getKey();
			if (word2Map.containsKey(id)) {
				uniFreq += Math.min(entry.getValue(), word2Map.get(id));
			}
		}
		
		if (uniFreq == 0) {
			return score;
		}
		double uniProb = div(uniFreq, totalWords, 8);
		return Math.log(div(uniProb, word1Prob*word2Prob, 8));
	}
	
	
	private static double div(double d1,double d2,int scale){
        if(scale < 0){
            throw new IllegalArgumentException(
                "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
    }
	
	
	private static Map<Long, Integer> list2Map(List<Long> ids) {
		Map<Long, Integer> map = new HashMap<Long, Integer>();
		for (Long id : ids) {
			if (map.containsKey(id)) {
				map.put(id, map.get(id) + 1);
			} else {
				map.put(id, 1);
			}
		}
		return map;
	}
	
	/**
	 * 获取一个词的共现词
	 * @param word
	 * @return
	 */
	private List<String> getCoWords(String word) {
		Set<String> words = new HashSet<String>();
		List<Long> commentIds = words2Comments.get(word);
		if (commentIds == null || commentIds.size() == 0) {
			return new ArrayList<String>();
		}
		for (Long id: commentIds) {
			words.addAll(comments2Words.get(id));
		}
		return new ArrayList<String>(words);
	}
	
	/**
	 * 计算与情感词的pmi分数并输出到文件
	 * @param path
	 * @throws IOException 
	 */
	private void computePMIScore(String path, Map<String, Double> senWordsMap) throws IOException {
		FileWriter fw = new FileWriter(path);
		Map<String, Double> scores = new HashMap<String, Double>();
		double weight = 0.0D;
		for (Map.Entry<String, Double> entry : senWordsMap.entrySet()) {
			String word = entry.getKey();
			List<String> coWords = getCoWords(word);
			if (coWords.isEmpty()) {
				continue;
			}
			for (String coWord : coWords) {
				weight = getPMIScore(word, coWord);
				if (scores.containsKey(coWord)) {
					weight += scores.get(coWord);
				}
				scores.put(coWord, weight);
			}
		}
		for (Map.Entry<String, Double> entry : scores.entrySet()) {
			weight = entry.getValue()/senWordsMap.size();
			LOGGER.info("word = {} score = {}", entry.getKey(), weight);
			fw.write(entry.getKey() + "=" + weight + "\n");
		}
		fw.close();
	}
	
	
	public void computePMI() {
		String posPath = "D:/changyancomments/pmiscores/posScore.txt";
		String negPath = "D:/changyancomments/pmiscores/negScore.txt";
		Map<String, Double> posWordsMap = wordsLoader.getPosSenWordsMap();
		Map<String, Double> negWordsMap = wordsLoader.getNegSenWordsMap();
		try {
			computePMIScore(posPath, posWordsMap);
			computePMIScore(negPath, negWordsMap);
		} catch (IOException e) {
			
		}
	}
	
	public static void main(String[] args) {
		PMIGenerator pmi = new PMIGenerator();
		String corpus = "D:/changyancomments/rawcomment/rawComments.txt";
		pmi.initFromCorpus(corpus);
		pmi.computePMI();
	}
	
}
