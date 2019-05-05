// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration;

import interfacebuilder.InterfaceBuilderApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith (MockitoExtension.class)
@SpringBootTest (classes = InterfaceBuilderApp.class)
public class CommandLineParametersTest {
	
	@Test
	public void testStringParamCompileRun() {
		// TODO improve test with freshly created directory
		final CommandLineParams param = new CommandLineParams(
				"--compileRun=D:\\Galaxy-Observer-UI\\dev\\heroes\\AhliObs.StormInterface\\Base.StormData\\UI\\Layout");
		assertEquals("D:\\Galaxy-Observer-UI\\dev\\heroes\\AhliObs.StormInterface", param.getParamCompilePath(),
				"compileRun's path is wrong");
		assertTrue(param.isHasParamCompilePath(), "hasParamCompilePath is wrong");
		assertNull(param.getParamRunPath(), "run path wrongfully set when command is: --compileRun");
	}
	
	@Test
	public void testStringParamCompile() {
		// TODO improve test with freshly created directory
		final CommandLineParams param = new CommandLineParams(
				"--compile=D:\\Galaxy-Observer-UI\\dev\\heroes\\AhliObs.StormInterface\\Base.StormData\\UI\\Layout");
		assertEquals("D:\\Galaxy-Observer-UI\\dev\\heroes\\AhliObs.StormInterface", param.getParamCompilePath(),
				"compile's path is wrong");
		assertTrue(param.isHasParamCompilePath(), "hasParamCompilePath is wrong");
		assertNull(param.getParamRunPath(), "run path wrongfully set when command is: --compile");
	}
	
	@Test
	public void testStringParamRun() {
		// TODO improve test with freshly created directory
		final CommandLineParams param = new CommandLineParams("--run=D:\\Path\\To\\Some\\replayFile.SC2Replay");
		assertEquals("D:\\Path\\To\\Some\\replayFile.SC2Replay", param.getParamRunPath(), "run's path is wrong");
		assertFalse(param.isHasParamCompilePath(), "hasParamCompilePath is wrong");
		assertNull(param.getParamCompilePath(), "compile path wrongfully set when command is: --run");
	}
	
	@Test
	public void testStringEmpty() {
		final CommandLineParams param = new CommandLineParams("");
		assertNull(param.getParamRunPath(), "run's path is wrong");
		assertFalse(param.isHasParamCompilePath(), "hasParamCompilePath is wrong");
		assertNull(param.getParamCompilePath(), "compile path is wrong");
	}
	
	// TODO test with the named params map, too
}
