package com.ahli.files;

import lombok.Getter;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@Getter
public class FileCountingVisitor extends SimpleFileVisitor<Path> {
	private int count = 0;
	
	public FileCountingVisitor() {
		// explicit definition to avoid compiler warning of exposing default constructor
	}
	
	@Override
	public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
		++count;
		return FileVisitResult.CONTINUE;
	}
	
}
