package hvl.projectparmorel.knowledge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActionMapTest {
	
	ActionMap<Double> actionMap;
	
	@BeforeEach
	public void setUp() {
		actionMap = new ActionMap<>();
		actionMap.setValue(45, 1.5);
		actionMap.setValue(12, 9.4);
		actionMap.setValue(7, 3.6);
		actionMap.setValue(315, 0.1);
		actionMap.setValue(4, 7.2);
	}

	@Test
	public void getHihgestValueKeyReturnsKeyForHihgestValue() {
		Integer highestActionId = actionMap.getHihgestValueKey();
		assertNotNull(highestActionId);
		assertEquals(12, highestActionId.intValue());
	}
	
	@Test
	public void getHihgestValueKeyReturnsKeyForOneOfTheHighestValuesIfTwoAreEqual() {
		actionMap.setValue(5, 9.4);
		Integer highestActionId = actionMap.getHihgestValueKey();
		assertNotNull(highestActionId);
		assertNotEquals(45, highestActionId.intValue());
		assertNotEquals(7, highestActionId.intValue());
		assertNotEquals(315, highestActionId.intValue());
		assertNotEquals(4, highestActionId.intValue());
	}

}
