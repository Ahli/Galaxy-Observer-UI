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
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

/**
 * TODO improve, it is ba
 *
 * @author Ahli
 */
public final class XmlCompressorVtd {
	public static final String NAME_XPATH = "//name";
	private static final String COMMENT_XPATH = "//comment()";
	private static final String AHLI_SETTING = "@setting";
	private static final String AHLI_HOTKEY = "@hotkey";
	private static final Logger logger = LogManager.getLogger(XmlCompressorVtd.class);
	private static final String TEXT_XPATH = "//text()";
	
	/**
	 *
	 */
	private XmlCompressorVtd() {
	}
	
	/**
	 * @param cachePath
	 * @param ignoreCommentCountPerFile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void processCache(final String cachePath, final int ignoreCommentCountPerFile) {
		
		logger.info("Compressing XML files...");
		logger.trace("cachePath: {}", () -> cachePath);
		
		final Collection<File> filesOfCache = FileUtils.listFiles(new File(cachePath), null, true);
		
		final VTDGen vtd = new VTDGen();
		
		for (final File curFile : filesOfCache) {
			logger.trace("compression - processing file: {}", () -> curFile.getPath());
			
			try {
				// setdoc causes a nullpointer error due to an internal bug
				vtd.setDoc_BR(Files.readAllBytes(curFile.toPath()));
				vtd.parse(false);
			} catch (final IOException | ParseException e) {
				continue;
			}
			
			final VTDNav nav = vtd.getNav();
			final AutoPilot ap = new AutoPilot(nav);
			try {
				final XMLModifier xm = new XMLModifier(nav);
				// remove comment nodes except first one
				ap.selectXPath(COMMENT_XPATH);
				for (int i = 0; i < ignoreCommentCountPerFile; ++i) {
					ap.evalXPath();
				}
				int i;
				String comment;
				while ((i = ap.evalXPath()) != -1) {
					comment = nav.toRawStringLowerCase(i);
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
					final int nos = (int) l;
					xm.updateToken(i, nav, nos, nlen);
				}
				
				xm.output(curFile.getAbsolutePath());
			} catch (final ModifyException | XPathParseException | XPathEvalException | NavException | IOException | TranscodeException e) {
				logger.error("ERROR: while compressing xml file " + curFile.getAbsolutePath(), e);
			}
		}
	}
	
}
