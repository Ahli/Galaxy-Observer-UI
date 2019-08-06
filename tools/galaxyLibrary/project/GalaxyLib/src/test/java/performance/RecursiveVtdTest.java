// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package performance;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RecursiveVtdTest {
	private static final List<Object> list = new ArrayList<>();
	private static final String TAG = "*";
	
	public static void main(final String[] args) {
		final long endMem;
		int iterations = 0;
		try {
			Thread.sleep(1_000);
		} catch (final InterruptedException e1) {
			e1.printStackTrace();
			Thread.currentThread().interrupt();
		}
		System.out.println("memory: " + Runtime.getRuntime().totalMemory());
		final Runtime rt = Runtime.getRuntime();
		final long startMem = rt.totalMemory() - rt.freeMemory();
		final long startTime = System.currentTimeMillis();
		final Path p = Paths.get(
				"D:\\Galaxy-Observer-UI\\baseUI\\heroes\\mods\\core.stormmod\\base.stormdata\\UI\\Layout\\UI\\GameUI.StormLayout");
		final VTDGen vtd;
		
		try {
			vtd = new VTDGen();
			// for (int i = 0; i < 1000; i++) {
			while (System.currentTimeMillis() - startTime < 60_000) {
				list.clear();
				loadRecursiveXML(vtd, p);
				iterations++;
				// if (i % 100 == 0) {
				// endMem = rt.totalMemory() - rt.freeMemory();
				// System.out.println("Memory Use: " + ((float) endMem - startMem) / (1 << 20) +
				// " MB.");
				// }
			}
		} catch (final NavException | IOException | ParseException e) {
			e.printStackTrace();
		}
		
		final long executionTime = System.currentTimeMillis() - startTime;
		endMem = rt.totalMemory() - rt.freeMemory();
		if (iterations != 0) {
			System.out.println(
					"recursive traversal took " + executionTime + "ms. Per iteration: " + executionTime / iterations +
							"ms.");
		} else {
			System.out.println("no iterations performed");
		}
		System.out.println("elements: " + list.size());
		System.out.println("Memory Use: " + ((float) endMem - startMem) / (1 << 20) + " MB.");
		System.out.println("iterations: " + iterations);
		
		try {
			Thread.sleep(1_000);
		} catch (final InterruptedException e1) {
			e1.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}
	
	public static void loadRecursiveXML(final VTDGen vtd, final Path p)
			throws NavException, IOException, ParseException {
		// long startTime = System.currentTimeMillis();
		// if (!vtd.parseFile(f.getPath(), false)) {
		// return;
		// }
		// final FileInputStream fs = new FileInputStream(f);
		// final FileChannel channel = fs.getChannel();
		// final MappedByteBuffer buffer1 = channel.map(FileChannel.MapMode.READ_ONLY,
		// 0, channel.size());
		// final ByteBuffer buffer2 = ByteBuffer.allocate((int) channel.size());
		// channel.read(buffer2);
		
		// setdoc causes a nullpointer error due to an internal bug
		vtd.setDoc_BR(Files.readAllBytes(p));
		vtd.parse(false);
		// vtd.setDoc_BR(bytes);
		// channel.close();
		// fs.close();
		
		// long executionTime = (System.currentTimeMillis() - startTime);
		// System.out.println("file loading took " + executionTime + "ms.");
		// startTime = System.currentTimeMillis();
		
		final VTDNav nav = vtd.getNav();
		final AutoPilot ap = new AutoPilot(nav);
		
		// ap.enableCaching(false);
		ap.selectElement(TAG);
		final AutoPilot ap2 = new AutoPilot(nav);
		// ap2.enableCaching(false);
		
		// executionTime = (System.currentTimeMillis() - startTime);
		// System.out.println("creating navigation objects took " + executionTime +
		// "ms.");
		// startTime = System.currentTimeMillis();
		int n;
		String elName;
		int i;
		String attrName;
		String attrVal;
		int d;
		while (ap.iterate()) {
			n = nav.getCurrentIndex();
			d = nav.getCurrentDepth();
			// elName = nav.toStringLowerCase(n);
			elName = nav.toRawStringLowerCase(n);
			list.add(d);
			list.add(elName);
			// executionTime = (System.currentTimeMillis() - startTime);
			// System.out.println(n + " - level" + d + " - " + elName + " @ " +
			// executionTime);
			// System.out.println(n + " - level" + d + " - " + elName);
			ap2.selectAttr(TAG);
			while ((i = ap2.iterateAttr()) != -1) {
				// i will be attr name, i+1 will be attribute value
				attrName = nav.toRawStringLowerCase(i);
				attrVal = nav.toRawString(i + 1);
				list.add(attrName);
				list.add(attrVal);
				// executionTime = (System.currentTimeMillis() - startTime);
				// System.out.println(attrName + "=" + attrVal + " @ " + executionTime);
			}
		}
	}
}
