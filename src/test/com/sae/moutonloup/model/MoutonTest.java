package com.sae.moutonloup.model;

import com.sae.moutonloup.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MoutonTest {

    @Test
    public void testMangerHerbe() {
        Mouton m = new Mouton(new Position(0, 0));
        m.manger(new Herbe());
        assertEquals(2, m.getVitesse());
    }

    @Test
    public void testMangerMarguerite() {
        Mouton m = new Mouton(new Position(0, 0));
        m.manger(new Marguerite());
        assertEquals(4, m.getVitesse());
    }

    @Test
    public void testMangerCactus() {
        Mouton m = new Mouton(new Position(0, 0));
        m.manger(new Cactus());
        assertEquals(1, m.getVitesse());
    }

}
