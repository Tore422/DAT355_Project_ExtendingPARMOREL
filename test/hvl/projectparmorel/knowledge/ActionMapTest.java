package hvl.projectparmorel.knowledge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActionMapTest {
	
	ActionMap<Double> actionMap;
	
	@BeforeEach
	public void setUp() {
		actionMap = new ActionMap<>();
		actionMap.addValue(0, 1.5);
		actionMap.addValue(1, 9.4);
		actionMap.addValue(2, 3.6);
		actionMap.addValue(3, 0.1);
		actionMap.addValue(4, 7.2);
	}

	@Test
	public void getHihgestValueKeyReturnsKeyForHihgestValue() {
		Integer highestActionId = actionMap.getHihgestValueKey();
		assertNotNull(highestActionId);
		assertEquals(1, highestActionId.intValue());
	}
	
	@Test
	public void getHihgestValueKeyReturnsKeyForOneOfTheHighestValuesIfTwoAreEqual() {
		actionMap.addValue(5, 9.4);
		Integer highestActionId = actionMap.getHihgestValueKey();
		assertNotNull(highestActionId);
		assertNotEquals(0, highestActionId.intValue());
		assertNotEquals(2, highestActionId.intValue());
		assertNotEquals(3, highestActionId.intValue());
		assertNotEquals(4, highestActionId.intValue());
	}

}
