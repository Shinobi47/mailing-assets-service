package com.benayed.mailing.assets.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
}
