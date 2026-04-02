package com.Blaike1x.JavaContent.RAG.JavaContent_RAG;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.Blaike1x.JavaContent.RAG.JavaContent_RAG.LoadVectorDatabase.LoadDataFunctions;

@SpringBootApplication
public class JavaContentRagApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaContentRagApplication.class, args);
	}

	@Bean
	CommandLineRunner loadVectorData(LoadDataFunctions loadRagDataFunctions) {
		return args -> loadRagDataFunctions.loadDocumentsIntoVectorDatabase();
	}

}
