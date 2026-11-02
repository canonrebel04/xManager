package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;

import android.graphics.BitmapFactory;
import java.io.File;
import java.util.ArrayList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class FileUtilTest {

	@Rule public TemporaryFolder tmp = new TemporaryFolder();

	private String path(String rel) {
		return new File(tmp.getRoot(), rel).getAbsolutePath();
	}

	@Test
	public void writeThenRead_roundTripsAndCreatesParents() {
		String p = path("a/b/c.txt");
		FileUtil.writeFile(p, "hello\nworld");
		assertTrue(FileUtil.isFile(p));
		assertTrue(FileUtil.isDirectory(path("a/b")));
		assertEquals("hello\nworld", FileUtil.readFile(p));
	}

	@Test
	public void writeFile_overwritesNotAppends() {
		String p = path("x.txt");
		FileUtil.writeFile(p, "first-long-content");
		FileUtil.writeFile(p, "second");
		assertEquals("second", FileUtil.readFile(p));
	}

	@Test
	public void readFile_missingFileIsCreatedEmpty() {
		String p = path("missing/new.txt");
		assertEquals("", FileUtil.readFile(p));
		assertTrue("readFile has the side-effect of creating the file", FileUtil.isExistFile(p));
	}

	@Test
	public void copyMoveAndLength() {
		String src = path("src.txt");
		String dst = path("out/dst.txt");
		FileUtil.writeFile(src, "12345");
		FileUtil.copyFile(src, dst);
		assertEquals("12345", FileUtil.readFile(dst));
		assertEquals(5, FileUtil.getFileLength(dst));

		String moved = path("moved.txt");
		FileUtil.moveFile(src, moved);
		assertFalse(FileUtil.isExistFile(src));
		assertEquals("12345", FileUtil.readFile(moved));
	}

	@Test
	public void copyFile_missingSourceIsNoop() {
		String dst = path("never.txt");
		FileUtil.copyFile(path("nope.txt"), dst);
		assertFalse(FileUtil.isExistFile(dst));
	}

	@Test
	public void copyDir_recursive() {
		FileUtil.writeFile(path("d1/f1.txt"), "1");
		FileUtil.writeFile(path("d1/sub/f2.txt"), "2");
		FileUtil.copyDir(path("d1"), path("d2"));
		assertEquals("1", FileUtil.readFile(path("d2/f1.txt")));
		assertEquals("2", FileUtil.readFile(path("d2/sub/f2.txt")));
	}

	@Test
	public void deleteFile_recursiveDirectory() {
		FileUtil.writeFile(path("del/a.txt"), "a");
		FileUtil.writeFile(path("del/deep/er/b.txt"), "b");
		FileUtil.deleteFile(path("del"));
		assertFalse(FileUtil.isExistFile(path("del")));
		FileUtil.deleteFile(path("does-not-exist")); // must not throw
	}

	@Test
	public void listDir_listsChildrenAndClearsList() {
		FileUtil.writeFile(path("ls/one"), "");
		FileUtil.writeFile(path("ls/two"), "");
		ArrayList<String> out = new ArrayList<>();
		out.add("stale");
		FileUtil.listDir(path("ls"), out);
		assertEquals(2, out.size());
		assertFalse(out.contains("stale"));
	}

	@Test
	public void lengthAndTypeOfMissingPath() {
		assertEquals(0, FileUtil.getFileLength(path("nothing")));
		assertFalse(FileUtil.isFile(path("nothing")));
		assertFalse(FileUtil.isDirectory(path("nothing")));
	}

	@Test
	public void calculateInSampleSize_powersOfTwo() {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.outWidth = 4000;
		o.outHeight = 3000;
		assertEquals(1, FileUtil.calculateInSampleSize(o, 4000, 3000));
		assertEquals(4, FileUtil.calculateInSampleSize(o, 1000, 750));
		assertEquals(1, FileUtil.calculateInSampleSize(o, 8000, 8000));
	}
}
