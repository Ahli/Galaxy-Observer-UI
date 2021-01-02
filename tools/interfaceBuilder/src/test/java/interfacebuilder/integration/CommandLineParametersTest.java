// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CommandLineParametersTest {
	
	@Test
	void testStringParamCompileRun() {
		// TODO improve test with freshly created directory
		final CommandLineParams param = new CommandLineParams(
				"--compileRun=D:\\Galaxy-Observer-UI\\dev\\heroes\\AhliObs.StormInterface\\Base.StormData\\UI\\Layout");
		assertEquals(
				"D:\\Galaxy-Observer-UI\\dev\\heroes\\AhliObs.StormInterface",
				param.getParamCompilePath(),
				"compileRun's path is wrong");
		assertTrue(param.isHasParamCompilePath(), "hasParamCompilePath is wrong");
		assertNull(param.getParamRunPath(), "run path wrongfully set when command is: --compileRun");
	}
	
	@Test
	void testStringParamCompile() {
		// TODO improve test with freshly created directory
		final CommandLineParams param = new CommandLineParams(
				"--compile=D:\\Galaxy-Observer-UI\\dev\\heroes\\AhliObs.StormInterface\\Base.StormData\\UI\\Layout");
		assertEquals(
				"D:\\Galaxy-Observer-UI\\dev\\heroes\\AhliObs.StormInterface",
				param.getParamCompilePath(),
				"compile's path is wrong");
		assertTrue(param.isHasParamCompilePath(), "hasParamCompilePath is wrong");
		assertNull(param.getParamRunPath(), "run path wrongfully set when command is: --compile");
	}
	
	@Test
	void testStringParamRun() {
		// TODO improve test with freshly created directory
		final CommandLineParams param = new CommandLineParams("--run=D:\\Path\\To\\Some\\replayFile.SC2Replay");
		assertEquals("D:\\Path\\To\\Some\\replayFile.SC2Replay", param.getParamRunPath(), "run's path is wrong");
		assertFalse(param.isHasParamCompilePath(), "hasParamCompilePath is wrong");
		assertNull(param.getParamCompilePath(), "compile path wrongfully set when command is: --run");
	}
	
	@Test
	void testStringEmpty() {
		final CommandLineParams param = new CommandLineParams("");
		assertNull(param.getParamRunPath(), "run's path is wrong");
		assertFalse(param.isHasParamCompilePath(), "hasParamCompilePath is wrong");
		assertNull(param.getParamCompilePath(), "compile path is wrong");
	}
	
	// TODO test with the named params map, too
}
