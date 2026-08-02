package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class CommonJsCacheBustContractTest {

	private static final String RELEASE_VERSION = "?v=20260802.1";
	private static final List<String> SHARED_SCRIPTS = Arrays.asList(
			"resources/js/common.js",
			"resources/js/common_dialog.js",
			"resources/js/i18n/common_i18n.js");

	@Test
	void everySharedScriptIncludeUsesTheCurrentReleaseVersion() throws Exception {
		Map<String, Integer> includeCounts = new LinkedHashMap<>();
		SHARED_SCRIPTS.forEach(script -> includeCounts.put(script, 0));
		try (Stream<Path> files = Files.walk(Paths.get("src/main/webapp"))) {
			files.filter(path -> path.getFileName().toString().endsWith(".jsp"))
					.forEach(path -> {
						try {
							String source = new String(Files.readAllBytes(path),
									StandardCharsets.UTF_8);
							SHARED_SCRIPTS.forEach(script -> {
								if (source.contains(script)) {
									includeCounts.put(script, includeCounts.get(script) + 1);
									assertTrue(source.contains(script + RELEASE_VERSION),
											path + " must invalidate cached " + script);
								}
							});
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					});
		}

		includeCounts.forEach((script, count) -> assertFalse(count == 0,
				"No shared script includes were found for " + script));
	}
}
