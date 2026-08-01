package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class CommonJsCacheBustContractTest {

	private static final String COMMON_JS = "resources/js/common.js";
	private static final String VERSIONED_COMMON_JS =
			COMMON_JS + "?v=20260801.1";

	@Test
	void everySharedCommonJsIncludeUsesTheCurrentReleaseVersion() throws Exception {
		List<Path> includes = new ArrayList<>();
		try (Stream<Path> files = Files.walk(Paths.get("src/main/webapp"))) {
			files.filter(path -> path.getFileName().toString().endsWith(".jsp"))
					.forEach(path -> {
						try {
							String source = new String(Files.readAllBytes(path),
									StandardCharsets.UTF_8);
							if (source.contains(COMMON_JS)) {
								includes.add(path);
								assertTrue(source.contains(VERSIONED_COMMON_JS),
										path + " must invalidate cached common.js releases");
							}
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					});
		}

		assertFalse(includes.isEmpty(), "No shared common.js includes were found");
	}
}
