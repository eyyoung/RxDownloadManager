package com.nd.sdp.android.options;

import com.nd.android.sdp.dm.options.DefaultConflictStragedy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by young on 2015/9/13.
 */
public class DefaultConflictStragedyTest {

    @Test
    public void testGetNewName() {
        testNewName("test(1).jpg", "test(2).jpg");
    }

    @Test
    public void testGetNewName2() {
        testNewName("test(1)(1).jpg", "test(1)(2).jpg");
    }

    @Test
    public void testGetNewName3() {
        String old3 = "test.jpg";
        String actual = "test(1).jpg";
        testNewName(old3, actual);
    }

    @Test
    public void testGetNewName4() {
        String old3 = ".jpg";
        String actual = "(1).jpg";
        testNewName(old3, actual);
    }

    @Test
    public void testGetNewName5() {
        String old3 = "(1).(1).jpg";
        String actual = "(1).(2).jpg";
        testNewName(old3, actual);
    }

    private void testNewName(String pOld, String pActual) {
        DefaultConflictStragedy stragedy = new DefaultConflictStragedy();
        String newName = stragedy.getNewName(pOld);
        assertEquals(newName, pActual);
    }

}
