import com.sae.moutonloup.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoupTest {

    @Test
    public void testVitesseLoup() {
        Loup l = new Loup(new Position(2, 2));
        assertEquals(3, l.getVitesse());
    }

    @Test
    public void testPositionInitiale() {
        Loup l = new Loup(new Position(2, 2));
        assertEquals(2, l.getPosition().getX());
        assertEquals(2, l.getPosition().getY());
    }

    @Test
    public void testChangementDePosition() {
        Loup l = new Loup(new Position(0, 0));
        Position nouvelle = new Position(4, 4);
        l.setPosition(nouvelle);
        assertEquals(nouvelle, l.getPosition());
    }


}
