package com.benayed.mailing.assets.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class FileUtils {

	public Path getFileRespectingAPredicateFromDirectory(Path dir, Predicate<Path> filePattern) {
		Assert.notNull(filePattern, "file pattern is null, cannot walk directory");
		Assert.notNull(dir, "cannot walk a null directory");
		Assert.isTrue(Files.isDirectory(dir), "input path is not a directory, cannot walk it !");
		
	    try (Stream<Path> stream = Files.walk(dir, 1)) {
	    	return stream
	          .filter(file -> !Files.isDirectory(file))
	          .filter(filePattern)
	          .findFirst()
	          .orElse(null);
	    }catch(IOException e) {
	    	throw new UncheckedIOException(e);
	    }
	}
	
	public boolean doesFileNameContainDomains(Path path) {
		return Optional.<Path>ofNullable(path)
		.filter(p -> !Files.isDirectory(p))
		.map(Path::getFileName)
		.map(Path::toString)
		.map(String::toLowerCase)
		.map(fname -> fname.contains("domains"))
		.orElse(false);
	}
	
	public boolean doesFileNameNotContainDomains(Path path) {
		return !doesFileNameContainDomains(path);
	}
	
	public List<Path> unzip(Path zipFile, Path unzipDestination) {
		Assert.notNull(zipFile, "cannot unzip null file !");
		Assert.notNull(unzipDestination, "unzip destination must not be null");
		
		try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
			Files.createDirectories(unzipDestination);
            ZipEntry entry;
            List<Path> entriesPaths = new ArrayList<Path>();
            
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final Path toPath = unzipDestination.resolve(entry.getName());
                if (!entry.isDirectory()) {
                    Files.copy(zipInputStream, toPath);
                } else {
                    Files.createDirectory(toPath);
                }
                 
                entriesPaths.add(toPath);
            }
            Files.delete(zipFile);
            return entriesPaths;
        } catch (IOException e) {
        	throw new UncheckedIOException(e);
        }
	}
}
