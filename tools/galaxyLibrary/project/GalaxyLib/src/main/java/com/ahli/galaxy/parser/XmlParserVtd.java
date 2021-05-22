// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.parser;

import com.ahli.galaxy.parser.abstracts.XmlParserAbstract;
import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.ui.exception.UIException;
import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class XmlParserVtd extends XmlParserAbstract {
	private static final String ANY_TAG = "*";
	private static final Logger logger = LoggerFactory.getLogger(XmlParserVtd.class);
	private VTDGen vtd;
	
	private List<String> attrTypes;
	private List<String> attrValues;
	
	/**
	 *
	 */
	public XmlParserVtd() {
		super(null);
	}
	
	/**
	 * @param consumer
	 */
	public XmlParserVtd(final ParsedXmlConsumer consumer) {
		super(consumer);
		init();
	}
	
	private void init() {
		if (vtd == null) {
			vtd = new VTDGen();
		}
		attrTypes = new ArrayList<>(3);
		attrValues = new ArrayList<>(3);
	}
	
	@Override
	public void setConsumer(final ParsedXmlConsumer consumer) {
		this.consumer = consumer;
		init();
	}
	
	@Override
	public void clear() {
		vtd = null;
		attrTypes = null;
		attrValues = null;
		consumer = null;
	}
	
	@Override
	public void parseFile(final Path p) throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("parsing layout file: {}", p.getFileName());
		}
		try {
			// setdoc causes a nullpointer error due to an internal bug => use byte array
			vtd.setDoc_BR(Files.readAllBytes(p));
			vtd.parse(false);
			final VTDNav nav = vtd.getNav();
			final AutoPilot ap = new AutoPilot(nav);
			ap.selectElement(ANY_TAG);
			final AutoPilot ap2 = new AutoPilot(nav);
			int i;
			while (ap.iterate()) {
				ap2.selectAttr(ANY_TAG);
				while ((i = ap2.iterateAttr()) != -1) {
					// i will be attr name, i+1 will be attribute value
					attrTypes.add(nav.toRawStringLowerCase(i));
					attrValues.add(nav.toRawString(i + 1));
				}
				consumer.parse(
						nav.getCurrentDepth() + 1,
						nav.toRawStringLowerCase(nav.getCurrentIndex()),
						attrTypes,
						attrValues);
				attrTypes.clear();
				attrValues.clear();
			}
		} catch (final IOException | UIException | ParseException | NavException e) {
			throw new IOException("ERROR during XML parsing.", e);
		}
		consumer.endLayoutFile();
	}
	
}
