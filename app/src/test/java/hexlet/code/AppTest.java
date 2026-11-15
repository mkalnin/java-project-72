package hexlet.code;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import io.javalin.testtools.TestCase;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.sql.SQLException;

public abstract class AppTest {
    protected Javalin app;

    @BeforeEach
    public void setUp() throws SQLException, IOException {
        Database.reset();
        app = App.getApp();
    }

    protected void runTest(TestCase testCase) {
        JavalinTest.test(app, testCase);
    }
}
