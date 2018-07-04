import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestWalkFileTree {
    @Rule
    public TemporaryFolder workdir = new TemporaryFolder();

    @Test
    public void test() throws IOException {
        this.workdir.newFile("file");
    }
}
