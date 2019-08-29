package hvl.projectparmorel.knowledge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HashErrorContextActionDirectoryTest {

	ErrorContextActionDirectory<Integer> directory;
	
	@BeforeEach
	public void setUp() {
		directory = new HashErrorContextActionDirectory<>();
		directory.setValue(0, 0, 24, 10);
	}
	
	@Test
	public void getRandomValueForWithOneValueReturnsThatValue() {
		Integer result = directory.getRandomValueForError(0);
		assertNotNull(result);
		assertEquals(10, result.intValue());
	}

}
