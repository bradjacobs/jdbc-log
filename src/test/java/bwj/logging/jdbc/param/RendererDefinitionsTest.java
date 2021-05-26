package bwj.logging.jdbc.param;


import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class RendererDefinitionsTest
{
    @Test
    public void testHasNullFields() throws Exception
    {
        RendererDefinitions rendereDefinitions = RendererDefinitionsFactory.createDefaultDefinitions(null);

        assertNotNull(rendereDefinitions);
        assertFalse(rendereDefinitions.hasNullRenderers());

        rendereDefinitions.setStringRenderer(null);
        assertTrue(rendereDefinitions.hasNullRenderers());
    }
}
