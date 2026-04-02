package com.Blaike1x.JavaContent.RAG.JavaContent_RAG.LoadVectorDatabase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

@Service
public class LoadDataFunctions {

    private static final Logger logger = LoggerFactory.getLogger(LoadDataFunctions.class);
    private final VectorStore vectorStore;

    @Value("${etl.pipeline.DataStorage.dataPath}")
    private String dataPath;

    public LoadDataFunctions(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Reads a single document from the given file path, splits it into chunks,
     * and loads the chunks into the vector database.
     */
    public void loadDocument(String filePath) {

        FileSystemResource resource = new FileSystemResource(filePath);
        if (!resource.exists() || !resource.isReadable())
        {
            logger.error("Tika Runner: Documents folder does not exist or is not a directory: {} ", filePath);
        }
        
        logger.info("----> Reading Filepath: {}", filePath);

        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();

        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        List<Document> chunks = splitter.apply(documents);

        vectorStore.add(chunks);
        logger.info("---> Added chunks to vector store < ---- ");
    }

    /**
     * Reads all documents from the configured data directory and loads them
     * into the vector database.
     */
    public void loadAllDocuments() {
        File dataDir = new File(dataPath.trim());
        logger.info("----> Fetching files from path: {} ", dataDir);
        File[] files = dataDir.listFiles();
        if (files == null) {
            return;
        }

        List<Document> allChunks = new ArrayList<>();
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();

        for (File file : files) {
            if (file.isFile()) {
                logger.info("$$--> Parsing file : {}", file.getName());
                FileSystemResource resource = new FileSystemResource(file);
                TikaDocumentReader reader = new TikaDocumentReader(resource);
                List<Document> documents = reader.get();
                allChunks.addAll(splitter.apply(documents));
                logger.info("Added chunks to allChunks list");
            }
        }

        if (!allChunks.isEmpty()) {
            vectorStore.add(allChunks);
        }
    }

    /**
     * Walks the configured data directory recursively, filters for supported
     * file extensions, reads each matching file, and loads all chunks into
     * the vector database.
     */
    public void loadDocumentsIntoVectorDatabase() {
        List<String> supportedExtensions = List.of(".pdf", ".doc", ".docx", ".txt", ".md", ".html", ".pptx");

        Path rootDir = Path.of(dataPath.trim());
        if (!Files.exists(rootDir) || !Files.isDirectory(rootDir)) {
            logger.error("Data directory does not exist or is not a directory: {}", rootDir);
            return;
        }

        logger.info("----> Walking directory: {}", rootDir);

        List<Document> allChunks = new ArrayList<>();
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();

        try (Stream<Path> paths = Files.walk(rootDir)) {
            List<Path> matchingFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return supportedExtensions.stream().anyMatch(fileName::endsWith);
                    })
                    .toList();

            logger.info("Found {} supported files", matchingFiles.size());

            for (Path filePath : matchingFiles) {
                logger.info("$$--> Parsing file: {}", filePath.getFileName());
                FileSystemResource resource = new FileSystemResource(filePath);
                TikaDocumentReader reader = new TikaDocumentReader(resource);
                List<Document> documents = reader.get();
                allChunks.addAll(splitter.apply(documents));
            }
        } catch (IOException e) {
            logger.error("Error walking directory: {}", rootDir, e);
            return;
        }

        if (!allChunks.isEmpty()) {
            vectorStore.add(allChunks);
            logger.info("---> Added {} chunks to vector store <----", allChunks.size());
        } else {
            logger.info("No documents found to load");
        }
    }

    /**
     * Reads a document from a file path and returns the chunked documents
     * without loading them into the vector store.
     */
    public List<Document> readAndSplitDocument(String filePath) {
        FileSystemResource resource = new FileSystemResource(filePath);
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();

        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        return splitter.apply(documents);
    }
}
