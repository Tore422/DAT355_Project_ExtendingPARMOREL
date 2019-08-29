package hvl.projectparmorel.knowledge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActionMapTest {
	
	ActionMap<Double> actionMap;
	
	@BeforeEach
	public void setUp() {
		actionMap = new ActionMap<>();
		actionMap.addValue(45, 1.5);
		actionMap.addValue(12, 9.4);
		actionMap.addValue(7, 3.6);
		actionMap.addValue(315, 0.1);
		actionMap.addValue(4, 7.2);
	}

	@Test
	public void getHihgestValueKeyReturnsKeyForHihgestValue() {
		Integer highestActionId = actionMap.getHihgestValueKey();
		assertNotNull(highestActionId);
		assertEquals(12, highestActionId.intValue());
	}
	
	@Test
	public void getHihgestValueKeyReturnsKeyForOneOfTheHighestValuesIfTwoAreEqual() {
		actionMap.addValue(5, 9.4);
		Integer highestActionId = actionMap.getHihgestValueKey();
		assertNotNull(highestActionId);
		assertNotEquals(45, highestActionId.intValue());
		assertNotEquals(7, highestActionId.intValue());
		assertNotEquals(315, highestActionId.intValue());
		assertNotEquals(4, highestActionId.intValue());
	}

}
