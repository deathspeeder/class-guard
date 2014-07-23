/**
 * 
 */
package org.lumanmed.classguard;

import java.io.File;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * @author Willard Wang
 *
 */
public class TargetClassNameFilterTest extends TestCase {

    @Test
    public void testAccept() {
        TargetClassNameFilter filter = new TargetClassNameFilter("com.test,com/july");
        assertTrue(filter.accept(new File("/build/com/test"), "Anything"));
        assertTrue(filter.accept(new File("/build/com/july"), "Anything"));
        assertTrue(filter.accept(new File("/build/com/test/"), "Anything"));
        assertTrue(filter.accept(new File("/build/com/test/log"), "LogFactory.class"));
    }
    
    @Test
    public void testReject() {
        TargetClassNameFilter filter = new TargetClassNameFilter("com.test,com/july");
        assertFalse(filter.accept(new File("/build/com/love"), "Anything"));
        assertFalse(filter.accept(new File("/build/com/"), "Anything"));
        assertFalse(filter.accept(new File("/build"), "Anything"));
    }
}
