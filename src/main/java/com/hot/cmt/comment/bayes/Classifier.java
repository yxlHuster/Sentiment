package com.hot.cmt.comment.bayes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import com.hot.cmt.comment.common.Constants;
import com.hot.cmt.comment.common.TrendencyWordsLoader;
import com.hot.cmt.comment.common.Constants.SentimentType;
import com.hot.cmt.comment.common.Constants.WordType;
import com.hot.cmt.comment.terms.NGramGenerator;
import com.hot.cmt.comment.tokenize.MMTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author yongleixiao
 *
 */
public class Classifier {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Classifier.class);
	
	private TrendencyWordsLoader wordLoader = null;
	private MMTokenizer tokenizer = null;
	private static final String POS_TAG = "positive";
	private static final String NEG_TAG = "negitive";
	
	public Classifier() {
		wordLoader = new TrendencyWordsLoader();
		tokenizer = new MMTokenizer(); 
	}
	
	public Map<String, Double> getJudgments(String sentence) {
		Map<String, Double> result = new HashMap<String, Double>();
		result.put(POS_TAG, 0.0D);
		result.put(NEG_TAG, 0.0D);
		if (StringUtils.isBlank(sentence)) { 
			return result;
		}
		List<String> rawWords = tokenizer.getSplitedWords(sentence);
		LOGGER.info("tokenizer = {}", rawWords.toString());
		if (rawWords.isEmpty()) {
			return result;
		}
		Map<String, Double> rawScore = judgeNGramList(rawWords);
		LOGGER.info("uniGramWords = {}, rawScore = {}", rawWords.toString(), rawScore.toString());
		result = addScores(result, rawScore);
		
		int wordCnt = rawWords.size();
		if (wordCnt > 1) {
			List<String> biGramWords = NGramGenerator.getBiGram(rawWords.toArray(new String[0]));
			rawScore = judgeNGramList(biGramWords);
			LOGGER.info("biGramWords = {}, rawScore = {}", biGramWords.toString(), rawScore.toString());
			result = addScores(result, rawScore);
		}
		if (wordCnt > 2) {
			List<String> triGramWords = NGramGenerator.getTriGram(rawWords.toArray(new String[0]));
			rawScore = judgeNGramList(triGramWords);
			LOGGER.info("triGramWords = {}, rawScore = {}", triGramWords.toString(), rawScore.toString());
			result = addScores(result, rawScore);
		}
		return result;
	}
	
	/**
	 * 先判断是不是情感词，如果不是再判断是不是评价词，如果再不是，看看是不是语气词
	 * 如果是未知的，则
	 * 以后算法调整基本只需要调整这里，类别主要是两类或者三类，及positive、negative
	 * @param wordList
	 * @return
	 */
	private Map<String, Double> judgeNGramList(List<String> wordList) {
		int size = wordList.size();
		double pos = 0.0D;
		double neg = 0.0D;
		for (int i = 0; i < size; i++) {
			WordType wType = getWordType(wordList.get(i));
			//LOGGER.info("word = {} $$ wordType = {}", wordList.get(i), wType);
			switch (wType) {
				case NEUTRAL_WORD:
					double weight = getWordWeight(wordList.get(i));
					if (weight < 0) {
						neg -= weight;
					} else {
						pos += weight;
					}
					break;
				case POS: 
					pos += Constants.NORMAL_WEIGHT;
					break;
				case NEG:
					neg += Constants.NORMAL_WEIGHT;
					break;
				case POS_SEM:
					pos += Constants.NORMAL_SEMANTIC_WEIGHT;
					break;
				case POS_COMMENT:
					pos += Constants.COMMENT_WEIGHT;
					break;
				case NEG_SEM:
					neg += Constants.NORMAL_SEMANTIC_WEIGHT;
					break;
				case NEG_COMMENT:
					neg += Constants.COMMENT_WEIGHT;
					break;
				case TONE:
					for (int j = i + 1; j < size;) { //语气词，想下搜索一个，进行语气的判断
						String word = wordList.get(j);
						WordType type = getWordType(word);
						if (type == WordType.STOP_WORD || type == WordType.NEUTRAL_WORD) {
							break;
						}
						if (type == WordType.POS || type == WordType.POS_COMMENT || type == WordType.POS_SEM) {
							pos += getWordWeight(word) * Constants.TONE_WEIGHT;
						}
						if (type == WordType.NEG || type == WordType.NEG_COMMENT || type == WordType.NEG_SEM) {
							neg += getWordWeight(word) * Constants.TONE_WEIGHT;
						}
						i = j;
						break;
					}
					break;
				default:
					break;
			}
		}
		Map<String, Double> result = new HashMap<String, Double>();
		result.put(POS_TAG, pos);
		result.put(NEG_TAG, neg);
		return result;
	}
	
	private Double getWordWeight(String word) {
		WordType wType = getWordType(word);
		if (wType == WordType.POS || wType == WordType.NEG) {
			return Constants.NORMAL_WEIGHT;
		}
		if (wType == WordType.POS_SEM || wType == WordType.NEG_SEM) {
			return Constants.NORMAL_SEMANTIC_WEIGHT;
		}
		if (wType == WordType.POS_COMMENT || wType == WordType.NEG_COMMENT) {
			return Constants.COMMENT_WEIGHT;
		}
		if (wType == WordType.NEUTRAL_WORD) {
			double posPMIScore = wordLoader.getPosPMIScoreOfWord(word);
			double negPMIScore = wordLoader.getNegPMIScoreOfWord(word);
			if (posPMIScore == 0.0D) {
				return -Constants.NORMAL_WEIGHT * negPMIScore;
			}
			if (negPMIScore == 0.0D) {
				return Constants.NORMAL_WEIGHT * posPMIScore;
			}
			if (posPMIScore/negPMIScore < 0.68D) {
				return -Constants.NORMAL_WEIGHT * (negPMIScore - posPMIScore);
			}
			if (negPMIScore/posPMIScore < 0.68D) {
				return Constants.NORMAL_WEIGHT * (posPMIScore - negPMIScore);
			}
		}
		return 0.0D;
	}
	
	private WordType getWordType(String word) {
		if (wordLoader.isPosSenWord(word)) {
			return WordType.POS;
		}
		if (wordLoader.isPosSemWord(word)) {
			return WordType.POS_SEM;
		}
		if (wordLoader.isPosCommentWord(word)) {
			return WordType.POS_COMMENT;
		}
		if (wordLoader.isNegSenWord(word)) {
			return WordType.NEG;
		}
		if (wordLoader.isNegSemWord(word)) {
			return WordType.NEG_SEM;
		}
		if (wordLoader.isNegCommentWord(word)) {
			return WordType.NEG_COMMENT;
		}
		if (wordLoader.isToneWords(word)) {
			return WordType.TONE;
		}
		if (wordLoader.isStopWord(word)) {
			return WordType.STOP_WORD;
		}
		return WordType.NEUTRAL_WORD;	
	}
	
	private Map<String, Double> addScores(Map<String, Double> first, Map<String, Double> second) {
		Map<String, Double> result = new HashMap<String, Double>();
		result.put(POS_TAG, 0.0D);
		result.put(NEG_TAG, 0.0D);
		double score = 0.0D;
		if (first.containsKey(POS_TAG) && second.containsKey(POS_TAG)) {
			score = first.get(POS_TAG) + second.get(POS_TAG);
			result.put(POS_TAG, score);
		}
		if (first.containsKey(NEG_TAG) && second.containsKey(NEG_TAG)) {
			score = first.get(NEG_TAG) + second.get(NEG_TAG);
			result.put(NEG_TAG, score);
		}
		return result;
	}
	
	public SentimentType judgeSenType(Map<String, Double> score) {
		SentimentType result = SentimentType.NEUTRAL;
		if (score.isEmpty()) {
			return result;
		}
		double pos = score.get(POS_TAG) + 0.0000001D;
		double neg = score.get(NEG_TAG) + 0.0000001D;
		double res = (Math.abs(pos-neg)/(pos+neg));
		if (res > Constants.SEN_THRESHOLD) {
			if (pos >= neg) {
				result = SentimentType.POSITIVE;
			} else {
				result = SentimentType.NEGATIVE;
			}	
		} else {
			result = SentimentType.NEUTRAL;
		}
		//LOGGER.info("score = {}, value = {}, type = {}", score.toString(), res, result);
		return result;
	}
	
	
	public static void main(String[] args) {
		Classifier cls = new Classifier();
	
		String text = "阿西克换布泽尔加选秀权";
		Map<String, Double> result = cls.getJudgments(text);
		System.out.printf("positive: %f\n", result.get(POS_TAG));
		System.out.printf("negitive: %f\n", result.get(NEG_TAG));
		SentimentType type = cls.judgeSenType(result);
		System.out.printf("type: %d\n", type.value());
		
	}
		
}
