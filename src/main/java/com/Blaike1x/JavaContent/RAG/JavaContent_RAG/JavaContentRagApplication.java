package com.Blaike1x.JavaContent.RAG.JavaContent_RAG;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.Blaike1x.JavaContent.RAG.JavaContent_RAG.LoadVectorDatabase.LoadDataFunctions;

@SpringBootApplication
public class JavaContentRagApplication {

	@Autowired
	private static LoadDataFunctions loadRagDataFunctions;

	
	public static void main(String[] args) {
		SpringApplication.run(JavaContentRagApplication.class, args);

		loadRagDataFunctions.loadAllDocuments();
		

	}

}
