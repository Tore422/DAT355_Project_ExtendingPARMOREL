package no.hvl.projectparmorel.qlearning.knowledge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.ecore.EcoreAction;

class ActionTest {
	Action action1;
	Action action2;
	
	@BeforeEach
	public void setUp() {
		action1 = new EcoreAction();
		action2 = new EcoreAction();
	}

	@Test
	public void twoActionsWithEqualWeightsComparesToZero() {
		action1.setWeight(100);
		action2.setWeight(100);
		assertEquals(0, action1.compareTo(action2));
	}
	
	@Test
	public void actionWithSmallerWeightThanOtherReturnsLessThanZero() {
		action1.setWeight(100);
		action2.setWeight(200);
		assertTrue(action1.compareTo(action2) < 0);
	}

	@Test
	public void actionWithGreaterWeightThanOtherReturnsMoreThanZero() {
		action1.setWeight(200);
		action2.setWeight(100);
		assertTrue(action1.compareTo(action2) > 0);
	}
	
	@Test
	public void methodIsDeleteIfStartsWith9999() {
		action1.setId(99991);
		assertTrue(action1.isDelete());
	}


	@Test
	public void methodIsNotDeleteIfOnlyStartsWith999() {
		action1.setId(9991);
		assertFalse(action1.isDelete());
	}
}
