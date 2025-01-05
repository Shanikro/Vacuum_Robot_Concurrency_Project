import bgu.spl.mics.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;

public class MessageBusTest {

    private MessageBusImpl messageBus;
    private MicroService m1;
    private MicroService m2;

    @BeforeEach
    public void setUp() {
        messageBus = MessageBusImpl.getInstance();
        m1 = new MicroService("Service1") {
            @Override
            protected void initialize() {}
        };
        m2 = new MicroService("Service2") {
            @Override
            protected void initialize() {}
        };
        messageBus.register(m1);
        messageBus.register(m2);
    }

    @Test
    public void testRegisterAndUnregister() {
        messageBus.unregister(m1);
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(m1));
    }

    @Test
    public void testSubscribeEvent() {
        class TestEvent implements Event<String> {}

        messageBus.subscribeEvent(TestEvent.class, m1);
        assertDoesNotThrow(() -> messageBus.sendEvent(new TestEvent()));
    }

    @Test
    public void testSubscribeBroadcast() {
        class TestBroadcast implements Broadcast {}

        messageBus.subscribeBroadcast(TestBroadcast.class, m1);
        messageBus.sendBroadcast(new TestBroadcast());

        assertDoesNotThrow(() -> messageBus.awaitMessage(m1));
    }

    @Test
    public void testSendEvent() throws InterruptedException {
        class TestEvent implements Event<String> {}

        messageBus.subscribeEvent(TestEvent.class, m1);
        Future<String> future = messageBus.sendEvent(new TestEvent());
        assertNotNull(future, "Future should not be null");
    }

    @Test
    public void testSendBroadcast() throws InterruptedException {
        class TestBroadcast implements Broadcast {}

        messageBus.subscribeBroadcast(TestBroadcast.class, m1);
        messageBus.subscribeBroadcast(TestBroadcast.class, m2);

        messageBus.sendBroadcast(new TestBroadcast());

        assertNotNull(messageBus.awaitMessage(m1));
        assertNotNull(messageBus.awaitMessage(m2));
    }

    @Test
    public void testComplete() {
        class TestEvent implements Event<String> {}

        messageBus.subscribeEvent(TestEvent.class, m1);
        TestEvent event = new TestEvent();
        Future<String> future = messageBus.sendEvent(event);
        messageBus.complete(event, "Success");

        assertTrue(future.isDone(), "Future should be completed");
        assertEquals("Success", future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void testAwaitMessage() throws InterruptedException {
        class TestBroadcast implements Broadcast {}

        messageBus.subscribeBroadcast(TestBroadcast.class, m1);
        messageBus.sendBroadcast(new TestBroadcast());

        Message msg = messageBus.awaitMessage(m1);
        assertTrue(msg instanceof TestBroadcast, "Should receive TestBroadcast message");
    }
}
