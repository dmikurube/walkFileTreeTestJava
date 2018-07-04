import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestWalkFileTree {
    @Rule
    public TemporaryFolder workdir = new TemporaryFolder();

    @Test
    public void testNormalize() throws IOException {
        System.out.printf("<%s>\n", Paths.get(""));
        System.out.printf("<%s>\n", Paths.get("").normalize());
        System.out.printf("<%s>\n", Paths.get("../"));
        System.out.printf("<%s>\n", Paths.get("../").normalize());
        System.out.printf("<%s>\n", Paths.get("../foo/"));
        System.out.printf("<%s>\n", Paths.get("../foo/").normalize());
        System.out.printf("<%s>\n", Paths.get("foo/../"));
        System.out.printf("<%s>\n", Paths.get("foo/../").normalize());
        System.out.printf("<%s>\n", Paths.get("../foo/../bar"));
        System.out.printf("<%s>\n", Paths.get("../foo/../bar").normalize());
        System.out.printf("<%s>\n", Paths.get("foo/../bar/.."));
        System.out.printf("<%s>\n", Paths.get("foo/../bar/..").normalize());
        System.out.printf("<%s>\n", Paths.get("../foo/qux/../bar"));
        System.out.printf("<%s>\n", Paths.get("../foo/qux/../bar").normalize());
        System.out.printf("<%s>\n", Paths.get("bar/../foo/../qux/../../baz"));
        System.out.printf("<%s>\n", Paths.get("bar/../foo/../qux/../../baz").normalize());
    }

    @Test
    public void testCurrentDirectory() throws IOException {
        System.out.printf("<%s>\n", Paths.get(""));
        System.out.printf("<%s>\n", Paths.get("").normalize());
        System.out.printf("<%s>\n", Paths.get("").toAbsolutePath());
        System.out.printf("<%s>\n", Paths.get("").toFile().getCanonicalPath());
    }

    private static Path getCaseSensitivePathOfDirectory(
            final Path dirNormalized,
            final Set<FileVisitOption> visitOptions)
            throws IOException {
        Path built = Paths.get("");
        for (final Path pathElement : dirNormalized) {
            if (pathElement.equals(PARENT)) {
                built = built.resolve(PARENT);
                continue;
            }

            final String pathElementString = pathElement.toString();

            final ArrayList<Path> matchedCaseInsensitive = new ArrayList<>();
            Files.walkFileTree(built, visitOptions, 1, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                        if (pathElementString.equalsIgnoreCase(dir.getFileName().toString())) {
                            matchedCaseInsensitive.add(dir);
                        }
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                });
            if (matchedCaseInsensitive.size() == 1) {
                built = matchedCaseInsensitive.get(0);
            } else if (matchedCaseInsensitive.size() > 1) {
                // If multiple paths are found, it means that the file system is case sensitive.
                built = built.resolve(pathElement);
            } else {
                throw new FileNotFoundException("Directory not found: " + built.resolve(pathElement).toString());
            }
        }
        return built;
    }

    private static final Path PARENT = Paths.get("..");
}
