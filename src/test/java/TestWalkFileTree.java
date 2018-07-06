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
import java.util.EnumSet;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestWalkFileTree {
    @Rule
    public TemporaryFolder workdir = new TemporaryFolder();

    @Test
    public void testAbsolute() throws IOException {
        System.out.printf("<%s>\n", Paths.get("/"));
        System.out.printf("<%s>\n", Paths.get("/").normalize());
        System.out.printf("<%s>\n", Paths.get("/").normalize().isAbsolute());
        System.out.printf("<%s>\n", Paths.get("/").normalize().getRoot());
        for (final Path pathElement : Paths.get("/")) {
            System.out.printf("  <%s>\n", pathElement);
            System.out.printf("  <%s>\n", pathElement.normalize());
            System.out.printf("  <%s>\n", pathElement.isAbsolute());
            System.out.printf("  <%s>\n", pathElement.getRoot());
        }
        System.out.printf("<%s>\n", Paths.get("/foo"));
        System.out.printf("<%s>\n", Paths.get("/foo").normalize());
        System.out.printf("<%s>\n", Paths.get("/foo").normalize().isAbsolute());
        System.out.printf("<%s>\n", Paths.get("/foo").normalize().getRoot());
        for (final Path pathElement : Paths.get("/foo")) {
            System.out.printf("  <%s>\n", pathElement);
            System.out.printf("  <%s>\n", pathElement.normalize());
            System.out.printf("  <%s>\n", pathElement.isAbsolute());
            System.out.printf("  <%s>\n", pathElement.getRoot());
        }
        this.workdir.newFolder("foo");
        System.out.printf("<%s>\n", ofWorkdir("foo"));
        System.out.printf("<%s>\n", ofWorkdir("foo").normalize());
        System.out.printf("<%s>\n", ofWorkdir("foo").normalize().isAbsolute());
        System.out.printf("<%s>\n", ofWorkdir("foo").normalize().getRoot());
        for (final Path pathElement : ofWorkdir("foo")) {
            System.out.printf("  <%s>\n", pathElement);
            System.out.printf("  <%s>\n", pathElement.normalize());
            System.out.printf("  <%s>\n", pathElement.isAbsolute());
            System.out.printf("  <%s>\n", pathElement.getRoot());
        }
    }

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

    @Test
    public void testCaseSensitivity() throws IOException {
        // From the root.
        this.workdir.newFolder("FoO");
        this.workdir.newFolder("FoO", "bAr");
        this.workdir.newFolder("FoO", "bAr", "Baz");
        System.out.printf("<%s>\n", ofWorkdir("FoO"));
        System.out.printf("<%s>\n", ofWorkdir("FoO/bAr"));
        System.out.printf("<%s>\n", ofWorkdir("FoO/bAr/Baz"));
        System.out.printf("<%s>\n", ofWorkdir("foo"));
        System.out.printf("<%s>\n", ofWorkdir("foo/bar"));
        System.out.printf("<%s>\n", ofWorkdir("foo/bar/baz"));
        System.out.printf("<%s>\n", getCaseSensitivePathOfDirectory(ofWorkdir("foo")));
        System.out.printf("<%s>\n", getCaseSensitivePathOfDirectory(ofWorkdir("foo/bar")));
        System.out.printf("<%s>\n", getCaseSensitivePathOfDirectory(ofWorkdir("foo/bar/baz")));

        // From the current working directory.
        Files.createDirectory(Paths.get("fOo"));
        Files.createDirectory(Paths.get("fOo", "Bar"));
        Files.createDirectory(Paths.get("fOo", "Bar", "baZ"));
        System.out.printf("<%s>\n", getCaseSensitivePathOfDirectory(Paths.get("foo")));
        System.out.printf("<%s>\n", getCaseSensitivePathOfDirectory(Paths.get("foo", "bar")));
        System.out.printf("<%s>\n", getCaseSensitivePathOfDirectory(Paths.get("foo", "bar", "baz")));
        Files.delete(Paths.get("fOo", "Bar", "baZ"));
        Files.delete(Paths.get("fOo", "Bar"));
        Files.delete(Paths.get("fOo"));
    }

    private Path ofWorkdir(final String subPath) {
        return Paths.get(this.workdir.getRoot().getPath()).resolve(subPath);
    }

    private static Path getCaseSensitivePathOfDirectory(final Path dirNormalized) throws IOException {
        Path built;
        if (dirNormalized.isAbsolute()) {
            built = Paths.get("/");
        } else {
            built = Paths.get("");
        }
        for (final Path pathElement : dirNormalized) {
            if (pathElement.equals(PARENT)) {
                built = built.resolve(PARENT);
                continue;
            }

            final ArrayList<Path> found = new ArrayList<>();
            // FOLLOW_LINKS is required to visit case-insensitive files.
            Files.walkFileTree(built, FOLLOW_LINKS, 2, new CaseInsensitiveFileVisitor(built, pathElement, found));
            if (found.size() == 1) {
                built = found.get(0);
            } else if (found.size() > 1) {
                // If multiple paths are found, take the original. It means that the file system is case sensitive.
                built = built.resolve(pathElement);
            } else {
                throw new FileNotFoundException("Directory not found: " + built.resolve(pathElement).toString());
            }
        }
        return built;
    }

    private static class CaseInsensitiveFileVisitor extends SimpleFileVisitor<Path> {
        public CaseInsensitiveFileVisitor(final Path startPath,
                                          final Path pathElementToMatchCaseInsensitive,
                                          final ArrayList<Path> matchedCaseInsensitive) {
            this.startPath = startPath;
            this.pathElementToMatchCaseInsensitiveString = pathElementToMatchCaseInsensitive.toString();
            this.matchedCaseInsensitive = matchedCaseInsensitive;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
            if (dir.equals(this.startPath)) {
                return FileVisitResult.CONTINUE;
            }
            if (dir.getFileName() == null) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            if (this.pathElementToMatchCaseInsensitiveString.equalsIgnoreCase(dir.getFileName().toString())) {
                this.matchedCaseInsensitive.add(dir);
            }
            return FileVisitResult.SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
            if (this.pathElementToMatchCaseInsensitiveString.equalsIgnoreCase(file.getFileName().toString())) {
                // It actually fails only when the failed file/directory is in interest.
                throw exc;
            } else {
                return FileVisitResult.SKIP_SUBTREE;
            }
        }

        private final Path startPath;
        private final String pathElementToMatchCaseInsensitiveString;
        private final ArrayList<Path> matchedCaseInsensitive;
    }

    private static final Path PARENT = Paths.get("..");
    private static final Set<FileVisitOption> FOLLOW_LINKS = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
}
