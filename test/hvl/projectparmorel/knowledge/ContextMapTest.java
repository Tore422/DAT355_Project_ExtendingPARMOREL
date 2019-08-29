package hvl.projectparmorel.knowledge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContextMapTest {
	
	private ContextMap<Double> contextMap;
	
	@BeforeEach
	public void setUp() {
		contextMap = new ContextMap<>();
		contextMap.insertNewContext(0, 0, 5.0);
		contextMap.insertNewValueForContext(0, 1, 5.0);
		contextMap.insertNewValueForContext(0, 2, 7.0);
		contextMap.insertNewContext(1, 0, 9.0);
		contextMap.insertNewValueForContext(1, 1, 3.0);
		contextMap.insertNewValueForContext(1, 2, 2.0);
		contextMap.insertNewContext(2, 0, 2.0);
	}

	@Test
	public void getOptimalActionIndexReturnsOptimalIndexInCorrectContext() {
		ActionLocation location = contextMap.getOptimalActionLocation();
		assertNotNull(location);
		assertEquals(1, location.getContextId().intValue());
		assertEquals(0, location.getActionId().intValue());
	}

	@Test
	public void getOptimalActionIndexReturnsOneOfOptimalIndexesIfSeveralAreBest() {
		contextMap.insertNewContext(3, 0, 9.0);
		contextMap.insertNewValueForContext(3, 1, 4.0);
		contextMap.insertNewValueForContext(3, 2, 4.0);
		ActionLocation location = contextMap.getOptimalActionLocation();
		assertNotNull(location);
		assertNotEquals(0, location.getContextId().intValue());
		assertNotEquals(2, location.getContextId().intValue());
		assertNotEquals(1, location.getActionId().intValue());
		assertNotEquals(2, location.getActionId().intValue());
	}
}
