package hvl.projectparmorel.knowledge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hvl.projectparmorel.general.Action;

class HashErrorContextActionDirectoryTest {
	private ErrorContextActionDirectory directory;
	
	@BeforeEach
	public void setUp() {
		directory = new HashErrorContextActionDirectory();
	}
	
	@Test
	public void ANewDirectoryIsEmtpy() {
		assertTrue(directory.getAllErrorCodes().isEmpty());
	}
	
	@Test
	public void addActionAddsAnActionToTheDirectory() {
		directory.addAction(1, 1, new Action());
		assertFalse(directory.getAllErrorCodes().isEmpty());
	}
	
	@Test
	public void addActionWithCodeThatAllreadyExistsUpdatesTheAction() {
		assertEquals(0, directory.getAllErrorCodes().size());
		Action action1 = new Action(10, "Hello", null, 1);
		directory.addAction(1, 1, action1);
		assertEquals(1, directory.getAllErrorCodes().size());
		Action action2 = new Action(10, "Updated", null, 2);
		directory.addAction(1, 1, action2);
		assertEquals(1, directory.getAllErrorCodes().size());
	}
	
	@Test
	public void addActionWithCodeThatAllreadyButDifferentErrorCodeExistsAddsTheAction() {
		assertEquals(0, directory.getAllErrorCodes().size());
		Action action1 = new Action(10, "Hello", null, 1);
		directory.addAction(1, 1, action1);
		assertEquals(1, directory.getAllErrorCodes().size());
		Action action2 = new Action(10, "Updated", null, 2);
		directory.addAction(2, 1, action2);
		assertEquals(2, directory.getAllErrorCodes().size());
	}

	/*
	 * This needs to be finished after adding a mocking framework, as the method has to have an equals message for actions to compare correctly 
	 */
//	@Test
//	public void getOptimalActionForErrorCodeReturnsTheActionWithHighestWeightForErrorCode() {
//		Action action1 = new Action(0, "msg1", null, 0);
//		Action action2 = new Action(1, "msg2", null, 1);
//		Action action3 = new Action(2, "msg3", null, 2);
//		Action action4 = new Action(3, "msg4", null, 3);
//		Action action5 = new Action(4, "msg15", null, 4);
//		action1.setCode(1);
//		action2.setCode(2);
//		action3.setCode(3);
//		action4.setCode(4);
//		action5.setCode(5);
//		action1.setWeight(100);
//		action2.setWeight(230);
//		action3.setWeight(901);
//		action4.setWeight(0);
//		action5.setWeight(-1000);
//		
//		directory.addAction(1, 1, action1);
//		directory.addAction(1, 3, action2);
//		directory.addAction(2, 1, action3);
//		directory.addAction(1, 2, action4);
//		directory.addAction(1, 1, action5);
//		
//		assertEquals(action2, directory.getOptimalActionForErrorCode(1));
//		
//	}
	
}
