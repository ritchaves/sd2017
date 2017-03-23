package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class BuyProductIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
		// clear remote service state before all tests
		client.clear();

		// fill-in test products
		// (since getProduct is read-only the initialization below
		// can be done once for all tests in this suite)
		{
			ProductView product = new ProductView();
			product.setId("XPTO1");
			product.setDesc("Guitar");
			product.setPrice(599);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("XPTO2");
			product.setDesc("Violin");
			product.setPrice(679);
			product.setQuantity(1);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("XPTO3");
			product.setDesc("Cello");
			product.setPrice(1059);
			product.setQuantity(100);
			client.createProduct(product);
		}
	}

	@AfterClass
	public static void oneTimeTearDown() {
		// clear remote service state after all tests
		client.clear();
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	// tests
	// assertEquals(expected, actual);

	// public String buyProduct(String productId, int quantity)
	// throws BadProductId_Exception, BadQuantity_Exception,
	// InsufficientQuantity_Exception {

	// bad input tests

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNullTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(null, 2);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductEmptyTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("", 2);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductWhitespaceTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(" ", 2);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductTabTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\t", 2);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNewlineTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\n", 2);
	}
	//FIXME ************************
	
	@Test(expected = BadProductId_Exception.class)
	public void buyProductNonAlphaNumericTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("XPTO 2", 2);
	}
	@Test(expected = BadProductId_Exception.class)
	public void buyProductOtherNonAlphaNumericTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("XPTO-2", 2);
	}
	@Test(expected = BadProductId_Exception.class)
	public void buyProductYetAnotherNonAlphaNumericTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("XPTO.2", 2);
	}
	
	//**************************
	@Test(expected = BadQuantity_Exception.class)
	public void buyProductNegativeQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("XPTO1", -2);
	}
	
	@Test(expected = BadQuantity_Exception.class)
	public void buyProductZeroTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("XPTO1", 0);
	}
		

	// main tests
	
	//Purchase ok
	@Test
	public void buyProductOKTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String purchaseId = client.buyProduct("XPTO1", 1);
		assertEquals("1", purchaseId);
	}
	
	//Purchase ok
	@Test
	public void buyProductOKAgainTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String purchaseId = client.buyProduct("XPTO1", 3);
		assertEquals("2", purchaseId);
	}
	//Purchase ok
	@Test
	public void buyProductOKBuyEverythingTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String purchaseId = client.buyProduct("XPTO3", 100);
		assertEquals("3", purchaseId);
	}
	
	
	//Purchase not ok - 0 in stock
	@Test (expected = InsufficientQuantity_Exception.class)
	public void buyProductWithZeroQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("XPTO2", 1);
		client.buyProduct("XPTO2", 1);
	}
	
	//Purchase not ok - 0 in stock
	@Test
	public void buyProductCaseSensitiveTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String product = client.buyProduct("xpto3", 2);
		assertNull(product);
	}
	
	//Purchase not ok - insufficient in stock
	@Test(expected = InsufficientQuantity_Exception.class)
	public void buyProductInsufficientQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("XPTO1", 7);
	}
	
	@Test
	public void buyProductDoesNotExistTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		// when product does not exist, null should be returned
		String product = client.buyProduct("A0", 5);
		assertNull(product);
	}
}
