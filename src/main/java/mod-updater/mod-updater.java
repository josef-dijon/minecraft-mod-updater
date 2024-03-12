///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2024 Josef de Joanelli (josef@pixelrift.io)
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
///////////////////////////////////////////////////////////////////////////////
//
// Minecraft mod update.
//
// A windows powershell script that automates the synchronization of Minecraft
// mods, shaders, and resource packs by downloading, updating, and validating
// files from a manifest, to ensure an up-to-date Minecraft environment.
//
// Version: 0.1
// Changelog:
// [0.1] - Initial release
///////////////////////////////////////////////////////////////////////////////

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class ModUpdater {

	static class ModItem {
		String url;
		String name;
		String version;
		String filename;
		String md5;
		String destination;
		boolean server;
		boolean client;

		public void print() {
			System.out.println("[" + name + "]");
			System.out.println("    Filename: " + destination + "/" + filename);
			System.out.println("    Version: " + version);
		}
	}

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		System.out.println("##################################################################");
		System.out.println(" Minecraft Mod Updater v" + ModUpdater.class.getPackage().getImplementationVersion());
		System.out.println(" Contact: josef@pixelrift.io");
		System.out.println("##################################################################");

		if (args.length != 4) {
			System.out.println("Usage: ModUpdater --manifest-url <manifest URL> --destination-dir <destination directory>");
			System.exit(1);
		}

		String manifestUrl = new String();
		String destinationDir = new String();

		if (args[0] == "--manifest-url" && args[2] == "--destination-dir") {
			manifestUrl = args[1];
			destinationDir = args[3];
		}
		else if (args[2] == "--manifest-url" && args[0] == "--destination-dir") {
			manifestUrl = args[3];
			destinationDir = args[1];
		}
		else {
			System.out.println("Usage: ModUpdater --manifest-url <manifest URL> --destination-dir <destination directory>");
			System.exit(1);
		}

		// Download manifest
		String manifestJson = downloadManifest(manifestUrl);
		Gson gson = new Gson();
		List<ModItem> items = gson.fromJson(manifestJson, new TypeToken<List<ModItem>>(){}.getType());
		List<ModItem> updateList = new ArrayList<>();

		System.out.println("Mods from this manifest:");

		for (ModItem item : items) {
			item.print();

			if (! Files.exists(destinationPath)) {
				System.out.println("    Status: MISSING");
				updateList.add(item);
			}
			else if (! verifyFileMD5(filePath, item.md5)) {
				System.out.println("    Status: OUT OF DATE");
				updateList.add(item);
			}
			else {
				System.out.println("    Status: UP TO DATE");
			}
		}

		for (ModItem item : updateList) {
			Path destinationDir = Paths.get(destinationDir, item.destination);
			Path tmpPath = Paths.get(destinationDir, item.destination, item.filename + ".tmp");
			Path destinationPath = Paths.get(destinationDir, item.destination, item.filename);

			ensureDirectoryExists(destinationDir);

			System.out.println("Downloading " + item.name + "...");
			downloadFile(item.url, tmpPath);

			if (! verifyFileMD5(tmpPath, item.md5)) {
				System.err.println("Failed to verify " + item.filename + ".tmp.");
				Files.delete(tmpPath);
			}
			else {
				System.out.println("Successfully verified. Moving " + item.filename + ".tmp to " itme.filename);
				Files.move(tmpPath, destinationPath, Files.StandardCopyOption.REPLACE_EXISTING);
			}
		}

		System.out.println("Mod updates complete!");
	}

	private static String downloadManifest(String manifestUrl) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(manifestUrl).openConnection();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			return reader.lines().reduce("", String::concat);
		}
	}

	private static void downloadFile(String fileUrl, Path destination) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();

		try (InputStream in = connection.getInputStream(); OutputStream out = Files.newOutputStream(destination)) {
			in.transferTo(out);
		}
	}

	private static boolean needsUpdate(Path filePath, String expectedMD5) throws IOException, NoSuchAlgorithmException {
		if (! Files.exists(filePath)) {
			return true;
		}

		return ! verifyFileMD5(filePath, expectedMD5);
	}

	private static boolean verifyFileMD5(Path filePath, String expectedMD5) throws IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");

		md.update(Files.readAllBytes(filePath));

		byte[] digest = md.digest();
		StringBuilder sb = new StringBuilder();

		for (byte b : digest) {
			sb.append(String.format("%02x", b));
		}

		return sb.toString().equals(expectedMD5);
	}

	private static void ensureDirectoryExists(Path directory) throws IOException {
		if (! Files.exists(directory)) {
			Files.createDirectories(directory);
		}
	}
}
