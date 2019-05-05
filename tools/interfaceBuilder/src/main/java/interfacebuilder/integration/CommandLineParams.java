// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration;

import javafx.application.Application;

import java.io.File;
import java.util.Map;

public class CommandLineParams {
	public static final String PARAM_PREFIX = "--";
	private static final String COMPILE_RUN = "compileRun";
	private static final String COMPILE = "compile";
	private static final String EQUAL = "=";
	private static final String RUN = "run";
	private final String paramRunPath;
	private final boolean compileAndRun;
	private final boolean hasParamCompilePath;
	private final String paramCompilePath;
	private final boolean wasStartedWithParameters;
	private boolean paramsOriginateFromExternalSource;
	
	public CommandLineParams(final String... params) {
		wasStartedWithParameters = (params.length > 0);
		String paramCompilePathTmp = getParamsValue(params, PARAM_PREFIX + COMPILE_RUN + EQUAL);
		if (paramCompilePathTmp != null) {
			compileAndRun = true;
		} else {
			paramCompilePathTmp = getParamsValue(params, PARAM_PREFIX + COMPILE + EQUAL);
			compileAndRun = false;
		}
		paramCompilePath = getInterfaceRootFromPath(paramCompilePathTmp);
		hasParamCompilePath = (paramCompilePath != null);
		paramRunPath = getParamsValue(params, PARAM_PREFIX + RUN + EQUAL);
	}
	
	/**
	 * Returns a value for a specified parameter.
	 *
	 * @param params
	 * 		parameter Strings with parameter with equal sign and value
	 * @param paramNameAndEqualSign
	 * 		parameter name and equal sign
	 * @return String following the equal sign for the specified parameter
	 */
	private static String getParamsValue(final String[] params, final String paramNameAndEqualSign) {
		for (final String param : params) {
			if (param.startsWith(paramNameAndEqualSign)) {
				return param.substring(paramNameAndEqualSign.length());
			}
		}
		return null;
	}
	
	/**
	 * ParamPath might be some layout or folder within the interface, so it is cut down to the interface base path.
	 *
	 * @param path
	 * 		interfacebuilder's compileParam's value
	 * @return shortens the path to the Interface root folder
	 */
	private static String getInterfaceRootFromPath(final String path) {
		String str = path;
		if (path != null) {
			while (str.length() > 0 && !str.endsWith("Interface")) {
				final int lastIndex = str.lastIndexOf(File.separatorChar);
				if (lastIndex != -1) {
					str = str.substring(0, lastIndex);
				} else {
					return null;
				}
			}
		}
		return str;
	}
	
	public CommandLineParams(final Application.Parameters params) {
		final Map<String, String> namedParams = params.getNamed();
		// named params
		// e.g. "--paramname=value".
		wasStartedWithParameters = !namedParams.isEmpty();
		
		// COMPILE / COMPILERUN PARAM
		// --compile="D:\GalaxyObsUI\dev\heroes\AhliObs.StormInterface"
		String paramCompilePathTmp = namedParams.get(COMPILE_RUN);
		if (paramCompilePathTmp != null) {
			compileAndRun = true;
		} else {
			paramCompilePathTmp = namedParams.get(COMPILE);
			compileAndRun = false;
		}
		paramCompilePath = getInterfaceRootFromPath(paramCompilePathTmp);
		hasParamCompilePath = (paramCompilePath != null);
		
		// RUN PARAM
		// --run="F:\Games\Heroes of the Storm\Support\HeroesSwitcher.exe"
		paramRunPath = namedParams.get(RUN);
	}
	
	public String getParamCompilePath() {
		return paramCompilePath;
	}
	
	public String getParamRunPath() {
		return paramRunPath;
	}
	
	public boolean isCompileAndRun() {
		return compileAndRun;
	}
	
	public boolean isWasStartedWithParameters() {
		return wasStartedWithParameters;
	}
	
	public boolean isHasParamCompilePath() {
		return hasParamCompilePath;
	}
	
	public boolean isParamsOriginateFromExternalSource() {
		return paramsOriginateFromExternalSource;
	}
	
	public void setParamsOriginateFromExternalSource(final boolean paramsOriginateFromExternalSource) {
		this.paramsOriginateFromExternalSource = paramsOriginateFromExternalSource;
	}
}
