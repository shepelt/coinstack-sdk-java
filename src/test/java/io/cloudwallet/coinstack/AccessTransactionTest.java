package io.cloudwallet.coinstack;

import static org.junit.Assert.*;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class AccessTransactionTest {

	@Test
	public void testScript() throws Exception {
		String rawScript = "6a09746573742064617461";
		Output testOutput = new Output("test", 0, "testaddress", false, 0l, rawScript);
		byte[] data = testOutput.getData();
		assertNotNull(data);
		String dataString = Hex.encodeHexString(data);
		System.out.println(dataString);
		
		testOutput = new Output("test", 0, "testaddress", false, 0l, "76a9147972b245454f8ef1a7c543878225cf14779d04ed88ac");
		assertNull(testOutput.getData());
	}

}
