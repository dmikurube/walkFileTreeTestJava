import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
}
