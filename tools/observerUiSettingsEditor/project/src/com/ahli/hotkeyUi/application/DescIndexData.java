package com.ahli.hotkeyUi.application;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ahli.mpq.MpqInterface;

import javafx.util.Pair;

/**
 * 
 * @author Ahli
 *
 */
public class DescIndexData {
	static Logger LOGGER = LogManager.getLogger("DescIndexData");

	private String descIndexIntPath = null;
	private ArrayList<Pair<File, String>> fileIntPathList = new ArrayList<>();
	private Main main;
	private MpqInterface mpqi;

	public DescIndexData(Main main, MpqInterface mpqi) {
		this.main = main;
		this.mpqi = mpqi;
		fileIntPathList = new ArrayList<>();
	}

	public String getDescIndexIntPath() {
		return descIndexIntPath;
	}

	public void setDescIndexIntPath(String descIndexIntPath) {
		this.descIndexIntPath = descIndexIntPath;
	}

	public String getLayoutIntPath(int i) {
		return fileIntPathList.get(i).getValue();
	}

	public void addLayoutIntPath(String intPath) {
		String intPath2 = intPath;
		File f = mpqi.getCachedFile(intPath);
		if (!f.exists()) {
			// add base folder to the path
			intPath2 = (main.isHeroesFile() ? "Base.StormData" : "Base.SC2Data") + "/" + intPath;
			f = mpqi.getCachedFile(intPath2);
		}
		if (!f.exists()) {
			return;
		}
		Pair<File, String> p = new Pair<>(f, intPath2);
		fileIntPathList.add(p);
		LOGGER.debug("added Layout path: " + intPath2);
		LOGGER.debug("added File path: " + f.getAbsolutePath());
	}

	public boolean removeLayoutIntPath(String intPath) {
		for (int i = 0; i < fileIntPathList.size(); i++) {
			Pair<File, String> p = fileIntPathList.get(i);
			if (p.getValue().equals(intPath)) {
				fileIntPathList.remove(i);
				return true;
			}
		}
		return false;
	}

	public void clear() {
		fileIntPathList.clear();
	}

	public void setDescIndexPathAndClear(String descIndexPath) {
		clear();
		setDescIndexIntPath(descIndexPath);
	}

	public void addLayoutIntPath(Iterable<String> layoutPathList) {
		Iterator<String> it = layoutPathList.iterator();
		while (it.hasNext()) {
			this.addLayoutIntPath(it.next());
		}
	}

	public Main getMain() {
		return main;
	}

	public void setMain(Main main) {
		this.main = main;
	}

	public int getLayoutCount() {
		return fileIntPathList.size();
	}

	public File getLayoutFile(String intPath) {
		for (int i = 0; i < fileIntPathList.size(); i++) {
			Pair<File, String> p = fileIntPathList.get(i);
			if (p.getValue().equals(intPath)) {
				return p.getKey();
			}
		}
		return null;
	}

}
