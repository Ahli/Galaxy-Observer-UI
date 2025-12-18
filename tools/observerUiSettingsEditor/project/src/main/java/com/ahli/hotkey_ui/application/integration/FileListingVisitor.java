package com.ahli.hotkey_ui.application.integration;

import org.jspecify.annotations.NonNull;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileListingVisitor extends SimpleFileVisitor<Path> {
	private final List<Path> paths = new ArrayList<>();
	
	@Override
	public @NonNull FileVisitResult visitFile(final @NonNull Path file, final @NonNull BasicFileAttributes attrs) {
		paths.add(file);
		return FileVisitResult.CONTINUE;
	}
	
	public List<Path> getFilePaths() {
		return paths;
	}
	
}
