package fr.univnantes.lina.uima.textSegmenter;

/** 
 * UIMA Text Segmenter
 * Copyright (C) 2010, 2011  Nicolas Hernandez
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.HashMap;
import java.util.Vector;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import fr.univnantes.lina.uima.textSegmenter.types.SegmentAnnotation;
import fr.univnantes.lina.uima.util.AnalysisEngine;
import fr.univnantes.lina.uima.util.UIMAUtils;


import uk.ac.man.cs.choif.extend.Debugx;
import uk.ac.man.cs.choif.nlp.surface.Stopword;
import java.io.File;


/**
 * Annotator engine which acts as an abstract class for text segmenter AE implementation such as C99 and JTextTile
 * Declares common parameters input/output view/annotation/feature and stopword file 
 * 
 * @author hernandez 
 *
 */
public class TextSegmenterAE extends AnalysisEngine {
	
	/*
	 * Properties param and default values 
	 */
	private final static String INPUT_SENTENCE_ANNOTATION_PARAM = "InputSentenceAnnotation";
	private final static String INPUT_WORD_ANNOTATION_PARAM = "InputWordAnnotation";
	private final static String INPUT_WORD_FEATURE_ANNOTATION_PARAM = "InputWordFeature";
	private final static String OUTPUT_SEGMENT_ANNOTATION_PARAM = "OutputSegmentAnnotation";
	private final static String STOPWORDFILE_PARAM = "StopWordFile";
	
	/**
	 * Default sentence annotation type value 
	 */
	private final static String SENTENCE_TYPE = "org.apache.uima.SentenceAnnotation";

	/**
	 * Default token annotation type value 
	 */
	private final static String TOKEN_TYPE="org.apache.uima.TokenAnnotation";

	/**
	 * Default token feature name
	 */
	private final static String DEFAULT_TOKEN_FEATURE_NAME= "coveredText" ; //""; stem


	/**
	 * Default segment annotation type value 
	 */
	private final static String SEGMENT_TYPE="fr.univnantes.lina.uima.textSegmenter.types.SegmentAnnotation";


	/*
	 * 
	 */
	
	/**
	 * 
	 */
	private String sentenceAnnotationType = SENTENCE_TYPE;

	
	
	private String tokenAnnotationType = TOKEN_TYPE;
	/**
	 * 
	 */
	private String tokenFeature ;
	
	/**
	 * 
	 */
	private String outputSegmentAnnotation;
	
	
	/**
	 * 
	 */
	private Stopword stopWord;

	/*
	 * Text segmenter specificities
	 */
	

	
	/*
	 * Accessors
	 */

	
	/**
	 * @return the sentenceAnnotationType
	 */
	public String getSentenceAnnotationType() {
		return sentenceAnnotationType;
	}

	/**
	 * @param sentenceAnnotationType the sentenceAnnotationType to set
	 */
	public void setSentenceAnnotationType(String sentenceAnnotationType) {
		this.sentenceAnnotationType = sentenceAnnotationType;
	}

	/**
	 * @return the tokenAnnotationType
	 */
	public String getTokenAnnotationType() {
		return tokenAnnotationType;
	}

	/**
	 * @param tokenAnnotationType the tokenAnnotationType to set
	 */
	public void setTokenAnnotationType(String tokenAnnotationType) {
		this.tokenAnnotationType = tokenAnnotationType;
	}



	/**
	 * @return the outputSegmenAnnotation
	 */
	protected String getOutputSegmentAnnotation() {
		return outputSegmentAnnotation;
	}

	/**
	 * @param outputSegmenAnnotation the outputSegmenAnnotation to set
	 */
	protected void setOutputSegmentAnnotation(String outputSegmentAnnotation) {
		this.outputSegmentAnnotation = outputSegmentAnnotation;
	}


	protected void setStopWord(File file) throws Exception {
		if (file.exists()) {
			if (file.isFile()) {
				this.stopWord = new Stopword(file);
			} else {
				String msg= "This file " + file;
				msg += " should have been a normal file instead of a directory.";
				throw new Exception (msg);
			}
		} else {
			String msg= "This file " + file + " doesn't exist.";
			throw new Exception (msg);
		}
	}

	protected void setStopWord(String path) throws Exception {
		File file = new File(path);
		this.setStopWord(file);
	}

	protected Stopword getStopWord() {
		return this.stopWord;
	}


	/**
	 * @return the tokenFeature
	 */
	protected String getTokenFeature() {
		return tokenFeature;
	}

	/**
	 * @param tokenFeature the tokenFeature to set
	 */
	protected void setTokenFeature(String tokenFeature) {
		this.tokenFeature = tokenFeature;
	}

	
	
	/*
	 * Methods 
	 */
	
	/**
	 * Get the parameter values
	 */
	public void initialize(UimaContext context) throws ResourceInitializationException {

		// generic AE parameters
		super.initialize(context);

		// current AE parameter
		try {
			String stringParameter = (String) context.getConfigParameterValue(INPUT_SENTENCE_ANNOTATION_PARAM);
			if (stringParameter != null) this.setSentenceAnnotationType(stringParameter);
			stringParameter = (String) context.getConfigParameterValue(INPUT_WORD_ANNOTATION_PARAM);
			if (stringParameter != null) this.setTokenAnnotationType(stringParameter);

			String tokenFeatureName = (String) context.getConfigParameterValue(INPUT_WORD_FEATURE_ANNOTATION_PARAM);
			if (stringParameter != null) { 
				//System.out.println("Debug: setTokenFeature with PARAMETER VALUE "+ tokenFeatureName);
				this.setTokenFeature(tokenFeatureName);
				} 
			else{
				//System.out.println("Debug: setTokenFeature with DEFAULT "+ DEFAULT_TOKEN_FEATURE_NAME);
				this.setTokenFeature(DEFAULT_TOKEN_FEATURE_NAME);
				}
			
			//System.out.println("Debug: getTokenFeature() "+this.getTokenFeature());

			String outputSegmentAnnotation = (String) context.getConfigParameterValue(OUTPUT_SEGMENT_ANNOTATION_PARAM);
			if (outputSegmentAnnotation != null) { this.setOutputSegmentAnnotation(outputSegmentAnnotation);} else
			{this.setOutputSegmentAnnotation(SEGMENT_TYPE);}

			String path = (String) context.getConfigParameterValue(STOPWORDFILE_PARAM);
			this.setStopWord(path);

		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}


}




