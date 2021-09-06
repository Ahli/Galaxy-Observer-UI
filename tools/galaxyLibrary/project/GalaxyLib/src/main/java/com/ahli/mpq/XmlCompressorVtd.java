// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * TODO improve, it is bad at compressing
 *
 * @author Ahli
 */
public final class XmlCompressorVtd {
	public static final String NAME_XPATH = "//name";
	private static final String COMMENT_XPATH = "//comment()";
	private static final String AHLI_SETTING = "@setting";
	private static final String AHLI_HOTKEY = "@hotkey";
	private static final Logger logger = LoggerFactory.getLogger(XmlCompressorVtd.class);
	private static final String TEXT_XPATH = "//text()";
	
	/**
	 *
	 */
	private XmlCompressorVtd() {
	}
	
	/**
	 * @param cachePath
	 * @param ignoreCommentCountPerFile
	 * @throws IOException
	 */
	public static void processCache(final Path cachePath, final int ignoreCommentCountPerFile) throws IOException {
		
		logger.info("Compressing XML files...");
		logger.trace("cachePath: {}", cachePath);
		
		final VTDGen vtd = new VTDGen();
		final AutoPilot ap = new AutoPilot();
		final XMLModifier xm = new XMLModifier();
		
		final FileVisitor<Path> visitor = new XmlCompressorVtd.FileProcessor(vtd, ap, xm, ignoreCommentCountPerFile);
		Files.walkFileTree(cachePath, visitor);
	}
	
	private static final class FileProcessor extends SimpleFileVisitor<Path> {
		private final VTDGen vtd;
		private final AutoPilot ap;
		private final XMLModifier xm;
		private final int ignoreCommentCountPerFile;
		
		public FileProcessor(
				final VTDGen vtd, final AutoPilot ap, final XMLModifier xm, final int ignoreCommentCountPerFile) {
			this.vtd = vtd;
			this.ap = ap;
			this.xm = xm;
			this.ignoreCommentCountPerFile = ignoreCommentCountPerFile;
		}
		
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
			
			logger.trace("compression - processing file: {}", file);
			
			try {
				// setDoc causes a nullpointer error due to an internal bug
				vtd.setDoc_BR(Files.readAllBytes(file));
				vtd.parse(false);
			} catch (final IOException | ParseException e) {
				logger.trace("Error while compressing xml.", e);
				return FileVisitResult.CONTINUE;
			}
			
			try {
				final VTDNav nav = vtd.getNav();
				ap.bind(nav);
				xm.bind(nav);
				// remove comment nodes except first one
				ap.selectXPath(COMMENT_XPATH);
				for (int i = 0; i < ignoreCommentCountPerFile; ++i) {
					ap.evalXPath();
				}
				int i;
				while ((i = ap.evalXPath()) != -1) {
					final String comment = nav.toRawStringLowerCase(i);
					if (!comment.contains(AHLI_HOTKEY) && !comment.contains(AHLI_SETTING)) {
						xm.remove();
					}
				}
				// remove ext
				ap.resetXPath();
				ap.selectXPath(NAME_XPATH);
				while ((ap.evalXPath()) != -1) {
					xm.remove(nav.expandWhiteSpaces(nav.getElementFragment()));
				}
				
				// trim whitespace of text values
				ap.resetXPath();
				ap.selectXPath(TEXT_XPATH);
				while ((i = ap.evalXPath()) != -1) {
					final int offset = nav.getTokenOffset(i);
					final int len = nav.getTokenLength(i);
					final long l = nav.trimWhiteSpaces((((long) len) << 32) | offset, VTDNav.WS_BOTH);
					final int nlen = (int) (l >> 32);
					final int nos = (int) l; // overflowing is desired behavior here
					xm.updateToken(i, nav, nos, nlen);
				}
				
				try (final OutputStream outputStream = Files.newOutputStream(file)) {
					xm.output(outputStream);
				}
			} catch (final ModifyException | XPathParseException | XPathEvalException | NavException | IOException | TranscodeException e) {
				logger.error("ERROR: while compressing xml file {}", file, e);
			}
			
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
			logger.error("Failed to access file: {}", file);
			throw exc;
		}
	}
}
